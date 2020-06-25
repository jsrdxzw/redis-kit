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
            try {
                return mapper.readValue(value, signature.getReturnType());
            } catch (JsonProcessingException e) {
                // maybe user changed the return type but old value still stayed in redis
                return getReturnValueAndSet(pjp, annotation, key);
            }
        }
        return getReturnValueAndSet(pjp, annotation, key);
    }

    @AfterReturning(value = "put()", returning = "value")
    public Object putCache(JoinPoint pjp, Object value) throws JsonProcessingException {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Put annotation = signature.getMethod().getAnnotation(Put.class);
        String key = annotation.key();
        Assert.hasLength(key, "cache key must not be null!");
        stringRedisTemplate.opsForValue().set(key, mapper.writeValueAsString(value), annotation.expireTime(), annotation.timeUnit());
        return value;
    }

    @Before("delete()")
    public void deleteCache(JoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Delete annotation = signature.getMethod().getAnnotation(Delete.class);
        String key = annotation.key();
        stringRedisTemplate.delete(key);
    }

    private Object getReturnValueAndSet(ProceedingJoinPoint pjp, Cache annotation, String key) throws Throwable {
        Object result = pjp.proceed();
        stringRedisTemplate.opsForValue().set(key, mapper.writeValueAsString(result), annotation.expireTime(), annotation.timeUnit());
        return result;
    }
}
