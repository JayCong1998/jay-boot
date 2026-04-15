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
export const getRequestLogPageApi = (token: string, params: RequestLogPageParams) =>
  get<RequestLogPageResponse>('/api/admin/logs/requests', params, token)

/**
 * 获取请求日志详情
 */
export const getRequestLogDetailApi = (token: string, id: string) =>
  get<RequestLogItem>(`/api/admin/logs/requests/${id}`, undefined, token)

/**
 * 删除请求日志
 */
export const deleteRequestLogApi = (token: string, id: string) =>
  post<null>(`/api/admin/logs/requests/${id}/delete`, undefined, token)

/**
 * 批量删除请求日志
 */
export const batchDeleteRequestLogApi = (token: string, ids: string[]) =>
  post<null>('/api/admin/logs/requests/batch-delete', { ids } as BatchDeletePayload, token)

/**
 * 获取异常日志分页列表
 */
export const getErrorLogPageApi = (token: string, params: ErrorLogPageParams) =>
  get<ErrorLogPageResponse>('/api/admin/logs/errors', params, token)

/**
 * 获取异常日志详情
 */
export const getErrorLogDetailApi = (token: string, id: string) =>
  get<ErrorLogItem>(`/api/admin/logs/errors/${id}`, undefined, token)

/**
 * 删除异常日志
 */
export const deleteErrorLogApi = (token: string, id: string) =>
  post<null>(`/api/admin/logs/errors/${id}/delete`, undefined, token)

/**
 * 批量删除异常日志
 */
export const batchDeleteErrorLogApi = (token: string, ids: string[]) =>
  post<null>('/api/admin/logs/errors/batch-delete', { ids } as BatchDeletePayload, token)
