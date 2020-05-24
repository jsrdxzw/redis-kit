package com.jsrdxzw.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 * @date 2020/05/23
 * this is the Distributed Redis Lock
 */
public interface RedisLock {

    /**
     * lock until the lock is released
     * this behavior will reset expire time of lock until that is be obtained
     */
    void lock();

    /**
     * tryLock and return true when lock is released
     * this may reset the expire time
     *
     * @param time     wait time
     * @param timeUnit timeUnit
     * @return true when get lock successfully or false when wait after expected time without getting a lock
     */
    boolean tryLock(long time, TimeUnit timeUnit);

    /**
     * try to get lock and return result immediately
     *
     * @return true if get a lock successfully or false
     */
    default boolean tryLock() {
        return tryLock(0, TimeUnit.MILLISECONDS);
    }

    /**
     * the same as tryLock, but add retry mechanism
     *
     * @param time     wait time
     * @param timeUnit timeUnit
     * @param retry    retry count
     * @return true when get lock successfully in wait time or in retry count
     */
    boolean tryLock(long time, TimeUnit timeUnit, int retry);

    /**
     * release the lock, it is recommended that invoke this method in finally block
     */
    void unlock();
}
