import { get } from '../index'

export interface HomeHero {
  eyebrow: string
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

export interface HomeFeatureCard {
  id: string
  title: string
  points: string[]
  soft: boolean
}

export interface HomeKpiCard {
  id: string
  label: string
  value: string
  desc: string
  soft: boolean
}

export interface HomeFaqItem {
  id: string
  question: string
  answer: string
}

export interface HomeFinalCta {
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

export interface HomeOverviewResponse {
  hero: HomeHero
  trustBadges: string[]
  sectionTitles: {
    features: string
    socialProof: string
    faq: string
  }
  featureCards: HomeFeatureCard[]
  kpiCards: HomeKpiCard[]
  faqList: HomeFaqItem[]
  finalCta: HomeFinalCta
  updatedAt: string
}

export interface HomeCaseItem {
  id: string
  title: string
  industry: string
  gain: string
  summary: string
}

interface HomeCasesParams extends Record<string, unknown> {
  limit?: number
}

/**
 * 获取首页概览数据
 * 功能描述：首页渲染所需的 Hero、价值点、社证、FAQ、CTA 一次性获取
 * 入参：无
 * 返回参数：首页概览数据
 * url地址：/api/user/home/overview
 * 请求方式：GET
 */
export const getHomeOverviewApi = () => get<HomeOverviewResponse>('/api/user/home/overview')

/**
 * 获取首页案例列表
 * 功能描述：获取“真实案例”弹窗所需案例卡片数据
 * 入参：limit 可选，返回案例数量
 * 返回参数：案例列表
 * url地址：/api/user/home/cases
 * 请求方式：GET
 */
export const getHomeCasesApi = (params?: HomeCasesParams) => get<HomeCaseItem[]>('/api/user/home/cases', params)
