package com.vivi.common.constant;

/**
 * @author wangwei
 * 2021/1/23 11:13
 */
public class OrderConstant {

    public static final String ORDER_TOKEN_PREFIX = "order:token:";

    // 消息队列，交换机和队列
    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";
    public static final String ORDER_DELAY_ORDER_QUEUE = "order.delay.order.queue";
    public static final String ORDER_RELEASE_ORDER_QUEUE = "order.release.order.queue";
    public static final String ORDER_RELEASE_COUPON_QUEUE = "order.release.coupon.queue";
    public static final String ORDER_RELEASE_ORDER_ROUTING_KEY = "order.release.order.#";
    public static final String ORDER_RELEASE_COUPON_ROUTING_KEY = "order.release.coupon.#";
    public static final String ORDER_RELEASE_STOCK_ROUTING_KEY = "order.release.stock.#";
    public static final String ORDER_CREATE_ROUTING_KEY = "order.create.#";
    public static final String DEAD_LETTER_EXCHANGE = "order-event-exchange";
    public static final String ORDER_DEAD_LETTER_ROUTING_KEY = "order.release.order";
    public static final Integer DEAD_LETTER_TTL = 2 * 60 * 1000; // 单位是ms
    public static final String ORDER_SECKILL_DEAL_QUEUE = "order.seckill.deal.queue";
    public static final String ORDER_SECKILL_DEAL_QUEUE_ROUTING_KEY = "order.seckill.deal.#";

    public enum  OrderStatusEnum {
        CREATE_NEW(0,"待付款"),
        PAYED(1,"已付款"),
        SENDED(2,"已发货"),
        RECIEVED(3,"已完成"),
        CANCLED(4,"已取消"),
        SERVICING(5,"售后中"),
        SERVICED(6,"售后完成");
        private Integer code;
        private String msg;

        OrderStatusEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum PayType {

        ALIPAY(1,"支付宝"),
        WECHAT(2,"微信"),
        UNIONPAY(3,"银联"),
        CASH_ON_DELIVERY(4,"货到付款");

        private Integer code;
        private String msg;

        PayType(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

    }
}
