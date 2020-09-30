package com.vivi.gulimall.product.feign;

import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author wangwei
 * 2020/9/30 18:16
 */
@FeignClient(name = "gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/feign/test/product")
    R productCouponList();
}
