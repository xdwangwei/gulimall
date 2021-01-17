package com.vivi.gulimall.cart.exception;

import com.vivi.common.exception.BizException;
import com.vivi.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author wangwei
 * 2021/1/13 17:13
 */
@Slf4j
@ControllerAdvice
public class CartExceptionHandler {

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
