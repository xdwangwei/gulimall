package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.AttrEntity;
import com.vivi.gulimall.product.entity.ProductAttrValueEntity;
import com.vivi.gulimall.product.vo.AttrRespVO;
import com.vivi.gulimall.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:46
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    // save()只能保存attr信息
    // attrService.save(attr);
    // 前端传来的attrVO中不仅包含attr信息，还包含所属分组等信息
    boolean saveDetail(AttrVO attrVO);


    /**
     * 根据关键字和分类id查询指定三级分类(菜单)下的全部属性
     * type指的是查询基本属性还是销售属性
     * @param params
     * @return
     */
    PageUtils queryPage(Map<String, Object> params, String type, Long catelogId);

    /**
     * getById()只能查询attr的信息，此方法查询attr本身及其所属分组和分类的详细信息
     * @param attrId
     * @return
     */
    AttrRespVO getDetailById(Long attrId);

    // update()只能更新attr信息
    // 前端传来的attrVO中不仅包含attr信息，还包含所属分组等信息
    boolean updateCascadeById(AttrVO attrVO);

    /**
     * 删除attr的同时，删除掉关联的分组
     * @param list
     */
    void removeCascadeByIds(List<Long> list);

    /**
     * 找出指定的属性集中可以用于搜索的属性
     * @param ids
     * @return
     */
    List<AttrEntity> listSearchAttrByIds(List<Long> ids);
}

