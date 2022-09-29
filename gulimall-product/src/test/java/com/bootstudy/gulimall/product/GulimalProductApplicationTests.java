package com.bootstudy.gulimall.product;

import com.bootstudy.gulimall.product.entity.BrandEntity;
import com.bootstudy.gulimall.product.service.BrandService;
import com.bootstudy.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

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
