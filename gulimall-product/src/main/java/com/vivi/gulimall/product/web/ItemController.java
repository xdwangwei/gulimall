package com.vivi.gulimall.product.web;

import com.vivi.gulimall.product.service.SkuInfoService;
import com.vivi.gulimall.product.vo.ItemDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wangwei
 * 2021/1/11 17:41
 */
@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId,
                       Model model) {
        ItemDetailVO detail = skuInfoService.detail(skuId);
        model.addAttribute("item", detail);
        return "item";
    }

}
