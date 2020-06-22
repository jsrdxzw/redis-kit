package com.jsrdxzw.redis.operator;

import org.springframework.lang.Nullable;

/**
 * @author xuzhiwei
 * @date 2020/06/21
 * this is the atomic operator of redis such as add and get
 */
public interface RedisOperator {

    /**
     * get and increment with given key and step
     *
     * @param key        the key
     * @param step       increment step
     * @param expireTime expireTime, time unit is milli seconds
     * @return value of key <strong>before</strong> increment
     */
    @Nullable
    Long getAndIncrement(String key, Integer step, Long expireTime);


    /**
     * get and increment with given key and step
     *
     * @param key        the key
     * @param step       increment step
     * @param expireTime expireTime, time unit is milli seconds
     * @return value of key <strong>after</strong> increment
     */
    @Nullable
    Long incrementAndGet(String key, Integer step, Long expireTime);


    /**
     * get and increment with given key and step
     *
     * @param key  the key
     * @param step add step
     * @return value of key
     */
    default Long getAndIncrement(String key, Integer step) {
        return getAndIncrement(key, step, null);
    }

    /**
     * increment and then get value from given key
     * and each step
     *
     * @param key  the key
     * @param step add step
     * @return value of key
     */
    default Long incrementAndGet(String key, Integer step) {
        return incrementAndGet(key, step, null);
    }

    /**
     * get and increment with given key
     * if no key in redis then return null
     *
     * @param key the key
     * @return value of key
     */

    default Long getAndIncrement(String key) {
        return getAndIncrement(key, 1);
    }

    /**
     * increment and then get value from given key
     * if no key in redis then return 1
     *
     * @param key the key
     * @return value of key
     */
    default Long incrementAndGet(String key) {
        return incrementAndGet(key, 1);
    }
}
