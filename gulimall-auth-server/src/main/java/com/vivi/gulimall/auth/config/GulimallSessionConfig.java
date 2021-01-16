package com.vivi.gulimall.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author wangwei
 * 2021/1/1 19:36
 *
 * 使用springsession管理session。使用redis存储会话信息
 */

@EnableRedisHttpSession
@Configuration
public class GulimallSessionConfig {

    /**
     * 序列化机制选择json格式，默认使用jdk序列化。所有要保存的对象都要实现Serializable接口
     * 方法名不能修改
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    /**
     * 自定义服务器返回给浏览器的cookie设置
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // 设置coolie名字
        serializer.setCookieName("GULISESSION"); // <1>
        // 默认coolie有效域是当前域。比如访问auth.gulimall.com服务器返回的cookie数据的有效域就是auth.gulimall.com
        // 为了实现一处登录处处可用。将其有效域设置为顶级域 gulimall.com
        serializer.setDomainName("gulimall.com");
        return serializer;
    }
}
