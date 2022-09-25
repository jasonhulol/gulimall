package com.bootstudy.gulimall.product.dao;

import com.bootstudy.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author hhd
 * @email 1017193846@qq.com
 * @date 2022-09-25 16:53:49
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
