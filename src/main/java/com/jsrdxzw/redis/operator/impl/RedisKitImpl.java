package com.jsrdxzw.redis.operator.impl;

import com.jsrdxzw.redis.operator.RedisKit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * @author xuzhiwei
 * @date 2020/06/21
 */
public class RedisKitImpl implements RedisKit {

    private final RedisScript<Long> getAndIncrementRedisScript;
    private final RedisScript<Long> incrementAndGetRedisScript;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String GET_AND_INCREMENT_SCRIPT = "if KEYS[1] ~= nil then\n" +
            "    local result = redis.call('GET', KEYS[1])\n" +
            "    redis.call('INCRBY', KEYS[1], ARGV[1])\n" +
            "    if ARGV[2] ~= 'null' then\n" +
            "        redis.call('PEXPIRE', KEYS[1], ARGV[2])\n" +
            "    else\n" +
            "        redis.call('PERSIST', KEYS[1])\n" +
            "    end\n" +
            "    return tonumber(result)\n" +
            "end";

    private static final String INCREMENT_AND_GET_SCRIPT = "if KEYS[1] ~= nil then\n" +
            "    local result = redis.call('INCRBY', KEYS[1], ARGV[1])\n" +
            "    if ARGV[2] ~= 'null' then\n" +
            "        redis.call('PEXPIRE', KEYS[1], ARGV[2])\n" +
            "    else\n" +
            "        redis.call('PERSIST', KEYS[1])\n" +
            "    end\n" +
            "    return result\n" +
            "end";

    public RedisKitImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.getAndIncrementRedisScript = new DefaultRedisScript<>(GET_AND_INCREMENT_SCRIPT, Long.class);
        this.incrementAndGetRedisScript = new DefaultRedisScript<>(INCREMENT_AND_GET_SCRIPT, Long.class);
    }

    @Nullable
    @Override
    public Long getAndIncrement(String key, Integer step, Long expireTime) {
        Assert.hasLength(key, "key can not be null");
        return stringRedisTemplate.execute(getAndIncrementRedisScript, Collections.singletonList(key), String.valueOf(step), String.valueOf(expireTime));
    }

    @Nullable
    @Override
    public Long incrementAndGet(String key, Integer step, Long expireTime) {
        Assert.hasLength(key, "key can not be null");
        return stringRedisTemplate.execute(incrementAndGetRedisScript, Collections.singletonList(key), String.valueOf(step), String.valueOf(expireTime));
    }

}
