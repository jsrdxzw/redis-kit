package com.jsrdxzw.redis.ratelimit;

import com.jsrdxzw.redis.ratelimit.strategy.RollingWindowRateLimit;
import com.jsrdxzw.redis.ratelimit.strategy.SimpleRateLimit;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;

/**
 * @author xuzhiwei
 */
public enum RateLimitEnum implements RateLimitCreator {
    /**
     * ROLLING_WINDOW_LIMIT
     */
    ROLLING_WINDOW("rollingWindow") {
        @Override
        public RateLimit createRateLimit(StringRedisTemplate redisTemplate) {
            return new RollingWindowRateLimit(redisTemplate);
        }
    },
    /**
     * SIMPLE_RATE_LIMIT
     */
    DEFAULT_RATE_LIMIT("default") {
        @Override
        public RateLimit createRateLimit(StringRedisTemplate redisTemplate) {
            return new SimpleRateLimit(redisTemplate);
        }
    },
    UNKNOWN("unknown") {
        @Override
        public RateLimit createRateLimit(StringRedisTemplate redisTemplate) {
            throw new RuntimeException("unknown rate limit, please set rate limit name correctly in application.yml");
        }
    };

    private final String name;

    RateLimitEnum(String name) {
        this.name = name;
    }

    public static RateLimitEnum fromName(String name) {
        return Arrays.stream(RateLimitEnum.values()).filter(it -> it.name.equals(name)).findFirst().orElse(UNKNOWN);
    }
}

interface RateLimitCreator {

    /**
     * create a rate limit
     *
     * @param redisTemplate StringRedisTemplate
     * @return RateLimit
     */
    RateLimit createRateLimit(StringRedisTemplate redisTemplate);
}
