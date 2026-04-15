import { get, post } from '../index'

export type PlanStatus = 'ACTIVE' | 'INACTIVE'
export type PlanBillingCycle = 'MONTHLY' | 'YEARLY'

export interface AdminPlanItem {
  id: string
  code: string
  name: string
  billingCycle: PlanBillingCycle
  quotaJson: string
  price: number
  status: PlanStatus
  updatedTime: string
}

export interface AdminPlanPageResponse {
  records: AdminPlanItem[]
  total: number
  page: number
  pageSize: number
}

export interface AdminPlanPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  status?: PlanStatus
  billingCycle?: PlanBillingCycle
}

export interface AdminPlanCreatePayload extends Record<string, unknown> {
  code: string
  name: string
  billingCycle: PlanBillingCycle
  quotaJson: string
  price: number
  status: PlanStatus
}

export interface AdminPlanUpdatePayload extends Record<string, unknown> {
  name: string
  billingCycle: PlanBillingCycle
  quotaJson: string
  price: number
  status: PlanStatus
}

interface AdminPlanStatusPayload extends Record<string, unknown> {
  status: PlanStatus
}

/**
 * 获取套餐分页列表
 * 功能描述：按分页和筛选条件查询管理端套餐列表
 * 入参：token、分页参数、筛选参数
 * 返回参数：分页结果
 * url地址：/api/admin/plans
 * 请求方式：GET
 */
export const getAdminPlanPageApi = (token: string, params: AdminPlanPageParams) =>
  get<AdminPlanPageResponse>('/api/admin/plans', params, token)

/**
 * 创建套餐
 * 功能描述：创建新的套餐定义
 * 入参：token、套餐创建参数
 * 返回参数：空
 * url地址：/api/admin/plans
 * 请求方式：POST
 */
export const createAdminPlanApi = (token: string, payload: AdminPlanCreatePayload) =>
  post<null>('/api/admin/plans', payload, token)

/**
 * 更新套餐
 * 功能描述：更新指定套餐信息
 * 入参：token、套餐ID、更新参数
 * 返回参数：空
 * url地址：/api/admin/plans/{id}
 * 请求方式：POST
 */
export const updateAdminPlanApi = (token: string, id: string, payload: AdminPlanUpdatePayload) =>
  post<null>(`/api/admin/plans/${id}`, payload, token)

/**
 * 更新套餐状态
 * 功能描述：启用或停用指定套餐
 * 入参：token、套餐ID、目标状态
 * 返回参数：空
 * url地址：/api/admin/plans/{id}/status
 * 请求方式：POST
 */
export const updateAdminPlanStatusApi = (token: string, id: string, status: PlanStatus) =>
  post<null>(`/api/admin/plans/${id}/status`, { status } as AdminPlanStatusPayload, token)

