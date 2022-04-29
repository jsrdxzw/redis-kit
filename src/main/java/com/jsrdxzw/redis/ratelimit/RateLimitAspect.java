package com.jsrdxzw.redis.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * @author xuzhiwei
 */
@Component
@ConditionalOnBean({RateLimit.class})
@Aspect
public class RateLimitAspect {
    @Autowired
    private RateLimit rateLimit;

    @Pointcut("@annotation(com.jsrdxzw.redis.ratelimit.RateLimiter)")
    private void rateLimiter() {
    }

    @Around("rateLimiter()")
    public Object rateLimit(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RateLimiter annotation = method.getAnnotation(RateLimiter.class);
        String key = annotation.key();
        Assert.hasLength(key, "key can not be null");
        Integer expire = annotation.expire() == -1 ? null : annotation.expire();
        boolean acquire = rateLimit.acquire(annotation.key(), annotation.limit(), annotation.time(), expire);
        if (!acquire) {
            throw new RuntimeException("invoke too frequently");
        }
        return pjp.proceed();
    }
}
