# gulimall
分布式商城

## Docker

### Docker安装Redis
```shell script
docker pull redis:5.0.9
mkdir /root/docker/redis
vim /root/docker/redis/redis.conf
    port 6379
    requirepass xxxx
    appendonly yes
docker run -p 6379:6379 --name redis \
        -v /root/docker/redis/data:/date \
        -v /root/docker/redis/redis.conf:/etc/redis/redis.conf \
        -d redis-server /etc/redis/redis.conf
```
### Docker安装Nginx
```shell script
mkdir /root/docker/nginx
mkdir /root/docker/nginx/conf
# 由于我们现在没有配置文件，也不知道配置什么。可以先启动一个nginx，讲他的配置文件拷贝出来
# 再作为映射，启动真正的nginx
docker pull nginx:1.17.4
docker run --name some-nginx -d nginx:1.17.4
docker container cp some-nginx:/etc/nginx /root/docker/nginx/conf
# 然后就可以删除这个容器了
docker docker rm -f some-nginx
# 启动nginx
docker run --name nginx -p 80:80 \
        -v /root/docker/nginx/conf:/etc/nginx \
        -v /root/docker/nginx/html:/usr/share/nginx/html \
        -d nginx:1.17.4

```
### Docker安装ElasticSearch
```shell script
docker pull elasticsearch:7.8.0
docker run -p 9200:9200 -p 9300:9300 --name es7.8 \
-e "discovery.type=single-node" \
-e ES_JAVA_OPTS="-Xms128m -Xmx512m" \
-v /root/docker/elasticsearch/plugins:/usr/share/elasticsearch/plugins \
-v /root/docker/elasticsearch/data:/usr/share/elasticsearch/data \
-v /root/docker/elasticsearch/logs:/usr/share/elasticsearch/logs \
-d elasticsearch:7.8.0
```
### Docker安装Kibana
```sehll
# 拉取镜像
# kibana版本必须和elasticsearch版本保持一致
docker pull kibana:7.8.0

# 启动容器
# YOUR_ELASTICSEARCH_CONTAINER_NAME_OR_ID 正在运行的ES容器ID或name
docker run --link YOUR_ELASTICSEARCH_CONTAINER_NAME_OR_ID:elasticsearch -p 5601:5601 {docker-repo}:{version}
docker run --link es7.8:elasticsearch -p 5601:5601 --name kibana -d kibana:7.8.0
```
### DockerElasticSearch安装IK分词器
```sehll
# Ik分词器版本要和ES和Kibana版本保持一致

# 进入容器
docker exec -it es7.8 /bin/bash
#此命令需要在容器中运行
elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.8.0/elasticsearch-analysis-ik-7.8.0.zip
# 退出容器，重启容器
docker restart es7.8
```
## spring-cloud-alibaba的使用
### 引入依赖，全套组件版本统一管理
```xml
    <properties>
        <springcloud.alibaba.version>2.2.2.RELEASE</springcloud.alibaba.version>
    </properties>
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
### nacos作为注册中心
1. 本地启动nacos客户端，windows双击startup.bat
若启动失败，则在当前目录下打开cmd，执行startup.bat -m standalone
2. 引入nacos作为注册中心的依赖
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```
3. 在配置文件中配置注册中心地址以及服务名和端口
```properties
# 服务端口
server.port=8070
# 服务名
spring.application.name=service-provider
# 注册中心地址
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
```
4. 启用服务注册发现配置 @EnableDiscoveryClient
```java
@SpringBootApplication
@EnableDiscoveryClient
public class NacosProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosProviderApplication.class, args);
	}
}
```
### nacos作为配置中心
1. 本地启动nacos客户端，windows双击startup.bat
若启动失败，则在当前目录下打开cmd，执行startup.bat -m standalone
2. 引入nacos作为配置中心的依赖
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```
3. 在配置文件bootstrap.properties中配置注册中心地址以及服务名
```properties
# 服务名,配置服务名是因为服务名是默认读取的dataId的一部分
spring.application.name=service-provider
# 配置中心地址
spring.cloud.nacos.config.server-addr=127.0.0.1:8848
```
当配置了nacos配置中心，服务启动时会去nacos配置中心加载默认命名空间(public)下默认命名的配置集，dataId的组成形式是 ${prefix}-${spring.profiles.active}.${file-extension}
- dataId：一组配置的集合的唯一表示
- prefix：默认值为配置文件中配置的服务名，也可以使用spring.cloud.nacos.config.prefix来自定义
- spring.profiles.active指当前激活的环境，如dev,prod等，如果当前服务不存在多个环境，则dataI
d将变为 ${prefix}.${file-extension}
- file-extension：配置中心中配置项的配置格式(默认是properties,因此只要内容格式是properties就可以不加后缀)，可在配置文件中通过spring.cloud.nacos.config.file-extension指定，当前只支持yaml和properties
4. 获取配合中心中配置项的值可使用@Value注解，若希望读取的值随着配置中心中的改变而实时更新（不用重启项目），使用@RefreshScope自动刷新
- 若某个配置项既在application.yaml有配置，也在配置中心进行了配置，优先读取配置中心中的值
5. 关于配置中心的几个名字概念
- 命名空间：用来做配置隔离，区分每个dataId所属哪个命名空间，比如可以让每个服务所创建的所有dataId属于自己的命名空间
- 配置中心中新建的dataId默认所属的命名空间都是PUBLIC，服务启动时默认去读取的dataId是服务名.properties，若自己修改了其所属命名空间，则无法读取到配置项的内容
可以在配置文件bootstrap.properties中指定要读取哪个配置空间
- 每个配置空间下所创建的配置集dataId，可以设置其所属组，默认组是DEFAULT_PUBLIC，可以通过这个组来进行生产、测试等多个环境下的多个配置，然后在配置文件中指定读取的是哪个组即可
6. 读取多个配置文件
我们可以将服务的全部配置分成多个配置文件(数据连接相关，mybatis相关，redis相关，其他)，为每部分的配置文件也都可以设置所属组，然后在配置文件中通过spring.cloud.nacos.config.extension-configs:来指定加在属于指定组的多个配置文件 
7. 通常情况下，每个模块(微服务)会去创建自己专属的命名空间(如服务名)，然后在命名空间内创建多个dataId进行相应部分的配置管理，并可以为其指定所属组来进行不同环境下的不同配置。
```java
@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigController {

    @Value("${useLocalCache:false}")
    private boolean useLocalCache;

    @RequestMapping("/get")
    public boolean get() {
        return useLocalCache;
    }
}
```
## JSR303
1)、导入 javax.validation、hibernate-validator依赖，尤其是第二个，在springboot应用中使用校验，必须导入
2）、给Bean的字段添加校验注解:javax.validation.constraints，并定义自己的message提示
3)、开启校验功能 使用@Valid
   效果：校验错误以后会有默认的响应；
4）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
5）、分组校验（多场景的复杂校验）
      1)、	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
       给校验注解标注什么情况需要进行校验
      2）、@Validated({AddGroup.class})
      3)、默认没有指定分组的字段校验使用注解@Valid，在分组校验情况下，只会在@Validated({AddGroup.class})生效；
5）、自定义校验
   1）、编写一个自定义的校验注解
   2）、编写一个自定义的校验器 ConstraintValidator
   3）、关联自定义的校验器和自定义的校验注解
      @Constraint(validatedBy = { ListValueConstraintValidator.class【可以指定多个不同的校验器，适配不同类型的校验】 })
统一的异常处理
@ControllerAdvice
   1）、编写异常处理类，使用@ControllerAdvice。
   2）、使用@ExceptionHandler标注方法可以处理的异常。
   
## 分布式锁Redisson的使用
   https://redis.io/topics/distlock
   https://github.com/redisson/redisson

   Redisson是一个在Redis的基础上实现的Java驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的Java常用对象，还提供了许多分布式服务。
   其中包括(BitSet, Set, Multimap, SortedSet, Map, List, Queue, BlockingQueue, Deque, BlockingDeque, Semaphore, Lock, AtomicLong, CountDownLatch, Publish / Subscribe, Bloom filter, Remote service, Spring cache, Executor service, Live Object service, Scheduler service)
   Redisson提供了使用Redis的最简单和最便捷的方法。Redisson的宗旨是促进使用者对Redis的关注分离（Separation of Concern），从而让使用者能够将精力更集中地放在处理业务逻辑上。
     
   1. 导入erdisson依赖，可去maven仓库
   2. 编写配置类，创建 RedissonClient对象
   3. @autowired注入RedissonClient对象
   4. 获取锁 参数就是锁的名字
            // 获取分布式可重入锁，最基本的锁
           RLock lock = redissonClient.getLock("锁名");
           // 获取读写锁
           redissonClient.getReadWriteLock("anyRWLock");
           // 信号量
           redissonClient.getSemaphore("semaphore");
  
      Rlock实现了juc下的lock，完全可以像使用本地锁一样使用它
  
   5. 以可重入锁为例
      如果直接执行 lock.lock();
         Redisson内部提供了一个监控锁的看门狗，它的作用是在Redisson实例被关闭前，(定时任务)不断的延长锁的有效期。
         默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改Config.lockWatchdogTimeout来另行指定
         也就是说，先加锁，然后执行业务，锁的默认有效期是30s，业务进行期间，会通过定时任务不断将锁的有效期续至30s。直到业务代码结束
         所以即便不手动释放锁。最终也会自动释放
         默认是任务调度的周期是 看门狗时间 / 3  = 10s
   
      也可以使用 lock.lock(10, TimeUnit.SECONDS);手动指定时间
         此时，不会有定时任务自动延期，超过这个时间后锁便自动解开了
         需要注意的是，如果代码块中有手动解锁，但是业务执行完成之前锁的有效期到了，
         此时执行unlock会报错：当前线程无法解锁
         因为现在redis中的锁是另一个线程加上的，而他的删锁逻辑是lua脚本执行
         先获取键值，判断是否是自己加的锁。如果是。则释放，lua脚本保证这是一个原子操作
         所以，手动设置时间必须保证这个时间内业务能够执行完成
         
## SpringCache的使用

https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/integration.html#cache
1. 导入依赖
    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    ```
