package com.vivi.gulimall.seckill.service;

import com.vivi.common.to.SeckillSkuTO;
import com.vivi.gulimall.seckill.to.SeckillSkuRedisTO;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/30 10:59
 */
public interface SeckillService {

    /**
     * 上架最近三天要参与秒杀的所有商品信息
     * @return
     */
    boolean uploadLatest3DaysSeckillSkus();

    /**
     * 获取当前所在秒杀场次的商品信息
     * @return
     */
    List<SeckillSkuRedisTO> currentSeckill();

    /**
     * 获取指定商品当前参与的秒杀信息
     * @param skuId
     * @return
     */
    SeckillSkuTO getSkuSeckillInfo(Long skuId);


    /**
     * 实际秒杀
     * @param seckillId
     * @param seckillToken
     * @param count
     */
    String seckillItem(String seckillId, String seckillToken, Integer count);
}
