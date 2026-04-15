# 请求日志与异常日志管理功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现请求日志和异常日志管理功能，支持记录接口执行情况和异常堆栈信息。

**Architecture:** 采用 AOP + HandlerInterceptor 组合方案，通过 RequestLogInterceptor 初始化请求上下文，RequestLogAspect 记录请求日志，ErrorLogHandler 在 GlobalExceptionHandler 中记录异常日志。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, Vue 3, Ant Design Vue, Pinia

---

## Task 1: 数据库表结构创建

**Files:**
- Modify: `database/schema.sql`

- [ ] **Step 1: 在 schema.sql 中添加请求日志表和异常日志表**

在 `database/schema.sql` 文件末尾添加以下内容：

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

- [ ] **Step 2: 执行数据库脚本**

连接 MySQL 数据库，执行上述 SQL 创建表结构。

---

## Task 2: 后端日志模块基础设施

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/context/RequestContext.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/config/LogProperties.java`

- [ ] **Step 1: 创建请求上下文类 RequestContext.java**

```java
package com.jaycong.boot.modules.log.context;

/**
 * 请求上下文，用于存储当前请求的相关信息
 */
public final class RequestContext {

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    private final String requestId;
    private final long startTimeMillis;
    private final Long userId;
    private final String username;

    private RequestContext(String requestId, long startTimeMillis, Long userId, String username) {
        this.requestId = requestId;
        this.startTimeMillis = startTimeMillis;
        this.userId = userId;
        this.username = username;
    }

    public static void init(String requestId, Long userId, String username) {
        RequestContext context = new RequestContext(requestId, System.currentTimeMillis(), userId, username);
        CONTEXT.set(context);
    }

    public static RequestContext current() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public String getRequestId() {
        return requestId;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public long getDurationMillis() {
        return System.currentTimeMillis() - startTimeMillis;
    }
}
```

- [ ] **Step 2: 创建日志配置属性类 LogProperties.java**

```java
package com.jaycong.boot.modules.log.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.log")
public class LogProperties {

    private RequestLogConfig request = new RequestLogConfig();
    private ErrorLogConfig error = new ErrorLogConfig();

    public RequestLogConfig getRequest() {
        return request;
    }

    public void setRequest(RequestLogConfig request) {
        this.request = request;
    }

    public ErrorLogConfig getError() {
        return error;
    }

    public void setError(ErrorLogConfig error) {
        this.error = error;
    }

    public static class RequestLogConfig {
        private boolean enabled = true;
        private int maxCount = 100000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }
    }

    public static class ErrorLogConfig {
        private boolean enabled = true;
        private int maxCount = 50000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }
    }
}
```

---

## Task 3: 后端实体类和 Mapper

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/entity/RequestLogEntity.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/entity/ErrorLogEntity.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/mapper/RequestLogMapper.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/mapper/ErrorLogMapper.java`

- [ ] **Step 1: 创建请求日志实体类 RequestLogEntity.java**

```java
package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("request_logs")
public class RequestLogEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String requestId;

    private Long userId;

    private String username;

    private String method;

    private String path;

    private String queryString;

    private String requestParams;

    private Integer statusCode;

    private Integer durationMs;

    private String clientIp;

    private String userAgent;
}
```

- [ ] **Step 2: 创建异常日志实体类 ErrorLogEntity.java**

```java
package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("error_logs")
public class ErrorLogEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String requestId;

    private Long userId;

    private String username;

    private String requestPath;

    private String requestParams;

    private String clientIp;

    private String exceptionClass;

    private String exceptionMessage;

    private String stackTrace;
}
```

- [ ] **Step 3: 创建请求日志 Mapper RequestLogMapper.java**

```java
package com.jaycong.boot.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.log.entity.RequestLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLogEntity> {
}
```

- [ ] **Step 4: 创建异常日志 Mapper ErrorLogMapper.java**

```java
package com.jaycong.boot.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.log.entity.ErrorLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErrorLogMapper extends BaseMapper<ErrorLogEntity> {
}
```

---

## Task 4: 后端 DTO 类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/RequestLogQueryRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/ErrorLogQueryRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/RequestLogItemView.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/ErrorLogItemView.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/BatchDeleteRequest.java`

- [ ] **Step 1: 创建请求日志查询请求类 RequestLogQueryRequest.java**

```java
package com.jaycong.boot.modules.log.dto;

import lombok.Data;

@Data
public class RequestLogQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String keyword;

    private String method;

    private Integer statusCode;

    private Long userId;

    private String startTime;

    private String endTime;

    public RequestLogQueryRequest() {
    }

    public RequestLogQueryRequest(Integer page, Integer pageSize, String keyword, String method,
                                  Integer statusCode, Long userId, String startTime, String endTime) {
        this.page = page != null ? page : 1;
        this.pageSize = pageSize != null ? pageSize : 20;
        this.keyword = keyword;
        this.method = method;
        this.statusCode = statusCode;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
```

