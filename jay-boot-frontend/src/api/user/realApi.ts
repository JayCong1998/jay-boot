import { userApiConfig } from '../../config/api'
import type { MockMethod } from './mockManager'

interface ApiEnvelope<T> {
  error: number
  body: T
  message: string
  success: boolean
}

const appendQuery = (url: string, query?: Record<string, unknown>) => {
  if (!query) {
    return url
  }

  const searchParams = new URLSearchParams()
  Object.entries(query).forEach(([key, value]) => {
    if (value === null || value === undefined) {
      return
    }
    searchParams.append(key, String(value))
  })

  const queryText = searchParams.toString()
  if (!queryText) {
    return url
  }

  return `${url}?${queryText}`
}

const resolveUrl = (path: string) => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  if (!userApiConfig.baseUrl) {
    return normalizedPath
  }
  return `${userApiConfig.baseUrl}${normalizedPath}`
}

/**
 * 调用真实接口
 * 功能描述：根据配置走真实 HTTP 请求，并校验统一响应结构
 * 入参：method 请求方式、url 接口地址、payload 请求参数
 * 返回参数：接口响应 body
 * url地址：任意真实后端接口
 * 请求方式：GET/POST
 */
export const requestRealApi = async <T>(
  method: MockMethod,
  url: string,
  payload?: Record<string, unknown>,
): Promise<T> => {
  const requestUrl = method === 'GET' ? appendQuery(resolveUrl(url), payload) : resolveUrl(url)
  const response = await fetch(requestUrl, {
    method,
    headers:
      method === 'POST'
        ? {
            'Content-Type': 'application/json',
          }
        : undefined,
    body: method === 'POST' ? JSON.stringify(payload ?? {}) : undefined,
  })

  if (!response.ok) {
    throw new Error(`请求失败（${response.status}）`)
  }

  const result = (await response.json()) as ApiEnvelope<T>
  if (!result.success) {
    throw new Error(result.message || '请求失败')
  }

  return result.body
}
