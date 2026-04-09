# Java Micro-SaaS Starter 技术开发文档（TDD）

## 1. 文档目标
基于《Java Micro-SaaS Starter PRD v1.0》，输出可执行的技术方案，指导 v1（MVP）研发落地，目标在 1-2 周内完成可收费 SaaS 基础能力上线。

## 2. 技术目标与范围
### 2.1 MVP 范围（v1）
- Auth（注册登录、Sa-Token 鉴权、用户资料）
- Tenant（伪多租户，workspace 自动创建，核心数据 tenant_id 隔离）
- RBAC（User-Role-Permission-API）
- Billing（套餐、订阅、Stripe Webhook、订单/发票）
- API Key + 用量统计（限流、额度、超额控制）
- AI Gateway（统一 /ai/chat、/ai/embedding、/ai/image）

### 2.2 非功能目标
- 支持 10k 用户
- 支持 100 万 API 调用/月
- Docker 单机可部署
- 安全基线：Sa-Token + RBAC + 限流 + 审计日志

## 3. 总体架构
采用“模块化单体 + 可演进微服务”架构：
- 早期以单体部署提升交付速度
- 通过清晰模块边界（domain package）支持后续拆分
- 异步任务与计费事件通过消息队列解耦

架构分层：
- API Layer：REST Controller、鉴权、参数校验
- Application Layer：用例编排、事务管理
- Domain Layer：核心领域模型与规则
- Infrastructure Layer：DB、缓存、消息、第三方（Stripe/AI Provider）

## 4. 技术选型
- 语言与框架：Java 21 + Spring Boot 3.x
- 安全：Sa-Token（登录态、权限、踢人下线、会话管理）
- 数据库：MySQL 8.0
- ORM：MyBatis-Plus（通用 CRUD + Wrapper + 分页插件）
- 缓存：Redis（Token 黑名单、限流计数、短期缓存）
- 消息队列：RabbitMQ（订单事件、通知事件、异步任务解耦）
- 任务调度：Spring Scheduler + MQ Worker
- 支付：微信支付 v3 / 支付宝当面付（服务端回调）
- AI 接入：OpenAI/Anthropic/本地模型适配器模式
- 可观测性：Micrometer + Prometheus + Grafana + Loki
- 部署：Docker Compose（v1），预留 K8s 清单（v1.1）

## 5. 模块设计
### 5.1 Auth 模块
功能：邮箱注册、密码登录、访问令牌刷新、密码修改、登录日志。
关键设计：
- 采用 Sa-Token 统一登录态管理（token 超时、续签、踢下线）
- 登录会话与权限缓存写入 Redis
- 密码使用 BCrypt

### 5.2 Tenant 模块（伪多租户）
功能：用户注册后自动创建默认 workspace（tenant）。
规则：
- 业务核心表强制 tenant_id
- 查询默认附带 tenant_id 条件
- 管理员跨租户操作需显式权限

### 5.3 RBAC 模块
模型：user_role、role_permission、permission。
能力：
- API 级权限（如 `billing.subscription.update`）
- 后台菜单权限（可选）
- 注解式鉴权：`@PreAuthorize`

### 5.4 Billing 模块
功能：套餐、订阅生命周期、订单记录、发票记录、Webhook 对账。
关键设计：
- 微信/支付宝为支付真源，平台做状态镜像
- 支付回调事件幂等（provider + out_trade_no + trade_status 唯一约束）
- 状态机：trialing -> active -> past_due -> canceled

### 5.5 API Key + 用量模块
功能：创建/禁用 API Key、限流、套餐额度控制。
关键设计：
- Key 仅显示一次明文，库内保存 hash
- 按 tenant + api_key + day 聚合用量
- 限流算法：令牌桶（Redis 实现）

### 5.6 AI Gateway 模块
统一接口：
- `POST /ai/chat`
- `POST /ai/embedding`
- `POST /ai/image`

关键设计：
- Provider SPI 抽象（OpenAI/Anthropic/本地）
- 统一计费单位（token/request）
- 失败重试与降级（可配置）

### 5.7 异步任务模块（v1 基础）
任务类型：邮件发送、AI 长任务、报表生成。
要求：
- 任务状态机（PENDING/RUNNING/SUCCESS/FAILED）
- 死信重试与告警

### 5.8 通知模块（v1 基础）
渠道：邮件、Webhook、站内通知。
要求：
- 模板化通知
- 通知事件可追踪（request_id）

## 6. 数据库设计（核心表）
### 6.0 全表公共字段规范（强制）
除中间关系表可按需裁剪外，业务表必须包含以下公共字段：
- `creator_id` BIGINT NOT NULL COMMENT '创建人ID'
- `creator_name` VARCHAR(64) NOT NULL COMMENT '创建人名称'
- `created_time` DATETIME(3) NOT NULL COMMENT '创建时间'
- `updater_id` BIGINT NOT NULL COMMENT '修改人ID'
- `updater_name` VARCHAR(64) NOT NULL COMMENT '修改人名称'
- `updated_time` DATETIME(3) NOT NULL COMMENT '修改时间'

建议同时包含：
- `id` BIGINT 主键（雪花或号段）
- `tenant_id` BIGINT（多租户业务表必填）
- `is_deleted` TINYINT(1) NOT NULL DEFAULT 0（逻辑删除）

