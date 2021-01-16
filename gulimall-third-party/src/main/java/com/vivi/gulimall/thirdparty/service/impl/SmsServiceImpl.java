package com.vivi.gulimall.thirdparty.service.impl;

import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.to.SmsSendCodeTO;
import com.vivi.gulimall.thirdparty.service.SmsService;
import com.vivi.common.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangwei
 * 2021/1/13 13:19
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.config.appcode}")
    private String appcode;

    @Value("${sms.config.template-id}")
    private String templateId;

    @Override
    public void sendCode(SmsSendCodeTO codeTO) {
        String host = "https://intlsms.market.alicloudapi.com";
        String path = "/comms/sms/sendmsgall";
        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("callbackUrl", "http://test.dev.esandcloud.com");
        bodys.put("channel", "0");
        bodys.put("mobile", "+86" + codeTO.getPhone());
        bodys.put("templateID", templateId);
        bodys.put("templateParamSet", codeTO.getCode() + "," + codeTO.getTimeout());
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtils.doPost(host, path, method, headers, querys, bodys);
        } catch (Exception e) {
            log.error("SendCodeResponse: {}", httpResponse);
            throw new BizException(BizCodeEnum.SMS_SEND_CODE_FAILED);
        }
    }

}

