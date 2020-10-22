package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.constant.ProductConstant;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.vivi.gulimall.product.dao.AttrDao;
import com.vivi.gulimall.product.dao.AttrGroupDao;
import com.vivi.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.vivi.gulimall.product.entity.AttrEntity;
import com.vivi.gulimall.product.entity.AttrGroupEntity;
import com.vivi.gulimall.product.service.AttrGroupService;
import com.vivi.gulimall.product.vo.AttrGroupWithAttrVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.management.relation.Relation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        // 查询附带的关键字
        String key = (String) params.get("key");

        // 构造查询条件
        // (attr_group_id=key or attr_group_name like %key%)是一个整体
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            // 需要用一个and连接一个整体
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        // 根据categoryId来判断是查询全部还是指定查询
        //select * from pms_attr_group where catelog_id=? and (attr_group_id=key or attr_group_name like %key%)
        //select * from pms_attr_group where  (attr_group_id=key or attr_group_name like %key%)

        // 查询指定分类(catgoryId)下的属性组
        if (catelogId > 0) {
            // 添加条件
            queryWrapper.eq("catelog_id", catelogId);
        }

        // this.page() 查询
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> queryRelationAttr(Long attrGroupId) {
        // 先查询关联表，得到这个分组关联的全部属性的id集合
        List<AttrAttrgroupRelationEntity> relationList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));

        // 不存在就直接返回空集
        if (relationList.size() <= 0) {
            return new ArrayList<AttrEntity>();
        }

        // 得到id集合
        List<Long> collect = relationList.stream().map((relationEntity) -> relationEntity.getAttrId()).collect(Collectors.toList());

        // 再根据id集合查询attr
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(collect);
        return attrEntities;
    }

    @Transactional
    @Override
    public boolean removeCascadeByIds(List<Long> list) {
        // 先删除关联表中的记录
        attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", list));
        // 删除group本身的记录
        this.removeByIds(list);
        return true;
    }

    @Override
    public PageUtils queryNoRelationAttr(Map<String, Object> params, Long attrGroupId) {
        // 每个分组都属于一个分类，能关联的属性也只能是这个分类下的，而且只能关联不属于任何分组的属性
        // 1. 先查询这个分组属于哪个分类
        AttrGroupEntity groupEntity = this.getById(attrGroupId);
        Long catelogId = groupEntity.getCatelogId();

        // 2. 这个分类下的所有分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 3. 这些分组已经关联了的属性
        // 3.1 这些分组的id集合
        List<Long> groupIds = groupEntities.stream().map(group -> group.getAttrGroupId()).collect(Collectors.toList());
        // 这里不用判空，因为至少存在参数传过来的这个分组
        // if (groupIds != null && groupIds.size() > 0) {
            // 3.2 根据分组id集合去关联表中查出属性集合
            List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
            // 3.3 得到这些属性的id集合
            List<Long> attrIds = relationEntities.stream().map(relationEntity -> relationEntity.getAttrId()).collect(Collectors.toList());
        // }
        // 4. 在这个分类下的所有属性里面，去掉这些已产生关联关系的这些属性，剩下的就是当前分组可以关联的
        // 而且一定要是基本属性，销售属性没有分组
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type", ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getValue());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }

        // 结合查询关键字
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        // page() 查询
        IPage<AttrEntity> page = attrDao.selectPage(new Query<AttrEntity>().getPage(params),
                wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrVO> getAttrGroupWithAttrByCatelogId(Long catelogId) {
        // 先查询这个分类下的所有属性分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 查询每个分组下的关联属性
        List<AttrGroupWithAttrVO> list = groupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrVO attrGroupWithAttrVO = new AttrGroupWithAttrVO();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrVO);
            List<AttrEntity> attrEntities = this.queryRelationAttr(attrGroupEntity.getAttrGroupId());
            attrGroupWithAttrVO.setAttrs(attrEntities);
            return attrGroupWithAttrVO;
        }).collect(Collectors.toList());
        return list;
    }

}