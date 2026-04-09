# jay-boot-ui 前端工程化实现设计（Vue3 + TS + Vite）

## 1. 目标

基于现有 `ui` 高保真原型、`api-docs.md` 和已完成后端接口，建设可运行的正式前端工程 `jay-boot-ui`，默认直连真实后端，同时保留 Mock 开关。

## 2. 技术栈与约束

- Vue 3 + Composition API
- TypeScript
- Vite
- Pinia
- Ant Design Vue
- 请求方式仅使用 GET / POST
- API 模块命名：`模块名称 + Api.ts`
- 保留 `mockManager.ts`，支持开关切换

## 3. 接口能力映射

当前后端已完成接口：

- Auth：`/api/auth/login`、`/api/auth/register`、`/api/auth/logout`、`/api/auth/session`、`/api/auth/token/refresh`
- Tenant：`/api/tenants/current`、`/api/tenants/current/update`
- RBAC：`/api/rbac/roles`、`/api/rbac/roles/{id}/permissions`、`/api/rbac/users/{id}/roles`、`/api/rbac/permissions/check`
- Billing：`/api/billing/plans`、`/api/billing/subscriptions`、`/api/billing/subscriptions/{id}/update`
- System：`/api/system/ping`

前端将真实接入上述接口；API Key / 用量 / AI 网关页面按“结构完整 + 待后端开放”方式实现，并支持 Mock 显示示例数据。

## 4. 工程结构

```text
jay-boot-ui/
├── src/
│   ├── api/
│   │   ├── authApi.ts
│   │   ├── tenantApi.ts
│   │   ├── rbacApi.ts
│   │   ├── billingApi.ts
│   │   ├── mockManager.ts
│   │   └── index.ts
│   ├── layouts/
│   ├── views/
│   ├── stores/
│   ├── router/
│   └── styles/
└── ...
```

## 5. 核心设计

### 5.1 API 层

- 统一请求封装：自动注入 `satoken` 请求头
- 统一响应解析：按 `{ code, message, data }` 处理
- 开关：`VITE_USE_MOCK=false` 默认直连后端；`true` 时优先走 `mockManager.ts`

### 5.2 鉴权与路由

- `auth` store 统一管理 token + 用户会话
- 受保护路由需登录后访问
- 401 自动清理会话并跳转登录页

### 5.3 页面与布局

- 主布局：左侧导航 + 顶栏 + 内容区
- 页面：登录、注册、工作台、租户、RBAC、计费、API Key、用量、AI 网关
- 视觉方向沿用原型风格并统一样式变量

## 6. 验收标准

1. 项目可完成依赖安装与构建
2. 登录后可访问受保护页面
3. Tenant / RBAC / Billing 页面可调用真实后端接口
4. API Key / 用量 / AI 页面有完整交互结构并可切 Mock 演示
5. 无 TODO、无伪代码

