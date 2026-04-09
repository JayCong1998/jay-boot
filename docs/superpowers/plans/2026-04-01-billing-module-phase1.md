# Billing 阶段一（套餐与订阅）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成 Billing 阶段一能力：套餐查询、当前租户订阅创建、订阅状态更新（状态机约束）。

**Architecture:** 采用“Controller 轻编排 + Service 状态机集中规则 + Mapper 持久化”结构。`BillingService` 统一处理订阅状态流转与租户隔离，Controller 仅做参数校验与调用。数据库先落 `plans/subscriptions` 两表，为后续支付回调和订单模块预留扩展字段。

**Tech Stack:** Spring Boot 3.3.x、MyBatis-Plus、Sa-Token、MySQL/H2、Swagger(OpenAPI)

---

## 文件结构与职责

- `database/schema.sql`
  - 新增 MySQL 的 `plans`、`subscriptions` 表结构与索引
- `src/test/resources/schema.sql`
  - 新增 H2 对应表结构与索引
- `src/main/java/com/jaycong/boot/modules/billing/domain/SubscriptionStatus.java`
  - 订阅状态枚举（TRIALING/ACTIVE/PAST_DUE/CANCELED）
- `src/main/java/com/jaycong/boot/modules/billing/entity/PlanEntity.java`
  - 套餐实体映射 `plans`
- `src/main/java/com/jaycong/boot/modules/billing/entity/SubscriptionEntity.java`
  - 订阅实体映射 `subscriptions`
- `src/main/java/com/jaycong/boot/modules/billing/mapper/PlanMapper.java`
  - 套餐数据访问
- `src/main/java/com/jaycong/boot/modules/billing/mapper/SubscriptionMapper.java`
  - 订阅数据访问
- `src/main/java/com/jaycong/boot/modules/billing/dto/PlanView.java`
  - 套餐响应 DTO
- `src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionCreateRequest.java`
  - 创建订阅请求 DTO
- `src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionUpdateRequest.java`
  - 更新订阅请求 DTO
- `src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionView.java`
  - 订阅响应 DTO
- `src/main/java/com/jaycong/boot/modules/billing/service/BillingService.java`
  - Billing 业务核心（租户校验 + 状态机）
- `src/main/java/com/jaycong/boot/modules/billing/controller/BillingController.java`
  - Billing API 暴露（含 Swagger 注解）
- `docs/superpowers/specs/2026-04-01-billing-module-design.md`
  - 若实现细节变化，回写设计说明

> 说明：按仓库 AGENTS 约束，“未经允许不新增测试用例”，本计划不创建新测试文件，使用编译 + 接口验收方式校验。

### Task 1: 新增 Billing 表结构（MySQL/H2）

**Files:**
- Modify: `database/schema.sql`
- Modify: `src/test/resources/schema.sql`

- [ ] **Step 1: 在 MySQL 脚本新增 `plans` 表**

