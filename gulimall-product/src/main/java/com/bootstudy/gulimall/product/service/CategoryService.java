package com.bootstudy.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bootstudy.gulimall.product.entity.CategoryEntity;
import com.bootstudy.gulimall.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author hhd
 * @email 1017193846@qq.com
 * @date 2022-09-25 16:53:49
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    Long[] findCatelogPath(Long catalogId);

    void updateCascade(CategoryEntity category);

    Map<String, List<Catelog2Vo>> getCatelogJson() throws InterruptedException;

    List<CategoryEntity> getLevel1Categorys();
}

