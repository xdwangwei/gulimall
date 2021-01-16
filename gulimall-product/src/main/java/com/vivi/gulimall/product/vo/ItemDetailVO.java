package com.vivi.gulimall.product.vo;

import com.vivi.gulimall.product.entity.SkuImagesEntity;
import com.vivi.gulimall.product.entity.SkuInfoEntity;
import com.vivi.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/11 18:25
 *
 * 商品详情模型
 */
@Data
public class ItemDetailVO {

    // sku基本信息
    private SkuInfoEntity skuInfo;

    // 此sku是否有库存
    private boolean hasStock = false;

    // 此sku的图片集
    private List<SkuImagesEntity> skuImages;

    // 同款商品所有销售属性组合
    private List<ItemSaleAttrVO> saleAttrs;

    // 商品描述信息(spu)
    private SpuInfoDescEntity spuDesc;

    // 商品规格参数(spu，属性分组与属性)
    private List<ItemAttrGroupWithAttrVO> spuAttrGroups;
}
