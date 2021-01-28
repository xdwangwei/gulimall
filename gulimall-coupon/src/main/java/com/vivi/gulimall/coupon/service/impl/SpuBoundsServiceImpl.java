package com.vivi.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.to.SpuBoundsTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.coupon.dao.SpuBoundsDao;
import com.vivi.gulimall.coupon.entity.SpuBoundsEntity;
import com.vivi.gulimall.coupon.service.SpuBoundsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service("spuBoundsService")
public class SpuBoundsServiceImpl extends ServiceImpl<SpuBoundsDao, SpuBoundsEntity> implements SpuBoundsService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuBoundsEntity> page = this.page(
                new Query<SpuBoundsEntity>().getPage(params),
                new QueryWrapper<SpuBoundsEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public SpuBoundsTO getBySpuId(Long spuId) {

        SpuBoundsEntity entity = this.getOne(new QueryWrapper<SpuBoundsEntity>().eq("spu_id", spuId));
        SpuBoundsTO spuBoundsTO = new SpuBoundsTO();
        BeanUtils.copyProperties(entity, spuBoundsTO);
        return spuBoundsTO;
    }

    /**
     * 远程调用，seata分布式事务
     * @param spuBounds
     * @return
     */
    @Transactional
    @Override
    public boolean saveSpuBoundS(SpuBoundsEntity spuBounds) {

        return this.save(spuBounds);
    }

}