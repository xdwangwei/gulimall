package com.vivi.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author wangwei
 * 2020/10/19 16:49
 */
@Data
public class SpuBoundsTO {
    /**
     * spuId
     */
    private Long spuId;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;

}
