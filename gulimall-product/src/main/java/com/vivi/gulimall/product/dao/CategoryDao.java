package com.vivi.gulimall.product.dao;

import com.vivi.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