MyBatis-Plus 落地建议：
- `MetaObjectHandler` 自动填充 `created_time/updated_time`
- 登录上下文自动注入 `creator_*/updater_*`
- 使用 `@TableLogic` 管理逻辑删除字段

### 6.1 用户与租户
- users(id, tenant_id, email, password_hash, status, ...公共字段)
- tenants(id, name, owner_user_id, plan_code, ...公共字段)
- user_tenant(id, tenant_id, user_id, role_in_tenant, ...公共字段)
- login_logs(id, tenant_id, user_id, ip, ua, success, ...公共字段)

### 6.2 权限
- roles(id, tenant_id, name, ...公共字段)
- permissions(id, code, description, ...公共字段)
- role_permissions(id, tenant_id, role_id, permission_id, ...公共字段)
- user_roles(id, tenant_id, user_id, role_id, ...公共字段)

### 6.3 订阅与支付
- plans(id, code, name, billing_cycle, quota_json, ...公共字段)
- subscriptions(id, tenant_id, plan_id, status, trial_end_at, current_period_end, ...公共字段)
- orders(id, tenant_id, provider, out_trade_no, amount, currency, status, ...公共字段)
- invoices(id, tenant_id, order_id, invoice_url, status, ...公共字段)
- billing_events(id, tenant_id, provider, out_trade_no, trade_status, payload_json, processed_time, ...公共字段)

### 6.4 API Key 与用量
- api_keys(id, tenant_id, name, key_hash, status, last_used_at, ...公共字段)
- usage_daily(id, tenant_id, api_key_id, date, request_count, token_count, cost, ...公共字段)
- rate_limit_policies(id, tenant_id, scope, qps, burst, ...公共字段)

### 6.5 AI 与异步任务
- ai_requests(id, tenant_id, provider, model, endpoint, token_in, token_out, cost, status, ...公共字段)
- jobs(id, tenant_id, job_type, payload_json, status, retry_count, next_retry_at, ...公共字段)
- notifications(id, tenant_id, channel, template_code, payload_json, status, ...公共字段)

## 7. API 设计（示例）
### 7.0 请求方式规范（强制）
- 项目 API 仅允许使用 `GET`（查询）和 `POST`（新增/修改/删除/动作指令）。  
- 不允许使用 `PUT`、`PATCH`、`DELETE`。

### 7.1 Auth
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/logout`
- `GET /auth/session`（获取当前 Sa-Token 会话信息）

### 7.2 Tenant
- `GET /tenants/current`
- `POST /tenants/current/update`

### 7.3 RBAC
- `GET /rbac/roles`
- `POST /rbac/roles`
- `POST /rbac/roles/{id}/permissions`

### 7.4 Billing
- `GET /billing/plans`
- `POST /billing/subscriptions`
- `POST /billing/subscriptions/{id}/update`
- `POST /webhooks/pay/wx`
- `POST /webhooks/pay/alipay`

### 7.5 API Key + Usage
- `GET /apikeys`
- `POST /apikeys`
- `POST /apikeys/{id}/disable`
- `GET /usage/daily`

### 7.6 AI Gateway
- `POST /ai/chat`
- `POST /ai/embedding`
- `POST /ai/image`

## 8. 安全与合规设计
- 鉴权：Sa-Token 登录态 + 权限注解 + 会话续签策略
- 密钥：API Key 哈希存储，不落盘明文
- 授权：RBAC + tenant_id 强约束
- 防护：限流、验证码（可选）、登录失败锁定、微信/支付宝回调签名校验
- 审计：关键操作审计日志（登录、权限变更、订阅变更、Key 管理）

## 9. 可观测性与运维
- 日志：结构化 JSON + trace_id/request_id
- 指标：登录成功率、接口 RT、错误率、支付回调处理成功率、AI 成本
- 告警：支付回调失败、任务堆积、5xx 激增、成本超阈值

## 10. 开发计划（2 周节奏）
### Week 1
- D1-D2：项目脚手架、用户认证、租户模型
- D3-D4：RBAC 与鉴权中间件
- D5：API Key 与基础限流

### Week 2
- D6-D7：Billing（套餐/订阅/Webhook）
- D8：AI Gateway Provider 抽象与统一接口
- D9：用量统计、异步任务框架、通知基础能力
- D10：联调、压测、安全检查、发布

## 11. 测试策略
- 单元测试：领域服务、权限判断、计费规则
- 集成测试：Auth/Billing/AI API 全链路
- 合约测试：微信/支付宝回调、AI Provider 适配器
- 压测：登录、AI 接口、限流、Webhook 峰值处理
- 验收标准：MVP 六大模块全部可用，关键路径可演示可收费

## 12. 风险与应对
- 支付回调乱序/重复：事件幂等 + 状态机保护
- AI 成本失控：套餐额度 + 单租户预算阈值 + 超额熔断
- 多租户越权：Repository 层 tenant_id 强制注入 + 安全测试
- 首版复杂度过高：坚持模块化单体，Admin/高级 Worker 延后到 v1.1

## 13. v1.1 / v2 演进建议
- v1.1：Admin 后台完善、Worker 独立部署、通知策略中心
- v2：团队协作、Feature Flag、审计日志增强、插件市场

---

该技术文档与 PRD 对齐，适用于直接进入研发拆解与排期评审。
