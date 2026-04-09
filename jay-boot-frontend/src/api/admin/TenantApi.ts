import { get, post } from './index'

export type TenantMemberStatus = 'active' | 'pending' | 'disabled'
export type TenantIsolationStatus = 'pass' | 'risk'

export interface TenantCurrent {
  tenantId: string
  name: string
  planCode: 'FREE' | 'PRO_MONTHLY' | 'TEAM_MONTHLY' | 'TEAM_YEARLY'
  planName: string
  region: string
  ownerUserId: number
  ownerEmail: string
  memberCount: number
  memberLimit: number
  storageUsagePercent: number
  isolationPolicy: string
  isolationStatus: TenantIsolationStatus
}

export interface TenantMember {
  id: number
  name: string
  email: string
  role: string
  lastActiveText: string
  status: TenantMemberStatus
}

export interface TenantOverviewResponse {
  tenant: TenantCurrent
  members: TenantMember[]
  updatedAt: string
}

interface UpdateTenantNamePayload extends Record<string, unknown> {
  name: string
}

/**
 * 获取租户总览
 * 功能描述：获取当前工作区基础信息与成员列表
 * 入参：无
 * 返回参数：租户信息、成员列表、更新时间
 * url地址：/api/admin/tenant/overview
 * 请求方式：GET
 */
export const getTenantOverviewApi = () => get<TenantOverviewResponse>('/api/admin/tenant/overview')

/**
 * 更新当前租户名称
 * 功能描述：修改当前工作区名称
 * 入参：name 工作区名称
 * 返回参数：更新后的租户信息
 * url地址：/api/admin/tenant/current/update-name
 * 请求方式：POST
 */
export const updateCurrentTenantNameApi = (payload: UpdateTenantNamePayload) =>
  post<TenantCurrent>('/api/admin/tenant/current/update-name', payload)
