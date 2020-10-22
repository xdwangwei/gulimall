package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.CategoryBrandRelationDao;
import com.vivi.gulimall.product.dao.CategoryDao;
import com.vivi.gulimall.product.entity.CategoryEntity;
import com.vivi.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有的菜单
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        return categoryEntities.stream()
                // 过滤出一级菜单
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                // 为一级菜单找到其所有的二级菜单
                .map(entity -> getChildren(entity, categoryEntities))
                // 按照菜单优先级排序
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                // 返回最终的一级菜单
                .collect(Collectors.toList());
    }

    @Override
    public boolean removeBatchByIds(List<Long> list) {
        // TODO 删除菜单前需要检查当前菜单是否在别处被引入，不能轻易删除，即便是逻辑删除也不行
        baseMapper.deleteBatchIds(list);
        return true;
    }

    @Override
    public List<Long> findCategoryPath(Long catId) {
        LinkedList<Long> path = new LinkedList<>();
        LinkedList<Long> fullPath = getFullPath(catId, path);
        // 逆序
        Collections.reverse(fullPath);
        return fullPath;
    }

    /**
     * 默认生成的updateById()只会更新存储category信息的表
     * 实际上可能在其他表中和此表有关联，通常会有一个category_id字段，关联真正的category表
     * 但是如果只存category_id，需要其他信息再去查，就会总成数据库压力大，所以通常会伴随有几个冗余字段，比如category_name
     * 所以在更新brand表的时候，如果更新字段部分包括了出现在其他表中的冗余字段，则需要将这些关联的表的这些部分也更新了
     * 这样才能保证数据一致性
     * @param categoryEntity
     */
    @Override
    public boolean updateCascadeById(CategoryEntity categoryEntity) {
        // 先更新category表本身
        this.updateById(categoryEntity);

        // 判断更新字段部分是否包括了出现在其他表中的冗余字段，

        // brand_category_relation表中存在category_id关联了category表，并有冗余字段category_name
        if (!StringUtils.isEmpty(categoryEntity.getName())) {
            // 更新brand_cagegory_relation表中的brand_name冗余字段
            categoryBrandRelationDao.updateCategoryName(categoryEntity.getCatId(), categoryEntity.getName());
        }

        // TODO 其他有关表中相关冗余字段的判断以及更新
        return true;
    }

    // 得到 [225, 25, 2]
    private LinkedList<Long> getFullPath(Long catId, LinkedList<Long> path) {
        // 把自己加入路径
        path.add(catId);
        // 如果它不是第一层菜单
        CategoryEntity categoryEntity = this.getById(catId);
        if (categoryEntity.getParentCid() != 0) {
            // 递归查找它的父菜单
            getFullPath(categoryEntity.getParentCid(), path);
        }
        // 返回完整路径
        return path;
    }

    /**
     * 传入一个一级菜单和全部菜单，找到这个菜单的全部孩子，设置到他相应的属性，再把它返回
     * @param entity
     * @param entityList
     * @return
     */
    private CategoryEntity getChildren(CategoryEntity entity, List<CategoryEntity> entityList) {
        // 设置属性children
        entity.setChildren(entityList.stream()
                // 找到他的所有孩子(二级)
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(entity.getCatId()))
                // 为他的孩子找到他们的下一级菜单(第三级)，再返回他的孩子
                .map(item -> getChildren(item, entityList))
                // 按照菜单优先级排序
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                // 最终得到找到了第三级菜单的二级菜单
                .collect(Collectors.toList()));
        // 返回找到了二级菜单的一级菜单
        return entity;
    }

}