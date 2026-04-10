# Plans Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在管理端新增独立的套餐管理能力，完成 plans 套餐的分页查询、新增、编辑、状态启停与配额 JSON 校验，并同步前端接口文档。

**Architecture:** 后端新增 `modules.plan` 业务模块和 `AdminPlanController`，接口统一挂载到 `/api/admin/plans`；前端新增 `PlansApi + plans store + PlansManagementView`，并在管理端菜单新增 `/admin/plans` 入口。写操作统一 POST，查询统一 GET，保持与现有管理端代码模式一致。

**Tech Stack:** Spring Boot 3 + MyBatis-Plus + Sa-Token + MySQL + Vue 3 + Pinia + Ant Design Vue + TypeScript

---

## 0. 范围与约束

- 本计划仅覆盖“管理端 plans 套餐管理”单一子系统。
- 严格遵守项目约束：不修改部署脚本，不重构全项目结构。
- 未经许可不新增测试用例；本次仅执行后端 `mvn ... -DskipTests compile` 与前端 `npm run build` 验证。
- 所有新增/修改文件使用 UTF-8（无 BOM）。

## 1. 文件结构（实施前锁定）

### 1.1 后端

- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/common/constant/enums/PlanStatus.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/common/constant/enums/PlanBillingCycle.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/entity/PlanEntity.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/mapper/PlanMapper.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanPageRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanItemView.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanPageResponse.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanCreateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanUpdateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanStatusUpdateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/service/AdminPlanService.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/controller/AdminPlanController.java`

### 1.2 前端

- Create: `jay-boot-frontend/src/api/admin/PlansApi.ts`
- Create: `jay-boot-frontend/src/stores/admin/plans.ts`
- Create: `jay-boot-frontend/src/views/admin/PlansManagementView.vue`
- Modify: `jay-boot-frontend/src/router/routes/admin.ts`
- Modify: `jay-boot-frontend/src/layouts/admin/AppShell.vue`
- Modify: `jay-boot-frontend/src/api/admin/mockManager.ts`

### 1.3 文档

- Create: `jay-boot-frontend/docs/admin/PlansAPI.md`

---

### Task 1: 后端枚举与实体映射

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/common/constant/enums/PlanStatus.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/common/constant/enums/PlanBillingCycle.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/entity/PlanEntity.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/mapper/PlanMapper.java`

- [ ] **Step 1: 新建套餐状态枚举**

```java
public enum PlanStatus {
    ACTIVE,
    INACTIVE
}
```

- [ ] **Step 2: 新建计费周期枚举**

```java
public enum PlanBillingCycle {
    MONTHLY,
    YEARLY
}
```

- [ ] **Step 3: 新建 PlanEntity 与 PlanMapper**

```java
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

```java
@Mapper
public interface PlanMapper extends BaseMapper<PlanEntity> {
}
```

- [ ] **Step 4: 提交本任务**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/common/constant/enums/PlanStatus.java jay-boot-backend/src/main/java/com/jaycong/boot/common/constant/enums/PlanBillingCycle.java jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/entity/PlanEntity.java jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/mapper/PlanMapper.java
git commit -m "feat(plan): add plan enums and entity mapping"
```

---

