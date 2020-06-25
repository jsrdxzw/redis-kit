package com.jsrdxzw.redis.ratelimit;

/**
 * @author xuzhiwei
 * implement three algorithm
 * including rolling window token bucket and leaky bucket
 * support 1w+ qps
 */
public interface RateLimit {
    /**
     * request within interval time
     *
     * @param key    user key
     * @param limit  limit request count
     * @param second limit per time
     * @param expire expire time
     * @return true if can request
     */
    boolean acquire(String key, Integer limit, Integer second, Integer expire);

    /**
     * request within interval time
     *
     * @param key    user key
     * @param limit  limit request count
     * @param second limit per time
     * @return true if can request in specific time
     */
    default boolean acquire(String key, Integer limit, Integer second) {
        return acquire(key, limit, second, null);
    }
}
