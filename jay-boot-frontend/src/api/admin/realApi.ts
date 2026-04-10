import { adminAuthApiConfig } from '../../config/api'

export type AdminAuthMethod = 'GET' | 'POST' | 'PUT'

interface ApiEnvelope<T> {
  code: number
  body: T
  message: string
  success: boolean
}

interface RealApiOptions {
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
  if (!adminAuthApiConfig.baseUrl) {
    return normalizedPath
  }
  return `${adminAuthApiConfig.baseUrl}${normalizedPath}`
}

/**
 * 调用 admin-auth 真实后端接口
 * 功能描述：向后端发起真实 HTTP 请求，并按统一响应结构返回 body
 * 入参：method 请求方式、url 接口地址、options 请求参数与 token
 * 返回参数：接口响应 body
 * url地址：任意 admin auth 真实后端接口
 * 请求方式：GET/POST/PUT
 */
export const requestAdminAuthRealApi = async <T>(
  method: AdminAuthMethod,
  url: string,
  options: RealApiOptions = {},
): Promise<T> => {
  const { payload, token } = options
  const requestUrl = method === 'GET' ? appendQuery(resolveUrl(url), payload) : resolveUrl(url)
  const headers: Record<string, string> = {}

  if (method !== 'GET') {
    headers['Content-Type'] = 'application/json'
  }

  if (token) {
    headers.satoken = token
  }

  const response = await fetch(requestUrl, {
    method,
    headers: Object.keys(headers).length > 0 ? headers : undefined,
    body: method === 'GET' ? undefined : JSON.stringify(payload ?? {}),
  })

  const result = (await response.json().catch(() => null)) as ApiEnvelope<T> | null
  if (!response.ok) {
    throw new Error(result?.message || `请求失败：${response.status}`)
  }
  if (!result?.success) {
    throw new Error(result?.message || '请求失败')
  }
  return result.body
}
