package com.vivi.gulimall.cart.feign;

import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wangwei
 * 2021/1/16 15:38
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @RequestMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    R getSaleAttrStringList(@PathVariable("skuId") Long skuId);
}
