package com.jsrdxzw.redis.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author xuzhiwei
 */
@Configuration
@ConditionalOnClass({StringRedisTemplate.class})
@ConfigurationProperties(prefix = "redis-kit.rate-limit")
public class RateLimitConfiguration {

    /**
     * default is counter limit rate
     * supports for counter, slide window and token buckets
     */
    private String strategy = "default";

    /**
     * bucket size for token bucket
     */
    private Integer bucketSize = 10;

    /**
     * per request neet token
     */
    private Integer token = 1;

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Integer getBucketSize() {
        return bucketSize;
    }

    public void setBucketSize(Integer bucketSize) {
        this.bucketSize = bucketSize;
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimit rateLimit(StringRedisTemplate stringRedisTemplate, RateLimitConfiguration configuration) {
        return RateLimitEnum.fromName(configuration.getStrategy()).createRateLimit(stringRedisTemplate, configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect() {
        return new RateLimitAspect();
    }
}
