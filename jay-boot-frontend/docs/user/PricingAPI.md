## 获取订阅定价页概览

**接口名称：** 获取订阅定价页概览  
**功能描述：** 返回 `/user/pricing` 页面渲染所需的 Hero、周期切换、套餐卡、权益对比、FAQ 与底部 CTA。  
**接口地址：** `/api/user/pricing/overview`  
**请求方式：** `GET`

### 请求参数

```json
{
  "billingCycle": "MONTHLY"
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| billingCycle | string | 否 | 计费周期，支持 `MONTHLY` / `YEARLY`，默认 `MONTHLY` | MONTHLY |

### 响应参数

```json
{
  "code": 200,
  "body": {
    "hero": {
      "eyebrow": "定价透明，按增长阶段选择",
      "title": "订阅定价",
      "description": "通过清晰权益和周期折扣，帮助你更快完成购买决策。",
      "primaryActionText": "咨询推荐方案",
      "secondaryActionText": "进入支付页"
    },
    "cycleOptions": [
      {
        "key": "MONTHLY",
        "label": "月付",
        "hint": "适合先验证增长节奏"
      },
      {
        "key": "YEARLY",
        "label": "年付",
        "hint": "相比月付更优惠"
      }
    ],
    "selectedCycle": "MONTHLY",
    "planCards": [
      {
        "id": "1949000000000000002",
        "code": "PRO_MONTHLY",
        "name": "Pro 创作版",
        "tag": "Most Popular",
        "price": 12900,
        "priceText": "¥129",
        "billingCycleText": "/ 月",
        "fitFor": "稳定生产内容的创作者",
        "highlights": ["每月 1200 次生成", "高级模板与导出交付", "优先模型队列与客服支持"],
        "recommended": true,
        "contactSales": false
      }
    ],
    "comparisonRows": [
      {
        "id": "monthly_quota",
        "feature": "月度生成额度",
        "description": "每个套餐的默认月度生成能力",
        "cells": [
          {
            "planCode": "STARTER_MONTHLY",
            "value": "200 次/月"
          },
          {
            "planCode": "PRO_MONTHLY",
            "value": "1200 次/月"
          }
        ]
      }
    ],
    "faqList": [
      {
        "id": "faq_1",
        "question": "可以随时升级或降级吗？",
        "answer": "支持按账单周期切换，差价自动折算。"
      }
    ],
    "finalCta": {
      "title": "从今天开始，让内容产出稳定转化为收入",
      "description": "优先推荐 Pro 档，后续可按团队规模平滑升级。",
      "primaryActionText": "立即开通",
      "secondaryActionText": "预约演示"
    },
    "updatedAt": "2026-04-13T12:00:00.000Z"
  },
  "message": "获取定价页数据成功",
  "success": true
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| code | number | 是 | 状态码，200 为成功 | 200 |
| body | object | 是 | 业务数据主体 | - |
| body.hero | object | 是 | 定价页 Hero 数据 | - |
| body.cycleOptions | object[] | 是 | 计费周期选项 | - |
| body.cycleOptions[].key | string | 是 | 周期编码 | MONTHLY |
| body.cycleOptions[].label | string | 是 | 周期显示名称 | 月付 |
| body.cycleOptions[].hint | string | 是 | 周期说明文案 | 适合先验证增长节奏 |
| body.selectedCycle | string | 是 | 当前生效周期 | MONTHLY |
| body.planCards | object[] | 是 | 套餐卡片列表 | - |
| body.planCards[].id | string | 是 | 套餐 ID | 1949000000000000002 |
| body.planCards[].code | string | 是 | 套餐编码 | PRO_MONTHLY |
| body.planCards[].name | string | 是 | 套餐名称 | Pro 创作版 |
| body.planCards[].tag | string | 否 | 套餐角标文案（如 Most Popular） | Most Popular |
| body.planCards[].price | number | 是 | 套餐价格（单位：分） | 12900 |
| body.planCards[].priceText | string | 是 | 套餐价格展示文案 | ¥129 |
| body.planCards[].billingCycleText | string | 是 | 周期展示文案 | / 月 |
| body.planCards[].fitFor | string | 是 | 适用人群描述 | 稳定生产内容的创作者 |
| body.planCards[].highlights | string[] | 是 | 套餐核心权益摘要 | ["每月 1200 次生成"] |
| body.planCards[].recommended | boolean | 是 | 是否推荐档位 | true |
| body.planCards[].contactSales | boolean | 是 | 是否为联系销售档位 | false |
| body.comparisonRows | object[] | 是 | 权益对比行列表 | - |
| body.comparisonRows[].id | string | 是 | 对比行唯一标识 | monthly_quota |
| body.comparisonRows[].feature | string | 是 | 权益名称 | 月度生成额度 |
| body.comparisonRows[].description | string | 是 | 权益描述 | 每个套餐的默认月度生成能力 |
| body.comparisonRows[].cells | object[] | 是 | 各套餐对应值 | - |
| body.comparisonRows[].cells[].planCode | string | 是 | 套餐编码 | PRO_MONTHLY |
| body.comparisonRows[].cells[].value | string | 是 | 权益值文案 | 1200 次/月 |
| body.faqList | object[] | 是 | FAQ 列表 | - |
| body.faqList[].id | string | 是 | FAQ ID | faq_1 |
| body.faqList[].question | string | 是 | FAQ 问题 | 可以随时升级或降级吗？ |
| body.faqList[].answer | string | 是 | FAQ 回答 | 支持按账单周期切换，差价自动折算。 |
| body.finalCta | object | 是 | 页面底部 CTA 数据 | - |
| body.finalCta.title | string | 是 | CTA 标题 | 从今天开始，让内容产出稳定转化为收入 |
| body.finalCta.description | string | 是 | CTA 描述 | 优先推荐 Pro 档，后续可按团队规模平滑升级。 |
| body.finalCta.primaryActionText | string | 是 | 主按钮文案 | 立即开通 |
| body.finalCta.secondaryActionText | string | 是 | 次按钮文案 | 预约演示 |
| body.updatedAt | string | 是 | 数据更新时间（ISO） | 2026-04-13T12:00:00.000Z |
| message | string | 是 | 响应消息 | 获取定价页数据成功 |
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

常见错误码：

- `400`：请求参数不合法（如 billingCycle 非法值）
- `404`：当前周期无可售套餐
- `500`：服务内部异常
