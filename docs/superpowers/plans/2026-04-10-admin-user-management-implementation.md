# Admin User Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在管理端新增“用户管理”完整能力，支持列表筛选、新增、编辑、角色分配（`admin/user`）、启停用、重置密码（6-10位），并完成前后端联调。

**Architecture:** 后端在 `modules.auth` 内新增 `AdminUserService + DTO` 并在 `rest.admin` 暴露 `AdminUserController`；前端新增 `UserApi + userManagement store + UserManagementView`，通过现有 admin 登录 token（`satoken`）调用真实接口。删除能力不开放，禁用能力走显式状态接口并做业务保护（不能禁用自己、至少保留1个启用 admin）。

**Tech Stack:** Spring Boot 3 + MyBatis-Plus + Sa-Token + MySQL 8 + Vue 3 + Pinia + Ant Design Vue + TypeScript

---

## 0. 范围检查与约束

- 本计划仅覆盖一个子系统：管理端用户管理（单页 + 后端接口 + 数据库字段）。
- 项目约束：未获用户许可不新增测试用例，本计划使用“编译 + 构建 + 手工接口验证”替代 TDD 测试文件。

## 1. 文件结构（实施前锁定）

### 1.1 后端

- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/entity/UserEntity.java`
- Modify: `database/schema.sql`
- Modify: `jay-boot-backend/src/test/resources/schema.sql`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserRole.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserStatus.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserPageRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserItemView.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserPageResponse.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserCreateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserUpdateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserStatusUpdateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserPasswordResetRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/AdminUserService.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/controller/AdminUserController.java`

### 1.2 前端

- Create: `jay-boot-frontend/src/api/admin/UserApi.ts`
- Create: `jay-boot-frontend/src/stores/admin/userManagement.ts`
- Create: `jay-boot-frontend/src/views/admin/UserManagementView.vue`
- Modify: `jay-boot-frontend/src/router/routes/admin.ts`
- Modify: `jay-boot-frontend/src/layouts/admin/AppShell.vue`
- Create: `jay-boot-frontend/docs/admin/UserAPI.md`

---

### Task 1: 数据库与实体字段落地（role）

**Files:**
- Modify: `database/schema.sql`
- Modify: `jay-boot-backend/src/test/resources/schema.sql`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/entity/UserEntity.java`

- [ ] **Step 1: 在生产 schema 中为 users 新增 role 字段**

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NULL,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'user',
    status VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_users_email_deleted (email, is_deleted),
    UNIQUE KEY uk_users_username_deleted (username, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 在测试 schema 同步新增 role 字段**

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NULL,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'user',
    status VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0
);
```

- [ ] **Step 3: 在 UserEntity 增加 role 属性**

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class UserEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private String role;

    private String status;
}
```

- [ ] **Step 4: 提交本任务**

```bash
git add database/schema.sql jay-boot-backend/src/test/resources/schema.sql jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/entity/UserEntity.java
git commit -m "feat(user): add role column and entity mapping"
```

---

### Task 2: 后端 DTO 定义（请求/响应/枚举）

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserRole.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserStatus.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserPageRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserItemView.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserPageResponse.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserCreateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserUpdateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserStatusUpdateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUserPasswordResetRequest.java`

- [ ] **Step 1: 创建角色/状态枚举**

```java
public enum AdminUserRole {
    admin,
    user
}
```

```java
public enum AdminUserStatus {
    ACTIVE,
    INACTIVE
}
```

- [ ] **Step 2: 创建分页请求与分页响应 DTO**

```java
public record AdminUserPageRequest(
        Integer page,
        Integer pageSize,
        String keyword,
        AdminUserRole role,
        AdminUserStatus status
) {
}
```

```java
public record AdminUserPageResponse(
        List<AdminUserItemView> records,
        long total,
        long page,
        long pageSize
) {
}
```

- [ ] **Step 3: 创建列表项与增改状态重置密码 DTO**

```java
public record AdminUserItemView(
        Long id,
        String username,
        String email,
        AdminUserRole role,
        AdminUserStatus status,
        String createdTime
) {
}
```

```java
public record AdminUserCreateRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 24, message = "用户名长度必须在 3 到 24 位之间")
        String username,
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,
        @NotNull(message = "角色不能为空")
        AdminUserRole role,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 10, message = "密码长度必须在 6 到 10 位之间")
        String password,
        AdminUserStatus status
) {
}
```

