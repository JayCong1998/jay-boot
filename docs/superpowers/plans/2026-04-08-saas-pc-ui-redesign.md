# SaaS PC端UI重设计（ui目录）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在仅修改 `ui/` 原型目录的前提下，完成一套轻科技风格、标准密度、全量页面覆盖的 PC 端 SaaS 脚手架 UI 重设计。

**Architecture:** 以 `ui/index.html + ui/assets/global.css + ui/assets/shell.js + ui/pages/*.html` 为实现核心，先统一设计 Token 和通用组件，再按页面分批改造，最后做导航联通、编码与断点回归。遵循 AGENTS 约束，本计划不新增测试用例，采用静态校验与人工验收替代自动化测试。

**Tech Stack:** HTML5 + CSS3（CSS Variables）+ Vanilla JavaScript

---

## 文件结构与职责

- Modify: `ui/assets/global.css`
- 责任: 定义轻科技版 Design Tokens、通用布局类、组件类、状态类、响应式与动效规范。

- Modify: `ui/index.html`
- 责任: 壳层布局重构（侧栏分组、顶部状态栏、iframe容器）、统一语义与可访问属性。

- Modify: `ui/assets/shell.js`
- 责任: 菜单映射、hash路由、标题同步、iframe加载状态与异常回退。

- Modify: `ui/pages/dashboard.html`
- 责任: 标准密度首页模板（4 KPI + 2趋势区 + 2列表区）。

- Modify: `ui/pages/tenant.html`
- 责任: 租户概览、租户列表、状态与操作区统一。

- Modify: `ui/pages/rbac.html`
- 责任: 角色列表、权限矩阵、变更记录结构统一。

- Modify: `ui/pages/billing.html`
- 责任: 套餐摘要、账单趋势、流水表统一。

- Modify: `ui/pages/apikey.html`
- 责任: Key列表、权限标签、创建入口与风险提示区统一。

- Modify: `ui/pages/usage.html`
- 责任: 用量KPI、配额进度、明细表统一。

- Modify: `ui/pages/ai-gateway.html`
- 责任: 路由状态、延迟/成本趋势、告警区统一。

- Modify: `ui/pages/auth-login.html`
- 责任: 品牌侧栏 + 登录卡片，统一视觉语言。

- Modify: `ui/pages/auth-register.html`
- 责任: 注册卡片与协议提示，保持与登录页同源。

### Task 1: 全局设计Token与通用组件层

**Files:**
- Modify: `ui/assets/global.css`

- [ ] **Step 1: 重写 Design Tokens（颜色/字体/圆角/阴影/间距）**

```css
:root {
  --font-sans: "Plus Jakarta Sans", "Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
  --bg-page: #f3f7fb;
  --bg-surface: #ffffff;
  --text-main: #0f172a;
  --text-sub: #475569;
  --line: #dce6f0;
  --line-soft: #eaf0f6;
  --brand: #0a84ff;
  --brand-strong: #0b5fcc;
  --ok: #16a34a;
  --warn: #d97706;
  --danger: #dc2626;
  --radius-sm: 10px;
  --radius-md: 14px;
  --radius-lg: 18px;
  --shadow-card: 0 8px 24px rgba(15, 23, 42, 0.06);
  --shadow-pop: 0 14px 36px rgba(15, 23, 42, 0.1);
}
```

- [ ] **Step 2: 建立壳层布局类（sidebar/topbar/frame）**

```css
.app-shell {
  display: grid;
  grid-template-columns: 272px 1fr;
  min-height: 100vh;
}
.sidebar {
  background: linear-gradient(180deg, #0f172a, #132b46);
  border-right: 1px solid rgba(148, 163, 184, 0.2);
}
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 22px;
  border-bottom: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
}
.frame {
  width: 100%;
  height: calc(100vh - 108px);
  border-radius: var(--radius-lg);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-pop);
  background: #fff;
}
```

- [ ] **Step 3: 建立页面模板组件类（PageHeader/KpiCard/Panel/Table/Badge）**

```css
.page-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
.kpi-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.kpi-card { border: 1px solid var(--line); border-radius: var(--radius-md); background: var(--bg-surface); box-shadow: var(--shadow-card); padding: 16px; }
.panel-card { border: 1px solid var(--line); border-radius: var(--radius-md); background: #fff; box-shadow: var(--shadow-card); padding: 16px; }
.table-wrap table { width: 100%; border-collapse: collapse; }
.badge-success { color: #166534; background: #dcfce7; }
.badge-warn { color: #92400e; background: #fef3c7; }
.badge-danger { color: #991b1b; background: #fee2e2; }
```

- [ ] **Step 4: 增加状态与动效规则（loading/empty/error + reduced-motion）**

