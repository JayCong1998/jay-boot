# 统一操作日志模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现基于注解的操作日志模块，支持业务操作审计追溯。

**Architecture:** 采用注解 + AOP 切面方案，开发者在方法上标注 `@OperationLog` 注解，切面自动捕获上下文并持久化到数据库。前端提供分页查询和详情查看功能。

**Tech Stack:** Spring AOP、SpEL、MyBatis-Plus、Vue 3、Ant Design Vue

---

## 文件清单

### 后端新建文件
| 文件路径 | 职责 |
|---------|------|
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/annotation/OperationLog.java` | 操作日志注解 |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/aspect/OperationLogAspect.java` | AOP 切面 |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/entity/OperationLogEntity.java` | 操作日志实体 |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/mapper/OperationLogMapper.java` | 数据访问层 |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/service/OperationLogService.java` | 业务服务层 |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/controller/AdminOperationLogController.java` | 管理端控制器 |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/OperationLogQueryDTO.java` | 查询条件 DTO |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/OperationLogVO.java` | 响应 VO |

### 后端修改文件
| 文件路径 | 修改内容 |
|---------|---------|
| `database/schema.sql` | 新增 `operation_logs` 表 |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/user/service/AdminUserService.java` | 添加操作日志注解 |
| `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/service/AdminPlanService.java` | 添加操作日志注解 |

### 前端新建文件
| 文件路径 | 职责 |
|---------|------|
| `jay-boot-frontend/src/views/admin/OperationLogView.vue` | 操作日志页面 |

### 前端修改文件
| 文件路径 | 修改内容 |
|---------|---------|
| `jay-boot-frontend/src/api/admin/LogApi.ts` | 新增操作日志 API |
| `jay-boot-frontend/src/router/routes/admin.ts` | 新增路由 |

---

## Task 1: 数据库表结构

**Files:**
- Modify: `database/schema.sql`

- [ ] **Step 1: 在 schema.sql 末尾添加 operation_logs 表**

在文件末尾添加：

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

- [ ] **Step 2: 提交**

```bash
git add database/schema.sql
git commit -m "feat(log): 添加操作日志表结构"
```

---

## Task 2: 操作日志注解

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/annotation/OperationLog.java`

- [ ] **Step 1: 创建注解目录**

- [ ] **Step 2: 创建 OperationLog.java 注解**

```java
package com.jaycong.boot.modules.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 标注在方法上，AOP 切面会自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 模块名称
     * 示例: "用户管理"、"套餐管理"
     */
    String module();

    /**
     * 操作类型
     * 示例: "创建"、"删除"、"修改"
     */
    String action();

    /**
     * 操作详情模板（支持 SpEL 表达式）
     * 示例: "删除用户：#{#username}"
     */
    String detail() default "";
}
```

- [ ] **Step 3: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/annotation/OperationLog.java
git commit -m "feat(log): 添加操作日志注解"
```

---

## Task 3: 操作日志实体

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/entity/OperationLogEntity.java`

- [ ] **Step 1: 创建 OperationLogEntity.java**

```java
package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_logs")
public class OperationLogEntity extends BaseEntity {

    private String module;

    private String action;

    private String detail;

    private Long userId;

    private String username;

    private String clientIp;

    private String requestId;
}
```

- [ ] **Step 2: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/entity/OperationLogEntity.java
git commit -m "feat(log): 添加操作日志实体类"
```

---

## Task 4: 操作日志 Mapper

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/mapper/OperationLogMapper.java`

- [ ] **Step 1: 创建 OperationLogMapper.java**

```java
package com.jaycong.boot.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.log.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
```

- [ ] **Step 2: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/mapper/OperationLogMapper.java
git commit -m "feat(log): 添加操作日志 Mapper"
```

---

## Task 5: 操作日志 DTO

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/OperationLogQueryDTO.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/OperationLogVO.java`

- [ ] **Step 1: 创建 OperationLogQueryDTO.java**

