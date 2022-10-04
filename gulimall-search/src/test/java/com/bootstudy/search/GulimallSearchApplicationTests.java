package com.bootstudy.search;

import com.alibaba.fastjson.JSON;
import com.bootstudy.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    RestHighLevelClient client;


    @Test
    public void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        //指定DSL，检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        //计算平均年龄
        searchSourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
        //计算平均薪资
        searchSourceBuilder.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"));

        System.out.println("检索条件是:{}" + searchSourceBuilder );
        searchRequest.source(searchSourceBuilder);

        //执行检索
        SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //分析结果
        System.out.println("响应结果是" + response);

        //获取所有查到的数据
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }

        //获取结果的分析数据
        Aggregations aggregations = response.getAggregations();
        Terms ageAgg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄：" + keyAsString);
        }

        Avg balanceAgg = aggregations.get("balanceAvg");
        System.out.println("工资：" + balanceAgg.getValue());
    }

    @Test
    public void testEs() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setUserName("阿巴阿巴");
        user.setAge(18);
        user.setGender("男");
        String s = JSON.toJSONString(user);
        indexRequest.source(s, XContentType.JSON);

        //执行操作
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }

    @Data
    @ToString
    static class Account{
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