```css
.state-empty,
.state-error {
  border: 1px dashed var(--line);
  border-radius: var(--radius-md);
  padding: 16px;
  color: var(--text-sub);
  background: #f8fbff;
}
.fade-in { animation: fadeIn 180ms ease both; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(4px);} to { opacity: 1; transform: translateY(0);} }
@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; transition: none !important; }
}
```

- [ ] **Step 5: 增加响应式断点（1200/1024/768）**

```css
@media (max-width: 1200px) { .kpi-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 1024px) { .app-shell { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .kpi-grid, .split-grid { grid-template-columns: 1fr; } }
```

- [ ] **Step 6: 提交本任务**

```bash
git add ui/assets/global.css
git commit -m "feat(ui): 统一轻科技设计token与通用组件层"
```

### Task 2: 壳层页面与导航脚本重构

**Files:**
- Modify: `ui/index.html`
- Modify: `ui/assets/shell.js`

- [ ] **Step 1: 重写 `ui/index.html` 的侧栏分组结构与文案**

```html
<section class="menu-group">
  <p class="menu-title">总览</p>
  <button class="menu-btn active" data-page="dashboard">工作台</button>
</section>
<section class="menu-group">
  <p class="menu-title">租户与权限</p>
  <button class="menu-btn" data-page="tenant">租户管理</button>
  <button class="menu-btn" data-page="rbac">角色权限</button>
</section>
```

- [ ] **Step 2: 重写顶部状态栏与 iframe 容器结构**

```html
<header class="topbar">
  <div class="breadcrumb">
    <small>JayBoot / SaaS Console</small>
    <strong id="currentPageName">工作台</strong>
  </div>
  <div class="topbar-right">
    <span class="chip">Enterprise</span>
    <span class="chip">CN-East</span>
    <span class="chip">SLA 99.95%</span>
    <span class="avatar">SY</span>
  </div>
</header>
<main class="frame-wrap">
  <iframe id="appFrame" class="frame" title="JayBoot 页面原型"></iframe>
</main>
```

- [ ] **Step 3: 更新 `ui/assets/shell.js` 的页面映射与标题**

```js
const PAGE_MAP = {
  dashboard: { title: "工作台", src: "pages/dashboard.html" },
  tenant: { title: "租户管理", src: "pages/tenant.html" },
  rbac: { title: "角色权限", src: "pages/rbac.html" },
  billing: { title: "订阅计费", src: "pages/billing.html" },
  apikey: { title: "API Key", src: "pages/apikey.html" },
  usage: { title: "用量统计", src: "pages/usage.html" },
  ai: { title: "AI 网关", src: "pages/ai-gateway.html" },
  login: { title: "登录", src: "pages/auth-login.html" },
  register: { title: "注册", src: "pages/auth-register.html" }
};
```

- [ ] **Step 4: 增加 iframe 加载失败回退**

```js
frame.addEventListener("error", () => {
  frame.classList.remove("loading");
  currentName.textContent = "页面加载失败";
});
```

- [ ] **Step 5: 静态检查脚本语法**

Run: `node --check ui/assets/shell.js`  
Expected: 无语法错误输出。

- [ ] **Step 6: 提交本任务**

```bash
git add ui/index.html ui/assets/shell.js
git commit -m "feat(ui): 重构控制台壳层与导航路由脚本"
```

### Task 3: 工作台页面（dashboard）重设计

**Files:**
- Modify: `ui/pages/dashboard.html`

- [ ] **Step 1: 重写页面头部与操作区**

```html
<header class="page-header">
  <div>
    <h1 class="page-title">运营工作台</h1>
    <p class="page-subtitle">统一查看今日指标、趋势、待办与告警。</p>
  </div>
  <div class="btns">
    <button class="btn btn-secondary">导出日报</button>
    <button class="btn btn-primary">新建任务</button>
  </div>
</header>
```

- [ ] **Step 2: 按标准密度重写4个 KPI 卡片**

```html
<section class="kpi-grid fade-in">
  <article class="kpi-card"><p class="muted">活跃用户</p><div class="metric-value">1,284</div><div class="metric-trend up">+8.2%</div></article>
  <article class="kpi-card"><p class="muted">API 调用量</p><div class="metric-value">96,340</div><div class="metric-trend up">+12.6%</div></article>
  <article class="kpi-card"><p class="muted">AI 成本</p><div class="metric-value">$842</div><div class="metric-trend down">+6.4%</div></article>
  <article class="kpi-card"><p class="muted">错误率</p><div class="metric-value">0.38%</div><div class="metric-trend up">-0.12%</div></article>
</section>
```

- [ ] **Step 3: 重写 2 趋势区 + 2 列表区结构**