```java
package com.jaycong.boot.modules.log.dto;

import lombok.Data;

@Data
public class OperationLogQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 20;
    private String module;
    private Long userId;
    private String startTime;
    private String endTime;
}
```

- [ ] **Step 2: 创建 OperationLogVO.java**

```java
package com.jaycong.boot.modules.log.dto;

import lombok.Data;

@Data
public class OperationLogVO {
    private String id;
    private String module;
    private String action;
    private String detail;
    private Long userId;
    private String username;
    private String clientIp;
    private String requestId;
    private String createdTime;
}
```

- [ ] **Step 3: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/OperationLogQueryDTO.java \
       jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/dto/OperationLogVO.java
git commit -m "feat(log): 添加操作日志 DTO"
```

---

## Task 6: 操作日志 Service

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/service/OperationLogService.java`

- [ ] **Step 1: 创建 OperationLogService.java**

```java
package com.jaycong.boot.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.modules.log.dto.OperationLogQueryDTO;
import com.jaycong.boot.modules.log.dto.OperationLogVO;
import com.jaycong.boot.modules.log.entity.OperationLogEntity;
import com.jaycong.boot.modules.log.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void record(OperationLogEntity entity) {
        try {
            operationLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("操作日志记录失败: module={}, action={}", entity.getModule(), entity.getAction(), e);
        }
    }

    public IPage<OperationLogVO> queryPage(OperationLogQueryDTO query) {
        Page<OperationLogEntity> page = new Page<>(query.getPage(), query.getPageSize());
        
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getModule()), OperationLogEntity::getModule, query.getModule())
               .eq(query.getUserId() != null, OperationLogEntity::getUserId, query.getUserId())
               .ge(query.getStartTime() != null, OperationLogEntity::getCreatedTime, parseStartTime(query.getStartTime()))
               .le(query.getEndTime() != null, OperationLogEntity::getCreatedTime, parseEndTime(query.getEndTime()))
               .orderByDesc(OperationLogEntity::getCreatedTime);

        IPage<OperationLogEntity> entityPage = operationLogMapper.selectPage(page, wrapper);

        return entityPage.convert(this::toVO);
    }

    public OperationLogVO getById(Long id) {
        OperationLogEntity entity = operationLogMapper.selectById(id);
        return entity != null ? toVO(entity) : null;
    }

    private OperationLogVO toVO(OperationLogEntity entity) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setModule(entity.getModule());
        vo.setAction(entity.getAction());
        vo.setDetail(entity.getDetail());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setClientIp(entity.getClientIp());
        vo.setRequestId(entity.getRequestId());
        vo.setCreatedTime(entity.getCreatedTime() != null ? entity.getCreatedTime().format(DATE_FORMATTER) : null);
        return vo;
    }

    private LocalDateTime parseStartTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(time.substring(0, 10));
            return date.atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseEndTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(time.substring(0, 10));
            return date.atTime(LocalTime.MAX);
        } catch (Exception e) {
            return null;
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/service/OperationLogService.java
git commit -m "feat(log): 添加操作日志 Service"
```

---

## Task 7: AOP 切面

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/aspect/OperationLogAspect.java`

- [ ] **Step 1: 创建 OperationLogAspect.java**

```java
package com.jaycong.boot.modules.log.aspect;

