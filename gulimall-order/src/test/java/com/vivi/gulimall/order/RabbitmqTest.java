package com.vivi.gulimall.order;

import com.vivi.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wangwei
 * 2021/1/17 20:01
 */
@Slf4j
public class RabbitmqTest extends GulimallOrderApplicationTests{

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 创建交换机
     *      DirectExchange
     *      TopicExchange
     */
    @Test
    public void createExchange() {
        /**
         * DirectExchange(String name, boolean durable, boolean autoDelete, Map<String,Object> arguments)
         * name: 交换机名字
         * durable: 是否持久化，消息队列代理服务重启后是否还存在
         * autoDelete: 这个交换机不再使用时，服务器是否会自动删除
         * arguments：其他参数
         */
        DirectExchange directExchange = new DirectExchange("gulimall-test-exchange" ,true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("创建交换机[gulimall-test-exchange]成功");
    }

    /**
     * 创建队列
     *
     */
    @Test
    public void createQueue() {
        /**
         * Queue(String name, boolean durable, boolean exclusive, boolean autoDelete,
         *                        @Nullable Map<String, Object> arguments)
         * name: 队列名字
         * durable: 是否持久化，消息队列代理服务重启后是否还存在
         * exclusive: 只对首次声明它的连接（Connection）可见，即创建一个只有创建者可见的队列，不允许其它用户访问
         * autoDelete: 这个队列不再使用时，服务器是否会自动删除
         * arguments：其他参数
         */
        Queue queue = new Queue("gulimall-test-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("创建队列[gulimall-test-queue]成功");
    }

    /**
     * 创建一个绑定关系，将交换机绑定到哪里
     */
    @Test
    void createBinding() {
        /**
         * Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
         *                        @Nullable Map<String, Object> arguments)
         * destination: 目的地
         * destinationType: 目的地类型，可以是交换机，也可以是队列
         * exchange: 要绑定的交换机
         * routingKey: 所使用的路由件
         * arguments: 其他参数
         */
        Binding binding = new Binding("gulimall-test-queue", Binding.DestinationType.QUEUE, "gulimall-test-exchange", "gulimall.test", null);
        amqpAdmin.declareBinding(binding);
        log.info("创建绑定关系成功");
    }

    /**
     * 测试消息发送
     */
    @Test
    void testSendMessage() {
        /**
         * convertAndSend(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor,
         * 			CorrelationData correlationData)
         * exchange: 发送到哪个交换机
         * routingKey: 使用哪个路由件
         * message：消息内容，可以是任意类型
         * messagePostProcessor： 消息发送前的处理器，比如加上消息头等信息
         * correlationData：与消息关联的唯一数据，用于消息确认机制(可认为是唯一辨识id)
         *      如果开启了生产者消息确认机制，则在发送消息时会带上这个，可以在消息头中看到这一条
         *          spring_returned_message_correlation=1610891599226
         *      如果没有开启生产者消息确认机制【默认】，即使发消息时带上了这个参数，并不会被保存到消息头中
         */
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(System.currentTimeMillis());
        rabbitTemplate.convertAndSend("gulimall-test-exchange", "gulimall.test",
                orderEntity
                /*,new CorrelationData(orderEntity.getId().toString())*/);
        log.info("给rabbitmq发送消息成功");
    }

    /**
     * 测试消息的监听处理
     */

}
