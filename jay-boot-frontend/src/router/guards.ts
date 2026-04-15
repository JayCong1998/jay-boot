import type { Router } from 'vue-router'
import { hasStorageSession } from '../stores/admin/auth'
import { hasUserStorageSession } from '../stores/user/auth'

/**
 * 根据路径判断是否为 admin 端
 */
const isAdminPath = (path: string): boolean => path.startsWith('/admin')

export function setupRouterGuards(router: Router): void {
  router.beforeEach((to) => {
    if (typeof to.meta.title === 'string') {
      document.title = `${to.meta.title} - Jay Boot Frontend`
    } else {
      document.title = 'Jay Boot Frontend'
    }

    const requiresAuth = to.matched.some((record) => Boolean(record.meta.requiresAuth))
    const guestOnly = to.matched.some((record) => Boolean(record.meta.guestOnly))

    // 根据目标路径判断使用哪个端的认证状态
    const isAdmin = isAdminPath(to.path)
    const loggedIn = isAdmin ? hasStorageSession() : hasUserStorageSession()

    // 需要认证但未登录，跳转到对应端的登录页
    if (requiresAuth && !loggedIn) {
      const loginPath = isAdmin ? '/admin/auth/login' : '/user/auth/login'
      return {
        path: loginPath,
        query: {
          redirect: to.fullPath,
        },
      }
    }

    // 仅游客可访问但已登录，重定向到对应端的默认页
    if (guestOnly && loggedIn) {
      // 优先使用路由配置中的 defaultRedirect，否则使用 redirect 参数，最后使用默认值
      const defaultRedirect = isAdmin ? '/admin/dashboard' : '/user/workspace'
      const redirect = typeof to.query.redirect === 'string'
        ? to.query.redirect
        : (to.meta.defaultRedirect ?? defaultRedirect)
      return redirect
    }

    return true
  })
}
