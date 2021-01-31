package com.vivi.gulimall.product.feign;

import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wangwei
 * 2021/1/31 9:53
 */
@FeignClient("gulimall-seckill")
public interface SeckillFeignService {

    @GetMapping("/seckill/sku/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
