package com.vivi.common.constant;

/**
 * @author wangwei
 * 2021/1/30 13:21
 */
public class SeckillConstant {

    public static final String UPLOAD_SKU_LOCK_KEY = "upload_seckill_skus";
    public static final String REDIS_SECKILL_SESSION_PREFIX = "seckill:session:";
    public static final String REDIS_SECKILL_SKU_INFO_KEY = "seckill:sku";
    // redisson信号量形式保存库存
    public static final String SEMAPHORE_SECKILL_SKU_STOCK_PREFIX = "seckill:sku:stock:";
    // 用户是否已秒杀此商品
    public static final String SECKILL_USER_ALREADY_EXISTS_PREFIX = "seckill:user:sku:";
}
