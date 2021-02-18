package com.jsrdxzw.redis.lock.factory;

import com.jsrdxzw.redis.lock.RedisLock;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 */
public abstract class AbstractRedisLockFactory implements RedisLockFactory {
    protected final Map<String, RedisLock> redisLockMap = new ConcurrentHashMap<>();

    protected final StringRedisTemplate stringRedisTemplate;

    public AbstractRedisLockFactory(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public RedisLock getLock(String lockKey, long expireTime, TimeUnit expireTimeUnit) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            throw new RuntimeException("lockKey can not be empty!");
        }
        RedisLock redisLock = redisLockMap.get(lockKey);
        if (redisLock == null) {
            redisLock = createRedisLock(stringRedisTemplate, lockKey, expireTime, expireTimeUnit);
            redisLockMap.putIfAbsent(lockKey, redisLock);
        }
        return redisLock;
    }

    /**
     * create redis lock instance
     *
     * @param stringRedisTemplate redis template
     * @param lockKey             redis key
     * @param expireTime          expire time
     * @param expireTimeUnit      expire time unit
     * @return RedisLock
     */
    protected abstract RedisLock createRedisLock(StringRedisTemplate stringRedisTemplate, String lockKey, long expireTime, TimeUnit expireTimeUnit);

}
