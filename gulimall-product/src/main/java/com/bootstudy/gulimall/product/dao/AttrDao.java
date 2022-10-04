package com.bootstudy.gulimall.product.dao;

import com.bootstudy.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author hhd
 * @email 1017193846@qq.com
 * @date 2022-09-25 16:53:49
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearchableBaseAttrs(@Param("attrs") List<Long> attrs);
}
