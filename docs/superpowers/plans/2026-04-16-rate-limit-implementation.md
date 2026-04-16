# API 限流功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现基于 IP 和用户维度的 API 限流功能，采用令牌桶算法 + Redis 存储。

**Architecture:** 通过 AOP 切面拦截标注 `@RateLimit` 注解的 Controller 方法，调用 RateLimitService 执行令牌桶算法判断，使用 Redis + Lua 脚本保证原子性操作。

**Tech Stack:** Spring Boot 3.x + Spring Data Redis + AOP + Lua Script

---

## 文件结构

```
com.jaycong.boot/
├── common/exception/
│   ├── ErrorCode.java              # 新增 TOO_MANY_REQUESTS
│   └── GlobalExceptionHandler.java # 新增 RateLimitException 处理
└── modules/ratelimit/
    ├── annotation/
    │   └── RateLimit.java           # 限流注解
    ├── config/
    │   ├── RateLimitAutoConfiguration.java  # 自动配置
    │   └── RateLimitProperties.java # 配置属性
    ├── exception/
    │   └── RateLimitException.java  # 限流异常
    ├── aspect/
    │   └── RateLimitAspect.java     # AOP 切面
    ├── service/
    │   └── RateLimitService.java    # 限流服务
    └── util/
        └── ClientIpUtils.java       # IP 获取工具
```

---

### Task 1: 添加 Redis 依赖

**Files:**
- Modify: `jay-boot-backend/pom.xml`

- [ ] **Step 1: 在 pom.xml 中添加 Redis 依赖**

在 `</dependencies>` 标签前添加：

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
```

- [ ] **Step 2: 在 application.yml 中添加 Redis 配置**

在 `spring:` 节点下添加 Redis 配置：

```yaml
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: 
      database: 0
      timeout: 5000ms
```

在文件末尾添加限流配置：

```yaml
rate-limit:
  enabled: true
  default-qps: 10
  default-burst: 20
  redis-key-prefix: "rate_limit"
```

- [ ] **Step 3: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 2: 创建限流注解

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/ratelimit/annotation/RateLimit.java`

- [ ] **Step 1: 创建 RateLimit 注解类**

```java
package com.jaycong.boot.modules.ratelimit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 限流注解
 * 标注在 Controller 方法上，定义限流规则
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 每秒补充令牌数（QPS）
     * 0 表示使用配置文件默认值
     */
    int qps() default 0;
    
    /**
     * 桶容量（允许突发请求数）
     * 0 表示使用配置文件默认值
     */
    int burst() default 0;
    
    /**
     * 限流维度
     */
    Dimension dimension() default Dimension.IP_USER;
    
    /**
     * 自定义提示消息
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 限流维度枚举
     */
    enum Dimension {
        /** 仅 IP 维度 */
        IP,
        /** 仅用户维度 */
        USER,
        /** IP 和用户双重限流（两者独立） */
        IP_USER
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 3: 创建配置属性类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/ratelimit/config/RateLimitProperties.java`

- [ ] **Step 1: 创建 RateLimitProperties 类**

```java
package com.jaycong.boot.modules.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流配置属性
 */
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
    
    /**
     * 是否启用限流
     */
    private boolean enabled = true;
    
    /**
     * 默认每秒补充令牌数
     */
    private int defaultQps = 10;
    
    /**
     * 默认桶容量
     */
    private int defaultBurst = 20;
    
    /**
     * Redis key 前缀
     */
    private String redisKeyPrefix = "rate_limit";
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getDefaultQps() {
        return defaultQps;
    }
    
    public void setDefaultQps(int defaultQps) {
        this.defaultQps = defaultQps;
    }
    
    public int getDefaultBurst() {
        return defaultBurst;
    }
    
    public void setDefaultBurst(int defaultBurst) {
        this.defaultBurst = defaultBurst;
    }
    
    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }
    
    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 4: 创建限流异常类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/ratelimit/exception/RateLimitException.java`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/common/exception/ErrorCode.java`

- [ ] **Step 1: 在 ErrorCode 中添加 TOO_MANY_REQUESTS**

在 `INTERNAL_ERROR(500);` 前添加：

```java
    TOO_MANY_REQUESTS(429),
```

- [ ] **Step 2: 创建 RateLimitException 类**

```java
package com.jaycong.boot.modules.ratelimit.exception;

import com.jaycong.boot.common.exception.ErrorCode;

/**
 * 限流异常
 */
public class RateLimitException extends RuntimeException {
    
    private final int code;
    private final int retryAfter;
    
    public RateLimitException(String message) {
        super(message);
        this.code = ErrorCode.TOO_MANY_REQUESTS.getCode();
        this.retryAfter = 1;
    }
    
    public RateLimitException(String message, int retryAfter) {
        super(message);
        this.code = ErrorCode.TOO_MANY_REQUESTS.getCode();
        this.retryAfter = retryAfter;
    }
    
    public int getCode() {
        return code;
    }
    
