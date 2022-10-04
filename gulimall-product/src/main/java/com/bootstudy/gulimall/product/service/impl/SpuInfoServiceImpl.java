package com.bootstudy.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.bootstudy.gulimall.product.entity.*;
import com.bootstudy.gulimall.product.feign.CouponFeignService;
import com.bootstudy.gulimall.product.feign.SearchFeignService;
import com.bootstudy.gulimall.product.feign.WareFeignService;
import com.bootstudy.gulimall.product.service.*;
import com.bootstudy.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.bootstudy.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService imagesService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService attrValueService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    WareFeignService wareService;
    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * //TODO 高级部分完善
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);


        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(), images);


        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);


        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }


        //5、保存当前spu对应的所有sku信息；
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }
        /**
         * status: 2
         * key:
         * brandId: 9
         * catelogId: 225
         */

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void up(Long spuId) {
        // 1.查询spu的所有baseattr   pms_product_attr_value （此表保存spuid与baseattrid关联关系）
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        // 1.1抽取baseAttrId
        List<Long> attrIds = baseAttrs.stream().
                map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        // 1.2查询当前 sku 的所有可被检索的【基本属性】（规格参数） pms_attr
        List<Long> finalAttrIds = attrService.selectSearchableBaseAttrs(attrIds);
        // 1.3过滤掉不可被检索的
        Set<Long> idSet = new HashSet<>(finalAttrIds);
        List<SkuEsModel.Attrs> finalAttrs = baseAttrs.stream().
                filter(item -> idSet.contains(item.getAttrId())).
                map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                }).collect(Collectors.toList());

        // 2.查出当前 spuId 对应的商品信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().
                map(SkuInfoEntity::getSkuId).
                collect(Collectors.toList());
        // 2.1 查询当前 sku 的所有可被检索的【销售属性】
        //List<SkuSaleAttrValueEntity> saleAttrs = skuSaleAttrValueService.getSaleAttrsBatch(skuIdList);

        // 3.查询库存信息
        Map<Long, Boolean> map = null;
        try {
            R wareResult = wareService.getSkusHasStock(skuIdList);
//            map = (Map<Long, Boolean>) wareResult.get("data");
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            map = wareResult.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("库存查询异常！");
            e.printStackTrace();
        }

        // 4. 封装每个 sku 信息
        Map<Long, Boolean> finalMap = map;
        List<SkuEsModel> collect = skus.stream().map(sku -> {
            SkuEsModel model = new SkuEsModel();
            BeanUtils.copyProperties(sku, model);
            // skuPrice, skuImg, hotScore
            // brandName brandImg catalogName attrs
            model.setSkuPrice(sku.getPrice());
            model.setSkuImg(sku.getSkuDefaultImg());
            //assert finalMap != null;
            if(finalMap == null) {
                model.setHasStock(true);
            } else {
                model.setHasStock(finalMap.get(sku.getSkuId()));
            }
            // TODO 热度评分，暂时默认 0
            model.setHotScore(0L);
            // 查出品牌和分类的名称信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            model.setBrandName(brandEntity.getName());
            model.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(model.getCatalogId());
            model.setCatalogName(categoryEntity.getName());

//            List<SkuEsModel.Attrs> finalAttr1 = new ArrayList<>(finalAttrs);
//            for (SkuSaleAttrValueEntity saleAttr : saleAttrs) {
//                if (Objects.equals(saleAttr.getSkuId(), sku.getSkuId())) {
//                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
//                    BeanUtils.copyProperties(saleAttr, attrs);
//                    finalAttr1.add(attrs);
//                }
//            }
            model.setAttrs(finalAttrs);
            return model;
        }).collect(Collectors.toList());

        // 5. 发送数据给 es
        log.info("this is debug item:" + JSON.toJSONString(collect));
        R result = searchFeignService.productUp(collect);
        if (result.getCode() == 0) {
            // 远程调用成功
            baseMapper.updateSpuStatus(spuId, ProductConstant.SpuStatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO 重复调用\接口幂等性\重试机制
            /**
             * Feign调用流程
             * 1.构造请求数据，将对象转换为json
             *   RequestTemplate template = buildTemplateFromArgs.create(argv);
             * 2.发送请求进行执行（执行成功会解码响应数据）
             *   executeAndDecode(template);
             * 3.执行请求有重试机制
             *   while(true){
             *      try{
             *          executeAndDecode(template);
             *      }catch(){
             *          try {
             *              retryer.continueOrPropagate(e);
             *          }catch(){
             *              throw ex;
             *          }
             *      }
             *  }
             */
            log.error("远程调用失败");
            throw new RuntimeException("远程调用失败");
        }
    }

}