- [ ] **Step 2: 创建异常日志查询请求类 ErrorLogQueryRequest.java**

```java
package com.jaycong.boot.modules.log.dto;

import lombok.Data;

@Data
public class ErrorLogQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String keyword;

    private String requestPath;

    private Long userId;

    private String startTime;

    private String endTime;

    public ErrorLogQueryRequest() {
    }

    public ErrorLogQueryRequest(Integer page, Integer pageSize, String keyword, String requestPath,
                                Long userId, String startTime, String endTime) {
        this.page = page != null ? page : 1;
        this.pageSize = pageSize != null ? pageSize : 20;
        this.keyword = keyword;
        this.requestPath = requestPath;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
```

- [ ] **Step 3: 创建请求日志视图类 RequestLogItemView.java**

```java
package com.jaycong.boot.modules.log.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RequestLogItemView {

    private Long id;

    private String requestId;

    private Long userId;

    private String username;

    private String method;

    private String path;

    private String queryString;

    private String requestParams;

    private Integer statusCode;

    private Integer durationMs;

    private String clientIp;

    private String userAgent;

    private LocalDateTime createdTime;
}
```

- [ ] **Step 4: 创建异常日志视图类 ErrorLogItemView.java**

```java
package com.jaycong.boot.modules.log.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ErrorLogItemView {

    private Long id;

    private String requestId;

    private Long userId;

    private String username;

    private String requestPath;

    private String requestParams;

    private String clientIp;

    private String exceptionClass;

    private String exceptionMessage;

    private String stackTrace;

    private LocalDateTime createdTime;
}
```

- [ ] **Step 5: 创建批量删除请求类 BatchDeleteRequest.java**

```java
package com.jaycong.boot.modules.log.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class BatchDeleteRequest {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
```

---

## Task 5: 后端服务层

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/service/RequestLogService.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/service/ErrorLogService.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/service/impl/RequestLogServiceImpl.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/service/impl/ErrorLogServiceImpl.java`

- [ ] **Step 1: 创建请求日志服务接口 RequestLogService.java**

```java
package com.jaycong.boot.modules.log.service;

import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.dto.RequestLogItemView;
import com.jaycong.boot.modules.log.dto.RequestLogQueryRequest;
import com.jaycong.boot.modules.log.entity.RequestLogEntity;

import java.util.List;

public interface RequestLogService {

    void save(RequestLogEntity entity);

    PageResult<RequestLogItemView> page(RequestLogQueryRequest request);

    RequestLogItemView getById(Long id);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);

    RequestLogEntity findByRequestId(String requestId);
}
```

- [ ] **Step 2: 创建异常日志服务接口 ErrorLogService.java**

```java
package com.jaycong.boot.modules.log.service;

import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.dto.ErrorLogItemView;
import com.jaycong.boot.modules.log.dto.ErrorLogQueryRequest;
import com.jaycong.boot.modules.log.entity.ErrorLogEntity;

import java.util.List;

public interface ErrorLogService {

    void save(ErrorLogEntity entity);

    PageResult<ErrorLogItemView> page(ErrorLogQueryRequest request);

