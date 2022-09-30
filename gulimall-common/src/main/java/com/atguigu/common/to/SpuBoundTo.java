package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/9/30 2:46 下午
 * @Version 1.0
 */
@Data
public class SpuBoundTo {
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
    private Long spuId;
}
