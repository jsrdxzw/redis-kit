package com.jsrdxzw.redis.ratelimit;

import java.lang.annotation.*;

/**
 * @author xuzhiwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimiter {
    String key();

    int limit();

    int time();

    int expire() default -1;
}