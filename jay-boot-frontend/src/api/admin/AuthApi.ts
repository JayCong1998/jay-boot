import { adminAuthApiConfig } from '../../config/api'
import { get, post } from './index'
import { requestAdminAuthRealApi } from './realApi'

interface RealAuthUser {
  id: number
  email: string
  status: string
}

interface RealAuthTokenResponse {
  token: string
  tokenTimeout: number
  user: RealAuthUser
}

interface RealAuthSessionResponse {
  loginId: number
  token: string
  tokenTimeout: number
  user: RealAuthUser
}

export interface AuthUser {
  id: number
  email: string
  status?: string
  username?: string
  createdAt?: string
}

export interface AuthSession {
  token: string
  tokenTimeout?: number
  user: AuthUser
}

export interface LoginPayload extends Record<string, unknown> {
  email: string
  password: string
}

export interface RegisterPayload extends Record<string, unknown> {
  email: string
  password: string
  username?: string
}

const resolveDisplayName = (email: string, username?: string) => {
  const normalizedName = typeof username === 'string' ? username.trim() : ''
  if (normalizedName) {
    return normalizedName
  }
  const [prefix] = email.split('@')
  return prefix || email
}

const mapRealUser = (user: RealAuthUser): AuthUser => ({
  id: user.id,
  email: user.email,
  status: user.status,
  username: resolveDisplayName(user.email),
})

const mapRealSession = (response: RealAuthTokenResponse): AuthSession => ({
  token: response.token,
  tokenTimeout: response.tokenTimeout,
  user: mapRealUser(response.user),
})

/**
 * 用户注册
 * 功能描述：完成账号注册并返回登录态信息
 * 入参：邮箱、密码（real 模式下会忽略 username）
 * 返回参数：token 与用户信息
 * url地址：/api/admin/auth/register
 * 请求方式：POST
 */
export const registerApi = async (payload: RegisterPayload): Promise<AuthSession> => {
  if (adminAuthApiConfig.mode === 'real') {
    const result = await requestAdminAuthRealApi<RealAuthTokenResponse>('POST', '/api/admin/auth/register', {
      payload: {
        email: String(payload.email ?? '').trim(),
        password: String(payload.password ?? ''),
      },
    })
    return mapRealSession(result)
  }

  return post<AuthSession>('/api/admin/auth/register', payload)
}

/**
 * 用户登录
 * 功能描述：完成账号认证并返回登录态信息
 * 入参：邮箱、密码
 * 返回参数：token 与用户信息
 * url地址：/api/admin/auth/login
 * 请求方式：POST
 */
export const loginApi = async (payload: LoginPayload): Promise<AuthSession> => {
  if (adminAuthApiConfig.mode === 'real') {
    const result = await requestAdminAuthRealApi<RealAuthTokenResponse>('POST', '/api/admin/auth/login', {
      payload: {
        email: String(payload.email ?? '').trim(),
        password: String(payload.password ?? ''),
      },
    })
    return mapRealSession(result)
  }

  return post<AuthSession>('/api/admin/auth/login', payload)
}

/**
 * 获取当前用户
 * 功能描述：根据 token 获取当前登录用户信息
 * 入参：token
 * 返回参数：当前用户信息
 * url地址：real=/api/admin/auth/session, mock=/api/admin/auth/me
 * 请求方式：GET
 */
export const meApi = async (token: string): Promise<AuthUser> => {
  if (adminAuthApiConfig.mode === 'real') {
    const result = await requestAdminAuthRealApi<RealAuthSessionResponse>('GET', '/api/admin/auth/session', {
      token,
    })
    return mapRealUser(result.user)
  }

  return get<AuthUser>('/api/admin/auth/me', { token })
}

/**
 * 退出登录
 * 功能描述：清理登录态
 * 入参：token
 * 返回参数：空
 * url地址：/api/admin/auth/logout
 * 请求方式：POST
 */
export const logoutApi = async (token: string): Promise<null> => {
  if (adminAuthApiConfig.mode === 'real') {
    return requestAdminAuthRealApi<null>('POST', '/api/admin/auth/logout', { token })
  }

  return post<null>('/api/admin/auth/logout', { token })
}
