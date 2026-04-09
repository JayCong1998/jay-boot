# Billing 模块（阶段一）设计文档

## 1. 背景与目标
- 项目：Java Micro-SaaS Starter
- 日期：2026-04-01
- 范围：先完成 Billing 的“套餐与订阅主流程”，暂不实现支付回调、发票与对账。

阶段一目标：
1. 实现套餐查询能力。
2. 实现当前租户订阅创建能力。
3. 实现订阅状态更新能力（受状态机约束）。
4. 确保租户隔离与统一异常语义。

## 2. 范围边界
### 2.1 本阶段包含
- 数据表：`plans`、`subscriptions`
- 接口：
  - `GET /api/billing/plans`
  - `POST /api/billing/subscriptions`
  - `POST /api/billing/subscriptions/{id}/update`
- 订阅状态机（集中在 Service）

### 2.2 本阶段不包含
- 支付回调接口（微信/支付宝/Stripe）
- 订单与计费事件（`orders`、`billing_events`）
- 发票流程（`invoices`）

## 3. 设计方案与取舍
已确认采用“领域状态机版”：
- 在 Service 层集中承载状态流转规则。
- Controller 仅负责参数校验与调用编排。
- 优先保证规则可维护性，避免状态逻辑散落。

## 4. 数据模型
### 4.1 `plans`
字段建议：
- `id` BIGINT PK
- `code` VARCHAR(64) NOT NULL（套餐唯一编码）
- `name` VARCHAR(64) NOT NULL
- `billing_cycle` VARCHAR(32) NOT NULL（如 MONTHLY/YEARLY）
- `quota_json` TEXT NOT NULL（额度配置 JSON）
- `price` BIGINT NOT NULL（以最小货币单位保存）
- `status` VARCHAR(32) NOT NULL（ACTIVE/INACTIVE）
- 公共字段：`creator_id`、`creator_name`、`created_time`、`updater_id`、`updater_name`、`updated_time`、`is_deleted`

约束建议：
- 唯一键：`uk_plans_code_deleted (code, is_deleted)`
- 索引：`idx_plans_status (status)`

### 4.2 `subscriptions`
字段建议：
- `id` BIGINT PK
- `tenant_id` BIGINT NOT NULL
- `plan_id` BIGINT NOT NULL
- `status` VARCHAR(32) NOT NULL（TRIALING/ACTIVE/PAST_DUE/CANCELED）
- `trial_end_at` DATETIME(3) NULL
- `current_period_end` DATETIME(3) NULL
- `effective_time` DATETIME(3) NOT NULL
- `cancel_time` DATETIME(3) NULL
- 公共字段：同上

约束建议：
- 索引：`idx_subscriptions_tenant_status (tenant_id, status)`
- 索引：`idx_subscriptions_tenant_plan (tenant_id, plan_id)`
- 可选唯一策略（若要求单租户仅一个有效订阅）：在业务层限制单租户有效订阅唯一

## 5. API 契约
### 5.1 `GET /api/billing/plans`
- 语义：查询可用套餐列表
- 返回：`code/name/billingCycle/quotaJson/price/status` 集合
- 鉴权：建议登录后可用（阶段一可先登录校验）

### 5.2 `POST /api/billing/subscriptions`
- 语义：为当前租户创建订阅
- 入参：
  - `planId`（必填）
  - `initialStatus`（可选，默认 `TRIALING`）
  - `trialEndAt`（可选）
  - `currentPeriodEnd`（可选）
- 规则：
  - 套餐必须存在且可用
  - 若限制单活跃订阅，已有有效订阅时返回冲突

### 5.3 `POST /api/billing/subscriptions/{id}/update`
- 语义：更新订阅状态/周期信息
- 入参：
  - `targetStatus`（必填）
  - `currentPeriodEnd`（可选）
  - `cancelTime`（当取消时可选）
- 规则：必须通过状态机校验

## 6. 订阅状态机
状态集合：
- `TRIALING`
- `ACTIVE`
- `PAST_DUE`
- `CANCELED`

合法流转：
1. 创建时：`TRIALING` 或 `ACTIVE`
2. `TRIALING -> ACTIVE`
3. `ACTIVE -> PAST_DUE`
4. `PAST_DUE -> ACTIVE`
5. `ACTIVE|PAST_DUE -> CANCELED`

非法流转处理：
- 统一抛出 `BusinessException(ErrorCode.BAD_REQUEST, "非法的订阅状态流转")`

## 7. 服务边界与职责
- `BillingController`
  - 参数校验
  - 调用 `BillingService`
  - 不承载状态规则
- `BillingService`
  - 套餐校验
  - 当前租户解析
  - 订阅创建与更新
  - 状态机校验
- `Mapper`
  - 基于 MyBatis-Plus 提供 CRUD 与 wrapper 查询

## 8. 多租户与安全约束
- `subscriptions` 的查询与更新必须附带 `tenant_id = 当前登录用户tenant_id`
- 禁止跨租户访问订阅记录，违规则返回 404
- 统一沿用 `Sa-Token + BusinessException + GlobalExceptionHandler`

## 9. 错误语义
- 未登录：401
- 套餐不存在/不可用：404
- 订阅不存在或跨租户：404
- 非法状态流转：400
- 重复有效订阅冲突：409
- 未知异常：500

## 10. 落地顺序
1. 更新 `database/schema.sql` 与 `src/test/resources/schema.sql`，新增 `plans`、`subscriptions`
2. 新增 Billing 模块实体、Mapper、DTO、Service、Controller
3. 接入当前租户解析与状态机校验
4. 自检接口与异常语义

## 11. 风险与缓解
- 风险：状态流转规则分散导致维护困难  
  缓解：规则集中在 `BillingService` 的单一方法内处理
- 风险：跨租户越权更新  
  缓解：所有订阅查询都强制 `tenant_id` 条件
- 风险：订阅与支付后续对接改动大  
  缓解：提前保留 `status/current_period_end/cancel_time` 等关键字段