    ErrorLogItemView getById(Long id);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 3: 创建请求日志服务实现 RequestLogServiceImpl.java**

```java
package com.jaycong.boot.modules.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.dto.RequestLogItemView;
import com.jaycong.boot.modules.log.dto.RequestLogQueryRequest;
import com.jaycong.boot.modules.log.entity.RequestLogEntity;
import com.jaycong.boot.modules.log.mapper.RequestLogMapper;
import com.jaycong.boot.modules.log.service.RequestLogService;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RequestLogServiceImpl implements RequestLogService {

    private final RequestLogMapper requestLogMapper;
    private final LogProperties logProperties;

    public RequestLogServiceImpl(RequestLogMapper requestLogMapper, LogProperties logProperties) {
        this.requestLogMapper = requestLogMapper;
        this.logProperties = logProperties;
    }

    @Override
    public void save(RequestLogEntity entity) {
        requestLogMapper.insert(entity);
        cleanupOldRecords();
    }

    @Override
    public PageResult<RequestLogItemView> page(RequestLogQueryRequest request) {
        LambdaQueryWrapper<RequestLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(request.getKeyword()), RequestLogEntity::getPath, request.getKeyword());
        wrapper.eq(StringUtils.isNotBlank(request.getMethod()), RequestLogEntity::getMethod, request.getMethod());
        wrapper.eq(request.getStatusCode() != null, RequestLogEntity::getStatusCode, request.getStatusCode());
        wrapper.eq(request.getUserId() != null, RequestLogEntity::getUserId, request.getUserId());
        wrapper.ge(request.getStartTime() != null, RequestLogEntity::getCreatedTime, parseDateTime(request.getStartTime()));
        wrapper.le(request.getEndTime() != null, RequestLogEntity::getCreatedTime, parseDateTime(request.getEndTime()));
        wrapper.orderByDesc(RequestLogEntity::getCreatedTime);

        IPage<RequestLogEntity> page = requestLogMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);

        List<RequestLogItemView> records = page.getRecords().stream()
                .map(this::toView)
                .toList();

        return new PageResult<>(records, (int) page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public RequestLogItemView getById(Long id) {
        RequestLogEntity entity = requestLogMapper.selectById(id);
        return entity != null ? toView(entity) : null;
    }

    @Override
    public void deleteById(Long id) {
        requestLogMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            requestLogMapper.deleteBatchIds(ids);
        }
    }

    @Override
    public RequestLogEntity findByRequestId(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            return null;
        }
        LambdaQueryWrapper<RequestLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestLogEntity::getRequestId, requestId);
        return requestLogMapper.selectOne(wrapper);
    }

    private void cleanupOldRecords() {
        int maxCount = logProperties.getRequest().getMaxCount();
        Long totalCount = requestLogMapper.selectCount(null);
        if (totalCount > maxCount) {
            int deleteCount = (int) (totalCount - maxCount + 1000);
            LambdaQueryWrapper<RequestLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(RequestLogEntity::getCreatedTime);
            wrapper.last("LIMIT " + deleteCount);
            List<RequestLogEntity> oldRecords = requestLogMapper.selectList(wrapper);
            for (RequestLogEntity entity : oldRecords) {
                requestLogMapper.deleteById(entity.getId());
            }
        }
    }

    private RequestLogItemView toView(RequestLogEntity entity) {
        RequestLogItemView view = new RequestLogItemView();
        BeanUtils.copyProperties(entity, view);
        return view;
    }

    private LocalDateTime parseDateTime(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}
```

- [ ] **Step 4: 创建异常日志服务实现 ErrorLogServiceImpl.java**

```java
package com.jaycong.boot.modules.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.dto.ErrorLogItemView;
import com.jaycong.boot.modules.log.dto.ErrorLogQueryRequest;
import com.jaycong.boot.modules.log.entity.ErrorLogEntity;
import com.jaycong.boot.modules.log.mapper.ErrorLogMapper;
import com.jaycong.boot.modules.log.service.ErrorLogService;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ErrorLogServiceImpl implements ErrorLogService {

    private final ErrorLogMapper errorLogMapper;
    private final LogProperties logProperties;

    public ErrorLogServiceImpl(ErrorLogMapper errorLogMapper, LogProperties logProperties) {
        this.errorLogMapper = errorLogMapper;
        this.logProperties = logProperties;
    }

    @Override
    public void save(ErrorLogEntity entity) {
        errorLogMapper.insert(entity);
        cleanupOldRecords();
    }

    @Override
    public PageResult<ErrorLogItemView> page(ErrorLogQueryRequest request) {
        LambdaQueryWrapper<ErrorLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.isNotBlank(request.getKeyword()), w ->
                w.like(ErrorLogEntity::getExceptionClass, request.getKeyword())
                 .or()
                 .like(ErrorLogEntity::getExceptionMessage, request.getKeyword()));
        wrapper.like(StringUtils.isNotBlank(request.getRequestPath()), ErrorLogEntity::getRequestPath, request.getRequestPath());
        wrapper.eq(request.getUserId() != null, ErrorLogEntity::getUserId, request.getUserId());
        wrapper.ge(request.getStartTime() != null, ErrorLogEntity::getCreatedTime, parseDateTime(request.getStartTime()));
        wrapper.le(request.getEndTime() != null, ErrorLogEntity::getCreatedTime, parseDateTime(request.getEndTime()));
        wrapper.orderByDesc(ErrorLogEntity::getCreatedTime);

        IPage<ErrorLogEntity> page = errorLogMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);

        List<ErrorLogItemView> records = page.getRecords().stream()
                .map(this::toView)
                .toList();

        return new PageResult<>(records, (int) page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public ErrorLogItemView getById(Long id) {
        ErrorLogEntity entity = errorLogMapper.selectById(id);
        return entity != null ? toView(entity) : null;
    }

    @Override
    public void deleteById(Long id) {
        errorLogMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            errorLogMapper.deleteBatchIds(ids);
        }
    }

    private void cleanupOldRecords() {
        int maxCount = logProperties.getError().getMaxCount();
        Long totalCount = errorLogMapper.selectCount(null);
        if (totalCount > maxCount) {
            int deleteCount = (int) (totalCount - maxCount + 1000);
            LambdaQueryWrapper<ErrorLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(ErrorLogEntity::getCreatedTime);
            wrapper.last("LIMIT " + deleteCount);
            List<ErrorLogEntity> oldRecords = errorLogMapper.selectList(wrapper);
            for (ErrorLogEntity entity : oldRecords) {
                errorLogMapper.deleteById(entity.getId());
            }
        }
    }

    private ErrorLogItemView toView(ErrorLogEntity entity) {
        ErrorLogItemView view = new ErrorLogItemView();
        BeanUtils.copyProperties(entity, view);
        return view;
    }

    private LocalDateTime parseDateTime(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}
```

---

## Task 6: 后端拦截器和切面

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/interceptor/RequestLogInterceptor.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/aspect/RequestLogAspect.java`

- [ ] **Step 1: 创建请求日志拦截器 RequestLogInterceptor.java**

```java
package com.jaycong.boot.modules.log.interceptor;

import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.auth.context.LoginPrincipal;
import com.jaycong.boot.modules.log.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = generateRequestId();
        Long userId = null;
        String username = null;

