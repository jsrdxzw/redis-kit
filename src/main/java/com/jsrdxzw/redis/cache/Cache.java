package com.jsrdxzw.redis.cache;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 * @date 2020-06-23
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
    String key();

    long expireTime() default 30;

    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