```html
<section class="split-grid">
  <article class="panel-card"><h3>近7天调用趋势</h3><div class="chart-placeholder"></div></article>
  <article class="panel-card"><h3>营收与续费进度</h3><div class="chart-placeholder"></div></article>
</section>
<section class="split-grid">
  <article class="panel-card table-wrap"><h3>待处理事项</h3><table>...</table></article>
  <article class="panel-card table-wrap"><h3>系统告警</h3><table>...</table></article>
</section>
```

- [ ] **Step 4: 提交本任务**

```bash
git add ui/pages/dashboard.html
git commit -m "feat(ui): 重设计工作台页面为标准密度模板"
```

### Task 4: 租户与权限页面改造（tenant + rbac）

**Files:**
- Modify: `ui/pages/tenant.html`
- Modify: `ui/pages/rbac.html`

- [ ] **Step 1: 改造 `tenant.html` 的页面头部与租户概览区**

```html
<header class="page-header">
  <div><h1 class="page-title">租户管理</h1><p class="page-subtitle">管理工作区、成员与套餐状态。</p></div>
  <div class="btns"><button class="btn btn-secondary">导出租户</button><button class="btn btn-primary">新建租户</button></div>
</header>
<section class="kpi-grid">
  <article class="kpi-card"><p class="muted">租户总数</p><div class="metric-value">42</div></article>
  <article class="kpi-card"><p class="muted">活跃租户</p><div class="metric-value">36</div></article>
  <article class="kpi-card"><p class="muted">试用中</p><div class="metric-value">8</div></article>
  <article class="kpi-card"><p class="muted">待续费</p><div class="metric-value">5</div></article>
</section>
```

- [ ] **Step 2: 改造 `tenant.html` 的列表与状态标签结构**

```html
<article class="panel-card table-wrap">
  <h3>租户列表</h3>
  <table>
    <thead><tr><th>租户</th><th>套餐</th><th>状态</th><th>负责人</th></tr></thead>
    <tbody><tr><td>workspace-g7m2</td><td>PRO</td><td><span class="badge badge-success">正常</span></td><td>Susan</td></tr></tbody>
  </table>
</article>
```

- [ ] **Step 3: 改造 `rbac.html` 为角色列表 + 权限矩阵 + 变更记录**

```html
<section class="split-grid">
  <article class="panel-card table-wrap"><h3>角色列表</h3><table>...</table></article>
  <article class="panel-card"><h3>权限矩阵</h3><div class="matrix-placeholder"></div></article>
</section>
<article class="panel-card table-wrap"><h3>变更记录</h3><table>...</table></article>
```

- [ ] **Step 4: 提交本任务**

```bash
git add ui/pages/tenant.html ui/pages/rbac.html
git commit -m "feat(ui): 统一租户与权限页面信息架构"
```

### Task 5: 商业化页面改造（billing + usage）

**Files:**
- Modify: `ui/pages/billing.html`
- Modify: `ui/pages/usage.html`

- [ ] **Step 1: 改造 `billing.html` 为套餐摘要 + 账单趋势 + 流水表**

```html
<section class="kpi-grid">
  <article class="kpi-card"><p class="muted">当前套餐</p><div class="metric-value">PRO</div></article>
  <article class="kpi-card"><p class="muted">本月账单</p><div class="metric-value">$3,240</div></article>
  <article class="kpi-card"><p class="muted">续费率</p><div class="metric-value">76%</div></article>
  <article class="kpi-card"><p class="muted">逾期账单</p><div class="metric-value">2</div></article>
</section>
```

- [ ] **Step 2: 改造 `usage.html` 为用量KPI + 配额进度 + 调用明细**

```html
<section class="split-grid">
  <article class="panel-card"><h3>配额进度</h3><div class="progress"><span style="width:72%"></span></div></article>
  <article class="panel-card"><h3>调用趋势</h3><div class="chart-placeholder"></div></article>
</section>
<article class="panel-card table-wrap"><h3>调用明细</h3><table>...</table></article>
```

- [ ] **Step 3: 提交本任务**

```bash
git add ui/pages/billing.html ui/pages/usage.html
git commit -m "feat(ui): 重构商业化页面布局与状态表达"
```

### Task 6: 开发者页面改造（apikey + ai-gateway）

**Files:**
- Modify: `ui/pages/apikey.html`
- Modify: `ui/pages/ai-gateway.html`

- [ ] **Step 1: 改造 `apikey.html` 为 Key列表 + 风险提示 + 操作入口**

```html
<section class="split-grid">
  <article class="panel-card table-wrap"><h3>API Key 列表</h3><table>...</table></article>
  <article class="panel-card"><h3>安全提示</h3><div class="state-error">请定期轮换 Key，避免共享明文凭证。</div></article>
</section>
```

- [ ] **Step 2: 改造 `ai-gateway.html` 为路由状态 + 趋势 + 告警区**

