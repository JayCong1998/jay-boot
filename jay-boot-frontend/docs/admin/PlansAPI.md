# Plans API

## 1. 接口概览

- 分页查询套餐：`GET /api/admin/plans`
- 创建套餐：`POST /api/admin/plans`
- 更新套餐：`POST /api/admin/plans/{id}`
- 更新套餐状态：`POST /api/admin/plans/{id}/status`

统一返回结构：

```json
{
  "code": 200,
  "body": {},
  "message": "OK",
  "success": true
}
```

## 2. 鉴权

所有接口需要请求头：

```text
satoken: <token>
```

## 3. 接口明细

### 3.1 分页查询套餐

- 地址：`/api/admin/plans`
- 方法：`GET`

Query 参数：

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页条数，默认 10 |
| keyword | string | 否 | 关键字，匹配 `code/name` |
| status | string | 否 | 状态：`ACTIVE` / `INACTIVE` |
| billingCycle | string | 否 | 周期：`MONTHLY` / `YEARLY` |

响应示例：

```json
{
  "code": 200,
  "body": {
    "records": [
      {
        "id": "1949000000000000001",
        "code": "PRO_MONTHLY",
        "name": "专业版（月付）",
        "billingCycle": "MONTHLY",
        "quotaJson": "{\"seats\":10,\"tokensMonthly\":500000}",
        "price": 29900,
        "status": "ACTIVE",
        "updatedTime": "2026-04-10T22:45:12"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10
  },
  "message": "OK",
  "success": true
}
```

### 3.2 创建套餐

- 地址：`/api/admin/plans`
- 方法：`POST`

请求示例：

```json
{
  "code": "PRO_MONTHLY",
  "name": "专业版（月付）",
  "billingCycle": "MONTHLY",
  "quotaJson": "{\"seats\":10,\"tokensMonthly\":500000}",
  "price": 29900,
  "status": "ACTIVE"
}
```

### 3.3 更新套餐

- 地址：`/api/admin/plans/{id}`
- 方法：`POST`

请求示例：

```json
{
  "name": "专业版（月付）",
  "billingCycle": "MONTHLY",
  "quotaJson": "{\"seats\":20,\"tokensMonthly\":900000}",
  "price": 39900,
  "status": "ACTIVE"
}
```

### 3.4 更新套餐状态

- 地址：`/api/admin/plans/{id}/status`
- 方法：`POST`

请求示例：

```json
{
  "status": "INACTIVE"
}
```

## 4. 错误码说明

| code | 含义 | 示例场景 |
|---|---|---|
| 400 | 参数错误 | `quotaJson` 非法 JSON |
| 401 | 未登录 | 请求头缺失或 token 失效 |
| 403 | 无权限 | 非管理员调用写接口 |
| 404 | 资源不存在 | 套餐 ID 不存在 |
| 409 | 资源冲突 | 套餐编码重复 |
| 500 | 服务内部错误 | 未预期异常 |