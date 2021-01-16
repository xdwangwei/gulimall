package com.vivi.gulimall.product.dao;

import com.vivi.gulimall.product.entity.ProductAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vivi.gulimall.product.vo.ItemAttrGroupWithAttrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * spu属性值
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
@Mapper
public interface ProductAttrValueDao extends BaseMapper<ProductAttrValueEntity> {

    List<ItemAttrGroupWithAttrVO> getAttrsWithAttrGroup(@Param("spuId") Long spuId, @Param("catelogId") Long catelogId);
}
