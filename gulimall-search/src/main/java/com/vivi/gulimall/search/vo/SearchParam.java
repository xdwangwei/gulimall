package com.vivi.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/28 16:28
 * 全部可能的检索条件
 */
@Data
public class SearchParam {
    /**
     * &keyword=小米
     * 关键字
     */
    private String keyword;

    /**
     * 三级分类id
     * &catelog3Id=225
     */
    private Long catelog3Id;

    /**
     * 排序条件
     *
     * &sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 价格区间
     * &price=1_500
     * &price=_500
     * &price=500_
     */
    private String price;

    /**
     * 有货无货
     * &hasStock=0/1
     */
    private Integer hasStock;

    /**
     * 哪些品牌
     * &brandId=1&brandId=2&brandId=3  ==> brandId=[1,2,3]
     *
     */
    private List<Long> brandId;


    /**
     * 按照属性规格检索
     * &attrs=1_陶瓷:铝合金&attrs=2_anzhuo:apple
     *
     * 属性id_属性值，如果是多个值，用 : 连接
     */
    private List<String> attrs;


    /**
     * 还要支持分页
     */
    private Integer pageNum;


}
