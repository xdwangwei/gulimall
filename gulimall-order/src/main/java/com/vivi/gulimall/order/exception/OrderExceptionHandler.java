package com.vivi.gulimall.order.exception;

import com.vivi.common.exception.BizException;
import com.vivi.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author wangwei
 * 2021/1/18 18:37
 */
@Slf4j
@ControllerAdvice
public class OrderExceptionHandler {

    @ResponseBody
    @ExceptionHandler({BizException.class})
    public R bizExceptionHandler(BizException e) {

        return R.error(e.getErrorCode(), e.getErrorMsg());
    }

    @ExceptionHandler(RuntimeException.class)
    public String runtimeExceptionHandler(RuntimeException e) {
        log.error("业务异常：{}", e.getMessage());
        // 重定向到购物车列表
        return "redirect:http://cart.gulimall.com/cart/list.html";
    }
}
