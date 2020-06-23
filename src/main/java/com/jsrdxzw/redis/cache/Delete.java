package com.jsrdxzw.redis.cache;

import java.lang.annotation.*;

/**
 * @author xuzhiwei
 * @date 2020-06-23
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delete {
    String key();
}
