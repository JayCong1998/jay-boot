# 数据库初始化种子设计（RBAC + 租户 + 管理员）

## 1. 背景与目标
- 日期：2026-04-02
- 目标：为当前项目提供一套可重复执行的数据库种子脚本，用于初始化：
  - RBAC 相关表
  - 租户相关表
  - 基础套餐与订阅数据
  - 管理员账号：`admin@jayboot.local / Admin1234`

本设计遵循最小改动原则，不改认证规则，不引入破坏性清理逻辑。

## 2. 范围与边界
### 2.1 范围内
- 新增 `database/seed.sql`
- 初始化以下表数据：
  - `tenants`
  - `users`
  - `user_tenant`
  - `roles`
  - `permissions`
  - `role_permissions`
  - `user_roles`
  - `plans`
  - `subscriptions`

### 2.2 范围外
- 不修改登录参数校验（仍要求邮箱格式、密码长度至少 8 位）
- 不执行清表/删数据
- 不新增测试用例

## 3. 数据初始化策略
采用“幂等增量”策略：
- 使用 `INSERT ... SELECT ... WHERE NOT EXISTS` 或 `INSERT IGNORE`
- 重复执行脚本不会造成重复脏数据
- 仅补齐缺失数据，不覆盖业务表中无关数据

## 4. 目标数据模型
### 4.1 管理员与租户
- 管理员账号：
  - email：`admin@jayboot.local`
  - password：明文目标 `Admin1234`，落库为 BCrypt 哈希
  - status：`ACTIVE`
- 系统租户：
  - name：`system-admin-tenant`
  - owner：管理员用户
  - plan：`FREE`

### 4.2 RBAC
- 角色：`OWNER`
- 权限（与 `RbacPermissionCatalog` 一致）：
  - `rbac.role.read`
  - `rbac.role.write`
  - `rbac.role.grant`
  - `rbac.user.grant`
  - `billing.plan.read`
  - `billing.subscription.create`
  - `billing.subscription.update`
- 授权关系：
  - `OWNER` 绑定全部内置权限
  - 管理员用户绑定 `OWNER`

### 4.3 Billing
- 套餐：
  - `FREE`
  - `PRO`
- 订阅：
  - 系统租户 1 条 `ACTIVE` 订阅

## 5. 固定主键规划
为保证幂等 SQL 简洁，使用固定 ID（仅用于种子数据）：
- tenant：`900000000000000001`
- user(admin)：`900000000000000101`
- role(OWNER)：`900000000000000201`
- permission：`900000000000000301` 起
- plan(FREE)：`900000000000000401`
- plan(PRO)：`900000000000000402`
- subscription：`900000000000000501`

## 6. 执行顺序
1. 执行 `database/schema.sql`
2. 执行 `database/seed.sql`

说明：脚本不依赖应用启动，可在数据库客户端直接执行。

## 7. 校验标准
### 7.1 SQL 校验
- `users` 中存在 `admin@jayboot.local` 且 `status=ACTIVE`
- `tenants` 中存在 `system-admin-tenant` 且 `owner_user_id=adminId`
- `user_tenant` 存在管理员与租户绑定，`role_in_tenant=OWNER`
- `roles/user_roles` 显示管理员持有 `OWNER`
- `permissions/role_permissions` 显示 7 项权限存在且已授予 `OWNER`
- `plans` 存在 `FREE/PRO`
- `subscriptions` 存在系统租户 `ACTIVE` 订阅

### 7.2 接口校验
- 登录接口：管理员可成功登录
- 权限接口：`/api/rbac/permissions/check?code=rbac.role.read` 返回有权限
- 套餐接口：`/api/billing/plans` 可正常返回

## 8. 回滚与修复策略
- 不提供“清空重建”回滚，避免误删业务数据
- 若初始化值有误，修正 `seed.sql` 后重复执行
- 对单条错误数据采用定点 `UPDATE`，不做批量清理

## 9. 风险与缓解
- 风险：明文密码误写入数据库  
  缓解：仅写入 BCrypt 哈希，禁止明文存储
- 风险：权限目录与代码不一致  
  缓解：权限清单与 `RbacPermissionCatalog` 保持一一对应
- 风险：多次执行产生重复数据  
  缓解：使用幂等插入策略

## 10. 交付文件
- 新增：`database/seed.sql`
- 不变：现有 schema 与业务代码逻辑
