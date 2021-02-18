package com.jsrdxzw.redis;

import com.jsrdxzw.redis.lock.factory.DefaultRedisLockFactory;
import com.jsrdxzw.redis.lock.factory.PreloadRedisLockFactory;
import com.jsrdxzw.redis.lock.factory.RedisLockFactory;
import com.jsrdxzw.redis.operator.RedisKit;
import com.jsrdxzw.redis.operator.impl.RedisKitImpl;
import com.jsrdxzw.redis.ratelimit.RateLimit;
import com.jsrdxzw.redis.ratelimit.RateLimitEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author xuzhiwei
 */
@ConfigurationProperties(prefix = "redis-kit")
@Configuration
@ConditionalOnClass({StringRedisTemplate.class})
public class RedisKitConfiguration {

    private String rateLimit = "default";
    private Boolean preload = false;

    public String getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(String rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Boolean getPreload() {
        return preload;
    }

    public void setPreload(Boolean preload) {
        this.preload = preload;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisLockFactory redisLockFactory(StringRedisTemplate stringRedisTemplate) {
        if (preload) {
            return new PreloadRedisLockFactory(stringRedisTemplate);
        }
        return new DefaultRedisLockFactory(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisKit redisOperator(StringRedisTemplate stringRedisTemplate) {
        return new RedisKitImpl(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimit rateLimit(StringRedisTemplate stringRedisTemplate) {
        return RateLimitEnum.fromName(rateLimit).createRateLimit(stringRedisTemplate);
    }
}
