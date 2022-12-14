package com.bootstudy.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.utils.Constant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.bootstudy.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.bootstudy.gulimall.product.dao.AttrGroupDao;
import com.bootstudy.gulimall.product.dao.CategoryDao;
import com.bootstudy.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.bootstudy.gulimall.product.entity.AttrGroupEntity;
import com.bootstudy.gulimall.product.entity.CategoryEntity;
import com.bootstudy.gulimall.product.service.CategoryService;
import com.bootstudy.gulimall.product.vo.AttrGroupRelationVo;
import com.bootstudy.gulimall.product.vo.AttrRespVo;
import com.bootstudy.gulimall.product.vo.AttrVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.bootstudy.gulimall.product.dao.AttrDao;
import com.bootstudy.gulimall.product.entity.AttrEntity;
import com.bootstudy.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    CategoryService categoryService;
    @Autowired
    AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.save(attrEntity);
        //??????????????????
        if(attr.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && Objects.nonNull(attr.getAttrGroupId())) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId,String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper)->{
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //??????????????????????????????
            if("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId())
                );
                if (Objects.nonNull(attrAttrgroupRelationEntity) && Objects.nonNull(attrAttrgroupRelationEntity.getAttrGroupId())) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (Objects.nonNull(categoryEntity)) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity,attrRespVo);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relation = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            //??????????????????
            if (Objects.nonNull(relation)) {
                attrRespVo.setAttrGroupId(relation.getAttrGroupId());
                AttrGroupEntity attrGroup = attrGroupDao.selectById(relation.getAttrGroupId());
                if (Objects.nonNull(attrGroup)) {
                    attrRespVo.setGroupName(attrGroup.getAttrGroupName());
                }
            }
        }
        //??????????????????
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrRespVo.setCatelogPath(catelogPath);
        CategoryEntity category = categoryDao.selectById(catelogId);
        if (Objects.nonNull(category)) {
            attrRespVo.setCatelogName(category.getName());
        }
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //??????????????????
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attr.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                //??????????????????
                attrAttrgroupRelationDao.update(relationEntity, new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()));
            } else {
                //????????????
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    /**
     * ????????????id?????????????????????????????????
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> collect = relations.stream().map((attr) -> attr.getAttrId()).collect(Collectors.toList());
        if (Objects.isNull(collect) || collect.size()==0) {
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(collect);
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((object) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(object, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBachRelation(entities);

    }

    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        //1.?????????????????????id?????????????????????ID
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long cateId = attrGroupEntity.getCatelogId();
        //2.???????????????id?????????????????????
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cateId);
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(queryWrapper);
        List<Long> groupIds = attrGroupEntities.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //3.?????????????????????????????????id??????????????????id
        List<AttrAttrgroupRelationEntity> attr_group_id = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        List<Long> attrIds = attr_group_id.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //3.??????????????????????????????????????????????????????
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", cateId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (Objects.nonNull(attrIds) && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
//
        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;

    }

    @Override
    public List<Long> selectSearchableBaseAttrs(List<Long> attrs) {
        return attrDao.selectSearchableBaseAttrs(attrs);
    }

    /**
     * ?????????????????????????????????????????????
     * @param params
     * @param attrgroupId
     * @return
     */
//    @Override
//    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
//        //1?????????????????????????????????????????????????????????????????????
//        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
//        Long catelogId = attrGroupEntity.getCatelogId();
//        //2????????????????????????????????????????????????????????????
//        //2.1)?????????????????????????????????
//        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
//        List<Long> collect = group.stream().map(item -> {
//            return item.getAttrGroupId();
//        }).collect(Collectors.toList());
//
//        //2.2)??????????????????????????????
//        List<AttrAttrgroupRelationEntity> groupId = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
//        List<Long> attrIds = groupId.stream().map(item -> {
//            return item.getAttrId();
//        }).collect(Collectors.toList());
//
//        //2.3)?????????????????????????????????????????????????????????
//        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
//        if(attrIds!=null && attrIds.size()>0){
//            wrapper.notIn("attr_id", attrIds);
//        }
//        String key = (String) params.get("key");
//        if(!StringUtils.isEmpty(key)){
//            wrapper.and((w)->{
//                w.eq("attr_id",key).or().like("attr_name",key);
//            });
//        }
//        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
//
//        PageUtils pageUtils = new PageUtils(page);
//
//        return pageUtils;
//    }
}