import { get, post } from '../index'

export type MailStatus = 'ENABLED' | 'DISABLED'
export type MailTlsMode = 'NONE' | 'STARTTLS' | 'SSL'
export type MailBizType = 'VERIFY_CODE' | 'SYSTEM_NOTICE'
export type MailBodyType = 'HTML' | 'TEXT'
export type MailSendStatus = 'PENDING' | 'SUCCESS' | 'FAILED'

interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface MailChannelItem {
  id: string
  channelCode: string
  channelName: string
  smtpHost: string
  smtpPort: number
  smtpUsername: string
  smtpPasswordMasked: string
  tlsMode: MailTlsMode
  fromName: string
  fromEmail: string
  priority: number
  status: MailStatus
  remark: string | null
  updatedTime: string
}

export interface MailChannelPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  status?: MailStatus
}

export interface MailChannelCreatePayload extends Record<string, unknown> {
  channelCode: string
  channelName: string
  smtpHost: string
  smtpPort: number
  smtpUsername: string
  smtpPassword: string
  tlsMode: MailTlsMode
  fromName: string
  fromEmail: string
  priority: number
  status: MailStatus
  remark?: string
}

export interface MailChannelUpdatePayload extends Record<string, unknown> {
  channelName: string
  smtpHost: string
  smtpPort: number
  smtpUsername: string
  smtpPassword?: string
  tlsMode: MailTlsMode
  fromName: string
  fromEmail: string
  priority: number
  status: MailStatus
  remark?: string
}

interface MailStatusPayload extends Record<string, unknown> {
  status: MailStatus
}

export interface MailTemplateItem {
  id: string
  templateCode: string
  templateName: string
  bizType: MailBizType
  sceneCode: string
  subjectTemplate: string
  bodyTemplate: string
  bodyType: MailBodyType
  varsSchemaJson: string | null
  status: MailStatus
  remark: string | null
  updatedTime: string
}

export interface MailTemplatePageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  bizType?: MailBizType
  sceneCode?: string
  status?: MailStatus
}

export interface MailTemplateCreatePayload extends Record<string, unknown> {
  templateCode: string
  templateName: string
  bizType: MailBizType
  sceneCode: string
  subjectTemplate: string
  bodyTemplate: string
  bodyType: MailBodyType
  varsSchemaJson?: string
  status: MailStatus
  remark?: string
}

export interface MailTemplateUpdatePayload extends Record<string, unknown> {
  templateName: string
  bizType: MailBizType
  sceneCode: string
  subjectTemplate: string
  bodyTemplate: string
  bodyType: MailBodyType
  varsSchemaJson?: string
  status: MailStatus
  remark?: string
}

export interface MailTemplatePreviewPayload extends Record<string, unknown> {
  templateCode: string
  variables?: Record<string, unknown>
}

export interface MailTemplatePreviewResult {
  subject: string
  body: string
}

export interface MailSendLogItem {
  id: string
  bizType: MailBizType
  sceneCode: string
  templateCode: string
  channelCode: string
  recipientEmail: string
  subjectRendered: string
  bodyRendered: string
  bizKey: string | null
  traceId: string | null
  status: MailSendStatus
  errorCode: string | null
  errorMessage: string | null
  retryCount: number
  maxRetryCount: number
  nextRetryTime: string | null
  sentTime: string | null
  createdTime: string
}

export interface MailSendLogPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  bizType?: MailBizType
  sceneCode?: string
  templateCode?: string
  recipientEmail?: string
  status?: MailSendStatus
  startTime?: string
  endTime?: string
}

/**
 * 获取邮件通道分页列表
 * 功能描述：按分页和筛选条件查询邮件通道
 * 入参：分页参数、关键字、状态
 * 返回参数：邮件通道分页数据
 * url地址：/api/admin/mails/channels/page
 * 请求方式：GET
 */
export const getMailChannelPageApi = (params: MailChannelPageParams) =>
  get<PageResponse<MailChannelItem>>('/api/admin/mails/channels/page', params)

/**
 * 获取邮件通道详情
 * 功能描述：根据ID查询邮件通道完整信息
 * 入参：通道ID
 * 返回参数：邮件通道详情
 * url地址：/api/admin/mails/channels/{id}
 * 请求方式：GET
 */
export const getMailChannelDetailApi = (id: string) =>
  get<MailChannelItem>(`/api/admin/mails/channels/${id}`)

/**
 * 创建邮件通道
 * 功能描述：创建新的SMTP邮件通道
 * 入参：邮件通道创建参数
 * 返回参数：空
 * url地址：/api/admin/mails/channels
 * 请求方式：POST
 */
export const createMailChannelApi = (payload: MailChannelCreatePayload) =>
  post<null>('/api/admin/mails/channels', payload)

/**
 * 更新邮件通道
 * 功能描述：更新指定邮件通道配置
 * 入参：通道ID、更新参数
 * 返回参数：空
 * url地址：/api/admin/mails/channels/{id}
 * 请求方式：POST
 */
