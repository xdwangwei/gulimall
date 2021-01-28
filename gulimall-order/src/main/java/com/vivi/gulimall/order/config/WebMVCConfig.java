package com.vivi.gulimall.order.config;

import com.vivi.gulimall.order.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author wangwei
 * 2021/1/16 10:09
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有请求
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**");
    }
}