        try {
            LoginPrincipal principal = LoginContext.currentPrincipal().orElse(null);
            if (principal != null) {
                userId = principal.userId();
                username = principal.username();
            }
        } catch (Exception ignored) {
            // 忽略登录上下文获取异常
        }

        RequestContext.init(requestId, userId, username);
        response.setHeader("X-Request-Id", requestId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContext.clear();
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
```

- [ ] **Step 2: 创建请求日志切面 RequestLogAspect.java**

```java
package com.jaycong.boot.modules.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.context.RequestContext;
import com.jaycong.boot.modules.log.entity.RequestLogEntity;
import com.jaycong.boot.modules.log.service.RequestLogService;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class RequestLogAspect {

    private final RequestLogService requestLogService;
    private final LogProperties logProperties;
    private final ObjectMapper objectMapper;

    public RequestLogAspect(RequestLogService requestLogService, LogProperties logProperties, ObjectMapper objectMapper) {
        this.requestLogService = requestLogService;
        this.logProperties = logProperties;
        this.objectMapper = objectMapper;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!logProperties.getRequest().isEnabled()) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        RequestContext context = RequestContext.current();
        if (context == null) {
            return joinPoint.proceed();
        }

        Object result = null;
        Throwable exception = null;
        int statusCode = 200;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            exception = ex;
            statusCode = 500;
            throw ex;
        } finally {
            try {
                saveRequestLog(request, context, statusCode);
            } catch (Exception ignored) {
                // 日志记录失败不影响业务
            }
        }

        return result;
    }

    private void saveRequestLog(HttpServletRequest request, RequestContext context, int statusCode) {
        RequestLogEntity entity = new RequestLogEntity();
        entity.setRequestId(context.getRequestId());
        entity.setUserId(context.getUserId());
        entity.setUsername(context.getUsername());
        entity.setMethod(request.getMethod());
        entity.setPath(request.getRequestURI());
        entity.setQueryString(request.getQueryString());
        entity.setRequestParams(extractRequestParams(request));
        entity.setStatusCode(statusCode);
        entity.setDurationMs((int) context.getDurationMillis());
        entity.setClientIp(getClientIp(request));
        entity.setUserAgent(request.getHeader("User-Agent"));
        entity.setCreatorId(context.getUserId() != null ? context.getUserId() : 0L);
        entity.setCreatorName(context.getUsername() != null ? context.getUsername() : "system");
        entity.setUpdaterId(entity.getCreatorId());
        entity.setUpdaterName(entity.getCreatorName());

        requestLogService.save(entity);
    }

    private String extractRequestParams(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                String[] values = entry.getValue();
                if (values.length == 1) {
                    params.put(entry.getKey(), values[0]);
                } else {
                    params.put(entry.getKey(), values);
                }
            }
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
```

---

## Task 7: 后端异常日志处理器

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/handler/ErrorLogHandler.java`

- [ ] **Step 1: 创建异常日志处理器 ErrorLogHandler.java**

```java
package com.jaycong.boot.modules.log.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.context.RequestContext;
import com.jaycong.boot.modules.log.entity.ErrorLogEntity;
import com.jaycong.boot.modules.log.service.ErrorLogService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class ErrorLogHandler {

    private final ErrorLogService errorLogService;
    private final LogProperties logProperties;
    private final ObjectMapper objectMapper;

    public ErrorLogHandler(ErrorLogService errorLogService, LogProperties logProperties, ObjectMapper objectMapper) {
        this.errorLogService = errorLogService;
        this.logProperties = logProperties;
        this.objectMapper = objectMapper;
    }

    public void recordError(Throwable ex) {
        if (!logProperties.getError().isEnabled()) {
            return;
        }

        HttpServletRequest request = getCurrentRequest();
        RequestContext context = RequestContext.current();

        ErrorLogEntity entity = new ErrorLogEntity();
        entity.setRequestId(context != null ? context.getRequestId() : null);
        entity.setUserId(context != null ? context.getUserId() : null);
        entity.setUsername(context != null ? context.getUsername() : null);
        entity.setRequestPath(request != null ? request.getRequestURI() : null);
        entity.setRequestParams(request != null ? extractRequestParams(request) : null);
        entity.setClientIp(request != null ? getClientIp(request) : null);
        entity.setExceptionClass(ex.getClass().getName());
        entity.setExceptionMessage(ex.getMessage());
        entity.setStackTrace(getStackTrace(ex));
        entity.setCreatorId(context != null && context.getUserId() != null ? context.getUserId() : 0L);
        entity.setCreatorName(context != null && context.getUsername() != null ? context.getUsername() : "system");
        entity.setUpdaterId(entity.getCreatorId());
        entity.setUpdaterName(entity.getCreatorName());

        errorLogService.save(entity);
    }

    private String extractRequestParams(HttpServletRequest request) {
        try {
            var paramMap = request.getParameterMap();
            if (paramMap.isEmpty()) {
                return null;
            }
            var params = new java.util.HashMap<String, Object>();
            for (var entry : paramMap.entrySet()) {
                String[] values = entry.getValue();
                if (values.length == 1) {
                    params.put(entry.getKey(), values[0]);
                } else {
                    params.put(entry.getKey(), values);
                }
            }
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
```

---

## Task 8: 后端自动配置类

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/config/LogAutoConfiguration.java`

- [ ] **Step 1: 创建日志自动配置类 LogAutoConfiguration.java**

```java
package com.jaycong.boot.modules.log.config;

import com.jaycong.boot.modules.log.interceptor.RequestLogInterceptor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(LogProperties.class)
public class LogAutoConfiguration implements WebMvcConfigurer {

