package com.vivi.gulimall.cart.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author wangwei
 * 2021/1/16 12:14
 *
 * 购物车--用于结算页查看总金额
 *
 * 需要计算的属性，重写其get方法，保证每次或者这个属性值都是重新计算的最新值
 *
 * 这样我们只需要关注于 items ，其他属性每次获取值都能自动计算
 */
public class CartVO {

    /**
     * 全部购物项
     */
    @Getter
    @Setter
    private List<CartItemVO> items;

    /**
     * 总共几件商品
     */
    @Setter
    private Integer totalCount;

    /**
     * 总共是几种类型的商品
     */
    @Setter
    private Integer totalType;

    /**
     * 总价格
     */
    @Setter
    private BigDecimal totalPrice;

    /**
     * 总减免金额，这里先写死
     */
    @Getter @Setter
    private BigDecimal totalReduce = new BigDecimal("0");

    public Integer getTotalCount() {
        if (!CollectionUtils.isEmpty(items)) {
            return items.stream().mapToInt(CartItemVO::getCount).sum();
        }
        return 0;
    }

    /**
     * 总共几种类型商品
     * @return
     */
    public Integer getTotalType() {
        if (items != null) {
            return items.size();
        }
        return 0;
    }

    /**
     * 当前【所选】购物项总价格
     * @return
     */
    public BigDecimal getTotalPrice() {
        BigDecimal total = new BigDecimal("0");
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItemVO item : items) {
                if (item.getChecked()) {
                    total = total.add(item.getTotalPrice());
                }
            }
        }
        return total.subtract(totalReduce);
    }

}
