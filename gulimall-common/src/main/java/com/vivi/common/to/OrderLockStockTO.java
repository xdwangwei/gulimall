package com.vivi.common.to;

import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/26 11:20
 */
@Data
public class OrderLockStockTO {

    private String orderSn;

    private List<SkuLockStock> locks;

    @Data
    public static class SkuLockStock {
        private Long skuId;

        private String skuName;

        private Integer count;
    }
}
