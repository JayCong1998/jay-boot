# 统一操作日志模块设计

## 1. 背景与目标

### 1.1 背景

当前系统已有三类日志：
- **请求日志** (`request_logs`)：记录 HTTP 请求的技术信息（路径、参数、耗时、状态码）
- **异常日志** (`error_logs`)：记录异常堆栈和错误上下文
- **登录日志** (`login_logs`)：记录用户登录/登出行为

这些日志都是**技术维度**的记录，无法回答"谁在什么时间对什么数据做了什么业务操作"这样的审计问题。

### 1.2 目标

构建**业务维度**的操作日志模块：
1. 记录关键业务操作的审计轨迹
2. 支持按模块、操作者、时间范围查询
3. 提供操作详情追溯能力

### 1.3 约束

1. 采用注解 + AOP 切面实现，开发者声明式指定需要记录的操作
2. 日志仅存储到数据库，不引入异步机制
3. 前端提供基础查询 + 详情查看功能
4. 遵循项目现有技术规范（MyBatis-Plus、统一响应格式等）

## 2. 整体设计

### 2.1 技术方案

采用 **注解 + AOP 切面** 方案：
- 开发者在需要记录的方法上添加 `@OperationLog` 注解
- AOP 切面自动捕获操作上下文并持久化

### 2.2 核心流程

```
Controller/Service 方法调用
         ↓
    AOP 切面拦截
         ↓
 解析注解属性 + SpEL 表达式
         ↓
    获取登录上下文
         ↓
   构建日志实体并保存
         ↓
    继续执行业务方法
```

## 3. 数据库设计

### 3.1 操作日志表 `operation_logs`

```sql
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY,
    module VARCHAR(64) NOT NULL COMMENT '模块名称',
    action VARCHAR(64) NOT NULL COMMENT '操作类型',
    detail TEXT NULL COMMENT '操作详情',
    user_id BIGINT NULL COMMENT '操作用户ID',
    username VARCHAR(64) NULL COMMENT '操作用户名',
    client_ip VARCHAR(64) NULL COMMENT '客户端IP',
    request_id VARCHAR(32) NULL COMMENT '关联请求ID',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_operation_logs_time (created_time),
    KEY idx_operation_logs_user (user_id),
    KEY idx_operation_logs_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

### 3.2 字段说明

| 字段 | 类型 | 说明 |
|-----|------|-----|
| id | BIGINT | 主键，雪花算法生成 |
| module | VARCHAR(64) | 模块名称，如"用户管理"、"套餐管理" |
| action | VARCHAR(64) | 操作类型，如"创建"、"删除"、"修改价格" |
| detail | TEXT | 操作详情，支持 SpEL 模板生成的动态内容 |
| user_id | BIGINT | 操作用户ID，从登录上下文获取 |
| username | VARCHAR(64) | 操作用户名，从登录上下文获取 |
| client_ip | VARCHAR(64) | 客户端IP，从请求上下文获取 |
| request_id | VARCHAR(32) | 关联请求ID，便于与请求日志关联 |

## 4. 后端设计

### 4.1 模块结构

```
com.jaycong.boot.modules.log/
├── annotation/
│   └── OperationLog.java              # 操作日志注解
├── aspect/
│   └── OperationLogAspect.java        # AOP切面
├── entity/
│   └── OperationLogEntity.java        # 操作日志实体
├── mapper/
│   └── OperationLogMapper.java        # 数据访问层
├── service/
│   └── OperationLogService.java       # 业务服务层
├── controller/
│   └── AdminOperationLogController.java  # 管理端控制器
└── dto/
    ├── OperationLogQueryDTO.java      # 查询条件DTO
    └── OperationLogVO.java            # 响应VO
