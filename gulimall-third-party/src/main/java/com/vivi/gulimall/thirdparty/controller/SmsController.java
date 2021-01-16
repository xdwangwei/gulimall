package com.vivi.gulimall.thirdparty.controller;


import com.vivi.common.to.SmsSendCodeTO;
import com.vivi.common.utils.R;
import com.vivi.gulimall.thirdparty.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangwei
 * 2021/1/13 11:05
 *
 * 短信服务
 */
@RestController
@RequestMapping("/thirdparty/sms")
public class SmsController {

    @Autowired
    SmsService smsService;

    /**
     * 供其他服务调用
     * @return
     */
    @PostMapping("/send/code")
    public R sendCode(@RequestBody SmsSendCodeTO smsSendCodeTO) {
        smsService.sendCode(smsSendCodeTO);
        return R.ok();
    }

}
