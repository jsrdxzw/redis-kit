## Redis Distributed Kit

this utils use local sync lock and redis lock to provide high performance redis utils

### Firstly import redis maven pom
```xml
<dependency>
    <groupId>com.github.jsrdxzw</groupId>
    <artifactId>redis-kit-spring-boot-starter</artifactId>
    <version>1.0.2</version>
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

it will get value from redis and if the key does not exist in redis it will go on next process and put value in redis as cache.
by default the expired time is `5 minutes`.
```java
@Cache(key="xzw")
public Student methodName() {
}
```
it will remove redis value based on [Cache aside](https://www.usenix.org/system/files/conference/nsdi13/nsdi13-final170_update.pdf)

it is recommended to use @Transactional annotation

```java
@Transactional(rollbackFor = Throwable.class)
@Put(key="xzw")
public Student methodName() {
}
```
delete is same as put

```java
@Transactional(rollbackFor = Throwable.class)
@Delete(key="xzw")
public void methodName() {
}
```
it will delete value from redis

### rate limiter
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
