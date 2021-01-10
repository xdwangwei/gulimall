package com.vivi.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.constant.WareConstant;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.ware.dao.PurchaseDao;
import com.vivi.gulimall.ware.entity.PurchaseDetailEntity;
import com.vivi.gulimall.ware.entity.PurchaseEntity;
import com.vivi.gulimall.ware.service.PurchaseDetailService;
import com.vivi.gulimall.ware.service.PurchaseService;
import com.vivi.gulimall.ware.service.WareSkuService;
import com.vivi.gulimall.ware.vo.PurchaseDetailDoneVO;
import com.vivi.gulimall.ware.vo.PurchaseDoneVO;
import com.vivi.gulimall.ware.vo.PurchaseMergeVO;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.print.attribute.standard.PrinterURI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceived(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", WareConstant.PurchaseStatus.CREATED.getValue())
                .or().eq("status", WareConstant.PurchaseStatus.ASSIGNED.getValue())
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean mergePurchase(PurchaseMergeVO purchaseMergeVO) {

        List<Long> purchaseDetailIds = purchaseMergeVO.getItems();
        if (CollectionUtils.isEmpty(purchaseDetailIds)) {
            return true;
        }
        boolean isNewPurchase = false;
        // 判断是需要先新建采购单再合并采购项还是直接合并到已有采购单
        Long purchaseId = purchaseMergeVO.getPurchaseId();
        if (purchaseId == null) {
            // 创建一个新的采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setPriority(1);
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatus.CREATED.getValue());
            // 保存采购单
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
            isNewPurchase = true;
        } else {
            // 已有的采购单
            // 这个采购单状态必须是创建或者分配
            PurchaseEntity purchaseEntity = this.getById(purchaseId);
            if (purchaseEntity.getStatus() != WareConstant.PurchaseStatus.CREATED.getValue()
                && purchaseEntity.getStatus() != WareConstant.PurchaseStatus.ASSIGNED.getValue()){
                // 不能合并到这个采购单
                return false;
            }
        }

        // 合并采购需求
        Long finalPurchaseId = purchaseId;
        boolean finalIsNewPurchase = isNewPurchase;
        List<PurchaseDetailEntity> entities = purchaseDetailIds.stream().map(id -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            // 指定id
            purchaseDetailEntity.setId(id);
            // 设置分配的采购单id
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            // 设置状态
            purchaseDetailEntity.setStatus(finalIsNewPurchase ? WareConstant.PurchaseDetailStatus.WAITING.getValue() : WareConstant.PurchaseDetailStatus.ASSIGNED.getValue());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        // 批量更新采购需求条目
        purchaseDetailService.updateBatchById(entities);

        // 更新一下这个采购单的更新时间
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(finalPurchaseId);
        purchaseEntity.setUpdateTime(new Date());
        return true;
    }

    @Override
    public boolean removeCascadeByIds(List<Long> list) {
        if (!CollectionUtils.isEmpty(list)) {
            // 删除采购单
            this.removeByIds(list);
            // 找到这个采购单关联的所有采购项
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.list(new QueryWrapper<PurchaseDetailEntity>().in("purchase_id", list));
            // 删除这条采购单上的采购需求
            List<Long> ids = detailEntities.stream().map(PurchaseDetailEntity::getId).collect(Collectors.toList());
            purchaseDetailService.removeByIds(ids);
        }
        return true;
    }

    /**
     * 领取采购任务：将这些采购项状态改为已领取，将其相关的采购条目状态改为正在进行
     * @param purchaseIds
     * @return
     */
    @Transactional
    @Override
    public boolean receivePurchase(List<Long> purchaseIds) {
        List<PurchaseEntity> purchaseEntities = this.list(new QueryWrapper<PurchaseEntity>().in("id", purchaseIds));
        // 过滤出其中状态为创建或者分配的
        List<PurchaseEntity> collect = purchaseEntities.stream()
                .filter(purchaseEntity ->
                        purchaseEntity.getStatus() == WareConstant.PurchaseStatus.CREATED.getValue()
                                || purchaseEntity.getStatus() == WareConstant.PurchaseStatus.ASSIGNED.getValue())
                .map(purchase -> {
                    PurchaseEntity entity = new PurchaseEntity();
                    entity.setId(purchase.getId());
                    // 修改其状态为已领取
                    entity.setStatus(WareConstant.PurchaseStatus.RECEIVED.getValue());
                    // 修改更新时间
                    entity.setUpdateTime(new Date());
                    return entity;
                }).collect(Collectors.toList());
        // 批量更新
        this.updateBatchById(collect);

        // 再把这些采购单上的采购条目状态改为正在进行
        List<Long> purchaseIdList = collect.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.list(new QueryWrapper<PurchaseDetailEntity>().in("purchase_id", purchaseIdList));
        List<PurchaseDetailEntity> detailEntityList = purchaseDetailEntities.stream().map(detail -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(detail.getId());
            detailEntity.setStatus(WareConstant.PurchaseDetailStatus.DOING.getValue());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(detailEntityList);
        return true;
    }


    @Transactional
    @Override
    public boolean purchaseDone(PurchaseDoneVO purchaseDoneVO) {
        Long purchaseId = purchaseDoneVO.getId();
        List<PurchaseDetailDoneVO> purchaseDetailDoneVOS = purchaseDoneVO.getItems();
        // 准备要修改的采购单对象
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        // 设置转态为已完成
        purchaseEntity.setStatus(WareConstant.PurchaseStatus.FINISHED.getValue());
        if (CollectionUtils.isEmpty(purchaseDetailDoneVOS)) {
            // 如果只有一个采购单id，没有采购项，则只修改采购单
            return this.updateById(purchaseEntity);
        }
        // 否则遍历采购项，得到要修改的采购单对象集合
        List<PurchaseDetailEntity> detailEntities = purchaseDetailDoneVOS.stream().map(purchaseDetailDoneVO -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(purchaseDetailDoneVO.getItemId());
            if (!StringUtils.isEmpty(purchaseDetailDoneVO.getReason())) {
                detailEntity.setFailReason(purchaseDetailDoneVO.getReason());
            }
            // 如果这个采购单上的某个采购项的状态是失败，那么将这个采购单的转态置为有异常，其他采购项的状态置为已完成
            if (purchaseDetailDoneVO.getStatus() == WareConstant.PurchaseDetailStatus.FAILED.getValue()) {
                purchaseEntity.setStatus(WareConstant.PurchaseStatus.WRONG.getValue());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatus.FAILED.getValue());
            } else {
                // 否则，所有采购项状态都置为已完成，采购单状态置为已完成
                detailEntity.setStatus(WareConstant.PurchaseDetailStatus.FINISHED.getValue());
                // 此项采购完成，则要修改此采购物品的库存
                PurchaseDetailEntity entity = purchaseDetailService.getById(detailEntity.getId());
                wareSkuService.addStock(entity.getWareId(), entity.getSkuId(), entity.getSkuNum());
            }
            return detailEntity;
        }).collect(Collectors.toList());
        // 修改采购单
        this.updateById(purchaseEntity);
        // 批量修改采购项
        return purchaseDetailService.updateBatchById(detailEntities);
    }

    @Override
    public boolean purchaseAssign(PurchaseEntity purchase) {
        // 先更新采购单，这个采购单单位状态必须是新建，才能分配
        if (purchase.getStatus() != null && (purchase.getStatus() == WareConstant.PurchaseStatus.CREATED.getValue())) {
            purchase.setStatus(WareConstant.PurchaseStatus.ASSIGNED.getValue());
            this.updateById(purchase);
            // 再更新此采购单上的采购项状态为已分配
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.list(new QueryWrapper<PurchaseDetailEntity>().in("purchase_id", purchase.getId()));
            if (!CollectionUtils.isEmpty(purchaseDetailEntities)) {
                List<PurchaseDetailEntity> detailEntityList = purchaseDetailEntities.stream().map(detail -> {
                    PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                    detailEntity.setId(detail.getId());
                    detailEntity.setStatus(WareConstant.PurchaseDetailStatus.ASSIGNED.getValue());
                    return detailEntity;
                }).collect(Collectors.toList());
                purchaseDetailService.updateBatchById(detailEntityList);
            }
            return true;
        }
        return false;
    }

}