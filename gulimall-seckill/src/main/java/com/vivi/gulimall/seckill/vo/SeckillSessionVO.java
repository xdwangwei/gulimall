package com.vivi.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author wangwei
 * 2021/1/30 14:39
 *
 * 属性和 com.vivi.gulimall.coupon.entity.SeckillSessionEntity 对应
 */
@Data
public class SeckillSessionVO {

    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    // 相关联的商品
    List<SeckillSkuRelationVO> relationSkus;
}
