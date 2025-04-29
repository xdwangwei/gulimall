package com.vivi.gulimall.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vivi.common.constant.AuthServerConstant;
import com.vivi.common.vo.MemberInfoVO;
import com.vivi.gulimall.auth.service.Oauth2WeiboService;

import jakarta.servlet.http.HttpSession;


/**
 * @author wangwei
 * 2021/1/15 13:18
 */
@Controller
@RequestMapping("/oauth2")
public class Oauth2Controller {

    @Autowired
    Oauth2WeiboService weiboService;

    /**
     * 选择微博登录，授权后，微博会将code返回给我们提供的地址
     * @return
     */
    @GetMapping("/weibo/return")
    public String weiboReturn(@RequestParam("code") String code, HttpSession session) {
        MemberInfoVO info = weiboService.access(code);
        // 登录成功，保存会话信息，返回主页
        session.setAttribute(AuthServerConstant.LOGIN_USER_KEY, info);
        return "redirect:http://gulimall.com";
    }

    /**
     * 取消微博登录，重回登录页
     * @return
     */
    @GetMapping("/weibo/cancel")
    public String weiboCancel() {
        return "redirect://auth.gulimall.com/login.html";
    }
}
