package com.vivi.gulimall.cart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author wangwei
 * 2021/1/16 10:09
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/list.html").setViewName("cartList");
        registry.addViewController("/success.html").setViewName("success");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

    }
}
