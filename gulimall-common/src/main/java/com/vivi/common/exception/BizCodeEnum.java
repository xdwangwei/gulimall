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
public enum BizCodeEnum implements CommonError{

    // 系统异常
    UNKNOW_ERROR(00001,"系统未知异常"),
    TOO_MANY_REQUEST(00002, "请求太频繁，请稍后重试"),
    THREAD_POOL_TASK_FAILED(00003, "线程池执行任务失败"),
    CALL_FEIGN_SERVICE_FAILED(00004, "调用远程服务失败"),


    // 商品模块异常
    PRODUCT_PARAM_INVAILD(10001,"参数格式校验失败"),

    PRODUCT_STATUS_UP_FAILED(10111, "商品上架至ES失败"),

    // 库存模块异常
    WARE_PURCHASE_MERGE_FAILED(11001, "无法将采购项合并到已被领取的采购单"),

    WARE_PURCHASE_ASSIGN_FAILED(11002, "只能给新建的采购单分配采购员"),

    // 认证服务异常
    AUTH_PARAM_INVAILD(30001,"参数格式校验失败"),
    AUTH_CODE_NOT_MATCH(30002, "验证码验证失败，请重试或重新获取"),
    AUTH_WEIBO_LOGIN_FAILED(30003, "微博登录失败，请重试"),

    // 会员服务异常
    MEMBER_ALREADY_EXIST(80001, "用户已存在"),
    MEMBER_NOT_EXIST(80002, "账户不存在"),
    MEMBER_ACCOUNT_PASSWORD_NOT_MATCH(80003, "用户名或密码错误"),

    // 第三方服务异常
    SMS_SEND_CODE_FAILED(20001, "短信验证码发送失败"),
    OSS_GET_POLICY_FAILED(20002, "获取阿里OSS文件上传签名失败");

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

    @Override
    public int getErrorCode() {
        return code;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}
