import type { RouteRecordRaw } from 'vue-router'

export const rootRoute: RouteRecordRaw = {
  path: '/',
  redirect: '/user/home',
}

export const notFoundRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'not-found',
  component: () => import('../../views/admin/NotFoundView.vue'),
  meta: { title: '页面不存在' },
}
