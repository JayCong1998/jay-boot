/**
 * API 配置
 * 用于配置 API 基础路径和运行模式
 */

export interface ApiConfig {
  baseUrl: string
  mode?: 'mock' | 'real'
}

/**
 * 全局 API 配置
 * baseUrl: 后端服务地址
 * mode: 运行模式，mock 为模拟数据，real 为真实 API
 */
export const apiConfig: ApiConfig = {
  baseUrl: import.meta.env.VITE_API_BASE_URL || '',
  mode: import.meta.env.VITE_API_MODE === 'mock' ? 'mock' : 'real',
}
