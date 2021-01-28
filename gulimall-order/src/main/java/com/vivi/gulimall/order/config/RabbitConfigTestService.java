package com.vivi.gulimall.order.config;

import com.rabbitmq.client.Channel;
import com.vivi.gulimall.order.entity.OrderEntity;
import com.vivi.gulimall.order.entity.RefundInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wangwei
 * 2021/1/17 20:38
 *
 * 对于配置好的rabbitmq的测试
 *
 * 关于创建队列、交换机、绑定关系、测试消息发送请看测试类 RabbitTest.java
 *
 * 测试完毕取消这个类的监听
 */
@Slf4j
// @Service           测试完毕取消这个类的监听
// @RabbitListener(queues = {"gulimall-test-queue"})
public class RabbitConfigTestService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 测试消息的监听处理
     *
     * 监听队列的方法参数，
     *     1、可以使用Object obj来接收消息内容，通过obj.getClass()能看到
     *              真正类型是 org.springframework.amqp.core.Message
     *     2、所以也可以直接使用 Message msg来接收，包含请求头请求体等完整内容，通过msg.getBody()能够得到消息体
     *     3、如果知道消息体的本质类型，也可以直接使用 XXXEntity 来接收，直接得到消息体
     *
     * @RabbitListener 可以标注在方法上或类上
     *      标注在方法上：指定这个方法监听哪个队列，可以指定多个
     *      标注在类上：一般结合 @RabbitHandler
     *          在类上指定监听哪个队列
     *          再用@RabbitHandler标注在方法上指定这个方法是个监听器，同时根据接收参数不同来消费处理这个队列了不同类型的消息
     */
    // @RabbitListener(queues = {"gulimall-test-queue"})
    @RabbitHandler
    public void getMessage(Message message, OrderEntity orderEntity, Channel channel) {
        System.out.println("################接收到类型为 OrderEntity 的消息：");
        System.out.println("Message: " + message);
        System.out.println("OrderEntity: " + orderEntity);
        // System.out.println("Channel: " + channel);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // channel.basicReject();
            // channel.basicNack();
            // 手动ack，选择性签收，没有签收的会被丢弃
            System.out.println("deliverytag: " + deliveryTag);
            if (deliveryTag % 2 == 0) {
                channel.basicAck(deliveryTag, false);
                System.out.println("###########签收消息[deliveryTag=" + deliveryTag + "]");
            }
        } catch (Exception e) {
            System.out.println("网络异常：" + e.getMessage());
        }
        System.out.println("################处理完成####################");
    }

    /**
     * 与上个处理集合，处理同意队列中不同类型的消息
     * @param message
     * @param refundInfoEntity
     * @param channel
     */
    @RabbitHandler
    public void getMessage1(Message message, RefundInfoEntity refundInfoEntity, Channel channel) {
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$接收到类型为 RefundInfoEntity 的消息：");
        System.out.println("Message: " + message);
        System.out.println("OrderEntity: " + refundInfoEntity);
        // System.out.println("Channel: " + channel);
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$处理完成$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }
}
