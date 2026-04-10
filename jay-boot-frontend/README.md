# Jay Boot Frontend

基于 `Vue 3 + TypeScript + Vite` 的前端项目。

## 技术栈

- Vue 3（Composition API）
- Vue Router 4
- Pinia
- Ant Design Vue
- Vite

## 当前主要路由

- `/admin/dashboard` 控制台总览
- `/admin/rbac` 角色权限
- `/admin/billing` 订阅计费
- `/admin/apikey` API Key 管理
- `/admin/ai-gateway` AI Gateway
- `/admin/usage` 用量与运营
- `/admin/auth/login` 管理端登录
- `/admin/auth/register` 管理端注册
- `/user/home` 用户端首页
- `/user/workspace` 用户端工作台
- `/user/auth/login` 用户端登录（仅邮箱）
- `/user/auth/register` 用户端注册（无租户字段）

## 本地命令

```bash
npm install
npm run build
```