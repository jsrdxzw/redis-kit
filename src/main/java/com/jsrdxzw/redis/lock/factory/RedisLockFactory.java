package com.jsrdxzw.redis.lock.factory;

import com.jsrdxzw.redis.lock.RedisLock;

import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 */
public interface RedisLockFactory {

    /**
     * get distributed lock
     * default wait time is 60s
     *
     * @param lockKey lock key
     * @return RedisLock
     */
    default RedisLock getLock(String lockKey) {
        return getLock(lockKey, 60, TimeUnit.SECONDS);
    }

    /**
     * get distributed lock
     *
     * @param lockKey        lock key
     * @param expireTime     expireTime
     * @param expireTimeUnit expireTimeUnit
     * @return RedisLock
     */
    RedisLock getLock(String lockKey, long expireTime, TimeUnit expireTimeUnit);
}
