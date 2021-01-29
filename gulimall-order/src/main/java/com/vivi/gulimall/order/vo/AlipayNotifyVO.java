package com.vivi.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * 支付宝支付完会异步通知消息POST
 *
 * 不能使用驼峰命名接收，字段名应该与ali官方文档给出的一致，否则封装请求中的参数会失败
 */
@ToString
@Data
public class AlipayNotifyVO {

    /**
     * 通知的发送时间。格式为 yyyy-MM-dd HH:mm:ss。
     *
     * 示例值：2015-14-27 15:45:58
     */
    private Date notify_time;
    /**
     * 通知的类型。
     *
     * 示例值：trade_status_sync
     */
    private String notify_type;
    /**
     * 通知校验 ID。
     *
     * 示例值：ac05099524730693a8b330c5ecf72da9786
     */
    private String notify_id;
    /**
     * 支付宝分配给开发者的应用 ID。
     *
     * 示例值：2014072300007148
     */
    private String app_id;
    /**
     * 编码格式，如 utf-8、gbk、gb2312 等。
     *
     * 示例值：utf-8
     */
    private String charset;
    /**
     * 调用的接口版本，固定为：1.0。
     *
     * 示例值：1.0
     */
    private String version;
    /**
     * 商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐使用 RSA2。
     *
     * 示例值：RSA2
     */
    private String sign_type;
    /**
     * 签名。详情请参见 异步返回结果的验签。
     * https://opendocs.alipay.com/open/203/105286#%E5%BC%82%E6%AD%A5%E8%BF%94%E5%9B%9E%E7%BB%93%E6%9E%9C%E7%9A%84%E9%AA%8C%E7%AD%BE
     *
     * 示例值：601510b7970e52cc63db0f44997cf70e
     */
    private String sign;
    /**
     * 支付宝交易凭证号。
     *
     * 示例值：2013112011001004330000121536
     */
    private String trade_no;
    /**
     * 原支付请求的商户订单号。
     *
     * 示例值：6823789339978248
     */
    private String out_trade_no;//订单号
    /**
     * 商户业务 ID，主要是退款通知中返回退款申请的流水号。
     *
     * 示例值：HZRF001
     */
    private String out_biz_no;
    /**
     * 买家支付宝账号对应的支付宝唯一用户号。以 2088 开头的纯 16 位数字。
     *
     * 示例值：2088102122524333
     */
    private String buyer_id;//支付者的id
    /**
     * 买家支付宝账号。
     *
     * 示例值：159﹡﹡﹡﹡﹡﹡20
     */
    private String buyer_logon_id;

    /**
     * 卖家支付宝用户号。
     *
     * 示例值：2088101106499364
     */
    private String seller_id;//商家的id
    /**
     * 卖家支付宝账号。
     *
     * 示例值：zhu﹡﹡﹡@alitest.com
     */
    private String seller_email;
    /**
     * 交易目前所处的状态。详情请参见 交易状态说明。
     *
     * 示例值：TRADE_CLOSED
     *
     * WAIT_BUYER_PAY	交易创建，等待买家付款。    【不触发通知】
     * TRADE_CLOSED	未付款交易超时关闭，或支付完成后全额退款。   【触发通知】
     * TRADE_SUCCESS	交易支付成功。         【触发通知】
     * TRADE_FINISHED	交易结束，不可退款。【触发通知】
     */
    private String trade_status;//交易状态  TRADE_SUCCESS
    /**
     * 本次交易支付的订单金额，单位为人民币（元）。
     *
     * 示例值：20
     */
    private String total_amount;
    /**
     * 商家在交易中实际收到的款项，单位为人民币（元）。
     *
     * 示例值：15
     */
    private String receipt_amount;//商家收到的款

    /**
     * 用户在交易中支付的可开发票的金额。
     *
     * 示例值：10.00
     */
    private String invoice_amount;

    /**
     * 用户在交易中支付的金额。
     *
     * 示例值：13.88
     */
    private String buyer_pay_amount;//最终支付的金额
    /**
     * 使用集分宝支付的金额。
     *
     * 示例值：12.00
     */
    private String point_amount;
    /**
     * 退款通知中，返回总退款金额，单位为人民币（元），支持两位小数。
     *
     * 示例值：2.58
     *
     */
    private String refund_fee;
    /**
     * 商品的标题/交易标题/订单标题/订单关键字等，是请求时对应的参数，原样通知回来。
     *
     * 示例值：当面付交易
     */
    private String subject;

    /**
     * 该订单的备注、描述、明细等。对应请求时的 body 参数，原样通知回来。
     *
     * 示例值：当面付交易内容
     */
    private String body;//订单的信息

    /**
     * 该笔交易创建的时间。格式为yyyy-MM-dd HH:mm:ss
     *
     * 示例值：2015-04-27 15:45:57
     */
    private Date gmt_create;
    /**
     * 该笔交易的买家付款时间。格式为yyyy-MM-dd HH:mm:ss
     *
     * 示例值：2015-04-27 15:45:57
     */
    private Date gmt_payment;
    /**
     * 该笔交易的退款时间。格式为yyyy-MM-dd HH:mm:ss.S
     *
     * 示例值：2015-04-28 15:45:57.320
     */
    private Date gmt_refund;
    /**
     * 该笔交易结束时间。格式为yyyy-MM-dd HH:mm:ss
     *
     * 示例值：2015-04-27 15:45:57
     */
    private Date gmt_close;


    /**
     * 支付成功的各个渠道金额信息。详情请参见 资金明细信息说明。
     * https://opendocs.alipay.com/open/203/105286#%E8%B5%84%E9%87%91%E6%98%8E%E7%BB%86%E4%BF%A1%E6%81%AF%E8%AF%B4%E6%98%8E
     *
     * 示例值：[{"amount":"15.00","fundChannel":"ALIPAYACCOUNT"}]
     */
    private String fund_bill_list;
    /**
     * 公共回传参数，如果请求时传递了该参数，则返回给商户时会在异步通知时将该参数原样返回。本参数必须进行UrlEncode之后才可以发送给支付宝。
     *
     * 示例值：merchantBizType%3d3C%26merchantBizNo%3d2016010101111
     */
    private String passback_params;


    /**
     * 本交易支付时所使用的所有优惠券信息，详见优惠券信息说明
     * https://opendocs.alipay.com/open/#s5
     *
     * 示例值：[{"amount":"0.20","merchantContribute":"0.00","name":"一键创建券模板的券名称","otherContribute":"0.20","type":"ALIPAY_DISCOUNT_VOUCHER","memo":"学生卡8折优惠"]
     */
    private String voucher_detail_list;


}
