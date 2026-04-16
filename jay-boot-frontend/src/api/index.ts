import { request, type HttpMethod } from '../config/request'

/**
 * GET 请求
 * 功能描述：发起 GET 请求并返回业务数据
 * 入参：url 接口地址、params 查询参数
 * 返回参数：业务响应 body
 * url地址：任意 GET API
 * 请求方式：GET
 */
export const get = <T>(url: string, params?: Record<string, unknown>) =>
  request<T>('GET', url, { payload: params })

/**
 * POST 请求
 * 功能描述：发起 POST 请求并返回业务数据
 * 入参：url 接口地址、data 请求体
 * 返回参数：业务响应 body
 * url地址：任意 POST API
 * 请求方式：POST
 */
export const post = <T>(url: string, data?: Record<string, unknown>) =>
  request<T>('POST', url, { payload: data })

export type { HttpMethod }
