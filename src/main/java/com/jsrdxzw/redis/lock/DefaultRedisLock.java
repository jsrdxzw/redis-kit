package com.jsrdxzw.redis.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xuzhiwei
 */
public final class DefaultRedisLock implements RedisLock {

    private final ReentrantLock lock = new ReentrantLock();
    private final RedisScript<Boolean> obtainRedisScript;
    private final RedisScript<Void> removeRedisScript;
    private final String clientId = UUID.randomUUID().toString();
    private final StringRedisTemplate stringRedisTemplate;
    private final String lockKey;
    private final long expireTime;
    private final TimeUnit expireTimeUnit;

    /**
     * obtain lock script
     */
    private static final String OBTAIN_LOCK = "local lockClientId = redis.call('GET', KEYS[1])\n" +
            "if lockClientId == ARGV[1] then\n" +
            "    redis.call('PEXPIRE', KEYS[1], ARGV[2])\n" +
            "    return true\n" +
            "elseif not lockClientId then\n" +
            "    redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])\n" +
            "    return true\n" +
            "else\n" +
            "    return false\n" +
            "end";

    /**
     * remove key script
     */
    private static final String REMOVE_LOCK = "local lockClientId = redis.call('GET', KEYS[1])\n" +
            "if lockClientId == ARGV[1] then\n" +
            "    redis.call('DEL', KEYS[1])\n" +
            "end";

    public DefaultRedisLock(StringRedisTemplate stringRedisTemplate, String lockKey, long expireTime, TimeUnit expireTimeUnit) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockKey = lockKey;
        this.expireTime = expireTime;
        this.expireTimeUnit = expireTimeUnit;
        this.obtainRedisScript = new DefaultRedisScript<>(OBTAIN_LOCK, Boolean.class);
        this.removeRedisScript = new DefaultRedisScript<>(REMOVE_LOCK);
    }

    @Override
    public void lock() {
        try {
            lock.lock();
            while (!obtainLockFromRedis()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ignore) {
            lock.unlock();
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit timeUnit) {
        long now = System.currentTimeMillis();
        try {
            if (lock.tryLock(time, timeUnit)) {
                long expire = now + timeUnit.toMillis(time);
                boolean acquired;
                while (!(acquired = obtainLockFromRedis()) && System.currentTimeMillis() < expire) {
                    Thread.sleep(100);
                }
                return acquired;
            }
        } catch (InterruptedException e) {
            lock.unlock();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit timeUnit, int retry) {
        if (retry <= 0) {
            return tryLock(time, timeUnit);
        }
        boolean acquired;
        do {
            acquired = tryLock(time, timeUnit);
        } while (!acquired && retry-- > 0);
        return acquired;
    }

    @Override
    public void unlock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
        removeLockFromRedis();
    }

    private boolean obtainLockFromRedis() {
        Boolean success = stringRedisTemplate.execute(
                obtainRedisScript,
                Collections.singletonList(lockKey),
                clientId,
                String.valueOf(expireTimeUnit.toMillis(expireTime))
        );
        return Boolean.TRUE.equals(success);
    }

    private void removeLockFromRedis() {
        stringRedisTemplate.execute(
                removeRedisScript,
                Collections.singletonList(lockKey),
                clientId
        );
    }
}