```html
<section class="kpi-grid">
  <article class="kpi-card"><p class="muted">在线模型</p><div class="metric-value">12</div></article>
  <article class="kpi-card"><p class="muted">平均延迟</p><div class="metric-value">412ms</div></article>
  <article class="kpi-card"><p class="muted">今日成本</p><div class="metric-value">$842</div></article>
  <article class="kpi-card"><p class="muted">限流事件</p><div class="metric-value">3</div></article>
</section>
```

- [ ] **Step 3: 提交本任务**

```bash
git add ui/pages/apikey.html ui/pages/ai-gateway.html
git commit -m "feat(ui): 重构开发者页为统一控制台风格"
```

### Task 7: 认证页面改造（login + register）

**Files:**
- Modify: `ui/pages/auth-login.html`
- Modify: `ui/pages/auth-register.html`

- [ ] **Step 1: 改造登录页为品牌侧栏 + 表单卡片布局**

```html
<div class="auth-wrap">
  <aside class="auth-side">
    <h1>JayBoot SaaS Console</h1>
    <p>统一租户、计费、权限与 AI 网关管理。</p>
  </aside>
  <section class="auth-card">
    <h2>欢迎登录</h2>
    <div class="field"><label for="email">邮箱</label><input id="email" type="email" /></div>
  </section>
</div>
```

- [ ] **Step 2: 改造注册页并统一字段与交互层级**

```html
<section class="auth-card">
  <h2>创建账号</h2>
  <div class="field"><label for="reg-email">邮箱</label><input id="reg-email" type="email" /></div>
  <div class="field"><label for="workspace">工作区名称</label><input id="workspace" type="text" /></div>
  <button class="btn btn-primary">注册并进入控制台</button>
</section>
```

- [ ] **Step 3: 提交本任务**

```bash
git add ui/pages/auth-login.html ui/pages/auth-register.html
git commit -m "feat(ui): 统一认证页轻科技视觉与表单结构"
```

### Task 8: 联调回归与编码规范校验

**Files:**
- Verify: `ui/index.html`
- Verify: `ui/assets/global.css`
- Verify: `ui/assets/shell.js`
- Verify: `ui/pages/*.html`

- [ ] **Step 1: 校验页面文件完整性（shell映射的页面都存在）**

Run:
```powershell
$pages = @('dashboard','tenant','rbac','billing','apikey','usage','ai-gateway','auth-login','auth-register');
$pages | ForEach-Object { if (-not (Test-Path -LiteralPath "ui/pages/$_.html")) { Write-Output "MISSING: $_.html" } }
```
Expected: 无 `MISSING` 输出。

- [ ] **Step 2: 校验关键文件均为 UTF-8 无 BOM**

Run:
```powershell
$files = @('ui/index.html','ui/assets/global.css','ui/assets/shell.js') + (Get-ChildItem -LiteralPath 'ui/pages' -Filter '*.html' | ForEach-Object FullName);
foreach ($f in $files) {
  $b = [System.IO.File]::ReadAllBytes((Resolve-Path $f));
  if ($b.Length -ge 3 -and $b[0] -eq 239 -and $b[1] -eq 187 -and $b[2] -eq 191) { Write-Output "BOM: $f" }
}
```
Expected: 无 `BOM:` 输出。

- [ ] **Step 3: 校验无乱码文案残留（常见异常字符）**

Run: `rg -n "宸|绉|鐧|娉|�" ui`  
Expected: 无匹配输出。

- [ ] **Step 4: 手工验收四个断点与交互反馈**

检查清单：
1. 1440、1200、1024、768 下布局正常。
2. 所有可点击元素有 `cursor: pointer`。
3. hover/focus/active 状态可见。
4. iframe 页面切换正常，标题同步正常。

- [ ] **Step 5: 提交本任务**

```bash
git add ui
git commit -m "chore(ui): 完成pc端saas脚手架原型重设计与回归校验"
```

## 计划自检记录

### 1. Spec覆盖检查
- 仅改 `ui/`：Task 1-8 均只涉及 `ui` 目录，覆盖完成。
- 轻科技风格：Task 1（Token）+ Task 2（壳层）+ Task 3-7（页面）覆盖完成。
- 标准密度：Task 3 明确 4 KPI + 2趋势 + 2列表，完成。
- 全量页面：Task 2-7 覆盖 `index + 9个子页面`，完成。
- 可访问与动效：Task 1、Task 2、Task 8 覆盖完成。

### 2. 占位符扫描
- 计划中无 `TODO`、`TBD`、`implement later` 等占位描述。

### 3. 命名一致性检查
- 页面键名统一为 `dashboard/tenant/rbac/billing/apikey/usage/ai/login/register`。
- 布局类命名统一为 `kpi-grid/split-grid/panel-card/page-header`。