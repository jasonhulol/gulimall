package com.bootstudy.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bootstudy.gulimall.product.dao.CategoryDao;
import com.bootstudy.gulimall.product.entity.CategoryEntity;
import com.bootstudy.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.先查出所有分类数据
        List<CategoryEntity> allCat = baseMapper.selectList(null);
        //2.递归设置父子结构
        List<CategoryEntity> catOne = allCat.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((cat)->{
                    cat.setChildren(getAllChildren(cat,allCat));
                    return cat;
                }).sorted((menu1,menu2)->{
                    //菜单排序
                    return (menu1.getSort()==null ? 0 : menu1.getSort() - (menu2.getSort()==null ? 0 : menu2.getSort()));
                }).collect(Collectors.toList());

        return catOne;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1.检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    public List<CategoryEntity> getAllChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //找子菜单
            categoryEntity.setChildren(getAllChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //菜单排序
            return (menu1.getSort()==null ? 0 : menu1.getSort() - (menu2.getSort()==null ? 0 : menu2.getSort()));
        }).collect(Collectors.toList());

        return children;
    }

}