package com.vivi.gulimall.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.vivi.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.vivi.gulimall.product.service.AttrAttrgroupRelationService;
import com.vivi.gulimall.product.vo.AttrAttrGroupRelationVO;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public boolean removeBatch(List<AttrAttrGroupRelationVO> relationVOList) {
        List<AttrAttrgroupRelationEntity> relationEntities = relationVOList.stream().map((relationVO) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(relationVO.getAttrId());
            relationEntity.setAttrGroupId(relationVO.getAttrGroupId());
            return relationEntity;
        }).collect(Collectors.toList());
        baseMapper.removeBatch(relationEntities);
        return false;
    }

    @Override
    public boolean saveBatch(List<AttrAttrGroupRelationVO> relationVOList) {
        List<AttrAttrgroupRelationEntity> relationEntities = relationVOList.stream().map(relationVO -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(relationVO, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());
        return this.saveBatch(relationEntities);
    }

}
