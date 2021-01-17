package com.vivi.gulimall.cart.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author wangwei
 * 2021/1/16 12:08
 *
 * 购物车中每一项------用于查看购物车列表
 *
 * 需要计算的属性，重写其get方法，保证每次或者这个属性值都是重新计算的最新值
 */
public class CartItemVO {

    @Getter @Setter
    private Long skuId;
    @Getter @Setter
    private Boolean checked;
    @Getter @Setter
    private String skuImg;
    @Getter @Setter
    private String skuTitle;
    @Getter @Setter
    private List<String> attrs;
    @Getter @Setter
    private BigDecimal price;
    @Getter @Setter
    private Integer count;
    @Setter
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(count.toString()));
    }
}
