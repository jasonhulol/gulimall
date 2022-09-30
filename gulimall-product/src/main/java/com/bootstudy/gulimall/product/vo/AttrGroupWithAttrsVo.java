package com.bootstudy.gulimall.product.vo;

import com.bootstudy.gulimall.product.entity.AttrEntity;
import com.bootstudy.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/9/30 10:40 上午
 * @Version 1.0
 */
@Data
public class AttrGroupWithAttrsVo extends AttrGroupEntity {
    private List<AttrEntity> attrs;
}
