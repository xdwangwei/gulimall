package com.vivi.gulimall.product.feign;

import com.vivi.common.to.SkuDiscountTO;
import com.vivi.common.to.SpuBoundsTO;
import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wangwei
 * 2020/9/30 18:16
 */
@FeignClient(name = "gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/feign/test/product")
    R productCouponList();

    @RequestMapping("/coupon/sku/discount/save")
    R saveDiscount(@RequestBody SkuDiscountTO skuDiscountTO);

    @RequestMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTO spuBoundsTO);

    @RequestMapping("/coupon/spubounds/info/spuId/{spuId}")
    R getBySpuId(@PathVariable("spuId") Long spuId);
}
