package com.jsrdxzw.redis.lock;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xuzhiwei
 */
public abstract class AbstractRedisLock implements RedisLock {

    private final ReentrantLock lock;
    /**
     * each machine has own clientId which is in order to avoid to release other client's lock
     */
    protected final String clientId;
    protected final StringRedisTemplate stringRedisTemplate;
    protected final String lockKey;

    public AbstractRedisLock(StringRedisTemplate stringRedisTemplate, String lockKey) {
        this.lock = new ReentrantLock();
        this.clientId = UUID.randomUUID().toString();
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockKey = lockKey;
    }

    @Override
    public void lock() {
        try {
            lock.lock();
            if (checkReentrantLock(lockKey)) {
                return;
            }
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
                if (checkReentrantLock(lockKey)) {
                    return true;
                }
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

    private boolean checkReentrantLock(String lockKey) {
        String clientId = stringRedisTemplate.opsForValue().get(lockKey);
        return clientId != null && !clientId.isEmpty();
    }

    /**
     * obtain lock from redis
     *
     * @return true if get lock from redis
     */
    protected abstract boolean obtainLockFromRedis();

    /**
     * remove lock from redis
     */
    protected abstract void removeLockFromRedis();
}
