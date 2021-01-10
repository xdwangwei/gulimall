package com.vivi.gulimall.auth.controller;

import com.vivi.gulimall.auth.vo.LoginVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

/**
 * @author wangwei
 * 2021/1/1 18:36
 */
@Controller
public class LoginController {

    @GetMapping ("/tologin")
    public String toLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(LoginVO loginVO,
                        HttpSession session) {
        session.setAttribute("loginUser", loginVO);
        return "success";
    }
}
