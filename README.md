# gulimall
分布式商城
## spring-cloud-alibaba的使用
### 引入依赖，全套组件版本统一管理
```xml
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
7. 通常情况下，每个模块(微服务)会去创建自己专属的命名空间(如服务名)，然后在命名空间内创建多个daytaId进行相应部分的配置管理，并可以为其指定所属组来进行不同环境下的不同配置。
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