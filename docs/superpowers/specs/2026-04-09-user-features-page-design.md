# 2026-04-09 用户端 Features 对比选型页设计

## 1. 背景与目标

当前 `/user/features` 页面仍为占位骨架，无法承担用户选型决策场景。  
本次目标是将该页面升级为“对比选型页”，帮助用户基于套餐能力差异快速做出购买或试用决策，并与现有用户端视觉与接口规范保持一致。

## 2. 范围定义

### 2.1 本次范围

- 实现 `/user/features` 页面功能（对比选型结构）
- 新增用户端 Features API 模块
- 扩展用户端 mockManager，补齐 Features 相关 mock 接口
- 新增 Features API 文档
- 保留 mock/真实接口配置切换能力

### 2.2 明确不做

- 不修改部署脚本
- 不重构项目整体结构
- 不新增用户端路由鉴权逻辑
- 不新增测试用例（按项目约束）

## 3. 页面定位与信息架构

页面定位：**对比选型决策页**（非品牌展示页、非流程教学页）。

### 3.1 模块划分

1. Hero 决策区  
- 目标：说明“按阶段选方案”的核心价值  
- 内容：标题、副文案、主次 CTA

2. 套餐对比矩阵（核心）  
- 维度：创作能力、协作能力、交付能力、数据能力、支持能力  
- 档位：Free / Pro / Team  
- 表达：支持状态 + 简短说明 + 推荐档位高亮

3. 角色适配区  
- 角色：个人创作者、团队负责人、运营负责人  
- 内容：推荐套餐、高频能力、落地建议

4. 决策 FAQ  
- 聚焦升级、协作、交付相关问题，降低决策阻力

5. 底部 CTA  
- 行动入口：注册试用、查看定价  
- 辅助信息：当前数据源（Mock/真实）

## 4. 交互与状态设计

## 4.1 数据加载策略

- 页面初始化并行请求：
  - `GET /api/user/features/overview`
  - `GET /api/user/features/role-fit`
- 加载中：展示 skeleton
- 失败：展示 error alert + 重试按钮
- 成功：渲染五大模块

## 4.2 关键交互

- 角色 Tab 切换：即时更新推荐套餐与建议内容
- 对比矩阵：推荐套餐列默认高亮
- CTA 行为：
  - `开始试用` -> `/user/auth/register`
  - `查看套餐` -> `/user/pricing`

## 4.3 可访问性与动效

- 标题、列表、表格/矩阵结构语义化
- 错误提示可读且不只依赖颜色
- 动效控制在 150-300ms
- 支持 `prefers-reduced-motion` 降级

## 5. 接口设计

新增文件：`src/api/user/FeaturesApi.ts`

### 5.1 获取对比页概览

- URL：`/api/user/features/overview`
- 方法：`GET`
- 返回：Hero、套餐概览、对比矩阵、FAQ、CTA、更新时间

### 5.2 获取角色适配数据

- URL：`/api/user/features/role-fit`
- 方法：`GET`
- 返回：默认角色、角色列表、每角色推荐套餐与建议

### 5.3 响应结构

沿用统一响应协议：

- 成功：`{ error: 0, body: T, message: string, success: true }`
- 失败：`{ error: number, body: null, message: string, success: false }`

## 6. Mock/真实接口切换

保持现有用户端机制不变：

- `VITE_USER_API_MODE=mock|real`
- `mock`：`src/api/user/mockManager.ts`
- `real`：`src/api/user/realApi.ts`

页面层不感知模式差异，统一走 `src/api/user/index.ts`。

## 7. 实现文件规划

### 7.1 前端页面

- 修改：`src/views/user/FeaturesView.vue`

### 7.2 API 层

- 新建：`src/api/user/FeaturesApi.ts`
- 修改：`src/api/user/mockManager.ts`（新增 features mock 路由）

### 7.3 文档

- 新建：`docs/user/FeaturesAPI.md`

## 8. 验收标准

1. `/user/features` 不再为占位页，具备完整“对比选型”模块
2. 页面数据来自 API，不在页面硬编码业务 mock 数据
3. 支持 mock/真实接口配置切换
4. 接口文档与代码实现一致
5. 前端编译通过（`npm run build`）

## 9. 风险与应对

1. 风险：对比矩阵字段过多导致页面信息过载  
应对：先实现核心能力分组，控制每组条目数量，保留后续扩展位。

2. 风险：真实后端字段与 mock 结构不一致  
应对：在 `FeaturesApi.ts` 固化类型边界，出现偏差时优先适配 API 层，不污染页面层。

3. 风险：移动端信息密度过高  
应对：矩阵在窄屏改为分组卡片/纵向展示，保证可读性与可操作性。
