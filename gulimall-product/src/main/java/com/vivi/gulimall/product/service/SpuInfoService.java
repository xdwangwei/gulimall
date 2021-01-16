package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.SpuInfoEntity;
import com.vivi.gulimall.product.vo.SpuVO;

import java.util.Map;

/**
 * spu信息
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    /**
     * 简单的分页查询
     * @param params
     * @return
     */
    PageUtils queryPage(Map<String, Object> params);

    boolean save(SpuVO spuVO);

    /**
     * 加了许多判断条件的分页查询
     * @param params
     * @return
     */
    PageUtils queryPageCondition(Map<String, Object> params);

    /**
     * 上架商品
     * @param id
     * @return
     */
    boolean statusUp(Long id);

    /**
     * 修改spu的publish_status状态
     * @return
     */
    boolean updateStatus(Long spuId, Integer publishStatus);

}

