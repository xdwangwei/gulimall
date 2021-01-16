package com.vivi.gulimall.product.vo;

import lombok.Data;

/**
 * @author wangwei
 * 2021/1/11 18:36
 *
 * 商品规格参数(基本属性)VO
 */
@Data
public class ItemAttrVO {

    /**
     * attr_id
     */
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;
    /**
     * 属性值，规格参数(基本属性)里面，他是单个值
     */
    private String attrValue;
}
