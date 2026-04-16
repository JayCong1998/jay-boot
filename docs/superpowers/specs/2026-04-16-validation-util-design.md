# 参数校验工具设计文档

## 概述

创建统一的参数校验工具类 `ValidateUtil`，减少重复的 `if (xxx == null) throw ...` 代码，提供静态方法和链式调用两种使用方式。

## 目标

- 减少重复的参数校验代码
- 提供简洁、一致的 API
- 与现有 `BusinessException` 体系无缝集成
- 保持轻量，不引入过度设计

## 设计方案

### 类结构

```
com.jaycong.boot.common.util
└── ValidateUtil.java
```

### 核心方法

#### 静态方法（单次校验）

| 方法签名 | 说明 |
|---------|------|
| `notNull(Object obj, String message)` | 对象不能为 null |
| `notBlank(String str, String message)` | 字符串不能为 null/空/纯空白 |
| `notEmpty(String str, String message)` | 字符串不能为 null 或空 |
| `notEmpty(Collection<?> collection, String message)` | 集合不能为 null 或空 |
| `isTrue(boolean condition, String message)` | 条件必须为 true |
| `isFalse(boolean condition, String message)` | 条件必须为 false |
| `minLength(String str, int min, String message)` | 字符串最小长度 |
| `maxLength(String str, int max, String message)` | 字符串最大长度 |
| `range(int value, int min, int max, String message)` | 数值范围校验 |
| `range(long value, long min, long max, String message)` | 数值范围校验 |
| `email(String email, String message)` | 邮箱格式校验 |
| `matches(String str, String regex, String message)` | 正则匹配校验 |

#### 链式调用

```java
ValidateUtil.chain()
    .notNull(user, "用户不存在")
    .isTrue(user.isActive(), "用户已被禁用")
    .notBlank(user.getEmail(), "用户邮箱不能为空");
```

### 异常处理

所有校验失败统一抛出 `BusinessException`：

```java
throw new BusinessException(ErrorCode.BAD_REQUEST, message);
```

无需修改 `GlobalExceptionHandler`，现有逻辑已支持。

### 使用示例

**改造前**：
```java
if (user == null) {
    throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
}
if (!user.isActive()) {
    throw new BusinessException(ErrorCode.FORBIDDEN, "用户已被禁用");
}
if (StringUtils.isEmpty(user.getEmail())) {
    throw new BusinessException(ErrorCode.BAD_REQUEST, "用户邮箱不能为空");
}
```

**改造后（单独调用）**：
```java
ValidateUtil.notNull(user, "用户不存在");
ValidateUtil.isTrue(user.isActive(), "用户已被禁用");
ValidateUtil.notBlank(user.getEmail(), "用户邮箱不能为空");
```

**改造后（链式调用）**：
```java
ValidateUtil.chain()
    .notNull(user, "用户不存在")
    .isTrue(user.isActive(), "用户已被禁用")
    .notBlank(user.getEmail(), "用户邮箱不能为空");
```

## 实现细节

### ValidateUtil 类设计

```java
public final class ValidateUtil {

    // 私有构造，防止实例化
    private ValidateUtil() {}

    // 静态校验方法
    public static void notNull(Object obj, String message) { ... }
    public static void notBlank(String str, String message) { ... }
    // ... 其他静态方法

    // 创建链式校验器
    public static Chain chain() {
        return new Chain();
    }

    // 链式校验器内部类
    public static final class Chain {
        public Chain notNull(Object obj, String message) { ... }
        public Chain notBlank(String str, String message) { ... }
        // ... 其他链式方法
    }
}
```

## 实施范围

仅新增工具类，不修改现有代码。后续开发可逐步采用新工具。

## 文件清单

| 文件 | 操作 |
|-----|------|
| `jay-boot-backend/src/main/java/com/jaycong/boot/common/util/ValidateUtil.java` | 新增 |
