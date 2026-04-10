import type { Router } from 'vue-router'
import { hasStorageSession } from '../stores/admin/auth'

export function setupRouterGuards(router: Router): void {
  router.beforeEach((to) => {
    if (typeof to.meta.title === 'string') {
      document.title = `${to.meta.title} - Jay Boot Frontend`
    } else {
      document.title = 'Jay Boot Frontend'
    }

    const loggedIn = hasStorageSession()
    const requiresAuth = to.matched.some((record) => Boolean(record.meta.requiresAuth))
    const guestOnly = to.matched.some((record) => Boolean(record.meta.guestOnly))

    if (requiresAuth && !loggedIn) {
      return {
        path: '/admin/auth/login',
        query: {
          redirect: to.fullPath,
        },
      }
    }

    if (guestOnly && loggedIn) {
      const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : '/admin/dashboard'
      return redirect
    }

    return true
  })
}
