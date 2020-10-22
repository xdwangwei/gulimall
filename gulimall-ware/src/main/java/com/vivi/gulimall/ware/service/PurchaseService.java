package com.vivi.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.ware.entity.PurchaseEntity;
import com.vivi.gulimall.ware.vo.PurchaseDoneVO;
import com.vivi.gulimall.ware.vo.PurchaseMergeVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:47:27
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询未被接收的采购单，也就是新建或者被分配，但不是正在进行中的采购单
     * @param params
     * @return
     */
    PageUtils queryPageUnreceived(Map<String, Object> params);

    /**
     * 将多个采购需求合并到一个采购单中
     * @param purchaseMergeVO
     * @return
     */
    boolean mergePurchase(PurchaseMergeVO purchaseMergeVO);

    /**
     * 删除采购单的同时修改关联的采购需求
     * @param list
     * @return
     */
    boolean removeCascadeByIds(List<Long> list);

    /**
     * 领取采购任务：讲这些采购项状态改为已领取，将其相关的采购条目状态改为正在进行
     * @param purchaseIds
     * @return
     */
    boolean receivePurchase(List<Long> purchaseIds);

    /**
     * 某个采购单完成
     * @param purchaseDoneVO
     * @return
     */
    boolean purchaseDone(PurchaseDoneVO purchaseDoneVO);
}

