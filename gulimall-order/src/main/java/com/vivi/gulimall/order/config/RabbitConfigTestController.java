package com.vivi.gulimall.order.config;

import com.vivi.common.utils.R;
import com.vivi.gulimall.order.entity.OrderEntity;
import com.vivi.gulimall.order.entity.RefundInfoEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * @author wangwei
 * 2021/1/17 21:15
 */
@Controller
public class RabbitConfigTestController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 测试10条消息到mq。发送两种类型的消息
     * @return
     */
    @RequestMapping("/test/sendMq")
    @ResponseBody
    public R sendMq() {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId(System.currentTimeMillis());
                rabbitTemplate.convertAndSend("gulimall-test-exchange",
                        "gulimall.test",
                        orderEntity,
                        new CorrelationData(orderEntity.getId().toString()));
            } else {
                RefundInfoEntity infoEntity = new RefundInfoEntity();
                infoEntity.setRefundContent("哈哈哈哈哈哈" + i);
                rabbitTemplate.convertAndSend("gulimall-test-exchange",
                        "gulimall.test",
                        infoEntity,
                        new CorrelationData(UUID.randomUUID().toString()));
            }
        }
        System.out.println("给rabbitmq发送消息成功");
        return R.ok();
    }

    /**
     * 测试10条消息到mq。其中5条使用错误的路由件
     * @return
     */
    @RequestMapping("/test/sendMq2")
    @ResponseBody
    public R sendMq2() {
        for (int i = 0; i < 10; i++) {
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setId(System.currentTimeMillis());
            if (i % 2 == 0) {
                rabbitTemplate.convertAndSend("gulimall-test-exchange",
                        "gulimall.test",
                        orderEntity,
                        new CorrelationData(orderEntity.getId().toString()));
            } else {
                // 故意使用错误路由件
                rabbitTemplate.convertAndSend("gulimall-test-exchange",
                        "gulimall.test222",
                        orderEntity,
                        new CorrelationData(orderEntity.getId().toString()));
            }
        }
        System.out.println("给rabbitmq发送完毕");
        return R.ok();
    }
}
