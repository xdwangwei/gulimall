package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:46
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 多个参数
     * @param params
     * @return
     */
    PageUtils queryPageCondition(Map<String, Object> params);
}

