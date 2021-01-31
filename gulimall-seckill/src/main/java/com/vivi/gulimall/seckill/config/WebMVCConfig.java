package com.vivi.gulimall.seckill.config;

import com.vivi.gulimall.seckill.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author wangwei
 * 2021/1/31 14:00
 *
 * 注册拦截器
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 秒杀下单需要登录
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/seckill/item");
    }
}
