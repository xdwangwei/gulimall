package com.vivi.gulimall.seckill.to;

import com.vivi.common.to.SkuInfoTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author wangwei
 * 2021/1/30 16:14
 *
 * 秒杀商品在redis中的存储模型
 */
@Data
public class SeckillSkuRedisTO {

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

    // 秒杀令牌，只有在秒杀活动开始时才会暴露出来，秒杀期间以令牌为准，防止提前消耗
    private String seckillToken;

    // 秒杀开始时间
    private Date startTime;

    // 秒杀结束时间
    private Date endTime;

    // sku基本信息
    private SkuInfoTO skuInfo;
}
