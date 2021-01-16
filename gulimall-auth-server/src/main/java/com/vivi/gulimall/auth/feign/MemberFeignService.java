package com.vivi.gulimall.auth.feign;

import com.vivi.common.to.MemberLoginTO;
import com.vivi.common.to.MemberRegisterTO;
import com.vivi.common.to.WeiboUserAuthTO;
import com.vivi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wangwei
 * 2021/1/13 20:35
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/member/register")
    R register(@RequestBody MemberRegisterTO registerTO);

    @RequestMapping("/member/member/login")
    R login(@RequestBody MemberLoginTO loginTO);

    @RequestMapping("/member/member/weibo/login")
    R weiboLogin(@RequestBody WeiboUserAuthTO authTO);
}
