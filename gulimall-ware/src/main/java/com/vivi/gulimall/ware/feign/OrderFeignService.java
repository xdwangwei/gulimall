package com.vivi.gulimall.ware.feign;

import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wangwei
 * 2021/1/27 21:23
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("/api/info/{orderSn}")
    R getOrderDetail(@PathVariable("orderSn") String orderSn);
}
