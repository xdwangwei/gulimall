package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.BrandEntity;
import com.vivi.gulimall.product.entity.CategoryEntity;
import com.vivi.gulimall.product.vo.Catelog2VO;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查出全部分类，并按照层级关系组装成树形结构
     * @return
     */
    List<CategoryEntity> listWithTree();

    /**
     * 批量删除菜单项
     * @param list
     * @return
     */
    boolean removeBatchByIds(List<Long> list);

    /**
     * 查询出此分类的完整层级关系
     * @param categoryId
     * @return
     */
    List<Long> findCategoryPath(Long categoryId);

    /**
     * 默认生成的updateById()只会更新存储category信息的表
     * 实际上可能在其他表中和此表有关联，通常会有一个category_id字段，关联真正的category表
     * 但是如果只存category_id，需要其他信息再去查，就会总成数据库压力大，所以通常会伴随有几个冗余字段，比如category_name
     * 所以在更新brand表的时候，如果更新字段部分包括了出现在其他表中的冗余字段，则需要将这些关联的表的这些部分也更新了
     * 这样才能保证数据一致性
     * @param categoryEntity
     */
    boolean updateCascadeById(CategoryEntity categoryEntity);

    /**
     * 得到所有的一级分类
     * @return
     */
    List<CategoryEntity> getLevel1Categories();


    /**
     * 首页三级分类渲染所需要的的数据模型
     * @return
     */
    Map<String, List<Catelog2VO>> getCatelogJson();
}

