package com.vivi.gulimall.auth.exception;

import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.exception.CommonError;

/**
 * @author wangwei
 * 2021/1/14 20:56
 *
 * 遇到这个异常时，最终要返回登录页面，而不是返回json数据
 */
public class LoginPageException extends BizException {

    public LoginPageException() {
    }

    public LoginPageException(CommonError commonError) {
        super(commonError);
    }

    public LoginPageException(CommonError commonError, String msg) {
        super(commonError, msg);
    }

    public LoginPageException(int code, String msg) {
        super(code, msg);
    }
}
