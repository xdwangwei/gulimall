package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.QueryResult;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.ProductAttrValueDao;
import com.vivi.gulimall.product.entity.ProductAttrValueEntity;
import com.vivi.gulimall.product.service.ProductAttrValueService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> listForSpu(Long spuId) {
        List<ProductAttrValueEntity> list = this.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        return list;
    }

    @Override
    public boolean updateForSpu(Long spuId, List<ProductAttrValueEntity> list) {
        // 删除掉之前的相关属性
        this.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        // 把新的属性插入进去
        List<ProductAttrValueEntity> collect = list.stream().map(item -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        return this.saveBatch(collect);
    }

}