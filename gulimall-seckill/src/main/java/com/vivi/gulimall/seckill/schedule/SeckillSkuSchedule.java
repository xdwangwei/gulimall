package com.vivi.gulimall.seckill.schedule;

import com.vivi.common.constant.SeckillConstant;
import com.vivi.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * @author wangwei
 * 2021/1/30 10:56
 *
 * 秒杀商品 定时任务
 */
@Slf4j
@Component
public class SeckillSkuSchedule {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 定时任务，上架秒杀商品，比如每月1号凌晨3点，测试期间，每10s执行一次
     * 使用redisson分布式锁保证分布式环境下同一时刻只能有一台机器执行秒杀
     *
     */
    @Scheduled(cron = "*/30 * * * * *")
    public void uploadSeckillSku() {
        log.info("开始上架最近三天参与秒杀的商品：" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        // 1.获取互斥锁，开始执行上架功能
        RLock lock = redissonClient.getLock(SeckillConstant.UPLOAD_SKU_LOCK_KEY);
        // 10s后自动释放
        lock.lock(10, TimeUnit.SECONDS);
        try {
            // 2.执行上架业务
            seckillService.uploadLatest3DaysSeckillSkus();
        } catch (Exception e) {
            log.error("定时任务异常：{}", e);
        } finally {
            // 3.无论业务成功与否，一定要释放锁，
            /**
             * 在thread-1还没有结束的时候,也就是在thread-1在获得锁但是还没有释放锁的时候,
             * thread-2由于被别的线程中断停止了等待从lock.lock(5, TimeUnit.MINUTES)的阻塞状态中返回继续执行接下来的逻辑,
             *
             * 并且由于尝试去释放一个属于线程thread-1的锁而抛出了一个运行时异常:
             *      attempt to unlock lock, not locked by current thread by node id: 83ba4b09-d68c-4771-9b6c-f07cce90a34a thread-id: 129
             * 导致该线程thread-2结束了, 然而thread-2完成了一系列操作后,线程thread-1才释放了自己的锁. 所以thread-2并没有获得锁,却执行了需要同步的内容,还尝试去释放锁.
             */
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
