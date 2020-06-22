package com.jsrdxzw.redis.operator.configuration;

import com.jsrdxzw.redis.operator.RedisOperator;
import com.jsrdxzw.redis.operator.impl.RedisOperatorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author xuzhiwei
 * @date 2020/06/21
 */
@Configuration
public class RedisOperatorConfiguration {
    @Bean
    public RedisOperator redisOperator(StringRedisTemplate stringRedisTemplate) {
        return new RedisOperatorImpl(stringRedisTemplate);
    }
}
