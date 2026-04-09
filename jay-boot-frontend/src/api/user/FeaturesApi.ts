import { get } from './index'

export type FeaturePlanCode = 'FREE' | 'PRO' | 'TEAM'
export type FeatureSupportLevel = 'supported' | 'partial' | 'unsupported'

export interface FeaturesHero {
  eyebrow: string
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

export interface FeaturesPlanCard {
  code: FeaturePlanCode
  name: string
  priceText: string
  billingCycleText: string
  fitFor: string
  highlights: string[]
  recommended: boolean
}

export interface FeaturesComparisonItem {
  key: string
  name: string
  description: string
  levels: {
    free: FeatureSupportLevel
    pro: FeatureSupportLevel
    team: FeatureSupportLevel
  }
}

export interface FeaturesComparisonGroup {
  id: string
  title: string
  items: FeaturesComparisonItem[]
}

export interface FeaturesFaqItem {
  id: string
  question: string
  answer: string
}

export interface FeaturesFinalCta {
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

export interface FeaturesOverviewResponse {
  hero: FeaturesHero
  planCards: FeaturesPlanCard[]
  comparisonGroups: FeaturesComparisonGroup[]
  faqList: FeaturesFaqItem[]
  finalCta: FeaturesFinalCta
  updatedAt: string
}

export interface FeaturesRoleProfile {
  key: string
  label: string
  recommendedPlan: FeaturePlanCode
  highlights: string[]
  advice: string
}

export interface FeaturesRoleFitResponse {
  defaultRoleKey: string
  roles: FeaturesRoleProfile[]
}

/**
 * 获取 Features 对比页概览
 * 功能描述：获取对比选型页面渲染所需的 Hero、套餐卡片、对比矩阵、FAQ 与底部 CTA
 * 入参：无
 * 返回参数：Features 对比页概览数据
 * url地址：/api/user/features/overview
 * 请求方式：GET
 */
export const getFeaturesOverviewApi = () =>
  get<FeaturesOverviewResponse>('/api/user/features/overview')

/**
 * 获取角色适配数据
 * 功能描述：获取不同角色下的推荐套餐与决策建议
 * 入参：无
 * 返回参数：角色适配数据
 * url地址：/api/user/features/role-fit
 * 请求方式：GET
 */
export const getFeaturesRoleFitApi = () =>
  get<FeaturesRoleFitResponse>('/api/user/features/role-fit')