```sql
CREATE TABLE IF NOT EXISTS plans (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    billing_cycle VARCHAR(32) NOT NULL,
    quota_json TEXT NOT NULL,
    price BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_plans_code_deleted (code, is_deleted),
    KEY idx_plans_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 在 MySQL 脚本新增 `subscriptions` 表**

```sql
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    trial_end_at DATETIME(3) NULL,
    current_period_end DATETIME(3) NULL,
    effective_time DATETIME(3) NOT NULL,
    cancel_time DATETIME(3) NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_subscriptions_tenant_status (tenant_id, status),
    KEY idx_subscriptions_tenant_plan (tenant_id, plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 3: 在 H2 脚本镜像新增两张表和索引**

```sql
CREATE TABLE IF NOT EXISTS plans (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    billing_cycle VARCHAR(32) NOT NULL,
    quota_json CLOB NOT NULL,
    price BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_plans_code_deleted ON plans(code, is_deleted);
CREATE INDEX IF NOT EXISTS idx_plans_status ON plans(status);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    trial_end_at TIMESTAMP(3),
    current_period_end TIMESTAMP(3),
    effective_time TIMESTAMP(3) NOT NULL,
    cancel_time TIMESTAMP(3),
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_status ON subscriptions(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_plan ON subscriptions(tenant_id, plan_id);
```

- [ ] **Step 4: 提交数据库变更**

```bash
git add database/schema.sql src/test/resources/schema.sql
git commit -m "chore: add billing plans and subscriptions schema"
```

### Task 2: 新增 Billing 领域实体与 Mapper

**Files:**
- Create: `src/main/java/com/jaycong/boot/modules/billing/domain/SubscriptionStatus.java`
- Create: `src/main/java/com/jaycong/boot/modules/billing/entity/PlanEntity.java`
- Create: `src/main/java/com/jaycong/boot/modules/billing/entity/SubscriptionEntity.java`
- Create: `src/main/java/com/jaycong/boot/modules/billing/mapper/PlanMapper.java`
- Create: `src/main/java/com/jaycong/boot/modules/billing/mapper/SubscriptionMapper.java`

- [ ] **Step 1: 新增订阅状态枚举**

```java
package com.jaycong.boot.modules.billing.domain;

public enum SubscriptionStatus {
    TRIALING,
    ACTIVE,
    PAST_DUE,
    CANCELED
}
```

- [ ] **Step 2: 新增套餐实体 `PlanEntity`**

```java
package com.jaycong.boot.modules.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plans")
public class PlanEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String code;
    private String name;
    private String billingCycle;
    private String quotaJson;
    private Long price;
    private String status;
}
```

- [ ] **Step 3: 新增订阅实体 `SubscriptionEntity`**

```java
package com.jaycong.boot.modules.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subscriptions")
public class SubscriptionEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long planId;
    private String status;
    private LocalDateTime trialEndAt;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime effectiveTime;
    private LocalDateTime cancelTime;
}
```

- [ ] **Step 4: 新增 Mapper 接口**

```java
@Mapper
public interface PlanMapper extends BaseMapper<PlanEntity> {}

@Mapper
public interface SubscriptionMapper extends BaseMapper<SubscriptionEntity> {}
```

- [ ] **Step 5: 编译检查并提交**

Run: `mvn -DskipTests compile`  
Expected: `BUILD SUCCESS`

```bash
git add src/main/java/com/jaycong/boot/modules/billing/domain/SubscriptionStatus.java \
        src/main/java/com/jaycong/boot/modules/billing/entity/PlanEntity.java \
        src/main/java/com/jaycong/boot/modules/billing/entity/SubscriptionEntity.java \
        src/main/java/com/jaycong/boot/modules/billing/mapper/PlanMapper.java \
        src/main/java/com/jaycong/boot/modules/billing/mapper/SubscriptionMapper.java
git commit -m "feat: add billing entities, enum and mappers"
```

### Task 3: 新增 Billing DTO 与 Controller 契约

**Files:**
- Create: `src/main/java/com/jaycong/boot/modules/billing/dto/PlanView.java`
- Create: `src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionCreateRequest.java`
- Create: `src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionUpdateRequest.java`
- Create: `src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionView.java`
- Create: `src/main/java/com/jaycong/boot/modules/billing/controller/BillingController.java`

- [ ] **Step 1: 新增 Billing 请求/响应 DTO（含校验与 Swagger 注解）**

```java
@Schema(description = "创建订阅请求")
public record SubscriptionCreateRequest(
        @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "套餐 ID 不能为空")
        @Positive(message = "套餐 ID 必须为正数")
        Long planId,
        @Schema(description = "初始状态，默认 TRIALING", example = "TRIALING")
        String initialStatus,
        @Schema(description = "试用结束时间")
        LocalDateTime trialEndAt,
        @Schema(description = "当前周期结束时间")
        LocalDateTime currentPeriodEnd
) {}
```

- [ ] **Step 2: 新增 BillingController 并暴露 3 个接口**

```java
@RestController
@RequestMapping("/api/billing")
@Validated
@Tag(name = "Billing 管理", description = "套餐与订阅管理")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @Operation(summary = "查询套餐列表", security = @SecurityRequirement(name = "satoken"))
    @GetMapping("/plans")
    public ApiResponse<List<PlanView>> plans() {
        return ApiResponse.success(billingService.listPlans());
    }

    @Operation(summary = "创建当前租户订阅", security = @SecurityRequirement(name = "satoken"))
    @PostMapping("/subscriptions")
    public ApiResponse<SubscriptionView> createSubscription(@Valid @RequestBody SubscriptionCreateRequest request) {
        return ApiResponse.success(billingService.createSubscription(request));
    }

    @Operation(summary = "更新订阅", security = @SecurityRequirement(name = "satoken"))
    @PostMapping("/subscriptions/{id}/update")
    public ApiResponse<SubscriptionView> updateSubscription(
            @PathVariable("id") Long id,
            @Valid @RequestBody SubscriptionUpdateRequest request) {
        return ApiResponse.success(billingService.updateSubscription(id, request));
    }
}
```

- [ ] **Step 3: 编译检查并提交**

Run: `mvn -DskipTests compile`  
Expected: `BUILD SUCCESS`

```bash
git add src/main/java/com/jaycong/boot/modules/billing/dto/PlanView.java \
        src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionCreateRequest.java \
        src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionUpdateRequest.java \
        src/main/java/com/jaycong/boot/modules/billing/dto/SubscriptionView.java \
        src/main/java/com/jaycong/boot/modules/billing/controller/BillingController.java
git commit -m "feat: add billing controller and dto contracts"
```

### Task 4: 实现 BillingService（状态机 + 租户隔离）

**Files:**
- Create: `src/main/java/com/jaycong/boot/modules/billing/service/BillingService.java`

- [ ] **Step 1: 实现套餐查询逻辑**

```java
public List<PlanView> listPlans() {
    requireCurrentUser();
    List<PlanEntity> plans = planMapper.selectList(new LambdaQueryWrapper<PlanEntity>()
            .eq(PlanEntity::getStatus, "ACTIVE")
            .orderByAsc(PlanEntity::getPrice));
    return plans.stream()
            .map(this::toPlanView)
            .toList();
}
```

- [ ] **Step 2: 实现创建当前租户订阅**

```java
@Transactional
public SubscriptionView createSubscription(SubscriptionCreateRequest request) {
    UserEntity user = requireCurrentUser();
    PlanEntity plan = requireActivePlan(request.planId());
    SubscriptionStatus init = parseInitialStatus(request.initialStatus());

    SubscriptionEntity entity = new SubscriptionEntity();
    entity.setTenantId(user.getTenantId());
    entity.setPlanId(plan.getId());
    entity.setStatus(init.name());
    entity.setTrialEndAt(request.trialEndAt());
    entity.setCurrentPeriodEnd(request.currentPeriodEnd());
    entity.setEffectiveTime(LocalDateTime.now());
    subscriptionMapper.insert(entity);
    return toSubscriptionView(entity);
}
```

- [ ] **Step 3: 实现更新订阅与状态机校验**

```java
@Transactional
public SubscriptionView updateSubscription(Long subscriptionId, SubscriptionUpdateRequest request) {
    UserEntity user = requireCurrentUser();
    SubscriptionEntity entity = requireTenantSubscription(user.getTenantId(), subscriptionId);

    SubscriptionStatus from = SubscriptionStatus.valueOf(entity.getStatus());
    SubscriptionStatus to = SubscriptionStatus.valueOf(request.targetStatus().trim().toUpperCase(Locale.ROOT));
    if (!canTransition(from, to)) {
        throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的订阅状态流转");
    }

    entity.setStatus(to.name());
    entity.setCurrentPeriodEnd(request.currentPeriodEnd());
    if (to == SubscriptionStatus.CANCELED) {
        entity.setCancelTime(request.cancelTime() == null ? LocalDateTime.now() : request.cancelTime());
    }
    subscriptionMapper.updateById(entity);
    return toSubscriptionView(entity);
}
```

- [ ] **Step 4: 实现状态机规则方法**

```java
private boolean canTransition(SubscriptionStatus from, SubscriptionStatus to) {
    if (from == to) {
        return true;
    }
    return switch (from) {
        case TRIALING -> to == SubscriptionStatus.ACTIVE;
        case ACTIVE -> to == SubscriptionStatus.PAST_DUE || to == SubscriptionStatus.CANCELED;
        case PAST_DUE -> to == SubscriptionStatus.ACTIVE || to == SubscriptionStatus.CANCELED;
        case CANCELED -> false;
    };
}
```

- [ ] **Step 5: 编译检查并提交**

Run: `mvn -DskipTests compile`  
Expected: `BUILD SUCCESS`

```bash
git add src/main/java/com/jaycong/boot/modules/billing/service/BillingService.java
git commit -m "feat: implement billing service with subscription state machine"
```

### Task 5: 接口验收与文档对齐

**Files:**
- Modify: `docs/superpowers/specs/2026-04-01-billing-module-design.md`（如实现有偏差）

- [ ] **Step 1: 启动应用并进行接口验收（不新增测试文件）**

Run: `mvn spring-boot:run`
Expected: `Started JayBootApplication`

验收用例（示例）：
```bash
# 1) 登录获取 token（示例）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"owner@example.com","password":"Password123"}'

# 2) 查询套餐
curl http://localhost:8080/api/billing/plans -H "satoken: <token>"

# 3) 创建订阅
curl -X POST http://localhost:8080/api/billing/subscriptions \
  -H "Content-Type: application/json" \
  -H "satoken: <token>" \
  -d '{"planId":1,"initialStatus":"TRIALING"}'

# 4) 合法流转：TRIALING -> ACTIVE
curl -X POST http://localhost:8080/api/billing/subscriptions/<id>/update \
  -H "Content-Type: application/json" \
  -H "satoken: <token>" \
  -d '{"targetStatus":"ACTIVE"}'
```

- [ ] **Step 2: 检查非法状态流转返回 400**

```bash
curl -X POST http://localhost:8080/api/billing/subscriptions/<id>/update \
  -H "Content-Type: application/json" \
  -H "satoken: <token>" \
  -d '{"targetStatus":"TRIALING"}'
```

Expected: `HTTP 400` 且 `code=400`

- [ ] **Step 3: 对齐设计文档并提交**

```bash
git add docs/superpowers/specs/2026-04-01-billing-module-design.md
git commit -m "docs: align billing phase1 design with implementation"
```

## 自检结果

### 1) 规格覆盖检查
- `plans/subscriptions` 数据表：Task 1 覆盖。
- 3 个 Billing API：Task 3 + Task 4 覆盖。
- 状态机规则：Task 4 Step 3/4 覆盖。
- 租户隔离：Task 4 的 `requireTenantSubscription` 查询约束覆盖。
- 错误语义（400/401/404/409）：Task 4 业务异常与全局异常处理链路覆盖。

### 2) 占位符检查
- 未使用 TODO/TBD/“后续补充”等占位描述。
- 每个改代码步骤均给出明确代码片段和文件路径。

### 3) 类型一致性检查
- 状态统一使用 `SubscriptionStatus` 枚举。
- 订阅实体状态字段与 DTO 的 `targetStatus` 在 Service 内统一转换。
- API 路径与规格一致：`/api/billing/*`。

