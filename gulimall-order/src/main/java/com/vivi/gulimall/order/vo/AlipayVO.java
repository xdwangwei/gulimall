package com.vivi.gulimall.order.vo;

import lombok.Data;

// 支付宝支付提交
@Data
public class AlipayVO {
    private String outTradeNo; // 商户订单号 必填
    private String subject; // 订单名称 必填
    private String totalAmount;  // 付款金额 必填，必须是小数点后两位
    private String body; // 商品描述 可空
}
