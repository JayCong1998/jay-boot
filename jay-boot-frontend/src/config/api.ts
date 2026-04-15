/**
 * API 配置
 * 用于配置 API 基础路径和运行模式
 */

export interface ApiConfig {
  baseUrl: string
  mode: 'real' | 'mock'
  mockDelayMs: number
}

/**
 * 全局 API 配置
 * baseUrl: 后端服务地址
 * mode: 'real' 走真实接口，'mock' 走本地模拟数据
 * mockDelayMs: mock 模式下接口延迟（毫秒）
 */
export const apiConfig: ApiConfig = {
  baseUrl: import.meta.env.VITE_API_BASE_URL || '',
  mode: (import.meta.env.VITE_API_MODE as ApiConfig['mode']) || 'real',
  mockDelayMs: Number(import.meta.env.VITE_MOCK_DELAY_MS) || 180,
}
