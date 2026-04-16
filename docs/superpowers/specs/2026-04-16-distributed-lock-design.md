# 分布式锁模块设计文档

## 一、功能概述

基于 Redisson 框架实现声明式分布式锁，通过自定义注解 `@DistributedLock` 实现方法级别的分布式互斥控制。

### 1.1 解决的问题

| 问题类型 | 具体场景 | 不加锁的后果 |
|---------|---------|------------|
| 超卖问题 | 秒杀、抢购、库存扣减 | 库存变为负数，数据不一致 |
| 重复操作 | 支付、转账、订单创建 | 重复扣款、重复发货 |
| 定时任务 | 定时批处理、数据同步 | 多实例重复执行 |
| 资源竞争 | 账户余额、积分操作 | 数据覆盖、计算错误 |
| 幂等性 | 接口重复调用 | 重复处理相同请求 |

### 1.2 与 @RateLimit 的区别

- `@RateLimit`：限流 → 控制请求频率（QPS），允许通过但限制速率
- `@DistributedLock`：互斥 → 控制并发执行，同一时刻只有一个能执行

---

## 二、模块结构

```
modules/lock/
├── annotation/
│   └── DistributedLock.java      # 分布式锁注解
├── aspect/
│   └── DistributedLockAspect.java # AOP 切面处理
├── config/
│   ├── RedissonConfig.java       # Redisson 客户端配置
│   └── DistributedLockProperties.java # 配置属性类
├── exception/
│   └── DistributedLockException.java # 锁获取失败异常
└── util/
    └── SpelKeyParser.java        # SpEL 表达式解析器
```

---

## 三、注解设计

### 3.1 @DistributedLock 注解

```java
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

### 3.2 配置属性类

```java
@ConfigurationProperties(prefix = "distributed-lock")
@Data
public class DistributedLockProperties {
    
    /** 是否启用分布式锁 */
    private boolean enabled = true;
    
    /** 锁 key 前缀 */
    private String keyPrefix = "lock";
    
    /** 默认等待时间（毫秒） */
    private long defaultWaitTime = 0;
    
    /** 默认持有时间（毫秒），-1 表示自动续期 */
    private long defaultLeaseTime = -1;
}
```

---

## 四、核心实现

### 4.1 Redisson 配置

复用项目中已有的 Redis 配置（`spring.data.redis.*`），创建 RedissonClient Bean。

### 4.2 AOP 切面逻辑

1. 解析 SpEL 表达式获取实际 key
2. 拼接完整 key：`keyPrefix + ":" + parsedKey`
3. 根据 lockType 获取对应类型的锁对象
4. 尝试获取锁（waitTime + leaseTime）
5. 执行业务方法
6. 释放锁（仅当前线程持有）

### 4.3 SpEL 表达式解析

支持完整 SpEL 表达式，包括：
- 参数引用：`#orderId`
- 属性访问：`#request.userId`
- 方法调用：`T(String).valueOf(#id)`
- 字符串拼接：`'order:' + #orderId`

---

## 五、使用示例

### 5.1 基础用法

```java
// 秒杀扣库存 - 立即失败模式
@PostMapping("/seckill/{productId}")
@DistributedLock(key = "'seckill:' + #productId")
public ApiResponse<Void> seckill(@PathVariable Long productId) {
    seckillService.decreaseStock(productId);
    return ApiResponse.success(null);
}

// 订单支付 - 等待超时模式
@PostMapping("/pay/{orderId}")
@DistributedLock(key = "'order:pay:' + #orderId", waitTime = 3000, message = "订单正在处理中")
public ApiResponse<Void> payOrder(@PathVariable Long orderId) {
    orderService.pay(orderId);
    return ApiResponse.success(null);
}

// 用户积分操作
@PostMapping("/points/use")
@DistributedLock(key = "'user:points:' + #request.userId")
public ApiResponse<Void> usePoints(@RequestBody UsePointsRequest request) {
    pointsService.usePoints(request.getUserId(), request.getAmount());
    return ApiResponse.success(null);
}
```

### 5.2 定时任务互斥

```java
@Scheduled(cron = "0 0 2 * * ?")
@DistributedLock(key = "'task:daily-batch'", leaseTime = 3600000)
public void dailyBatchProcess() {
    // 多实例部署时只有一个能执行
}
```

### 5.3 读写锁场景

```java
// 缓存读取 - 读锁（多读可并发）
@DistributedLock(key = "'cache:' + #key", lockType = LockType.READ)
public Object readCache(String key) {
    return cacheService.get(key);
}

// 缓存写入 - 写锁（写互斥）
@DistributedLock(key = "'cache:' + #key", lockType = LockType.WRITE)
public void writeCache(String key, Object value) {
    cacheService.set(key, value);
}
```

---

## 六、配置示例

```yaml
distributed-lock:
  enabled: true
  key-prefix: "jay_lock"
  default-wait-time: 0
  default-lease-time: -1
```

---

## 七、依赖变更

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.0</version>
</dependency>
```

---

## 八、异常处理

分布式锁获取失败时抛出 `DistributedLockException`，由全局异常处理器统一处理，返回 429 状态码。

---

## 九、设计决策

| 决策点 | 选择 | 理由 |
|-------|------|------|
| 等待策略 | waitTime 参数控制 | 灵活适应立即失败和等待超时两种场景 |
| 锁类型 | 全部支持 | 可重入、公平、读写锁按需选用 |
| Key 表达式 | 完整 SpEL | 支持复杂参数场景 |
| Key 前缀 | 配置化 | 便于区分不同环境/应用 |
