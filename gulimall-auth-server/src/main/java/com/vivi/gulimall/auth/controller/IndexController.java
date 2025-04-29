package com.vivi.gulimall.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.vivi.common.constant.AuthServerConstant;

import jakarta.servlet.http.HttpSession;
/**
 * @author wangwei
 * 2021/1/13 10:25
 */
@Controller
public class IndexController {

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        if (session.getAttribute(AuthServerConstant.LOGIN_USER_KEY) != null) {
            // 用户已登录
            return "redirect:http://gulimall.com";
        }
        return "login";
    }

    @GetMapping("/reg.html")
    public String regPage() {
        return "reg";
    }

}
