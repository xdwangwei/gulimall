package com.vivi.gulimall.ware.controller;

import com.vivi.common.to.OrderLockStockTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * @author wangwei
 * 2021/1/27 17:08
 */
@Controller
public class TestController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/ware/test/sendmq")
    public String sendmq() {
        OrderLockStockTO orderLockStockTO = new OrderLockStockTO();
        orderLockStockTO.setOrderSn(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked", orderLockStockTO);
        return "ok";
    }
}
