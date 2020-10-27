package com.vivi.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.constant.ProductConstant;
import com.vivi.common.to.SkuDiscountTO;
import com.vivi.common.to.SkuESModel;
import com.vivi.common.to.SkuStockTO;
import com.vivi.common.to.SpuBoundsTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.common.utils.R;
import com.vivi.gulimall.product.dao.SpuInfoDao;
import com.vivi.gulimall.product.entity.*;
import com.vivi.gulimall.product.feign.CouponFeignService;
import com.vivi.gulimall.product.feign.SearchFeignService;
import com.vivi.gulimall.product.feign.WareFeignService;
import com.vivi.gulimall.product.service.*;
import com.vivi.gulimall.product.vo.SpuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );
        return new PageUtils(page);
    }

    // TODO 高级部分完善,事务回滚
    @Transactional
    @Override
    public boolean save(SpuVO spuVO) {
        // System.out.println(spuVO);
        // 1. 保存spu基本信息 pms->pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuVO, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);

        Long spuId = spuInfoEntity.getId();

        // 2. spu的介绍图片信息 pms->pms_spu_info_desc
        List<String> descript = spuVO.getDescript();
        if (!CollectionUtils.isEmpty(descript)) {
            String desUrlPath = String.join(",", descript);
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setDescript(desUrlPath);
            spuInfoDescEntity.setSpuId(spuId);
            spuInfoDescService.save(spuInfoDescEntity);
        }

        // 3. spu的图片合集信息 pms->pms_spu_images
        List<String> images = spuVO.getImages();
        if (!CollectionUtils.isEmpty(images)) {
            List<SpuImagesEntity> imagesEntities = images.stream().map(image -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuId);
                spuImagesEntity.setImgUrl(image);
                return spuImagesEntity;
            }).filter(image -> !StringUtils.isEmpty(image.getImgUrl())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(imagesEntities)) {
                spuImagesService.saveBatch(imagesEntities);
            }
        }

        // 4. spu的规格参数信息 pms->pms_product_attr_value
        List<SpuVO.BaseAttrs> baseAttrs = spuVO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                AttrEntity attrEntity = attrService.getById(baseAttr.getAttrId());
                productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                productAttrValueEntity.setAttrName(attrEntity.getAttrName());
                productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                productAttrValueEntity.setSpuId(spuId);
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(productAttrValueEntities);
        }

        // 5. spu的积分奖励策略 sms->sms_spu_bounds
        SpuVO.Bounds spuVOBounds = spuVO.getBounds();
        SpuBoundsTO spuBoundsTO = new SpuBoundsTO();
        BeanUtils.copyProperties(spuVOBounds, spuBoundsTO);
        spuBoundsTO.setSpuId(spuId);
        R r = couponFeignService.saveSpuBounds(spuBoundsTO);
        if(r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }

        // 6. spu下的所有sku信息
        List<SpuVO.Sku> skus = spuVO.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            for (SpuVO.Sku sku : skus) {
                // 6.1 sku基本信息 pms->pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setBrandId(spuVO.getBrandId());
                skuInfoEntity.setCatelogId(spuVO.getCatelogId());
                skuInfoEntity.setSkuDesc(String.join(",", sku.getDescar()));
                String defaultImg = "";
                for (SpuVO.Images image : sku.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                // 6.2 sku图集 pms->pms_sku_images
                List<SpuVO.Images> imagesList = sku.getImages();
                if (!CollectionUtils.isEmpty(imagesList)) {
                    List<SkuImagesEntity> imagesEntities = imagesList.stream().map(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        BeanUtils.copyProperties(image, skuImagesEntity);
                        skuImagesEntity.setSkuId(skuId);
                        return skuImagesEntity;
                    }).filter(entity->{
                        //返回true就是需要，false就是剔除
                        return !StringUtils.isEmpty(entity.getImgUrl());
                    }).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(imagesEntities)) {
                        skuImagesService.saveBatch(imagesEntities);
                    }
                }
                // 6.3 sku销售属性集 pms->pms_sku_sale_attr_value
                List<SpuVO.Attr> attrList = sku.getAttr();
                if (!CollectionUtils.isEmpty(attrList)) {
                    List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attrList.stream().map(attr -> {
                        SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                        BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                        skuSaleAttrValueEntity.setSkuId(skuId);
                        return skuSaleAttrValueEntity;
                    }).collect(Collectors.toList());
                    skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);
                }
                // 6.4 sku打折策略 sms_sku_ladder
                // 6.5 sku满减策略 sms->sms_sku_full_reduction
                // 6.6 sku的会员价格 sms->sms_member_price
                SkuDiscountTO skuDiscountTO = new SkuDiscountTO();
                BeanUtils.copyProperties(sku, skuDiscountTO);
                skuDiscountTO.setSkuId(skuId);
                R r1 = couponFeignService.saveDiscount(skuDiscountTO);
                if(r1.getCode() != 0){
                    log.error("远程保存sku优惠信息失败");
                }
            }
        }
        return true;
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        // 查询关键字
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).or().like("spu_name", key);
            });
        }
        // 指定分类
        String catelogId = (String) params.get("catelogId");
        if (isValidId(catelogId)) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        // 指定品牌
        String brandId = (String) params.get("brandId");
        if (isValidId(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        // 指定发布状态
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public boolean statusUp(Long id) {
        // 2.5 设置检索属性集，由于所有sku公用一个spu。所以可以先查出attrs。后直接设置。避免多次查询
        // 查出该spu所有规格属性中可以用于检索的那些属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.listForSpu(id);
        List<SkuESModel.Attrs> attrs = null;
        if (!CollectionUtils.isEmpty(productAttrValueEntities)) {
            // 先拿到id集合
            List<Long> ids = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
            // 再根据id去得到这些attr的详细信息
            List<AttrEntity> searchAttrs = attrService.listSearchAttrByIds(ids);
            // 从中得到可以用于检索的那些attrId
            List<Long> searchIds = searchAttrs.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
            Set<Long> searchAttrIdSet = new HashSet<>(searchIds);
            // 再根据这个id集合，去过滤出刚查出的此spu下的所有attr。选出可检索的那些
            attrs = productAttrValueEntities.stream().filter(item -> searchAttrIdSet.contains(item.getAttrId())).map(item -> {
                SkuESModel.Attrs attr = new SkuESModel.Attrs();
                attr.setAttrId(item.getAttrId());
                attr.setAttrName(item.getAttrName());
                attr.setAttrValue(item.getAttrValue());
                // 转成SkuESmodel中需要的数据
                return attr;
            }).collect(Collectors.toList());
        }
        // 1. 查出该spu下所有sku
        List<SkuInfoEntity> skuList = skuInfoService.listBySpuId(id);
        // 2. 将sku -> skuESModel
        if (!CollectionUtils.isEmpty(skuList)) {
            // 2.6 调用gulimall-ware服务查询此sku是否有库存
            // 如果每个sku都去调用一次远程服务，消耗太大，让远程服务一次查出全部sku的库存信息
            Map<Long, Long> stockMap = new HashMap<>();
            List<Long> ids = skuList.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            try {
                R r = wareFeignService.skuStock(ids);
                if (r.getCode() == 0) {
                    List<SkuStockTO> stockTOList = r.getData(new TypeReference<List<SkuStockTO>>() {
                    });
                    stockMap = stockTOList.stream().collect(Collectors.toMap(SkuStockTO::getSkuId, SkuStockTO::getStock));
                }
            } catch (Exception e) {
                log.error("调用gulimall-ware查询商品库存失败：{}", e);
            }
            List<SkuESModel.Attrs> finalAttrs = attrs;
            Map<Long, Long> finalStockMap = stockMap;
            List<SkuESModel> collect = skuList.stream().map(sku -> {
                SkuESModel skuESModel = new SkuESModel();
                // 2.1 基本属性拷贝
                BeanUtils.copyProperties(sku, skuESModel);

                // 2.2 不同属性名处理
                // skuImg skuPrice hotScore hasStock brandName brandImg catelogName attrs
                skuESModel.setSkuPrice(sku.getPrice());
                skuESModel.setSkuImg(sku.getSkuDefaultImg());
                skuESModel.setHotScore(0L);
                // 2.3 设置brand相关内容
                BrandEntity brandEntity = brandService.getById(sku.getBrandId());
                if (brandEntity != null) {
                    skuESModel.setBrandName(brandEntity.getName());
                    skuESModel.setBrandImg(brandEntity.getLogo());
                }
                // 2.4 设置category相关内容
                CategoryEntity categoryEntity = categoryService.getById(sku.getCatelogId());
                if (categoryEntity != null) {
                    skuESModel.setCatelogName(categoryEntity.getName());
                }
                // 2.5 设置检索属性集
                skuESModel.setAttrs(finalAttrs);
                // 2.6 调用gulimall-ware服务查询此sku是否有库存
                Long stock = finalStockMap.getOrDefault(sku.getSkuId(), 0L);
                skuESModel.setHasStock(stock > 0 ? true : false);
                return skuESModel;
            }).collect(Collectors.toList());
            // 3. 调用gulimall-search服务将List<SkuESModel>存储
            searchFeignService.batchSaveSku(collect);
        }
        // 4. 修改该spu的状态为已上架
        updateStatus(id, ProductConstant.SpuPublishStatus.UP.getValue());
        return true;
    }

    @Override
    public boolean updateStatus(Long spuId, Integer publishStatus) {
        this.baseMapper.updateStatus(spuId, publishStatus);
        return false;
    }

    private boolean isValidId(String key) {
        return !StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(key);
    }

}