package com.jsrdxzw.redis.ratelimit.strategy;

import com.jsrdxzw.redis.ratelimit.RateLimit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Optional;

/**
 * @author xuzhiwei
 * @date 2020-06-23
 */
public class SimpleRateLimit implements RateLimit {

    private static final String RATE_LIMIT_SCRIPT = "local key = KEYS[1]\n" +
            "if key ~= nil then\n" +
            "    local current = tonumber(redis.call('GET', key))\n" +
            "    local limit = tonumber(ARGV[1])\n" +
            "    local expire = tonumber(ARGV[2])\n" +
            "    if current == nil or current <= limit then\n" +
            "        local v = redis.call('INCR', key)\n" +
            "        if v == 1 then\n" +
            "            redis.call('EXPIRE', key, expire)\n" +
            "        end\n" +
            "        return true\n" +
            "    end\n" +
            "end\n" +
            "return false";

    private final RedisScript<Boolean> redisScript;
    private final StringRedisTemplate stringRedisTemplate;

    public SimpleRateLimit(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisScript = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Boolean.class);
    }

    @Override
    public boolean acquire(String key, Integer limit, Integer second, Integer expire) {
        Assert.hasLength(key, "key can not be null");
        return Optional.ofNullable(stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(second)
        )).orElse(false);
    }
}
