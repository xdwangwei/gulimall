package com.vivi.gulimall.order.config;

import com.vivi.common.constant.OrderConstant;
import com.vivi.common.constant.WareConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangwei
 * 2021/1/17 17:48
 *
 * 开启rabbitmq自动配置，并添加自定义配
 *
 * 如果只需要创建Exchange，queue，binding，发送消息等，只需使用AmqpAdmin就足够，
 * 但如果要使用 @RabbitListener监听消息(消费)，必须有 @EnableRabbit开启功能
 * 这个注解其实是往容器中注册了RabbitListenerAnnotationBeanPostProcessor
 *
 * 保证消息不丢失：
 * 1、开始publisher确认机制(confirmCallback,returnCallback)和consumer确认(手动ack/nack)
 * 2、每条发送的消息在数据库做好记录，定期扫描数据库将发送失败的消息重新发送。
 */
@EnableRabbit
@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 如果发送的消息是一个对象，会使用序列化机制，由MessageConverter转换器处理，
     * 默认是WhiteListDeserializingMessageConverter，使用jdk序列化，所以这些bean必须实现Serializable接口
     *
     * 为了使用json序列化，我们需要往容器中添加一个使用json格式的MessageConverter，发送的消息会标记这个对象的全类名
     *
     * 监听队列的方法参数，可以使用Object obj来接收消息内容，通过obj.getClass()能看到真正类型是 org.springframework.amqp.core.Message
     * 所以也可以直接使用 Message me来接收，通过me.getBody()能够得到消息体
     * 如果知道消息体的本质类型，也可以直接使用 XXXEntity 来接收，
     * @return
     */
    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct
    public void setCallback() {
        /**
         * 消息由生产者投递到Broker/Exchange回调
         */
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息投递到交换机成功：[correlationData={}]",correlationData);
            } else {
                log.error("消息投递到交换机失败：[correlationData={}，原因：{}]", correlationData, cause);
            }
        });
        /**
         * 消息由Exchange路由到Queue失败回调
         */
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.error("路由到队列失败，消息内容：{}，交换机：{}，路由件：{}，回复码：{}，回复文本：{}", message, exchange, routingKey, replyCode, replyText);
        });
    }

    @Bean
    public Exchange orderEventExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange(OrderConstant.ORDER_EVENT_EXCHANGE,
                true,
                false);
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        // 			@Nullable Map<String, Object> arguments
        return new Queue(OrderConstant.ORDER_RELEASE_ORDER_QUEUE,
                true,
                false,
                false);
    }

    // 暂时未使用，订单取消时，要回滚扣减的优惠
    @Bean
    public Queue orderReleaseCouponQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        // 			@Nullable Map<String, Object> arguments
        return new Queue(OrderConstant.ORDER_RELEASE_COUPON_QUEUE,
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
    public Queue orderDelayQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        // 			@Nullable Map<String, Object> arguments
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", OrderConstant.DEAD_LETTER_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", OrderConstant.DEAD_LETTER_ROUTING_KEY);
        arguments.put("x-message-ttl", OrderConstant.DEAD_LETTER_TTL);
        return new Queue(OrderConstant.ORDER_DELAY_ORDER_QUEUE,
                true,
                false,
                false,
                arguments);
    }

    /**
     * 绑定关系
     */
    @Bean
    public Binding orderCreateBinding() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			@Nullable Map<String, Object> arguments
        return new Binding(OrderConstant.ORDER_DELAY_ORDER_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderConstant.ORDER_EVENT_EXCHANGE,
                OrderConstant.ORDER_CREATE_ROUTING_KEY,
                null);
    }

    @Bean
    public Binding orderReleaseOrderBind() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			@Nullable Map<String, Object> arguments
        return new Binding(OrderConstant.ORDER_RELEASE_ORDER_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderConstant.ORDER_EVENT_EXCHANGE,
                OrderConstant.ORDER_RELEASE_ORDER_ROUTING_KEY,
                null);
    }

    /**
     * 绑定到库存的解锁队列，
     * @return
     */
    @Bean
    public Binding orderReleaseStockBinding() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			@Nullable Map<String, Object> arguments
        return new Binding(WareConstant.STOCK_RELEASE_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderConstant.ORDER_EVENT_EXCHANGE,
                OrderConstant.ORDER_RELEASE_STOCK_ROUTING_KEY,
                null);
    }
}
