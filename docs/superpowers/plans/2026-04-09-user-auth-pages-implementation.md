# 用户端认证页面实现计划

> **给执行代理：** 必须按任务清单逐项执行并打勾。默认在当前会话内执行（Inline Execution），不创建测试用例。

**目标：** 完成用户端登录页与注册页的可用实现，新增独立用户认证 API 与 Store，并支持 Mock/真实接口切换。  
**架构：** 在 `src/api/user` 扩展 `AuthApi` 与 mock 路由，在 `src/stores/user` 新建认证状态管理；页面只负责表单与交互，认证逻辑下沉到 store。  
**技术栈：** Vue3 + Composition API + TypeScript + Pinia + Ant Design Vue + Vite

---

### 任务 1：补齐用户端认证 API

**文件：**
- 新建：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/src/api/user/AuthApi.ts`

- [x] 步骤 1：定义用户认证类型与请求入参/出参
- [x] 步骤 2：实现 `register/login/me/logout` 四个接口函数，全部补齐 JSDoc
- [x] 步骤 3：确保仅使用 GET/POST

---

### 任务 2：扩展 user mockManager 认证接口

**文件：**
- 修改：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/src/api/user/mockManager.ts`

- [x] 步骤 1：新增认证相关类型（Mock 用户记录、公开用户信息、会话结构）
- [x] 步骤 2：新增 localStorage 读写工具（用户列表、ID 种子）
- [x] 步骤 3：实现认证辅助方法（默认用户、签发 token、token 解析）
- [x] 步骤 4：注册 `/api/user/auth/register`、`/api/user/auth/login`、`/api/user/auth/me`、`/api/user/auth/logout`
- [x] 步骤 5：在 `initializeMockApis` 中挂载认证初始化

---

### 任务 3：新增用户端认证 Store

**文件：**
- 新建：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/src/stores/user/auth.ts`

- [x] 步骤 1：定义 `token/user/hydrated` 状态和 `isLoggedIn/displayName` 派生属性
- [x] 步骤 2：实现本地会话读写与清理（独立 key：`jay_boot_user_auth_*`）
- [x] 步骤 3：实现 `hydrate/login/register/logout/setSession/clearSession` 方法
- [x] 步骤 4：确保异常场景下可安全回收本地会话

---

### 任务 4：实现 AuthLoginView 页面

**文件：**
- 修改：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/src/views/user/AuthLoginView.vue`

- [x] 步骤 1：替换占位骨架为真实登录页面布局（左价值区 + 右表单卡片）
- [x] 步骤 2：接入 `useUserAuthStore`，提交时调用 `login`
- [x] 步骤 3：补齐字段校验、加载态、错误提示、成功提示
- [x] 步骤 4：登录成功后跳转 `/user/workspace`
- [x] 步骤 5：补充响应式与 reduced-motion 样式

---

### 任务 5：实现 AuthRegisterView 页面

**文件：**
- 修改：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/src/views/user/AuthRegisterView.vue`

- [x] 步骤 1：替换占位骨架为真实注册页面布局（与登录页视觉统一）
- [x] 步骤 2：接入 `useUserAuthStore`，提交时调用 `register`
- [x] 步骤 3：补齐邮箱/密码/确认密码校验
- [x] 步骤 4：注册成功后跳转 `/user/onboarding`
- [x] 步骤 5：补充响应式与 reduced-motion 样式

---

### 任务 6：补齐用户端认证接口文档

**文件：**
- 新建：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/docs/user/AuthAPI.md`

- [x] 步骤 1：按规范写四个接口说明（功能、URL、方法、参数、响应）
- [x] 步骤 2：提供请求/响应 JSON 示例与错误约定
- [x] 步骤 3：写明 Mock/真实接口切换配置

---

### 任务 7：构建验证

**文件：**
- 无代码文件变更

- [x] 步骤 1：执行 `npm run build`（`jay-boot-frontend` 目录）
- [x] 步骤 2：若失败，记录失败命令、首个关键报错、涉及文件路径并修复
- [x] 步骤 3：输出最终变更清单与验证结果

---

## 计划自检

- 规格覆盖：已覆盖页面实现、独立认证链路、接口文档、Mock/真实切换。  
- 占位检查：无 TODO/TBD/“后续补”。  
- 一致性检查：跳转策略与已确认需求一致（登录 `/user/workspace`、注册 `/user/onboarding`，本轮不做用户路由鉴权）。