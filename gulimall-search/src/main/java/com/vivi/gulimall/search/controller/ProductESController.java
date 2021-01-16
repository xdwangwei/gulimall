package com.vivi.gulimall.search.controller;

import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.to.SkuESModel;
import com.vivi.common.utils.R;
import com.vivi.gulimall.search.service.ProductESService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author wangwei
 * 2020/10/22 23:15
 */
@RestController
@RequestMapping("/es/product")
public class ProductESController {

    @Autowired
    private ProductESService productESService;

    @RequestMapping("/batch/save/sku")
    public R batchSaveSku(@RequestBody List<SkuESModel> list) {
        try {
            productESService.batchSave(list);
        } catch (IOException e) {
            return R.error(BizCodeEnum.PRODUCT_STATUS_UP_FAILED.getCode(), BizCodeEnum.PRODUCT_STATUS_UP_FAILED.getMsg());
        }
        return R.ok();
    }
}
