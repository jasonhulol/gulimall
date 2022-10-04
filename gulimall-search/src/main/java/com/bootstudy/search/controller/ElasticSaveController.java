package com.bootstudy.search.controller;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.bootstudy.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/2 1:59 下午
 * @Version 1.0
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean res = false;
        try {
            res = productSaveService.saveProduct(skuEsModels);
        }catch (Exception e) {
            log.error("ElasticSaveController商品上架错误:{}", e);
            R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (!res) {
            return R.ok();
        }
        return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
    }
}
