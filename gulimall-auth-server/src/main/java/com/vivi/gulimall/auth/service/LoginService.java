package com.vivi.gulimall.auth.service;

import com.vivi.common.to.MemberInfoTO;
import com.vivi.gulimall.auth.vo.LoginVO;

/**
 * @author wangwei
 * 2021/1/14 20:26
 */
public interface LoginService {

    MemberInfoTO doLogin(LoginVO loginVO);
}
