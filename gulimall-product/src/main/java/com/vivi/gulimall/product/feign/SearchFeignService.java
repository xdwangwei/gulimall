package com.vivi.gulimall.product.feign;

import com.vivi.common.to.SkuESModel;
import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/23 14:31
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {

    @RequestMapping("/es/product/batch/save/sku")
    R batchSaveSku(@RequestBody List<SkuESModel> list);
}
