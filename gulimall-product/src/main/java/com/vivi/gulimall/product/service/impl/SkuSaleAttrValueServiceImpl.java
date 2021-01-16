package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.SkuSaleAttrValueDao;
import com.vivi.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.vivi.gulimall.product.service.SkuSaleAttrValueService;
import com.vivi.gulimall.product.vo.ItemSaleAttrVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuSaleAttrValueEntity> listBySkuId(Long skuId) {

        return this.list(new QueryWrapper<SkuSaleAttrValueEntity>().eq("sku_id", skuId));
    }

    @Override
    public List<ItemSaleAttrVO> allAttrValueWithSkuBySpuId(Long spuId) {

        return this.baseMapper.allAttrValueWithSkuBySpuId(spuId);
    }

}