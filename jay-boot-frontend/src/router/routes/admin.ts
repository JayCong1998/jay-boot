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
    component: () => import('../../views/admin/user/UserManagementView.vue'),
    meta: { title: '用户管理' },
  },
  {
    path: 'plans',
    name: 'admin-plans',
    component: () => import('../../views/admin/plan/PlansManagementView.vue'),
    meta: { title: '套餐管理' },
  },
  {
    path: 'dicts',
    name: 'admin-dicts',
    component: () => import('../../views/admin/dict/DictManagementView.vue'),
    meta: { title: '字典管理' },
  },
  {
    path: 'mails/channels',
    name: 'admin-mails-channels',
    component: () => import('../../views/admin/mail/MailChannelManagementView.vue'),
    meta: { title: '邮件通道管理' },
  },
  {
    path: 'mails/templates',
    name: 'admin-mails-templates',
    component: () => import('../../views/admin/mail/MailTemplateManagementView.vue'),
    meta: { title: '邮件模板管理' },
  },
  {
    path: 'mails/logs',
    name: 'admin-mails-logs',
    component: () => import('../../views/admin/mail/MailLogView.vue'),
    meta: { title: '邮件发送日志' },
  },
  {
    path: 'logs/requests',
    name: 'admin-logs-requests',
    component: () => import('../../views/admin/log/RequestLogView.vue'),
    meta: { title: '请求日志' },
  },
  {
    path: 'logs/errors',
    name: 'admin-logs-errors',
    component: () => import('../../views/admin/log/ErrorLogView.vue'),
    meta: { title: '异常日志' },
  },
  {
    path: 'logs/operations',
    name: 'admin-logs-operations',
    component: () => import('../../views/admin/log/OperationLogView.vue'),
    meta: { title: '操作日志' },
  },
]

export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin/auth/login',
    name: 'admin-auth-login',
    component: () => import('../../views/admin/auth/AuthLoginView.vue'),
    meta: { title: '登录', guestOnly: true, defaultRedirect: '/admin/dashboard' },
  },
  {
    path: '/admin/auth/register',
    name: 'admin-auth-register',
    component: () => import('../../views/admin/auth/AuthRegisterView.vue'),
    meta: { title: '注册', guestOnly: true, defaultRedirect: '/admin/dashboard' },
  },
  {
    path: '/admin',
    component: () => import('../../layouts/admin/AdminShell.vue'),
    meta: { requiresAuth: true },
    children: adminChildren,
  },
]
