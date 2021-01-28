package com.vivi.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.vivi.common.constant.WareConstant;
import com.vivi.common.to.OrderTO;
import com.vivi.common.to.mq.StockLockTO;
import com.vivi.gulimall.ware.service.WareOrderTaskDetailService;
import com.vivi.gulimall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author wangwei
 * 2021/1/27 16:25
 *
 * 监听延时1min后延时队列
 *
 * 解锁库存：两种消息
 *  1、手动取消订单后，发过来的消息。明确指出解锁库存 (首要解锁库存机制)
 *  2、库存锁定时发过来的消息已在延时队列中待满30min (自动补偿机制)
 *      我们需要判断：
 *      1、库存扣减时的任务单已不存在，说明扣减库存时，发送完消息后，其他业务逻辑失败，导致数据库回滚，此时无需再手动恢复
 *      2、库存扣减时的任务单仍然存在，且仍然是已锁定状态，且当时的订单已不存在(异常回滚)，或者已变成被取消状态
 *
 *
 * 为了保证解锁库存可靠完成，需要开启rabbitmq手动ack模式，解锁完成手动ack，出现问题reject并重新入队
 */
@Slf4j
@Component
@RabbitListener(queues = {WareConstant.STOCK_RELEASE_QUEUE})
public class StockReleaseListener {

    @Autowired
    WareOrderTaskDetailService taskDetailService;

    @Autowired
    WareSkuService wareSkuService;


    /**
     * 自动补偿机制，收到消息2
     * @param stockLockTO
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleStockLockMessage(StockLockTO stockLockTO, Message message, Channel channel) throws IOException {
        System.out.println("收到过期锁库存消息：库存工作单号：" + stockLockTO.getTaskDetailId());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.unlockStock(stockLockTO);
            // 消费成功，手动ack
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 消费失败，消息重新入队
            channel.basicReject(deliveryTag, true);
            log.error("消息队列手动ack失败：com.vivi.gulimall.ware.listener.StockReleaseListener.handleStockLockMessage, Error: {}", e.getMessage());
        }
    }

    /**
     * 订单系统手动取消订单，告诉库存系统解锁库存的消息
     * @param message
     * @param orderTO
     * @param channel
     */
    @RabbitHandler
    public void handleReaseOrderMessage(Message message, OrderTO orderTO, Channel channel) throws IOException {
        System.out.println("收到过期订单消息，订单号：" + orderTO.getOrderSn());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.unlockStock(orderTO);
            // 消费成功，手动ack
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 消费失败，消息重新入队
            channel.basicReject(deliveryTag, true);
            log.error("消息队列手动ack失败：com.vivi.gulimall.ware.listener.StockReleaseListener.handleReaseOrderMessage, Error: {}", e.getMessage());
        }
    }
}
