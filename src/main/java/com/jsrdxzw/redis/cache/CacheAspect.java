package com.jsrdxzw.redis.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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

    private final static ExpressionParser parser = new SpelExpressionParser();

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
        String key = concertRedisKey(pjp, annotation.key());
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

    @After(value = "put()")
    public void putCache(JoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Put annotation = signature.getMethod().getAnnotation(Put.class);
        String key = concertRedisKey(pjp, annotation.key());
        stringRedisTemplate.delete(key);
    }

    @After("delete()")
    public void deleteCache(JoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Delete annotation = signature.getMethod().getAnnotation(Delete.class);
        String key = concertRedisKey(pjp, annotation.key());
        stringRedisTemplate.delete(key);
    }

    private Object getReturnValueAndSet(ProceedingJoinPoint pjp, Cache annotation, String key) throws Throwable {
        Object result = pjp.proceed();
        stringRedisTemplate.opsForValue().set(key, mapper.writeValueAsString(result), annotation.expireTime(), annotation.timeUnit());
        return result;
    }

    private boolean isSpelExpression(String key) {
        return key.startsWith("{") && key.endsWith("}");
    }

    private String parseKey(String key, Object arg) {
        if (!isSpelExpression(key)) {
            return arg.toString();
        }
        String spel = key.replaceAll("[{}]", "");
        EvaluationContext context = new StandardEvaluationContext(arg);
        Expression expression = parser.parseExpression(spel);
        Object value = expression.getValue(context);
        if (value == null) {
            throw new RuntimeException("redis key parse error, the " + key + " can not retrieve value from: " + arg);
        }
        return value.toString();
    }

    private String concertRedisKey(JoinPoint pjp, String key) {
        Assert.hasLength(key, "cache key must not be null!");
        Object[] args = pjp.getArgs();
        if (args.length == 0) {
            return key;
        }
        key = key.replaceAll("\\s*", "");
        String[] split = key.split("#");
        if (split.length > args.length) {
            throw new RuntimeException("expected args length is greater than " + split.length + " but actual args length is " + args.length);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String parsedKey = parseKey(split[i], args[i]);
            sb.append(parsedKey);
        }
        return sb.toString();
    }
}
