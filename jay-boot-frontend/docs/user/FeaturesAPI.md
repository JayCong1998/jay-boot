## 获取 Features 对比页概览

**接口名称：** 获取 Features 对比页概览  
**功能描述：** 返回 `/user/features` 页面渲染所需的 Hero、套餐卡、能力对比矩阵、FAQ 与底部 CTA。  
**接口地址：** `/api/user/features/overview`  
**请求方式：** `GET`

### 请求参数

```json
{}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| 无 | - | - | 本接口无请求参数 | - |

### 响应参数

```json
{
  "code": 200,
  "body": {
    "hero": {
      "eyebrow": "按阶段选择最适合的功能方案",
      "title": "把功能差异讲清楚，让选型一步到位",
      "description": "围绕创作、协作、交付、数据与支持五个维度...",
      "primaryActionText": "开始免费试用",
      "secondaryActionText": "查看套餐价格"
    },
    "planCards": [],
    "comparisonGroups": [],
    "faqList": [],
    "finalCta": {
      "title": "还在犹豫选哪档？先试用再决定",
      "description": "从 Free 开始，按业务增长平滑升级...",
      "primaryActionText": "立即注册试用",
      "secondaryActionText": "查看定价页"
    },
    "updatedAt": "2026-04-09T09:00:00.000Z"
  },
  "message": "获取 features 概览成功",
  "success": true
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| code | number | 是 | 状态码，200 为成功 | 200 |
| body | object | 是 | 业务数据主体 | - |
| body.hero | object | 是 | Hero 区域数据 | - |
| body.planCards | object[] | 是 | 套餐概览卡列表 | - |
| body.comparisonGroups | object[] | 是 | 对比矩阵分组 | - |
| body.faqList | object[] | 是 | FAQ 列表 | - |
| body.finalCta | object | 是 | 底部 CTA 数据 | - |
| body.updatedAt | string | 是 | 更新时间（ISO） | 2026-04-09T09:00:00.000Z |
| message | string | 是 | 响应消息 | 获取 features 概览成功 |
| success | boolean | 是 | 是否成功 | true |

---

## 获取角色适配数据

**接口名称：** 获取角色适配数据  
**功能描述：** 返回不同角色下的推荐套餐与决策建议。  
**接口地址：** `/api/user/features/role-fit`  
**请求方式：** `GET`

### 请求参数

```json
{}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| 无 | - | - | 本接口无请求参数 | - |

### 响应参数

```json
{
  "code": 200,
  "body": {
    "defaultRoleKey": "creator",
    "roles": [
      {
        "key": "creator",
        "label": "个人创作者",
        "recommendedPlan": "PRO",
        "highlights": ["高频内容生成", "多版本 A/B 测试"],
        "advice": "建议先用 Pro 建立稳定产出节奏..."
      }
    ]
  },
  "message": "获取角色适配数据成功",
  "success": true
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| code | number | 是 | 状态码，200 为成功 | 200 |
| body | object | 是 | 业务数据主体 | - |
| body.defaultRoleKey | string | 是 | 默认激活角色 key | creator |
| body.roles | object[] | 是 | 角色建议列表 | - |
| body.roles[].key | string | 是 | 角色 key | creator |
| body.roles[].label | string | 是 | 角色名称 | 个人创作者 |
| body.roles[].recommendedPlan | string | 是 | 推荐套餐 | PRO |
| body.roles[].highlights | string[] | 是 | 重点能力说明 | ["高频内容生成"] |
| body.roles[].advice | string | 是 | 决策建议 | 建议先用 Pro... |
| message | string | 是 | 响应消息 | 获取角色适配数据成功 |
| success | boolean | 是 | 是否成功 | true |

---

## 错误响应约定

```json
{
  "code": 500,
  "body": null,
  "message": "错误描述",
  "success": false
}
```

---

## Mock 与真实接口切换

- `VITE_USER_API_MODE=mock`：走 `src/api/user/mockManager.ts`
- `VITE_USER_API_MODE=real`：走真实后端
- `VITE_USER_API_BASE_URL`：真实接口基础地址
- `VITE_USER_MOCK_DELAY_MS`：mock 延迟（毫秒）

示例：

```bash
VITE_USER_API_MODE=mock
VITE_USER_API_BASE_URL=http://127.0.0.1:8080
VITE_USER_MOCK_DELAY_MS=180
```
