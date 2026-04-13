# Admin Auth API

## 1. Overview

- Login: `POST /api/admin/auth/login`
- Register: `POST /api/admin/auth/register`
- Current Session: `GET /api/admin/auth/session`
- Logout: `POST /api/admin/auth/logout`

Unified response envelope:

```json
{
  "code": 200,
  "body": {},
  "message": "OK",
  "success": true
}
```

## 2. Request Parameters

### 2.1 Login

- Endpoint: `/api/admin/auth/login`
- Method: `POST`

```json
{
  "email": "admin@example.com",
  "password": "Password123"
}
```

### 2.2 Register

- Endpoint: `/api/admin/auth/register`
- Method: `POST`

```json
{
  "email": "admin@example.com",
  "password": "Password123"
}
```

Notes:

- Frontend register form no longer requires `username`.
- If `username` is sent by frontend, real mode ignores it and only submits email/password.

### 2.3 Get Current Session

- Endpoint: `/api/admin/auth/session`
- Method: `GET`
- Header: `satoken: <token>`

### 2.4 Logout

- Endpoint: `/api/admin/auth/logout`
- Method: `POST`
- Header: `satoken: <token>`

## 3. Response Examples

### 3.1 Login/Register Response

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

### 3.2 Session Response

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

## 4. Frontend Mode Config

`src/api/admin/AuthApi.ts` supports both `real` and `mock`:

- `real`: call backend real API (default)
- `mock`: use `src/api/admin/mockManager.ts`

Environment variables:

```bash
VITE_ADMIN_AUTH_API_MODE=real
VITE_ADMIN_AUTH_API_BASE_URL=http://127.0.0.1:8080
VITE_ADMIN_AUTH_MOCK_DELAY_MS=180
```