package com.vivi.gulimall.ware.vo;

import lombok.Data;

/**
 * @author wangwei
 * 2020/10/20 12:49
 */
@Data
public class PurchaseDetailDoneVO {

    /**
     * items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     */
    Long itemId;

    Integer status;

    String reason;
}
