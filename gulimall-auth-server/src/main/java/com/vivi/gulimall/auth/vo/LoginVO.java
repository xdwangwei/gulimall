package com.vivi.gulimall.auth.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wangwei
 * 2021/1/1 19:35
 *
 * 用户登录数据模型
 */
@Data
public class LoginVO {

    private String account;

    private String password;
}
