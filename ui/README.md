# Java Micro-SaaS Starter UI 原型

## 使用方式

1. 直接在浏览器打开 `ui/index.html`
2. 通过左侧导航进入各业务页面
3. 点击页面中的按钮可触发原型交互提示

## 页面清单

- `index.html`：原型总览与模块映射
- `pages/auth-register.html`：注册引导
- `pages/auth-login.html`：登录与会话
- `pages/dashboard.html`：控制台总览
- `pages/tenant.html`：Workspace 租户管理
- `pages/rbac.html`：角色权限矩阵
- `pages/billing.html`：订阅计费
- `pages/apikey.html`：API Key 管理
- `pages/ai-gateway.html`：AI Gateway
- `pages/usage.html`：用量、任务与通知中心

## 说明

- 该目录为产品原型，不依赖前端构建流程
- 视觉风格基于 Tech Business（Space Grotesk + DM Sans + Blue/Cyan）
- 已覆盖 MVP 核心模块与关键运营能力
- 顶部操作区支持「强科技 / 中科技」档位切换，并自动记住上次选择
