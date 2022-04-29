## Redis Distributed Kit

this repo uses local sync lock and redis lock to provide high performance redis tools This redis kit is recommended in
single redis machine.

![distribute_lock](images/distribute-lock.jpg)

### performance test report
we use our redis kit to compare with their performance in concurrent environment.

1000qps * 10 count

|  machine  | redisson  | redis-kit | redis-kit (preload mode)
|  ----  | ----  | ---- | ---- |
| single instance (lock)  | 5286ms | 5394ms | 5184ms  |
| two instances (lock)  | 5854ms | 6620ms | 6184ms |
| single instance (tryLock) | 1271ms | 738ms | 720ms  |
| two instances (tryLock)  | 2230ms | 1714ms | 1620ms |

*In conclusion, redis-kit is almost as fast as redisson when using lock, but
when using tryLock the redis-kit is faster about 40% than redisson.*


### Import Redis Kit in your project

```xml

<dependency>
    <groupId>com.github.jsrdxzw</groupId>
    <artifactId>redis-kit-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency> 
```

```java
import com.jsrdxzw.redis.core.EnableRedisKit;

@EnableRedisKit
public class SpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootApplication.class, args);
    }
}
```

### Use Distributed Lock

by default `StringRedisTemplate` which is provided by Spring is used, Of course you can choose other RedisTemplate by
yourself.

```java

@Configuration
public class DistributedLockConfiguration {
    @Bean
    public RedisLockFactory redisLockFactory(StringRedisTemplate redisTemplate) {
        return new DefaultRedisLockFactory(redisTemplate);
    }
}
```

use lock in your own business logic code

```java
public class UserService {
    @Autowired
    private RedisLockFactory redisLockFactory;

    public void method() {
        RedisLock RLock = redisLockFactory.getLock("xzw");
        try {
            //RLock.lock(); by default the expire time is 60s
            // set expire time is recommended because busy waiting may cause deadlock
            // when redis machine is down..
            RLock.tryLock(30, TimeUnit.SECONDS);
            //RLock.tryLock(30, TimeUnit.SECONDS, 3);
            // your own logic
        } finally {
            RLock.unlock();
        }
    }
}
```

In the other way, annotations such as `@DistributedLock`, `DistributedTryLock` are also provided, please import the
spring aop at the first place before using annotations.

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

```java
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication(scanBasePackages = {"your.path", "com.jsrdxzw.redis"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### example of distributed lock by annotation

```java
// @DistributedLock(lockKey = "your key")
@DistributedTryLock(lockKey = "your key", waitTime = 10)
public void method(){
        //...
        }
```

### Notice
we don't recommend use redis lock with @Transactional because it may cause visibility problems.
```java
@Transactional
public void reduceStock(Long id) {
    RedisLock lock = redisLockFactory.getLock("test2");
    try {
        lock.lock();
        SkuStock skuStock = skuStockRepository.getOne(id);
        Integer stock = skuStock.getStock();
        if (stock > 0) {
            log.info("stock is {}", stock);
            skuStock.setStock(stock - 1);
            skuStockRepository.save(skuStock);
        }
        longAdder.increment();
        log.info("这是第{}个请求, 改之前的stock:{}", longAdder.longValue(), stock);
    } finally {
        // when lock is released by one client, the other client will
        // get redis lock immediately when Transaction may not commit.
        // The other client will get old value by using Mysql.
        lock.unlock();
    }

}
```

### enable preload mode

we support preload mode from v1.0.4 that means the lua script is preloaded before used. it can save memory and increase
performance.

```yaml
# by default preload mode is disabled 
redis-kit:
  preload: true
```

### Redis Cache Example

it will get value from redis and if the key does not exist in redis it will do next process and put value in redis as
cache. by default the expired time is `5 minutes`.
redis key can retrieve params from invoked function.

```java
// key = id
@Cache(key = "id", expireTime = 10, timeUnit = TimeUnit.SECONDS)
public somethingVo testCache(Integer id) {
    //
}

// key = ro.title + ro.name
@Cache(key = "{title + name}", expireTime = 10, timeUnit = TimeUnit.SECONDS)
public somethingVo testCache(MerchandiseGroupRo ro) {
    //
}

// key = ro.title + id
@Cache(key = "{title}#id", expireTime = 10, timeUnit = TimeUnit.SECONDS)
public somethingVo testCache(MerchandiseGroupRo ro, Integer id) {
    //
}

// key = "hello"
@Cache(key = "hello", expireTime = 10, timeUnit = TimeUnit.SECONDS)
public somethingVo testCache() {
    //
}
```

it will remove redis value based on cache principle
-- [Cache aside](https://www.usenix.org/system/files/conference/nsdi13/nsdi13-final170_update.pdf)

it is recommended to use @Transactional annotation when modifying cache values

```java
@Transactional(rollbackFor = Throwable.class)
@Put(key = "xzw")
public Student methodName(){
        }
```

`@Delete` is same as `@Put`

```java
@Transactional(rollbackFor = Throwable.class)
@Delete(key = "xzw")
public void methodName(){
        }
```

it will delete value from redis

### rate limiter

```java
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private RateLimit rateLimit;

boolean require=rateLimit.acquire("xzw",5,10);
```

it means we allow 5 requests per seconds, and when the request of per second is greater than 5, it will return false

we have provided three distributed limit rate algorithms:
1. counter [by default]
2. rolling window
3. token bucket

```yaml
# you can change limit algorithm by overriding spring yaml file
redis-kit:
  rate-limit: 
    strategy: tokenBucket # default, rollingWindow
    bucket-size: 100 # default is 10
```

also, Spring AOP is used to support @Annotation features

```java
import com.jsrdxzw.redis.ratelimit.RateLimiter;

@RateLimiter(key = "123", limit = 10, time = 10, expire = 30)
public Object method() {
    
}
```