    private final RequestLogInterceptor requestLogInterceptor;

    public LogAutoConfiguration(RequestLogInterceptor requestLogInterceptor) {
        this.requestLogInterceptor = requestLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/admin/auth/**", "/api/health/**");
    }
}
```

---

## Task 9: 修改 GlobalExceptionHandler

**Files:**
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/common/web/GlobalExceptionHandler.java`

- [ ] **Step 1: 在 GlobalExceptionHandler 中注入 ErrorLogHandler**

修改 GlobalExceptionHandler.java，添加 ErrorLogHandler 依赖并在异常处理中记录异常日志：

```java
package com.jaycong.boot.common.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.log.handler.ErrorLogHandler;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一输出 ApiResponse 错误结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorLogHandler errorLogHandler;

    public GlobalExceptionHandler(ErrorLogHandler errorLogHandler) {
        this.errorLogHandler = errorLogHandler;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getCode());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLogin(NotLoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), "用户未登录"));
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotPermission(NotPermissionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), "无权限访问"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = "请求参数不合法";
        if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
            FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
            message = fieldError.getDefaultMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        log.error("系统发生未处理异常", ex);
        errorLogHandler.recordError(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), "服务器内部错误"));
    }
}
```

---

## Task 10: 后端控制器

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/log/RequestLogController.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/log/ErrorLogController.java`

- [ ] **Step 1: 创建请求日志控制器 RequestLogController.java**

```java
package com.jaycong.boot.rest.admin.log;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.dto.BatchDeleteRequest;
import com.jaycong.boot.modules.log.dto.RequestLogItemView;
import com.jaycong.boot.modules.log.dto.RequestLogQueryRequest;
import com.jaycong.boot.modules.log.service.RequestLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs/requests")
@Validated
@Tag(name = "请求日志管理", description = "请求日志列表、详情、删除")
@SaCheckRole({"admin", "super_admin"})
public class RequestLogController {

    private final RequestLogService requestLogService;

    public RequestLogController(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    @Operation(summary = "分页查询请求日志")
    @GetMapping
    public ApiResponse<PageResult<RequestLogItemView>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        RequestLogQueryRequest request = new RequestLogQueryRequest(
                page, pageSize, keyword, method, statusCode, userId, startTime, endTime);
        return ApiResponse.success(requestLogService.page(request));
    }

    @Operation(summary = "获取请求日志详情")
    @GetMapping("/{id}")
    public ApiResponse<RequestLogItemView> getById(@PathVariable Long id) {
        return ApiResponse.success(requestLogService.getById(id));
    }

    @Operation(summary = "删除请求日志")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requestLogService.deleteById(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量删除请求日志")
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        requestLogService.batchDelete(request.getIds());
        return ApiResponse.success(null);
    }
}
```

- [ ] **Step 2: 创建异常日志控制器 ErrorLogController.java**

```java
package com.jaycong.boot.rest.admin.log;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.dto.BatchDeleteRequest;
import com.jaycong.boot.modules.log.dto.ErrorLogItemView;
import com.jaycong.boot.modules.log.dto.ErrorLogQueryRequest;
import com.jaycong.boot.modules.log.service.ErrorLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs/errors")
@Validated
@Tag(name = "异常日志管理", description = "异常日志列表、详情、删除")
@SaCheckRole({"admin", "super_admin"})
public class ErrorLogController {

    private final ErrorLogService errorLogService;

    public ErrorLogController(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @Operation(summary = "分页查询异常日志")
    @GetMapping
    public ApiResponse<PageResult<ErrorLogItemView>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String requestPath,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        ErrorLogQueryRequest request = new ErrorLogQueryRequest(
                page, pageSize, keyword, requestPath, userId, startTime, endTime);
        return ApiResponse.success(errorLogService.page(request));
    }

    @Operation(summary = "获取异常日志详情")
    @GetMapping("/{id}")
    public ApiResponse<ErrorLogItemView> getById(@PathVariable Long id) {
        return ApiResponse.success(errorLogService.getById(id));
    }

    @Operation(summary = "删除异常日志")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        errorLogService.deleteById(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量删除异常日志")
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        errorLogService.batchDelete(request.getIds());
        return ApiResponse.success(null);
    }
}
```

---

## Task 11: 更新配置文件

**Files:**
- Modify: `jay-boot-backend/src/main/resources/application.yml`

- [ ] **Step 1: 在 application.yml 中添加日志配置**

在 application.yml 文件末尾添加：

```yaml
app:
  log:
    request:
      enabled: true
      max-count: 100000
    error:
      enabled: true
      max-count: 50000
```

---

## Task 12: 后端编译验证

- [ ] **Step 1: 执行后端编译验证**

运行: `cd jay-boot-backend; mvn compile -q`

预期: 编译成功，无错误

---

## Task 13: 前端 API 接口

**Files:**
- Create: `jay-boot-frontend/src/api/admin/LogApi.ts`

- [ ] **Step 1: 创建日志 API 接口 LogApi.ts**

```typescript
import { requestAdminAuthRealApi } from './realApi'

export interface RequestLogItem {
  id: string
  requestId: string
  userId: string | null
  username: string | null
  method: string
  path: string
  queryString: string | null
  requestParams: string | null
  statusCode: number
  durationMs: number
  clientIp: string | null
  userAgent: string | null
  createdTime: string
}

export interface RequestLogPageResponse {
  records: RequestLogItem[]
  total: number
  page: number
  pageSize: number
}

export interface RequestLogPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  method?: string
  statusCode?: number
  userId?: string
  startTime?: string
  endTime?: string
}

export interface ErrorLogItem {
  id: string
  requestId: string | null
  userId: string | null
  username: string | null
  requestPath: string | null
  requestParams: string | null
  clientIp: string | null
  exceptionClass: string
  exceptionMessage: string | null
  stackTrace: string | null
  createdTime: string
}

export interface ErrorLogPageResponse {
  records: ErrorLogItem[]
  total: number
  page: number
  pageSize: number
}

export interface ErrorLogPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  requestPath?: string
  userId?: string
  startTime?: string
  endTime?: string
}

interface BatchDeletePayload extends Record<string, unknown> {
  ids: string[]
}

/**
 * 获取请求日志分页列表
 */
export const getRequestLogPageApi = (token: string, params: RequestLogPageParams) =>
  requestAdminAuthRealApi<RequestLogPageResponse>('GET', '/api/admin/logs/requests', { token, payload: params })

/**
 * 获取请求日志详情
 */
export const getRequestLogDetailApi = (token: string, id: string) =>
  requestAdminAuthRealApi<RequestLogItem>('GET', `/api/admin/logs/requests/${id}`, { token })

/**
 * 删除请求日志
 */
export const deleteRequestLogApi = (token: string, id: string) =>
  requestAdminAuthRealApi<null>('DELETE', `/api/admin/logs/requests/${id}`, { token })

/**
 * 批量删除请求日志
 */
export const batchDeleteRequestLogApi = (token: string, ids: string[]) =>
  requestAdminAuthRealApi<null>('POST', '/api/admin/logs/requests/batch-delete', { token, payload: { ids } as BatchDeletePayload })

/**
 * 获取异常日志分页列表
 */
export const getErrorLogPageApi = (token: string, params: ErrorLogPageParams) =>
  requestAdminAuthRealApi<ErrorLogPageResponse>('GET', '/api/admin/logs/errors', { token, payload: params })

/**
 * 获取异常日志详情
 */
export const getErrorLogDetailApi = (token: string, id: string) =>
  requestAdminAuthRealApi<ErrorLogItem>('GET', `/api/admin/logs/errors/${id}`, { token })

/**
 * 删除异常日志
 */
export const deleteErrorLogApi = (token: string, id: string) =>
  requestAdminAuthRealApi<null>('DELETE', `/api/admin/logs/errors/${id}`, { token })

/**
 * 批量删除异常日志
 */
export const batchDeleteErrorLogApi = (token: string, ids: string[]) =>
  requestAdminAuthRealApi<null>('POST', '/api/admin/logs/errors/batch-delete', { token, payload: { ids } as BatchDeletePayload })
```

---

## Task 14: 前端 Store

**Files:**
- Create: `jay-boot-frontend/src/stores/admin/requestLog.ts`
- Create: `jay-boot-frontend/src/stores/admin/errorLog.ts`

- [ ] **Step 1: 创建请求日志 Store requestLog.ts**

```typescript
import { defineStore } from 'pinia'
import {
  getRequestLogPageApi,
  getRequestLogDetailApi,
  deleteRequestLogApi,
  batchDeleteRequestLogApi,
  type RequestLogItem,
  type RequestLogPageParams,
} from '../../api/admin/LogApi'
import { useAuthStore } from './auth'

type FetchMode = 'initial' | 'refresh'

export const useRequestLogStore = defineStore('admin-request-log', {
  state: () => ({
    records: [] as RequestLogItem[],
    total: 0,
    page: 1,
    pageSize: 20,
    keyword: '',
    method: '',
    statusCode: null as number | null,
    userId: '',
    startTime: '',
    endTime: '',
    loadingInitial: false,
    refreshing: false,
    loadedOnce: false,
    errorMessage: '',
    detailItem: null as RequestLogItem | null,
  }),
  getters: {
    hasData: (state) => state.records.length > 0,
  },
  actions: {
    async initialize() {
      if (this.loadedOnce) {
        return
      }
      await this.fetchList('initial')
    },
    async fetchList(mode: FetchMode = 'refresh') {
      const token = await this.requireToken()
      if (!token) {
        this.errorMessage = '用户未登录'
        this.records = []
        this.total = 0
        return
      }

      this.errorMessage = ''
      if (mode === 'initial') {
        this.loadingInitial = true
      }
      if (mode === 'refresh') {
        this.refreshing = true
      }

      try {
        const params: RequestLogPageParams = {
          page: this.page,
          pageSize: this.pageSize,
        }
        if (this.keyword) params.keyword = this.keyword
        if (this.method) params.method = this.method
        if (this.statusCode !== null) params.statusCode = this.statusCode
        if (this.userId) params.userId = this.userId
        if (this.startTime) params.startTime = this.startTime
        if (this.endTime) params.endTime = this.endTime

        const data = await getRequestLogPageApi(token, params)
        this.records = data.records
        this.total = data.total
        this.page = data.page
        this.pageSize = data.pageSize
        this.loadedOnce = true

        if (this.total > 0 && this.records.length === 0 && this.page > 1) {
          this.page -= 1
          await this.fetchList('refresh')
        }
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = '请求日志加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    setFilters(payload: {
      keyword: string
      method: string
      statusCode: number | null
      userId: string
      startTime: string
      endTime: string
    }) {
      this.keyword = payload.keyword
      this.method = payload.method
      this.statusCode = payload.statusCode
      this.userId = payload.userId
      this.startTime = payload.startTime
      this.endTime = payload.endTime
    },
    resetFilters() {
      this.keyword = ''
      this.method = ''
      this.statusCode = null
      this.userId = ''
      this.startTime = ''
      this.endTime = ''
      this.page = 1
    },
    async searchWithCurrentFilters() {
      this.page = 1
      await this.fetchList('refresh')
    },
    async changePage(page: number, pageSize?: number) {
      this.page = page
      if (pageSize && pageSize > 0) {
        this.pageSize = pageSize
      }
      await this.fetchList('refresh')
    },
    async fetchDetail(id: string) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      this.detailItem = await getRequestLogDetailApi(token, id)
      return this.detailItem
    },
    async deleteOne(id: string) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      await deleteRequestLogApi(token, id)
    },
    async batchDelete(ids: string[]) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      await batchDeleteRequestLogApi(token, ids)
    },
    async requireToken() {
      const authStore = useAuthStore()
      await authStore.hydrate()
      return authStore.token || ''
    },
  },
})
```

- [ ] **Step 2: 创建异常日志 Store errorLog.ts**

```typescript
import { defineStore } from 'pinia'
import {
  getErrorLogPageApi,
  getErrorLogDetailApi,
  deleteErrorLogApi,
  batchDeleteErrorLogApi,
  type ErrorLogItem,
  type ErrorLogPageParams,
} from '../../api/admin/LogApi'
import { useAuthStore } from './auth'

