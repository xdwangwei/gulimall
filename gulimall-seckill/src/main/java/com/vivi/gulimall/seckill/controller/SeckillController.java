package com.vivi.gulimall.seckill.controller;

import com.vivi.common.to.SeckillSkuTO;
import com.vivi.common.utils.R;
import com.vivi.gulimall.seckill.service.SeckillService;
import com.vivi.gulimall.seckill.to.SeckillSkuRedisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/30 19:50
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    SeckillService seckillService;


    @ResponseBody
    @GetMapping("/current")
    public R getCurrentSeckillSessions() {
        List<SeckillSkuRedisTO> skus = seckillService.currentSeckill();
        return R.ok().setData(skus);
    }

    @ResponseBody
    @GetMapping("/sku/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuTO seckillSkuTO = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(seckillSkuTO);
    }

    /**
     * 秒杀下单
     * @param seckillId
     * @param seckillToken
     * @param count
     * @param model
     * @return
     */
    @GetMapping("/item")
    public String seckillItem(@RequestParam("seckillId") String seckillId,
                         @RequestParam("seckillToken") String seckillToken,
                         @RequestParam("count") Integer count,
                         Model model)  {
        // 秒杀成功，订单号
        String orderSn = seckillService.seckillItem(seckillId, seckillToken, count);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
