package com.bootstudy.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author: jiegege
 * @Description:
 * 整合mybatis plus
 * 1.<dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.3.2</version>
 *         </dependency>
 * 2.配置
 *  （1）配置数据源
 *      1）导入数据库驱动
 *      2）在application.yml配置数据源相关信息
 *  （2）配置MyBatis-Plus
 *      1) 使用@MapperScan
 *      2) 告诉MyBatis-Plus，sql映射文件为位置
 *
 * 二.逻辑删除功能
 * 给Bean加上逻辑删除注解@TableLogic
 *
 * 三.JSR303（数据校验）
 *  1.给Bean加校验注解,并增加自己定义的错误信息
 *  2.开启校验攻呢个@Valid
 *      效果：校验错误以后会有默认的响应
 *  3.给检验的bean后增加一个BindingResult，就可以获取到被封装的校验信息
 *  4.分组校验(多场景的复杂校验 )
 *      1）@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
 *      给校验注解标注什么情况需要进行校验
 *      2）@Validated({AddGroup.class})
 *      3) 默认没有指定分组的校验注解@NotBlank,在分组校验情况下不生效，只会在@Validated生效。
 *  5.自定义校验器
 *      1) 编写一个自定义的检验注解
 *      2）编写一个自定义的校验器
 *      3）关联自定义的校验器和自定义的校验注解
 *      @Documented
 *      @Constraint(
 *         validatedBy = {ListValueConstrainValidator.class}[此处可以添加多个自定义的检验器]
 *      )
 *      @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
 *      @Retention(RetentionPolicy.RUNTIME)
 * 四.统一的异常处理类
 * @ControllerAdvice
 *  1.编写异常处理类，使用@ControllerAdvice
 *  2.使用@ExceptionHandler标注方法可以处理的异常
 *五.模板引擎
 *  1.thymeleaf-starter:关闭缓存
 *  2.静态资源都放在static文件夹下就可以按照路径直接访问
 *  3.页面放在templates下，直接访问
 *      springboot，访问项目的时候，默认会找index
 *  4.页面修改不重启服务器实时更新
 *      1）引入dev-tools
 *      2）修改完页面后control shift f9重新自动编译下页面，代码配置，推荐重启
 *六.整合Redis
 *  1.引入data-redis-starter
 *  2.简单配置redis的host等信息
 *  3.使用springboot自动配置好的StringRedisTemplate来操作redis
 *  redis->map;
 *七.整合redisson作为分布式锁等功能框架
 *  1.引入依赖
 *      <dependency>
 *             <groupId>org.redisson</groupId>
 *             <artifactId>redisson</artifactId>
 *             <version>3.12.0</version>
 *         </dependency>
 *  2.配置redisson
 *      MyRedissonConfig给容器中配置一个RedissonClient实例即可
 *  3.使用
 *      参照文档做
 *八.整合SpringCache简化缓存开发
 *  1.引入依赖 spring-boot-starter-data-redis spring-boot-starter-cache
 *  2.写配置
 *      1）自动配置了那些
 *          CacheAutoConfiguration会导入RedisCacheConfiguration
 *          自动配好了缓存管理器RedisCacheManager
 *      2) 配置使用redis作为缓存
 *
 *  3.测试使用缓存
 *      @cacheable:  触发将数据保存到缓存的操作
 *      @CacheEvict:  触发将数据从缓存删除的操作
 *      @CachePut    不影响方法执行更新缓存
 *      @Caching:    组合以上多个操作
 *      @CacheConfig: 在类级别共享缓存的相同配置
 *
 *      使用：
 *       启功类加注解 @EnableCaching
 *       application.properties设置缓存类型为redis
 *       具体方法加注解，并指定缓存名 @Cacheable({"category"})
 *  4.原理
 *      CacheAutoConfiguration -> RedisCacheConfiguration ->
 *      自动配置了RedisCacheManager -> 初始化所有的缓存 -> 每个缓存决定使用什么配置
 *      -> 如果redisCacheConfiguration有就用已有的，没有就用默认配置
 *      -> 想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration即可
 *      -> 就会应用到当前RedisCacheManager管理的所有缓存分区中
 *
 *
 * @Date: 2022/9/25 5:02 下午
 * @Version 1.0
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.bootstudy.gulimall.product.feign")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
