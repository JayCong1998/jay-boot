## 用户注册

**接口名称：** 用户注册  
**功能描述：** 在用户端创建账号并返回登录会话信息。  
**接口地址：** `/api/user/auth/register`  
**请求方式：** `POST`

### 功能说明

用于用户端注册入口，注册成功后前端持久化会话并跳转到新手引导页。

### 请求参数

```json
{
  "username": "creator01",
  "email": "creator01@example.com",
  "password": "creator123",
  "tenantName": "Acme 内容团队"
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| username | string | 是 | 用户名，3-24 位 | creator01 |
| email | string | 是 | 邮箱 | creator01@example.com |
| password | string | 是 | 登录密码，至少 6 位 | creator123 |
| tenantName | string | 是 | 工作区名称 | Acme 内容团队 |

### 响应参数

```json
{
  "error": 0,
  "body": {
    "token": "user-mock-token-2-1744171200000",
    "user": {
      "id": 2,
      "username": "creator01",
      "email": "creator01@example.com",
      "tenantName": "Acme 内容团队",
      "createdAt": "2026-04-09T08:00:00.000Z"
    }
  },
  "message": "注册成功",
  "success": true
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| error | number | 是 | 错误码，0 为成功 | 0 |
| body | object | 是 | 业务数据 | - |
| body.token | string | 是 | 会话令牌 | user-mock-token-2-1744171200000 |
| body.user | object | 是 | 当前用户信息 | - |
| body.user.id | number | 是 | 用户 ID | 2 |
| body.user.username | string | 是 | 用户名 | creator01 |
| body.user.email | string | 是 | 邮箱 | creator01@example.com |
| body.user.tenantName | string | 是 | 工作区名称 | Acme 内容团队 |
| body.user.createdAt | string | 是 | 创建时间（ISO） | 2026-04-09T08:00:00.000Z |
| message | string | 是 | 响应消息 | 注册成功 |
| success | boolean | 是 | 是否成功 | true |

---

## 用户登录

**接口名称：** 用户登录  
**功能描述：** 用户端账号密码登录并返回会话信息。  
**接口地址：** `/api/user/auth/login`  
**请求方式：** `POST`

### 请求参数

```json
{
  "account": "creator01",
  "password": "creator123"
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| account | string | 是 | 用户名或邮箱 | creator01 |
| password | string | 是 | 登录密码 | creator123 |

### 响应参数

```json
{
  "error": 0,
  "body": {
    "token": "user-mock-token-2-1744171205000",
    "user": {
      "id": 2,
      "username": "creator01",
      "email": "creator01@example.com",
      "tenantName": "Acme 内容团队",
      "createdAt": "2026-04-09T08:00:00.000Z"
    }
  },
  "message": "登录成功",
  "success": true
}
```

---

## 获取当前用户

**接口名称：** 获取当前用户  
**功能描述：** 根据 token 获取当前登录用户信息。  
**接口地址：** `/api/user/auth/me`  
**请求方式：** `GET`

### 请求参数

```json
{
  "token": "user-mock-token-2-1744171205000"
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| token | string | 是 | 登录令牌 | user-mock-token-2-1744171205000 |

### 响应参数

```json
{
  "error": 0,
  "body": {
    "id": 2,
    "username": "creator01",
    "email": "creator01@example.com",
    "tenantName": "Acme 内容团队",
    "createdAt": "2026-04-09T08:00:00.000Z"
  },
  "message": "获取当前用户成功",
  "success": true
}
```

---

## 用户退出登录

**接口名称：** 用户退出登录  
**功能描述：** 清理用户会话状态。  
**接口地址：** `/api/user/auth/logout`  
**请求方式：** `POST`

### 请求参数

```json
{
  "token": "user-mock-token-2-1744171205000"
}
```

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
| --- | --- | --- | --- | --- |
| token | string | 是 | 登录令牌 | user-mock-token-2-1744171205000 |

### 响应参数

```json
{
  "error": 0,
  "body": null,
  "message": "退出成功",
  "success": true
}
```

---

## 错误响应约定

```json
{
  "error": 1,
  "body": null,
  "message": "错误描述",
  "success": false
}
```

说明：

- `error`：错误码，非 0 表示失败
- `message`：可直接用于前端提示
- `success`：`false` 表示请求失败

---

## Mock 与真实接口切换

通过环境变量切换：

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
