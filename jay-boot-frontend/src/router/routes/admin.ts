import type { RouteRecordRaw } from 'vue-router'

const adminChildren: RouteRecordRaw[] = [
  { path: '', redirect: '/admin/dashboard' },
  {
    path: 'dashboard',
    name: 'admin-dashboard',
    component: () => import('../../views/admin/DashboardView.vue'),
    meta: { title: '\u63A7\u5236\u53F0\u603B\u89C8', requiresAuth: true },
  },
  {
    path: 'users',
    name: 'admin-users',
    component: () => import('../../views/admin/UserManagementView.vue'),
    meta: { title: '\u7528\u6237\u7BA1\u7406', requiresAuth: true },
  },
  {
    path: 'plans',
    name: 'admin-plans',
    component: () => import('../../views/admin/PlansManagementView.vue'),
    meta: { title: '\u5957\u9910\u7BA1\u7406', requiresAuth: true },
  },
  {
    path: 'billing',
    name: 'admin-billing',
    component: () => import('../../views/admin/BillingView.vue'),
    meta: { title: '\u8BA2\u9605\u8BA1\u8D39', requiresAuth: true },
  },
  {
    path: 'apikey',
    name: 'admin-apikey',
    component: () => import('../../views/admin/ApiKeyView.vue'),
    meta: { title: 'API Key \u7BA1\u7406', requiresAuth: true },
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
    meta: { title: '\u7528\u91CF\u4E0E\u8FD0\u8425', requiresAuth: true },
  },
]

export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin/auth/login',
    name: 'admin-auth-login',
    component: () => import('../../views/admin/AuthLoginView.vue'),
    meta: { title: '\u767B\u5F55', guestOnly: true },
  },
  {
    path: '/admin/auth/register',
    name: 'admin-auth-register',
    component: () => import('../../views/admin/AuthRegisterView.vue'),
    meta: { title: '\u6CE8\u518C', guestOnly: true },
  },
  {
    path: '/admin',
    component: () => import('../../layouts/admin/AppShell.vue'),
    meta: { requiresAuth: true },
    children: adminChildren,
  },
]
