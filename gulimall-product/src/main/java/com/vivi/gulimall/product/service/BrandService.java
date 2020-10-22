package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 默认生成的updateById()只会更新存储brand信息的表
     * 实际上可能在其他表中和此表有关联，通常会有一个brand_id字段，关联真正的brand表
     * 但是如果只存brand_id，需要其他信息再去查，就会总成数据库压力大，所以通常会伴随有几个冗余字段，比如brand_name
     * 所以在更新brand表的时候，如果更新字段部分包括了出现在其他表中的冗余字段，则需要将这些关联的表的这些部分也更新了
     * 这样才能保证数据一致性
     * @param brand
     */
    boolean updateCascadeById(BrandEntity brand);
}