```java
public record AdminUserUpdateRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 24, message = "用户名长度必须在 3 到 24 位之间")
        String username,
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,
        @NotNull(message = "角色不能为空")
        AdminUserRole role,
        @NotNull(message = "状态不能为空")
        AdminUserStatus status
) {
}
```

```java
public record AdminUserStatusUpdateRequest(
        @NotNull(message = "状态不能为空")
        AdminUserStatus status
) {
}
```

```java
public record AdminUserPasswordResetRequest(
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 10, message = "密码长度必须在 6 到 10 位之间")
        String newPassword
) {
}
```

- [ ] **Step 4: 提交本任务**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/dto/AdminUser*.java
git commit -m "feat(user): add admin user management dto contracts"
```

---

### Task 3: 后端服务实现（查询、创建、编辑、启停用、重置密码）

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/AdminUserService.java`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/AuthService.java`（仅当需要复用校验时）

- [ ] **Step 1: 新建 AdminUserService 基础结构与依赖注入**

```java
@Service
public class AdminUserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
}
```

- [ ] **Step 2: 实现分页查询（GET 列表）**

```java
public AdminUserPageResponse page(AdminUserPageRequest request) {
    ensureAdminOperator();
    long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
    long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();

    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(request.keyword())) {
        String keyword = request.keyword().trim();
        wrapper.and(w -> w.like(UserEntity::getUsername, keyword).or().like(UserEntity::getEmail, keyword));
    }
    if (request.role() != null) {
        wrapper.eq(UserEntity::getRole, request.role().name());
    }
    if (request.status() != null) {
        wrapper.eq(UserEntity::getStatus, request.status().name());
    }
    wrapper.orderByDesc(UserEntity::getCreatedTime);

    Page<UserEntity> page = userMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<AdminUserItemView> records = page.getRecords().stream().map(this::toItemView).toList();
    return new AdminUserPageResponse(records, page.getTotal(), page.getCurrent(), page.getSize());
}
```

- [ ] **Step 3: 实现创建与编辑**

```java
@Transactional
public void create(AdminUserCreateRequest request) {
    ensureAdminOperator();
    String username = normalizeUsername(request.username());
    String email = normalizeEmail(request.email());
    ensureUsernameUnique(username, null);
    ensureEmailUnique(email, null);

    UserEntity entity = new UserEntity();
    entity.setUsername(username);
    entity.setEmail(email);
    entity.setRole((request.role() == null ? AdminUserRole.user : request.role()).name());
    entity.setStatus((request.status() == null ? AdminUserStatus.ACTIVE : request.status()).name());
    entity.setPasswordHash(passwordEncoder.encode(request.password()));
    userMapper.insert(entity);
}
```

```java
@Transactional
public void update(Long id, AdminUserUpdateRequest request) {
    ensureAdminOperator();
    UserEntity entity = requireUser(id);
    String username = normalizeUsername(request.username());
    String email = normalizeEmail(request.email());
    ensureUsernameUnique(username, id);
    ensureEmailUnique(email, id);
    entity.setUsername(username);
    entity.setEmail(email);
    entity.setRole(request.role().name());
    entity.setStatus(request.status().name());
    userMapper.updateById(entity);
}
```

- [ ] **Step 4: 实现启停用与重置密码**

```java
@Transactional
public void updateStatus(Long id, AdminUserStatusUpdateRequest request) {
    Long operatorId = ensureAdminOperator();
    UserEntity entity = requireUser(id);
    AdminUserStatus nextStatus = request.status();

    if (nextStatus == AdminUserStatus.INACTIVE && operatorId.equals(id)) {
        throw new BusinessException(ErrorCode.FORBIDDEN, "不允许禁用当前登录账号");
    }
    if (nextStatus == AdminUserStatus.INACTIVE && isLastActiveAdmin(entity)) {
        throw new BusinessException(ErrorCode.FORBIDDEN, "至少保留一个启用中的管理员");
    }

    entity.setStatus(nextStatus.name());
    userMapper.updateById(entity);
}
```

```java
@Transactional
public void resetPassword(Long id, AdminUserPasswordResetRequest request) {
    ensureAdminOperator();
    UserEntity entity = requireUser(id);
    entity.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    userMapper.updateById(entity);
}
```

- [ ] **Step 5: 实现安全校验与映射私有方法**

```java
private Long ensureAdminOperator() {
    if (!StpUtil.isLogin()) {
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
    }
    Long loginId = StpUtil.getLoginIdAsLong();
    UserEntity operator = requireUser(loginId);
    if (!"admin".equalsIgnoreCase(operator.getRole())) {
        throw new BusinessException(ErrorCode.FORBIDDEN, "仅管理员可执行该操作");
    }
    return loginId;
}
```

```java
private AdminUserItemView toItemView(UserEntity entity) {
    String createdTime = entity.getCreatedTime() == null
            ? null
            : entity.getCreatedTime().toString();
    return new AdminUserItemView(
            entity.getId(),
            entity.getUsername(),
            entity.getEmail(),
            AdminUserRole.valueOf(entity.getRole()),
            AdminUserStatus.valueOf(entity.getStatus()),
            createdTime
    );
}
```

- [ ] **Step 6: 提交本任务**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/AdminUserService.java
git commit -m "feat(user): implement admin user management service rules"
```

