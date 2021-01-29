package com.vivi.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.constant.OrderConstant;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.to.*;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.common.utils.R;
import com.vivi.common.vo.MemberInfoVO;
import com.vivi.gulimall.order.config.AlipayTemplate;
import com.vivi.gulimall.order.dao.OrderDao;
import com.vivi.gulimall.order.entity.OrderEntity;
import com.vivi.gulimall.order.entity.OrderItemEntity;
import com.vivi.gulimall.order.entity.PaymentInfoEntity;
import com.vivi.gulimall.order.feign.CartFeignService;
import com.vivi.gulimall.order.feign.MemberFeignService;
import com.vivi.gulimall.order.feign.ProductFeignService;
import com.vivi.gulimall.order.feign.WareFeignService;
import com.vivi.gulimall.order.interceptor.LoginInterceptor;
import com.vivi.gulimall.order.service.FareService;
import com.vivi.gulimall.order.service.OrderItemService;
import com.vivi.gulimall.order.service.OrderService;
import com.vivi.gulimall.order.service.PaymentInfoService;
import com.vivi.gulimall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    FareService fareService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder() {
        MemberInfoVO loginUser = LoginInterceptor.threadLocal.get();
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        // 原线程绑定的requestAttributes
        // 异步,防止feign丢失请求头
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> orderItemsTask = CompletableFuture.runAsync(() -> {
            // 赋值到新线程绑定的request
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // TODO 1.异步查询购物车
            R res = cartFeignService.getCheckedItems();
            if (res.getCode() != 0) {
                log.error("确认订单远程调用购物车服务查询购物车失败");
                throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "查询购物车失败");
            }
            List<CartItemTO> itemTOS = res.getData(new TypeReference<List<CartItemTO>>() {
            });
            orderConfirmVO.setItems(itemTOS.stream().map(to -> convertOrderItemTO2OrderSkuVO(to)).collect(Collectors.toList()));
        }, executor);
        CompletableFuture<Void> stockTask = orderItemsTask.thenRunAsync(() -> {
            // TODO 3.查询商品库存
            List<Long> ids = orderConfirmVO.getItems().stream().map(OrderSkuVO::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.getSkuStockBatch(ids);
            if (r.getCode() != 0) {
                log.error("调用gulimall-ware查询商品库存失败：{}");
                throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "查询库存失败");
            }
            List<SkuStockTO> stockTOList = r.getData(new TypeReference<List<SkuStockTO>>() {
            });
            Map<Long, Long> collect = stockTOList.stream().collect(Collectors.toMap(SkuStockTO::getSkuId, SkuStockTO::getStock));
            orderConfirmVO.getItems().forEach(item -> item.setStock(collect.getOrDefault(item.getSkuId(), 0L)));
        }, executor);
        CompletableFuture<Void> memberAddressTask = CompletableFuture.runAsync(() -> {
            // TODO 2.异步查询用户地址列表
            R r2 = memberFeignService.getAddresses(loginUser.getId());
            if (r2.getCode() != 0) {
                log.error("确认订单远程调用会员服务查询地址失败");
                throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "查询收货地址失败");
            }
            List<MemberAddressTO> addressTOS = r2.getData(new TypeReference<List<MemberAddressTO>>() {
            });
            orderConfirmVO.setAddresses(addressTOS.stream().map(to -> convert2MemberAddressVO(to)).collect(Collectors.toList()));
        }, executor);
        // TODO 4.异步查询用户优惠，这里简化为用户积分
        orderConfirmVO.setIntegration(loginUser.getIntegration());

        // TODO 5.生成防重令牌，有效时间30min
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + loginUser.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVO.setToken(token);

        // 等待异步任务执行完毕
        try {
            CompletableFuture.allOf(stockTask, memberAddressTask).get();
        } catch (Exception e) {
            log.error("异步编排获取确认订单页面数据失败：{}", e);
            throw new BizException(BizCodeEnum.THREAD_POOL_TASK_FAILED, "出错了，请重试");
        }

        return orderConfirmVO;
    }

    // 使用seata分布式事务，seata at模式(自动提交，全局锁，并发串行化)不适合高并发
    // @GlobalTransactional
    // 使用消息队列实现最终一致性
    @Transactional
    @Override
    public OrderCreateVO submit(OrderSubmitVO submitVO) {
        MemberInfoVO loginUser = LoginInterceptor.threadLocal.get();
        OrderCreateVO createVO = new OrderCreateVO();

        String orderToken = submitVO.getOrderToken();
        // TODO 1.验证令牌
        String key = OrderConstant.ORDER_TOKEN_PREFIX + loginUser.getId();
        // 获取令牌、验证令牌、删除令牌 应该是原子性操作
        // String redisToken = redisTemplate.opsForValue().get(OrderConstant.ORDER_TOKEN_PREFIX + loginUser.getId());
        // if (StringUtils.equals(orderToken, redisToken)) {
        //     // 删除令牌
        //     redisTemplate.delete(OrderConstant.ORDER_TOKEN_PREFIX + loginUser.getId());
        // }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long res = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(key), orderToken);
        if (res == 0) {
            log.warn("订单令牌校验失败，已创建或已过期，请重新下单");
            throw new BizException(BizCodeEnum.ORDER_HAS_EXPIRED);
        }
        // TODO 2.构建订单
        OrderEntity orderEntity = buildOrder(submitVO);
        createVO.setOrder(orderEntity);
        // TODO 3.构建订单项
        List<OrderItemEntity> items = buildOrderItems(orderEntity.getOrderSn());
        createVO.setItems(items);
        // TODO 4.设置订单需要求和项
        calculateOrder(orderEntity, items);

        // TODO 5.后端计算的价格和前端传来的价格进行验证
        BigDecimal totalPrice = submitVO.getTotalPrice();
        BigDecimal payAmount = orderEntity.getPayAmount();
        // 差距大于0.01验价失败
        if (!(Math.abs(totalPrice.subtract(payAmount).doubleValue()) < 0.01)) {
            log.warn("创建订单，前后端验价失败");
            throw new BizException(BizCodeEnum.UNKNOW_ERROR, "下单失败，请重试");
        }
        // TODO 6.远程锁定库存
        lockStock(createVO);


        // TODO 7.保存订单，保存订单项
        this.save(orderEntity);
        orderItemService.saveBatch(items);

        // 向mq中发送订单创建完成消息
        rabbitTemplate.convertAndSend(OrderConstant.ORDER_EVENT_EXCHANGE, OrderConstant.ORDER_CREATE_ROUTING_KEY, orderEntity);

        /**
         * 此处出错，本地事务无法控制远程库存回滚，seata分布式事务可解决，
         * 也可以选择消息队列实现最终一致性
         */
        // int a = 1 / 0;

        // TODO 8.清空购物车中这些购物项
        List<Long> skuIds = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
        R r = cartFeignService.delBatch(skuIds);
        if (r.getCode() != 0) {
            log.error("gulimall-order调用gulimall-cart删除购物项失败：{}");
            throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "下单失败，请重试");
        }

        return createVO;
    }

    @Override
    public OrderCreateVO getOrderDetail(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        if (orderEntity == null) {
            throw new BizException(BizCodeEnum.ORDER_CREATE_FAILED, "订单不存在");
        }
        OrderCreateVO createVO = new OrderCreateVO();
        List<OrderItemEntity> items = orderItemService.listByOrderSn(orderSn);
        createVO.setOrder(orderEntity);
        createVO.setItems(items);
        return createVO;
    }

    @Override
    public OrderTO getOrderTOByOrderSn(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        if (orderEntity == null) {
            return null;
        } else {
            OrderTO orderTO = new OrderTO();
            BeanUtils.copyProperties(orderEntity, orderTO);
            return orderTO;
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Transactional
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 获取订单最新状态
        OrderEntity order = getOrderByOrderSn(orderEntity.getOrderSn());
        // 仍然是未付款状态
        if (order.getStatus() == OrderConstant.OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity entity = new OrderEntity();
            entity.setId(order.getId());
            entity.setStatus(OrderConstant.OrderStatusEnum.CANCLED.getCode());
            this.updateById(entity);
            // 向mq发送释放库存消息
            rabbitTemplate.convertAndSend(OrderConstant.ORDER_EVENT_EXCHANGE, OrderConstant.ORDER_RELEASE_STOCK_ROUTING_KEY, getOrderTOByOrderSn(order.getOrderSn()));
        }
    }

    @Override
    public String payOrder(String orderSn) {
        OrderEntity order = getOrderByOrderSn(orderSn);
        if (order == null) {
            throw new BizException(BizCodeEnum.ORDER_PAY_FEILED, "订单不存在");
        } else if(order.getStatus() == OrderConstant.OrderStatusEnum.PAYED.getCode()) {
            // 订单已支付
            throw new BizException(BizCodeEnum.ORDER_PAY_FEILED, "此订单已支付");
        }
        AlipayVO alipayVO = new AlipayVO();
        alipayVO.setOutTradeNo(orderSn);
        // 支付宝支付要求金额必须为小数点后两位
        alipayVO.setTotalAmount(order.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString());
        alipayVO.setSubject("谷粒商城订单");
        alipayVO.setBody("谷粒商城订单");
        try {
            String response = alipayTemplate.pay(alipayVO);
            log.info("支付宝支付响应：{}", response);
            return response;
        } catch (AlipayApiException e) {
            log.error("阿里支付失败：{}", e.getMessage());
            throw new BizException(BizCodeEnum.ORDER_PAY_FEILED);
        }
    }

    @Override
    public PageUtils getCurrentUserOrderList(Map<String, Object> params) {
        MemberInfoVO memberInfoVO = LoginInterceptor.threadLocal.get();
        // 获取当前登录用户的所有订单
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberInfoVO.getId()).orderByDesc("id")
        );
        // 查出每个订单下的订单项
        List<OrderEntity> collect = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> orderItemEntities = orderItemService.listByOrderSn(order.getOrderSn());
            order.setItems(orderItemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(collect);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public String handleAlipayNotify(AlipayNotifyVO notifyVO, HttpServletRequest request) {
        boolean verified;
        // 1.验签
        try {
            verified = alipayTemplate.signVerify(request);
        } catch (Exception e) {
            log.warn("阿里支付异步通知验签失败");
            return "error";
        }
        // 验签失败
        if (!verified) {
            return "error";
        }
        // 2.保存支付流水
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setAlipayTradeNo(notifyVO.getTrade_no());
        paymentInfo.setCallbackTime(notifyVO.getNotify_time());
        paymentInfo.setOrderSn(notifyVO.getOut_trade_no());
        paymentInfo.setPaymentStatus(notifyVO.getTrade_status());
        paymentInfo.setTotalAmount(new BigDecimal(notifyVO.getTotal_amount()));
        paymentInfo.setSubject(notifyVO.getSubject());
        paymentInfoService.save(paymentInfo);
        // 3.修改订单状态为已支付
        if (notifyVO.getTrade_status().equals("TRADE_SUCCESS") || notifyVO.getTrade_status().equals("TRADE_FINISHED")) {
            String orderSn = notifyVO.getOut_trade_no();
            updateOrderStatusByOrderSn(orderSn, OrderConstant.OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    private boolean updateOrderStatusByOrderSn(String orderSn, Integer status) {
        return this.baseMapper.updateOrderStatusByOrderSn(orderSn, status);
    }

    /**
     * 锁定库存
     * @param createVO
     */
    private void lockStock(OrderCreateVO createVO) {
        OrderLockStockTO lockStockTO = new OrderLockStockTO();
        lockStockTO.setOrderSn(createVO.getOrder().getOrderSn());
        List<OrderLockStockTO.SkuLockStock> collect = createVO.getItems().stream().map(item -> {
            OrderLockStockTO.SkuLockStock skuLock = new OrderLockStockTO.SkuLockStock();
            skuLock.setSkuId(item.getSkuId());
            skuLock.setSkuName(item.getSkuName());
            skuLock.setCount(item.getSkuQuantity());
            return skuLock;
        }).collect(Collectors.toList());
        lockStockTO.setLocks(collect);
        // 远程调用
        R r = wareFeignService.lockStock(lockStockTO);
        if (r.getCode() != 0) {
            throw new BizException(BizCodeEnum.WARE_SKU_STOCK_NOT_ENOUGH, "下单失败：" + r.get("msg"));
        }
    }

    /**
     * 创建订单
     * @param submitVO
     * @return
     */
    private OrderEntity buildOrder(OrderSubmitVO submitVO) {
        MemberInfoVO loginUser = LoginInterceptor.threadLocal.get();
        OrderEntity orderEntity = new OrderEntity();
        // 1.订单创建者
        orderEntity.setMemberId(loginUser.getId());
        orderEntity.setMemberUsername(loginUser.getUsername());
        // 2.收货地址和运费
        FareInfoTO fare = fareService.getFare(submitVO.getAddressId());
        MemberAddressTO address = fare.getAddress();
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        // orderEntity.setReceiverRegion();
        // orderEntity.setReceiverPostCode();
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        // orderEntity.setReceiveTime();
        // 运费
        orderEntity.setFreightAmount(fare.getFare());
        // 自动确认收货天数
        orderEntity.setAutoConfirmDay(7);
        // 3.支付信息
        orderEntity.setPayType(submitVO.getPayType());
        // 4.订单/订单项部分
        orderEntity.setOrderSn(IdWorker.getTimeId());
        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(new Date());
        orderEntity.setStatus(OrderConstant.OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setDeleteStatus(0);
        orderEntity.setConfirmStatus(0);
        // 备注
        // orderEntity.setNote();
        // 5.优惠/发票/快递部分
        return orderEntity;
    }

    /**
     * 构建一个订单项
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 1.获取购物车中所有已选购物项
        // TODO 1.异步查询购物车
        R res = cartFeignService.getCheckedItems();
        if (res.getCode() != 0) {
            log.error("确认订单远程调用购物车服务查询购物车失败");
            throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "查询购物车失败");
        }
        List<CartItemTO> itemTOS = res.getData(new TypeReference<List<CartItemTO>>() {
        });
        List<OrderItemEntity> items = itemTOS.stream().map(cartItemTO -> {
            OrderItemEntity orderItem = buildOrderItemFromCartItem(cartItemTO);
            orderItem.setOrderSn(orderSn);
            return orderItem;
        }).collect(Collectors.toList());
        return items;
    }

    private OrderItemEntity buildOrderItemFromCartItem(CartItemTO cartItem) {
        OrderItemEntity orderItemVO = new OrderItemEntity();
        // 1. sku部分
        orderItemVO.setSkuId(cartItem.getSkuId());
        orderItemVO.setSkuName(cartItem.getSkuTitle());
        orderItemVO.setSkuPrice(cartItem.getPrice());
        orderItemVO.setSkuPic(cartItem.getSkuImg());
        orderItemVO.setSkuQuantity(cartItem.getCount());
        orderItemVO.setSkuAttrsVals(cartItem.getAttrs().stream().collect(Collectors.joining(";")));
        // 2. spu部分
        R r = productFeignService.getBySkuId(cartItem.getSkuId());
        if (r.getCode() != 0) {
            log.error("gulimall-order调用gulimall-product查询spuinfo失败");
            throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "下单失败请重新");

        }
        SpuInfoTO spuInfoTO = r.getData(SpuInfoTO.class);
        orderItemVO.setSpuId(spuInfoTO.getId());
        orderItemVO.setSpuName(spuInfoTO.getSpuName());
        // orderItemVO.setSpuPic;
        orderItemVO.setSpuBrand(spuInfoTO.getBrandName());
        orderItemVO.setCategoryId(spuInfoTO.getCatelogId());

        // 3.积分部分
        orderItemVO.setGiftGrowth(spuInfoTO.getGrowBounds().intValue() * cartItem.getCount());
        orderItemVO.setGiftIntegration(spuInfoTO.getIntegration().intValue() * cartItem.getCount());

        // 4. 价格部分
        BigDecimal zero = new BigDecimal("0");
        // 各种优惠，简略为0
        orderItemVO.setCouponAmount(zero);
        orderItemVO.setIntegrationAmount(zero);
        orderItemVO.setPromotionAmount(zero);
        BigDecimal origin = orderItemVO.getSkuPrice().multiply(new BigDecimal(orderItemVO.getSkuQuantity().toString()));
        // 原价 数量 * 单价
        BigDecimal subtract = origin.subtract(orderItemVO.getCouponAmount()).subtract(orderItemVO.getIntegrationAmount()).subtract(orderItemVO.getPromotionAmount());
        // 实际价格=原价-优惠
        orderItemVO.setRealAmount(subtract);
        return orderItemVO;
    }

    /**
     * 订单中一些属性需要订单项总和的计算
     * @param items
     * @return
     */
    private void calculateOrder(OrderEntity orderEntity, List<OrderItemEntity> items) {
        int growth = 0, integration = 0;
        BigDecimal totalAmount = new BigDecimal("0");
        BigDecimal couponAmount = new BigDecimal("0");
        BigDecimal promotionAmount = new BigDecimal("0");
        BigDecimal integrationAmount = new BigDecimal("0");
        if (!CollectionUtils.isEmpty(items)) {
            for (OrderItemEntity item : items) {
                growth += item.getGiftGrowth();
                integration += item.getGiftIntegration();
                totalAmount = totalAmount.add(item.getRealAmount());
                couponAmount = couponAmount.add(item.getCouponAmount());
                promotionAmount = promotionAmount.add(item.getPromotionAmount());
                integrationAmount = integrationAmount.add(item.getIntegrationAmount());
            }
        }
        // 积分和成长值，所有订单项的总和
        orderEntity.setIntegration(integration);
        orderEntity.setGrowth(growth);
        // 总优惠 = 每项优惠了的总和
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        // 总金额 = 每项应付金额和
        orderEntity.setTotalAmount(totalAmount);
        // 总应付金额 = 总金额+运费
        orderEntity.setPayAmount(totalAmount.add(orderEntity.getFreightAmount()));
    }

    private OrderSkuVO convertOrderItemTO2OrderSkuVO(CartItemTO itemTO) {
        OrderSkuVO orderSkuVO = new OrderSkuVO();
        BeanUtils.copyProperties(itemTO, orderSkuVO);
        return orderSkuVO;
    }

    private MemberAddressVO convert2MemberAddressVO(MemberAddressTO addressTO) {
        MemberAddressVO addressVO = new MemberAddressVO();
        BeanUtils.copyProperties(addressTO, addressVO);
        return addressVO;
    }


}