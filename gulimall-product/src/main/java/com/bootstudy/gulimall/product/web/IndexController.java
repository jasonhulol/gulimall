package com.bootstudy.gulimall.product.web;

import com.bootstudy.gulimall.product.entity.CategoryEntity;
import com.bootstudy.gulimall.product.service.CategoryService;
import com.bootstudy.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/10/3 11:01 上午
 * @Version 1.0
 */
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/","index.html"})
    public String getCatelogJson(Model model) {
        System.out.println(""+Thread.currentThread().getId());
        List<CategoryEntity> level1Categorys = categoryService.getLevel1Categorys();
        //视图解析器进行拼串：
        //classpath:/templates/ + 返回值+ .html
        model.addAttribute("categorys",level1Categorys);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() throws InterruptedException {
        return categoryService.getCatelogJson();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        //1.获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");
        //2。加锁
        //lock.lock(); //阻塞式等待
        //1)锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删掉
        //2）加锁的业务只要运行完成，就不会给当前锁续期，及时不手动解锁，锁默认在30s后自动删除

        lock.lock(10, TimeUnit.SECONDS); //10秒自动解锁，自动解锁时间一定要大于业务的执行时间
        //问题：lock.lock(10, TimeUnit.SECONDS);在锁时间到了以后，不会自动续期。
        //1.如果我们传了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        //2。如果我们未指定锁的超时时间，就是用30*1000【LockWatchdogTimeout看门狗的默认时间】；
        //      只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】，每隔10秒就会自动续期为30s
        //      internalLockLeaseTime[看门狗时间] / 3  -> 10s

        //最佳实战
        // lock.lock(10, TimeUnit.SECONDS);省掉整个续期操作。手动解锁
        try{
            System.out.println("加锁成功，执行业务。。。" + Thread.currentThread().getId());
            Thread.sleep(30000);

        } catch (Exception e) {

        }finally {
            //3.解锁
            System.out.println("释放锁。。。" + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        try {
            s = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    //保证一定能读到最新数据，修改期间，写锁是一个拍他锁（互斥锁），读锁是一个共享锁
    //写锁没释放就必须等待
    //读+读：相当于无所，并发读，只会在redis中记录好，所有当前的读锁。他们都会同时加锁成功
    //写+读：等待写锁释放
    //写+写：阻塞方式
    //读+写：写也需要等待读锁释放
    //只要有写的存在，都必须等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = readWriteLock.writeLock();
        try {
            //改数据加写锁，读数据加读锁
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            stringRedisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    /**
     * 车库停车
     * 共有3个车位
     * 还可用作分布式限流
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
//        park.acquire();  //获取一个信号量，占一个车位，如果获取不到，会阻塞等待，直到有空余信号量被释放
        boolean b = park.tryAcquire();  //尝试去获取一个信号量，如果获取不到会直接返回false，不会阻塞等待
        if(b){
            //执行业务
        } else {
            return "error";
        }
        return "ok=>" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();  //释放一个车位
        return "ok";
    }

    /**
     * 放假，锁门
     * 比如5个班，必须所有班的人走完才能锁门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();  //等待闭锁都完成

        return "放假了。。。";
    }

    @GetMapping("/gogo/{id}")
    @ResponseBody
    public String gogo(@PathVariable("id") Long id) throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();

        return id+ "班的人走了";
    }
}
