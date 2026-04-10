## 认证接口说明（用户端）

> 约束更新（2026-04-10）：
> - 注册不需要租户信息
> - 登录仅支持邮箱 + 密码
> - 获取当前用户信息不返回任何租户字段

## 用户注册

**接口地址：** `/api/user/auth/register`  
**请求方式：** `POST`

### 请求参数

```json
{
  "username": "creator01",
  "email": "creator01@example.com",
  "password": "creator123"
}
```

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 用户名，3-24 位 |
| email | string | 是 | 邮箱 |
| password | string | 是 | 密码，至少 6 位 |

### 响应示例

```json
{
  "code": 200,
  "body": {
    "token": "user-mock-token-2-1744171200000",
    "user": {
      "id": 2,
      "username": "creator01",
      "email": "creator01@example.com",
      "createdAt": "2026-04-09T08:00:00.000Z"
    }
  },
  "message": "注册成功",
  "success": true
}
```

## 用户登录

**接口地址：** `/api/user/auth/login`  
**请求方式：** `POST`

### 请求参数

```json
{
  "email": "creator01@example.com",
  "password": "creator123"
}
```

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| email | string | 是 | 邮箱（唯一登录标识） |
| password | string | 是 | 登录密码 |

## 获取当前用户

**接口地址：** `/api/user/auth/me`  
**请求方式：** `GET`

### 请求参数

```json
{
  "token": "user-mock-token-2-1744171205000"
}
```

### 响应示例

```json
{
  "code": 200,
  "body": {
    "id": 2,
    "username": "creator01",
    "email": "creator01@example.com",
    "createdAt": "2026-04-09T08:00:00.000Z"
  },
  "message": "获取当前用户成功",
  "success": true
}
```

## 用户登出

**接口地址：** `/api/user/auth/logout`  
**请求方式：** `POST`

### 请求参数

```json
{
  "token": "user-mock-token-2-1744171205000"
}
```

### 响应示例

```json
{
  "code": 200,
  "body": null,
  "message": "退出成功",
  "success": true
}
```