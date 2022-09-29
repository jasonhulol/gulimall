package com.bootstudy.gulimall.product.vo;

import lombok.Data;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/9/29 3:47 下午
 * @Version 1.0
 */
@Data
public class AttrRespVo extends AttrVo {
    //"/手机/数码/手机"，所属分类名字
    private String catelogName;

    //"主体"， 所属分组名字
    private String groupName;

    //分类完整路径
    private Long[] catelogPath;
}
