# RBAC租户管理员种子数据 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增可重复执行的数据库种子脚本，初始化租户、RBAC、套餐订阅与管理员账号数据。  
**Architecture:** 采用幂等增量SQL方案，使用固定ID与唯一键防重，避免清表或覆盖业务数据；通过SQL落库后再执行查询校验。  
**Tech Stack:** MySQL 8.0、SQL脚本（`schema.sql` + `seed.sql`）

---

## 文件结构与职责
- Create: `database/seed.sql`
- Modify: 无
- Verification: 本次按项目约束不新增测试用例，仅执行SQL与查询校验

### Task 1: 编写幂等种子脚本

**Files:**
- Create: `database/seed.sql`

- [ ] **Step 1: 定义固定主键和审计字段初始化值**
- [ ] **Step 2: 插入/补齐管理员租户与管理员账号**
- [ ] **Step 3: 插入/补齐OWNER角色、内置权限及角色权限关系**
- [ ] **Step 4: 插入/补齐管理员用户角色、用户租户关系**
- [ ] **Step 5: 插入/补齐FREE/PRO套餐与系统租户订阅**

### Task 2: 执行数据库初始化

**Files:**
- Execute: `database/schema.sql`
- Execute: `database/seed.sql`

- [ ] **Step 1: 连接本地MySQL并执行schema脚本**
- [ ] **Step 2: 连接本地MySQL并执行seed脚本**

### Task 3: 数据校验

**Files:**
- Query target tables: `users`, `tenants`, `user_tenant`, `roles`, `permissions`, `role_permissions`, `user_roles`, `plans`, `subscriptions`

- [ ] **Step 1: 校验管理员账号状态、租户绑定与OWNER角色绑定**
- [ ] **Step 2: 校验7个内置权限及OWNER权限映射**
- [ ] **Step 3: 校验FREE/PRO套餐及系统租户订阅数据**
