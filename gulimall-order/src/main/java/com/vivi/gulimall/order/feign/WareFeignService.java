package com.vivi.gulimall.order.feign;

import com.vivi.common.to.OrderLockStockTO;
import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/21 16:48
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    @RequestMapping("/ware/waresku/stock/batch")
    R getSkuStockBatch(@RequestBody List<Long> skuIds);

    @RequestMapping("/ware/waresku/stock/{skuId}")
    R getSkuStock(@PathVariable("skuId") Long skuId);

    @PostMapping("/ware/waresku/lockStock")
    R lockStock(@RequestBody OrderLockStockTO lockStockTO);
}
