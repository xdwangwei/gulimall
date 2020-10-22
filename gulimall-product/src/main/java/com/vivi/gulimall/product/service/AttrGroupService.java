package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.AttrEntity;
import com.vivi.gulimall.product.entity.AttrGroupEntity;
import com.vivi.gulimall.product.vo.AttrGroupWithAttrVO;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:47
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    /**
     * 分页查询所有属性组
     * @param params
     * @return
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 分页查询指定分类(catgoryId)下的属性组
     * @param params
     * @param catelogId
     * @return
     */
    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    /**
     * 查询指定属性分组下的所有属性
     * @param attrGroupId
     * @return
     */
    List<AttrEntity> queryRelationAttr(Long attrGroupId);

    /**
     * 删除attrGroup的同时，删除掉关联表中信息
     * @param list
     */
    boolean removeCascadeByIds(List<Long> list);

    /**
     * 查询当前分组可关联的属性(未被关联过的)
     * 而且是个分页查询
     * @param params
     * @param attrGroupId
     * @return
     */
    PageUtils queryNoRelationAttr(Map<String, Object> params, Long attrGroupId);


    /**
     * 查询指定分类下的所有属性分组以及属性
     * @param catelogId
     * @return
     */
    List<AttrGroupWithAttrVO> getAttrGroupWithAttrByCatelogId(Long catelogId);
}

