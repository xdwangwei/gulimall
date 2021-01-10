package com.vivi.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.constant.ProductConstant;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.CategoryBrandRelationDao;
import com.vivi.gulimall.product.dao.CategoryDao;
import com.vivi.gulimall.product.entity.CategoryEntity;
import com.vivi.gulimall.product.service.CategoryService;
import com.vivi.gulimall.product.vo.Catelog2VO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }


    @Cacheable(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
            key = "'categoryList'")
    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有的菜单
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        return categoryEntities.stream()
                // 过滤出一级菜单
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                // 为一级菜单找到其所有的二级菜单
                .map(entity -> getChildren(entity, categoryEntities))
                // 按照菜单优先级排序
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                // 返回最终的一级菜单
                .collect(Collectors.toList());
    }

    /**
     * 为了保证数据一致性，此方法执行后清空缓存
     * @param list
     * @return
     */
    @CacheEvict(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
            allEntries = true)
    @Override
    public boolean removeBatchByIds(List<Long> list) {
        // TODO 删除菜单前需要检查当前菜单是否在别处被引入，不能轻易删除，即便是逻辑删除也不行
        baseMapper.deleteBatchIds(list);
        return true;
    }

    @Override
    public List<Long> findCategoryPath(Long catId) {
        LinkedList<Long> path = new LinkedList<>();
        LinkedList<Long> fullPath = getFullPath(catId, path);
        // 逆序
        Collections.reverse(fullPath);
        return fullPath;
    }

    /**
     * 默认生成的updateById()只会更新存储category信息的表
     * 实际上可能在其他表中和此表有关联，通常会有一个category_id字段，关联真正的category表
     * 但是如果只存category_id，需要其他信息再去查，就会总成数据库压力大，所以通常会伴随有几个冗余字段，比如category_name
     * 所以在更新brand表的时候，如果更新字段部分包括了出现在其他表中的冗余字段，则需要将这些关联的表的这些部分也更新了
     * 这样才能保证数据一致性
     *
     *
     * 更新完删除缓存
     * @param categoryEntity
     */
    @CacheEvict(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
                allEntries = true)
    @Override
    public boolean updateCascadeById(CategoryEntity categoryEntity) {
        // 先更新category表本身
        this.updateById(categoryEntity);

        // 判断更新字段部分是否包括了出现在其他表中的冗余字段，

        // brand_category_relation表中存在category_id关联了category表，并有冗余字段category_name
        if (!StringUtils.isEmpty(categoryEntity.getName())) {
            // 更新brand_category_relation表中的brand_name冗余字段
            categoryBrandRelationDao.updateCategoryName(categoryEntity.getCatId(), categoryEntity.getName());
        }

        // TODO 其他有关表中相关冗余字段的判断以及更新
        return true;
    }

    /**
     * 查出所有一级分类菜单
     * @return
     */
    @Cacheable(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
            key = "'level1Categories'")
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    /**
     * 首页三级分类渲染所需要的的数据模型     ----新版
     *
     * springCache整合
     * @return
     */
    @Cacheable(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
                key = "'catelogJson'")
    @Override
    public Map<String, List<Catelog2VO>> getCatelogJson() {
        return getCatelogJsonFromDB();
    }

    /**
     * 首页三级分类渲染所需要的的数据模型  ---- 旧版
     *
     * 加入自己实现的缓存逻辑，加入锁，保证只访问一次数据库，其他请求直接访问redis
     * @return
     */
    private Map<String, List<Catelog2VO>> getCatelogJsonOld() {
        String str = stringRedisTemplate.opsForValue().get(ProductConstant.RedisKey.CATELOG_JSON_VALUE);
        // 如果redis中有，就直接返回
        if (!StringUtils.isEmpty(str)) {
            System.out.println("缓存中有数据，直接返回");
            return JSON.parseObject(str, new TypeReference<Map<String, List<Catelog2VO>>>(){});
        //  redis中没有
        } else {
            System.out.println("缓存中没有数据，重新获取...");
            /**
             * 访问数据库，重新加入缓存，使用锁控制并发
             * 如果同步代码块中只从数据库得到数据，在此处存入缓存，
             * 可能存在第一个线程过来，发现缓存无数据，加锁，查询数据库，释放锁。
             * 接着要把数据放入缓存。因为他已经释放锁了。可能可能在此时第二个线程进来了。
             * 第一个线程还没有把数据放进去，所以第二个线程发现缓存中没有，也会去查询数据库。
             * 这样我们就没有做到保证只访问一次数据库。
             *
             * 因此，我们应该把 查询数据库+放入缓存 放在同步代码块内，这样就只会有一次查数据库，
             * 紧接着放入缓存，然后释放锁。其他所有请求进来都能直接从缓存中拿到数据。
             */
            // 使用本地锁
            // Map<String, List<Catelog2VO>> catelogJsonFromDB = getCatelogJsonWithLocalLock();
            // 使用redis实现的简单分布式锁
            // Map<String, List<Catelog2VO>> catelogJsonFromDB = getCatelogJsonWithRedisLock();
            // 使用redisson分布式锁
            Map<String, List<Catelog2VO>> catelogJsonFromDB = getCatelogJsonWithRedisson();
            // 返回
            return catelogJsonFromDB;
        }
    }


    /**
     * 加入本地锁
     * synchronized互斥锁，只要是同一把锁，就能锁住需要这个锁的所有线程
     * springboot默认创建的对象都是单例的。所以这里锁this是可以的。
     *
     * 局限：本地锁只能应用于当前服务，当前进程，无法在分布式环境下使用。
     *      假如有8个商品微服务，每个都使用本地锁。能够保证每个微服务内同一时刻只能有一个线程访问数据库
     *      但是不能保证整个项目同一时刻只有一个线程访问数据库
     *
     * 第一个请求落到了第一个服务，拿到本地锁
     * 第一个线程redis中没有，去查数据库，查完后，还没放入redis
     * 第二个线程落到了第二个服务，拿到本地锁。去访问redis，发现里面没有，也去查数据库
     * @return
     */
    private Map<String, List<Catelog2VO>> getCatelogJsonWithLocalLock() {
        synchronized (this) {
            // 拿到锁以后，应该再去缓存中确定一次是否存在数据(上一个释放锁的人可能把数据放入缓存了)
            // 方法中包含了这段逻辑
            return getCatelogJsonData();
        }
    }

    /**
     * 使用redis的set和del指令，或lua脚本来实现最简单的分布式锁
     * https://redis.io/commands/set
     *
     * setnx命令 : 除非这个键不存在才能设置成功。也就是只能有一个人能拿到锁
     * 线程先尝试set<key,value>，如果成功，表明抢到锁，开始执行业务。执行完毕后，释放锁，也就是删除键
     *  1. 去redis中设置键值。占坑
     *  2. 占锁成功，执行业务
     *  3. 释放锁
     *
     *  问题一：
     *      如果占锁成功后，执行业务的时候断电，那么其他线程永远拿不到锁
     *  解决：
     *      占锁成功后，设置锁的有效时间，即便业务自己终断没有手动释放锁。时间到了redis会自己删除
     *  示例：
     *      Boolean result = stringRedisTemplate.opsForValue().setIfAbsent("lock","v");
     *      stringRedisTemplate.expire("lock", 30s)
     *      执行业务
     *      del("lock")
     *  问题二：
     *      如果先拿到锁，将要去设置过期时间的时候断电了。那么过期时间就没设置上，还会造成锁无法释放
     *  解决：
     *      保证，抢锁+设置过期时间 是一个原子操作
     *      setnx，可以同时制定过期时间，
     *      一条指令，就是一个原子操作 redisTemplate.setIfAbsent("lock","v",30s)
     *
     *  问题三：
     *      抢锁+设置时间成功，执行业务。
     *      但是由于设置的过期时间比较短，现在锁到期了，redis自动删除。
     *      那么别的线程就能够抢到锁(设置一个键值)，
     *      此时我执行完业务。执行释放锁。那么我删除的就是别人的锁！！！
     *  解决：
     *      线程在抢锁的时候，设置的值加一个随机数，抢占同一把锁，是同一个键，但是每个线程设置的值不一样
     *      我在删锁的时候先按这个键去redis访问一把，得到的值和我放进去的一样，说明我的锁没到期，我要手动释放
     *      如果不一样，说明我的锁已经到期了，redis自动释放了。现在别的线程抢到锁了。我就不用管了
     *  示例：
     *      抢锁set("lock",uuid)，设置过期时间
     *      执行业务
     *      ①if(uuid.equals(redis.get("lock")))  ②redis.del("key")
     *  问题四：
     *      释放锁，先去获取值，比对成功再去删除。
     *      如果我去获取值的时候，锁还没到期，那么redis返回给我的就是我的锁，
     *      然后我要去删除，但是这个时候我的锁到期了，第二个线程进入redis设置了一把锁。
     *      那么我执行del("lock")就删除了别人的锁(删除操作只需要键)
     *  解决：
     *      保证 访问锁，确认是自己的，再删除  是一个原子操作。
     *      先get()，再del()是不行的。redis提供了lua脚本的方式来执行这个原子操作
     *      https://redis.io/commands/set
     *
     *  总结：
     *      最简单的方式就是设置锁的时候保证过期时间一定能满足业务完全执行完，
     *      这样一定是自己删除自己的锁。即便断电，最终redis也能自己释放
     * @return
     */
    private Map<String, List<Catelog2VO>> getCatelogJsonWithRedisLock() {
        // 自己唯一的键值
        String uuid = UUID.randomUUID().toString();
        // 1. 去redis中抢锁，原子性，假设设置过期时间30s
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(ProductConstant.RedisKey.CATELOG_JSON_LOCK, uuid, 30, TimeUnit.SECONDS);
        // 抢锁成功
        Map<String, List<Catelog2VO>> data = null;
        if (result) {
            // 2. 执行业务
            try {
                // 拿到锁以后，应该再去缓存中确定一次是否存在数据(上一个释放锁的人可能把数据放入缓存了)
                // 方法中包含了这段逻辑
                data =  getCatelogJsonData();
            } finally {
            //     3. 释放锁，原子性, lua脚本
            //     String res = stringRedisTemplate.opsForValue().get(ProductConstant.RedisKey.CATELOG_JSON_LOCK);
            //     // 确定是自己的锁
            //     if (res.equals(uuid)) {
            //         stringRedisTemplate.delete(ProductConstant.RedisKey.CATELOG_JSON_LOCK);
            //     }
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                stringRedisTemplate.execute(new DefaultRedisScript<Integer>(script) {}, Arrays.asList(ProductConstant.RedisKey.CATELOG_JSON_LOCK), uuid);
                return data;
            }
        } else {
            //    休眠100ms。再去尝试，相当于自旋
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("heheheheh");
            }
            return getCatelogJsonWithRedisLock();
        }
    }


    /**
     * 使用分布式锁 redisson
     * https://redis.io/topics/distlock
     * https://github.com/redisson/redisson
     *
     * Redisson是一个在Redis的基础上实现的Java驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的Java常用对象，还提供了许多分布式服务。
     * 其中包括(BitSet, Set, Multimap, SortedSet, Map, List, Queue, BlockingQueue, Deque, BlockingDeque, Semaphore, Lock, AtomicLong, CountDownLatch, Publish / Subscribe, Bloom filter, Remote service, Spring cache, Executor service, Live Object service, Scheduler service)
     * Redisson提供了使用Redis的最简单和最便捷的方法。Redisson的宗旨是促进使用者对Redis的关注分离（Separation of Concern），从而让使用者能够将精力更集中地放在处理业务逻辑上。
     *
     * 1. 导入erdisson依赖，可去maven仓库
     * 2. 编写配置类，创建 RedissonClient对象
     * 3. @autowired注入RedissonClient对象
     * 4. 获取锁 参数就是锁的名字
     *          // 获取分布式可重入锁，最基本的锁
     *         RLock lock = redissonClient.getLock("锁名");
     *         // 获取读写锁
     *         redissonClient.getReadWriteLock("anyRWLock");
     *         // 信号量
     *         redissonClient.getSemaphore("semaphore");
     *
     *    Rlock实现了juc下的lock，完全可以像使用本地锁一样使用它
     *
     * 5. 以可重入锁为例
     *   如果直接执行 lock.lock();
     *      Redisson内部提供了一个监控锁的看门狗，它的作用是在Redisson实例被关闭前，(定时任务)不断的延长锁的有效期。
     *      默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改Config.lockWatchdogTimeout来另行指定
     *      也就是说，先加锁，然后执行业务，锁的默认有效期是30s，业务进行期间，会通过定时任务不断将锁的有效期续至30s。直到业务代码结束
     *      所以即便不手动释放锁。最终也会自动释放
     *      默认是任务调度的周期是 看门狗时间 / 3  = 10s
     *
     *   也可以使用 lock.lock(10, TimeUnit.SECONDS);手动指定时间
     *      此时，不会有定时任务自动延期，超过这个时间后锁便自动解开了
     *      需要注意的是，如果代码块中有手动解锁，但是业务执行完成之前锁的有效期到了，
     *      此时执行unlock会报错：当前线程无法解锁
     *      因为现在redis中的锁是另一个线程加上的，而他的删锁逻辑是lua脚本执行
     *      先获取键值，判断是否是自己加的锁。如果是。则释放，lua脚本保证这是一个原子操作
     *      所以，手动设置时间必须保证这个时间内业务能够执行完成
     *
     *
     * @return
     */
    private Map<String, List<Catelog2VO>> getCatelogJsonWithRedisson() {
        // 获取分布式可重入锁
        RLock lock = redissonClient.getLock("anyLock");
        // 加锁
        lock.lock();
        Map<String, List<Catelog2VO>> catelogJsonData = null;
        try {
            // 执行业务
            // 模拟业务执行时间很长，看redisson会不会自动给锁续期
            Thread.sleep(30000);
            catelogJsonData = getCatelogJsonData();
        } catch (InterruptedException e) {
            System.out.println("hahahahha");
        } finally {
        //     释放锁
            lock.unlock();
        }
        return catelogJsonData;
    }

    /**
     * 从数据库或者缓存中，拿到需要的三级分类数据
     * @return
     */
    private Map<String, List<Catelog2VO>> getCatelogJsonData() {
        // 拿到锁以后，应该再去缓存中确定一次是否存在数据(上一个释放锁的人可能把数据放入缓存了)
        String str = stringRedisTemplate.opsForValue().get(ProductConstant.RedisKey.CATELOG_JSON_VALUE);
        // 如果redis中有，就直接返回
        if (!StringUtils.isEmpty(str)) {
            return JSON.parseObject(str, new TypeReference<Map<String, List<Catelog2VO>>>() {
            });
        }
        // 确定缓存中没有，就去访问DB
        Map<String, List<Catelog2VO>> catelogMap = getCatelogJsonFromDB();
        // 保存进缓存
        stringRedisTemplate.opsForValue().set(ProductConstant.RedisKey.CATELOG_JSON_VALUE, JSON.toJSONString(catelogMap));
        // 返回最终需要的map
        return catelogMap;
    }

    /**
     * 实际访问数据库，拿到三级分类数据，封装成指定格式
     * 最终需要一个 Map<id, List<Catelog2VO>>，map里面的键是catelog1Id，也就是一级分类的id
     * @return
     */
    private Map<String, List<Catelog2VO>> getCatelogJsonFromDB() {
        System.out.println("实际查询MySQL数据库...");
        // 查出所有的菜单
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 所有一级菜单
        List<CategoryEntity> level1Categories = categoryEntities.stream().filter(item -> item.getParentCid() == 0).collect(Collectors.toList());

        Map<String, List<Catelog2VO>> catelogMap = level1Categories.stream()
                // collectors.toMap 生成map
                .collect(Collectors.toMap(
                        // 一级分类的catId作为map的键
                        level1Category -> level1Category.getCatId().toString(),
                        // 一级分类的所有二级分类作为map的值
                        leve1Category -> {
                            // 先过滤出这个一级分类下的全部二级分类
                            List<Catelog2VO> catelog2VOList = categoryEntities.stream().filter(category -> category.getParentCid().equals(leve1Category.getCatId()))
                                    // 再讲二级分类实体类 -> Catelog2VO
                                    .map(level2Category -> {
                                        // 设置属性
                                        Catelog2VO catelog2VO = new Catelog2VO();
                                        catelog2VO.setCatelog1Id(leve1Category.getCatId().toString());
                                        catelog2VO.setId(level2Category.getCatId().toString());
                                        catelog2VO.setName(level2Category.getName());
                                        // Catelog2VO中有个字段是Catelog3VO
                                        // 先过滤出这个二级分类下的所有分类
                                        List<Catelog2VO.Catelog3VO> catelog3VOList = categoryEntities.stream().filter(category -> category.getParentCid().equals(level2Category.getCatId()))
                                                // 再把这些三级分类对象 -> catelog3VO
                                                .map(level3Category -> {
                                                    // 设置属性
                                                    Catelog2VO.Catelog3VO catelog3VO = new Catelog2VO.Catelog3VO();
                                                    catelog3VO.setCatelog2Id(level2Category.getCatId().toString());
                                                    catelog3VO.setId(level3Category.getCatId().toString());
                                                    catelog3VO.setName(level3Category.getName());
                                                    return catelog3VO;
                                                }).collect(Collectors.toList());
                                        // 填充给catelog2VO对应的字段
                                        catelog2VO.setCatelog3List(catelog3VOList);
                                        return catelog2VO;
                                    }).collect(Collectors.toList());
                            // 返回catelog2VOList，作为map<k,v>的值
                            return catelog2VOList;
                        }));
        return catelogMap;
    }

    // 得到 [225, 25, 2]
    private LinkedList<Long> getFullPath(Long catId, LinkedList<Long> path) {
        // 把自己加入路径
        path.add(catId);
        // 如果它不是第一层菜单
        CategoryEntity categoryEntity = this.getById(catId);
        if (categoryEntity.getParentCid() != 0) {
            // 递归查找它的父菜单
            getFullPath(categoryEntity.getParentCid(), path);
        }
        // 返回完整路径
        return path;
    }

    /**
     * 传入一个一级菜单和全部菜单，找到这个菜单的全部孩子，设置到他相应的属性，再把它返回
     * @param entity
     * @param entityList
     * @return
     */
    private CategoryEntity getChildren(CategoryEntity entity, List<CategoryEntity> entityList) {
        // 设置属性children
        entity.setChildren(entityList.stream()
                // 找到他的所有孩子(二级)
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(entity.getCatId()))
                // 为他的孩子找到他们的下一级菜单(第三级)，再返回他的孩子
                .map(item -> getChildren(item, entityList))
                // 按照菜单优先级排序
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                // 最终得到找到了第三级菜单的二级菜单
                .collect(Collectors.toList()));
        // 返回找到了二级菜单的一级菜单
        return entity;
    }

}