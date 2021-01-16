package com.vivi.gulimall.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.to.MemberInfoTO;
import com.vivi.common.to.WeiboUserAuthTO;
import com.vivi.common.utils.HttpUtils;
import com.vivi.common.utils.R;
import com.vivi.gulimall.auth.exception.LoginPageException;
import com.vivi.gulimall.auth.feign.MemberFeignService;
import com.vivi.gulimall.auth.service.Oauth2WeiboService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author wangwei
 * 2021/1/15 13:33
 */
@Slf4j
@Service
public class Oauth2WeiboServiceImpl implements Oauth2WeiboService {

    @Autowired
    MemberFeignService memberFeignService;

    @Value("${oauth2.social.weibo.client-id}")
    private String clientId;

    @Value("${oauth2.social.weibo.client-secret}")
    private String clientSecret;

    @Value("${oauth2.social.weibo.grant-type}")
    private String grantType;

    @Value("${oauth2.social.weibo.redirect-uri}")
    private String redirectUri;

    @Override
    public MemberInfoTO access(String code) {
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "3661722387");
        map.put("client_secret", "1b253ebc0906e625b02f73050c7cde69");
        map.put("grant_type", "authorization_code");
        map.put("code", code);
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2/weibo/return");
        try {
            HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), null, map);
            if (response.getStatusLine().getStatusCode() == 200) {
                // 访问成功
                String json = EntityUtils.toString(response.getEntity());
                // 拿到访问令牌
                // 拿到响应内容，转为 WeiboUserAccessTO
                WeiboUserAuthTO authTO = JSON.parseObject(json, WeiboUserAuthTO.class);
                // 调用远程服务完成用户此次登录
                R r = memberFeignService.weiboLogin(authTO);
                if (r.getCode() == 0) {
                    return r.getData(MemberInfoTO.class);
                } else {
                    log.error("微博登录-用token调用member服务登录失败：{}", r.get("msg"));
                    throw new LoginPageException(BizCodeEnum.AUTH_WEIBO_LOGIN_FAILED);
                }
            } else {
                log.error("微博登录-用code获取token失败：{}", EntityUtils.toString(response.getEntity()));
                throw new LoginPageException(BizCodeEnum.AUTH_WEIBO_LOGIN_FAILED);
            }
        } catch (Exception e) {
            log.error("微博登录-用code获取token失败：{}", e.getMessage());
            throw new LoginPageException(BizCodeEnum.AUTH_WEIBO_LOGIN_FAILED);
        }
    }
}
