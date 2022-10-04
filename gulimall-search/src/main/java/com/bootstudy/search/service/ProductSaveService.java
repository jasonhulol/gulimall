package com.bootstudy.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/2 2:03 下午
 * @Version 1.0
 */
public interface ProductSaveService {
    boolean saveProduct(List<SkuEsModel> skuEsModels) throws IOException;
}
