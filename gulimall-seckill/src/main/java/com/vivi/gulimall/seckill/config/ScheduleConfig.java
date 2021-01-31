package com.vivi.gulimall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wangwei
 * 2021/1/29 20:38
 *
 * 开启定时任务，开启异步定时任务
 */
@Configuration
@EnableAsync
@EnableScheduling
public class ScheduleConfig {
}
