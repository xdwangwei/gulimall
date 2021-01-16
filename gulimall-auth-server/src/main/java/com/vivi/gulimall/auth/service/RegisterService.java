package com.vivi.gulimall.auth.service;


import com.vivi.gulimall.auth.vo.RegisterVO;

/**
 * @author wangwei
 * 2021/1/13 15:41
 */
public interface RegisterService {

    /**
     * 发送验证码
     * @param phone
     */
    void sendCode(String phone);

    /**
     * 用户注册
     * @param registerVO
     * @return
     */
    boolean register(RegisterVO registerVO);
}