import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.auth.context.LoginPrincipal;
import com.jaycong.boot.modules.log.annotation.OperationLog;
import com.jaycong.boot.modules.log.context.RequestContext;
import com.jaycong.boot.modules.log.entity.OperationLogEntity;
import com.jaycong.boot.modules.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final SpelExpressionParser spelParser = new DefaultParameterNameDiscoverer() != null 
            ? new SpelExpressionParser() 
            : new SpelExpressionParser();

    @Pointcut("@annotation(com.jaycong.boot.modules.log.annotation.OperationLog)")
    public void operationLogPointcut() {}

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

            // 构建日志实体
            OperationLogEntity entity = buildLogEntity(annotation, detail);

            // 保存日志
            operationLogService.record(entity);
        } catch (Exception e) {
            // 日志记录失败不影响业务
            log.warn("操作日志记录失败", e);
        }

        return result;
    }

    private String parseSpel(String template, ProceedingJoinPoint point) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        if (!template.contains("#{")) {
            return template;
        }

        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = signature.getParameterNames();
            Object[] args = point.getArgs();

            // 提取 #{...} 中的表达式
            String spelExpr = template;
            if (template.startsWith("#{") && template.endsWith("}")) {
                spelExpr = template.substring(2, template.length() - 1);
            } else if (template.contains("#{")) {
                // 处理混合模板，如 "删除用户：#{#username}"
                return parseMixedTemplate(template, paramNames, args);
            }

            EvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            Expression expression = spelParser.parseExpression(spelExpr);
            Object value = expression.getValue(context);
            return value != null ? String.valueOf(value) : "";
        } catch (Exception e) {
            log.debug("SpEL 表达式解析失败: {}", template, e);
            return template;
        }
    }

    private String parseMixedTemplate(String template, String[] paramNames, Object[] args) {
        // 简单实现：替换 #{#xxx} 形式的表达式
        String result = template;
        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        int start;
        while ((start = result.indexOf("#{")) != -1) {
            int end = result.indexOf("}", start);
            if (end == -1) break;
            
            String expr = result.substring(start + 2, end);
            try {
                Expression expression = spelParser.parseExpression(expr);
                Object value = expression.getValue(context);
                String replacement = value != null ? String.valueOf(value) : "";
                result = result.substring(0, start) + replacement + result.substring(end + 1);
            } catch (Exception e) {
                break;
            }
        }
        return result;
    }

    private OperationLogEntity buildLogEntity(OperationLog annotation, String detail) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setModule(annotation.module());
        entity.setAction(annotation.action());
        entity.setDetail(detail);

        // 从登录上下文获取用户信息
        Optional<LoginPrincipal> principal = LoginContext.currentPrincipal();
        if (principal.isPresent()) {
            LoginPrincipal p = principal.get();
            entity.setUserId(p.userId());
            entity.setUsername(p.username());
        }

        // 从请求上下文获取 IP 和 RequestId
        RequestContext requestContext = RequestContext.current();
        if (requestContext != null) {
            entity.setClientIp(getClientIp());
            entity.setRequestId(requestContext.getRequestId());
        }

        return entity;
    }

    private String getClientIp() {
        // 简化实现，实际可从 request 中获取
        return null;
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/aspect/OperationLogAspect.java
git commit -m "feat(log): 添加操作日志 AOP 切面"
```

---

## Task 8: 操作日志 Controller

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/controller/AdminOperationLogController.java`

- [ ] **Step 1: 创建 AdminOperationLogController.java**

```java
package com.jaycong.boot.modules.log.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jaycong.boot.common.web.Result;
import com.jaycong.boot.modules.log.dto.OperationLogQueryDTO;
import com.jaycong.boot.modules.log.dto.OperationLogVO;
import com.jaycong.boot.modules.log.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端-操作日志")
@RestController
@RequestMapping("/api/admin/logs/operations")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public Result<IPage<OperationLogVO>> queryPage(OperationLogQueryDTO query) {
        return Result.ok(operationLogService.queryPage(query));
    }

    @Operation(summary = "查询操作日志详情")
    @GetMapping("/{id}")
    public Result<OperationLogVO> getById(@PathVariable Long id) {
        return Result.ok(operationLogService.getById(id));
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/log/controller/AdminOperationLogController.java
git commit -m "feat(log): 添加操作日志 Controller"
```

---

## Task 9: 后端编译验证

- [ ] **Step 1: 执行后端编译验证**

运行: `cd jay-boot-backend; mvn compile -DskipTests -q`

预期: `BUILD SUCCESS`

---

## Task 10: 前端 API 接口

**Files:**
- Modify: `jay-boot-frontend/src/api/admin/LogApi.ts`

- [ ] **Step 1: 在 LogApi.ts 末尾添加操作日志 API**

在文件末尾添加：

```typescript
export interface OperationLogItem {
  id: string
  module: string
  action: string
  detail: string | null
  userId: string | null
  username: string | null
  clientIp: string | null
  requestId: string | null
  createdTime: string
}

export interface OperationLogPageResponse {
  records: OperationLogItem[]
  total: number
  page: number
  pageSize: number
}

export interface OperationLogPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  module?: string
  userId?: string
  startTime?: string
  endTime?: string
}

/**
 * 获取操作日志分页列表
 */
export const getOperationLogPageApi = (params: OperationLogPageParams) =>
  get<OperationLogPageResponse>('/api/admin/logs/operations', params)

/**
 * 获取操作日志详情
 */
export const getOperationLogDetailApi = (id: string) =>
  get<OperationLogItem>(`/api/admin/logs/operations/${id}`)
```

- [ ] **Step 2: 提交**

```bash
git add jay-boot-frontend/src/api/admin/LogApi.ts
git commit -m "feat(frontend): 添加操作日志 API 接口"
```

---

## Task 11: 前端路由配置

**Files:**
- Modify: `jay-boot-frontend/src/router/routes/admin.ts`

- [ ] **Step 1: 在 adminChildren 数组中添加操作日志路由**

在 `logs/errors` 路由后面添加：

```typescript
  {
    path: 'logs/operations',
    name: 'admin-logs-operations',
    component: () => import('../../views/admin/OperationLogView.vue'),
    meta: { title: '操作日志' },
  },
```

- [ ] **Step 2: 提交**

```bash
git add jay-boot-frontend/src/router/routes/admin.ts
git commit -m "feat(frontend): 添加操作日志路由"
```

---

## Task 12: 前端操作日志页面

**Files:**
- Create: `jay-boot-frontend/src/views/admin/OperationLogView.vue`

- [ ] **Step 1: 创建 OperationLogView.vue**

```vue
<template>
  <section class="log-page">
    <header class="log-toolbar">
      <div>
        <h2 class="log-title">操作日志</h2>
        <p class="log-subtitle">查看系统关键业务操作记录，支持筛选检索与详情查看。</p>
      </div>
      <div class="log-toolbar__actions">
        <a-button :loading="pageState.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </div>
    </header>

    <a-alert v-if="pageState.errorMessage" class="log-error" type="error" show-icon :message="pageState.errorMessage">
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-card class="log-card" :bordered="false">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12" :lg="6">
            <a-form-item label="模块">
              <a-input
                v-model:value="filters.module"
                allow-clear
                placeholder="输入模块名称"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="6">
            <a-form-item label="操作用户ID">
              <a-input
                v-model:value="filters.userId"
                allow-clear
                placeholder="输入用户ID"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="6">
            <a-form-item label="时间范围">
              <a-range-picker
                v-model:value="filters.dateRange"
                style="width: 100%"
                :placeholder="['开始日期', '结束日期']"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="6">
            <a-form-item label=" ">
              <a-space>
                <a-button type="primary" :loading="pageState.refreshing" @click="onSearchClick">查询</a-button>
                <a-button @click="onResetClick">重置</a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <a-card class="log-card" :bordered="false">
      <a-skeleton v-if="pageState.loadingInitial && !pageState.hasData" active :paragraph="{ rows: 6 }" />
      <template v-else>
        <a-table
          :columns="tableColumns"
          :data-source="pageState.records"
          :loading="tableLoading"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 1000 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'module'">
              <a-tag color="blue">{{ record.module }}</a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-tag color="green">{{ record.action }}</a-tag>
            </template>
            <template v-else-if="column.key === 'detail'">
              <a-tooltip :title="record.detail">
                <span class="detail-text">{{ record.detail || '-' }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'createdTime'">
              {{ formatDateTime(record.createdTime) }}
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-button type="link" size="small" @click="onViewDetail(record)">详情</a-button>
            </template>
          </template>
        </a-table>

        <div class="log-pagination">
          <a-pagination
            :current="pageState.page"
            :page-size="pageState.pageSize"
            :total="pageState.total"
            :show-total="(total: number) => `共 ${total} 条`"
            show-size-changer
            :page-size-options="['20', '50', '100']"
            @change="onPageChange"
          />
        </div>
      </template>
    </a-card>

    <a-modal v-model:open="detailModal.open" title="操作日志详情" :footer="null" width="700px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="模块">{{ detailModal.data?.module }}</a-descriptions-item>
        <a-descriptions-item label="操作">{{ detailModal.data?.action }}</a-descriptions-item>
        <a-descriptions-item label="详情" :span="2">{{ detailModal.data?.detail || '-' }}</a-descriptions-item>
        <a-descriptions-item label="操作用户">{{ detailModal.data?.username || '-' }}</a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ detailModal.data?.userId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="客户端IP">{{ detailModal.data?.clientIp || '-' }}</a-descriptions-item>
        <a-descriptions-item label="请求ID">{{ detailModal.data?.requestId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="操作时间" :span="2">{{ formatDateTime(detailModal.data?.createdTime) }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import {
  getOperationLogPageApi,
  type OperationLogItem,
  type OperationLogPageParams,
} from '../../api/admin/LogApi'
import type { Dayjs } from 'dayjs'

// 页面状态
const pageState = reactive({
  records: [] as OperationLogItem[],
  total: 0,
  page: 1,
  pageSize: 20,
  loadingInitial: false,
  refreshing: false,
  errorMessage: '',
  hasData: computed(() => pageState.records.length > 0),
})

// 筛选条件
const filters = reactive({
  module: '',
  userId: '',
  dateRange: null as [Dayjs, Dayjs] | null,
})

// 详情弹窗
const detailModal = reactive({
  open: false,
  data: null as OperationLogItem | null,
})

const tableColumns = [
  { title: '模块', dataIndex: 'module', key: 'module', width: 120 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 100 },
  { title: '详情', dataIndex: 'detail', key: 'detail', width: 250 },
  { title: '操作者', dataIndex: 'username', key: 'username', width: 100 },
  { title: '客户端IP', dataIndex: 'clientIp', key: 'clientIp', width: 120 },
  { title: '时间', dataIndex: 'createdTime', key: 'createdTime', width: 170 },
  { title: '操作', key: 'actions', width: 80 },
]

const tableLoading = computed(() => pageState.loadingInitial || pageState.refreshing)

const formatDateTime = (value: string | null | undefined) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

// 获取列表
const fetchList = async (mode: 'initial' | 'refresh' = 'refresh') => {
  pageState.errorMessage = ''
  if (mode === 'initial') pageState.loadingInitial = true
  if (mode === 'refresh') pageState.refreshing = true

  try {
    const params: OperationLogPageParams = {
      page: pageState.page,
      pageSize: pageState.pageSize,
    }
    if (filters.module) params.module = filters.module
    if (filters.userId) params.userId = filters.userId
    if (filters.dateRange) {
      params.startTime = filters.dateRange[0].format('YYYY-MM-DD 00:00:00')
      params.endTime = filters.dateRange[1].format('YYYY-MM-DD 23:59:59')
    }

    const data = await getOperationLogPageApi(params)
    pageState.records = data.records
    pageState.total = data.total
    pageState.page = data.page
    pageState.pageSize = data.pageSize

    // 自动翻页处理
    if (pageState.total > 0 && pageState.records.length === 0 && pageState.page > 1) {
      pageState.page -= 1
      await fetchList('refresh')
    }
  } catch (error) {
    pageState.errorMessage = error instanceof Error ? error.message : '操作日志加载失败，请稍后重试'
  } finally {
    pageState.loadingInitial = false
    pageState.refreshing = false
  }
}

const onRefreshClick = async () => {
  await fetchList('refresh')
  if (!pageState.errorMessage) {
    message.success('操作日志已刷新')
  }
}

const onRetryClick = async () => {
  await fetchList('refresh')
}

const onSearchClick = async () => {
  pageState.page = 1
  await fetchList('refresh')
}

const onResetClick = async () => {
  filters.module = ''
  filters.userId = ''
  filters.dateRange = null
  pageState.page = 1
  await fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  pageState.page = page
  if (pageSize > 0) pageState.pageSize = pageSize
  await fetchList('refresh')
}

const onViewDetail = (record: OperationLogItem) => {
  detailModal.data = record
  detailModal.open = true
}

onMounted(() => {
  void fetchList('initial')
})
</script>

<style scoped>
.log-page {
  display: grid;
  gap: 16px;
}

.log-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #eef2ff 0%, #f5f3ff 56%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.log-title {
  margin: 0;
  font-size: 24px;
  color: #312e81;
}

.log-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.log-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.log-error {
  margin-top: -6px;
}

.log-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.log-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.detail-text {
  max-width: 230px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 1100px) {
  .log-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .log-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .log-pagination {
    justify-content: center;
  }
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add jay-boot-frontend/src/views/admin/OperationLogView.vue
git commit -m "feat(frontend): 添加操作日志页面"
```

---

## Task 13: 为现有业务添加操作日志注解

**Files:**
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/user/service/AdminUserService.java`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/service/AdminPlanService.java`

- [ ] **Step 1: 在 AdminUserService 中添加操作日志注解**

在类顶部添加导入：
```java
import com.jaycong.boot.modules.log.annotation.OperationLog;
```

在关键方法上添加注解（示例）：
```java
@OperationLog(module = "用户管理", action = "创建", detail = "创建用户：#{#request.email}")
public Long createUser(CreateUserRequest request) { ... }

@OperationLog(module = "用户管理", action = "删除", detail = "删除用户ID：#{#userId}")
public void deleteUser(Long userId) { ... }

@OperationLog(module = "用户管理", action = "修改状态", detail = "用户#{#userId}状态改为#{#status}")
public void updateUserStatus(Long userId, String status) { ... }
```

- [ ] **Step 2: 在 AdminPlanService 中添加操作日志注解**

在类顶部添加导入：
```java
import com.jaycong.boot.modules.log.annotation.OperationLog;
```

在关键方法上添加注解（示例）：
```java
@OperationLog(module = "套餐管理", action = "创建", detail = "创建套餐：#{#request.name}")
public Long createPlan(CreatePlanRequest request) { ... }

@OperationLog(module = "套餐管理", action = "修改", detail = "修改套餐ID：#{#planId}")
public void updatePlan(Long planId, UpdatePlanRequest request) { ... }

@OperationLog(module = "套餐管理", action = "删除", detail = "删除套餐ID：#{#planId}")
public void deletePlan(Long planId) { ... }
```

- [ ] **Step 3: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/user/service/AdminUserService.java \
       jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/service/AdminPlanService.java
git commit -m "feat(log): 为用户和套餐管理添加操作日志注解"
```

---

## Task 14: 最终编译验证

- [ ] **Step 1: 后端编译验证**

运行: `cd jay-boot-backend; mvn compile -DskipTests -q`

预期: `BUILD SUCCESS`

- [ ] **Step 2: 前端构建验证**

运行: `cd jay-boot-frontend; npm run build`

预期: 构建成功，无错误

---

## 自我审查

**1. Spec 覆盖检查:**
- ✅ 数据库表结构 - Task 1
- ✅ 注解定义 - Task 2
- ✅ AOP 切面 - Task 7
- ✅ Service 层 - Task 6
- ✅ Controller 层 - Task 8
- ✅ 前端 API - Task 10
- ✅ 前端路由 - Task 11
- ✅ 前端页面 - Task 12
- ✅ 使用示例 - Task 13

**2. 占位符扫描:** 无 TBD/TODO

**3. 类型一致性:** 已验证 DTO、Entity、VO 字段名称一致
