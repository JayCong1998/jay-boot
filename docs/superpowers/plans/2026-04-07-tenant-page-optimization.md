# 租户管理页优化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在不新增后端接口的前提下，优化租户管理页的信息呈现与交互体验，突出业务可读性并完善加载/错误/提交反馈。

**Architecture:** 保持现有 API 层不变，仅重构 `TenantView.vue` 的页面结构与状态逻辑。页面分为“顶部区 + 业务概览区 + 基础设置区”，通过显式状态管理处理首次加载、刷新、失败重试和提交流程。样式以内聚到页面级 scoped CSS，不破坏全局主题。

**Tech Stack:** Vue 3 + TypeScript + Ant Design Vue + Axios（现有项目栈）

---

### Task 1: 页面结构重构（业务概览 + 基础设置）

**Files:**
- Modify: `jay-boot-ui/src/views/TenantView.vue`

- [ ] **Step 1: 重写模板结构**
  - 顶部区：标题、说明、刷新按钮
  - 概览区：3 张卡片展示工作区名称、套餐、负责人
  - 基础设置区：仅保留工作区名称编辑与提交

- [ ] **Step 2: 增加页面级样式**
  - 采用 scoped CSS 完成卡片布局、分组层级与移动端适配
  - 沿用全局设计变量，不新增全局样式文件

- [ ] **Step 3: 本地静态检查**
  - 确认模板与脚本类型无报错

### Task 2: 状态与数据流优化（加载、刷新、失败重试）

**Files:**
- Modify: `jay-boot-ui/src/views/TenantView.vue`

- [ ] **Step 1: 增加关键状态**
  - `loading`：首次加载
  - `saving`：提交中
  - `loadError`：首屏失败信息
  - `nameDirty`：输入是否已修改

- [ ] **Step 2: 实现 `loadTenant` 同步策略**
  - 初次/重试时同步输入值
  - 手动刷新时，若输入已修改则不覆盖输入，避免误清空

- [ ] **Step 3: 增加页面内错误态**
  - 首屏失败显示错误说明 + 重试按钮
  - 重试调用同一加载方法恢复页面

### Task 3: 表单校验与提交体验优化

**Files:**
- Modify: `jay-boot-ui/src/views/TenantView.vue`

- [ ] **Step 1: 增加前端校验规则**
  - `trim` 后非空
  - 长度 2-64（与后端约束一致）

- [ ] **Step 2: 优化提交流程**
  - 提交中按钮 loading、禁用重复点击
  - 成功后更新概览数据并重置 dirty 标识
  - 失败时保留输入并展示错误信息

- [ ] **Step 3: 增加交互细节**
  - 刷新按钮在 loading/saving 时禁用
  - 空值统一显示 `-`

### Task 4: 构建验证与交付说明

**Files:**
- Modify: `jay-boot-ui/src/views/TenantView.vue`
- Verify: `jay-boot-ui` 构建输出

- [ ] **Step 1: 执行构建验证**
  - Run: `npm run build`
  - Expected: 构建成功，无阻断错误

- [ ] **Step 2: 结果检查**
  - 页面无首屏“空白 + 只弹 toast”问题
  - 名称校验与提交流程符合预期

- [ ] **Step 3: 交付说明**
  - 说明修改文件、主要行为变化、验证命令结果
  - 标注未覆盖项（如未新增后端能力）
