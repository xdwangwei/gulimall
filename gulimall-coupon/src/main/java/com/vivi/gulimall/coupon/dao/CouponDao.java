package com.vivi.gulimall.coupon.dao;

import com.vivi.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:52:22
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
