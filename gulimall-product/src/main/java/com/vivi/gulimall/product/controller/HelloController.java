package com.vivi.gulimall.product.controller;

import com.vivi.common.utils.R;
import com.vivi.gulimall.product.feign.CouponFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangwei
 * 2020/9/30 17:08
 *
 * 测试配置文件和配置中心配置项的读取
 */
@RefreshScope
@RestController
public class HelloController {

    @Autowired
    private CouponFeignService couponFeignService;

    @Value("${test.nacos.config.name}")
    private String name;

    @Value("${test.nacos.config.age}")
    private Integer age;


    @GetMapping("/hello")
    public String hello() {
        return "hello " + name + ", " + age + " years old.";
    }

    @GetMapping("/product/coupon/list")
    public R couponList() {
        return R.ok().put("couponList", couponFeignService.productCouponList());
    }

}