```

### 4.2 注解定义

```java
package com.jaycong.boot.modules.log.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 模块名称
     */
    String module();

    /**
     * 操作类型
     */
    String action();

    /**
     * 操作详情模板（支持 SpEL 表达式）
     * 示例: "删除用户：#{#username}"
     */
    String detail() default "";
}
```

### 4.3 AOP 切面设计

```java
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final RequestContext requestContext;
    private final LoginContext loginContext;

    /**
     * 切点：标注了 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.jaycong.boot.modules.log.annotation.OperationLog)")
    public void operationLogPointcut() {}

    /**
     * 环绕通知：在方法执行后记录日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 先执行业务方法
        Object result = point.proceed();

        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            OperationLog annotation = method.getAnnotation(OperationLog.class);

            // 解析 SpEL 表达式
            String detail = parseSpel(annotation.detail(), point);

            // 构建并保存日志
            saveLog(annotation, detail);
        } catch (Exception e) {
            // 日志记录失败不影响业务
            log.warn("操作日志记录失败", e);
        }

        return result;
    }
}
```

### 4.4 SpEL 表达式解析

支持从方法参数中提取值：
- `#paramName` - 引用方法参数
- `#request.email` - 引用对象属性
- `#result` - 引用返回值（可选支持）

示例：
```java
@OperationLog(module = "用户管理", action = "创建", detail = "创建用户：#{#request.email}")
public Long createUser(CreateUserRequest request) { ... }
```

### 4.5 API 接口

#### 4.5.1 分页查询

**接口地址**：`GET /api/admin/operation-logs`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页数量，默认 20 |
| module | string | 否 | 模块名称筛选 |
| userId | long | 否 | 操作用户ID筛选 |
| startTime | string | 否 | 开始时间 (yyyy-MM-dd HH:mm:ss) |
| endTime | string | 否 | 结束时间 (yyyy-MM-dd HH:mm:ss) |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "body": {
    "total": 100,
    "list": [
      {
        "id": "123456789",
        "module": "用户管理",
        "action": "删除",
        "detail": "删除用户ID：456",
        "userId": 1,
        "username": "admin",
        "clientIp": "127.0.0.1",
        "createdTime": "2026-04-16 10:30:00"
      }
    ]
  }
}
```

#### 4.5.2 查看详情

**接口地址**：`GET /api/admin/operation-logs/{id}`

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "body": {
    "id": "123456789",
    "module": "用户管理",
    "action": "删除",
    "detail": "删除用户ID：456",
    "userId": 1,
    "username": "admin",
    "clientIp": "127.0.0.1",
    "requestId": "abc123",
    "createdTime": "2026-04-16 10:30:00"
  }
}
```

## 5. 前端设计

### 5.1 页面位置

管理后台：`/admin/operation-logs`

### 5.2 功能列表

1. **列表展示**：表格展示操作日志记录
2. **筛选条件**：
   - 时间范围（日期选择器）
   - 模块（下拉选择）
   - 操作用户（输入框）
3. **详情查看**：点击行弹出详情弹窗

### 5.3 页面布局

```
┌─────────────────────────────────────────────────────┐
│ 操作日志                                              │
├─────────────────────────────────────────────────────┤
│ [时间范围] [模块▼] [操作用户] [查询] [重置]            │
├─────────────────────────────────────────────────────┤
│ 模块       │ 操作   │ 详情        │ 操作者  │ 时间     │
│───────────│────────│────────────│────────│─────────│
│ 用户管理   │ 删除   │ 删除用户ID:456 │ admin │ 04-16 10:30│
│ 套餐管理   │ 修改   │ 价格改为99元   │ admin │ 04-16 09:15│
└─────────────────────────────────────────────────────┘
│ 共 100 条  < 1 2 3 ... >                            │
└─────────────────────────────────────────────────────┘
```

## 6. 使用示例

### 6.1 用户管理模块

```java
@Service
public class AdminUserService {

    @OperationLog(module = "用户管理", action = "创建", detail = "创建用户：#{#request.email}")
    public Long createUser(CreateUserRequest request) { ... }

    @OperationLog(module = "用户管理", action = "删除", detail = "删除用户ID：#{#userId}")
    public void deleteUser(Long userId) { ... }

    @OperationLog(module = "用户管理", action = "修改状态", 
                  detail = "用户#{#userId}状态改为#{#status}")
    public void updateUserStatus(Long userId, String status) { ... }
}
```

### 6.2 套餐管理模块

```java
@Service
public class AdminPlanService {

    @OperationLog(module = "套餐管理", action = "创建", detail = "创建套餐：#{#request.name}")
    public Long createPlan(CreatePlanRequest request) { ... }

    @OperationLog(module = "套餐管理", action = "修改价格", 
                  detail = "套餐#{#planId}价格改为#{#price}分")
    public void updatePlanPrice(Long planId, Long price) { ... }

    @OperationLog(module = "套餐管理", action = "删除", detail = "删除套餐ID：#{#planId}")
    public void deletePlan(Long planId) { ... }
}
```

### 6.3 权限管理模块

```java
@Service
public class AdminRbacService {

    @OperationLog(module = "权限管理", action = "分配角色", 
                  detail = "为用户#{#userId}分配角色#{#roleId}")
    public void assignRole(Long userId, Long roleId) { ... }

    @OperationLog(module = "权限管理", action = "移除角色", 
                  detail = "移除用户#{#userId}的角色#{#roleId}")
    public void removeRole(Long userId, Long roleId) { ... }
}
```

## 7. 非目标

本次不包含：
1. 异步日志写入机制
2. 日志导出功能
3. 日志统计图表
4. 日志自动清理策略

## 8. 风险与控制

| 风险 | 控制措施 |
|-----|---------|
| 日志记录失败影响业务 | 切面中 try-catch，失败仅记录警告日志 |
| SpEL 表达式解析异常 | 捕获异常后使用原始模板字符串 |
| 日志表数据量增长 | 后续可添加定时清理任务 |

## 9. 验证策略

1. **编译验证**：`mvn -DskipTests compile`
2. **功能验证**：
   - 在关键业务方法上添加注解
   - 执行业务操作，检查日志表是否有记录
   - 前端页面查询和详情功能正常
