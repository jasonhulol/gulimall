package com.atguigu.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/2 12:08 下午
 * @Version 1.0
 */
@Data
public class SkuEsModel {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Boolean hasStock;
    private Long hotScore;
    private Long catalogId;
    private Long brandId; //pms_brand数据表  //pms_sku_info数据表
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attrs> attrs;

    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
