package com.vivi.gulimall.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class GulimallCartApplicationTests {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {

    }

}
