package com.vivi.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/20 12:49
 */
@Data
public class PurchaseDoneVO {

    /**
     * {
     *    id: 123,//采购单id
     *    items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     * }
     */
    Long id;

    List<PurchaseDetailDoneVO> items;
}
