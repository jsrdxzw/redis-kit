package com.jsrdxzw.redis.cache;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Put {
    String key();

    long expireTime() default 30;

    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
