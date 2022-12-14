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
     * //TODO ??????????????????
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1?????????spu???????????? pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2?????????Spu??????????????? pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);


        //3?????????spu???????????? pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(), images);


        //4?????????spu???????????????;pms_product_attr_value
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


        //5?????????spu??????????????????gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("????????????spu??????????????????");
        }


        //5???????????????spu???????????????sku?????????
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
                //5.1??????sku??????????????????pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //??????true???????????????false????????????
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2??????sku??????????????????pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO ?????????????????????????????????

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3??????sku????????????????????????pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // //5.4??????sku??????????????????????????????gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("????????????sku??????????????????");
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
        // 1.??????spu?????????baseattr   pms_product_attr_value ???????????????spuid???baseattrid???????????????
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        // 1.1??????baseAttrId
        List<Long> attrIds = baseAttrs.stream().
                map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        // 1.2???????????? sku ???????????????????????????????????????????????????????????? pms_attr
        List<Long> finalAttrIds = attrService.selectSearchableBaseAttrs(attrIds);
        // 1.3???????????????????????????
        Set<Long> idSet = new HashSet<>(finalAttrIds);
        List<SkuEsModel.Attrs> finalAttrs = baseAttrs.stream().
                filter(item -> idSet.contains(item.getAttrId())).
                map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                }).collect(Collectors.toList());

        // 2.???????????? spuId ?????????????????????
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().
                map(SkuInfoEntity::getSkuId).
                collect(Collectors.toList());
        // 2.1 ???????????? sku ??????????????????????????????????????????
        //List<SkuSaleAttrValueEntity> saleAttrs = skuSaleAttrValueService.getSaleAttrsBatch(skuIdList);

        // 3.??????????????????
        Map<Long, Boolean> map = null;
        try {
            R wareResult = wareService.getSkusHasStock(skuIdList);
//            map = (Map<Long, Boolean>) wareResult.get("data");
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            map = wareResult.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("?????????????????????");
            e.printStackTrace();
        }

        // 4. ???????????? sku ??????
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
            // TODO ??????????????????????????? 0
            model.setHotScore(0L);
            // ????????????????????????????????????
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

        // 5. ??????????????? es
        log.info("this is debug item:" + JSON.toJSONString(collect));
        R result = searchFeignService.productUp(collect);
        if (result.getCode() == 0) {
            // ??????????????????
            baseMapper.updateSpuStatus(spuId, ProductConstant.SpuStatusEnum.SPU_UP.getCode());
        } else {
            // ??????????????????
            // TODO ????????????\???????????????\????????????
            /**
             * Feign????????????
             * 1.???????????????????????????????????????json
             *   RequestTemplate template = buildTemplateFromArgs.create(argv);
             * 2.???????????????????????????????????????????????????????????????
             *   executeAndDecode(template);
             * 3.???????????????????????????
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
            log.error("??????????????????");
            throw new RuntimeException("??????????????????");
        }
    }

}