2. CacheAutoconfiguration类导入了多种类型的缓存自动配置
```java
static class CacheConfigurationImportSelector implements ImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            CacheType[] types = CacheType.values();
            String[] imports = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                imports[i] = CacheConfigurations.getConfigurationClass(types[i]);
            }
            return imports;
        }

    }
// 缓存类型
public enum CacheType {
    GENERIC,
    JCACHE,
    EHCACHE,
    HAZELCAST,
    INFINISPAN,
    COUCHBASE,
    REDIS,
    CAFFEINE,
    SIMPLE,
    NONE;}
// 各种类型缓存对应的自动配置类
mappings.put(CacheType.GENERIC, GenericCacheConfiguration.class);
mappings.put(CacheType.EHCACHE, EhCacheCacheConfiguration.class);
mappings.put(CacheType.HAZELCAST, HazelcastCacheConfiguration.class);
mappings.put(CacheType.INFINISPAN, InfinispanCacheConfiguration.class);
mappings.put(CacheType.JCACHE, JCacheCacheConfiguration.class);
mappings.put(CacheType.COUCHBASE, CouchbaseCacheConfiguration.class);
mappings.put(CacheType.REDIS, RedisCacheConfiguration.class);
mappings.put(CacheType.CAFFEINE, CaffeineCacheConfiguration.class);
mappings.put(CacheType.SIMPLE, SimpleCacheConfiguration.class);
mappings.put(CacheType.NONE, NoOpCacheConfiguration.class);
MAPPINGS = Collections.unmodifiableMap(mappings);
```
3. 每种类型的缓存自动配置类中，创建了CacheManager，根据默认配置或用户自定义配置初始化了一系列Cache
以RedisCacheConfiguration为例
```java
    // 创建cacheManager。初始化cache
    @Bean
    RedisCacheManager cacheManager(){}
    // 用于决定初始化cache用什么配置，如果用户自定义了RedisCacheConfiguration。就用用户的配置
    // 否则就自己创建一个配置
    private determineConfiguration(){
                    CacheProperties cacheProperties,
                    ObjectProvider<org.springframework.data.redis.cache.RedisCacheConfiguration> redisCacheConfiguration,
                    ClassLoader classLoader) {
                return redisCacheConfiguration.getIfAvailable(() -> createConfiguration(cacheProperties, classLoader));
    }
    // 自己创建一个RedisCacheConfiguration
    private createConfiguration(
        CacheProperties cacheProperties, ClassLoader classLoader) {
        Redis redisProperties = cacheProperties.getRedis();
        // 这里就是默认策略的设置
        org.springframework.data.redis.cache.RedisCacheConfiguration config = org.springframework.data.redis.cache.RedisCacheConfiguration
                .defaultCacheConfig();
        // 值的序列化采用Jdk序列化
        config = config.serializeValuesWith(
                SerializationPair.fromSerializer(new JdkSerializationRedisSerializer(classLoader)));
        // 读取配置文件中用户指定的缓存有效期
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        // 读取配置文件中用户指定的缓存键的前缀
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        // 读取配置文件中用户指定的缓存是否要缓存空值，缓存控制能够解决缓存雪崩(疯狂访问一个缓存和数据库中都没有的id，导致崩溃)
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        // 读取配置文件中用户指定的缓存是否使用指定的键前缀
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
```
4. 使用缓存
```java
@Cacheable: Triggers cache population: 触发将值存入缓存的操作
@CacheEvict: Triggers cache eviction.   触发将值从缓存移除的操作
@CachePut: Updates the cache without interfering with the method execution：触发更新缓存的操作 
@Caching: Regroups multiple cache operations to be applied on a method：组合以上多种操作
@CacheConfig: Shares some common cache-related settings at class-level：在类级别上共享相同的缓存配置
```
    