package com.vivi.gulimall.search.controller;

import com.vivi.common.utils.R;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangwei
 * 2020/10/22 23:15
 */
@RestController
@RequestMapping("/es/product")
public class ProductESController {

    @RequestMapping("/up")
    public R spuStatusUp() {
        return R.ok();
    }
}
