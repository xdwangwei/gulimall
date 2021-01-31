package com.vivi.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.vivi.common.constant.OrderConstant;
import com.vivi.common.to.mq.SeckillOrderTO;
import com.vivi.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author wangwei
 * 2021/1/31 15:12
 *
 * 秒杀服务发送过来的下单消息
 */
@Slf4j
@Component
@RabbitListener(queues = {OrderConstant.ORDER_SECKILL_DEAL_QUEUE})
public class SeckillOrderListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void handleSeckillOrderMessage(SeckillOrderTO seckillOrderTO, Message message, Channel channel) throws IOException {
        System.out.println("收到秒杀订单：" + seckillOrderTO.getOrderSn());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            orderService.createSeckillOrder(seckillOrderTO);
            // 消费成功，手动ack
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 消费失败，消息重新入队
            channel.basicReject(deliveryTag, true);
            log.error("消息队列手动ack失败：com.vivi.gulimall.order.listener.SeckillOrderListener.handleSeckillOrderMessage, Error: {}", e);
        }
    }
}
