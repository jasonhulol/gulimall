package com.bootstudy.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: hhd
 * @Description:  1.导入依赖
 *                2.编写配置，给容器中注入一个RestHighLevelClient
 *                3.参照API开发
 * @Date: 2022/10/1 5:11 下午
 * @Version 1.0
 */
@Configuration
public class GulimallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();;
        COMMON_OPTIONS = builder.build();
    }
    @Bean
    public RestHighLevelClient esRestClient(){
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost("192.168.56.20", 9200, "http"));
        return new RestHighLevelClient(restClientBuilder);
    }
}
