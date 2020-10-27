package com.vivi.gulimall.product.config;

import org.checkerframework.common.value.qual.StringVal;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangwei
 * 2020/10/26 20:30
 *
 * Redisson分布式锁的配置
 */
@Configuration
public class RedissonConfiguration {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;


    /**
     * 基于单节点redis的分布式锁配置
     * @return
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        config.useSingleServer().setAddress(address);
        config.useSingleServer().setPassword(password);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
