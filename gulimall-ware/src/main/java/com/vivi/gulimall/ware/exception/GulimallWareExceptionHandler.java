package com.vivi.gulimall.ware.exception;

import com.vivi.common.exception.BizException;
import com.vivi.common.utils.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author wangwei
 * 2021/1/26 12:02
 */
@RestControllerAdvice
public class GulimallWareExceptionHandler {

    @ExceptionHandler({BizException.class})
    public R bizExceptionHandler(BizException e) {
        return R.error(e.getErrorCode(), e.getErrorMsg());
    }
}
