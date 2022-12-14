package com.bootstudy.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.bootstudy.gulimall.product.dao.BrandDao;
import com.bootstudy.gulimall.product.dao.CategoryDao;
import com.bootstudy.gulimall.product.entity.BrandEntity;
import com.bootstudy.gulimall.product.entity.CategoryEntity;
import com.bootstudy.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.bootstudy.gulimall.product.dao.CategoryBrandRelationDao;
import com.bootstudy.gulimall.product.entity.CategoryBrandRelationEntity;
import com.bootstudy.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    CategoryBrandRelationDao brandRelationDao;
    @Autowired
    BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brand = brandDao.selectById(brandId);
        CategoryEntity category = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brand.getName());
        categoryBrandRelation.setCatelogName(category.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateDetail(Long brandId, String brandName) {
        CategoryBrandRelationEntity categoryBrandRelation = new CategoryBrandRelationEntity();
        categoryBrandRelation.setBrandId(brandId);
        categoryBrandRelation.setBrandName(brandName);
        QueryWrapper<CategoryBrandRelationEntity> queryWrapper = new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId);
        this.update(categoryBrandRelation, queryWrapper);
    }

    @Override
    public void updateCatDetail(Long catId, String catName) {
        this.baseMapper.updateCategory(catId,catName);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> entities = brandRelationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        if (Objects.nonNull(entities) && entities.size() > 0) {
            List<BrandEntity> brands = entities.stream().map((item) -> {
                Long id = item.getBrandId();
                BrandEntity byId = brandService.getById(id);
                return byId;
            }).collect(Collectors.toList());
            return brands;
        }
        return null;
    }
}