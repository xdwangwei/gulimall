package com.vivi.gulimall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.vivi.gulimall.cart.feign")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallCartApplication {

    // 哈哈哈哈哈你说神马好呢搜剿解耦多四季豆
    public static void main(String[] args) {
        SpringApplication.run(GulimallCartApplication.class, args);
    }

}
