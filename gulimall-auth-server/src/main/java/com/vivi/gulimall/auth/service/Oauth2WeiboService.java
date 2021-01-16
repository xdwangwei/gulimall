package com.vivi.gulimall.auth.service;

import com.vivi.common.to.MemberInfoTO;
import com.vivi.common.to.WeiboUserAuthTO;

/**
 * @author wangwei
 * 2021/1/15 13:23
 */
public interface Oauth2WeiboService {

    MemberInfoTO access(String code);
}
