import { get, post } from './index'

export type UsageKpiTone = 'normal' | 'warning'
export type UsageJobStatus = 'success' | 'retrying' | 'failed'
export type UsageNotifyStatus = 'normal' | 'timeout'

export interface UsageKpiItem {
  key: string
  title: string
  valueText: string
  hintText: string
  tone: UsageKpiTone
}

export interface UsageJobItem {
  id: string
  jobId: string
  type: string
  status: UsageJobStatus
  retryCount: number
  nextRetryAt: string
}

export interface UsageNotifyChannelItem {
  id: string
  name: string
  status: UsageNotifyStatus
  detail: string
}

export interface UsageMetricBarItem {
  id: string
  label: string
  percent: number
  valueText: string
}

export interface UsageAdviceItem {
  id: string
  content: string
}

export interface UsageOverviewResponse {
  kpis: UsageKpiItem[]
  jobs: UsageJobItem[]
  notifyChannels: UsageNotifyChannelItem[]
  metricBars: UsageMetricBarItem[]
  adviceItems: UsageAdviceItem[]
  latestReportId: string
  updatedAt: string
}

interface UsageJobRetryPayload extends Record<string, unknown> {
  jobId: string
}

/**
 * 获取用量与运营总览
 * 功能描述：返回用量 KPI、任务队列、通知通道、运营报表与告警治理建议
 * 入参：无
 * 返回参数：Usage 总览数据
 * url地址：/api/admin/usage/overview
 * 请求方式：GET
 */
export const getUsageOverviewApi = () => get<UsageOverviewResponse>('/api/admin/usage/overview')

/**
 * 生成运营日报
 * 功能描述：触发一次日报生成任务并返回最新 Usage 总览
 * 入参：无
 * 返回参数：Usage 总览数据
 * url地址：/api/admin/usage/report/generate
 * 请求方式：POST
 */
export const generateUsageReportApi = () => post<UsageOverviewResponse>('/api/admin/usage/report/generate')

/**
 * 同步告警状态
 * 功能描述：执行一次全量告警同步并返回最新 Usage 总览
 * 入参：无
 * 返回参数：Usage 总览数据
 * url地址：/api/admin/usage/alerts/sync
 * 请求方式：POST
 */
export const syncUsageAlertsApi = () => post<UsageOverviewResponse>('/api/admin/usage/alerts/sync')

/**
 * 重试任务
 * 功能描述：对指定任务执行重试并返回最新 Usage 总览
 * 入参：jobId 任务 ID
 * 返回参数：Usage 总览数据
 * url地址：/api/admin/usage/job/retry
 * 请求方式：POST
 */
export const retryUsageJobApi = (payload: UsageJobRetryPayload) =>
  post<UsageOverviewResponse>('/api/admin/usage/job/retry', payload)

/**
 * 发送测试通知
 * 功能描述：触发一次全渠道测试通知并返回最新 Usage 总览
 * 入参：无
 * 返回参数：Usage 总览数据
 * url地址：/api/admin/usage/notify/test
 * 请求方式：POST
 */
export const sendUsageTestNotifyApi = () => post<UsageOverviewResponse>('/api/admin/usage/notify/test')
