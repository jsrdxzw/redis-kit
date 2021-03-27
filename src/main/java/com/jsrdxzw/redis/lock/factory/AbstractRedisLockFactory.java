package com.jsrdxzw.redis.lock.factory;

import com.jsrdxzw.redis.lock.RedisLock;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 */
public abstract class AbstractRedisLockFactory implements RedisLockFactory {
    protected static final String CLIENT_ID = UUID.randomUUID().toString();
    protected static final Map<String, RedisLock> REDIS_LOCK_MAP = new ConcurrentHashMap<>();

    protected final StringRedisTemplate stringRedisTemplate;

    public AbstractRedisLockFactory(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public RedisLock getLock(String lockKey, long expireTime, TimeUnit expireTimeUnit) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            throw new RuntimeException("lockKey can not be empty!");
        }
        return REDIS_LOCK_MAP.computeIfAbsent(lockKey, (key) -> createRedisLock(stringRedisTemplate, key, expireTime, expireTimeUnit));
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
