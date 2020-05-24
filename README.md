## Redis Distributed Lock

this utils use local sync lock and redis lock to provide high performance

### Example
```java
@Configuration
public class DistributedLockConf{
    @Bean
    public RedisLockFactory redisLockFactory(StringRedisTemplate redisTemplate){
        return new DefaultRedisLockFactory(redisTemplate);
    }
}
```

```java
public class UserService{
    @Autowired
    private RedisLockFactory redisLockFactory;
    public void method() {
        RedisLock RLock = redisLockFactory.getLock("hello");
        try {
            //RLock.lock();
            RLock.tryLock(30, TimeUnit.SECONDS);
            //RLock.tryLock(30, TimeUnit.SECONDS, 3);
            // your own logic
        } finally{
            RLock.unlock();
        }
    }
}
```
