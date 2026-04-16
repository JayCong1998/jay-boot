import { get, post } from '../index'

export interface RequestLogItem {
  id: string
  requestId: string
  userId: string | null
  username: string | null
  method: string
  path: string
  queryString: string | null
  requestParams: string | null
  statusCode: number
  durationMs: number
  clientIp: string | null
  userAgent: string | null
  createdTime: string
}

export interface RequestLogPageResponse {
  records: RequestLogItem[]
  total: number
  page: number
  pageSize: number
}

export interface RequestLogPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  method?: string
  statusCode?: number
  userId?: string
  startTime?: string
  endTime?: string
}

export interface ErrorLogItem {
  id: string
  requestId: string | null
  userId: string | null
  username: string | null
  requestPath: string | null
  requestParams: string | null
  clientIp: string | null
  exceptionClass: string
  exceptionMessage: string | null
  stackTrace: string | null
  createdTime: string
}

export interface ErrorLogPageResponse {
  records: ErrorLogItem[]
  total: number
  page: number
  pageSize: number
}

export interface ErrorLogPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  requestPath?: string
  userId?: string
  startTime?: string
  endTime?: string
}

interface BatchDeletePayload extends Record<string, unknown> {
  ids: string[]
}

/**
 * 获取请求日志分页列表
 */
export const getRequestLogPageApi = (params: RequestLogPageParams) =>
  get<RequestLogPageResponse>('/api/admin/logs/requests', params)

/**
 * 获取请求日志详情
 */
export const getRequestLogDetailApi = (id: string) =>
  get<RequestLogItem>(`/api/admin/logs/requests/${id}`)

/**
 * 删除请求日志
 */
export const deleteRequestLogApi = (id: string) =>
  post<null>(`/api/admin/logs/requests/${id}/delete`)

/**
 * 批量删除请求日志
 */
export const batchDeleteRequestLogApi = (ids: string[]) =>
  post<null>('/api/admin/logs/requests/batch-delete', { ids } as BatchDeletePayload)

/**
 * 获取异常日志分页列表
 */
export const getErrorLogPageApi = (params: ErrorLogPageParams) =>
  get<ErrorLogPageResponse>('/api/admin/logs/errors', params)

/**
 * 获取异常日志详情
 */
export const getErrorLogDetailApi = (id: string) =>
  get<ErrorLogItem>(`/api/admin/logs/errors/${id}`)

/**
 * 删除异常日志
 */
export const deleteErrorLogApi = (id: string) =>
  post<null>(`/api/admin/logs/errors/${id}/delete`)

/**
 * 批量删除异常日志
 */
export const batchDeleteErrorLogApi = (ids: string[]) =>
  post<null>('/api/admin/logs/errors/batch-delete', { ids } as BatchDeletePayload)

export interface OperationLogItem {
  id: string
  module: string
  action: string
  detail: string | null
  userId: string | null
  username: string | null
  clientIp: string | null
  requestId: string | null
  createdTime: string
}

export interface OperationLogPageResponse {
  records: OperationLogItem[]
  total: number
  page: number
  pageSize: number
}

export interface OperationLogPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  module?: string
  userId?: string
  startTime?: string
  endTime?: string
}

/**
 * 获取操作日志分页列表
 */
export const getOperationLogPageApi = (params: OperationLogPageParams) =>
  get<OperationLogPageResponse>('/api/admin/logs/operations', params)

/**
 * 获取操作日志详情
 */
export const getOperationLogDetailApi = (id: string) =>
  get<OperationLogItem>(`/api/admin/logs/operations/${id}`)
