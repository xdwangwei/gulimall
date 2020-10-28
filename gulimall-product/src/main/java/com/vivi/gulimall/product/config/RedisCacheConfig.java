package com.vivi.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author wangwei
 * 2020/10/27 10:58
 */
@EnableConfigurationProperties(CacheProperties.class)
@Configuration
public class RedisCacheConfig {

    /**
     * 没有无参构造方法。不能直接 new RedisCacheConfiguration();
     * 使用RedisCacheConfiguration.defaultCacheConfig();默认配置，再修改
     *
     * 用户的自定义配置都在 application.yml中其中spring.cache部分。
     * 它和CacheProperties.class进行了绑定，
     *      @ConfigurationProperties(prefix = "spring.cache")
     *      public class CacheProperties {}
     * 但是它没有被作为一个bean放入容器中。
     *
     * 我们需要手动导入
     * @EnableConfigurationProperties(CacheProperties.class)
     * 这样容器中就会创建出一个 cacheProperties对象。我们可以使用参数接收
     *
     * 注意这些设置方法是重新创建对象，所以需要重新接收返回值
     * config = config.serializeKeysWith(xxx);
     * @return
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // 设置键的序列化策略
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        // 设置值的序列化策略
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
        // 加载配置文件中用户自定义配置
        // 拿出redis部分
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        // 缓存的有效期
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        // 键的前缀
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        // 是否缓存空值
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        // 是否使用这个前缀
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
