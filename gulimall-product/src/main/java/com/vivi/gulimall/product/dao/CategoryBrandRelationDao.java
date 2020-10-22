package com.vivi.gulimall.product.dao;

import com.vivi.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 品牌分类关联
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    @Update("update pms_category_brand_relation set brand_name = #{brandName} where brand_id = #{brandId}")
    boolean updateBrandName(@Param("brandId") Long brandId, @Param("brandName") String brandName);


    @Update("update pms_category_brand_relation set catelog_name = #{catName} where catelog_id = #{catId}")
    boolean updateCategoryName(@Param("catId") Long catId, @Param("catName") String catName);
}
