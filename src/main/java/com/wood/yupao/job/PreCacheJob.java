package com.wood.yupao.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.wood.yupao.model.domain.User;
import com.wood.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    List<Long> mainUserList = Arrays.asList(1L, 2L);

    // 每天执行，预热用户
    @Scheduled(cron = "0 0 0 * * ? ")
    public void doPerCache() {
        RLock lock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock Thread: " + Thread.currentThread().getId());
                // 如果有缓存，直接读缓存
                Random random = new Random();
                HashSet<Integer> idSet = new HashSet<>();
                while (idSet.size() != 100) {
                    int number = random.nextInt(1000) + 1;
                    idSet.add(number);
                }
                List<Integer> idList = new ArrayList<>(idSet);
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.in("id", idList);
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("yupao:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    Gson gson = new Gson();
                    String json = gson.toJson(userPage);
                    try {
                        valueOperations.set(redisKey, json, 1, TimeUnit.DAYS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doPerCache error " + e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("unLock Thread: " + Thread.currentThread().getId());
            }
            else {
                System.out.println("没抢到捏 我系: " + Thread.currentThread().getId());
            }
        }
    }
}
