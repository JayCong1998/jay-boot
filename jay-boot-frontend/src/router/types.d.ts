import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题 */
    title?: string
    /** 需要认证 */
    requiresAuth?: boolean
    /** 仅游客可访问（已登录用户不可访问） */
    guestOnly?: boolean
    /** 登录后默认重定向路径 */
    defaultRedirect?: string
  }
}
