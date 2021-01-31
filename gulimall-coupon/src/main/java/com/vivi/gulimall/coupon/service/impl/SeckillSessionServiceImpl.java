package com.vivi.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.coupon.dao.SeckillSessionDao;
import com.vivi.gulimall.coupon.entity.SeckillSessionEntity;
import com.vivi.gulimall.coupon.service.SeckillSessionService;
import com.vivi.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> latest3DaysSessions() {
        List<SeckillSessionEntity> sessions = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        if (!CollectionUtils.isEmpty(sessions)) {
            // 为每个场次保存活动信息
            List<SeckillSessionEntity> collect = sessions.stream().map(session -> {
                session.setRelationSkus(seckillSkuRelationService.getRelationSkusBySessionId(session.getId()));
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 最近三天的开始时间
     * @return
     */
    private String startTime() {
        LocalDate now = LocalDate.now();
        String start = LocalDateTime.of(now, LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return start;
    }

    /**
     * 最近三天的结束时间
     * @return
     */
    private String endTime() {
        LocalDate after2Days = LocalDate.now().plusDays(2);
        String end = LocalDateTime.of(after2Days, LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return end;
    }

}