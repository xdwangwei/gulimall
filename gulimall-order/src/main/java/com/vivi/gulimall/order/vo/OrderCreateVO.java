package com.vivi.gulimall.order.vo;

import com.vivi.gulimall.order.entity.OrderEntity;
import com.vivi.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/25 22:45
 *
 * 订单创建完的数据模型
 */
@Data
public class OrderCreateVO {

    private OrderEntity order;

    private List<OrderItemEntity> items;
}
