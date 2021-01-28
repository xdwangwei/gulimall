package com.vivi.gulimall.order.service;

import com.vivi.common.to.FareInfoTO;

/**
 * @author wangwei
 * 2021/1/21 21:37
 */
public interface FareService {

    FareInfoTO getFare(Long addressId);
}
