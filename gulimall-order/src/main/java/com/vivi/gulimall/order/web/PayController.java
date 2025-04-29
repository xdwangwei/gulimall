package com.vivi.gulimall.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.vivi.gulimall.order.config.AlipayTemplate;
import com.vivi.gulimall.order.service.OrderService;
import com.vivi.gulimall.order.vo.AlipayNotifyVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangwei
 * 2021/1/28 20:57
 */
@Slf4j
@Controller
public class PayController {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    /**
     * 告诉浏览器，返回的字符串是text/html
     * @param orderSn
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/pay/{orderSn}", produces = "text/html;charset=UTF-8")
    public String pay(@PathVariable("orderSn") String orderSn) {
        return orderService.payOrder(orderSn);
    }

    /**
     * 支付宝支付完成后，向我们指定的地址发送支付结果POST
     *
     * 程序执行完后必须打印输出 success。如果商户反馈给支付宝的字符不是 success 这 7 个字符，支付宝服务器会不断重发通知，直到超过 24 小时 22 分钟
     * @return
     */
    @ResponseBody
    @PostMapping("/alipay/notify")
    public String payNotify(AlipayNotifyVO notifyVO,
                            HttpServletRequest request) {
        log.info("支付宝异步通知消息：" + notifyVO);
        return orderService.handleAlipayNotify(notifyVO, request);
    }
}
