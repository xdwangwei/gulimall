package com.vivi.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.vivi.common.constant.OrderConstant;
import com.vivi.common.constant.SeckillConstant;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.to.SeckillSkuTO;
import com.vivi.common.to.SkuInfoTO;
import com.vivi.common.to.mq.SeckillOrderTO;
import com.vivi.common.utils.R;
import com.vivi.common.vo.MemberInfoVO;
import com.vivi.gulimall.seckill.feign.CouponFeignService;
import com.vivi.gulimall.seckill.feign.ProductFeignService;
import com.vivi.gulimall.seckill.interceptor.LoginInterceptor;
import com.vivi.gulimall.seckill.service.SeckillService;
import com.vivi.gulimall.seckill.to.SeckillSkuRedisTO;
import com.vivi.gulimall.seckill.vo.SeckillSessionVO;
import com.vivi.gulimall.seckill.vo.SeckillSkuRelationVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wangwei
 * 2021/1/30 11:01
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CouponFeignService couponFeignService;


    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;


    /**
     * 上架最近三天参与秒杀的活动以及商品
     * @return
     */
    @Override
    public boolean uploadLatest3DaysSeckillSkus() {
        // 1.远程调用gulimall-coupon服务查询最近三天秒杀活动和商品
        R r = couponFeignService.latest3DaysSessions();
        if (r.getCode() != 0) {
            log.error("gulimall-seckill调用gulimall-coupon获取最近三天秒杀活动失败");
            throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "上架秒杀活动信息失败");
        }
        List<SeckillSessionVO> sessionVOS = r.getData(new TypeReference<List<SeckillSessionVO>>(){});
        if (CollectionUtils.isEmpty(sessionVOS)) {
            return true;
        }
        // 3.保存活动场次信息
        log.info(">>>保存秒杀活动场次信息");
        saveSeckillSession(sessionVOS);
        // 4.保存活动商品和库存信息
        log.info(">>>>>>保存秒杀活动商品和库存信息");
        saveSeckillSessionSkus(sessionVOS);
        return true;
    }

    /**
     * 当前正在进行的秒杀商品
     * @return
     */
    @Override
    public List<SeckillSkuRedisTO> currentSeckill() {
        // 1.获取全部秒杀活动
        Set<String> sessionsKeys = redisTemplate.keys(SeckillConstant.REDIS_SECKILL_SESSION_PREFIX + "*");
        if (!CollectionUtils.isEmpty(sessionsKeys)) {
            // 获取当前时间
            long now = new Date().getTime();
            // 2.判断当前处于哪个秒杀场次
            for (String sessionKey : sessionsKeys) {
                // seckill:session:1611972000000_1611979200000 -> 1611972000000_1611979200000
                String sessionTime = sessionKey.replace(SeckillConstant.REDIS_SECKILL_SESSION_PREFIX, "");
                String[] s = sessionTime.split("_");
                long start = Long.parseLong(s[0]);
                long end = Long.parseLong(s[1]);
                if (now >= start && now <= end) {
                    // 3.获取当前场次下所有商品id信息
                    // 每个场次下都是 list，里面是 场次_skuid，range是一次取多个
                    List<String> range = redisTemplate.opsForList().range(sessionKey, 0, 10000);
                    if (!CollectionUtils.isEmpty(range)) {
                        // 4.商品的真正信息
                        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.REDIS_SECKILL_SKU_INFO_KEY);
                        List<String> strings = ops.multiGet(range);
                        // 返回
                        return strings.stream().map(str -> {
                            SeckillSkuRedisTO skuRedisTO = JSON.parseObject(str, SeckillSkuRedisTO.class);
                            // 活动未开启时，不暴露秒杀令牌
                            // skuRedisTO.setSeckillToken(null);
                            return skuRedisTO;
                        }).collect(Collectors.toList());
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * 指定商品参与的最近一次秒杀信息
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuTO getSkuSeckillInfo(Long skuId) {
        // 当前时间
        Date now = new Date();
        // 1.获取所有参与秒杀的商品
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.REDIS_SECKILL_SKU_INFO_KEY);
        Set<String> keys = ops.keys();
        if (!CollectionUtils.isEmpty(keys)) {
            // 每一个key都是 秒杀场次id_skuid
            String regx = "\\d{1,9}_" + skuId;
            // 2.找到指定sku对应的键，一个sku可能参与多场秒杀
            List<SeckillSkuRedisTO> redisTOS = new ArrayList<>();
            for (String key : keys) {
                // 匹配成功
                if (Pattern.matches(regx, key)) {
                    String s = ops.get(key);
                    SeckillSkuRedisTO redisTO = JSON.parseObject(s, SeckillSkuRedisTO.class);
                    redisTOS.add(redisTO);
                }
            }
            // 该商品参与多场秒杀
            if (!CollectionUtils.isEmpty(redisTOS)) {
                // 3.过滤掉已经结束的秒杀活动，并按照开始顺序从先到后排序，返回最早的那场
                List<SeckillSkuRedisTO> collect = redisTOS.stream()
                        .filter(redisTO -> redisTO.getEndTime().after(now))
                        .sorted(Comparator.comparingLong(redisTO -> redisTO.getStartTime().getTime()))
                        .collect(Collectors.toList());
                // 4.要返回的秒杀信息
                if (collect.size() > 0) {
                    SeckillSkuRedisTO redisTO = collect.get(0);
                    SeckillSkuTO seckillSkuTO = new SeckillSkuTO();
                    BeanUtils.copyProperties(redisTO, seckillSkuTO);
                    // 如果这次秒杀还未开始，就屏蔽秒杀令牌
                    if (now.before(seckillSkuTO.getStartTime())) {
                        seckillSkuTO.setSeckillToken(null);
                    }
                    return seckillSkuTO;
                }
            }
        }
        return null;
    }

    /**
     * 秒杀
     * @param seckillId
     * @param seckillToken
     * @param count
     */
    @Override
    public String seckillItem(String seckillId, String seckillToken, Integer count) {
        MemberInfoVO memberInfoVO = LoginInterceptor.threadLocal.get();
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.REDIS_SECKILL_SKU_INFO_KEY);
        // 校验秒杀活动和商品是否存在
        String s = ops.get(seckillId);
        if (StringUtils.isEmpty(s)) {
            throw new BizException(BizCodeEnum.SECKILL_FAILED, "秒杀失败，所选商品秒杀不存在");
        }
        // 校验令牌是否一致
        SeckillSkuRedisTO redisTO = JSON.parseObject(s, SeckillSkuRedisTO.class);
        if (!seckillToken.equals(redisTO.getSeckillToken())) {
            throw new BizException(BizCodeEnum.SECKILL_FAILED, "秒杀失败，商品秒杀令牌校验失败");
        }
        // 校验数量是否合法
        if (count > redisTO.getSeckillLimit()) {
            throw new BizException(BizCodeEnum.SECKILL_FAILED, "秒杀失败，每人最多购买此商品" + redisTO.getSeckillLimit() + "件");
        }
        // 校验该用户是否已秒杀过此商品 seckill:user:sku:场次id_skuid:用户id
        String key = SeckillConstant.SECKILL_USER_ALREADY_EXISTS_PREFIX + seckillId + ":" + memberInfoVO.getId();
        String val = count.toString();
        // 活动结束自动释放锁
        long expire = redisTO.getEndTime().getTime() - new Date().getTime();
        Boolean res = redisTemplate.opsForValue().setIfAbsent(key, val, expire, TimeUnit.MILLISECONDS);
        if (!res) {
            throw new BizException(BizCodeEnum.SECKILL_FAILED, "秒杀失败，您已秒杀过此商品");
        }
        // 秒杀
        RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SEMAPHORE_SECKILL_SKU_STOCK_PREFIX + seckillToken);
        // 5ms内扣减库存
        try {
            semaphore.tryAcquire(count, 5, TimeUnit.MILLISECONDS);
            // 秒杀成功，走快速下单流程
            return createSeckillOrder(redisTO, count);
        } catch (InterruptedException e) {
            log.error("秒杀出错：{}", e);
            throw new BizException(BizCodeEnum.SECKILL_FAILED, "秒杀失败，扣减库存失败");
        }
    }

    /**
     * 快速创建秒杀订单
     * @param redisTO
     * @param count
     * @return
     */
    private String createSeckillOrder(SeckillSkuRedisTO redisTO, Integer count) {
        MemberInfoVO memberInfoVO = LoginInterceptor.threadLocal.get();
        SeckillOrderTO orderTO = new SeckillOrderTO();
        String orderSn = IdWorker.getTimeId();
        orderTO.setOrderSn(orderSn);
        orderTO.setSkuId(redisTO.getSkuId());
        orderTO.setSeckillPrice(redisTO.getSeckillPrice());
        orderTO.setCount(count);
        orderTO.setMemberId(memberInfoVO.getId());
        orderTO.setUserName(memberInfoVO.getUsername());
        orderTO.setPromotionSessionId(redisTO.getPromotionSessionId());
        // 发送消息到mq，订单服务后台处理
        try {
            rabbitTemplate.convertSendAndReceive(OrderConstant.ORDER_EVENT_EXCHANGE, OrderConstant.ORDER_SECKILL_DEAL_QUEUE_ROUTING_KEY, orderTO);
        } catch (AmqpException e) {
            rabbitTemplate.convertSendAndReceive(OrderConstant.ORDER_EVENT_EXCHANGE, OrderConstant.ORDER_SECKILL_DEAL_QUEUE_ROUTING_KEY, orderTO);
            log.warn("秒杀后发送下单消息到gulimall-order交换机失败");
        }
        return orderSn;
    }

    /**
     * 保存秒杀活动场次信息
     */
    private void saveSeckillSession(List<SeckillSessionVO> sessions) {
        for (SeckillSessionVO session : sessions) {
            Date startTime = session.getStartTime();
            Date endTime = session.getEndTime();
            // 前后缀组成key
            String suffix = startTime.getTime() + "_" + endTime.getTime();
            String key = SeckillConstant.REDIS_SECKILL_SESSION_PREFIX + suffix;
            // 避免重复上架
            if (redisTemplate.hasKey(key)) {
                continue;
            }
            // 值为当前场次下所有skuid，有的sku在多个场次中都有，所以保存成 场次id_skuid
            List<String> sessionSkus = session.getRelationSkus().stream().map(sku -> session.getId() + "_" + sku.getSkuId()).collect(Collectors.toList());
            redisTemplate.opsForList().leftPushAll(key, sessionSkus);
            // 设置过期时间，活动结束自动过期,ms
            redisTemplate.expireAt(key, endTime);
        }
    }

    /**
     * 提前锁定好库存。
     * 保存每个场次里的sku详细信息以及库存(信号量形式)
     *
     * hash结构 ：seckill:sku: 场次id_skuid->skuinfo
     *
     * hash结构多用于保存对象信息，键值设置过期时间
     * 我们这里保存 场次:sku信息，又因为不同场次结束时间不同，所以不能对同一个键下不同属性设置不同过期时间
     */
    private void saveSeckillSessionSkus(List<SeckillSessionVO> sessions) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.REDIS_SECKILL_SKU_INFO_KEY);
        for (SeckillSessionVO session : sessions) {
            List<SeckillSkuRelationVO> relationSkus = session.getRelationSkus();
            if (!CollectionUtils.isEmpty(relationSkus)) {
                for (SeckillSkuRelationVO seckillSku : relationSkus) {
                    String key = session.getId() + "_" + seckillSku.getSkuId();
                    // 当前商品信息已上架
                    if (ops.hasKey(key)) {
                        continue;
                    }
                    // 构建seckillSkuredisTO
                    SeckillSkuRedisTO redisTO = new SeckillSkuRedisTO();
                    BeanUtils.copyProperties(seckillSku, redisTO);
                    // 保存开始结束时间
                    redisTO.setStartTime(session.getStartTime());
                    redisTO.setEndTime(session.getEndTime());
                    // 远程调用，设置此商品的详细信息
                    R r = productFeignService.getSkuInfo(seckillSku.getSkuId());
                    if (r.getCode() != 0) {
                        log.error("gulimall-seckill调用gulimall-produt获取最近商品详情失败");
                        throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "上架秒杀活动信息失败");
                    }
                    SkuInfoTO skuInfo = r.getData("skuInfo", SkuInfoTO.class);
                    redisTO.setSkuInfo(skuInfo);
                    // 为当前商品生成唯一令牌
                    String token = UUID.randomUUID().toString();
                    redisTO.setSeckillToken(token);
                    // 上架当前商品，保存到redis
                    ops.put(key, JSON.toJSONString(redisTO));
                    // 信号量key
                    String semaphoreKey = SeckillConstant.SEMAPHORE_SECKILL_SKU_STOCK_PREFIX + token;
                    RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
                    // 设置秒杀库存量为信号量的值
                    semaphore.trySetPermits(seckillSku.getSeckillCount());
                    // 设置库存信号量过期时间
                    semaphore.expireAt(session.getEndTime());
                }
            }
        }

    }


}
