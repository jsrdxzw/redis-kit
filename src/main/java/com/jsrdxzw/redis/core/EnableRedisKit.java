package com.jsrdxzw.redis.core;

import com.jsrdxzw.redis.RedisKitConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author xuzhiwei
 * @date 2021-02-08
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RedisKitConfiguration.class)
public @interface EnableRedisKit {
}
