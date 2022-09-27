package com.bootstudy.gulimall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 如何使用Nacos作为配置中心
 * 1）引入依赖
 *      <!-- 配置中心做配置管理-->
 *         <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
 *         </dependency>
 * 2）创建一个bootstrap.properties
 *      spring.application.name=gulimall-coupon
 *      spring.cloud.nacos.config.server-addr=192.168.56.20:8848
 * 3）给配置中心添加一个配置项，Data-id为gulimall-coupon.properties,应用名.properties
 * 4）给应用名.properties添加任意配置
 * 5）动态获取配置
 *      @RefreshScope：动态获取配置注解
 *      @Value("${要读取的配置项}")： 获取配置值
 *      如果配置中心和当前应用的配置未见都配置了相同的项，优先使用配置中心的配置
 *
 * 2.细节
 *    1）命名空间：配置隔离;
 *          默认：public(保留空间)；默认新增的所有配置都在public空间
 *          1，开发，测试，生产：利用命名空间来做环境隔离。
 *              注意：在bootstrap.properties中配置上，需要使用哪个命名空间下的配置
 *              spring.cloud.nacos.config.namespace=57c46908-0927-4aea-ac98-cdd2a6a21a27
 *          2，每个微服务之间互相隔离陪你，每个微服务都创建自己的命名空间，只家在自己命名空间下的所有配置
 *     2）配置集：所有的配置的集合
 *     3）配置集ID：类似文件名
 *        Data ID：文件名
 *     4）配置分组：
 *        默认所有的配置集都属于：DEFAULT_GROUP;
 *        可以创建单独配置集
 *
 *     每个微服务创建自己的命名空间，使用配置分组区分环境，dev,test,prod
 *
 * 3.同时加载多个配置集
 *  1）微服务任何配置信息，任何配置文件都可以放在配置中心中
 *  2）只需要在bootstrap.properties说明加载配置中心中那些配置文件即可
 *  3）@Value，@ConfigurationProperties。。。
 *  以前springboot任何方法从配置文件中获取值，都能使用
 */

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.bootstudy.gulimall.coupon.dao")
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
