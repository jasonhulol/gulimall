package com.bootstudy.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/2 3:13 下午
 * @Version 1.0
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {
    /**
     * product  --->   cloud   ---> coupon
     * ->SpuBoundsTo  ->Json      ->SpuBounds
     *
     * @param spuBoundTo
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(SkuReductionTo skuReductionTo);
}
