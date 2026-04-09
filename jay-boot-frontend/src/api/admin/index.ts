import {
  createErrorResponse,
  initializeMockApis,
  requestMockApi,
  type MockMethod,
  type MockSuccessResponse,
} from './mockManager'

initializeMockApis()

const DEFAULT_DELAY = 180

const wait = (milliseconds: number) =>
  new Promise<void>((resolve) => {
    window.setTimeout(() => resolve(), milliseconds)
  })

const request = async <T>(
  method: MockMethod,
  url: string,
  payload?: Record<string, unknown>,
): Promise<T> => {
  await wait(DEFAULT_DELAY)
  const response = await requestMockApi(method, url, payload)
  if (!response.success) {
    throw new Error(response.message)
  }
  return (response as MockSuccessResponse<T>).body
}

/**
 * GET 请求
 * 功能描述：调用 Mock GET 接口并返回业务数据
 * 入参：url 接口地址、params 查询参数
 * 返回参数：业务响应 body
 * url地址：任意已注册 GET Mock API
 * 请求方式：GET
 */
export const get = <T>(url: string, params?: Record<string, unknown>) => request<T>('GET', url, params)

/**
 * POST 请求
 * 功能描述：调用 Mock POST 接口并返回业务数据
 * 入参：url 接口地址、data 请求体
 * 返回参数：业务响应 body
 * url地址：任意已注册 POST Mock API
 * 请求方式：POST
 */
export const post = <T>(url: string, data?: Record<string, unknown>) => request<T>('POST', url, data)

export { createErrorResponse }

