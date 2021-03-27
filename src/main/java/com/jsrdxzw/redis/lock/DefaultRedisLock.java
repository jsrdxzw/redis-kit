package com.jsrdxzw.redis.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 */
public final class DefaultRedisLock extends AbstractRedisLock {

    private final RedisScript<Boolean> obtainRedisScript;
    private final RedisScript<Void> removeRedisScript;
    private final long expireTime;
    private final TimeUnit expireTimeUnit;

    /**
     * obtain lock script
     */
    private static final String OBTAIN_LOCK = "local lockClientId = redis.call('GET', KEYS[1])\n" +
            "if lockClientId == ARGV[1] then\n" +
            "    redis.call('PEXPIRE', KEYS[1], ARGV[2])\n" +
            "    return true\n" +
            "elseif not lockClientId then\n" +
            "    redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])\n" +
            "    return true\n" +
            "else\n" +
            "    return false\n" +
            "end";

    /**
     * remove key script
     */
    private static final String REMOVE_LOCK = "local lockClientId = redis.call('GET', KEYS[1])\n" +
            "if lockClientId == ARGV[1] then\n" +
            "    redis.call('DEL', KEYS[1])\n" +
            "end";

    public DefaultRedisLock(StringRedisTemplate stringRedisTemplate, String lockKey, long expireTime, TimeUnit expireTimeUnit, String clientId) {
        super(stringRedisTemplate, lockKey, clientId);
        this.expireTime = expireTime;
        this.expireTimeUnit = expireTimeUnit;
        this.obtainRedisScript = new DefaultRedisScript<>(OBTAIN_LOCK, Boolean.class);
        this.removeRedisScript = new DefaultRedisScript<>(REMOVE_LOCK);
    }

    @Override
    protected boolean obtainLockFromRedis() {
        Boolean success = stringRedisTemplate.execute(
                obtainRedisScript,
                Collections.singletonList(lockKey),
                clientId,
                String.valueOf(expireTimeUnit.toMillis(expireTime))
        );
        return Boolean.TRUE.equals(success);
    }

    @Override
    protected void removeLockFromRedis() {
        stringRedisTemplate.execute(
                removeRedisScript,
                Collections.singletonList(lockKey),
                clientId
        );
    }
}
