package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.CategoryBrandRelationEntity;
import com.vivi.gulimall.product.vo.BrandVO;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询指定品牌下的 关联的所有分类信息
     * @param brandId
     * @return
     */
    List<CategoryBrandRelationEntity> getByBrandId(Long brandId);

    /**
     * 前端传来的有brandId,catelogId
     * 数据库中需要 brand_id, catelog_id, brand_name, catelog_name
     * 剩下两个字段需要我们单独查出来
     * 默认生成的save方法只会保存前端传来的部分
     * @param categoryBrandRelationEntity
     * @return
     */
    boolean saveDetail(CategoryBrandRelationEntity categoryBrandRelationEntity);

    /**
     * 查询和指定分类关联的所有品牌
     * @param catalogId
     * @return
     */
    List<BrandVO> getBrandByCatalogId(String catalogId);
}

