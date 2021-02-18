package com.jsrdxzw.redis.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xuzhiwei
 */
public abstract class AbstractRedisLock implements RedisLock {

    private final ReentrantLock lock = new ReentrantLock();

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
