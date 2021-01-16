package com.vivi.gulimall.auth.exception;

import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wangwei
 * 2021/1/13 17:13
 */
@Slf4j
@ControllerAdvice
public class AuthExceptionHandler {

    /**
     * 注册，表单提交数据格式校验失败，重新跳转到注册页，携带提示信息
     * 为了防止重复提交，我们使用重定向，因为要使用视图解析，所以不能加 @ResponseBody
     * @param e
     * @return
     */
    @ExceptionHandler({BindException.class})
    public String bindingExceptionHandler(BindException e, RedirectAttributes redirectAttributes) {
        BindingResult bindingResult = e.getBindingResult();
        // Map<String, String> map = new HashMap<>();
        // bindingResult.getFieldErrors().forEach(fieldError -> {
        //     String field = fieldError.getField();
        //     String message = fieldError.getDefaultMessage();
        //     map.put(field, map.getOrDefault(field, "") + " " + message);
        // });
        Map<String, String> map = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        redirectAttributes.addFlashAttribute("errMap", map);
        // 重回注册页
        return "redirect:http://auth.gulimall.com/reg.html";
    }

    /**
     * 封装错误信息，并返回注册页面
     * @param e
     * @return
     */
    @ExceptionHandler({RegisterPageException.class})
    public String registerPageException(RegisterPageException e,
                                   RedirectAttributes redirectAttributes) {
        // 放入错误消息
        redirectAttributes.addFlashAttribute("regErrMsg", e.getMessage());
        // 重回注册页
        return "redirect:http://auth.gulimall.com/reg.html";
    }

    /**
     * 封装错误信息，并返回登录页面
     * @param e
     * @return
     */
    @ExceptionHandler({LoginPageException.class})
    public String loginPageException(LoginPageException e,
                                        RedirectAttributes redirectAttributes) {
        // 放入错误消息
        redirectAttributes.addFlashAttribute("loginErrMsg", e.getMessage());
        // 重回登录页
        return "redirect:http://auth.gulimall.com/login.html";
    }

    /**
     * 其他业务异常
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler({BizException.class})
    public R bizException(BizException e) {
        // 其他业务异常则直接返回json数据
        return R.error(e.getErrorCode(), e.getErrorMsg());
    }

}