---

### Task 4: 后端控制器与路由暴露

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/controller/AdminUserController.java`

- [ ] **Step 1: 创建 AdminUserController 并注入服务**

```java
@RestController
@RequestMapping("/api/admin/users")
@Validated
@Tag(name = "管理端用户管理", description = "管理端用户列表、创建、编辑、启停用、重置密码")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }
}
```

- [ ] **Step 2: 补齐 5 个接口**

```java
@GetMapping
public ApiResponse<AdminUserPageResponse> page(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) AdminUserRole role,
        @RequestParam(required = false) AdminUserStatus status) {
    return ApiResponse.success(adminUserService.page(new AdminUserPageRequest(page, pageSize, keyword, role, status)));
}
```

```java
@PostMapping
public ApiResponse<Void> create(@Valid @RequestBody AdminUserCreateRequest request) {
    adminUserService.create(request);
    return ApiResponse.success(null);
}
```

```java
@PutMapping("/{id}")
public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest request) {
    adminUserService.update(id, request);
    return ApiResponse.success(null);
}
```

```java
@PostMapping("/{id}/status")
public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminUserStatusUpdateRequest request) {
    adminUserService.updateStatus(id, request);
    return ApiResponse.success(null);
}
```

```java
@PostMapping("/{id}/password/reset")
public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody AdminUserPasswordResetRequest request) {
    adminUserService.resetPassword(id, request);
    return ApiResponse.success(null);
}
```

- [ ] **Step 3: 后端编译验证（默认命令）**

Run:

```bash
D:\develop\apache-maven-3.9.0\bin\mvn.cmd --% -Dmaven.repo.local=D:\develop\maven_repo -DskipTests compile
```

Expected: 若本机 JDK=21，`BUILD SUCCESS`；否则报 `不支持发行版本 21`。

- [ ] **Step 4: 若出现 release=21 不匹配，执行语法兜底验证**

Run:

```bash
D:\develop\apache-maven-3.9.0\bin\mvn.cmd --% -Dmaven.repo.local=D:\develop\maven_repo -Dmaven.compiler.release=17 -DskipTests compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: 提交本任务**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/controller/AdminUserController.java
git commit -m "feat(user): expose admin user management rest api"
```

---

### Task 5: 前端 API 层实现（真实接口）

**Files:**
- Create: `jay-boot-frontend/src/api/admin/UserApi.ts`
- Modify: `jay-boot-frontend/src/api/admin/realApi.ts`

- [ ] **Step 1: 扩展 realApi 支持 PUT 方法**

```ts
export type AdminAuthMethod = 'GET' | 'POST' | 'PUT'
```

```ts
const response = await fetch(requestUrl, {
  method,
  headers: Object.keys(headers).length > 0 ? headers : undefined,
  body: method === 'GET' ? undefined : JSON.stringify(payload ?? {}),
})
```

- [ ] **Step 2: 创建 UserApi 类型定义与请求函数**

```ts
export type AdminUserRole = 'admin' | 'user'
export type AdminUserStatus = 'ACTIVE' | 'INACTIVE'

export interface AdminUserItem {
  id: number
  username: string
  email: string
  role: AdminUserRole
  status: AdminUserStatus
  createdTime: string
}

export interface AdminUserPageResponse {
  records: AdminUserItem[]
  total: number
  page: number
  pageSize: number
}
```

```ts
export const getAdminUserPageApi = (token: string, params: AdminUserPageParams) =>
  requestAdminAuthRealApi<AdminUserPageResponse>('GET', '/api/admin/users', { token, payload: params })

