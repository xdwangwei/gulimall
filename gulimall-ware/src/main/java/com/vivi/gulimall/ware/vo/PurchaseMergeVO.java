package com.vivi.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/19 22:25
 *
 * 将多个采购需求合并到一个采购单中
 */
@Data
public class PurchaseMergeVO {

    /**
     *   purchaseId: 1, //整单id
     *   items:[1,2,3,4] //合并项id集合
     */
    private Long purchaseId;
    private List<Long> items;
}