### Task 2: 后端 DTO 契约定义

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanPageRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanItemView.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanPageResponse.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanCreateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanUpdateRequest.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/AdminPlanStatusUpdateRequest.java`

- [ ] **Step 1: 定义分页请求和分页响应 DTO**

```java
public record AdminPlanPageRequest(
        Integer page,
        Integer pageSize,
        String keyword,
        PlanStatus status,
        PlanBillingCycle billingCycle
) {
}
```

```java
public record AdminPlanPageResponse(
        List<AdminPlanItemView> records,
        long total,
        long page,
        long pageSize
) {
}
```

- [ ] **Step 2: 定义列表项 DTO**

```java
public record AdminPlanItemView(
        Long id,
        String code,
        String name,
        PlanBillingCycle billingCycle,
        Long price,
        PlanStatus status,
        String updatedTime
) {
}
```

- [ ] **Step 3: 定义创建/更新/状态更新 DTO**

```java
public record AdminPlanCreateRequest(
        @NotBlank(message = "套餐编码不能为空")
        @Size(max = 64, message = "套餐编码长度不能超过64")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "套餐编码仅支持大写字母、数字和下划线")
        String code,
        @NotBlank(message = "套餐名称不能为空")
        @Size(max = 64, message = "套餐名称长度不能超过64")
        String name,
        @NotNull(message = "计费周期不能为空")
        PlanBillingCycle billingCycle,
        @NotBlank(message = "配额JSON不能为空")
        String quotaJson,
        @NotNull(message = "价格不能为空")
        @Min(value = 0, message = "价格不能小于0")
        Long price,
        @NotNull(message = "状态不能为空")
        PlanStatus status
) {
}
```

```java
public record AdminPlanUpdateRequest(
        @NotBlank(message = "套餐名称不能为空")
        @Size(max = 64, message = "套餐名称长度不能超过64")
        String name,
        @NotNull(message = "计费周期不能为空")
        PlanBillingCycle billingCycle,
        @NotBlank(message = "配额JSON不能为空")
        String quotaJson,
        @NotNull(message = "价格不能为空")
        @Min(value = 0, message = "价格不能小于0")
        Long price,
        @NotNull(message = "状态不能为空")
        PlanStatus status
) {
}
```

```java
public record AdminPlanStatusUpdateRequest(
        @NotNull(message = "状态不能为空")
        PlanStatus status
) {
}
```

- [ ] **Step 4: 提交本任务**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/dto/*.java
git commit -m "feat(plan): add admin plan dto contracts"
```

---

### Task 3: 后端服务与控制器实现

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/service/AdminPlanService.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/controller/AdminPlanController.java`

- [ ] **Step 1: 实现 AdminPlanService 分页查询**

```java
public AdminPlanPageResponse page(AdminPlanPageRequest request) {
    ensureAdminOperator();
    long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
    long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();

    LambdaQueryWrapper<PlanEntity> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(request.keyword())) {
        String keyword = request.keyword().trim();
        wrapper.and(w -> w.like(PlanEntity::getCode, keyword).or().like(PlanEntity::getName, keyword));
    }
    if (request.status() != null) {
        wrapper.eq(PlanEntity::getStatus, request.status().name());
    }
    if (request.billingCycle() != null) {
        wrapper.eq(PlanEntity::getBillingCycle, request.billingCycle().name());
    }
    wrapper.orderByDesc(PlanEntity::getUpdatedTime);

    Page<PlanEntity> page = planMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<AdminPlanItemView> records = page.getRecords().stream().map(this::toItemView).toList();
    return new AdminPlanPageResponse(records, page.getTotal(), page.getCurrent(), page.getSize());
}
```

- [ ] **Step 2: 实现 create/update/status 业务逻辑与 JSON 校验**

```java
@Transactional
public void create(AdminPlanCreateRequest request) {
    ensureAdminOperator();
    validateQuotaJson(request.quotaJson());

    PlanEntity entity = new PlanEntity();
    entity.setCode(normalizeCode(request.code()));
    entity.setName(normalizeName(request.name()));
    entity.setBillingCycle(request.billingCycle().name());
    entity.setQuotaJson(request.quotaJson().trim());
    entity.setPrice(request.price());
    entity.setStatus(request.status().name());

    insertPlan(entity);
}
```

```java
@Transactional
public void update(Long id, AdminPlanUpdateRequest request) {
    ensureAdminOperator();
    validateQuotaJson(request.quotaJson());

    PlanEntity entity = requirePlan(id);
    entity.setName(normalizeName(request.name()));
    entity.setBillingCycle(request.billingCycle().name());
    entity.setQuotaJson(request.quotaJson().trim());
    entity.setPrice(request.price());
    entity.setStatus(request.status().name());
    updatePlan(entity);
}
```

```java
@Transactional
public void updateStatus(Long id, AdminPlanStatusUpdateRequest request) {
    ensureAdminOperator();
    PlanEntity entity = requirePlan(id);
    entity.setStatus(request.status().name());
    updatePlan(entity);
}
```

- [ ] **Step 3: 实现冲突与不存在处理**

```java
private void insertPlan(PlanEntity entity) {
    try {
        planMapper.insert(entity);
    } catch (DuplicateKeyException ex) {
        throw new BusinessException(ErrorCode.CONFLICT, "套餐编码已存在");
    }
}

