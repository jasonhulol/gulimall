package com.bootstudy.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.bootstudy.search.config.GulimallElasticSearchConfig;
import com.bootstudy.search.constant.EsConstant;
import com.bootstudy.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/2 2:03 下午
 * @Version 1.0
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean saveProduct(List<SkuEsModel> skuEsModels) throws IOException {
        //保存到es
        //1.给es创建索引，product,建立好映射关系
        //2.给es中保存数据
        //BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(EsConstant.PRODUCT_INTDEX);
            indexRequest.id(String.valueOf(skuEsModel.getSkuId()));
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        boolean b = bulkResponse.hasFailures();
        List<String> collect = Arrays.stream(bulkResponse.getItems()).map((item) -> item.getId()).collect(Collectors.toList());
        if (b) {
            log.error("商品上架错误：{}" + collect);
        }
        return b;
    }
}
