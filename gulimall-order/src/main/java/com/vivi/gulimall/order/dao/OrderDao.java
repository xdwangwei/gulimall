package com.vivi.gulimall.order.dao;

import com.vivi.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:49:57
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