private void updatePlan(PlanEntity entity) {
    try {
        planMapper.updateById(entity);
    } catch (DuplicateKeyException ex) {
        throw new BusinessException(ErrorCode.CONFLICT, "套餐编码已存在");
    }
}

private PlanEntity requirePlan(Long id) {
    PlanEntity entity = planMapper.selectById(id);
    if (entity == null) {
        throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
    }
    return entity;
}
```

- [ ] **Step 4: 新建 AdminPlanController 暴露接口**

```java
@RestController
@RequestMapping("/api/admin/plans")
@Validated
@Tag(name = "管理端套餐管理", description = "套餐分页、创建、编辑与状态启停")
public class AdminPlanController {

    private final AdminPlanService adminPlanService;

    public AdminPlanController(AdminPlanService adminPlanService) {
        this.adminPlanService = adminPlanService;
    }

    @GetMapping
    public ApiResponse<AdminPlanPageResponse> page(...) { ... }

    @PostMapping
    public ApiResponse<Void> create(...) { ... }

    @PostMapping("/{id}")
    public ApiResponse<Void> update(...) { ... }

    @PostMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(...) { ... }
}
```

- [ ] **Step 5: 编译验证（后端）**

Run:

```bash
mvn -Dmaven.repo.local=D:\develop\maven_repo -DskipTests compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: 提交本任务**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan jay-boot-backend/src/main/java/com/jaycong/boot/rest/admin/controller/AdminPlanController.java
git commit -m "feat(plan): implement admin plan management backend api"
```

---

### Task 4: 前端 API 与 Store 实现

**Files:**
- Create: `jay-boot-frontend/src/api/admin/PlansApi.ts`
- Create: `jay-boot-frontend/src/stores/admin/plans.ts`
- Modify: `jay-boot-frontend/src/api/admin/mockManager.ts`

- [ ] **Step 1: 新建 PlansApi.ts 契约与函数**

```ts
export type PlanStatus = 'ACTIVE' | 'INACTIVE'
export type PlanBillingCycle = 'MONTHLY' | 'YEARLY'

export interface AdminPlanItem {
  id: string
  code: string
  name: string
  billingCycle: PlanBillingCycle
  price: number
  status: PlanStatus
  updatedTime: string
}
```

```ts
export const getAdminPlanPageApi = (token: string, params: AdminPlanPageParams) =>
  requestAdminAuthRealApi<AdminPlanPageResponse>('GET', '/api/admin/plans', { token, payload: params })

export const createAdminPlanApi = (token: string, payload: AdminPlanCreatePayload) =>
  requestAdminAuthRealApi<null>('POST', '/api/admin/plans', { token, payload })

export const updateAdminPlanApi = (token: string, id: string, payload: AdminPlanUpdatePayload) =>
  requestAdminAuthRealApi<null>('POST', `/api/admin/plans/${id}`, { token, payload })

export const updateAdminPlanStatusApi = (token: string, id: string, status: PlanStatus) =>
  requestAdminAuthRealApi<null>('POST', `/api/admin/plans/${id}/status`, { token, payload: { status } })
