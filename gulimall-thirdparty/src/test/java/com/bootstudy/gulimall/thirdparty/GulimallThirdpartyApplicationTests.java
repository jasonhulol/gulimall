package com.bootstudy.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallThirdpartyApplicationTests {

    @Autowired
    OSSClient ossClient;

    @Test
    public void testUpload() throws FileNotFoundException {
//        String endpoint = "oss-cn-beijing.aliyuncs.com";
//        String accessKeyId = "LTAI5tELBEtmbzUNbXM9DseX";
//        String accessKeySecret = "uF6Bss6iXAjJ2PmqFRtqEudAP69Nbl";
//
//        //创建OSSClient实例
//        OSS ossclient = new OSSClientBuilder().build(endpoint,accessKeyId,accessKeySecret);

        //上传文件流
        InputStream inputStream = new FileInputStream("/Users/robert/Downloads/data/avatorImages/1547476912596tou.jpg");

        ossClient.putObject("arronmall-oss", "girl.jpg", inputStream);

        ossClient.shutdown();
        System.out.println("上传完成！");
    }

    @Test
    public void contextLoads() {
    }

}
