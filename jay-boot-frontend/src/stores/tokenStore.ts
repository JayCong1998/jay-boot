/**
 * Token 存储管理
 * 统一管理 token 存取，admin 和 user 端共用同一个 token
 */

/** 存储键常量 */
const TOKEN_KEY = 'jay_boot_auth_token'

/**
 * 获取 token
 */
export const getToken = (): string => localStorage.getItem(TOKEN_KEY) ?? ''

/**
 * 设置 token
 */
export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 清除 token
 */
export const clearToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 检查是否已登录（有 token）
 */
export const hasToken = (): boolean => Boolean(getToken())
