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
public class RollingWindowRateLimit implements RateLimit {


    private static final String RATE_LIMIT_SCRIPT = "local now = tonumber(ARGV[1])\n" +
            "local from = now - tonumber(ARGV[3])\n" +
            "local key = KEYS[1]\n" +
            "local limit = ARGV[2]\n" +
            "if key ~= nil then\n" +
            "    local count = redis.call('ZREMRANGEBYSCORE', key, '-inf', from)\n" +
            "    if count <= limit then\n" +
            "        redis.call('ZREMRANGEBYSCORE', key, '-inf', from)\n" +
            "        redis.call('ZADD', key, now, now)\n" +
            "        if ARGV[4] ~= 'null' then\n" +
            "            redis.call('EXPIRE', key, ARGV[4])\n" +
            "        end\n" +
            "        return true\n" +
            "    else\n" +
            "        return false\n" +
            "    end\n" +
            "end";

    private final RedisScript<Boolean> rateLimitScript;
    private final StringRedisTemplate stringRedisTemplate;

    public RollingWindowRateLimit(StringRedisTemplate stringRedisTemplate) {
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
