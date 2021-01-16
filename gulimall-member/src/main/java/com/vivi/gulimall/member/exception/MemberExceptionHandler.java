package com.vivi.gulimall.member.exception;

import com.vivi.common.exception.BizException;
import com.vivi.common.utils.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author wangwei
 * 2021/1/14 17:40
 */
@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler({BizException.class})
    public R bizException(BizException e) {
        return R.error(e.getErrorCode(), e.getErrorMsg());
    }
}
