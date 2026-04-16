# API 限流功能设计文档

## 1. 功能概述

实现基于 IP 和用户维度的 API 请求频率限制，采用令牌桶算法 + Redis 存储，防止恶意攻击、资源滥用和意外过载。

## 2. 解决的问题

| 问题类型 | 具体场景 | 危害 |
|---------|---------|------|
| 恶意攻击 | DDoS、暴力破解密码、短信轰炸 | 系统瘫痪、数据泄露 |
| 资源滥用 | 恶意刷接口、爬虫过度抓取 | 服务器资源耗尽 |
| 意外过载 | 客户端 bug 导致循环请求 | 服务不可用 |
| 成本失控 | AI 接口按 token 计费被滥用 | 巨额账单 |

## 3. 技术选型

- **存储方式**: Redis（高性能，支持分布式）
- **限流算法**: 令牌桶（允许突发流量，平滑限流）
- **限流维度**: IP 维度 + 用户维度
- **配置方式**: 注解 + YAML 配置文件

## 4. 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                        Controller                           │
│  @RateLimit(qps=5, burst=10, dimension=IP_USER)            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   RateLimitAspect (AOP切面)                 │
│  1. 解析 @RateLimit 注解                                    │
│  2. 提取限流维度 (IP/用户ID)                                │
│  3. 调用 RateLimitService 执行限流判断                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   RateLimitService                          │
│  令牌桶算法实现 (Redis + Lua 脚本)                          │
│  - key: rate_limit:{dimension}:{value}:{endpoint}          │
│  - 原子性操作：获取令牌、计算剩余、返回结果                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        Redis                                │
│  存储令牌桶状态：tokens、last_refill_time                   │
└─────────────────────────────────────────────────────────────┘
```

## 5. 核心组件

### 5.1 @RateLimit 注解

标注在 Controller 方法上，定义限流规则。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /** 每秒补充令牌数（QPS） */
    int qps() default 0;  // 0 表示使用配置文件默认值
    
    /** 桶容量（允许突发请求数） */
    int burst() default 0;  // 0 表示使用配置文件默认值
    
    /** 限流维度 */
    Dimension dimension() default Dimension.IP_USER;
    
    /** 自定义提示消息 */
    String message() default "请求过于频繁，请稍后再试";
    
    enum Dimension {
        IP,        // 仅 IP 维度
        USER,      // 仅用户维度
        IP_USER    // IP 和用户双重限流（两者独立）
    }
}
```

### 5.2 RateLimitProperties 配置类

映射 YAML 配置，定义默认限流参数。

```java
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
    /** 是否启用限流 */
    private boolean enabled = true;
    
    /** 默认每秒补充令牌数 */
    private int defaultQps = 10;
    
    /** 默认桶容量 */
    private int defaultBurst = 20;
    
    /** Redis key 前缀 */
    private String redisKeyPrefix = "rate_limit";
}
```

### 5.3 RateLimitService 服务类

令牌桶核心逻辑，使用 Redis + Lua 脚本实现原子性操作。

**Lua 脚本逻辑**：
1. 获取当前令牌数和上次补充时间
2. 根据时间差计算应补充的令牌数
3. 尝试获取 1 个令牌
4. 返回是否成功 + 剩余令牌数

### 5.4 RateLimitAspect 切面

AOP 切面，拦截标注了 `@RateLimit` 的方法。

**处理流程**：
1. 解析注解参数（qps、burst、dimension）
2. 获取客户端 IP（从请求头或 RemoteAddr）
3. 获取当前用户 ID（从 LoginContext）
4. 根据维度调用 RateLimitService 判断
5. 通过则继续执行，不通过则抛出 RateLimitException

### 5.5 RateLimitException 异常类

限流异常，被全局异常处理器捕获后返回 HTTP 429。

## 6. 配置文件

**application.yml**:
```yaml
rate-limit:
  enabled: true
  default-qps: 10
  default-burst: 20
  redis-key-prefix: "rate_limit"
```

## 7. 限流维度与 Key 设计

| 维度 | Key 格式 | 说明 |
|------|---------|------|
| IP | `rate_limit:ip:{ip}:{path}` | 按客户端 IP 限流 |
| USER | `rate_limit:user:{userId}:{path}` | 按登录用户 ID 限流 |
| IP_USER | 两者独立 | IP 和用户双重限流，任一触发即拒绝 |

**IP 获取优先级**：
1. `X-Forwarded-For` 第一个 IP
2. `X-Real-IP`
3. `request.getRemoteAddr()`

## 8. 响应格式

触发限流时返回：

```json
HTTP/1.1 429 Too Many Requests
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": null
}
```

响应头包含：
- `X-RateLimit-Limit`: 限流上限（QPS）
- `X-RateLimit-Remaining`: 剩余令牌数
- `Retry-After`: 建议重试等待秒数

## 9. 使用示例

### 9.1 登录接口限流（防暴力破解）

```java
@RateLimit(qps = 5, burst = 10, dimension = Dimension.IP, 
           message = "登录请求过于频繁，请稍后再试")
@PostMapping("/auth/login")
public ApiResponse<?> login(@RequestBody LoginRequest request) {
    // ...
}
```

### 9.2 敏感操作限流

```java
@RateLimit(qps = 3, burst = 5, dimension = Dimension.USER)
@PostMapping("/password/change")
public ApiResponse<?> changePassword(@RequestBody ChangePasswordRequest request) {
    // ...
}
```

### 9.3 使用默认配置

```java
@RateLimit  // 使用 YAML 配置的默认值
@GetMapping("/api/data")
public ApiResponse<?> getData() {
    // ...
}
```

## 10. 文件结构

```
com.jaycong.boot.modules.ratelimit/
├── annotation/
│   └── RateLimit.java              # 限流注解
├── config/
│   ├── RateLimitAutoConfiguration.java  # 自动配置
│   └── RateLimitProperties.java    # 配置属性
├── exception/
│   └── RateLimitException.java     # 限流异常
├── aspect/
│   └── RateLimitAspect.java        # AOP 切面
├── service/
│   └── RateLimitService.java       # 限流服务
└── util/
    └── ClientIpUtils.java          # IP 获取工具
```

## 11. 依赖变更

**pom.xml 新增依赖**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 12. 测试策略

1. **单元测试**: RateLimitService 令牌桶逻辑
2. **集成测试**: 标注注解的 Controller 限流效果
3. **边界测试**: 并发请求、令牌耗尽、时间窗口边界

## 13. 后续扩展

- v1.1: 支持租户维度限流
- v1.1: 后台管理界面动态调整限流配置
- v1.2: 限流统计与监控告警
