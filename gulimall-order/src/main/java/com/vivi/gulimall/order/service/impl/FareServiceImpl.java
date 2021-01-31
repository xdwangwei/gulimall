package com.vivi.gulimall.order.service.impl;

import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.to.FareInfoTO;
import com.vivi.common.to.MemberAddressTO;
import com.vivi.common.utils.R;
import com.vivi.gulimall.order.feign.MemberFeignService;
import com.vivi.gulimall.order.service.FareService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author wangwei
 * 2021/1/21 21:38
 */
@Slf4j
@Service
public class FareServiceImpl implements FareService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public FareInfoTO getFare(Long addressId) {
        R r = memberFeignService.getAddress(addressId);
        if (r.getCode() != 0) {
            log.error("调用gulimall-member服务查询地址信息失败");
            throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED);
        }
        MemberAddressTO address = r.getData("memberReceiveAddress", MemberAddressTO.class);
        FareInfoTO fareInfoTO = new FareInfoTO();
        fareInfoTO.setAddress(address);
        fareInfoTO.setFare(getSimpleFare(address));
        return fareInfoTO;
    }

    @Override
    public FareInfoTO getMemberDefaultAddressFare(Long memberId) {
        R r = memberFeignService.getMemberDefaultAddress(memberId);
        if (r.getCode() != 0) {
            log.error("调用gulimall-member服务查询用户默认地址");
            throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED);
        }
        MemberAddressTO address = r.getData(MemberAddressTO.class);
        FareInfoTO fareInfoTO = new FareInfoTO();
        fareInfoTO.setAddress(address);
        fareInfoTO.setFare(getSimpleFare(address));
        return fareInfoTO;
    }

    /**
     * 简单的运费计算
     * @param address
     * @return
     */
    private BigDecimal getSimpleFare(MemberAddressTO address) {
        // 简化运费计算
        if (address != null) {
            String phone = address.getPhone();
            if (!StringUtils.isEmpty(phone)) {
                // 简化计算运费过程
                return new BigDecimal(phone.substring(phone.length() - 1));
            }
        }
        return new BigDecimal("0");
    }
}
