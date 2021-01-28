package com.vivi.gulimall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vivi.gulimall.ware.entity.WareSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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

    // 查询此商品在哪些仓库有库存
    @Select("SELECT ware_id FROM wms_ware_sku WHERE sku_id = #{skuId} AND stock - stock_locked > 0")
    List<Long> listWaresBySkuId(@Param("skuId") Long skuId);

    @Update("UPDATE wms_ware_sku SET stock_locked = stock_locked + #{count} WHERE sku_id = #{skuId} AND ware_id = #{wareId} AND stock - stock_locked >= #{count}")
    int lockSkuStock(@Param("wareId") Long wareId, @Param("skuId") Long skuId, @Param("count") Integer count);

    @Update("UPDATE wms_ware_sku SET stock_locked = stock_locked - #{count} WHERE sku_id = #{skuId} AND ware_id = #{wareId}")
    boolean unlockStock(@Param("wareId") Long wareId, @Param("skuId") Long skuId, @Param("count") Integer count);
}
