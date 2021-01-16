package com.vivi.gulimall.product.feign;

import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/23 13:47
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    @RequestMapping("/ware/waresku/stock/batch")
    R getSkuStockBatch(@RequestBody List<Long> skuIds);

    @RequestMapping("/ware/waresku/stock/{skuId}")
    R getSkuStock(@PathVariable("skuId") Long skuId);
}
