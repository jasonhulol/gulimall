package com.bootstudy.gulimall.product;

import com.bootstudy.gulimall.product.entity.BrandEntity;
import com.bootstudy.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 1.引入oss-starter
 * 2.配置yml文件中的secret key等信息
 * 3.OSSClient进行操作
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimalProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("哈哈1哈");
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

}
