package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.constant.ProductConstant;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.vivi.gulimall.product.dao.AttrDao;
import com.vivi.gulimall.product.dao.AttrGroupDao;
import com.vivi.gulimall.product.dao.CategoryDao;
import com.vivi.gulimall.product.entity.*;
import com.vivi.gulimall.product.service.AttrAttrgroupRelationService;
import com.vivi.gulimall.product.service.AttrService;
import com.vivi.gulimall.product.service.CategoryService;
import com.vivi.gulimall.product.vo.AttrRespVO;
import com.vivi.gulimall.product.vo.AttrVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    // save()只能保存attr信息
    // attrService.save(attr);
    // 前端传来的attrVO中不仅包含attr信息，还包含所属分组等信息
    @Override
    public boolean saveDetail(AttrVO attrVO) {
        // 保存attr信息
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVO, attrEntity);
        this.save(attrEntity);

        // 销售属性没有属性分组，只有基本属性才会存在属性分组
        if (attrVO.getAttrType() == ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getValue()) {
            // 如果前端填表没有选择分组，就不用插入
            if (attrVO.getAttrGroupId() != null) {// 保存attr和attrGroup的关联信息
                AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                // attrId是主键，数据库插入新数据生成主键后我们可以获取到
                relationEntity.setAttrId(attrEntity.getAttrId());
                relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
        return true;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, String type, Long catelogId) {
        // 判断是查询基本属性还是销售属性
        int attrType =
                type.equals(ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getName())
                ? ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getValue()
                : ProductConstant.AttrTypeEnum.ATTR_TYPE_SALE.getValue();
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();

        // 拼装查询条件
        queryWrapper.eq("attr_type", attrType);

        // 取出查询关键字
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        // 判断是否是指定分类下的查询，否则查询全部
        if (catelogId > 0) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("catelog_id", catelogId);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper);
        // 从数据库中查出的结果
        List<AttrEntity> records = page.getRecords();
        // attrEntity转成attrRespVO
        List<AttrRespVO> attrRespVOList = records.stream().map((record) -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtils.copyProperties(record, attrRespVO);
            // 填充catelogName和groupName字段

            // 首先根据catelogId查出catelogEntity。再取出name设置给attrRespVO
            if (record.getCatelogId() != null) {
                attrRespVO.setCatelogName(categoryDao.selectById(record.getCatelogId()).getName());
            }

            // 填充group信息
            // 销售属性没有属性分组，只有基本属性才会存在属性分组
            if (record.getAttrType() == ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getValue()) {
                // 首先根据attrId查出它所属attrGroup
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", record.getAttrId()));
                // 存在
                if (attrAttrgroupRelationEntity != null) {
                    // 则根据attrGroupId查出它的name
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    if (attrGroupEntity != null) {
                        // 设置到attrRespVO
                        attrRespVO.setAttrGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }
            return attrRespVO;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        // 设置新的结果集
        pageUtils.setList(attrRespVOList);

        return pageUtils;

    }

    /**
     * getById()只能查询attr的信息，此方法查询attr本身及其所属分组和分类的详细信息
     * @param attrId
     * @return
     */
    @Override
    public AttrRespVO getDetailById(Long attrId) {

        AttrRespVO attrRespVO = new AttrRespVO();
        // 先查询基本信息
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVO);

        // 查询并设置catelogPath，attr中包含catelogId
        attrRespVO.setCatelogPath(categoryService.findCategoryPath(attrEntity.getCatelogId()));

        // 查询并设置attrGroup
        // 销售属性没有属性分组，只有基本属性才会存在属性分组
        if (attrEntity.getAttrType() == ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getValue()) {
            // 首先根据attrId查出它所属attrGroup
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            // 存在
            if (attrAttrgroupRelationEntity != null) {
                attrRespVO.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                // 则根据attrGroupId查出它的name
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    // 设置到attrRespVO
                    attrRespVO.setAttrGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        return attrRespVO;
    }

    @Transactional
    @Override
    public boolean updateCascadeById(AttrVO attrVO) {
        // 先更新attr本身的信息
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVO, attrEntity);
        this.updateById(attrEntity);

        // 销售属性没有属性分组，只有基本属性才会存在属性分组
        if (attrVO.getAttrType() == ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getValue()) {
            // 更新所属分组信息,把relation表中attr_id=#{id}的记录的groupId改成新的id
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrVO.getAttrId());
            relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
            // 但是如果这个attr刚开始没有关联的组，修改时传来了组，那应该是一个添加操作
            Integer res = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVO.getAttrId()));
            // 存在关联关系就是更新
            if (res > 0) {
                attrAttrgroupRelationDao.update(relationEntity, new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVO.getAttrId()));
            } else {
                // 否则就是新增
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }

        return true;
    }

    @Transactional
    @Override
    public void removeCascadeByIds(List<Long> list) {
        // 删除所有与之关联的属性分组记录
        attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_id", list));
        // 删除所有指定的属性记录
        this.removeByIds(list);
    }

}