package com.vivi.common.constant;

/**
 * @author wangwei
 * 2020/10/19 22:13
 */
public class WareConstant {

    /**
     * 采购单状态
     */
    public enum PurchaseStatus {
        CREATED(0, "刚创建"),
        ASSIGNED(1,  "已分配"),
        RECEIVED(2,  "已领取"),
        FINISHED(3, "已完成"),
        WRONG(4,  "有异常");

        private int value;
        private String desc;

        PurchaseStatus(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }


        public String getDesc() { return desc; }
    }

    /**
     * 采购需求状态
     */
    public enum PurchaseDetailStatus {
        CREATED(0, "刚创建"),
        ASSIGNED(1,  "已分配"),
        DOING(2,  "正在采购"),
        FINISHED(3, "已完成"),
        FAILED(4,  "已失败");

        private int value;
        private String desc;

        PurchaseDetailStatus(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }


        public String getDesc() { return desc; }
    }
}
