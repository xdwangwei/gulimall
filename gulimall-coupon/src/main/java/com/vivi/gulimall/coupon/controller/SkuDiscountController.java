package com.vivi.gulimall.coupon.controller;

import com.vivi.common.to.SkuDiscountTO;
import com.vivi.common.utils.R;
import com.vivi.gulimall.coupon.service.SkuDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangwei
 * 2020/10/19 16:21
 */
@RestController
@RequestMapping("coupon/sku/discount")
public class SkuDiscountController {

    @Autowired
    private SkuDiscountService skuDiscountService;

    @RequestMapping("/save")
    public R saveDiscount(@RequestBody SkuDiscountTO skuDiscountTO) {
        skuDiscountService.save(skuDiscountTO);
        return R.ok();
    }
}
