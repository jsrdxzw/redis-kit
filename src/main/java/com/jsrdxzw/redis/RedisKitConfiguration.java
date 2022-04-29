package com.jsrdxzw.redis;

import com.jsrdxzw.redis.cache.CacheAspect;
import com.jsrdxzw.redis.lock.DistributedLockAspect;
import com.jsrdxzw.redis.lock.factory.DefaultRedisLockFactory;
import com.jsrdxzw.redis.lock.factory.PreloadRedisLockFactory;
import com.jsrdxzw.redis.lock.factory.RedisLockFactory;
import com.jsrdxzw.redis.operator.RedisKit;
import com.jsrdxzw.redis.operator.impl.RedisKitImpl;
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

    private Boolean preload = false;

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
    public CacheAspect cacheAspect() {
        return new CacheAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAspect distributedLockAspect() {
        return new DistributedLockAspect();
    }
}
