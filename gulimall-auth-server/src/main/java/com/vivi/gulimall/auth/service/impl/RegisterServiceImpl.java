package com.vivi.gulimall.auth.service.impl;

import com.vivi.common.constant.AuthConstant;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.to.MemberRegisterTO;
import com.vivi.common.to.SmsSendCodeTO;
import com.vivi.common.utils.R;
import com.vivi.gulimall.auth.exception.RegisterPageException;
import com.vivi.gulimall.auth.feign.MemberFeignService;
import com.vivi.gulimall.auth.feign.ThirdPartyFeignService;
import com.vivi.gulimall.auth.service.RegisterService;
import com.vivi.gulimall.auth.vo.RegisterVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.RealmRuleSet;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import sun.rmi.runtime.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author wangwei
 * 2021/1/13 15:43
 */
@Slf4j
@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 发送验证码，同一用户，一分钟内只发送一次
     * @param phone
     */
    @Override
    public void sendCode(String phone) {
        // 尝试从redis中获取用户的验证码
        String key = AuthConstant.RANDOM_CODE_REDIS_PREFIX + phone;
        String codeWithTime = redisTemplate.opsForValue().get(key);
        // 如果存在，一分钟内不给他再发
        if (!StringUtils.isEmpty(codeWithTime)) {
            String[] info = codeWithTime.split("_");
            // 当前时间
            long now = new Date().getTime();
            // 上次发送时的时间
            long last = Long.parseLong(info[1]);
            // 间隔不到60s
            if (now - last < 60000) {
                throw new RegisterPageException(BizCodeEnum.TOO_MANY_REQUEST, "操作频繁，请1分钟后再试");
            }
        }
        // 如果redis中不存在当前用户的验证码，或者一分钟已过，就执行发送验证码逻辑

        // 生成验证码
        String code = randomCode();

        // 将用户的验证码存入redis，带上当前时间，并设置过期时间
        String val = code + "_" + new Date().getTime();
        redisTemplate.opsForValue().set(key, val, AuthConstant.RANDOM_CODE_VALID_TIME, TimeUnit.MINUTES);

        // 调用远程服务发送验证码
        SmsSendCodeTO smsSendCodeTO = new SmsSendCodeTO();
        smsSendCodeTO.setPhone(phone);
        smsSendCodeTO.setCode(code);
        smsSendCodeTO.setTimeout(AuthConstant.RANDOM_CODE_VALID_TIME);
        R r = thirdPartyFeignService.sendCode(smsSendCodeTO);
        if (r.getCode() != 0) {
            log.error("调用远程服务发送验证码失败，返回结果：{}", r);
            // 抛出异常，远程服务执行失败，会返回json格式的r，里面包含了错误信息
            throw new RegisterPageException(r.getCode(), r.getData("msg", String.class));
        }
    }

    @Override
    public boolean register(RegisterVO registerVO) {
        // 验证验证码合法性
        // 从redis中获取用户的验证码
        String key = AuthConstant.RANDOM_CODE_REDIS_PREFIX + registerVO.getPhone();
        String codeWithTime = redisTemplate.opsForValue().get(key);
        // 匹配用户传过来的验证码
        // 匹配失败
        if (StringUtils.isEmpty(codeWithTime) || !StringUtils.equals(codeWithTime.split("_")[0], registerVO.getCode())) {
            // 抛出异常
            throw new RegisterPageException(BizCodeEnum.AUTH_CODE_NOT_MATCH);
        }
        // 验证成功，删除redis中的验证码
        redisTemplate.delete(key);
        // 调用远程服务完成保存
        MemberRegisterTO to = new MemberRegisterTO();
        BeanUtils.copyProperties(registerVO, to);
        R r = memberFeignService.register(to);
        if (r.getCode() != 0) {
            // 抛出异常,远程服务执行失败，会返回json格式的r，里面包含了错误信息
            throw new RegisterPageException(r.getCode(), r.getData("msg", String.class));
        }
        return true;
    }

    /**
     * 生成6位随机验证码
     * @return	生成的验证码
     */
    private String randomCode() {

        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < 6; i++) {
            // 1.生成随机数
            double doubleRandom = Math.random();
            // 2.调整
            int integerRandom = (int) (doubleRandom * 10);
            // 3.拼接
            builder.append(integerRandom);
        }
        return builder.toString();
    }
}
