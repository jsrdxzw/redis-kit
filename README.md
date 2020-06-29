## Redis Distributed Kit

this utils use local sync lock and redis lock to provide high performance redis utils

### Firstly import redis maven pom
```xml
<dependency>
    <groupId>com.github.jsrdxzw</groupId>
    <artifactId>redis-kit-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency> 
```

### Use Distributed Lock

#### by default StringRedisTemplate is used, Of course you can
choose other redisTemplate
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
        RedisLock RLock = redisLockFactory.getLock("xzw");
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

If you want to use annotation such as `@DistributedLock`, `@Cache`, please import the spring aop at the first place
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```
```java
import org.springframework.boot.autoconfigure.SpringBootApplication;import org.springframework.context.annotation.ComponentScan;@ComponentScan

@SpringBootApplication(scanBasePackages = {"your.path", "com.jsrdxzw.redis"})
public class Application{
    public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
    }
}
```

### example of distributed lock by annotation
```java
// @DistributedLock(lockKey = "haha")
@DistributedTryLock(lockKey = "xzw", waitTime = 10)
public void method() {
   //...
}
```
### Redis Cache Example

```java
@Cache(key="xzw")
public Student methodName() {
}
```
it will get value from redis and put value in redis if the value is absent

**notice** it just put the return value to redis, if the method is `void` then it will clear the cache value in redis
```java
@Put(key="xzw")
public Student methodName() {
}
```
it will always update value to redis

```java
@Delete(key="xzw")
public void methodName() {
}
```
it will delete value from redis

### rate limit
```java
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private RateLimit rateLimit;

boolean require = rateLimit.acquire("xzw", 5, 10);
```
it means we allow 5 requests per seconds, and id the request > 5, it will return false

we have provided two algorithms -- token bucket and rolling window
token bucket is used by default 

```yaml
# if you want to use rolling window
redis-kit:
  rate-limit: rollingWindow
```
