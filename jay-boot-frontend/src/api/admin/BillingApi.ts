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
 * 鑾峰彇璁㈤槄璁¤垂鎬昏
 * 鍔熻兘鎻忚堪锛氳幏鍙栧椁愬崱鐗囥€佸綋鍓嶈闃呫€佺姸鎬佹満銆佸洖璋冨仴搴峰拰璁㈠崟鍒楄〃
 * 鍏ュ弬锛氭棤
 * 杩斿洖鍙傛暟锛欱illing 鎬昏鏁版嵁
 * url鍦板潃锛?api/admin/billing/overview
 * 璇锋眰鏂瑰紡锛欸ET
 */
export const getBillingOverviewApi = () => get<BillingOverviewResponse>('/api/admin/billing/overview')

/**
 * 鍒囨崲璁㈤槄濂楅
 * 鍔熻兘鎻忚堪锛氬皢褰撳墠绉熸埛鍒囨崲涓虹洰鏍囧椁愬苟杩斿洖鏈€鏂?Billing 鎬昏
 * 鍏ュ弬锛歱lanCode 濂楅缂栫爜
 * 杩斿洖鍙傛暟锛欱illing 鎬昏鏁版嵁
 * url鍦板潃锛?api/admin/billing/subscription/change-plan
 * 璇锋眰鏂瑰紡锛歅OST
 */
export const changeBillingPlanApi = (payload: ChangeBillingPlanPayload) =>
  post<BillingOverviewResponse>('/api/admin/billing/subscription/change-plan', payload)

/**
 * 鎵ц璐﹀崟瀵硅处
 * 鍔熻兘鎻忚堪锛氳Е鍙戜竴娆?Mock 瀵硅处娴佺▼骞惰繑鍥炴渶鏂?Billing 鎬昏
 * 鍏ュ弬锛歵riggerBy 瑙﹀彂鏉ユ簮
 * 杩斿洖鍙傛暟锛欱illing 鎬昏鏁版嵁
 * url鍦板潃锛?api/admin/billing/reconciliation/trigger
 * 璇锋眰鏂瑰紡锛歅OST
 */
export const triggerBillingReconcileApi = (payload: BillingReconcilePayload) =>
  post<BillingOverviewResponse>('/api/admin/billing/reconciliation/trigger', payload)

/**
 * 澶勭悊鍙戠エ鍔ㄤ綔
 * 鍔熻兘鎻忚堪锛氬鎸囧畾璁㈠崟鎵ц鍙戠エ涓嬭浇鎴栭偖浠跺彂閫佸苟杩斿洖鏈€鏂?Billing 鎬昏
 * 鍏ュ弬锛歰rderNo 璁㈠崟鍙枫€乤ction 鍙戠エ鍔ㄤ綔
 * 杩斿洖鍙傛暟锛欱illing 鎬昏鏁版嵁
 * url鍦板潃锛?api/admin/billing/order/invoice-action
 * 璇锋眰鏂瑰紡锛歅OST
 */
export const billingInvoiceActionApi = (payload: BillingInvoiceActionPayload) =>
  post<BillingOverviewResponse>('/api/admin/billing/order/invoice-action', payload)
