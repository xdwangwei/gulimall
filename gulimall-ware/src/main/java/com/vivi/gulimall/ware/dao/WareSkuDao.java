package com.vivi.gulimall.ware.dao;

import com.vivi.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 商品库存
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:47:27
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    @Select("SELECT SUM(stock - stock_locked) FROM wms_ware_sku WHERE sku_id = #{skuId}")
    Long getSkuStock(Long skuId);
}
