# 用户端 Features 对比选型页实现计划

> **给执行代理：** 必须按任务清单逐项执行并打勾。默认在当前会话内执行（Inline Execution），不创建测试用例。

**目标：** 完成 `/user/features` 对比选型页能力，实现页面、接口、mock 与文档的端到端闭环。  
**架构：** 新增 `FeaturesApi` 承接类型与请求，扩展 `user/mockManager` 提供 Features mock 数据，页面并行拉取 overview 与 role-fit 数据并渲染“矩阵 + 角色匹配”结构。  
**技术栈：** Vue3 + Composition API + TypeScript + Ant Design Vue + Vite

---

### 任务 1：新增 Features API 模块

**文件：**
- 新建：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/src/api/user/FeaturesApi.ts`

- [x] 步骤 1：定义 overview 与 role-fit 的类型接口
- [x] 步骤 2：实现 `getFeaturesOverviewApi` 与 `getFeaturesRoleFitApi`
- [x] 步骤 3：补齐 JSDoc 并保持 GET 请求规范

---

### 任务 2：扩展 user mockManager Features 接口

**文件：**
- 修改：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/src/api/user/mockManager.ts`

- [x] 步骤 1：新增 Features 类型与 mock 数据构造函数
- [x] 步骤 2：注册 `/api/user/features/overview` 与 `/api/user/features/role-fit`
- [x] 步骤 3：在 `initializeMockApis` 中接入 Features mock 初始化

---

### 任务 3：实现 FeaturesView 页面

**文件：**
- 修改：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/src/views/user/FeaturesView.vue`

- [x] 步骤 1：替换占位骨架为真实对比选型页面结构
- [x] 步骤 2：并行加载 overview 与 role-fit 数据，处理 loading/error/retry
- [x] 步骤 3：实现套餐对比矩阵、角色适配 Tab、FAQ 与 CTA
- [x] 步骤 4：补齐响应式样式与 reduced-motion 处理

---

### 任务 4：补齐 Features API 文档

**文件：**
- 新建：`D:/develop/CodeProject/jay-boot/jay-boot-frontend/docs/user/FeaturesAPI.md`

- [x] 步骤 1：编写两个接口的请求/响应说明与参数表
- [x] 步骤 2：补充 JSON 示例与错误响应约定
- [x] 步骤 3：写明 mock/真实接口切换配置

---

### 任务 5：构建验证

**文件：**
- 无代码文件变更

- [x] 步骤 1：执行 `npm run build`（`jay-boot-frontend` 目录）
- [x] 步骤 2：若失败，记录失败命令、首个关键报错、涉及文件路径并修复
- [x] 步骤 3：输出最终变更清单与验证结果

---

## 计划自检

- 规格覆盖：已覆盖页面功能、接口、mock、文档与验证。  
- 占位检查：无 TODO/TBD。  
- 一致性检查：页面定位为“对比选型”，并保留 mock/real 切换策略。
