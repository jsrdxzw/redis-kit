package com.jsrdxzw.redis.lock;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 */
public final class PreloadRedisLock extends AbstractRedisLock {
    private static final DefaultRedisScript<Boolean> OBTAIN_REDIS_SCRIPT;
    private static final DefaultRedisScript<Void> REMOVE_REDIS_SCRIPT;

    private final String clientId = UUID.randomUUID().toString();
    private final StringRedisTemplate stringRedisTemplate;
    private final String lockKey;
    private final long expireTime;
    private final TimeUnit expireTimeUnit;

    static {
        OBTAIN_REDIS_SCRIPT = new DefaultRedisScript<>();
        OBTAIN_REDIS_SCRIPT.setLocation(new ClassPathResource("obtain_lock.lua"));
        REMOVE_REDIS_SCRIPT = new DefaultRedisScript<>();
        REMOVE_REDIS_SCRIPT.setLocation(new ClassPathResource("remove_lock.lua"));
    }

    public PreloadRedisLock(StringRedisTemplate stringRedisTemplate, String lockKey, long expireTime, TimeUnit expireTimeUnit) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockKey = lockKey;
        this.expireTime = expireTime;
        this.expireTimeUnit = expireTimeUnit;
    }

    @Override
    protected boolean obtainLockFromRedis() {
        Boolean success = stringRedisTemplate.execute(
                OBTAIN_REDIS_SCRIPT,
                Collections.singletonList(lockKey),
                clientId,
                String.valueOf(expireTimeUnit.toMillis(expireTime))
        );
        return Boolean.TRUE.equals(success);
    }

    @Override
    protected void removeLockFromRedis() {
        stringRedisTemplate.execute(
                REMOVE_REDIS_SCRIPT,
                Collections.singletonList(lockKey),
                clientId
        );
    }
}
