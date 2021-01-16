package com.vivi.gulimall.product.exception;

import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangwei
 * 2020/10/10 17:01
 */
@Slf4j
@RestControllerAdvice
public class GulimallProductExceptionHandler {

    /**
     * 处理参数校验失败的异常处理器
     * @param e
     * @return
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public R validateExceptionHandler(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> resMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(item -> {
            resMap.put(item.getField(), item.getDefaultMessage());
        });
        return R
                .error(BizCodeEnum.PRODUCT_PARAM_INVAILD.getCode(), BizCodeEnum.PRODUCT_PARAM_INVAILD.getMsg())
                .put("data", resMap);
    }

    @ExceptionHandler({BizException.class})
    public R threadPoolException(BizException e) {
        return R
                .error(e.getErrorCode(), e.getErrorMsg());
    }

    /**
     * 其他异常处理器
     * @param e
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public R throwableExceptionHandler(Throwable e) {
        log.error("Throwable: {}", e.getMessage());
        return R
                .error(BizCodeEnum.UNKNOW_ERROR.getCode(), BizCodeEnum.UNKNOW_ERROR.getMsg());
    }
}
