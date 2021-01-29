package com.vivi.gulimall.order.dao;

import com.vivi.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:49:57
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	OrderEntity getOrderById(@Param("id") Long id);

	@Update("update oms_order set status = #{status} where order_sn=#{orderSn}")
    boolean updateOrderStatusByOrderSn(@Param("orderSn") String orderSn, @Param("status") Integer status);
}