```

- [ ] **Step 2: 新建 plans store 编排查询与写操作**

```ts
export const useAdminPlansStore = defineStore('admin-plans-management', {
  state: () => ({
    records: [] as AdminPlanItem[],
    total: 0,
    page: 1,
    pageSize: 10,
    keyword: '',
    billingCycle: '' as '' | PlanBillingCycle,
    status: '' as '' | PlanStatus,
    loadingInitial: false,
    refreshing: false,
    submitting: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  actions: {
    async fetchList(...) { ... },
    async createPlan(...) { ... },
    async updatePlan(...) { ... },
    async updatePlanStatus(...) { ... },
  },
})
```

- [ ] **Step 3: 在 mockManager 增加 plans mock 接口**

```ts
registerMockApi('GET', '/api/admin/plans', ...)
registerMockApi('POST', '/api/admin/plans', ...)
registerMockApi('POST', '/api/admin/plans/:id', ...)
registerMockApi('POST', '/api/admin/plans/:id/status', ...)
```

备注：保持与现有 mock 注册风格一致，不引入路由参数解析器，使用与现有模块一致的固定 URL 匹配策略（id 通过 payload 透传或在 API 层保持 mock 可达）。

- [ ] **Step 4: 提交本任务**

```bash
git add jay-boot-frontend/src/api/admin/PlansApi.ts jay-boot-frontend/src/stores/admin/plans.ts jay-boot-frontend/src/api/admin/mockManager.ts
git commit -m "feat(plan): add admin plans api client and store"
```

---

### Task 5: 前端页面、路由与菜单

**Files:**
- Create: `jay-boot-frontend/src/views/admin/PlansManagementView.vue`
- Modify: `jay-boot-frontend/src/router/routes/admin.ts`
- Modify: `jay-boot-frontend/src/layouts/admin/AppShell.vue`

- [ ] **Step 1: 新建 PlansManagementView 页面骨架**

```vue
<template>
  <section class="plans-page">
    <header class="plans-toolbar">...</header>
    <section class="plans-filters">...</section>
    <a-table ... />
    <a-modal ... />
  </section>
</template>
```

- [ ] **Step 2: 完成筛选、表格、弹窗与状态切换交互**

```ts
const onSubmit = async () => {
  await validateForm()
  if (!isValidJson(formState.quotaJson)) {
    message.error('配额 JSON 格式不合法')
    return
  }
  ...
}
```

```ts
const onToggleStatus = async (record: AdminPlanItem) => {
  const targetStatus: PlanStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  await plansStore.updatePlanStatus(record.id, targetStatus)
  await plansStore.fetchList('refresh')
}
```

- [ ] **Step 3: 增加 `/admin/plans` 路由与菜单项**

```ts
{
  path: 'plans',
  name: 'admin-plans',
  component: () => import('../../views/admin/PlansManagementView.vue'),
  meta: { title: '套餐管理', requiresAuth: true },
}
```

```ts
{ key: '/admin/plans', label: '套餐管理' },
```

- [ ] **Step 4: 前端构建验证**

Run:

```bash
npm run build
```

Expected: `vue-tsc -b` 和 `vite build` 均成功。

- [ ] **Step 5: 提交本任务**

```bash
git add jay-boot-frontend/src/views/admin/PlansManagementView.vue jay-boot-frontend/src/router/routes/admin.ts jay-boot-frontend/src/layouts/admin/AppShell.vue
git commit -m "feat(plan): add plans management page and navigation"
```

---

### Task 6: 前端接口文档

**Files:**
- Create: `jay-boot-frontend/docs/admin/PlansAPI.md`

- [ ] **Step 1: 编写接口文档**

```md
## 套餐分页查询
- URL: /api/admin/plans
- Method: GET
- Query: page,pageSize,keyword,status,billingCycle

## 新增套餐
- URL: /api/admin/plans
- Method: POST

## 编辑套餐
- URL: /api/admin/plans/{id}
- Method: POST

## 更新套餐状态
- URL: /api/admin/plans/{id}/status
- Method: POST
```

- [ ] **Step 2: 文档自检**

Run:

```bash
Select-String -Path jay-boot-frontend/docs/admin/PlansAPI.md -Pattern "GET|POST|/api/admin/plans"
```

Expected: 四个接口均存在，且请求方式只出现 GET/POST。

- [ ] **Step 3: 提交本任务**

```bash
git add jay-boot-frontend/docs/admin/PlansAPI.md
git commit -m "docs(plan): add frontend plans api documentation"
```

---

## 计划自检（已完成）

### 1) 规格覆盖检查

- 已覆盖：分页查询、新增、编辑、状态启停、配额 JSON 校验、前端独立页面、前端接口文档、构建校验。
- 未覆盖项：删除、订阅关联统计（符合已确认范围）。

### 2) 占位词检查

- 已检查无 `TODO/TBD/待补充` 等占位描述。

### 3) 类型一致性检查

- 状态统一为 `ACTIVE/INACTIVE`。
- 周期统一为 `MONTHLY/YEARLY`。
- 写接口统一 `POST`，查询接口统一 `GET`。