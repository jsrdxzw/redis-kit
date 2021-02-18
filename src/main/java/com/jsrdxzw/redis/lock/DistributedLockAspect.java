package com.jsrdxzw.redis.lock;

import com.jsrdxzw.redis.lock.factory.RedisLockFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xuzhiwei
 */
@Component
@Aspect
public class DistributedLockAspect {
    @Autowired
    private RedisLockFactory redisLockFactory;

    @Pointcut("@annotation(com.jsrdxzw.redis.lock.DistributedLock)")
    private void distributedLockPoint() {
    }

    @Pointcut("@annotation(com.jsrdxzw.redis.lock.DistributedTryLock)")
    private void distributedTryLockPoint() {
    }

    @Around("distributedLockPoint()")
    public Object distributedLockAround(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        DistributedLock annotation = signature.getMethod().getAnnotation(DistributedLock.class);
        RedisLock lock = redisLockFactory.getLock(annotation.lockKey(), annotation.expireTime(), annotation.expireTimeUnit());
        try {
            lock.lock();
            return pjp.proceed();
        } catch (Throwable throwable) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Around("distributedTryLockPoint()")
    public Object distributedTryLockAround(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        DistributedTryLock annotation = signature.getMethod().getAnnotation(DistributedTryLock.class);
        RedisLock lock = redisLockFactory.getLock(annotation.lockKey(), annotation.expireTime(), annotation.expireTimeUnit());
        try {
            return lock.tryLock(annotation.waitTime(), annotation.waitTimeUnit(), annotation.retry()) ?
                    pjp.proceed() : null;
        } catch (Throwable throwable) {
            return null;
        } finally {
            lock.unlock();
        }
    }
}