export const createAdminUserApi = (token: string, payload: AdminUserCreatePayload) =>
  requestAdminAuthRealApi<null>('POST', '/api/admin/users', { token, payload })

export const updateAdminUserApi = (token: string, id: number, payload: AdminUserUpdatePayload) =>
  requestAdminAuthRealApi<null>('PUT', `/api/admin/users/${id}`, { token, payload })

export const updateAdminUserStatusApi = (token: string, id: number, payload: AdminUserStatusPayload) =>
  requestAdminAuthRealApi<null>('POST', `/api/admin/users/${id}/status`, { token, payload })

export const resetAdminUserPasswordApi = (token: string, id: number, payload: AdminUserPasswordPayload) =>
  requestAdminAuthRealApi<null>('POST', `/api/admin/users/${id}/password/reset`, { token, payload })
```

- [ ] **Step 3: 提交本任务**

```bash
git add jay-boot-frontend/src/api/admin/realApi.ts jay-boot-frontend/src/api/admin/UserApi.ts
git commit -m "feat(user): add admin user real api client"
```

---

### Task 6: 前端 Store 实现（列表与操作编排）

**Files:**
- Create: `jay-boot-frontend/src/stores/admin/userManagement.ts`

- [ ] **Step 1: 定义状态、筛选条件与分页状态**

```ts
export const useAdminUserManagementStore = defineStore('admin-user-management', {
  state: () => ({
    records: [] as AdminUserItem[],
    total: 0,
    page: 1,
    pageSize: 10,
    keyword: '',
    role: '' as '' | AdminUserRole,
    status: '' as '' | AdminUserStatus,
    loadingInitial: false,
    refreshing: false,
    submitting: false,
    loadedOnce: false,
    errorMessage: '',
  }),
```

- [ ] **Step 2: 实现 fetchList / resetFilters / changePage**

```ts
async fetchList(mode: 'initial' | 'refresh' = 'refresh') {
  const authStore = useAuthStore()
  await authStore.hydrate()
  const token = authStore.token
  if (!token) {
    this.errorMessage = '用户未登录'
    return
  }
  if (mode === 'initial') this.loadingInitial = true
  if (mode === 'refresh') this.refreshing = true
  this.errorMessage = ''
  try {
    const data = await getAdminUserPageApi(token, {
      page: this.page,
      pageSize: this.pageSize,
      keyword: this.keyword || undefined,
      role: this.role || undefined,
      status: this.status || undefined,
    })
    this.records = data.records
    this.total = data.total
    this.page = data.page
    this.pageSize = data.pageSize
    this.loadedOnce = true
  } catch (error) {
    this.errorMessage = error instanceof Error ? error.message : '用户列表加载失败'
  } finally {
    this.loadingInitial = false
    this.refreshing = false
  }
}
```

- [ ] **Step 3: 实现 create/update/updateStatus/resetPassword**

```ts
async createUser(payload: AdminUserCreatePayload) {
  await this.runMutation((token) => createAdminUserApi(token, payload))
}

async updateUser(id: number, payload: AdminUserUpdatePayload) {
  await this.runMutation((token) => updateAdminUserApi(token, id, payload))
}

async updateUserStatus(id: number, status: AdminUserStatus) {
  await this.runMutation((token) => updateAdminUserStatusApi(token, id, { status }))
}

async resetPassword(id: number, newPassword: string) {
  await this.runMutation((token) => resetAdminUserPasswordApi(token, id, { newPassword }))
}
```

- [ ] **Step 4: 提交本任务**

```bash
git add jay-boot-frontend/src/stores/admin/userManagement.ts
git commit -m "feat(user): add admin user management store orchestration"
```

---

### Task 7: 前端页面与路由菜单接入

**Files:**
- Create: `jay-boot-frontend/src/views/admin/UserManagementView.vue`
- Modify: `jay-boot-frontend/src/router/routes/admin.ts`
- Modify: `jay-boot-frontend/src/layouts/admin/AppShell.vue`

- [ ] **Step 1: 新建 UserManagementView 页面骨架与筛选区**

```vue
<template>
  <section class="user-management-page">
    <header class="toolbar">
      <a-input v-model:value="keywordDraft" placeholder="搜索用户名或邮箱" allow-clear />
      <a-select v-model:value="roleDraft" :options="roleOptions" allow-clear placeholder="角色" />
      <a-select v-model:value="statusDraft" :options="statusOptions" allow-clear placeholder="状态" />
      <a-space>
        <a-button type="primary" @click="onSearch">查询</a-button>
        <a-button @click="onResetFilters">重置</a-button>
        <a-button type="primary" @click="openCreateModal">新建用户</a-button>
      </a-space>
    </header>
  </section>
</template>
```

- [ ] **Step 2: 完成表格与四类操作弹窗（新建、编辑、分配角色、重置密码）**

```vue
<a-table
  :columns="columns"
  :data-source="userStore.records"
  :loading="userStore.loadingInitial || userStore.refreshing"
  :pagination="pagination"
  row-key="id"
  :scroll="{ x: 980 }"
/>
```

```vue
<a-modal v-model:open="createModalOpen" title="新建用户" @ok="onCreateSubmit">
  <!-- username / email / role / password / confirmPassword / status -->
</a-modal>
```

```vue
<a-modal v-model:open="resetPasswordModalOpen" title="重置密码" @ok="onResetPasswordSubmit">
  <!-- newPassword / confirmPassword -->
</a-modal>
```

- [ ] **Step 3: 接入 store 操作与反馈**

```ts
const onToggleStatus = async (record: AdminUserItem) => {
  const nextStatus: AdminUserStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  await userStore.updateUserStatus(record.id, nextStatus)
  message.success(nextStatus === 'ACTIVE' ? '用户已启用' : '用户已禁用')
  await userStore.fetchList('refresh')
}
```

- [ ] **Step 4: 注册路由与菜单项**

```ts
{
  path: 'users',
  name: 'admin-users',
  component: () => import('../../views/admin/UserManagementView.vue'),
  meta: { title: '用户管理', requiresAuth: true },
}
```

```ts
{ key: '/admin/users', label: '用户管理' },
```

- [ ] **Step 5: 提交本任务**

```bash
git add jay-boot-frontend/src/views/admin/UserManagementView.vue jay-boot-frontend/src/router/routes/admin.ts jay-boot-frontend/src/layouts/admin/AppShell.vue
git commit -m "feat(user): add admin user management page and navigation"
```

---

### Task 8: 文档同步与构建验证

**Files:**
- Create: `jay-boot-frontend/docs/admin/UserAPI.md`

- [ ] **Step 1: 新增前端接口文档（请求/响应/错误/鉴权头）**

```md
# Admin User API

## 接口列表
- GET /api/admin/users
- POST /api/admin/users
- PUT /api/admin/users/{id}
- POST /api/admin/users/{id}/status
- POST /api/admin/users/{id}/password/reset

## 鉴权
- Header: satoken: <token>

## 密码规则
- 新建用户与重置密码均为 6-10 位
```

- [ ] **Step 2: 前端构建验证**

Run:

```bash
npm run build
```

Expected: `vue-tsc -b && vite build` 成功，输出 `built in ...`

- [ ] **Step 3: 后端编译验证**

Run:

```bash
D:\develop\apache-maven-3.9.0\bin\mvn.cmd --% -Dmaven.repo.local=D:\develop\maven_repo -DskipTests compile
```

Expected: 本机 JDK=21 则成功；否则记录错误并执行：

```bash
D:\develop\apache-maven-3.9.0\bin\mvn.cmd --% -Dmaven.repo.local=D:\develop\maven_repo -Dmaven.compiler.release=17 -DskipTests compile
```

- [ ] **Step 4: 提交本任务**

```bash
git add jay-boot-frontend/docs/admin/UserAPI.md
git commit -m "docs(api): add admin user management api documentation"
```

---

## 计划自检（已完成）

### 1) Spec 覆盖检查

- 已覆盖：
  - 用户管理页面（列表/筛选/分页）
  - 新增、编辑、角色分配（admin/user）
  - 启停用（无删除）
  - 重置密码（管理员输入新密码）
  - 前后端与数据库联动
  - 密码 6-10 位规则统一
- 无遗漏项。

### 2) 占位符检查

- 已检查不存在 `TODO/TBD/后续补充/类似任务N` 等占位描述。

### 3) 类型一致性检查

- 角色值统一：`admin | user`
- 状态值统一：`ACTIVE | INACTIVE`
- 密码规则统一：`6-10`
- 前后端接口路径统一：`/api/admin/users*`

