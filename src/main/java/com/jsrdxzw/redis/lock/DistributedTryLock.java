package com.jsrdxzw.redis.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xuzhiwei
 * @date 2020/05/31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributedTryLock {
    String lockKey();

    int expireTime() default 60;

    TimeUnit expireTimeUnit() default TimeUnit.SECONDS;

    int waitTime() default 30;

    TimeUnit waitTimeUnit() default TimeUnit.SECONDS;

    int retry() default 0;
}
