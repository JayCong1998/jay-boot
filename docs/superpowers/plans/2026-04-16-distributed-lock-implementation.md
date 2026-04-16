# 分布式锁模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现基于 Redisson 的声明式分布式锁模块，通过 @DistributedLock 注解实现方法级别的分布式互斥控制。

**Architecture:** 采用注解 + AOP 切面模式，与现有 @RateLimit 限流模块风格保持一致。支持可重入锁、公平锁、读写锁四种锁类型，支持 SpEL 表达式解析动态 Key。

**Tech Stack:** Spring Boot 3.3.x + Redisson 3.27.0 + AOP

---

## 文件结构

```
jay-boot-backend/
├── pom.xml                                          # 添加 Redisson 依赖
├── src/main/resources/application.yml               # 添加分布式锁配置
└── src/main/java/com/jaycong/boot/
    └── modules/lock/
        ├── annotation/
        │   └── DistributedLock.java                 # 分布式锁注解
        ├── aspect/
        │   └── DistributedLockAspect.java           # AOP 切面
        ├── config/
        │   ├── RedissonConfig.java                  # Redisson 配置
        │   └── DistributedLockProperties.java       # 配置属性类
        ├── exception/
        │   └── DistributedLockException.java        # 锁异常
        └── util/
            └── SpelKeyParser.java                   # SpEL 解析器
```

---

### Task 1: 添加 Redisson 依赖

**Files:**
- Modify: `jay-boot-backend/pom.xml`

- [ ] **Step 1: 在 pom.xml 中添加 Redisson 依赖**

在 `</dependencies>` 标签前添加：

```xml
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>3.27.0</version>
        </dependency>
```

- [ ] **Step 2: 验证依赖下载**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn dependency:resolve -q`
Expected: 无报错，依赖下载成功

---

### Task 2: 添加配置属性

**Files:**
- Modify: `jay-boot-backend/src/main/resources/application.yml`

- [ ] **Step 1: 在 application.yml 末尾添加分布式锁配置**

```yaml

distributed-lock:
  enabled: true
  key-prefix: "jay_lock"
  default-wait-time: 0
  default-lease-time: -1
```

---

### Task 3: 创建配置属性类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/lock/config/DistributedLockProperties.java`

- [ ] **Step 1: 创建配置属性类**

```java
package com.jaycong.boot.modules.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁配置属性
 */
@Data
@ConfigurationProperties(prefix = "distributed-lock")
public class DistributedLockProperties {
    
    /**
     * 是否启用分布式锁
     */
    private boolean enabled = true;
    
    /**
     * 锁 key 前缀
     */
    private String keyPrefix = "lock";
    
    /**
     * 默认等待时间（毫秒）
     */
    private long defaultWaitTime = 0;
    
    /**
     * 默认持有时间（毫秒），-1 表示自动续期
     */
    private long defaultLeaseTime = -1;
}
```

---

### Task 4: 创建 Redisson 配置类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/lock/config/RedissonConfig.java`

- [ ] **Step 1: 创建 Redisson 配置类**

```java
package com.jaycong.boot.modules.lock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 */
@Configuration
@EnableConfigurationProperties({DistributedLockProperties.class})
public class RedissonConfig {
    
    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        
        String address = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
        
        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty() 
                        ? redisProperties.getPassword() 
                        : null)
                .setDatabase(redisProperties.getDatabase())
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(5)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000);
        
        return Redisson.create(config);
    }
}
```

---

### Task 5: 创建分布式锁异常类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/lock/exception/DistributedLockException.java`

- [ ] **Step 1: 创建锁异常类**

```java
package com.jaycong.boot.modules.lock.exception;

/**
 * 分布式锁获取失败异常
 */
public class DistributedLockException extends RuntimeException {
    
    public DistributedLockException(String message) {
        super(message);
    }
}
```

---

### Task 6: 创建分布式锁注解

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/lock/annotation/DistributedLock.java`

- [ ] **Step 1: 创建分布式锁注解**

```java
package com.jaycong.boot.modules.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * 标注在方法上，实现分布式互斥控制
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    
    /**
     * 锁的 key，支持完整 SpEL 表达式
     * 示例: "'order:' + #orderId" 或 "'payment:' + #request.userId"
     */
    String key();
    
    /**
     * 锁等待时间，0 表示不等待立即失败
     * 默认值 0，可通过配置文件修改默认值
     */
    long waitTime() default 0;
    
    /**
     * 锁持有时间（毫秒），-1 表示持有直到方法执行完成（自动续期）
     * 默认值 -1，可通过配置文件修改默认值
     */
    long leaseTime() default -1;
    
    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
    
    /**
     * 锁类型
     */
    LockType lockType() default LockType.REENTRANT;
    
    /**
     * 获取锁失败的提示消息
     */
    String message() default "操作过于频繁，请稍后再试";
    
    /**
     * 锁类型枚举
     */
    enum LockType {
        /** 可重入锁（默认） */
        REENTRANT,
        /** 公平锁 */
        FAIR,
        /** 读写锁 - 读锁 */
        READ,
        /** 读写锁 - 写锁 */
        WRITE
    }
}
```

---

### Task 7: 创建 SpEL 表达式解析器

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/lock/util/SpelKeyParser.java`

- [ ] **Step 1: 创建 SpEL 解析器**

