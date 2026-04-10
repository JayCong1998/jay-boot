import type { RouteRecordRaw } from 'vue-router'

const adminChildren: RouteRecordRaw[] = [
  { path: '', redirect: '/admin/dashboard' },
  {
    path: 'dashboard',
    name: 'admin-dashboard',
    component: () => import('../../views/admin/DashboardView.vue'),
    meta: { title: '控制台总览', requiresAuth: true },
  },
  {
    path: 'users',
    name: 'admin-users',
    component: () => import('../../views/admin/UserManagementView.vue'),
    meta: { title: '用户管理', requiresAuth: true },
  },
  {
    path: 'rbac',
    name: 'admin-rbac',
    component: () => import('../../views/admin/RbacView.vue'),
    meta: { title: '角色权限', requiresAuth: true },
  },
  {
    path: 'billing',
    name: 'admin-billing',
    component: () => import('../../views/admin/BillingView.vue'),
    meta: { title: '订阅计费', requiresAuth: true },
  },
  {
    path: 'apikey',
    name: 'admin-apikey',
    component: () => import('../../views/admin/ApiKeyView.vue'),
    meta: { title: 'API Key 管理', requiresAuth: true },
  },
  {
    path: 'ai-gateway',
    name: 'admin-ai-gateway',
    component: () => import('../../views/admin/AiGatewayView.vue'),
    meta: { title: 'AI Gateway', requiresAuth: true },
  },
  {
    path: 'usage',
    name: 'admin-usage',
    component: () => import('../../views/admin/UsageView.vue'),
    meta: { title: '用量与运营', requiresAuth: true },
  },
]

export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin/auth/login',
    name: 'admin-auth-login',
    component: () => import('../../views/admin/AuthLoginView.vue'),
    meta: { title: '登录', guestOnly: true },
  },
  {
    path: '/admin/auth/register',
    name: 'admin-auth-register',
    component: () => import('../../views/admin/AuthRegisterView.vue'),
    meta: { title: '注册', guestOnly: true },
  },
  {
    path: '/admin',
    component: () => import('../../layouts/admin/AppShell.vue'),
    meta: { requiresAuth: true },
    children: adminChildren,
  },
]
