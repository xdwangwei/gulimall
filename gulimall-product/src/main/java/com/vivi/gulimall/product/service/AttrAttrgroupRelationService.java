package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.vivi.gulimall.product.vo.AttrAttrGroupRelationVO;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:46
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean removeBatch(List<AttrAttrGroupRelationVO> relationVOList);

    boolean saveBatch(List<AttrAttrGroupRelationVO> relationVOList);
}

