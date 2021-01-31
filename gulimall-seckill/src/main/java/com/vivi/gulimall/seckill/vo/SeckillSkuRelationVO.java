package com.vivi.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author wangwei
 * 2021/1/30 14:40
 *
 * 属性和 com.vivi.gulimall.coupon.entity.SeckillSkuRelationEntity 对应
 */
@Data
public class SeckillSkuRelationVO {

    /**
     * id
     */
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
}
