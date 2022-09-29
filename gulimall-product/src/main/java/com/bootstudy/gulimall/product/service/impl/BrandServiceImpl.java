package com.bootstudy.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.bootstudy.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.bootstudy.gulimall.product.dao.BrandDao;
import com.bootstudy.gulimall.product.entity.BrandEntity;
import com.bootstudy.gulimall.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<BrandEntity>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void updateDetailById(BrandEntity brand) {
        this.updateById(brand);
        if (!StringUtils.isEmpty(brand.getName())) {
            Long brandId = brand.getBrandId();
            String name = brand.getName();
            categoryBrandRelationService.updateDetail(brandId,name);
        }
    }
}