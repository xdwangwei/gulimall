package com.vivi.gulimall.auth.controller;

import com.vivi.common.constant.AuthConstant;
import com.vivi.common.vo.MemberInfoVO;
import com.vivi.gulimall.auth.service.LoginService;
import com.vivi.gulimall.auth.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 * @author wangwei
 * 2021/1/14 20:24
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    /**
     * 使用Model传值，返回字符串，视图解析器自动跳转到页面，可以取值
     * 使用Model传值，返回forward，同一个请求内，也可以取值
     * 使用Model传值，返回redirect，不同请求，无法取值
     *      此时，可以使用 RedirectAttributes 传值，
     *          addFlashAttribute 是添加一个数据，只取一次，就会清除
     *          addAttribute() 是往请求路径后面拼接参数
     *      它是【模拟session】进行传值，所以前提是原请求所在域和重定向的域是同一个域，这样两次请求虽然不同，但cookie中都会有服务器给的session，所以可以传数据，能取到数据
     *      如果重定向的地址是其他域或者是当前域的父域等更大的域，第二次请求就会丢失cookie，因为返回器给浏览器的cookie默认有效域是当前域，此时，这个传值就会丢失，取不到数据
     *
     * 可以在服务端对session管理进行配置，比如设置返回给浏览器的cookie数据有效域为父域，这样，即使我是从访问子域的请求重定向到访问父域，因为cookie的有效域是大域，一直有效，也能取到数据
     *      @Bean
     *     public CookieSerializer cookieSerializer()
     * @return
     */

    @Autowired
    LoginService loginService;

    @PostMapping("/login")
    public String login(LoginVO loginVO, HttpSession session) {
        MemberInfoVO info = loginService.doLogin(loginVO);
        // 登录失败会被异常处理器处理
        // 登录成功要保存会话信息，返回主页面
        session.setAttribute(AuthConstant.LOGIN_USER_KEY, info);
        // System.out.println(infoTO);
        return "redirect:http://gulimall.com/";
    }
}
