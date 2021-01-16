package com.vivi.common.to;

import lombok.Data;

/**
 * @author wangwei
 * 2021/1/13 15:25
 */
@Data
public class SmsSendCodeTO {

    private String phone; // 手机号

    private String code; // 验证码

    private Integer timeout; // 有效时间
}
