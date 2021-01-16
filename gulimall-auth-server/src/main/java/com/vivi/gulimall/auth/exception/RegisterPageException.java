package com.vivi.gulimall.auth.exception;

import com.vivi.common.exception.BizException;
import com.vivi.common.exception.CommonError;

/**
 * @author wangwei
 * 2021/1/14 19:26
 *
 * 遇到这个异常时，最终要返回注册页面，而不是返回json数据
 */
public class RegisterPageException extends BizException {

    public RegisterPageException() {
    }

    public RegisterPageException(CommonError commonError) {
        super(commonError);
    }

    public RegisterPageException(CommonError commonError, String msg) {
        super(commonError, msg);
    }

    public RegisterPageException(int code, String msg) {
        super(code, msg);
    }

}
