package com.vivi.gulimall.product.service.impl;

import ch.qos.logback.core.joran.action.IADataForComplexProperty;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.to.SkuDiscountTO;
import com.vivi.common.to.SpuBoundsTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.common.utils.R;
import com.vivi.gulimall.product.dao.SpuImagesDao;
import com.vivi.gulimall.product.dao.SpuInfoDao;
import com.vivi.gulimall.product.dao.SpuInfoDescDao;
import com.vivi.gulimall.product.entity.*;
import com.vivi.gulimall.product.feign.CouponFeignService;
import com.vivi.gulimall.product.service.*;
import com.vivi.gulimall.product.vo.SpuVO;
import org.bouncycastle.asn1.esf.SPUserNotice;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import org.hibernate.validator.internal.constraintvalidators.hv.ISBNValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.WebSphereUowTransactionManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private boolean isValidId(String key) {
        return !StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(key);
    }

}