package com.vivi.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.regexp.internal.RE;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.R;
import com.vivi.gulimall.ware.vo.PurchaseDoneVO;
import com.vivi.gulimall.ware.vo.PurchaseMergeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.vivi.gulimall.ware.entity.PurchaseEntity;
import com.vivi.gulimall.ware.service.PurchaseService;


/**
 * 采购信息
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:47:27
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/unreceived/list")
    // @RequiresPermissions("ware:purchase:list")
    public R unReceivedList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceived(params);

        return R.ok().put("page", page);
    }

    @PostMapping("/merge")
    public R mergePurchase(@RequestBody PurchaseMergeVO purchaseMergeVO) {
        if (purchaseService.mergePurchase(purchaseMergeVO)) {
            return R.ok();
        }
        return R.error(BizCodeEnum.WARE_PURCHASE_MERGE_EXCEPTION.getCode(),
                BizCodeEnum.WARE_PURCHASE_MERGE_EXCEPTION.getMsg());
    }

    // http://localhost:88/api/ware/purchase/receive
    @PostMapping("/receive")
    public R receivePurchase(@RequestBody List<Long> purchaseIds) {
        purchaseService.receivePurchase(purchaseIds);
        return R.ok();
    }

    // /ware/purchase/done
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVO purchaseDoneVO) {
        purchaseService.purchaseDone(purchaseDoneVO);
        return R.ok();

    }

    // /ware/purchase/assign
    @PostMapping("/assign")
    public R done(@RequestBody PurchaseEntity purchase) {
        boolean res = purchaseService.purchaseAssign(purchase);
        if (res) {
            return R.ok();
        }
        return R.error(BizCodeEnum.WARE_PURCHASE_ASSIGN_EXCEPTION.getCode(),
                BizCodeEnum.WARE_PURCHASE_ASSIGN_EXCEPTION.getMsg());
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		// purchaseService.removeByIds(Arrays.asList(ids));
        purchaseService.removeCascadeByIds(Arrays.asList(ids));
        return R.ok();
    }

}
