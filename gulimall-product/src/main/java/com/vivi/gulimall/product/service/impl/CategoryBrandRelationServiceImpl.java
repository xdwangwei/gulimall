package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.BrandDao;
import com.vivi.gulimall.product.dao.CategoryBrandRelationDao;
import com.vivi.gulimall.product.dao.CategoryDao;
import com.vivi.gulimall.product.entity.BrandEntity;
import com.vivi.gulimall.product.entity.CategoryBrandRelationEntity;
import com.vivi.gulimall.product.entity.CategoryEntity;
import com.vivi.gulimall.product.service.CategoryBrandRelationService;
import com.vivi.gulimall.product.vo.BrandVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    protected BrandDao brandDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> getByBrandId(Long brandId) {
        return this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    /**
     * 前端传来的有brandId,catelogId
     * 数据库中需要 brand_id, catelog_id, brand_name, catelog_name
     * 剩下两个字段需要我们单独查出来
     * 默认生成的save方法只会保存前端传来的部分
     * @param categoryBrandRelationEntity
     * @return
     */
    @Override
    public boolean saveDetail(CategoryBrandRelationEntity categoryBrandRelationEntity) {
        // 根据brandId查出brandName
        BrandEntity brandEntity = brandDao.selectById(categoryBrandRelationEntity.getBrandId());
        // 根据catId查出catName
        CategoryEntity categoryEntity = categoryDao.selectById(categoryBrandRelationEntity.getCatelogId());
        // 填充到categoryBrandRelationEntity，执行保存
        categoryBrandRelationEntity.setBrandName(brandEntity.getName());
        categoryBrandRelationEntity.setCatelogName(categoryEntity.getName());

        return this.save(categoryBrandRelationEntity);
    }

    @Override
    public List<BrandVO> getBrandByCatalogId(String catalogId) {
        List<CategoryBrandRelationEntity> relationEntities = this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catalogId));
        if (relationEntities != null && relationEntities.size() > 0) {
            List<BrandVO> voList = relationEntities.stream().map(relationEntity -> {
                BrandVO brandVO = new BrandVO();
                BeanUtils.copyProperties(relationEntity, brandVO);
                return brandVO;
            }).collect(Collectors.toList());
            return voList;
        }
        return new ArrayList<>();
    }

}