package com.vivi.gulimall.coupon.controller;

import com.vivi.common.utils.R;
import com.vivi.gulimall.coupon.entity.CouponEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author wangwei
 * 2020/9/30 18:37
 */
@RestController
@RequestMapping("/coupon/feign/test")
public class FeignTestController {

    @GetMapping("/product")
    public R productCouponList() {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("p30");
        couponEntity.setAmount(BigDecimal.valueOf(200));
        return R.ok().put("list", Arrays.asList(couponEntity));
    }
}
