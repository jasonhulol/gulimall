package com.bootstudy.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

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
 *
 * @Date: 2022/9/25 5:02 下午
 * @Version 1.0
 */

@SpringBootApplication
@MapperScan("com.bootstudy.gulimall.product.dao")
@EnableDiscoveryClient
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
