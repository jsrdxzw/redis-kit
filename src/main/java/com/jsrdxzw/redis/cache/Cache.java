package com.jsrdxzw.redis.cache;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
    String key();

    long expireTime() default 5;

    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
