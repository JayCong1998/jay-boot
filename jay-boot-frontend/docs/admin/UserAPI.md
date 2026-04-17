# 用户管理接口文档

## 1. 接口概览

- 分页查询用户：`GET /api/admin/users/page`
- 创建用户：`POST /api/admin/users`
- 更新用户：`POST /api/admin/users/{id}`
- 更新用户状态：`POST /api/admin/users/{id}/status`
- 重置用户密码：`POST /api/admin/users/{id}/password/reset`

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

所有接口都需要请求头：

```text
satoken: <token>
```

## 3. 接口详情

### 3.1 分页查询用户

- 地址：`/api/admin/users/page`
- 方法：`GET`

Query 参数：

| 参数名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页条数，默认 10 |
| keyword | string | 否 | 关键字，匹配用户名/邮箱 |
| role | string | 否 | 角色：`super_admin` / `admin` / `user` |
| status | string | 否 | 状态：`ACTIVE` / `INACTIVE` |

### 3.2 创建用户

- 地址：`/api/admin/users`
- 方法：`POST`

请求示例：

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "role": "admin",
  "password": "Password123",
  "status": "ACTIVE"
}
```

### 3.3 更新用户

- 地址：`/api/admin/users/{id}`
- 方法：`POST`

请求示例：

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "role": "admin",
  "status": "ACTIVE"
}
```

### 3.4 更新用户状态

- 地址：`/api/admin/users/{id}/status`
- 方法：`POST`

请求示例：

```json
{
  "status": "INACTIVE"
}
```

### 3.5 重置用户密码

- 地址：`/api/admin/users/{id}/password/reset`
- 方法：`POST`

请求示例：

```json
{
  "newPassword": "NewPassword123"
}
```