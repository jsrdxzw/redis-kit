package com.jsrdxzw.redis.lock.factory;

import com.jsrdxzw.redis.lock.DefaultRedisLock;
import com.jsrdxzw.redis.lock.RedisLock;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 */
public class DefaultRedisLockFactory extends AbstractRedisLockFactory {

    public DefaultRedisLockFactory(StringRedisTemplate stringRedisTemplate) {
        super(stringRedisTemplate);
    }

    @Override
    protected RedisLock createRedisLock(StringRedisTemplate stringRedisTemplate, String lockKey, long expireTime, TimeUnit expireTimeUnit) {
        return new DefaultRedisLock(stringRedisTemplate, lockKey, expireTime, expireTimeUnit, CLIENT_ID);
    }
}
