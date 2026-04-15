# 请求日志与异常日志管理功能设计文档

## 一、概述

### 1.1 功能目标

开发请求日志管理和异常错误日志管理两个功能模块，方便后台管理员清晰查看每个接口的执行情况，以及排查报错堆栈信息。

### 1.2 核心需求

- **请求日志**：记录所有接口请求的详细信息，包括请求路径、方法、参数、响应状态、耗时、客户端信息等
- **异常日志**：记录系统运行时的异常错误，包含完整堆栈信息，便于问题排查
- **松散关联**：异常日志通过 request_id 与请求日志关联，可追溯查看

---

## 二、技术决策

### 2.1 存储方式

- **数据库存储**：使用 MySQL 存储日志数据
- **条数限制**：按条数限制保留数据，超出时自动清理最旧记录
- **可配置上限**：请求日志默认保留 100000 条，异常日志默认保留 50000 条

### 2.2 实现方案

采用 **AOP + HandlerInterceptor 组合** 方案：

| 组件 | 职责 |
|------|------|
| `RequestContext` | ThreadLocal 存储请求 ID、开始时间、用户信息等上下文 |
| `RequestLogInterceptor` | 请求进入时初始化上下文，生成 request_id |
| `RequestLogAspect` | 方法执行后记录请求日志，计算耗时 |
| `ErrorLogHandler` | 在 GlobalExceptionHandler 中调用，记录异常日志 |

### 2.3 日志关联

- 请求日志和异常日志通过 `request_id` 字段进行松散关联
- 异常日志记录时不存储 `request_log_id`，仅通过 `request_id` 进行关联查询

---

## 三、数据库设计

### 3.1 请求日志表 `request_logs`

```sql
CREATE TABLE IF NOT EXISTS request_logs (
    id BIGINT PRIMARY KEY,
    request_id VARCHAR(32) NOT NULL COMMENT '请求唯一标识',
    user_id BIGINT NULL COMMENT '操作用户ID',
    username VARCHAR(64) NULL COMMENT '操作用户名',
    method VARCHAR(8) NOT NULL COMMENT '请求方法',
    path VARCHAR(255) NOT NULL COMMENT '请求路径',
    query_string TEXT NULL COMMENT '查询参数',
    request_params TEXT NULL COMMENT '请求参数(JSON)',
    status_code INT NOT NULL COMMENT 'HTTP响应状态码',
    duration_ms INT NOT NULL COMMENT '执行耗时(毫秒)',
    client_ip VARCHAR(64) NULL COMMENT '客户端IP',
    user_agent VARCHAR(500) NULL COMMENT '浏览器标识',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_request_logs_time (created_time),
    KEY idx_request_logs_user (user_id),
    KEY idx_request_logs_path (path),
    KEY idx_request_logs_status (status_code),
    KEY idx_request_logs_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求日志表';
```

### 3.2 异常日志表 `error_logs`

```sql
CREATE TABLE IF NOT EXISTS error_logs (
    id BIGINT PRIMARY KEY,
    request_id VARCHAR(32) NULL COMMENT '请求唯一标识',
    user_id BIGINT NULL COMMENT '操作用户ID',
    username VARCHAR(64) NULL COMMENT '操作用户名',
    request_path VARCHAR(255) NULL COMMENT '请求路径',
    request_params TEXT NULL COMMENT '请求参数(JSON)',
    client_ip VARCHAR(64) NULL COMMENT '客户端IP',
    exception_class VARCHAR(255) NOT NULL COMMENT '异常类名',
    exception_message TEXT NULL COMMENT '异常消息',
    stack_trace MEDIUMTEXT NULL COMMENT '完整堆栈信息',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_error_logs_time (created_time),
    KEY idx_error_logs_user (user_id),
    KEY idx_error_logs_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常日志表';
```

---

## 四、后端架构设计

### 4.1 模块结构

```
com.jaycong.boot.modules.log/
├── config/
│   └── LogAutoConfiguration.java          # 日志模块自动配置
├── context/
│   └── RequestContext.java                # 请求上下文(ThreadLocal)
├── entity/
│   ├── RequestLogEntity.java              # 请求日志实体
│   └── ErrorLogEntity.java                # 异常日志实体
├── mapper/
│   ├── RequestLogMapper.java
│   └── ErrorLogMapper.java
├── service/
│   ├── RequestLogService.java             # 请求日志服务
│   ├── ErrorLogService.java               # 异常日志服务
│   └── impl/
│       ├── RequestLogServiceImpl.java
│       └── ErrorLogServiceImpl.java
├── interceptor/
│   └── RequestLogInterceptor.java         # 请求日志拦截器
├── aspect/
│   └── RequestLogAspect.java              # 请求日志切面
├── handler/
│   └── ErrorLogHandler.java               # 异常日志处理器
└── dto/
    ├── RequestLogQueryDTO.java            # 查询条件DTO
    ├── RequestLogVO.java                  # 请求日志VO
    └── ErrorLogVO.java                    # 异常日志VO
```

### 4.2 数据流程

