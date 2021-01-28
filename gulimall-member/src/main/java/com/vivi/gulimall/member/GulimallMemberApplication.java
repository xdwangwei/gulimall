package com.vivi.gulimall.member;

import io.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@EnableDiscoveryClient
@SpringBootApplication(exclude = {SeataAutoConfiguration.class})
public class GulimallMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallMemberApplication.class, args);
	}

	/**
	 * 放入一个密码加密器
	 * @return
	 */
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
