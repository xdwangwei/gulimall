package com.vivi.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:47:27
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 给指定仓库中的指定商品增加库存
     * @param wareId
     * @param skuId
     * @param num
     * @return
     */
    boolean addStock(Long wareId, Long skuId, Integer num);
}

