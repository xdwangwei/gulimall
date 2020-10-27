package com.vivi.gulimall.product.dao;

import com.vivi.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * spu信息
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    @Update("update pms_spu_info set publish_status = #{publishStatus} where id = #{spuId}")
    boolean updateStatus(@Param("spuId") Long spuId, @Param("publishStatus") Integer publishStatus);
}
