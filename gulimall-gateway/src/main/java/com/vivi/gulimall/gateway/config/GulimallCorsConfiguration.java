package com.vivi.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.swing.*;

/**
 * @author wangwei
 * 2020/10/2 9:33
 *
 * 全局跨域处理
 *
 * 这里需要注释掉renren-fast项目中的corsConfig，否则前端会报错 The 'Access-Control-Allow-Origin' header contains multiple values 'http://localhost:8001, http://localhost:8001', but only one is allowed.
 */
@Configuration
public class GulimallCorsConfiguration {

    /**
     * 非一般请求会先发送optional请求进行跨域试探
     * 利用过滤器对其回应允许跨域
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();

        // corsConfiguration 处理策略
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 注意这里调用的是add方法
        // 允许所有请求头
        corsConfiguration.addAllowedHeader("*");
        // 允许所有来源
        corsConfiguration.addAllowedOrigin("*");
        // 允许所有请求方法
        corsConfiguration.addAllowedMethod("*");
        // 允许携带coolie
        corsConfiguration.setAllowCredentials(true);

        // path 要对哪些请求进行跨域处理；corsConfiguration 处理策略
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);

        // 创建过滤器
        return new CorsWebFilter(configurationSource);
    }
}
