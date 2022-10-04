package com.bootstudy.gulimall.product;

import com.bootstudy.gulimall.product.entity.BrandEntity;
import com.bootstudy.gulimall.product.service.BrandService;
import com.bootstudy.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.UUID;

/**
 * 1.引入oss-starter
 * 2.配置yml文件中的secret key等信息
 * 3.OSSClient进行操作
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimalProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Test
    public void testRedisson(){
        System.out.println(redissonClient);
    }

    @Test
    public void testStringRedisTemplate(){
        ValueOperations<String, String> forValue = stringRedisTemplate.opsForValue();
        forValue.set("hello","world_" + UUID.randomUUID().toString());
        String hello = forValue.get("hello");
        System.out.println(hello);
    }

    @Test
    public void getCatePath() {
        Long[] res = categoryService.findCatelogPath(225L);
        log.info("完整路径为{}", Arrays.asList(res));
    }

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("哈哈1哈");
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

}
