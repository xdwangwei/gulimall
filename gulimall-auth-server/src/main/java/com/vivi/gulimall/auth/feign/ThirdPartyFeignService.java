package com.vivi.gulimall.auth.feign;

import com.vivi.common.to.SmsSendCodeTO;
import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author wangwei
 * 2021/1/13 15:38
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    @PostMapping("/thirdparty/sms/send/code")
    R sendCode(@RequestBody SmsSendCodeTO smsSendCodeTO);
}
