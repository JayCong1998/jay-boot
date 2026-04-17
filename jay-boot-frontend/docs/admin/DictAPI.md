# 字典管理接口文档

## 1. 接口概览

公共字典查询（前端展示层使用）：

- 按类型查询选项：`GET /api/public/dict/options`
- 批量查询选项：`GET /api/public/dict/options/batch`

管理端字典维护（需要管理员权限）：

- 分页查询字典类型：`GET /api/admin/dicts/types/page`
- 查询字典类型详情：`GET /api/admin/dicts/types/{id}`
- 创建字典类型：`POST /api/admin/dicts/types`
- 更新字典类型：`POST /api/admin/dicts/types/{id}`
- 更新字典类型状态：`POST /api/admin/dicts/types/{id}/status`
- 删除字典类型：`POST /api/admin/dicts/types/{id}/delete`
- 分页查询字典项：`GET /api/admin/dicts/items/page`
- 查询字典项详情：`GET /api/admin/dicts/items/{id}`
- 创建字典项：`POST /api/admin/dicts/items`
- 更新字典项：`POST /api/admin/dicts/items/{id}`
- 更新字典项状态：`POST /api/admin/dicts/items/{id}/status`
- 更新字典项排序：`POST /api/admin/dicts/items/{id}/sort`
- 删除字典项：`POST /api/admin/dicts/items/{id}/delete`

统一响应结构：

```json
{
  "code": 200,
  "body": {},
  "message": "OK",
  "success": true
}
```

## 2. 鉴权

- `/api/public/dict/*`：公共读取接口，当前不要求管理员角色。
- `/api/admin/dicts/*`：需要请求头中的管理员登录令牌。

请求头示例：

```text
satoken: <token>
```

## 3. 接口详情

### 3.1 按类型查询选项

- 地址：`/api/public/dict/options`
- 方法：`GET`

Query 参数：

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| typeCode | string | 是 | 字典类型编码，例如 `plan_status` |

响应示例：

```json
{
  "code": 200,
  "body": [
    {
      "itemCode": "ACTIVE",
      "value": "ACTIVE",
      "label": "启用",
      "sort": 10,
      "color": "success",
      "extJson": null
    }
  ],
  "message": "OK",
  "success": true
}
```

### 3.2 批量查询选项

- 地址：`/api/public/dict/options/batch`
- 方法：`GET`

Query 参数：

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| typeCodes | string | 是 | 逗号分隔的类型编码，例如 `admin_user_role,admin_user_status` |

响应示例：

```json
{
  "code": 200,
  "body": [
    {
      "typeCode": "admin_user_role",
      "options": [
        { "itemCode": "super_admin", "value": "super_admin", "label": "超管", "sort": 10, "color": "red", "extJson": null },
        { "itemCode": "admin", "value": "admin", "label": "管理员", "sort": 20, "color": "processing", "extJson": null }
      ]
    },
    {
      "typeCode": "admin_user_status",
      "options": [
        { "itemCode": "ACTIVE", "value": "ACTIVE", "label": "启用", "sort": 10, "color": "success", "extJson": null },
        { "itemCode": "INACTIVE", "value": "INACTIVE", "label": "禁用", "sort": 20, "color": "default", "extJson": null }
      ]
    }
  ],
  "message": "OK",
  "success": true
}
```

说明：
- 单次批量查询最多支持 20 个 `typeCode`。
- 仅返回状态为 `ENABLED` 的类型和字典项。

### 3.3 分页查询字典类型

- 地址：`/api/admin/dicts/types/page`
- 方法：`GET`

Query 参数：

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页条数，默认 10 |
| keyword | string | 否 | 关键字，匹配 `typeCode/typeName` |
| status | string | 否 | `ENABLED` / `DISABLED` |

### 3.4 查询字典类型详情

- 地址：`/api/admin/dicts/types/{id}`
- 方法：`GET`

### 3.5 创建字典类型

- 地址：`/api/admin/dicts/types`
- 方法：`POST`

请求示例：

```json
{
  "typeCode": "order_status",
  "typeName": "订单状态",
  "status": "ENABLED",
  "description": "订单流程状态字典"
}
```

### 3.6 更新字典类型

- 地址：`/api/admin/dicts/types/{id}`
- 方法：`POST`

请求示例：

```json
{
  "typeName": "订单状态",
  "status": "ENABLED",
  "description": "订单流程状态字典"
}
```

### 3.7 更新字典类型状态

- 地址：`/api/admin/dicts/types/{id}/status`
- 方法：`POST`

请求示例：

```json
{
  "status": "DISABLED"
}
```

### 3.8 分页查询字典项

- 地址：`/api/admin/dicts/items/page`
- 方法：`GET`

Query 参数：

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页条数，默认 10 |
| typeCode | string | 否 | 按字典类型过滤 |
| keyword | string | 否 | 关键字，匹配 `itemCode/itemLabel/itemValue` |
| status | string | 否 | `ENABLED` / `DISABLED` |

### 3.9 查询字典项详情

- 地址：`/api/admin/dicts/items/{id}`
- 方法：`GET`

### 3.10 创建字典项

- 地址：`/api/admin/dicts/items`
- 方法：`POST`

请求示例：

```json
{
  "typeCode": "order_status",
  "itemCode": "PAID",
  "itemLabel": "已支付",
  "itemValue": "PAID",
  "sort": 20,
  "color": "success",
  "extJson": "{\"editable\":false}",
  "status": "ENABLED"
}
```

### 3.11 更新字典项

- 地址：`/api/admin/dicts/items/{id}`
- 方法：`POST`

请求示例：

```json
{
  "itemLabel": "已支付",
  "itemValue": "PAID",
  "sort": 20,
  "color": "success",
  "extJson": "{\"editable\":false}",
  "status": "ENABLED"
}
```

### 3.12 更新字典项状态

- 地址：`/api/admin/dicts/items/{id}/status`
- 方法：`POST`

请求示例：

```json
{
  "status": "DISABLED"
}
```

### 3.13 更新字典项排序

- 地址：`/api/admin/dicts/items/{id}/sort`
- 方法：`POST`

请求示例：

```json
{
  "sort": 30
}
```

### 3.14 删除字典类型

- 地址：`/api/admin/dicts/types/{id}/delete`
- 方法：`POST`

说明：
- 若该类型下仍存在字典项，将返回 `409`，提示先删除字典项。

### 3.15 删除字典项

- 地址：`/api/admin/dicts/items/{id}/delete`
- 方法：`POST`

## 4. 错误码说明

| code | 含义 | 示例场景 |
|---|---|---|
| 400 | 参数错误 | `typeCode` 格式不合法、`sort` 非整数 |
| 401 | 未登录 | 请求头缺失或 token 失效 |
| 403 | 无权限 | 非管理员访问 `/api/admin/dicts/*` |
| 404 | 资源不存在 | 字典类型/字典项 ID 不存在 |
| 409 | 资源冲突 | 类型编码或字典项编码重复；删除类型时仍存在字典项 |
| 500 | 服务内部错误 | 未预期异常 |
