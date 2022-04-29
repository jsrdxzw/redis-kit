package com.jsrdxzw.redis.ratelimit.strategy;

import com.jsrdxzw.redis.ratelimit.RateLimit;
import com.jsrdxzw.redis.ratelimit.RateLimitConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;

/**
 * @author xuzhiwei
 */
public class TokenBucketRateLimit implements RateLimit {

    private static final String RATE_LIMIT_SCRIPT = "local requirement = tonumber(ARGV[1])\n" +
            "local now = tonumber(ARGV[2])\n" +
            "local bucketSize = tonumber(ARGV[3])\n" +
            "local rate = tonumber(ARGV[4])\n" +
            "local data = redis.call(\"HMGET\", KEYS[1], \"time\", \"permits\")\n" +
            "local lastAcquire = data[1]\n" +
            "local currPermits = data[2]\n" +
            "if not lastAcquire then\n" +
            "    lastAcquire = now\n" +
            "    currPermits = bucketSize\n" +
            "else\n" +
            "    local reverse_permits = math.floor(((now - lastAcquire) / 1000) * rate)\n" +
            "    currPermits = math.min(bucketSize, currPermits + reverse_permits)\n" +
            "end \n" +
            "local result = false\n" +
            "if requirement <= currPermits then\n" +
            "    redis.call(\"HMSET\", KEYS[1], \"time\", now, \"permits\", currPermits - requirement)\n" +
            "    result = true\n" +
            "else\n" +
            "    result = false\n" +
            "end\n" +
            "if ARGV[5] ~= 'null' then\n" +
            "    redis.call(\"EXPIRE\", KEYS[1], tonumber(ARGV[5]))\n" +
            "end\n" +
            "return result";

    private final RedisScript<Boolean> rateLimitScript;
    private final StringRedisTemplate stringRedisTemplate;

    private final Integer tokenBucketSize;

    private final Integer token;

    public TokenBucketRateLimit(StringRedisTemplate stringRedisTemplate, RateLimitConfiguration configuration) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Boolean.class);
        this.tokenBucketSize = configuration.getBucketSize();
        this.token = configuration.getToken();
    }

    @Override
    public boolean acquire(String key, Integer limit, Integer second, Integer expire) {
        return acquire(key, token, limit, second, expire);
    }

    public boolean acquire(String key, Integer token, Integer limit, Integer second, Integer expire) {
        Assert.hasLength(key, "key can not be null");
        Instant instant = Instant.now();
        BigDecimal rate = BigDecimal.valueOf(limit).divide(BigDecimal.valueOf(second), 2, RoundingMode.HALF_UP);
        return Boolean.TRUE.equals(stringRedisTemplate.execute(rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(token),
                String.valueOf(instant.toEpochMilli()),
                String.valueOf(tokenBucketSize),
                String.valueOf(rate),
                String.valueOf(expire)));
    }
}
