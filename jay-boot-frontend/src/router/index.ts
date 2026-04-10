import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuards } from './guards'
import { adminRoutes } from './routes/admin'
import { rootRoute, notFoundRoute } from './routes/common'
import { userRoutes } from './routes/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [rootRoute, ...adminRoutes, ...userRoutes, notFoundRoute],
})

setupRouterGuards(router)

export default router