package com.vivi.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/11 18:38
 *
 * 商品详情页，规格参数部分，属性分组与组内属性
 */
@Data
public class ItemAttrGroupWithAttrVO {

    private Long attrGroupId;

    private String attrGroupName;

    private List<ItemAttrVO> attrs;
}
