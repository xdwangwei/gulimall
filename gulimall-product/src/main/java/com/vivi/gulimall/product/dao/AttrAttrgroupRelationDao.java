package com.vivi.gulimall.product.dao;

import com.vivi.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:46
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void removeBatch(@Param("entityList") List<AttrAttrgroupRelationEntity> entityList);
}
