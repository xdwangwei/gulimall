package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.common.utils.R;
import com.vivi.gulimall.product.dao.SkuInfoDao;
import com.vivi.gulimall.product.entity.*;
import com.vivi.gulimall.product.feign.WareFeignService;
import com.vivi.gulimall.product.service.*;
import com.vivi.gulimall.product.vo.ItemAttrGroupWithAttrVO;
import com.vivi.gulimall.product.vo.ItemDetailVO;
import com.vivi.gulimall.product.vo.ItemSaleAttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    WareFeignService wareFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        /**
         * key: '华为',//检索关键字
         * catelogId: 0,
         * brandId: 0,
         * min: 0,
         * max: 0
         */
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        // 查询关键字
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
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
        try {
            // 最低价格
            BigDecimal min = new BigDecimal((String) params.get("min"));
            if (min.compareTo(BigDecimal.ZERO) >= 0) {
                queryWrapper.ge("price", min);
            }
            // 最高价格
            BigDecimal max = new BigDecimal((String) params.get("max"));
            if (max.compareTo(BigDecimal.ZERO) > 0) {
                queryWrapper.le("price", max);
            }
        } catch (Exception e) {

        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> listBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    /**
     * 查询商品详情
     * 使用completeableFuture异步编排
     * @param skuId
     * @return
     */
    @Override
    public ItemDetailVO detail(Long skuId) {
        ItemDetailVO itemDetailVO = new ItemDetailVO();
        // TODO 1.sku基本信息 接收返回值，后面的异步任务需要用到这个结果
        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            itemDetailVO.setSkuInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);
        // TODO 6.商品是否有库存，与1无关，与1并列
        CompletableFuture<Void> skuStockFuture = CompletableFuture.runAsync(() -> {
            R r = wareFeignService.getSkuStock(skuId);
            if (r.getCode() == 0) {
                Long stock = r.getData(Long.class);
                itemDetailVO.setHasStock(stock > 0);
            }
        }, executor);
        // TODO 2.此sku的图片集 不需要1的结果，也无返回值，与1，2并列
        CompletableFuture<Void> skuImageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.listBySkuId(skuId);
            itemDetailVO.setSkuImages(skuImagesEntities);
        }, executor);
        // TODO 3.同商品所有销售销售属性及其对应值，需要1的结果 1.thenAcceptAsync
        CompletableFuture<Void> skuSaleAttrFuture = skuInfoFuture.thenAcceptAsync((skuInfoEntity) -> {
            List<ItemSaleAttrVO> saleAttrs = skuSaleAttrService.allAttrValueWithSkuBySpuId(skuInfoEntity.getSpuId());
            itemDetailVO.setSaleAttrs(saleAttrs);
        }, executor);
        // TODO 4.商品描述信息(spu)，需要1的结果，1.thenAcceptAsync，与3并列
        CompletableFuture<Void> spuDescFuture = skuInfoFuture.thenAcceptAsync((skuInfoEntity) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getBySpuId(skuInfoEntity.getSpuId());
            itemDetailVO.setSpuDesc(spuInfoDescEntity);
        }, executor);
        // TODO 5.商品规格参数(spu，属性分组与属性)，需要1的结果，1.thenAcceptAsync，与3,4并列
        CompletableFuture<Void> spuAttrGroupFuture = skuInfoFuture.thenAcceptAsync((skuInfoEntity) -> {
            List<ItemAttrGroupWithAttrVO> itemAttrGroupWithAttrVOS = productAttrValueService.getAttrsWithAttrGroupBySpuId(skuInfoEntity.getSpuId());
            itemDetailVO.setSpuAttrGroups(itemAttrGroupWithAttrVOS);
        }, executor);

        // 等待所有异步任务执行完成,345在1之后执行，所以345完成1肯定完成，不用等1
        try {
            CompletableFuture.allOf(skuStockFuture, skuImageFuture, skuSaleAttrFuture, spuDescFuture, spuAttrGroupFuture).get();
        } catch (Exception e) {
            throw new BizException(BizCodeEnum.THREAD_POOL_TASK_FAILED, "异步编排查询商品详情失败");
        }
        return itemDetailVO;
    }

    private boolean isValidId(String key) {
        return !StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(key);
    }

}