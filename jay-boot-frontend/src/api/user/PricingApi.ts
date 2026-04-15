import { get } from '../index'

export type PricingBillingCycle = 'MONTHLY' | 'YEARLY'

export interface PricingHero {
  eyebrow: string
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

export interface PricingCycleOption {
  key: PricingBillingCycle
  label: string
  hint: string
}

export interface PricingPlanCard {
  id: string
  code: string
  name: string
  tag?: string | null
  price: number
  priceText: string
  billingCycleText: string
  fitFor: string
  highlights: string[]
  recommended: boolean
  contactSales: boolean
}

export interface PricingComparisonCell {
  planCode: string
  value: string
}

export interface PricingComparisonRow {
  id: string
  feature: string
  description: string
  cells: PricingComparisonCell[]
}

export interface PricingFaqItem {
  id: string
  question: string
  answer: string
}

export interface PricingFinalCta {
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

export interface PricingOverviewResponse {
  hero: PricingHero
  cycleOptions: PricingCycleOption[]
  selectedCycle: PricingBillingCycle
  planCards: PricingPlanCard[]
  comparisonRows: PricingComparisonRow[]
  faqList: PricingFaqItem[]
  finalCta: PricingFinalCta
  updatedAt: string
}

interface PricingOverviewParams extends Record<string, unknown> {
  billingCycle?: PricingBillingCycle
}

/**
 * 获取订阅定价页概览
 * 功能描述：获取 `/user/pricing` 页面渲染所需的 Hero、周期切换、套餐卡、权益对比、FAQ 与底部 CTA
 * 入参：billingCycle 可选，支持 MONTHLY / YEARLY
 * 返回参数：定价页概览数据
 * url地址：/api/user/pricing/overview
 * 请求方式：GET
 */
export const getPricingOverviewApi = (params?: PricingOverviewParams) =>
  get<PricingOverviewResponse>('/api/user/pricing/overview', params)
