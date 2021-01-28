package com.vivi.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author wangwei
 * 2021/1/25 18:57
 */
@Data
public class SpuInfoTO {

    private Long id;
    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 商品描述
     */
    private String spuDescription;
    /**
     * 所属分类id
     */
    private Long catelogId;
    // 新加，分类名
    private String catelogName;
    /**
     * 品牌id
     */
    private Long brandId;
    // 新加。品牌名
    private String brandName;
    /**
     * 重量
     */
    private BigDecimal weight;
    /**
     * 上架状态[0-新建，1 - 上架，2 - 下架]
     */
    private Integer publishStatus;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    // 成长值
    private BigDecimal growBounds;

    // 积分
    private BigDecimal integration;
}