```java
package com.jaycong.boot.modules.lock.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * SpEL 表达式解析器
 * 用于解析分布式锁 key 中的 SpEL 表达式
 */
public class SpelKeyParser {
    
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    
    /**
     * 解析 SpEL 表达式
     *
     * @param spel  SpEL 表达式
     * @param point 切点
     * @return 解析后的字符串
     */
    public static String parse(String spel, ProceedingJoinPoint point) {
        if (spel == null || spel.isEmpty()) {
            return "";
        }
        
        // 如果不包含 SpEL 表达式特征，直接返回
        if (!spel.contains("#") && !spel.contains("T(")) {
            return spel;
        }
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();
        Parameter[] parameters = method.getParameters();
        
        // 创建上下文
        EvaluationContext context = new StandardEvaluationContext();
        
        // 将方法参数绑定到上下文
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }
        
        // 解析表达式
        Expression expression = PARSER.parseExpression(spel);
        Object value = expression.getValue(context);
        
        return value != null ? value.toString() : "";
    }
}
```

---

### Task 8: 创建 AOP 切面

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/lock/aspect/DistributedLockAspect.java`

- [ ] **Step 1: 创建分布式锁切面**

```java
package com.jaycong.boot.modules.lock.aspect;

import com.jaycong.boot.modules.lock.annotation.DistributedLock;
import com.jaycong.boot.modules.lock.config.DistributedLockProperties;
import com.jaycong.boot.modules.lock.exception.DistributedLockException;
import com.jaycong.boot.modules.lock.util.SpelKeyParser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 */
@Aspect
@Component
public class DistributedLockAspect {
    
    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);
    
    private final RedissonClient redissonClient;
    private final DistributedLockProperties properties;
    
    public DistributedLockAspect(RedissonClient redissonClient, DistributedLockProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
    }
    
    @Around("@annotation(com.jaycong.boot.modules.lock.annotation.DistributedLock)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!properties.isEnabled()) {
            return point.proceed();
        }
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        
        if (distributedLock == null) {
            return point.proceed();
        }
        
        // 解析 SpEL 表达式获取实际 key
        String lockKey = SpelKeyParser.parse(distributedLock.key(), point);
        String fullKey = properties.getKeyPrefix() + ":" + lockKey;
        
        // 获取配置参数
        long waitTime = distributedLock.waitTime() > 0 
                ? distributedLock.waitTime() 
                : properties.getDefaultWaitTime();
        long leaseTime = distributedLock.leaseTime() > 0 
                ? distributedLock.leaseTime() 
                : properties.getDefaultLeaseTime();
        TimeUnit timeUnit = distributedLock.timeUnit();
        
        // 获取锁对象
        RLock lock = getLock(fullKey, distributedLock.lockType());
        
        // 尝试获取锁
        boolean acquired = tryAcquireLock(lock, waitTime, leaseTime, timeUnit);
        
        if (!acquired) {
            log.warn("分布式锁获取失败: key={}, waitTime={}ms", fullKey, waitTime);
            throw new DistributedLockException(distributedLock.message());
        }
        
        log.debug("分布式锁获取成功: key={}", fullKey);
        
        try {
            // 执行业务方法
            return point.proceed();
        } finally {
            // 释放锁（仅当前线程持有）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("分布式锁释放成功: key={}", fullKey);
            }
        }
    }
    
    /**
     * 根据锁类型获取对应的锁对象
     */
    private RLock getLock(String key, DistributedLock.LockType lockType) {
        return switch (lockType) {
            case FAIR -> redissonClient.getFairLock(key);
            case READ -> {
                RReadWriteLock rwLock = redissonClient.getReadWriteLock(key);
                yield rwLock.readLock();
            }
            case WRITE -> {
                RReadWriteLock rwLock = redissonClient.getReadWriteLock(key);
                yield rwLock.writeLock();
            }
            default -> redissonClient.getLock(key);
        };
    }
    
    /**
     * 尝试获取锁
     */
    private boolean tryAcquireLock(RLock lock, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        if (leaseTime > 0) {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } else {
            // leaseTime <= 0 时使用自动续期
            return lock.tryLock(waitTime, timeUnit);
        }
    }
}
```

---

### Task 9: 全局异常处理

**Files:**
- Search: 查找现有的全局异常处理器
- Modify: 添加分布式锁异常处理

- [ ] **Step 1: 查找现有全局异常处理器**

Run: 在 `D:\develop\CodeProject\jay-boot\jay-boot-backend\src\main\java` 目录下搜索 `@RestControllerAdvice` 注解的类

- [ ] **Step 2: 在全局异常处理器中添加分布式锁异常处理**

在全局异常处理器类中添加：

```java
@ExceptionHandler(DistributedLockException.class)
public ResponseEntity<ApiResponse<Void>> handleDistributedLockException(DistributedLockException ex) {
    log.warn("分布式锁异常: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResponse.fail(429, ex.getMessage()));
}
```

---

### Task 10: 编译验证

- [ ] **Step 1: 编译项目**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 11: 提交代码

- [ ] **Step 1: 提交所有变更**

```bash
git add jay-boot-backend/pom.xml
git add jay-boot-backend/src/main/resources/application.yml
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/lock/
git commit -m "feat: 实现基于 Redisson 的分布式锁模块"
```

---

## 使用示例

实现完成后，可在业务代码中使用：

```java
// 秒杀扣库存
@PostMapping("/seckill/{productId}")
@DistributedLock(key = "'seckill:' + #productId")
public ApiResponse<Void> seckill(@PathVariable Long productId) {
    seckillService.decreaseStock(productId);
    return ApiResponse.success(null);
}

// 订单支付 - 等待超时
@PostMapping("/pay/{orderId}")
@DistributedLock(key = "'order:pay:' + #orderId", waitTime = 3000, message = "订单正在处理中")
public ApiResponse<Void> payOrder(@PathVariable Long orderId) {
    orderService.pay(orderId);
    return ApiResponse.success(null);
}
```
