package com.jsrdxzw.redis.core;

import com.jsrdxzw.redis.RedisKitConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author xuzhiwei
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RedisKitConfiguration.class)
public @interface EnableRedisKit {
}
