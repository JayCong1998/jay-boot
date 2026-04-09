import { get, post } from './index'

export type ApiKeyStatus = 'active' | 'pending_disable' | 'disabled'

export interface ApiKeyItem {
  id: string
  name: string
  keyPrefix: string
  scope: string
  limitPolicy: string
  lastUsedText: string
  status: ApiKeyStatus
}

export interface ApiKeyQuotaItem {
  id: string
  label: string
  percent: number
}

export interface ApiKeyOverviewResponse {
  keys: ApiKeyItem[]
  scopeOptions: string[]
  limitOptions: string[]
  quotaReminders: ApiKeyQuotaItem[]
  securityTips: string[]
  latestSecret: string
  updatedAt: string
}

interface CreateApiKeyPayload extends Record<string, unknown> {
  name: string
  scope: string
  limitPolicy: string
}

interface RotateApiKeyPayload extends Record<string, unknown> {
  id: string
}

interface UpdateApiKeyStatusPayload extends Record<string, unknown> {
  id: string
  targetStatus: ApiKeyStatus
}

/**
 * 获取 API Key 总览
 * 功能描述：获取 Key 列表、创建表单选项、配额提醒和安全建议
 * 入参：无
 * 返回参数：API Key 总览数据
 * url地址：/api/admin/apikey/overview
 * 请求方式：GET
 */
export const getApiKeyOverviewApi = () => get<ApiKeyOverviewResponse>('/api/admin/apikey/overview')

/**
 * 创建 API Key
 * 功能描述：创建新密钥并返回最新总览，latestSecret 仅用于当前响应展示
 * 入参：name 名称、scope 权限范围、limitPolicy 限流策略
 * 返回参数：API Key 总览数据
 * url地址：/api/admin/apikey/create
 * 请求方式：POST
 */
export const createApiKeyApi = (payload: CreateApiKeyPayload) =>
  post<ApiKeyOverviewResponse>('/api/admin/apikey/create', payload)

/**
 * 轮换 API Key
 * 功能描述：轮换指定密钥并返回最新总览，latestSecret 仅用于当前响应展示
 * 入参：id 密钥 ID
 * 返回参数：API Key 总览数据
 * url地址：/api/admin/apikey/rotate
 * 请求方式：POST
 */
export const rotateApiKeyApi = (payload: RotateApiKeyPayload) =>
  post<ApiKeyOverviewResponse>('/api/admin/apikey/rotate', payload)

/**
 * 更新 API Key 状态
 * 功能描述：启用、待禁用或禁用指定密钥，并返回最新总览
 * 入参：id 密钥 ID、targetStatus 目标状态
 * 返回参数：API Key 总览数据
 * url地址：/api/admin/apikey/update-status
 * 请求方式：POST
 */
export const updateApiKeyStatusApi = (payload: UpdateApiKeyStatusPayload) =>
  post<ApiKeyOverviewResponse>('/api/admin/apikey/update-status', payload)
