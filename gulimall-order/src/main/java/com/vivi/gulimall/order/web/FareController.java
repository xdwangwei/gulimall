package com.vivi.gulimall.order.web;

import com.vivi.common.to.FareInfoTO;
import com.vivi.common.utils.R;
import com.vivi.gulimall.order.service.FareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author wangwei
 * 2021/1/21 21:35
 *
 * 计算运费
 */
@Controller
@RequestMapping("/order/fare")
public class FareController {

    @Autowired
    FareService fareService;

    @ResponseBody
    @GetMapping("/address/{id}")
    public R getFare(@PathVariable("id") Long addrId) {
        FareInfoTO fare = fareService.getFare(addrId);
        return R.ok().setData(fare);
    }
}