type FetchMode = 'initial' | 'refresh'

export const useErrorLogStore = defineStore('admin-error-log', {
  state: () => ({
    records: [] as ErrorLogItem[],
    total: 0,
    page: 1,
    pageSize: 20,
    keyword: '',
    requestPath: '',
    userId: '',
    startTime: '',
    endTime: '',
    loadingInitial: false,
    refreshing: false,
    loadedOnce: false,
    errorMessage: '',
    detailItem: null as ErrorLogItem | null,
  }),
  getters: {
    hasData: (state) => state.records.length > 0,
  },
  actions: {
    async initialize() {
      if (this.loadedOnce) {
        return
      }
      await this.fetchList('initial')
    },
    async fetchList(mode: FetchMode = 'refresh') {
      const token = await this.requireToken()
      if (!token) {
        this.errorMessage = '用户未登录'
        this.records = []
        this.total = 0
        return
      }

      this.errorMessage = ''
      if (mode === 'initial') {
        this.loadingInitial = true
      }
      if (mode === 'refresh') {
        this.refreshing = true
      }

      try {
        const params: ErrorLogPageParams = {
          page: this.page,
          pageSize: this.pageSize,
        }
        if (this.keyword) params.keyword = this.keyword
        if (this.requestPath) params.requestPath = this.requestPath
        if (this.userId) params.userId = this.userId
        if (this.startTime) params.startTime = this.startTime
        if (this.endTime) params.endTime = this.endTime

        const data = await getErrorLogPageApi(token, params)
        this.records = data.records
        this.total = data.total
        this.page = data.page
        this.pageSize = data.pageSize
        this.loadedOnce = true

        if (this.total > 0 && this.records.length === 0 && this.page > 1) {
          this.page -= 1
          await this.fetchList('refresh')
        }
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = '异常日志加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    setFilters(payload: {
      keyword: string
      requestPath: string
      userId: string
      startTime: string
      endTime: string
    }) {
      this.keyword = payload.keyword
      this.requestPath = payload.requestPath
      this.userId = payload.userId
      this.startTime = payload.startTime
      this.endTime = payload.endTime
    },
    resetFilters() {
      this.keyword = ''
      this.requestPath = ''
      this.userId = ''
      this.startTime = ''
      this.endTime = ''
      this.page = 1
    },
    async searchWithCurrentFilters() {
      this.page = 1
      await this.fetchList('refresh')
    },
    async changePage(page: number, pageSize?: number) {
      this.page = page
      if (pageSize && pageSize > 0) {
        this.pageSize = pageSize
      }
      await this.fetchList('refresh')
    },
    async fetchDetail(id: string) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      this.detailItem = await getErrorLogDetailApi(token, id)
      return this.detailItem
    },
    async deleteOne(id: string) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      await deleteErrorLogApi(token, id)
    },
    async batchDelete(ids: string[]) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      await batchDeleteErrorLogApi(token, ids)
    },
    async requireToken() {
      const authStore = useAuthStore()
      await authStore.hydrate()
      return authStore.token || ''
    },
  },
})
```

---

## Task 15: 前端路由配置

**Files:**
- Modify: `jay-boot-frontend/src/router/routes/admin.ts`

- [ ] **Step 1: 在 admin.ts 中添加日志管理路由**

在 `adminChildren` 数组中添加以下路由配置：

```typescript
  {
    path: 'logs/requests',
    name: 'admin-logs-requests',
    component: () => import('../../views/admin/RequestLogView.vue'),
    meta: { title: '请求日志', requiresAuth: true },
  },
  {
    path: 'logs/errors',
    name: 'admin-logs-errors',
    component: () => import('../../views/admin/ErrorLogView.vue'),
    meta: { title: '异常日志', requiresAuth: true },
  },
