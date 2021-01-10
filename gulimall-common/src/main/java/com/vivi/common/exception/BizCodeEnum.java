package com.vivi.common.exception;

/***
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 *  10: 通用
 *      001：参数格式校验
 *  11: 商品
 *  12: 订单
 *  13: 购物车
 *  14: 物流
 *
 *
 */
public enum BizCodeEnum {

    UNKNOW_EXCEPTION(00000,"系统未知异常"),
    // 商品模块异常
    PRODUCT_VAILD_EXCEPTION(10001,"参数格式校验失败"),

    PRODUCT_UP_FAILED(10111, "商品上架至ES失败"),

    WARE_PURCHASE_MERGE_EXCEPTION(80001, "无法将采购项合并到已被领取的采购单"),

    WARE_PURCHASE_ASSIGN_EXCEPTION(80002, "只能给新建的采购单分配采购员");

    private int code;
    private String msg;

    BizCodeEnum(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
