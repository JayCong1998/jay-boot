import { get, post } from '../index'

export interface UserAuthUser {
  id: string
  username: string
  email: string
  createdAt: string
}

export interface UserAuthSession {
  token: string
  user: UserAuthUser
}

export interface UserLoginPayload extends Record<string, unknown> {
  email: string
  password: string
}

export interface UserRegisterPayload extends Record<string, unknown> {
  username: string
  email: string
  password: string
}

/**
 * 用户注册
 * 功能描述：完成用户端账号注册并返回登录态
 * 入参：用户名、邮箱、密码
 * 返回参数：token 与用户信息
 * url地址：/api/user/auth/register
 * 请求方式：POST
 */
export const userRegisterApi = (payload: UserRegisterPayload) =>
  post<UserAuthSession>('/api/user/auth/register', payload)

/**
 * 用户登录
 * 功能描述：完成用户端账号认证并返回登录态
 * 入参：邮箱、密码
 * 返回参数：token 与用户信息
 * url地址：/api/user/auth/login
 * 请求方式：POST
 */
export const userLoginApi = (payload: UserLoginPayload) =>
  post<UserAuthSession>('/api/user/auth/login', payload)

/**
 * 获取当前用户
 * 功能描述：根据 token 获取当前登录用户信息
 * 入参：token
 * 返回参数：当前用户信息
 * url地址：/api/user/auth/me
 * 请求方式：GET
 */
export const userMeApi = (token: string) => get<UserAuthUser>('/api/user/auth/me', undefined, token)

/**
 * 退出登录
 * 功能描述：清理用户端登录状态
 * 入参：token
 * 返回参数：空
 * url地址：/api/user/auth/logout
 * 请求方式：POST
 */
export const userLogoutApi = (token: string) => post<null>('/api/user/auth/logout', undefined, token)
