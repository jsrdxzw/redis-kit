## Redis Distributed Lock

this utils use local sync lock and redis lock to provide high performance

### Firstly import redis maven pom
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency> 
```

### Use Example
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
If you use the annotation, please import the spring aop at the first place
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

```java
// @DistributedLock(lockKey = "haha")
@DistributedTryLock(lockKey = "haha", waitTime = 10)
public void method() {
   //...
}
```
