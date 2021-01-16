package com.vivi.gulimall.product.vo;

import lombok.Data;

/**
 * @author wangwei
 * 2021/1/12 11:31
 * <p>
 * /**
 * * 每种属性值和skuIds的对应关系
 * * 比如1号商品销售属性有，颜色：绿色；内存8G
 * * 那么颜色为绿色涉及的sku中就有1号，内存为8g涉及的sku中就有1号
 * * 这样方便于对不同属性值进行组合时快速确定这个组合对应的是哪一个商品
 */
@Data
public class ItemSaleAttrValueWithSkuVO {

    String attrValue;
    String skuIds; // 逗号分隔的多个skuId
}
