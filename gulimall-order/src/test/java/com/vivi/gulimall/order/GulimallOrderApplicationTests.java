package com.vivi.gulimall.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;

@SpringBootTest
class GulimallOrderApplicationTests {

	@Autowired
	StringRedisTemplate redisTemplate;

	@Test
	void contextLoads() {
		String key = "abc", arg = "hhh";
		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
		Long res = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(key), arg);
		System.out.println(res);
		key = "hello";
		arg = "world";
		Long res2 = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(key), arg);
		System.out.println(res2);
		key = "hel";
		Long res3 = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(key), arg);
		System.out.println(res3);
	}

}
