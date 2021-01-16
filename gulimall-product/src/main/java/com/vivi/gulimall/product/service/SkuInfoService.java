package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.SkuInfoEntity;
import com.vivi.gulimall.product.vo.ItemDetailVO;

import java.util.List;
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

    /**
     * 根据spuId查询sku
     * @param spuId
     * @return
     */
    List<SkuInfoEntity> listBySpuId(Long spuId);

    /**
     * 查询此sku的详情页面数据
     * @param skuId
     * @return
     */
    ItemDetailVO detail(Long skuId);
}

