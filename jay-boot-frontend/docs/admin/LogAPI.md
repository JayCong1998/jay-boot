# 日志管理接口文档

## 1. 接口概览

### 1.1 操作日志

- 分页查询：`GET /api/admin/logs/operations/page`
- 查询详情：`GET /api/admin/logs/operations/{id}`

### 1.2 请求日志

- 分页查询：`GET /api/admin/logs/requests/page`
- 查询详情：`GET /api/admin/logs/requests/{id}`
- 删除单条：`POST /api/admin/logs/requests/{id}/delete`
- 批量删除：`POST /api/admin/logs/requests/batch-delete`

### 1.3 异常日志

- 分页查询：`GET /api/admin/logs/errors/page`
- 查询详情：`GET /api/admin/logs/errors/{id}`
- 删除单条：`POST /api/admin/logs/errors/{id}/delete`
- 批量删除：`POST /api/admin/logs/errors/batch-delete`

## 2. 鉴权

所有接口都需要请求头：

```text
satoken: <token>
```

## 3. 关键说明

- 分页查询路径统一为`/page`，例如：`/api/admin/logs/requests/page`。
- 前端“详情”按钮必须调用对应详情接口（`/{id}`），不能直接使用列表行数据作为详情数据源。
- 写操作统一使用`POST`。

## 4. 分页参数

各分页接口共用以下查询参数：

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页条数，默认 20 |
| keyword | string | 否 | 关键字（按接口语义匹配） |
| startTime | string | 否 | 开始时间，格式：`YYYY-MM-DD HH:mm:ss` |
| endTime | string | 否 | 结束时间，格式：`YYYY-MM-DD HH:mm:ss` |

不同接口还支持专有筛选字段：

- 操作日志：`module`、`userId`
- 请求日志：`method`、`statusCode`、`userId`
- 异常日志：`requestPath`、`userId`

## 5. 统一响应结构

```json
{
  "code": 200,
  "body": {
    "records": [],
    "total": 0,
    "page": 1,
    "pageSize": 20
  },
  "message": "OK",
  "success": true
}
```