# gulimall
分布式商城
## spring-alibaba
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
3. 在配置文件中配置注册中心地址以及服务名
```properties
# 服务名
spring.application.name=service-provider
# 配置中心地址
spring.cloud.nacos.config.server-addr=127.0.0.1:8848
```
在nacos配置中心，dataId的组成形式是 ${prefix}-${spring.profiles.active}.${file-extension}
- dataId：一组配置的集合的唯一表示，服务启动后默认读取
- prefix：默认值为配置文件中配置的服务名，也可以使用spring.cloud.nacos.config.prefix来自定义
- spring.profiles.active指当前激活的环境，如dev,prod等，如果当前服务不存在多个环境，则dataI
d将变为 ${prefix}.${file-extension}
- file-extension：配置中心中配置项的配置格式，可在配置文件中通过spring.cloud.nacos.config.file-extension指定，当前只支持yaml和properties
4. 获取配合中心中配置项的值可使用@Value注解，若希望读取的值随着配置中心中的改变而实时更新（不用重启项目），使用@RefreshScope自动刷新
- 若某个配置项既在spplication.yaml有配置，也在配置中心进行了配置，优先读取配置中心中的值
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