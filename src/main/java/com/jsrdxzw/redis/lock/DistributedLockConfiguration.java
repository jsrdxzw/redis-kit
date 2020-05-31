package com.jsrdxzw.redis.lock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author xuzhiwei
 * @date 2020/05/31
 */
@Configuration
public class DistributedLockConfiguration {
    @Bean
    public RedisLockFactory redisLockFactory(StringRedisTemplate stringRedisTemplate) {
        return new DefaultRedisLockFactory(stringRedisTemplate);
    }
}
