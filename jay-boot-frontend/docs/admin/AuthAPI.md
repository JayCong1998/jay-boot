# 管理员认证接口文档

## 1. 概述

- 登录：`POST /api/admin/auth/login`
- 注册：`POST /api/admin/auth/register`
- 当前会话：`GET /api/admin/auth/session`
- 登出：`POST /api/admin/auth/logout`

统一响应格式：

```json
{
  "code": 200,
  "body": {},
  "message": "OK",
  "success": true
}
```

## 2. 请求参数

### 2.1 登录

- 接口地址：`/api/admin/auth/login`
- 请求方法：`POST`

```json
{
  "email": "admin@example.com",
  "password": "Password123"
}
```

### 2.2 注册

- 接口地址：`/api/admin/auth/register`
- 请求方法：`POST`

```json
{
  "email": "admin@example.com",
  "password": "Password123"
}
```

说明：

- 前端注册表单不再需要 `username` 字段。
- 如果前端发送了 `username`，真实模式将忽略它，只提交邮箱和密码。

### 2.3 获取当前会话

- 接口地址：`/api/admin/auth/session`
- 请求方法：`GET`
- 请求头：`satoken: <token>`

### 2.4 登出

- 接口地址：`/api/admin/auth/logout`
- 请求方法：`POST`
- 请求头：`satoken: <token>`

## 3. 响应示例

### 3.1 登录/注册响应

```json
{
  "code": 200,
  "body": {
    "token": "xxx",
    "tokenTimeout": 2592000,
    "user": {
      "id": "1001",
      "email": "admin@example.com",
      "status": "ACTIVE"
    }
  },
  "message": "OK",
  "success": true
}
```

### 3.2 会话响应

```json
{
  "code": 200,
  "body": {
    "loginId": "1001",
    "token": "xxx",
    "tokenTimeout": 2592000,
    "user": {
      "id": "1001",
      "email": "admin@example.com",
      "status": "ACTIVE"
    }
  },
  "message": "OK",
  "success": true
}
```

## 4. 前端模式配置

`src/api/admin/AuthApi.ts` 支持 `real`（真实）和 `mock`（模拟）两种模式：

- `real`：调用后端真实接口（默认）
- `mock`：使用 `src/api/admin/mockManager.ts`

环境变量配置：

```bash
VITE_ADMIN_AUTH_API_MODE=real
VITE_ADMIN_AUTH_API_BASE_URL=http://127.0.0.1:8080
VITE_ADMIN_AUTH_MOCK_DELAY_MS=180
```