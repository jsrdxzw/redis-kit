package com.jsrdxzw.redis.ratelimit.strategy;

import com.jsrdxzw.redis.ratelimit.RateLimit;
import org.springframework.util.Assert;

/**
 * @author xuzhiwei
 * @date 2020-06-23
 */
public class TokenBucketRateLimit implements RateLimit {
    @Override
    public boolean acquire(String key, Integer limit, Integer second, Integer expire) {
        Assert.hasLength(key, "key can not be null");
        return false;
    }
}