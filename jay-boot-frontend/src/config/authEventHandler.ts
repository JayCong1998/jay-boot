import { message } from 'ant-design-vue'
import type { Router } from 'vue-router'
import { clearToken } from '../stores/tokenStore'

/**
 * 认证过期事件名称
 */
export const AUTH_EXPIRED_EVENT = 'auth:expired'

/**
 * 防抖状态：避免短时间内多次触发处理
 */
let isHandling401 = false
let handleTimer: ReturnType<typeof setTimeout> | null = null

/**
 * 根据当前路径判断是否为 admin 端
 */
const isAdminPath = (): boolean => {
  const path = window.location.pathname
  return path.startsWith('/admin')
}

/**
 * 清除认证信息
 * 同时清除可能存在的旧版本存储键
 */
const clearAuthSession = (): void => {
  clearToken()
  // 当前使用的用户存储键
  localStorage.removeItem('jay_boot_auth_user')
  // 清除旧版本存储键（兼容历史数据）
  localStorage.removeItem('jay_boot_user_auth_user')
}

/**
 * 获取登录页路径
 */
const getLoginPath = (isAdmin: boolean): string => {
  const currentPath = window.location.pathname
  const loginPath = isAdmin ? '/admin/auth/login' : '/user/auth/login'
  
  // 避免在登录页本身触发跳转循环
  if (currentPath === loginPath) {
    return ''
  }
  
  // 保存当前路径用于登录后重定向
  const redirect = currentPath !== '/' ? currentPath : (isAdmin ? '/admin/dashboard' : '/user/workspace')
  return `${loginPath}?redirect=${encodeURIComponent(redirect)}`
}

/**
 * 处理认证过期事件
 * 清除对应端的认证信息并跳转到登录页
 */
const handleAuthExpired = (router: Router): void => {
  // 防抖：避免并发请求触发多次处理
  if (isHandling401) {
    return
  }
  isHandling401 = true

  // 清除之前的定时器
  if (handleTimer) {
    clearTimeout(handleTimer)
  }

  try {
    const isAdmin = isAdminPath()
    
    // 清除认证信息
    clearAuthSession()

    // 提示用户
    message.warning('登录已过期，请重新登录')

    // 计算登录页路径
    const loginPath = getLoginPath(isAdmin)
    if (loginPath) {
      router.push(loginPath)
    }
  } finally {
    // 500ms 后重置防抖状态，避免影响后续正常登录流程
    handleTimer = setTimeout(() => {
      isHandling401 = false
    }, 500)
  }
}

/**
 * 派发认证过期事件
 * 供请求层调用
 */
export const dispatchAuthExpired = (): void => {
  window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT))
}

/**
 * 初始化认证过期事件监听
 * 在应用启动时调用
 */
export const setupAuthExpiredHandler = (router: Router): void => {
  window.addEventListener(AUTH_EXPIRED_EVENT, () => {
    handleAuthExpired(router)
  })
}
