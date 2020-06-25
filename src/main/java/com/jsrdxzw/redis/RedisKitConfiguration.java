package com.jsrdxzw.redis;

import com.jsrdxzw.redis.lock.DefaultRedisLockFactory;
import com.jsrdxzw.redis.lock.RedisLockFactory;
import com.jsrdxzw.redis.operator.RedisKit;
import com.jsrdxzw.redis.operator.impl.RedisKitImpl;
import com.jsrdxzw.redis.ratelimit.RateLimit;
import com.jsrdxzw.redis.ratelimit.strategy.SimpleRateLimit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author xuzhiwei
 * @date 2020/06/23
 */
@Configuration
public class RedisKitConfiguration {

    @Bean
    public RedisLockFactory redisLockFactory(StringRedisTemplate stringRedisTemplate) {
        return new DefaultRedisLockFactory(stringRedisTemplate);
    }

    @Bean
    public RedisKit redisOperator(StringRedisTemplate stringRedisTemplate) {
        return new RedisKitImpl(stringRedisTemplate);
    }

    @Bean
    public RateLimit rateLimit(StringRedisTemplate stringRedisTemplate) {
        return new SimpleRateLimit(stringRedisTemplate);
    }
}
