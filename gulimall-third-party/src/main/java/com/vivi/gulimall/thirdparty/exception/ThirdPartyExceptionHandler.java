package com.vivi.gulimall.thirdparty.exception;

import com.vivi.common.exception.BizException;
import com.vivi.common.utils.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author wangwei
 * 2021/1/13 15:11
 */
@RestControllerAdvice
public class ThirdPartyExceptionHandler {

    @ExceptionHandler({BizException.class})
    public R ossExceptionHandler(BizException e) {
        return R.error(e.getErrorCode(), e.getErrorMsg());
    }
}
