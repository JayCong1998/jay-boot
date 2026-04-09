# RBAC 模块（MVP）设计文档

## 1. 背景
- 项目：Java Micro-SaaS Starter
- 日期：2026-04-01
- 目标：完成 RBAC MVP，包含角色/权限模型、用户角色分配、接口级权限校验

## 2. 目标范围
- 实现 `roles`、`permissions`、`role_permissions`、`user_roles` 四张表能力
- 暴露 `/api/rbac/*` 相关接口
- 使用 Sa-Token 原生能力进行接口权限校验
- 所有角色关系数据按 `tenant_id` 进行隔离

## 3. 已确认决策
- 权限来源：内置权限清单（本期不开放权限管理 API）
- 包含用户分配角色 API
- 包含 RBAC 接口级权限校验
- 复用现有 `ApiResponse` 与 `BusinessException` 体系

## 4. 数据模型
- `roles(id, tenant_id, name, ...审计字段, is_deleted)`
- `permissions(id, code, description, ...审计字段, is_deleted)`
- `role_permissions(id, tenant_id, role_id, permission_id, ...审计字段, is_deleted)`
- `user_roles(id, tenant_id, user_id, role_id, ...审计字段, is_deleted)`

唯一约束：
- `roles`：`(tenant_id, name, is_deleted)`
- `permissions`：`(code, is_deleted)`
- `role_permissions`：`(tenant_id, role_id, permission_id, is_deleted)`
- `user_roles`：`(tenant_id, user_id, role_id, is_deleted)`

## 5. 内置权限与默认角色
内置权限编码：
- `rbac.role.read`
- `rbac.role.write`
- `rbac.role.grant`
- `rbac.user.grant`

默认角色：
- 每个租户初始化 `OWNER` 角色
- 用户注册并创建租户后自动绑定 `OWNER`
- `OWNER` 自动授予全部内置 RBAC 权限

## 6. API 约定
- `GET /api/rbac/roles`（需要 `rbac.role.read`）
- `POST /api/rbac/roles`（需要 `rbac.role.write`）
- `POST /api/rbac/roles/{id}/permissions`（需要 `rbac.role.grant`，覆盖式）
- `POST /api/rbac/users/{id}/roles`（需要 `rbac.user.grant`，覆盖式）
- `GET /api/rbac/permissions/check?code=...`（登录可用，返回当前用户是否拥有该权限）

## 7. 安全与租户隔离
- 角色授权、用户角色分配都绑定当前登录用户租户
- 跨租户角色/用户操作返回 404
- 通过 `StpInterface` 动态加载当前用户角色与权限

## 8. 错误码约定
- 未登录：401
- 无权限：403
- 资源不存在或跨租户：404
- 参数错误：400
- 资源冲突：409

## 9. 实施要点
- 新增 `modules/rbac`（controller/service/entity/mapper/dto/config）
- 补充 MySQL 与 H2 的 RBAC 表结构
- 在全局异常处理器中接入 Sa-Token 权限异常映射
- 在租户开通流程中增加 OWNER 角色初始化与授权
