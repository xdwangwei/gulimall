package com.vivi.gulimall.member.exception;

import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author wangwei
 * 2021/1/14 17:40
 */
@Slf4j
@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler({BizException.class})
    public R bizException(BizException e) {
        return R.error(e.getErrorCode(), e.getErrorMsg());
    }

    @ExceptionHandler(Throwable.class)
    public R throwableExceptionHandler(Throwable e) {
        log.error("运行期异常：{}", e);
        return R.error(BizCodeEnum.UNKNOW_ERROR.getCode(), BizCodeEnum.UNKNOW_ERROR.getMsg());
    }
}
