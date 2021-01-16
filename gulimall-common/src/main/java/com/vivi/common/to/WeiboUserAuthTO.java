package com.vivi.common.to;

import lombok.Data;

/**
 * @author wangwei
 * 2021/1/15 13:28
 *
 * 微博用户信息访问令牌
 */
@Data
public class WeiboUserAuthTO {

    /**
     * access_token : ACCESS_TOKEN
     * expires_in : 1234
     * remind_in : 798114
     * uid : 12341234
     */

    /**
     * 用户授权的唯一票据，用于访问用户在微博平台的信息
     */
    private String accessToken;
    /**
     * access_token的生命周期，单位是秒数。
     */
    private int expiresIn;
    /**
     * access_token的生命周期（该参数即将废弃，开发者请使用expires_in）。
     */
    private String remindIn;
    /**
     * 当前授权用户在微博的UID，本字段只是为了方便开发者，减少一次user/show接口调用而返回的，
     * 第三方应用不能用此字段作为用户登录状态的识别，只有access_token才是用户授权的唯一票据。
     */
    private String uid;

}
