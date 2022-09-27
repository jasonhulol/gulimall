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
