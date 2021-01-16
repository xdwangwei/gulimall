package com.vivi.gulimall.auth.vo;

import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * @author wangwei
 * 2021/1/13 11:02
 *
 * 用户注册数据模型
 */
@Data
public class RegisterVO {

    // @NotBlank
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,6}$|^[\\dA-Za-z_]{4,20}$", message = "用户名为2到6中文或4到20字符组合")
    private String username;

    // @NotBlank(message = "密码不能为空")
    // @Length(min = 8, max = 16, message = "密码长度为8-16字符")
    @Pattern(regexp = "^[a-zA-Z0-9_/.]{8,16}$", message = "格式错误，密码为8到16位字母数字符号的组合")

    private String password;

    @Pattern(regexp = "^1[3-9][0-9]{9}$", message = "手机号格式不合法")
    private String phone;

    // @NotNull(message = "验证码不能为空")
    @Pattern(regexp = "^[0-9]{6}$", message = "验证码为6位数字")
    private String code;
}
