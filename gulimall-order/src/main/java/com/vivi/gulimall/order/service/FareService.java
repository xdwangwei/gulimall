package com.vivi.gulimall.order.service;

import com.vivi.common.to.FareInfoTO;

/**
 * @author wangwei
 * 2021/1/21 21:37
 */
public interface FareService {

    FareInfoTO getFare(Long addressId);

    /**
     * 获取指定用户默认地址以及运费信息
     * @param memberId
     * @return
     */
    FareInfoTO getMemberDefaultAddressFare(Long memberId);
}
