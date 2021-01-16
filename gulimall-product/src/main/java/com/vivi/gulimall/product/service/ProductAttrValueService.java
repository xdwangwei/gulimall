package com.vivi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.product.entity.ProductAttrValueEntity;
import com.vivi.gulimall.product.vo.ItemAttrGroupWithAttrVO;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取指定spu的规格属性
     * @param spuId
     * @return
     */
    List<ProductAttrValueEntity> listForSpu(Long spuId);

    /**
     * 批量更新指定spu的规格属性
     * @param spuId
     * @return
     */
    boolean updateForSpu(Long spuId, List<ProductAttrValueEntity> list);

    /**
     * 查出此商品所有的规格参数信息(属性分组，组内属性)
     * @param spuId
     * @return
     */
    List<ItemAttrGroupWithAttrVO> getAttrsWithAttrGroupBySpuId(Long spuId);
}

