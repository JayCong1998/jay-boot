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
 * 入参：分页参数、筛选参数
 * 返回参数：分页结果
 * url地址：/api/admin/plans/page
 * 请求方式：GET
 */
export const getAdminPlanPageApi = (params: AdminPlanPageParams) =>
  get<AdminPlanPageResponse>('/api/admin/plans/page', params)

/**
 * 获取套餐详情
 * 功能描述：根据套餐ID查询最新套餐信息
 * 入参：套餐ID
 * 返回参数：套餐详情
 * url地址：/api/admin/plans/{id}
 * 请求方式：GET
 */
export const getAdminPlanDetailApi = (id: string) =>
  get<AdminPlanItem>(`/api/admin/plans/${id}`)

/**
 * 创建套餐
 * 功能描述：创建新的套餐定义
 * 入参：套餐创建参数
 * 返回参数：空
 * url地址：/api/admin/plans
 * 请求方式：POST
 */
export const createAdminPlanApi = (payload: AdminPlanCreatePayload) =>
  post<null>('/api/admin/plans', payload)

/**
 * 更新套餐
 * 功能描述：更新指定套餐信息
 * 入参：套餐ID、更新参数
 * 返回参数：空
 * url地址：/api/admin/plans/{id}
 * 请求方式：POST
 */
export const updateAdminPlanApi = (id: string, payload: AdminPlanUpdatePayload) =>
  post<null>(`/api/admin/plans/${id}`, payload)

/**
 * 更新套餐状态
 * 功能描述：启用或停用指定套餐
 * 入参：套餐ID、目标状态
 * 返回参数：空
 * url地址：/api/admin/plans/{id}/status
 * 请求方式：POST
 */
export const updateAdminPlanStatusApi = (id: string, status: PlanStatus) =>
  post<null>(`/api/admin/plans/${id}/status`, { status } as AdminPlanStatusPayload)

