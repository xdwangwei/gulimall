package com.vivi.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wangwei
 * 2021/1/18 13:50
 */
@Controller
public class WebController {

    @GetMapping("/center/list.html")
    public String orderList() {
        return "list";
    }

    @GetMapping("/{name}.html")
    public String page(@PathVariable("name") String name) {
        return name;
    }
}
