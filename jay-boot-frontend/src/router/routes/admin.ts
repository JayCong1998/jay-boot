import type { RouteRecordRaw } from 'vue-router'

const adminChildren: RouteRecordRaw[] = [
  { path: '', redirect: '/admin/dashboard' },
  {
    path: 'dashboard',
    name: 'admin-dashboard',
    component: () => import('../../views/admin/DashboardView.vue'),
    meta: { title: '控制台总览' },
  },
  {
    path: 'users',
    name: 'admin-users',
    component: () => import('../../views/admin/UserManagementView.vue'),
    meta: { title: '用户管理' },
  },
  {
    path: 'plans',
    name: 'admin-plans',
    component: () => import('../../views/admin/PlansManagementView.vue'),
    meta: { title: '套餐管理' },
  },
  {
    path: 'logs/requests',
    name: 'admin-logs-requests',
    component: () => import('../../views/admin/RequestLogView.vue'),
    meta: { title: '请求日志' },
  },
  {
    path: 'logs/errors',
    name: 'admin-logs-errors',
    component: () => import('../../views/admin/ErrorLogView.vue'),
    meta: { title: '异常日志' },
  },
]

export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin/auth/login',
    name: 'admin-auth-login',
    component: () => import('../../views/admin/AuthLoginView.vue'),
    meta: { title: '登录', guestOnly: true, defaultRedirect: '/admin/dashboard' },
  },
  {
    path: '/admin/auth/register',
    name: 'admin-auth-register',
    component: () => import('../../views/admin/AuthRegisterView.vue'),
    meta: { title: '注册', guestOnly: true, defaultRedirect: '/admin/dashboard' },
  },
  {
    path: '/admin',
    component: () => import('../../layouts/admin/AdminShell.vue'),
    meta: { requiresAuth: true },
    children: adminChildren,
  },
]
