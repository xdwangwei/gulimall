package com.vivi.gulimall.auth.service;

import com.vivi.common.vo.MemberInfoVO;
import com.vivi.gulimall.auth.vo.LoginVO;

/**
 * @author wangwei
 * 2021/1/14 20:26
 */
public interface LoginService {

    MemberInfoVO doLogin(LoginVO loginVO);
}