```

---

## Task 16: 前端页面组件

**Files:**
- Create: `jay-boot-frontend/src/views/admin/RequestLogView.vue`
- Create: `jay-boot-frontend/src/views/admin/ErrorLogView.vue`

- [ ] **Step 1: 创建请求日志页面 RequestLogView.vue**

参考 UserManagementView.vue 的风格，创建请求日志管理页面。包含：
- 页面头部：标题、描述、刷新按钮
- 筛选区域：关键词、请求方法、状态码、用户ID、时间范围
- 数据表格：请求ID、路径、方法、状态码、耗时、用户、时间、操作
- 详情弹窗：展示完整请求信息
- 删除确认

- [ ] **Step 2: 创建异常日志页面 ErrorLogView.vue**

参考 UserManagementView.vue 的风格，创建异常日志管理页面。包含：
- 页面头部：标题、描述、刷新按钮
- 筛选区域：关键词、请求路径、用户ID、时间范围
- 数据表格：请求ID、请求路径、异常类型、异常消息、用户、时间、操作
- 详情弹窗：展示完整堆栈信息、请求参数、客户端IP
- 删除确认

---

## Task 17: 前端菜单集成

**Files:**
- Modify: `jay-boot-frontend/src/layouts/admin/AppShell.vue`

- [ ] **Step 1: 在 AppShell.vue 中添加日志管理菜单**

修改 `menuItems` 数组，添加日志管理菜单项：

```typescript
const menuItems: MenuProps['items'] = [
  { key: '/admin/dashboard', label: '控制台总览' },
  { key: '/admin/users', label: '用户管理' },
  { key: '/admin/plans', label: '套餐管理' },
  { key: '/admin/logs/requests', label: '请求日志' },
  { key: '/admin/logs/errors', label: '异常日志' },
]
```

---

## Task 18: 前端编译验证

- [ ] **Step 1: 执行前端编译验证**

运行: `cd jay-boot-frontend; npm run build`

预期: 编译成功，无错误

---

## Task 19: 功能验证

- [ ] **Step 1: 启动后端服务**

运行后端服务，确保无启动错误

- [ ] **Step 2: 启动前端开发服务器**

运行前端开发服务器，确保页面正常访问

- [ ] **Step 3: 验证请求日志功能**

- 访问几个 API 接口
- 进入请求日志管理页面
- 检查请求日志是否正确记录
- 测试筛选、详情、删除功能

- [ ] **Step 4: 验证异常日志功能**

- 触发一个异常（如访问不存在的接口）
- 进入异常日志管理页面
- 检查异常日志是否正确记录
- 测试筛选、详情、删除功能
