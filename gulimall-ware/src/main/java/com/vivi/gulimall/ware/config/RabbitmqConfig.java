package com.vivi.gulimall.ware.config;

import com.vivi.common.constant.WareConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangwei
 * 2021/1/27 15:55
 *
 * 首先开启，@EnableRabbit
 * 可以 @Autowired AmqpAdmin手动创建，也可以@Bean自动创建
 * 只有在真正监听某个队列的时候，或者发送消息，或者使用amqpadmin创建别的队列，这些不存在的交换机和队列以及绑定关系就会被自动创建
 * 如果已有，则不会重复创建，哪怕自己指定了别的参数，也不会创建一个新的去覆盖
 */
@Configuration
public class RabbitmqConfig {

    /**
     * 消息转换器，json格式
     * @return
     */
    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange(WareConstant.STOCK_EVENT_EXCHANGE,
                                true,
                                false);
    }

    @Bean
    public Queue stockReleaseQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        // 			@Nullable Map<String, Object> arguments
        return new Queue(WareConstant.STOCK_RELEASE_QUEUE,
                        true,
                        false,
                        false);
    }

    /**
     * 死信队列/延时队列
     * @return
     *
     * x-dead-letter-exchange="stock-event-exchange"
     * x-dead-letter-routing-key="stock.release"
     * x-message-ttl="60000"
     */
    @Bean
    public Queue stockDelayQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        // 			@Nullable Map<String, Object> arguments
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", WareConstant.DEAD_LETTER_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", WareConstant.DEAD_LETTER_ROUTING_KEY);
        arguments.put("x-message-ttl", WareConstant.DEAD_LETTER_TTL);
        return new Queue(WareConstant.STOCK_DELAY_QUEUE,
                true,
                false,
                false,
                arguments);
    }

    /**
     * 绑定关系
     */
    @Bean
    public Binding stockLockBind() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			@Nullable Map<String, Object> arguments
        return new Binding(WareConstant.STOCK_DELAY_QUEUE,
                            Binding.DestinationType.QUEUE,
                            WareConstant.STOCK_EVENT_EXCHANGE,
                            WareConstant.STOCK_LOCKED_ROUTING_KEY,
                            null);
    }

    @Bean
    public Binding stockReleaseBind() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			@Nullable Map<String, Object> arguments
        return new Binding(WareConstant.STOCK_RELEASE_QUEUE,
                Binding.DestinationType.QUEUE,
                WareConstant.STOCK_EVENT_EXCHANGE,
                WareConstant.STOCK_RELEASE_ROUTING_KEY,
                null);
    }

}
