package com.vivi.gulimall.product.test;

import com.vivi.common.constant.ProductConstant;
import com.vivi.gulimall.product.entity.CategoryEntity;
import com.vivi.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/27 10:23
 *
 * 测试下对springcache的整合
 */
@RestController
@RequestMapping("/test/cache")
public class TestSpringCache {

    @Autowired
    CategoryService categoryService;


    /**
     * @Cacheable 代表当前方法的结果需要放入缓存
     *      并且，每次访问这个方法时，会先去缓存中判断数据是否存在，若存在则直接返回缓存中数据，不会执行方法
     *      但是我们需要指定，要将结果放入哪个缓存中，每个cacheManager管理多个cache.
     *      我们需要指定cache的名字(相当于对缓存进行分区管理，建议按照业务进行划分)
     *      使用cacheNames或value属性都可以。可以同时放入多个分区
     *
     *  存入缓存中的键名：product-category::SimpleKey []
     *                  cacheName::默认生成的键名 [方法参数列表]
     *      这个simplekey[]就是自己生成的键名
     *      我们可以使用注解的key属性，指定键名，它接收一个Spel表达式(如果指定普通字符串就要用单引号引起来)
     *          比如 #root.methodName 就是获取方法名
     *       详细用法：https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/integration.html#cache-spel-context
     *   指定key后，得到的键名是：product-category::getLevel1Category
     *
     *  默认生成的值是采用jdk序列化，并且过期时间是-1，
     *  如果我们想要改变默认配置，
     *      一些最基本的配置可以在配置文件中设置：
     *          # 是否缓存空值
     *          spring.cache.redis.cache-null-values=true
     *          # 键的前缀
     *          spring.cache.redis.key-prefix=CACHE_
     *          # 是否使用这个前缀
     *          spring.cache.redis.use-key-prefix=true
     *          # 缓存的有效期
     *          spring.cache.redis.time-to-live=3600000
     *  比较高级的配置，比如设置序列化策略采用json形式，就得自己编写RedisCacheConfiguration
     *
     *  sync 属性：
     *    默认为false。如果设为true。则会为处理过程加上synchronized本地锁。也就是说在一个微服务里面，能够保证线程间互斥访问这个方法
     *
     * @return
     */
    @RequestMapping("/product/category/level1")
    @Cacheable(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
            key = "'level1Category'")
    public List<CategoryEntity> getLevel1Category() {
        System.out.println("查询数据库");
        List<CategoryEntity> level1Categories = categoryService.getLevel1Categories();
        return level1Categories;
    }


    /**
     * @CacheEvict: 触发删除缓存操作
     *      cacheNames: 指定要删除的是哪个缓存分区的缓存
     *      key: 要删除的是这个分区中的那个缓存
     *      allEntries: 是否删除这个分区中的所有缓存。默认false
     * 所以。如果只指定cacheNames.不指定key。不会进行任何删除。
     *      如果设置allEntries = true。那么不用指定key。全部删除
     *
     * key。一次只能指定一个key。那如果要删除多个，但不想全部删除，就需要使用 @Caching
     *     @Caching(
     *             evict = {
     *                     @CacheEvict(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
     *                             key = "'level1Category'"),
     *                     @CacheEvict(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
     *                             key = "'level2Category'")
     *             },
     *             cacheable = {}
     *     )
     * @return
     */
    /**
     * @CachePut
     * 最简单的保证缓存和数据库一致性的方式就是，每次执行更新操作。就将缓存删除。下次查询方法自然会将最新数据存入缓存
     *
     * 如果我们的更新方法没有返回值，也就是更新完就结束，那么我们使用@CacheEvit删除缓存
     * 如果我们的更新方法有返回值，也就是更新成功后将最新数据返回，那么我们使用@CachePut将最新数据更新到缓存
     * @return
     */
    @RequestMapping("/product/category/update")
    @CacheEvict(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
                key = "'level1Category'")
    // @Caching(
    //         evict = {
    //                 @CacheEvict(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
    //                         key = "'level1Category'"),
    //                 @CacheEvict(cacheNames = {ProductConstant.CacheName.PRODUCT_CATEGORY},
    //                         key = "'level2Category'")
    //         }
    // )
    public String updateCategory() {
        System.out.println("更新category数据");
        return "更新成功";
    }

}