export const updateMailChannelApi = (id: string, payload: MailChannelUpdatePayload) =>
  post<null>(`/api/admin/mails/channels/${id}`, payload)

/**
 * 更新邮件通道状态
 * 功能描述：启用或禁用指定邮件通道
 * 入参：通道ID、目标状态
 * 返回参数：空
 * url地址：/api/admin/mails/channels/{id}/status
 * 请求方式：POST
 */
export const updateMailChannelStatusApi = (id: string, status: MailStatus) =>
  post<null>(`/api/admin/mails/channels/${id}/status`, { status } as MailStatusPayload)

/**
 * 删除邮件通道
 * 功能描述：删除指定邮件通道
 * 入参：通道ID
 * 返回参数：空
 * url地址：/api/admin/mails/channels/{id}/delete
 * 请求方式：POST
 */
export const deleteMailChannelApi = (id: string) =>
  post<null>(`/api/admin/mails/channels/${id}/delete`)

/**
 * 获取邮件模板分页列表
 * 功能描述：按分页和筛选条件查询邮件模板
 * 入参：分页参数、关键字、业务类型、场景码、状态
 * 返回参数：邮件模板分页数据
 * url地址：/api/admin/mails/templates/page
 * 请求方式：GET
 */
export const getMailTemplatePageApi = (params: MailTemplatePageParams) =>
  get<PageResponse<MailTemplateItem>>('/api/admin/mails/templates/page', params)

/**
 * 获取邮件模板详情
 * 功能描述：根据ID查询邮件模板完整信息
 * 入参：模板ID
 * 返回参数：邮件模板详情
 * url地址：/api/admin/mails/templates/{id}
 * 请求方式：GET
 */
export const getMailTemplateDetailApi = (id: string) =>
  get<MailTemplateItem>(`/api/admin/mails/templates/${id}`)

/**
 * 创建邮件模板
 * 功能描述：创建新的邮件模板
 * 入参：模板创建参数
 * 返回参数：空
 * url地址：/api/admin/mails/templates
 * 请求方式：POST
 */
export const createMailTemplateApi = (payload: MailTemplateCreatePayload) =>
  post<null>('/api/admin/mails/templates', payload)

/**
 * 更新邮件模板
 * 功能描述：更新指定邮件模板
 * 入参：模板ID、更新参数
 * 返回参数：空
 * url地址：/api/admin/mails/templates/{id}
 * 请求方式：POST
 */
export const updateMailTemplateApi = (id: string, payload: MailTemplateUpdatePayload) =>
  post<null>(`/api/admin/mails/templates/${id}`, payload)

/**
 * 更新邮件模板状态
 * 功能描述：启用或禁用指定模板
 * 入参：模板ID、目标状态
 * 返回参数：空
 * url地址：/api/admin/mails/templates/{id}/status
 * 请求方式：POST
 */
export const updateMailTemplateStatusApi = (id: string, status: MailStatus) =>
  post<null>(`/api/admin/mails/templates/${id}/status`, { status } as MailStatusPayload)

/**
 * 删除邮件模板
 * 功能描述：删除指定邮件模板
 * 入参：模板ID
 * 返回参数：空
 * url地址：/api/admin/mails/templates/{id}/delete
 * 请求方式：POST
 */
export const deleteMailTemplateApi = (id: string) =>
  post<null>(`/api/admin/mails/templates/${id}/delete`)

/**
 * 预览模板渲染结果
 * 功能描述：根据模板编码和变量预览渲染后的主题与正文
 * 入参：模板编码、变量对象
 * 返回参数：渲染结果
 * url地址：/api/admin/mails/templates/preview
 * 请求方式：POST
 */
export const previewMailTemplateApi = (payload: MailTemplatePreviewPayload) =>
  post<MailTemplatePreviewResult>('/api/admin/mails/templates/preview', payload)

/**
 * 获取邮件发送日志分页列表
 * 功能描述：按筛选条件查询邮件发送日志
 * 入参：分页参数、业务筛选参数、时间范围
 * 返回参数：邮件发送日志分页数据
 * url地址：/api/admin/mails/logs/page
 * 请求方式：GET
 */
export const getMailSendLogPageApi = (params: MailSendLogPageParams) =>
  get<PageResponse<MailSendLogItem>>('/api/admin/mails/logs/page', params)

/**
 * 获取邮件发送日志详情
 * 功能描述：根据日志ID查询邮件发送日志详情
 * 入参：日志ID
 * 返回参数：邮件发送日志详情
 * url地址：/api/admin/mails/logs/{id}
 * 请求方式：GET
 */
export const getMailSendLogDetailApi = (id: string) =>
  get<MailSendLogItem>(`/api/admin/mails/logs/${id}`)

/**
 * 重试发送邮件
 * 功能描述：按日志ID触发一次重试发送
 * 入参：日志ID
 * 返回参数：空
 * url地址：/api/admin/mails/logs/{id}/retry
 * 请求方式：POST
 */
export const retryMailSendLogApi = (id: string) =>
  post<null>(`/api/admin/mails/logs/${id}/retry`)
