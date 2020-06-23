package com.jsrdxzw.redis.ratelimit.strategy;

import com.jsrdxzw.redis.ratelimit.RateLimit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

/**
 * @author xuzhiwei
 * @date 2020-06-23
 */
public class LeakyBucketRateLimit implements RateLimit {

    private static final String RATE_LIMIT_SCRIPT = "local now = tonumber(ARGV[1])\n" +
            "local rate = tonumber(ARGV[2])\n" +
            "local key = KEYS[1]\n" +
            "local from = now - tonumber(ARGV[3])\n" +
            "redis.call('ZREMRANGEBYSCORE', key, '-inf', from)\n" +
            "local last = redis.call('ZRANGE', key, -1, -1)\n" +
            "local next = now\n" +
            "if type(last) == 'table' and #last > 0 then\n" +
            "    for _, value in pairs(last) do\n" +
            "        if tonumber(value) + 1/rate > now then\n" +
            "            return false\n" +
            "        else\n" +
            "            next = tonumber(value) + 1/rate\n" +
            "        end\n" +
            "        break\n" +
            "    end\n" +
            "end\n" +
            "return redis.call('ZADD', key, next, next)";

    private final RedisScript<Boolean> rateLimitScript;
    private final StringRedisTemplate stringRedisTemplate;

    public LeakyBucketRateLimit(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        rateLimitScript = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Boolean.class);
    }

    @Override
    public boolean acquire(String key, Integer limit, Integer second, Integer expire) {
        Assert.hasLength(key, "key can not be null");
        Instant instant = Instant.now();
        int millisecond = second * 1000;
        return Optional.ofNullable(stringRedisTemplate.execute(rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(instant.toEpochMilli()),
                String.valueOf(limit),
                String.valueOf(millisecond),
                String.valueOf(expire)
        )).orElse(false);
    }
}
