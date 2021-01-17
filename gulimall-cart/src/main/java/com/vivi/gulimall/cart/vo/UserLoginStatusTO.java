package com.vivi.gulimall.cart.vo;

import lombok.Data;

/**
 * @author wangwei
 * 2021/1/16 13:36
 *
 * 用户登录状态
 */
@Data
public class UserLoginStatusTO {

    /**
     * 登录用户既有key，又有id
     */
    private Long id;

    /**
     * 未登录会有一个key，id为null
     */
    private String userKey;

    /**
     * 是否是第一次访问，第一次访问购物车，会为其分配user-key
     */
    private boolean firstVisit;
}
