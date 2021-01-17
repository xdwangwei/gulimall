package com.vivi.gulimall.cart.service;


import com.vivi.gulimall.cart.vo.CartItemVO;
import com.vivi.gulimall.cart.vo.CartVO;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/16 12:32
 */
public interface CartService {

    /**
     * 获取当前登录用户的全部购物项列表
     * @return
     */
    CartVO getCart();

    /**
     * 添加某个商品到当前登录用户的购物车
     * @param skuId
     * @param count
     * @return
     */
    CartItemVO addToCart(Long skuId, Integer count);

    /**
     * 获取当前登录用户购物车某个购物项
     * @param skuId
     * @return
     */
    CartItemVO getCartItem(Long skuId);

    void deleteBatch(List<String> skuIds);


    /**
     * 改变购物车中某个购物项选中状态
     */
    void changeItemStatus(String skuId, Boolean checked);

    /**
     * 改变购物车中某个购物项的数量
     */
    void changeItemCount(String skuId, Integer count);
}
