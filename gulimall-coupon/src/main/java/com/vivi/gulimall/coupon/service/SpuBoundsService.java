package com.vivi.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.to.SpuBoundsTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.coupon.entity.SpuBoundsEntity;

import java.util.Map;

/**
 * 商品spu积分设置
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:52:21
 */
public interface SpuBoundsService extends IService<SpuBoundsEntity> {

    PageUtils queryPage(Map<String, Object> params);

    SpuBoundsTO getBySpuId(Long spuId);

    boolean saveSpuBoundS(SpuBoundsEntity spuBounds);
}

