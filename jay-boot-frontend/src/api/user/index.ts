import { userApiConfig } from '../../config/api'
import {
  createErrorResponse,
  initializeMockApis,
  requestMockApi,
  type MockMethod,
  type MockSuccessResponse,
} from './mockManager'
import { requestRealApi } from './realApi'

if (userApiConfig.mode === 'mock') {
  initializeMockApis()
}

const wait = (milliseconds: number) =>
  new Promise<void>((resolve) => {
    window.setTimeout(() => resolve(), milliseconds)
  })

const request = async <T>(
  method: MockMethod,
  url: string,
  payload?: Record<string, unknown>,
): Promise<T> => {
  if (userApiConfig.mode === 'real') {
    return requestRealApi<T>(method, url, payload)
  }

  await wait(userApiConfig.mockDelayMs)
  const response = await requestMockApi(method, url, payload)
  if (!response.success) {
    throw new Error(response.message)
  }
  return (response as MockSuccessResponse<T>).body
}

/**
 * GET 请求
 * 功能描述：根据配置调用 Mock 或真实 GET 接口并返回业务数据
 * 入参：url 接口地址、params 查询参数
 * 返回参数：业务响应 body
 * url地址：任意 GET API
 * 请求方式：GET
 */
export const get = <T>(url: string, params?: Record<string, unknown>) => request<T>('GET', url, params)

/**
 * POST 请求
 * 功能描述：根据配置调用 Mock 或真实 POST 接口并返回业务数据
 * 入参：url 接口地址、data 请求体
 * 返回参数：业务响应 body
 * url地址：任意 POST API
 * 请求方式：POST
 */
export const post = <T>(url: string, data?: Record<string, unknown>) => request<T>('POST', url, data)

export { createErrorResponse }
