package com.bootstudy.gulimall.product.feign;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/2 2:30 下午
 * @Version 1.0
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R productUp(@RequestBody List<SkuEsModel> skuEsModels);
}
