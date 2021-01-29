package com.vivi.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.to.OrderTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.order.entity.OrderEntity;
import com.vivi.gulimall.order.vo.AlipayNotifyVO;
import com.vivi.gulimall.order.vo.OrderConfirmVO;
import com.vivi.gulimall.order.vo.OrderCreateVO;
import com.vivi.gulimall.order.vo.OrderSubmitVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 订单
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:49:57
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 返回订单确认页需要的数据
     * @return
     */
    OrderConfirmVO confirmOrder();

    /**
     * 创建订单
     * @param submitVO
     * @return
     */
    OrderCreateVO submit(OrderSubmitVO submitVO);

    /**
     * 获取订单及订单项
     * @param orderSn
     * @return
     */
    OrderCreateVO getOrderDetail(String orderSn);

    OrderTO getOrderTOByOrderSn(String orderSn);

    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 订单超时未支付，取消订单
     * @param orderEntity
     */
    void closeOrder(OrderEntity orderEntity);


    /**
     * 支付订单
     * @param orderSn
     * @return
     */
    String payOrder(String orderSn);


    /**
     * 分页查询当前登录用户的订单列表
     * @param params
     * @return
     */
    PageUtils getCurrentUserOrderList(Map<String, Object> params);

    /**
     * 处理阿里支付异步通知消息
     * @param notifyVO
     */
    String handleAlipayNotify(AlipayNotifyVO notifyVO, HttpServletRequest request);
}

