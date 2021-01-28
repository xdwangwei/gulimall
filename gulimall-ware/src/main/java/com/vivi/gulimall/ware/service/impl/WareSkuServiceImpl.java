package com.vivi.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.constant.OrderConstant;
import com.vivi.common.constant.WareConstant;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.to.OrderLockStockTO;
import com.vivi.common.to.OrderTO;
import com.vivi.common.to.SkuInfoTO;
import com.vivi.common.to.SkuStockTO;
import com.vivi.common.to.mq.StockLockTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.common.utils.R;
import com.vivi.gulimall.ware.dao.WareSkuDao;
import com.vivi.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.vivi.gulimall.ware.entity.WareOrderTaskEntity;
import com.vivi.gulimall.ware.entity.WareSkuEntity;
import com.vivi.gulimall.ware.feign.OrderFeignService;
import com.vivi.gulimall.ware.feign.ProductFeignService;
import com.vivi.gulimall.ware.service.WareOrderTaskDetailService;
import com.vivi.gulimall.ware.service.WareOrderTaskService;
import com.vivi.gulimall.ware.service.WareSkuService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService taskService;

    @Autowired
    WareOrderTaskDetailService taskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        if (isValidId(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        String skuId = (String) params.get("skuId");
        if (isValidId(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 先判断，指定仓库指定商品，是否存在这个记录，存在则改库存，不存在则新增
     * @param wareId
     * @param skuId
     * @param num
     * @return
     */
    @Transactional
    @Override
    public boolean addStock(Long wareId, Long skuId, Integer num) {
        WareSkuEntity entity = this.getOne(new QueryWrapper<WareSkuEntity>().eq("ware_id", wareId).eq("sku_id", skuId));
        // 新增
        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        if (entity == null) {
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(num);
            wareSkuEntity.setStockLocked(0);
            try {
                R res = productFeignService.info(skuId);
                if (res.getCode() == 0) {
                    SkuInfoTO skuInfo = res.getData("skuInfo", SkuInfoTO.class);
                    wareSkuEntity.setSkuName(skuInfo.getSkuName());
                }
            } catch (Exception e) {
                log.error("调用远程服务gulimall-product查询skuinfo失败");
            }
            return this.save(wareSkuEntity);
        }
        // 只修改库存
        wareSkuEntity.setId(entity.getId());
        wareSkuEntity.setStock(entity.getStock() + num);
        return this.updateById(wareSkuEntity);
    }

    @Override
    public List<SkuStockTO> getSkusStock(List<Long> skuIds) {
        List<SkuStockTO> collect = skuIds.stream().map(skuId -> {
            SkuStockTO skuStockTO = new SkuStockTO();
            Long stock = baseMapper.getSkuStock(skuId);
            skuStockTO.setSkuId(skuId);
            skuStockTO.setStock(stock == null ? 0 : stock);
            return skuStockTO;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public Long getSkuStock(Long skuId) {
        return baseMapper.getSkuStock(skuId);
    }

    @Transactional
    @Override
    public boolean lockOrderStock(OrderLockStockTO lockStockTO) {
        // 通常情况下，根据订单收货地址选择最近仓库锁定库存
        // 这里简化为，遍历存有此商品的所有库存，挨个尝试锁定，有一个仓库锁定成功就算成功
        // 1. 创建库存工作单
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(lockStockTO.getOrderSn());
        taskService.save(taskEntity);
        // 2. 尝试锁定所有订单项库存
        List<OrderLockStockTO.SkuLockStock> locks = lockStockTO.getLocks();
        for (OrderLockStockTO.SkuLockStock skuLockStock : locks) {
            Long skuId = skuLockStock.getSkuId();
            // 先查询此商品在哪些仓库有货
            List<Long> wareIds = this.baseMapper.listWaresBySkuId(skuId);
            if (CollectionUtils.isEmpty(wareIds)) {
                // 当前商品库存不足，锁定失败
                throw new BizException(BizCodeEnum.WARE_SKU_STOCK_NOT_ENOUGH, "库存不足，商品：" + skuLockStock);
            }
            // 挨个仓库尝试锁定
            boolean currSkuLockRes = false;
            for (Long wareId : wareIds) {
                int rows = this.baseMapper.lockSkuStock(wareId, skuId, skuLockStock.getCount());
                if (rows == 1) {
                    // 当前商品库存锁定成功
                    // 创键任务详情
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, skuLockStock.getSkuName(), skuLockStock.getCount(), taskEntity.getId(), wareId, WareConstant.StockLockStatus.LOCKED.getValue());
                    taskDetailService.save(taskDetailEntity);
                    // 向mq发送当前锁定消息
                    StockLockTO stockLockTO = new StockLockTO(taskDetailEntity.getId(), lockStockTO.getOrderSn(), skuId, wareId, skuLockStock.getCount());
                    rabbitTemplate.convertAndSend(WareConstant.STOCK_EVENT_EXCHANGE, WareConstant.STOCK_LOCKED_ROUTING_KEY, stockLockTO);
                    currSkuLockRes = true;
                    break;
                }
            }
            // 全部仓库都锁定失败
            if (currSkuLockRes == false) {
                throw new BizException(BizCodeEnum.WARE_SKU_STOCK_NOT_ENOUGH, "库存不足，商品：" + skuLockStock);
            }
        }
        return true;
    }

    /**
     * 收到库存锁定过期消息，释放库存
     * @param stockLockTO
     * @return
     */
    @Transactional
    @Override
    public void unlockStock(StockLockTO stockLockTO) {
        Long taskDetailId = stockLockTO.getTaskDetailId();
        WareOrderTaskDetailEntity taskDetail = taskDetailService.getById(taskDetailId);
        // 库存扣减时的任务单已不存在，说明扣减时失败已被回滚，无需处理
        // 库存单虽存在，但是状态已经是已解锁状态或已扣除状态(非锁定状态)，也无需处理
        if (taskDetail == null || taskDetail.getLockStatus() != WareConstant.StockLockStatus.LOCKED.getValue()) {
            return;
        }
        // 库存扣减时的任务单仍然存在，且仍然是已锁定状态
        // 远程调用，判断当时这个订单目前的状态
        R r = orderFeignService.getOrderDetail(stockLockTO.getOrderSn());
        if (r.getCode() != 0) {
            log.error("gulimall-ware调用gulimall-order查询订单失败");
            // 消费失败，重新入队
            throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED);
        } else {
            OrderTO orderTO = r.getData(OrderTO.class);
            // 订单已不存在或已取消 就需要解锁库存
            if (orderTO == null || orderTO.getStatus() == OrderConstant.OrderStatusEnum.CANCLED.getCode()) {
                // 回滚，解锁库存
                System.out.println("<<<<<<<<<<<<<<<解锁库存<<<<<<<<<<<<<<");
                unlockStock(taskDetailId, stockLockTO.getWareId(), stockLockTO.getSkuId(), stockLockTO.getCount());
            }
            // 消费成功，手动ack
        }
    }

    /**
     * 收到订单关闭消息，释放库存
     */
    @Transactional
    @Override
    public void unlockStock(OrderTO orderTO) {
        // 1.获取库存工作单
        WareOrderTaskEntity taskEntity = taskService.getTaskByOrderSn(orderTO.getOrderSn());
        if (taskEntity != null) {
            // 2.获取此条工作单上的全部工作项
            List<WareOrderTaskDetailEntity> detailEntities = taskDetailService.listByTtaskId(taskEntity.getId());
            if (!CollectionUtils.isEmpty(detailEntities)) {
                // 3.找出其中状态为锁定状态的工作项，执行解锁库存方法
                detailEntities.stream()
                        .filter(detailEntity -> detailEntity.getLockStatus() == WareConstant.StockLockStatus.LOCKED.getValue())
                        .forEach(detailEntity -> unlockStock(detailEntity.getId(), detailEntity.getWareId(), detailEntity.getSkuId(), detailEntity.getSkuNum()));
            }
        }
    }

    /**
     * 解锁库存，并修改对应库存工作单状态
     * @param wareId
     * @param skuId
     * @param count
     * @return
     */
    private boolean unlockStock(Long taskDetailId, Long wareId, Long skuId, Integer count) {
        // 解锁库存
        this.baseMapper.unlockStock(wareId, skuId, count);
        // 修改库存工作单状态为已释放
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(taskDetailId);
        detailEntity.setLockStatus(WareConstant.StockLockStatus.RELEASED.getValue());
        return taskDetailService.updateById(detailEntity);
    }

    private boolean isValidId(String key) {
        return !StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(key);
    }

}