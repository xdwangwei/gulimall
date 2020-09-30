package com.vivi.gulimall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关需要开启服务的注册发现，就需要配置nacos
 * 本应用依赖了common，common中导入了mybatis相关依赖，自动配置中会去连接数据库，需要配置数据源
 * 但是这里不需要数据源，所以需要排除这个自动配置或者从pom中排除掉相关依赖
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallGatewayApplication.class, args);
	}

}
