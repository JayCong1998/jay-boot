# Sa-Token 登录上下文与审计字段联动设计

## 1. 背景与目标

当前后端认证与审计存在三个问题：

1. 登录后会话中缺少统一的“登录实体”，业务层只能到处通过 `StpUtil.getLoginIdAsLong()` + 查库拿用户信息，重复且分散。
2. 登录日志写入逻辑高度耦合在 `AuthService`，成功/失败分支代码重复，扩展事件（踢下线、顶下线、注销）不够优雅。
3. `MybatisPlusMetaObjectHandler` 审计字段固定填 `0/system`，无法自动关联当前登录用户。

本次目标：

1. 同时覆盖管理端与用户端登录流程，建立统一登录上下文模型。
2. 基于 Sa-Token 监听机制重构登录日志记录链路，成功态事件化，失败态显式记录。
3. 让 `MetaObjectHandler` 从登录上下文自动获取当前用户，填充 `creator/updater`。

约束口径：

1. `creatorName/updaterName` 统一使用 `username`。
2. 未登录场景保持现状：`creatorId/updaterId = 0`，`creatorName/updaterName = system`。
3. 不修改部署脚本，不重构整体项目结构。

## 2. 设计总览

采用“登录上下文标准化 + 事件化日志 + 审计自动填充”三层方案：

1. 认证成功时，统一构建 `LoginPrincipal` 并写入 Sa-Token `SaSession`。
2. 成功登录/登出等事件由 `SaTokenListener` 统一记录日志；失败登录在认证服务内记录。
3. `MybatisPlusMetaObjectHandler` 通过 `LoginContext` 读取当前登录人并自动填充审计字段。

## 3. 方案对比与选型

### 3.1 方案A：轻改（最小改动）

1. 在 `AuthService` 登录成功后手动写 `SaSession`。
2. 登录日志继续在 `AuthService` 手工插入。
3. `MetaObjectHandler` 直接调用 `StpUtil` 并临时查库取用户名。

优点：改动小。  
缺点：职责仍分散，不利于持续演进。

### 3.2 方案B：标准化（本次采用）

1. 新增统一登录实体 `LoginPrincipal` 与 `LoginContext` 工具类。
2. 登录成功流程统一写会话实体和扩展参数。
3. 成功态日志改为 Sa-Token 监听器记录；失败态保留业务内记录。
4. 审计自动填充统一读取 `LoginContext`。

优点：边界清晰、复用性高、覆盖管理端和用户端。  
缺点：新增少量基础设施类。

### 3.3 方案C：重构增强（暂不采用）

1. 引入异步事件总线，日志异步化。
2. 扩展风控策略、告警链路。

优点：扩展性最高。  
缺点：当前阶段过重，超出本次范围。

## 4. 详细设计

### 4.1 登录上下文模型

新增 `LoginPrincipal`，建议字段：

1. `Long userId`
2. `String username`
3. `String email`
4. `String role`
5. `String clientType`（`ADMIN` / `SITE`）
6. `String loginIp`
7. `String loginUa`
8. `LocalDateTime loginTime`

SaSession 约定：

1. 固定键：`LOGIN_PRINCIPAL`
2. 扩展键：`LOGIN_TYPE`（`LOGIN` / `REGISTER`）、`CLIENT_TYPE`、`LOGIN_IP`、`LOGIN_UA`

### 4.2 认证成功链路

在 `AuthService` 统一封装登录成功处理（示意）：

1. `StpUtil.login(userId)`
2. 构建 `LoginPrincipal`
3. `StpUtil.getSession().set("LOGIN_PRINCIPAL", principal)`
4. 写扩展参数（登录类型、端类型、IP、UA、登录时间）
5. 返回现有响应 DTO（兼容原接口）

管理端与用户端都复用同一套成功处理逻辑，仅 `clientType` 不同。

### 4.3 登录日志方案（基于 Sa-Token）

成功态：

1. 新增 Sa-Token 监听器实现类（如 `AuthSaTokenListener`）。
2. 监听登录成功、主动登出、被踢下线、被顶下线等事件。
3. 监听器内统一写 `login_logs`，`success = true`，`reason` 使用统一枚举值。

失败态：

1. 邮箱不存在、密码错误、账号禁用等失败不触发成功事件。
2. 失败日志继续在认证服务写入：`success = false`，`reason` 为失败码。
3. 失败写入逻辑统一到私有方法，避免重复代码。

字段来源优先级：

1. 优先从 `LoginPrincipal` / 会话扩展参数获取。
2. 失败场景使用 `AuthRequestContext`。
3. 取值失败则允许空值，不影响主认证流程。

### 4.4 审计字段自动填充

`MybatisPlusMetaObjectHandler` 改造为：

1. `insertFill`：
   - `creatorId = currentUserId or 0`
   - `creatorName = currentUsername or system`
   - `updaterId/updaterName` 同上
   - `createdTime/updatedTime/isDeleted` 保持现有逻辑
2. `updateFill`：
   - `updaterId = currentUserId or 0`
   - `updaterName = currentUsername or system`
   - `updatedTime = now`

`LoginContext` 行为：

1. 已登录且会话含 `LoginPrincipal`：直接返回。
2. 已登录但会话无实体：允许一次兜底查库并重建 principal（可选实现）。
3. 任意异常：返回空，最终走 `0/system` 回退。

### 4.5 兼容与迁移

1. 旧 token 可能没有 `LoginPrincipal`，通过 `LoginContext` 兜底，避免线上瞬时异常。
2. 业务层原有 `StpUtil.getLoginIdAsLong()` 可逐步替换，不要求一次性全量迁移。
3. 不变更现有 API 入参/出参，确保前端无感知。

## 5. 影响范围

预计涉及：

1. `modules.auth.service.AuthService`
2. 新增 `modules.auth.dto.LoginPrincipal`（或等价包路径）
3. 新增 `modules.auth.context.LoginContext`（或等价包路径）
4. 新增 Sa-Token 监听器类
5. `common.config.MybatisPlusMetaObjectHandler`
6. 少量业务服务中与“当前登录人”强耦合代码（按需最小调整）

## 6. 风险与控制

1. 会话对象类型不一致风险：`LoginContext` 做类型安全判断并回退。
2. 日志重复写入风险：明确成功态仅监听器写、失败态仅认证服务写。
3. 非登录请求写库风险：统一回退 `0/system`，保障稳定性。

## 7. 验证策略

按“先编译、后链路”执行，不新增测试用例：

1. 编译验证：后端执行 `mvn -DskipTests compile`。
2. 认证链路验证：注册、登录、登出、错误密码登录。
3. 数据验证：
   - `login_logs` 成功与失败记录都可写入。
   - 业务表 `creator/updater` 在登录态下可写入当前用户 ID 与 `username`。
   - 未登录写入时仍为 `0/system`。

## 8. 非目标

本次不包含：

1. 全量权限体系重构
2. 异步日志基础设施建设
3. 前端接口协议调整

## 9. 实施顺序（供后续计划使用）

1. 引入 `LoginPrincipal` 与 `LoginContext`
2. 重构 `AuthService` 登录成功流程并写会话扩展参数
3. 新增 Sa-Token 监听器并迁移成功态日志
4. 改造 `MybatisPlusMetaObjectHandler`
5. 编译验证与关键链路回归