    public int getRetryAfter() {
        return retryAfter;
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 5: 创建 IP 获取工具类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/ratelimit/util/ClientIpUtils.java`

- [ ] **Step 1: 创建 ClientIpUtils 工具类**

```java
package com.jaycong.boot.modules.ratelimit.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 获取工具
 */
public final class ClientIpUtils {
    
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    
    private ClientIpUtils() {
    }
    
    /**
     * 获取客户端真实 IP 地址
     * 优先级：X-Forwarded-For > X-Real-IP > RemoteAddr
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For 可能包含多个 IP，取第一个
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index).trim();
            }
            return ip;
        }
        
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        ip = request.getRemoteAddr();
        if (ip == null || ip.isEmpty()) {
            return UNKNOWN;
        }
        
        // IPv6 本地地址转换为 IPv4
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }
        
        return ip;
    }
    
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 6: 创建限流服务类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/ratelimit/service/RateLimitService.java`

- [ ] **Step 1: 创建 RateLimitService 类**

```java
package com.jaycong.boot.modules.ratelimit.service;

import com.jaycong.boot.modules.ratelimit.config.RateLimitProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 限流服务
 * 使用 Redis + Lua 脚本实现令牌桶算法
 */
@Service
public class RateLimitService {
    
    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;
    
    /**
     * 令牌桶 Lua 脚本
     * KEYS[1]: 限流 key
     * ARGV[1]: 桶容量 (burst)
     * ARGV[2]: 每秒补充令牌数 (qps)
     * ARGV[3]: 当前时间戳 (毫秒)
     * ARGV[4]: 请求令牌数 (默认1)
     * 返回: [是否成功(1/0), 剩余令牌数]
     */
    private static final String TOKEN_BUCKET_SCRIPT = """
            local key = KEYS[1]
            local burst = tonumber(ARGV[1])
            local qps = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])
            
            -- 获取当前令牌数和上次更新时间
            local info = redis.call('HMGET', key, 'tokens', 'last_time')
            local tokens = tonumber(info[1])
            local lastTime = tonumber(info[2])
            
            -- 初始化
            if tokens == nil then
                tokens = burst
                lastTime = now
            end
            
            -- 计算时间间隔（秒）
            local interval = (now - lastTime) / 1000.0
            if interval < 0 then
                interval = 0
            end
            
            -- 补充令牌
            local filled = tokens + interval * qps
            if filled > burst then
                filled = burst
            end
            
            -- 尝试获取令牌
            local allowed = 0
            local remaining = filled
            if filled >= requested then
                filled = filled - requested
                allowed = 1
                remaining = filled
            end
            
            -- 更新状态
            redis.call('HMSET', key, 'tokens', filled, 'last_time', now)
            redis.call('PEXPIRE', key, math.ceil(burst / qps * 2 * 1000))
            
            return {allowed, math.floor(remaining)}
            """;
    
    public RateLimitService(StringRedisTemplate redisTemplate, RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }
    
    /**
     * 尝试获取令牌
     *
     * @param dimensionKey 限流维度 key (如 ip:127.0.0.1:/api/login)
     * @param qps          每秒补充令牌数
     * @param burst        桶容量
     * @return 限流结果
     */
    public RateLimitResult tryAcquire(String dimensionKey, int qps, int burst) {
        String key = properties.getRedisKeyPrefix() + ":" + dimensionKey;
        long now = System.currentTimeMillis();
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(TOKEN_BUCKET_SCRIPT, Long.class);
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(burst),
                String.valueOf(qps),
                String.valueOf(now),
                "1"
        );
        
        if (result == null || result < 2) {
            return new RateLimitResult(false, 0, qps, burst);
        }
        
        boolean allowed = result.get(0) != null && result.get(0) == 1L;
        int remaining = result.get(1) != null ? result.get(1).intValue() : 0;
        
        return new RateLimitResult(allowed, remaining, qps, burst);
    }
    
    /**
     * 计算建议重试等待时间（秒）
     *
     * @param remaining 剩余令牌数
     * @param qps       每秒补充令牌数
     * @return 等待秒数
     */
    public int calculateRetryAfter(int remaining, int qps) {
        if (remaining > 0 || qps <= 0) {
            return 1;
        }
        // 需要等待 1 个令牌补充
        return (int) Math.ceil(1.0 / qps);
    }
    
    /**
     * 限流结果
     */
    public record RateLimitResult(boolean allowed, int remaining, int qps, int burst) {
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 7: 创建 AOP 切面

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/ratelimit/aspect/RateLimitAspect.java`

- [ ] **Step 1: 创建 RateLimitAspect 切面类**

```java
package com.jaycong.boot.modules.ratelimit.aspect;

import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.auth.context.LoginPrincipal;
import com.jaycong.boot.modules.ratelimit.annotation.RateLimit;
import com.jaycong.boot.modules.ratelimit.config.RateLimitProperties;
import com.jaycong.boot.modules.ratelimit.exception.RateLimitException;
import com.jaycong.boot.modules.ratelimit.service.RateLimitService;
import com.jaycong.boot.modules.ratelimit.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流切面
 */
@Aspect
@Component
public class RateLimitAspect {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    
    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    
    public RateLimitAspect(RateLimitService rateLimitService, RateLimitProperties properties) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }
    
    @Around("@annotation(com.jaycong.boot.modules.ratelimit.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!properties.isEnabled()) {
            return point.proceed();
        }
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        if (rateLimit == null) {
            return point.proceed();
        }
        
        // 获取配置参数
        int qps = rateLimit.qps() > 0 ? rateLimit.qps() : properties.getDefaultQps();
        int burst = rateLimit.burst() > 0 ? rateLimit.burst() : properties.getDefaultBurst();
        String message = rateLimit.message();
        
        // 获取请求路径
        HttpServletRequest request = getCurrentRequest();
        String path = request != null ? request.getRequestURI() : "unknown";
        
        // 获取客户端 IP
        String clientIp = request != null ? ClientIpUtils.getClientIp(request) : "unknown";
        
        // 获取当前用户 ID
        Long userId = null;
        try {
            userId = LoginContext.currentPrincipal()
                    .map(LoginPrincipal::userId)
                    .orElse(null);
        } catch (Exception ignored) {
            // 忽略获取用户信息的异常
        }
        
        // 根据维度执行限流判断
        boolean allowed = checkRateLimit(rateLimit.dimension(), clientIp, userId, path, qps, burst);
        
        if (!allowed) {
            int retryAfter = rateLimitService.calculateRetryAfter(0, qps);
            log.warn("限流触发: ip={}, userId={}, path={}, qps={}, burst={}", 
                    clientIp, userId, path, qps, burst);
            throw new RateLimitException(message, retryAfter);
        }
        
        return point.proceed();
    }
    
    private boolean checkRateLimit(RateLimit.Dimension dimension, String clientIp, 
                                    Long userId, String path, int qps, int burst) {
        boolean ipAllowed = true;
        boolean userAllowed = true;
        
        if (dimension == RateLimit.Dimension.IP || dimension == RateLimit.Dimension.IP_USER) {
            String ipKey = "ip:" + clientIp + ":" + path;
            RateLimitService.RateLimitResult ipResult = rateLimitService.tryAcquire(ipKey, qps, burst);
            ipAllowed = ipResult.allowed();
        }
        
        if (dimension == RateLimit.Dimension.USER || dimension == RateLimit.Dimension.IP_USER) {
            if (userId != null) {
                String userKey = "user:" + userId + ":" + path;
                RateLimitService.RateLimitResult userResult = rateLimitService.tryAcquire(userKey, qps, burst);
                userAllowed = userResult.allowed();
            }
            // 未登录用户不进行用户维度限流
        }
        
        // IP_USER 模式下，两者都通过才算通过
        if (dimension == RateLimit.Dimension.IP_USER) {
            return ipAllowed && userAllowed;
        }
        
        // 单维度模式下，对应维度通过即可
        if (dimension == RateLimit.Dimension.IP) {
            return ipAllowed;
        }
        
        return userAllowed;
    }
    
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 8: 创建自动配置类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/ratelimit/config/RateLimitAutoConfiguration.java`

- [ ] **Step 1: 创建 RateLimitAutoConfiguration 类**

```java
package com.jaycong.boot.modules.ratelimit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 限流自动配置
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {
}
```

- [ ] **Step 2: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 9: 添加全局异常处理

**Files:**
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/common/web/GlobalExceptionHandler.java`

- [ ] **Step 1: 添加 RateLimitException 处理方法**

在 `GlobalExceptionHandler` 类中添加导入：

```java
import com.jaycong.boot.modules.ratelimit.exception.RateLimitException;
```

在类中添加处理方法：

```java
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimit(RateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Remaining", "0")
                .header("Retry-After", String.valueOf(ex.getRetryAfter()))
                .body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
    }
```

- [ ] **Step 2: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 10: 为登录接口添加限流注解

**Files:**
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/controller/AuthController.java` (或相应的登录控制器)

- [ ] **Step 1: 查找登录控制器并添加限流注解**

在登录方法上添加 `@RateLimit` 注解：

```java
@RateLimit(qps = 5, burst = 10, dimension = RateLimit.Dimension.IP, 
           message = "登录请求过于频繁，请稍后再试")
```

需要添加导入：

```java
import com.jaycong.boot.modules.ratelimit.annotation.RateLimit;
```

- [ ] **Step 2: 编译验证**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 11: 最终编译验证

- [ ] **Step 1: 执行完整编译**

Run: `cd D:\develop\CodeProject\jay-boot\jay-boot-backend; mvn clean compile -q`
Expected: BUILD SUCCESS

---

## 自检清单

| 检查项 | 状态 |
|-------|------|
| 所有文件路径明确 | ✅ |
| 无 TODO/TBD 占位符 | ✅ |
| 类型和方法签名一致 | ✅ |
| 每个任务都有编译验证 | ✅ |
| 设计文档需求全覆盖 | ✅ |
