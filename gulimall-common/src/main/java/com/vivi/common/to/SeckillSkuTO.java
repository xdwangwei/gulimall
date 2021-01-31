package com.vivi.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author wangwei
 * 2021/1/30 21:59
 */
@Data
public class SeckillSkuTO {

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

    // 秒杀令牌，只有在秒杀活动开始时才会暴露出来，秒杀期间以令牌为准，防止提前消耗
    private String seckillToken;

    // 秒杀开始时间
    private Date startTime;

    // 秒杀结束时间
    private Date endTime;
}
