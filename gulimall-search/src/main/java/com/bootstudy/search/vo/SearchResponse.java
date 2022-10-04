package com.bootstudy.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/4 6:50 下午
 * @Version 1.0
 */
@Data
@ToString
public class SearchResponse {
    // 查询到的所有商品信息
    private List<SkuEsModel> products;   // 查询到的所有商品信息
    // 三个聚合结果
    private List<BrandVo> brands;        // 当前查询到的结果，所涉及到的所有【品牌】
    private List<AttrVo> attrs;          // 当前查询到的结果，所涉及到的所有【属性】
    private List<CatalogVo> catalogs;    // 当前查询到的结果，所涉及到的所有【分类】

    private Integer pageNum;  // 当前页码
    private Long total;   // 总记录
    private Integer totalPage; // 总页码
    private Set<Long> attrIds;

    // 以上是要返回的数据
    // 面包屑导航
    private List<NavVo> navs;

    @Data
    @ToString
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    @ToString
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    @ToString
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}