```
请求进入 → RequestLogInterceptor.preHandle()
    ↓
    初始化 RequestContext（生成 request_id，记录开始时间）
    ↓
Controller 方法执行 → RequestLogAspect.around()
    ↓
    记录请求日志到数据库
    ↓
    [发生异常] → GlobalExceptionHandler → ErrorLogHandler
        ↓
        记录异常日志（关联 request_id）
    ↓
请求结束 → RequestLogInterceptor.afterCompletion()
    ↓
    清理 RequestContext
```

### 4.3 条数限制清理策略

- 每次写入日志时检查当前条数
- 若超过配置上限，删除最旧的记录
- 上限值通过配置文件配置：
  - `app.log.request.max-count`: 请求日志最大条数，默认 100000
  - `app.log.error.max-count`: 异常日志最大条数，默认 50000

---

## 五、API 接口设计

### 5.1 请求日志接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/logs/requests` | 分页查询请求日志列表 |
| GET | `/admin/logs/requests/{id}` | 获取请求日志详情 |
| DELETE | `/admin/logs/requests/{id}` | 删除单条请求日志 |
| POST | `/admin/logs/requests/batch-delete` | 批量删除请求日志 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 关键词（路径模糊匹配） |
| method | String | 否 | 请求方法 |
| statusCode | Integer | 否 | 响应状态码 |
| userId | Long | 否 | 用户ID |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 20 |

### 5.2 异常日志接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/logs/errors` | 分页查询异常日志列表 |
| GET | `/admin/logs/errors/{id}` | 获取异常日志详情 |
| DELETE | `/admin/logs/errors/{id}` | 删除单条异常日志 |
| POST | `/admin/logs/errors/batch-delete` | 批量删除异常日志 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 关键词（异常类名/消息模糊匹配） |
| requestPath | String | 否 | 请求路径 |
| userId | Long | 否 | 用户ID |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 20 |

---

## 六、前端界面设计

### 6.1 页面规划

在管理后台新增两个页面：
- **请求日志管理** - `/admin/logs/requests`
- **异常日志管理** - `/admin/logs/errors`

### 6.2 请求日志管理页面

**功能点：**
- 列表展示：请求ID、路径、方法、状态码、耗时、用户、时间
- 筛选查询：关键词、请求方法、状态码、用户ID、时间范围
- 详情弹窗：展示完整请求信息（参数、IP、User-Agent等）
- 删除操作：单条删除、批量删除（带确认）

### 6.3 异常日志管理页面

**功能点：**
- 列表展示：请求ID、请求路径、异常类型、异常消息、用户、时间
- 筛选查询：关键词（异常类名/消息）、请求路径、用户ID、时间范围
- 详情弹窗：展示完整堆栈信息、请求参数、客户端IP
- 删除操作：单条删除、批量删除（带确认）
- 关联跳转：可通过请求ID跳转到对应的请求日志详情

### 6.4 菜单集成

在管理后台侧边栏新增「日志管理」菜单组：

```
📊 数据统计
   └── Dashboard
📝 日志管理          ← 新增
   ├── 请求日志      ← 新增
   └── 异常日志      ← 新增
⚙️ 系统设置
   └── 用户管理
```

---

## 七、文件清单

### 7.1 后端新增文件

```
jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/
├── config/LogAutoConfiguration.java
├── context/RequestContext.java
├── entity/RequestLogEntity.java
├── entity/ErrorLogEntity.java
├── mapper/RequestLogMapper.java
├── mapper/ErrorLogMapper.java
├── service/RequestLogService.java
├── service/ErrorLogService.java
├── service/impl/RequestLogServiceImpl.java
├── service/impl/ErrorLogServiceImpl.java
├── interceptor/RequestLogInterceptor.java
├── aspect/RequestLogAspect.java
├── handler/ErrorLogHandler.java
└── dto/
    ├── RequestLogQueryDTO.java
    ├── ErrorLogQueryDTO.java
    ├── RequestLogVO.java
    └── ErrorLogVO.java

jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/log/
├── RequestLogController.java
└── ErrorLogController.java
```

### 7.2 后端修改文件

```
jay-boot-backend/src/main/java/com/jaycong/boot/common/web/GlobalExceptionHandler.java
```
- 在异常处理方法中调用 ErrorLogHandler 记录异常日志

### 7.3 前端新增文件

```
jay-boot-frontend/src/views/admin/RequestLogView.vue
jay-boot-frontend/src/views/admin/ErrorLogView.vue
jay-boot-frontend/src/api/admin/LogApi.ts
jay-boot-frontend/src/stores/admin/log.ts
```

### 7.4 前端修改文件

```
jay-boot-frontend/src/router/index.ts
```
- 新增请求日志和异常日志页面的路由配置

```
jay-boot-frontend/src/layouts/AdminLayout.vue
```
- 新增日志管理菜单项

---

## 八、配置项

```yaml
# application.yml
app:
  log:
    request:
      enabled: true
      max-count: 100000
    error:
      enabled: true
      max-count: 50000
```
