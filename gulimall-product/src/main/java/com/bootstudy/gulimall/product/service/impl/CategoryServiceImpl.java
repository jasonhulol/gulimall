package com.bootstudy.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.bootstudy.gulimall.product.service.CategoryBrandRelationService;
import com.bootstudy.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bootstudy.gulimall.product.dao.CategoryDao;
import com.bootstudy.gulimall.product.entity.CategoryEntity;
import com.bootstudy.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

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

    @Override
    public Long[] findCatelogPath(Long catalogId) {
        List<Long> paths = new ArrayList<>();
        this.findCatelogPath(catalogId,paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    /**
     *  @CacheEvict 失效模式
     *  1.同时进行多种缓存操作 @Caching
     *  2.指定删除某个分区下的所有数据@CacheEvict(value = "category", allEntries = true)
     *  3.存储统一类型的数据，都可以指定同一个分区，分区名默认就是缓存的前缀
     * @param category
     */

//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatelogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true) //失效模式
    //@CachePut  双写模式
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCatDetail(category.getCatId(), category.getName());
        }
    }

    private void findCatelogPath(Long catalogPath,List<Long> paths) {
        paths.add(catalogPath);
        CategoryEntity category = this.getById(catalogPath);
        if (category.getParentCid() != 0) {
            this.findCatelogPath(category.getParentCid(), paths);
        }
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

    @Cacheable(value = "category", key = "#root.method.name")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson(){
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
        //1、查出所有分类
        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        //封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parentCid;
    }

    /**
     * 产生堆外内存异常 outofdirectmemoryError
     * 1.springboot2.0以后默认使用lettuce作为操作redis的客户端，它默认使用netty进行网络通信
     * 2。lettuce的bug导致netty堆外内存溢出 调大-Xmx300m；netty如果没有指定堆外内存，默认使用-Xmx300m
     * 可通过 -Dio.netty.maxDirectMemory进行设置
     * 解决方案：不能只使用-Dio.netty.maxDirectMemory去调大堆外内存，只能延缓，不能解决问题
     * 1）升级lettuce客户端  2）切换使用jedis
     * lettuce和jedis都是操作redis的客户端，spring对他们再次封装称redisTemplate.
     * @return
     */
    //暂时注解用于测试cache注解
//    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson2() throws InterruptedException {
        //给缓存中发给你json字符串，拿出的json字符串，还应逆转为能用的对象类型；【序列化与反序列化】
        /**
         * 1.空结果缓存：解决缓存穿透
         * 2.设置过期时间（加随机值）：解决缓存雪崩
         * 3.加锁：解决缓存击穿
         */

        //1.加入缓存逻辑，缓存中存的数据是json字符串
        //JSON跨语言，跨平台兼容
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            //缓存中没有，查数据库
            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDbWithRedisLock();
            return catelogJsonFromDb;
        }
        System.out.println("查询了数据库。。。");
        Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJSON,new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return stringListMap;
    }

    /**
     * 1.每一个需要缓存的数据我都要来指定要放到哪个名字的缓存
     * 2.@Cacheable({"category"})
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 3.默认行为
     *      1）如果缓存中有，方法不用调用
     *      2）key默认自动生成，缓存的名字：SimpleKey[](自主生成的key值)
     *      3）默认的value的值，默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4）默认的ttl时间 -1；永不过期
     *    自定义
     *    1）指定生成的缓存使用的key：key属性指定，接受一个SpEL
     *    2）指定缓存的数据的存货时间：配置文件中修改ttl
     *    3) 将数据保存为json格式
     * 4.Spring-Cache的不足
     *  1）读模式：
     *      缓存穿透：查询一个null数据。解决：缓存空数据；cache-null-value = true
     *      缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁；而spring-cache默认是不加锁的，可加sync=true参数进行加锁
     *      缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间：spring.cache.redis.time-to-live = ...
     *  2) 写模式：（缓存与数据库一致）
     *      （1）读写加锁
     *      （2）引入Canal，感知到mysql的数据更新去更新redis
     *      （3）读多写多，直接去数据库查询就行
     *  总结：
     *      常规数据（读多写少，及时性，一致性要求不高的数据）：完全可以使用Spring-Cache；写模式（只要缓存的数据有过期时间就足够了）
     *      特殊数据：特殊设计
     *  原理：
     *      CacheManager(RedisCacheManager) -> Cache(RedisCache) -> Cache负责缓存的读写
     *
     * @return
     */

    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity>  getLevel1Categorys() {
        System.out.println("getLevel1Categorys....");
        List<CategoryEntity> parent_cid = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return parent_cid;
    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLock() throws InterruptedException {
        //占分布式锁。去redis占锁
        //设置过期时间必须是原子的，原子加锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功。。。");
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                //加锁成功
                 dataFromDb = getDataFromDb();
            }finally {
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                //原子删锁
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
            //获取值对比+对比成功删锁=原子操作，   lua脚本解锁
//            String lockVal = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockVal)) {
//                stringRedisTemplate.delete("lock");
//            }

        } else {
            //加锁失败,重试
            //休眠100ms重试
            System.out.println("获取分布式锁失败，等待重试。。。");
            Thread.sleep(200);
            return getCatelogJsonFromDbWithRedisLock(); //自旋方式等待锁
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //得到锁以后，应该再去缓存中确定一下，如果没有再去数据库中查询
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJSON,new TypeReference<Map<String, List<Catelog2Vo>>>(){});
            return stringListMap;
        }
        System.out.println("查询了数据库");
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
        //1、查出所有分类
        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        //封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        //查到的数据再放入缓存中，将对象转为json放在缓存中
        String s = JSON.toJSONString(parentCid);
        stringRedisTemplate.opsForValue().set("catalogJSON",  s);
        return parentCid;
    }

    //查询数据库
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithLocalLock(){
        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //synchronized (this)---本地锁
        synchronized (this) {
            return getDataFromDb();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        // return this.baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {

        //1、收集当前节点id
        paths.add(catelogId);

        //根据当前分类id查询信息
        CategoryEntity byId = this.getById(catelogId);
        //如果当前不是父分类
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }

        return paths;
    }

}