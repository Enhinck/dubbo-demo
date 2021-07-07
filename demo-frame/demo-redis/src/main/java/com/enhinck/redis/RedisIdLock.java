package com.enhinck.redis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;


/**
 * <p>redis对象存储配置
 *
 * @author xiaomi（huenbin ）
 * @since 4/27/21 3:16 PM
 */
@Component
@Slf4j
public class RedisIdLock {
    public static final String MESSAGE_SUMMARY_TABLE = "MessageSummaryDO";
    @Resource
    RedissonClient redissonClient;
    private static volatile Map<String, String> requestIdMap = new ConcurrentHashMap<>();
    ThreadLocal<String> local = new ThreadLocal<>();

    public void setSessionId(String requestId) {
        local.set(requestId);
        requestIdMap.put(requestId, requestId);
    }

    public void complete(String requestId) {
        local.remove();
        requestIdMap.remove(requestId);
    }

    private static final String LOCK_LUA =
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then redis.call('expire', KEYS[1], ARGV[2]) return 'true' else return 'false' end";
    /**
     * 解锁脚本
     */
    private static final String UNLOCK_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) end return 'true' ";


    ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("RedisIdLock-thread-%d").build();


    private final ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(50000), threadFactory);

    /**
     * 获取Redisson锁
     *
     * @param objectName
     * @return
     */
    public RLock getRLock(String objectName) {
        RLock rLock = redissonClient.getLock(objectName);
        return rLock;
    }


    @Autowired
    @Qualifier("countRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    public static final int TIMEOUT = 10;

    public static final int INTERVAL = 5000;

    /**
     * 获取锁
     * redis 表id程序锁
     *
     * @param tableName
     * @param id
     * @return 获得锁成功与否
     */
    public boolean lock(String tableName, final String id) {
        String lockKey = getKey(tableName, id);

        boolean flag;
        try {
            RedisScript<String> redisScript = new DefaultRedisScript<>(LOCK_LUA, String.class);
            Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockKey, String.valueOf(TIMEOUT));
            flag = "true".equals(result);
        } catch (Exception e) {
            log.error("获取锁异常， lockKey={}, value={},e:{}", lockKey, id, e);
            flag = false;
        }

        if (flag) {
            log.debug("lock success {}", lockKey);
            String requestId = local.get();
            requestIdMap.put(requestId, lockKey);
            executorService.execute(new RenewThread(redisTemplate, lockKey, requestId));
        }

        return flag;
    }

    public static class RenewThread extends Thread {
        private RedisTemplate<String, String> redisTemplate;
        private String lockKey;
        private String requestId;

        public RenewThread(RedisTemplate<String, String> redisTemplate, String lockKey, String requestId) {
            this.redisTemplate = redisTemplate;
            this.lockKey = lockKey;
            this.requestId = requestId;
            this.setDaemon(true);
        }

        public void delay() {
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void run() {
            delay();
            while (requestIdMap.containsKey(requestId)) {
                // 主线程存活，续期锁的时间
                redisTemplate.opsForValue().set(lockKey, lockKey, TIMEOUT, TimeUnit.SECONDS);
                log.debug("Renew lock {}", lockKey);
                delay();
            }
            // 主线程完成删除锁
            releaseLock(lockKey);
            requestIdMap.remove(requestId);
            log.debug("release lock {}", lockKey);
        }

        public boolean releaseLock(String lockKey) {
            RedisScript<String> redisScript = new DefaultRedisScript<>(UNLOCK_LUA, String.class);
            Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockKey);
            return "true".equals(result);
        }
    }

    /**
     * 释放锁
     * redis 表id程序锁
     *
     * @return 释放锁成功与否
     */
    public boolean releaseLock() {
        String requestId = local.get();
        if (StringUtils.isNotBlank(requestId)) {
            String lockKey = requestIdMap.get(requestId);
            if (StringUtils.isNotBlank(lockKey)) {
                RedisScript<String> redisScript = new DefaultRedisScript<>(UNLOCK_LUA, String.class);
                Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockKey);
                requestIdMap.remove(requestId);
                return "true".equals(result);
            }
        }

        return false;
    }


    private String getKey(String biz, String key) {
        return new StringBuilder(biz).append(key).toString();
    }

}