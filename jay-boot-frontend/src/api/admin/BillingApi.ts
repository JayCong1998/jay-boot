import { get, post } from './index'

export type BillingPlanCode = 'FREE' | 'PRO_MONTHLY' | 'TEAM_MONTHLY'
export type BillingSubscriptionStatus = 'trialing' | 'active' | 'past_due' | 'canceled'
export type BillingCallbackStatus = 'healthy' | 'degraded'
export type BillingOrderStatus = 'paid' | 'pending' | 'failed' | 'refunded'
export type BillingInvoiceStatus = 'generated' | 'sent' | 'none'
export type BillingInvoiceAction = 'download' | 'send'

export interface BillingPlanCard {
  code: BillingPlanCode
  name: string
  priceText: string
  billingCycleText: string
  featureList: string[]
  contactSales: boolean
  highlighted: boolean
}

export interface BillingCurrentSubscription {
  planCode: BillingPlanCode
  planName: string
  status: BillingSubscriptionStatus
  renewAt: string
  trialEndAt: string
  amountText: string
}

export interface BillingStatusStage {
  key: BillingSubscriptionStatus
  title: string
  description: string
}

export interface BillingCallbackHealthItem {
  id: string
  provider: string
  successRate: number
  status: BillingCallbackStatus
  lastCheckedAt: string
}

export interface BillingOrderItem {
  id: string
  orderNo: string
  period: string
  amountText: string
  channel: string
  status: BillingOrderStatus
  invoiceStatus: BillingInvoiceStatus
}

export interface BillingOverviewResponse {
  plans: BillingPlanCard[]
  currentSubscription: BillingCurrentSubscription
  statusStages: BillingStatusStage[]
  callbackHealth: BillingCallbackHealthItem[]
  orders: BillingOrderItem[]
  updatedAt: string
}

interface ChangeBillingPlanPayload extends Record<string, unknown> {
  planCode: BillingPlanCode
}

interface BillingReconcilePayload extends Record<string, unknown> {
  triggerBy: string
}

interface BillingInvoiceActionPayload extends Record<string, unknown> {
  orderNo: string
  action: BillingInvoiceAction
}

/**
 * 获取订阅计费总览
 * 功能描述：获取套餐卡片、当前订阅、状态机、回调健康和订单列表
 * 入参：无
 * 返回参数：Billing 总览数据
 * url地址：/api/admin/billing/overview
 * 请求方式：GET
 */
export const getBillingOverviewApi = () => get<BillingOverviewResponse>('/api/admin/billing/overview')

/**
 * 切换订阅套餐
 * 功能描述：将当前租户切换为目标套餐并返回最新 Billing 总览
 * 入参：planCode 套餐编码
 * 返回参数：Billing 总览数据
 * url地址：/api/admin/billing/subscription/change-plan
 * 请求方式：POST
 */
export const changeBillingPlanApi = (payload: ChangeBillingPlanPayload) =>
  post<BillingOverviewResponse>('/api/admin/billing/subscription/change-plan', payload)

/**
 * 执行账单对账
 * 功能描述：触发一次 Mock 对账流程并返回最新 Billing 总览
 * 入参：triggerBy 触发来源
 * 返回参数：Billing 总览数据
 * url地址：/api/admin/billing/reconciliation/trigger
 * 请求方式：POST
 */
export const triggerBillingReconcileApi = (payload: BillingReconcilePayload) =>
  post<BillingOverviewResponse>('/api/admin/billing/reconciliation/trigger', payload)

/**
 * 处理发票动作
 * 功能描述：对指定订单执行发票下载或邮件发送并返回最新 Billing 总览
 * 入参：orderNo 订单号、action 发票动作
 * 返回参数：Billing 总览数据
 * url地址：/api/admin/billing/order/invoice-action
 * 请求方式：POST
 */
export const billingInvoiceActionApi = (payload: BillingInvoiceActionPayload) =>
  post<BillingOverviewResponse>('/api/admin/billing/order/invoice-action', payload)
