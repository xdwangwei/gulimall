package com.vivi.gulimall.coupon.service;

import com.vivi.common.to.SkuDiscountTO;

/**
 * @author wangwei
 * 2020/10/19 16:27
 */
public interface SkuDiscountService {


    /**
     * 保存一个sku完整的优惠信息(满减，打折，会员价)
     * @param skuDiscountTO
     * @return
     */
    boolean save(SkuDiscountTO skuDiscountTO);
}
