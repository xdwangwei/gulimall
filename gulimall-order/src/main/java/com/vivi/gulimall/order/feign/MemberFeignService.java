package com.vivi.gulimall.order.feign;

import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wangwei
 * 2021/1/18 18:35
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/memberreceiveaddress/listby/{memberId}")
    R getMemberAddresses(@PathVariable("memberId") Long memberId);

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R getAddress(@PathVariable("id") Long addressId);

    @RequestMapping("/member/memberreceiveaddress/default/{memberId}")
    R getMemberDefaultAddress(@PathVariable("memberId") Long memberId);
}
