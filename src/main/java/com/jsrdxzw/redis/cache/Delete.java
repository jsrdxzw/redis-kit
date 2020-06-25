package com.jsrdxzw.redis.cache;

import java.lang.annotation.*;

/**
 * @author xuzhiwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delete {
    String key();
}
