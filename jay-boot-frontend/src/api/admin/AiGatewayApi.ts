import { get, post } from './index'

export type AiGatewayProviderStatus = 'online' | 'warning' | 'offline'
export type AiGatewayRouteStatus = 'available' | 'high_load' | 'degraded'
export type AiGatewayBillingUnit = 'token' | 'request'
export type AiGatewayTestStatus = 'success' | 'retrying' | 'failed'

export interface AiGatewayProviderItem {
  id: string
  name: string
  status: AiGatewayProviderStatus
  detail: string
}

export interface AiGatewayRoutePolicyItem {
  id: string
  endpoint: string
  defaultModel: string
  timeoutPolicy: string
  billingUnit: AiGatewayBillingUnit
  status: AiGatewayRouteStatus
}

export interface AiGatewayCostItem {
  id: string
  label: string
  percent: number
  costText: string
}

export interface AiGatewayFallbackStep {
  id: string
  title: string
  detail: string
}

export interface AiGatewayTestResult {
  traceId: string
  endpoint: string
  status: AiGatewayTestStatus
  message: string
  time: string
}

export interface AiGatewayOverviewResponse {
  activeProviderId: string
  providers: AiGatewayProviderItem[]
  routePolicies: AiGatewayRoutePolicyItem[]
  costTrend: AiGatewayCostItem[]
  fallbackSteps: AiGatewayFallbackStep[]
  latestTestResult: AiGatewayTestResult | null
  updatedAt: string
}

interface SwitchAiGatewayProviderPayload extends Record<string, unknown> {
  providerId: string
}

interface SaveAiGatewayRoutePolicyPayload extends Record<string, unknown> {
  routePolicies: AiGatewayRoutePolicyItem[]
}

interface AiGatewayTestCallPayload extends Record<string, unknown> {
  endpoint: string
}

/**
 * 获取 AI Gateway 总览
 * 功能描述：获取 Provider 状态、路由策略、成本趋势和降级路径
 * 入参：无
 * 返回参数：AI Gateway 总览数据
 * url地址：/api/admin/ai-gateway/overview
 * 请求方式：GET
 */
export const getAiGatewayOverviewApi = () => get<AiGatewayOverviewResponse>('/api/admin/ai-gateway/overview')

/**
 * 切换默认 Provider
 * 功能描述：切换默认模型供应商并返回最新总览
 * 入参：providerId 供应商 ID
 * 返回参数：AI Gateway 总览数据
 * url地址：/api/admin/ai-gateway/provider/switch
 * 请求方式：POST
 */
export const switchAiGatewayProviderApi = (payload: SwitchAiGatewayProviderPayload) =>
  post<AiGatewayOverviewResponse>('/api/admin/ai-gateway/provider/switch', payload)

/**
 * 保存路由策略
 * 功能描述：保存统一接口路由策略并返回最新总览
 * 入参：routePolicies 路由策略列表
 * 返回参数：AI Gateway 总览数据
 * url地址：/api/admin/ai-gateway/route-policy/save
 * 请求方式：POST
 */
export const saveAiGatewayRoutePoliciesApi = (payload: SaveAiGatewayRoutePolicyPayload) =>
  post<AiGatewayOverviewResponse>('/api/admin/ai-gateway/route-policy/save', payload)

/**
 * 接口试调用
 * 功能描述：对指定接口执行一次试调用并返回最新总览
 * 入参：endpoint 目标接口
 * 返回参数：AI Gateway 总览数据（含最新试调用结果）
 * url地址：/api/admin/ai-gateway/test-call
 * 请求方式：POST
 */
export const runAiGatewayTestCallApi = (payload: AiGatewayTestCallPayload) =>
  post<AiGatewayOverviewResponse>('/api/admin/ai-gateway/test-call', payload)
