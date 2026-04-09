# 2026-04-09 用户端认证页面与链路设计

## 1. 背景与目标

当前 `AuthLoginView.vue`、`AuthRegisterView.vue` 仍为占位骨架，用户端尚未形成独立认证链路。  
本次目标是在不影响 admin 认证体系的前提下，完成用户端登录/注册页面、用户侧 API、用户侧状态管理，并支持通过配置在 Mock 与真实接口之间切换。

## 2. 范围定义

### 2.1 本次范围

- 实现 `src/views/user/AuthLoginView.vue`
- 实现 `src/views/user/AuthRegisterView.vue`
- 新增 `src/api/user/AuthApi.ts`
- 扩展 `src/api/user/mockManager.ts`，新增 `/api/user/auth/*` Mock 接口
- 新增 `src/stores/user/auth.ts`
- 新增 `docs/user/AuthAPI.md`
- 保持用户端 API 模式切换机制：`VITE_USER_API_MODE=mock|real`

### 2.2 明确不做

- 不新增用户端路由鉴权守卫（按本轮确认）
- 不修改部署脚本
- 不重构整体项目结构
- 不新增测试用例

## 3. 关键决策

1. 用户端认证与 admin 认证彻底隔离  
   用户端使用独立 API 与独立 store，不复用 `src/api/admin/AuthApi.ts` 和 `src/stores/admin/auth.ts`。

2. 跳转策略固定  
- 注册成功：跳转 `/user/onboarding`
- 登录成功：跳转 `/user/workspace`

3. 鉴权策略延后  
   本次不改 router 守卫，仅完成登录注册与会话持久化能力，为后续鉴权接入预留基础。

## 4. 页面设计方案

## 4.1 视觉与布局

- 双栏布局（桌面端）：左侧品牌价值区，右侧认证卡片
- 单栏布局（移动端）：价值区在上，表单在下
- 保持与用户端首页一致的蓝金轻品牌风格，避免紫色主色偏置
- 字体优先 `Plus Jakarta Sans`，中文回退 `PingFang SC` / `Microsoft YaHei`

## 4.2 登录页字段与交互

- 字段：`account`、`password`
- 校验：
  - 必填
  - 密码最少 6 位
- 提交流程：
  - 按钮进入 `loading` 态
  - 请求成功后提示并跳转 `/user/workspace`
  - 请求失败显示明确错误消息

## 4.3 注册页字段与交互

- 字段：`username`、`email`、`tenantName`、`password`、`confirmPassword`
- 校验：
  - 必填
  - 邮箱格式
  - 密码最少 6 位
  - 确认密码与密码一致
- 提交流程：
  - 按钮进入 `loading` 态
  - 请求成功后提示并跳转 `/user/onboarding`
  - 请求失败显示明确错误消息

## 4.4 可访问性与交互一致性

- 所有输入项提供明确 `label`
- 错误信息文案可见，不仅依赖颜色
- 过渡动画控制在 150-250ms
- 支持 `prefers-reduced-motion` 降级

## 5. 接口设计

新增文件：`src/api/user/AuthApi.ts`

### 5.1 接口列表

1. `POST /api/user/auth/register`  
入参：`username/email/password/tenantName`  
出参：`{ token, user }`

2. `POST /api/user/auth/login`  
入参：`account/password`  
出参：`{ token, user }`

3. `GET /api/user/auth/me`  
入参：`token`  
出参：`user`

4. `POST /api/user/auth/logout`  
入参：`token`  
出参：`null`

### 5.2 响应规范

延续前端统一结构：

- 成功：`{ error: 0, body: T, message: string, success: true }`
- 失败：`{ error: number, body: null, message: string, success: false }`

## 6. Mock 与真实接口切换

沿用现有用户 API 基础设施：

- `src/api/user/index.ts` 根据 `VITE_USER_API_MODE` 决定走 mock 或 real
- `mock`：由 `src/api/user/mockManager.ts` 处理
- `real`：由 `src/api/user/realApi.ts` 发起真实 HTTP 请求

本次只补认证相关 mock 路由，不改变切换机制。

## 7. 用户端状态管理设计

新增：`src/stores/user/auth.ts`

### 7.1 状态字段

- `token: string`
- `user: AuthUser | null`
- `hydrated: boolean`

### 7.2 动作设计

- `hydrate()`：从本地存储恢复会话并按需调用 `me`
- `login(payload)`：登录并写入会话
- `register(payload)`：注册并写入会话
- `logout()`：清理本地会话并调用登出接口
- `setSession()` / `clearSession()`：内部通用能力

### 7.3 存储键名

- `jay_boot_user_auth_token`
- `jay_boot_user_auth_user`

与 admin 侧键名隔离，避免互相覆盖。

## 8. 文档交付

新增 `docs/user/AuthAPI.md`，包含：

- 接口名称与功能说明
- 请求参数与响应参数表
- 请求/响应 JSON 示例
- 错误约定
- Mock/真实接口切换说明

## 9. 验证与验收标准

## 9.1 构建验证

前端改动完成后执行：

`npm run build`（在 `jay-boot-frontend` 目录）

## 9.2 验收标准

- 登录页和注册页均为可提交的真实表单，不再是占位骨架
- 页面中不内嵌业务 mock 常量，数据通过 API/store 获取
- 用户认证链路独立于 admin 链路
- `mock` 与 `real` 模式可配置切换
- `docs/user/AuthAPI.md` 与代码实现一致

## 10. 风险与回滚策略

### 10.1 风险

- 当前 router 仍使用 admin 会话判断，用户端登录后不会触发页面级鉴权逻辑（已确认本轮不处理）
- 若真实后端返回结构与统一响应协议不一致，`realApi` 分支需按后端格式调整

### 10.2 回滚

如需回滚本次能力，仅回退以下文件：

- `src/views/user/AuthLoginView.vue`
- `src/views/user/AuthRegisterView.vue`
- `src/api/user/AuthApi.ts`
- `src/stores/user/auth.ts`
- `src/api/user/mockManager.ts`（认证路由相关变更）
- `docs/user/AuthAPI.md`
