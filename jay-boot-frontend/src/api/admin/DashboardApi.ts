import { get } from '../index'

export type DashboardRange = '24h' | '7d' | '30d'
export type DashboardTrendDirection = 'up' | 'down' | 'flat'
export type DashboardAlertLevel = 'info' | 'warn' | 'error'
export type DashboardEventStatus = 'success' | 'retrying' | 'failed'

export interface DashboardKpiItem {
  key: string
  title: string
  valueText: string
  trendText: string
  trendDirection: DashboardTrendDirection
}

export interface DashboardAlertItem {
  id: string
  module: string
  level: DashboardAlertLevel
  message: string
  time: string
}

export interface DashboardEventItem {
  id: string
  time: string
  module: string
  event: string
  traceId: string
  status: DashboardEventStatus
}

export interface DashboardOverviewResponse {
  range: DashboardRange
  updatedAt: string
  kpis: DashboardKpiItem[]
  alerts: DashboardAlertItem[]
  events: DashboardEventItem[]
}

interface DashboardOverviewParams extends Record<string, unknown> {
  range: DashboardRange
}

/**
 * 获取仪表盘总览数据
 * 功能描述：根据时间范围获取 KPI、告警和关键事件流数据
 * 入参：range 时间范围（24h/7d/30d）
 * 返回参数：仪表盘总览数据
 * url地址：/api/admin/dashboard/overview
 * 请求方式：GET
 */
export const getDashboardOverviewApi = (params: DashboardOverviewParams) =>
  get<DashboardOverviewResponse>('/api/admin/dashboard/overview', params)

