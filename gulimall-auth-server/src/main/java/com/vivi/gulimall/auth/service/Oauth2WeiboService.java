package com.vivi.gulimall.auth.service;

import com.vivi.common.vo.MemberInfoVO;

/**
 * @author wangwei
 * 2021/1/15 13:23
 */
public interface Oauth2WeiboService {

    MemberInfoVO access(String code);
}
