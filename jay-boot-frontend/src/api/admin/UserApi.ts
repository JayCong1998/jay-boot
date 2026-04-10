import { requestAdminAuthRealApi } from './realApi'

export type AdminUserRole = 'super_admin' | 'admin' | 'user'
export type AdminUserStatus = 'ACTIVE' | 'INACTIVE'

export interface AdminUserItem {
  id: string
  username: string
  email: string
  role: AdminUserRole
  status: AdminUserStatus
  createdTime: string
}

export interface AdminUserPageResponse {
  records: AdminUserItem[]
  total: number
  page: number
  pageSize: number
}

export interface AdminUserPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  role?: AdminUserRole
  status?: AdminUserStatus
}

export interface AdminUserCreatePayload extends Record<string, unknown> {
  username: string
  email: string
  role: AdminUserRole
  password: string
  status?: AdminUserStatus
}

export interface AdminUserUpdatePayload extends Record<string, unknown> {
  username: string
  email: string
  role: AdminUserRole
  status: AdminUserStatus
}

interface AdminUserStatusPayload extends Record<string, unknown> {
  status: AdminUserStatus
}

interface AdminUserPasswordResetPayload extends Record<string, unknown> {
  newPassword: string
}

/**
 * 获取用户分页列表
 * 功能描述：按分页与筛选条件查询管理端用户列表
 * 入参：token、分页参数、筛选参数
 * 返回参数：分页结果
 * url地址：/api/admin/users
 * 请求方式：GET
 */
export const getAdminUserPageApi = (token: string, params: AdminUserPageParams) =>
  requestAdminAuthRealApi<AdminUserPageResponse>('GET', '/api/admin/users', { token, payload: params })

/**
 * 创建用户
 * 功能描述：创建一个新的管理端用户
 * 入参：token、用户信息
 * 返回参数：空
 * url地址：/api/admin/users
 * 请求方式：POST
 */
export const createAdminUserApi = (token: string, payload: AdminUserCreatePayload) =>
  requestAdminAuthRealApi<null>('POST', '/api/admin/users', { token, payload })

/**
 * 更新用户
 * 功能描述：更新指定用户基础资料
 * 入参：token、用户ID、更新内容
 * 返回参数：空
 * url地址：/api/admin/users/{id}
 * 请求方式：PUT
 */
export const updateAdminUserApi = (token: string, id: string, payload: AdminUserUpdatePayload) =>
  requestAdminAuthRealApi<null>('POST', `/api/admin/users/${id}`, { token, payload })

/**
 * 更新用户状态
 * 功能描述：启用或禁用指定用户
 * 入参：token、用户ID、目标状态
 * 返回参数：空
 * url地址：/api/admin/users/{id}/status
 * 请求方式：POST
 */
export const updateAdminUserStatusApi = (token: string, id: string, status: AdminUserStatus) =>
  requestAdminAuthRealApi<null>('POST', `/api/admin/users/${id}/status`, { token, payload: { status } as AdminUserStatusPayload })

/**
 * 重置用户密码
 * 功能描述：管理员设置指定用户的新密码
 * 入参：token、用户ID、新密码
 * 返回参数：空
 * url地址：/api/admin/users/{id}/password/reset
 * 请求方式：POST
 */
export const resetAdminUserPasswordApi = (token: string, id: string, newPassword: string) =>
  requestAdminAuthRealApi<null>('POST', `/api/admin/users/${id}/password/reset`, {
    token,
    payload: { newPassword } as AdminUserPasswordResetPayload,
  })
