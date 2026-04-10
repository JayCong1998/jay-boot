# 管理端 Plans 套餐管理设计文档

## 1. 背景与目标

本设计用于在管理端新增独立的套餐管理能力，覆盖套餐分页查询、新增、编辑、状态启停与配额 JSON 校验。

目标：
- 提供 `/admin/plans` 独立页面，不复用现有 Billing 页面。
- 提供 `/api/admin/plans` 独立后端接口，不与订阅、对账逻辑混用。
- 仅使用 GET/POST 请求方式。
- 保证套餐编码在未删除数据范围内唯一。

非目标：
- 不实现套餐删除接口。
- 不实现订阅关联数量统计与禁删逻辑。

## 2. 方案对比与结论

### 2.1 备选方案

1. 方案A（采用）：新增独立 plans 模块与页面。
2. 方案B：挂在 billing 模块下但页面独立。
3. 方案C：前端 mock 优先，后端最小实现。

### 2.2 采用方案

采用方案A，原因：
- 领域边界清晰，后续可维护性更好。
- 不污染现有 Billing 语义。
- 可直接承载后续套餐扩展能力。

## 3. 后端设计

### 3.1 模块与包结构

新增模块 `modules.plan`：
- `entity/PlanEntity.java`
- `mapper/PlanMapper.java`
- `dto/*`（分页请求/响应、创建请求、更新请求、状态更新请求、列表项）
- `service/AdminPlanService.java`

新增控制器：
- `rest/admin/controller/AdminPlanController.java`

### 3.2 数据模型映射

数据库表：`plans`

关键字段映射：
- `id: Long`
- `code: String`
- `name: String`
- `billingCycle: String`
- `quotaJson: String`
- `price: Long`
- `status: String`
- `isDeleted: Integer`（逻辑删除）
- 审计字段沿用 `BaseEntity`

### 3.3 接口定义

1. `GET /api/admin/plans`
- 入参：`page,pageSize,keyword,status,billingCycle`
- 功能：分页查询套餐
- 返回：`records,total,page,pageSize`

2. `POST /api/admin/plans`
- 入参：`code,name,billingCycle,quotaJson,price,status`
- 功能：创建套餐

3. `POST /api/admin/plans/{id}`
- 入参：`name,billingCycle,quotaJson,price,status`
- 功能：编辑套餐

4. `POST /api/admin/plans/{id}/status`
- 入参：`status`
- 功能：状态启停

### 3.4 业务规则

- `code`：1~64，格式 `^[A-Z0-9_]+$`。
- `name`：1~64。
- `billingCycle`：本期支持 `MONTHLY`、`YEARLY`。
- `quotaJson`：必须是合法 JSON（服务端使用 Jackson 校验）。
- `price`：`>= 0`，单位为分。
- `status`：`ACTIVE`、`INACTIVE`。
- `code` 唯一性依赖 `uk_plans_code_deleted(code, is_deleted)`，冲突转业务异常 409。

### 3.5 异常处理

- 参数非法：400。
- 未登录：401。
- 无权限：403。
- 套餐不存在：404。
- 编码冲突：409。
- 其他异常：500。

说明：返回结构统一 `ApiResponse<T>`。

## 4. 前端设计

### 4.1 文件与职责

新增文件：
- `src/api/admin/PlansApi.ts`
- `src/stores/admin/plans.ts`
- `src/views/admin/PlansManagementView.vue`

修改文件：
- `src/router/routes/admin.ts`（新增 `/admin/plans` 路由）
- `src/layouts/admin/AppShell.vue`（新增菜单入口）
- `src/api/admin/mockManager.ts`（新增 plans mock 接口）

### 4.2 页面功能

- 顶部：标题、描述、刷新、新建套餐。
- 筛选：关键词、计费周期、状态，支持查询与重置。
- 列表：编码、名称、周期、价格、状态、更新时间、操作。
- 操作：编辑、启用/停用。
- 表单：新建/编辑共用弹窗，编辑时 `code` 只读。
- `quotaJson`：前端提交前校验 JSON 合法性。

### 4.3 视觉与交互规范

- 采用蓝青中性色风格，避免紫色主导。
- 保持管理端统一信息密度与组件样式。
- 表格移动端支持横向滚动。
- 状态展示使用“颜色 + 文本”双标识。
- 表单提交需要 loading 和成功/失败反馈。

## 5. 数据流

1. 页面初始化调用列表接口。
2. 筛选变更后重置到第一页并重新查询。
3. 新建/编辑成功后刷新列表。
4. 状态切换成功后刷新列表。
5. 出错时保留筛选条件并提示错误信息。

## 6. 安全与权限

- 默认复用当前管理端鉴权逻辑（Sa-Token）。
- 服务端执行管理员身份校验后才允许写操作。
- 所有写操作走 POST。

## 7. 验收标准

1. 能在 `/admin/plans` 进入套餐管理页面。
2. 分页查询、筛选、重置可用。
3. 新增套餐可用，编码冲突正确提示。
4. 编辑套餐可用，`quotaJson` 非法可拦截。
5. 启停状态可用并即时刷新。
6. 前端接口文档完整并与实际接口一致。
7. 后端改动后在 `jay-boot-backend` 目录执行 `mvn -Dmaven.repo.local=D:\develop\maven_repo -DskipTests compile` 通过。

## 8. 实施约束

- 全部文件使用 UTF-8（无 BOM）。
- 不修改部署脚本。
- 不重构整个项目结构。
- 未经用户许可不新增测试用例，本次仅做编译校验。