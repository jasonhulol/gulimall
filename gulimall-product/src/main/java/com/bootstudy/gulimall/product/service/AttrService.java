package com.bootstudy.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bootstudy.gulimall.product.entity.AttrEntity;
import com.bootstudy.gulimall.product.vo.AttrGroupRelationVo;
import com.bootstudy.gulimall.product.vo.AttrRespVo;
import com.bootstudy.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author hhd
 * @email 1017193846@qq.com
 * @date 2022-09-25 16:53:49
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String,Object> params, Long catelogId,String type);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity>  getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Long attrgroupId,Map<String, Object> params);

    List<Long> selectSearchableBaseAttrs(List<Long> attrs);
}

