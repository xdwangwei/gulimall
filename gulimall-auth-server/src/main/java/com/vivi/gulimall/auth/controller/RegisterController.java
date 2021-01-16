package com.vivi.gulimall.auth.controller;

import com.vivi.common.utils.R;
import com.vivi.gulimall.auth.service.RegisterService;
import com.vivi.gulimall.auth.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * @author wangwei
 * 2021/1/13 15:51
 */
@Controller
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    RegisterService registerService;

    @ResponseBody
    @GetMapping("/send/code")
    public R sendCode(@RequestParam("phone") String phone) {
        registerService.sendCode(phone);
        return R.ok();
    }

    /**
     * 如果这里参数没有加 @RequestBody
     *      Spring 使用 FormHttpMessageConverter 转化请求体(表单数据，2021-01-13 17:52:19.549 ERROR 20804 --- [p-nio-99-exec-1] c.v.g.a.exception.AuthExceptionHandler   : 参数校验失败：Content type 'application/x-www-form-urlencoded;charset=UTF-8' not supported，异常类型：class org.springframework.web.HttpMediaTypeNotSupportedException)到 对象 ，
     *          抛出异常 BindException.
     * 如果加了 @RequestBody Spring按照json格式进行解析以及封装，最终抛出 MethodArgumentNotValidException
     *
     * 所以如果是前端表单格式提交数据，不能加 @RequestBody，数据校验失败后，处理异常 BindException
     * @param registerVO
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/register")
    public String register(@Validated RegisterVO registerVO,
                           RedirectAttributes redirectAttributes) {
        // 数据校验失败会抛异常并被处理，这里只需要执行校验成功后的逻辑
        // 完成用户注册逻辑
        boolean res = registerService.register(registerVO);
        // 重定向到登录页
        return "redirect://auth.gulimall.com/login.html";
    }

}
