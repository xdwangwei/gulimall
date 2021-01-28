package com.vivi.gulimall.order.feign;

import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wangwei
 * 2021/1/25 18:46
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/skuId/{skuId}")
    R getBySkuId(@PathVariable("skuId") Long skuId);
}
