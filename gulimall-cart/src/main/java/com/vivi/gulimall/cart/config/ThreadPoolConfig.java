package com.vivi.gulimall.cart.config;

import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.utils.R;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.*;

/**
 * @author wangwei
 * 2021/1/12 17:30
 */
@ControllerAdvice
@Configuration
public class ThreadPoolConfig {

    /**
     * corePoolSize: 核心线程数
     * maximumPoolSize: 最大线程数
     * keepAliveTime: 核心线程以外的线程空闲多长时间将被释放
     * unit: 时间单位
     * blockQueue: 阻塞队列，超过最大线程数之后的请求会放进阻塞队列等待执行
     * factory: 线程池创建工厂
     * handler: 阻塞队列满之后，其他请求如何处理(使用默认策略记得处理异常)
     *       ThreadPoolExecutor.AbortPolicy 默认：抛出异常 RejectedExecutionException
     *       ThreadPoolExecutor.CallerRunsPolicy,直接执行线程的call方法，相当于同步执行
     *        ThreadPoolExecutor.DiscardPolicy：直接抛弃，不处理
     *        ThreadPoolExecutor.DiscardOldestPolicy：抛弃最久未处理的请求(队列头)，尝试执行新请求
     * @param properties
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolProperties properties) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                properties.getCoreSize(),
                properties.getMaximumSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(properties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }

    @ResponseBody
    @ExceptionHandler({RejectedExecutionException.class})
    public R handler() {
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
    }
}
