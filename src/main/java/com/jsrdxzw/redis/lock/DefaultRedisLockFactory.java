package com.jsrdxzw.redis.lock;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 * @date 2020/05/23
 */
public class DefaultRedisLockFactory implements RedisLockFactory {

    private final Map<String, RedisLock> redisLockMap = new ConcurrentHashMap<>();

    private final StringRedisTemplate stringRedisTemplate;

    public DefaultRedisLockFactory(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public RedisLock getLock(String lockKey, long expireTime, TimeUnit expireTimeUnit) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            throw new RuntimeException("lockKey can not be empty!");
        }
        RedisLock redisLock = redisLockMap.get(lockKey);
        if (redisLock == null) {
            redisLock = new DefaultRedisLock(stringRedisTemplate, lockKey, expireTime, expireTimeUnit);
            redisLockMap.putIfAbsent(lockKey, redisLock);
        }
        return redisLock;
    }
}
