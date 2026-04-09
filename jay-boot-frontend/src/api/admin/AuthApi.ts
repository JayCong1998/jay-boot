import { get, post } from './index'

export interface AuthUser {
  id: number
  username: string
  email: string
  tenantName: string
  createdAt: string
}

export interface AuthSession {
  token: string
  user: AuthUser
}

export interface LoginPayload extends Record<string, unknown> {
  account: string
  password: string
}

export interface RegisterPayload extends Record<string, unknown> {
  username: string
  email: string
  password: string
  tenantName: string
}

/**
 * 用户注册
 * 功能描述：完成账号注册并返回登录态
 * 入参：用户名、邮箱、密码、租户名称
 * 返回参数：token 与用户信息
 * url地址：/api/admin/auth/register
 * 请求方式：POST
 */
export const registerApi = (payload: RegisterPayload) =>
  post<AuthSession>('/api/admin/auth/register', payload)

/**
 * 用户登录
 * 功能描述：完成账号认证并返回登录态
 * 入参：账号（用户名/邮箱）、密码
 * 返回参数：token 与用户信息
 * url地址：/api/admin/auth/login
 * 请求方式：POST
 */
export const loginApi = (payload: LoginPayload) =>
  post<AuthSession>('/api/admin/auth/login', payload)

/**
 * 获取当前用户
 * 功能描述：根据 token 获取当前登录用户信息
 * 入参：token
 * 返回参数：当前用户信息
 * url地址：/api/admin/auth/me
 * 请求方式：GET
 */
export const meApi = (token: string) => get<AuthUser>('/api/admin/auth/me', { token })

/**
 * 退出登录
 * 功能描述：清理服务端登录态
 * 入参：token
 * 返回参数：空
 * url地址：/api/admin/auth/logout
 * 请求方式：POST
 */
export const logoutApi = (token: string) => post<null>('/api/admin/auth/logout', { token })
