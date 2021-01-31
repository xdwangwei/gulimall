package com.vivi.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author wangwei
 * 2021/1/31 14:58
 *
 * 秒杀订单
 */
@Data
public class SeckillOrderTO {

    /**
     * 秒杀服务为其创建的订单号
     */
    private String orderSn;

    /**
     * 下单人id
     */
    private Long memberId;

    /**
     * 下单人用户名
     */
    private String userName;

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
     * 创建数量
     */
    private Integer count;
}
