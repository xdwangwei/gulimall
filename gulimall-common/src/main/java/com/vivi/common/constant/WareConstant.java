package com.vivi.common.constant;

/**
 * @author wangwei
 * 2020/10/19 22:13
 */
public class WareConstant {

    // 消息队列，交换机和队列
    public static final String STOCK_EVENT_EXCHANGE = "stock-event-exchange";
    public static final String STOCK_DELAY_QUEUE = "stock.delay.queue";
    public static final String STOCK_RELEASE_QUEUE = "stock.release.queue";
    public static final String STOCK_RELEASE_ROUTING_KEY = "stock.release.#";
    public static final String STOCK_LOCKED_ROUTING_KEY = "stock.locked.#";
    public static final String DEAD_LETTER_EXCHANGE = "stock-event-exchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "stock.release";
    public static final Integer DEAD_LETTER_TTL = 2 * 60 * 1000; // 单位是ms

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
        WAITING(1, "待分配"),
        ASSIGNED(2,  "已分配"),
        DOING(3,  "正在采购"),
        FINISHED(4, "已完成"),
        FAILED(5,  "已失败");

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

    /**
     * 商品库存锁定状态
     */
    public enum StockLockStatus {
        LOCKED(1, "已锁定"),
        RELEASED(2, "已释放"),
        DEDUCTED(3,  "已扣减");

        private int value;
        private String desc;

        StockLockStatus(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }

        public String getDesc() { return desc; }
    }
}
