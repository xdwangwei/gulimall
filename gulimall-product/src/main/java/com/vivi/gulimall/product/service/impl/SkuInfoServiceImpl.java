package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.SkuInfoDao;
import com.vivi.gulimall.product.entity.SkuInfoEntity;
import com.vivi.gulimall.product.entity.SpuInfoEntity;
import com.vivi.gulimall.product.service.SkuInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        /**
         * key: '华为',//检索关键字
         * catelogId: 0,
         * brandId: 0,
         * min: 0,
         * max: 0
         */
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        // 查询关键字
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        // 指定分类
        String catelogId = (String) params.get("catelogId");
        if (isValidId(catelogId)) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        // 指定品牌
        String brandId = (String) params.get("brandId");
        if (isValidId(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        try {
            // 最低价格
            BigDecimal min = new BigDecimal((String) params.get("min"));
            if (min.compareTo(BigDecimal.ZERO) >= 0) {
                queryWrapper.ge("price", min);
            }
            // 最高价格
            BigDecimal max = new BigDecimal((String) params.get("max"));
            if (max.compareTo(BigDecimal.ZERO) > 0) {
                queryWrapper.le("price", max);
            }
        } catch (Exception e) {

        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> listBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    private boolean isValidId(String key) {
        return !StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(key);
    }

}