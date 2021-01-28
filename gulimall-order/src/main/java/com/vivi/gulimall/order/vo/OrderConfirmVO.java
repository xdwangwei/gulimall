package com.vivi.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author wangwei
 * 2021/1/18 16:36
 *
 * 订单详情，订单确认页数据
 */
public class OrderConfirmVO {

    // 订单商品项
    @Getter @Setter
    List<OrderSkuVO> items;

    // 收货列表
    @Getter @Setter
    List<MemberAddressVO> addresses;

    // 发票信息

    // 优惠信息，满减、优惠券等
    /**
     * 用用户积分代替
     */
    @Getter @Setter
    private Integer integration;

    /**
     * 总价
     */
    @Setter
    private BigDecimal totalPrice;

    /**
     * 应付价格
     */
    @Setter
    private BigDecimal payPrice;

    /**
     * 防刷令牌，避免重复提交
     */
    @Getter @Setter
    private String token;

    /**
     * 实时计算总数
     * @return
     */
    public Integer getTotalCount() {
        if (!CollectionUtils.isEmpty(items)) {
            return items.stream().mapToInt(OrderSkuVO::getCount).sum();
        }
        return 0;
    }

    /**
     * 实时计算总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        BigDecimal total = new BigDecimal("0");
        if (!CollectionUtils.isEmpty(items)) {
            for (OrderSkuVO item : items) {
                total = total.add(item.getTotalPrice());
            }
        }
        return total;
    }

    /**
     * 实时计算应付价格 = 总价 - 优惠，这里不考虑优惠
     */
    public BigDecimal getPayPrice() {
        return getTotalPrice();
    }
}
