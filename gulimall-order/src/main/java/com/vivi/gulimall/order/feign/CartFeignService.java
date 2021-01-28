package com.vivi.gulimall.order.feign;

import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/18 18:34
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/cart/checked")
    R getCheckedItems();

    @PostMapping("/cart/del/batch")
    R delBatch(@RequestBody List<Long> skuIds);
}
