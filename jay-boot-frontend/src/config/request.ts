import { message } from 'ant-design-vue'

import { apiConfig } from './api'
import { dispatchAuthExpired } from './authEventHandler'

export type HttpMethod = 'GET' | 'POST' | 'PUT'

interface ApiEnvelope<T> {
  code: number
  body: T
  message: string
  success: boolean
}

interface RequestOptions {
  token?: string
  payload?: Record<string, unknown>
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
  if (!apiConfig.baseUrl) {
    return normalizedPath
  }
  return `${apiConfig.baseUrl}${normalizedPath}`
}

/**
 * 统一请求方法
 * 功能描述：向后端发起 HTTP 请求，统一处理网络错误并返回业务数据
 * 入参：method 请求方式、url 接口地址、options 请求参数与 token
 * 返回参数：接口响应 body
 * url地址：任意后端接口
 * 请求方式：GET/POST/PUT
 */
export const request = async <T>(
  method: HttpMethod,
  url: string,
  options: RequestOptions = {},
): Promise<T> => {
  const { payload, token } = options
  const requestUrl = method === 'GET' ? appendQuery(resolveUrl(url), payload) : resolveUrl(url)
  const headers: Record<string, string> = {}

  if (method !== 'GET') {
    headers['Content-Type'] = 'application/json'
  }

  if (token) {
    headers.Authorization = token
  }

  let response: Response
  try {
    response = await fetch(requestUrl, {
      method,
      headers: Object.keys(headers).length > 0 ? headers : undefined,
      body: method === 'GET' ? undefined : JSON.stringify(payload ?? {}),
    })
  } catch (error) {
    // 网络错误处理（断网、超时、CORS等）
    message.error('网络连接失败，请检查网络后重试')
    throw new Error('网络连接失败')
  }

  if (!response.ok) {
    // 401 认证过期处理：派发事件，由事件处理器统一处理清除存储和跳转
    if (response.status === 401) {
      dispatchAuthExpired()
      throw new Error('登录已过期')
    }
    throw new Error(`请求失败（${response.status}）`)
  }

  const result = (await response.json()) as ApiEnvelope<T>
  if (!result.success) {
    throw new Error(result.message || '请求失败')
  }

  return result.body
}
