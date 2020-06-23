package com.jsrdxzw.redis.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author xuzhiwei
 * @date 2020-06-23
 */
@Component
@Aspect
public class CacheAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @Pointcut("@annotation(com.jsrdxzw.redis.cache.Cache)")
    private void cache() {
    }

    @Pointcut("@annotation(com.jsrdxzw.redis.cache.Put)")
    private void put() {
    }

    @Pointcut("@annotation(com.jsrdxzw.redis.cache.Delete)")
    private void delete() {

    }

    @SuppressWarnings("unchecked")
    @Around("cache()")
    public Object cache(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Cache annotation = signature.getMethod().getAnnotation(Cache.class);
        String key = annotation.key();
        Assert.hasLength(key, "cache key must not be null!");
        String value = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(value)) {
            return mapper.readValue(value, signature.getReturnType());
        }
        Object result = pjp.proceed();
        stringRedisTemplate.opsForValue().set(key, mapper.writeValueAsString(result), annotation.expireTime(), annotation.timeUnit());
        return result;
    }

    @AfterReturning(value = "put()", returning = "value")
    public Object putCache(JoinPoint pjp, Object value) throws JsonProcessingException {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Cache annotation = signature.getMethod().getAnnotation(Cache.class);
        String key = annotation.key();
        Assert.hasLength(key, "cache key must not be null!");
        stringRedisTemplate.opsForValue().set(key, mapper.writeValueAsString(value), annotation.expireTime(), annotation.timeUnit());
        return value;
    }

    @Before("delete()")
    public void deleteCache(JoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Cache annotation = signature.getMethod().getAnnotation(Cache.class);
        String key = annotation.key();
        stringRedisTemplate.delete(key);
    }
}
