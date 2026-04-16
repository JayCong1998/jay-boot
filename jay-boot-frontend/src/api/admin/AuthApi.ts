import { get, post } from '../index'

interface RealAuthUser {
  id: string
  email: string
  status: string
  username?: string
}

interface RealAuthTokenResponse {
  token: string
  tokenTimeout: number
  user: RealAuthUser
}

interface RealAuthSessionResponse {
  loginId: string
  token: string
  tokenTimeout: number
  user: RealAuthUser
}

export interface AuthUser {
  id: string
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
  id: String(user.id),
  email: user.email,
  status: user.status,
  username: resolveDisplayName(user.email, user.username),
})

const mapRealSession = (response: RealAuthTokenResponse): AuthSession => ({
  token: response.token,
  tokenTimeout: response.tokenTimeout,
  user: mapRealUser(response.user),
})

/**
 * 用户注册
 * 功能描述：完成账号注册并返回登录态信息
 * 入参：邮箱、密码
 * 返回参数：token 与用户信息
 * url地址：/api/admin/auth/register
 * 请求方式：POST
 */
export const registerApi = async (payload: RegisterPayload): Promise<AuthSession> => {
  const result = await post<RealAuthTokenResponse>('/api/admin/auth/register', {
    email: String(payload.email ?? '').trim(),
    password: String(payload.password ?? ''),
  })
  return mapRealSession(result)
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
  const result = await post<RealAuthTokenResponse>('/api/admin/auth/login', {
    email: String(payload.email ?? '').trim(),
    password: String(payload.password ?? ''),
  })
  return mapRealSession(result)
}

/**
 * 获取当前用户
 * 功能描述：根据 token 获取当前登录用户信息
 * 入参：无
 * 返回参数：当前用户信息
 * url地址：/api/admin/auth/session
 * 请求方式：GET
 */
export const meApi = async (): Promise<AuthUser> => {
  const result = await get<RealAuthSessionResponse>('/api/admin/auth/session')
  return mapRealUser(result.user)
}

/**
 * 退出登录
 * 功能描述：清理登录态
 * 入参：无
 * 返回参数：空
 * url地址：/api/admin/auth/logout
 * 请求方式：POST
 */
export const logoutApi = async (): Promise<null> => {
  return post<null>('/api/admin/auth/logout')
}
