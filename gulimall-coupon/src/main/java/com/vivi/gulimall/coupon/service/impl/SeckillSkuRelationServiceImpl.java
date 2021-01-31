package com.vivi.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.vivi.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.vivi.gulimall.coupon.service.SeckillSkuRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> queryWrapper = new QueryWrapper<>();
        String sessionId = (String) params.get("promotionSessionId");
        // 判断请求参数
        if (!StringUtils.isEmpty(sessionId)) {
            queryWrapper.eq("promotion_session_id", sessionId);
        }
        String key = (String) params.get("key");
        // 判断请求关键字
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(query -> {
                query.eq("id", key).or().eq("sku_id", key);
            });
        }
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSkuRelationEntity> getRelationSkusBySessionId(Long sessionId) {

        return this.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", sessionId));
    }

}