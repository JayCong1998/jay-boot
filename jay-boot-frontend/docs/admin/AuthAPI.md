# Admin Auth API

## 1. 接口概览

- 登录：`POST /api/admin/auth/login`
- 注册：`POST /api/admin/auth/register`
- 当前会话：`GET /api/admin/auth/session`
- 退出登录：`POST /api/admin/auth/logout`

统一返回结构：

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
- 请求方式：`POST`

```json
{
  "email": "admin@example.com",
  "password": "Password123"
}
```

### 2.2 注册

- 接口地址：`/api/admin/auth/register`
- 请求方式：`POST`

```json
{
  "email": "admin@example.com",
  "password": "Password123"
}
```

说明：

- 前端注册页不再要求用户名。
- 如前端调用时传入 `username`，real 模式下会自动忽略该字段，仅提交邮箱和密码。

### 2.3 获取当前会话

- 接口地址：`/api/admin/auth/session`
- 请求方式：`GET`
- 请求头：`satoken: <token>`

### 2.4 退出登录

- 接口地址：`/api/admin/auth/logout`
- 请求方式：`POST`
- 请求头：`satoken: <token>`

## 3. 返回参数说明

### 3.1 登录/注册返回

```json
{
  "code": 200,
  "body": {
    "token": "xxx",
    "tokenTimeout": 2592000,
    "user": {
      "id": 1001,
      "email": "admin@example.com",
      "status": "ACTIVE"
    }
  },
  "message": "OK",
  "success": true
}
```

### 3.2 会话返回

```json
{
  "code": 200,
  "body": {
    "loginId": 1001,
    "token": "xxx",
    "tokenTimeout": 2592000,
    "user": {
      "id": 1001,
      "email": "admin@example.com",
      "status": "ACTIVE"
    }
  },
  "message": "OK",
  "success": true
}
```

## 4. 前端模式切换配置

`src/api/admin/AuthApi.ts` 支持 `real/mock` 两种模式：

- `real`：调用后端真实接口（本次默认）
- `mock`：走 `src/api/admin/mockManager.ts`

环境变量：

```bash
VITE_ADMIN_AUTH_API_MODE=real
VITE_ADMIN_AUTH_API_BASE_URL=http://127.0.0.1:8080
VITE_ADMIN_AUTH_MOCK_DELAY_MS=180
```
