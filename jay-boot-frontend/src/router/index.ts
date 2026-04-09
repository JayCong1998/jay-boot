import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { hasStorageSession } from '../stores/admin/auth'

const adminChildren: RouteRecordRaw[] = [
  { path: '', redirect: '/admin/dashboard' },
  {
    path: 'dashboard',
    name: 'admin-dashboard',
    component: () => import('../views/admin/DashboardView.vue'),
    meta: { title: '控制台总览', requiresAuth: true },
  },
  {
    path: 'tenant',
    name: 'admin-tenant',
    component: () => import('../views/admin/TenantView.vue'),
    meta: { title: 'Workspace 租户', requiresAuth: true },
  },
  {
    path: 'rbac',
    name: 'admin-rbac',
    component: () => import('../views/admin/RbacView.vue'),
    meta: { title: '角色权限', requiresAuth: true },
  },
  {
    path: 'billing',
    name: 'admin-billing',
    component: () => import('../views/admin/BillingView.vue'),
    meta: { title: '订阅计费', requiresAuth: true },
  },
  {
    path: 'apikey',
    name: 'admin-apikey',
    component: () => import('../views/admin/ApiKeyView.vue'),
    meta: { title: 'API Key 管理', requiresAuth: true },
  },
  {
    path: 'ai-gateway',
    name: 'admin-ai-gateway',
    component: () => import('../views/admin/AiGatewayView.vue'),
    meta: { title: 'AI Gateway', requiresAuth: true },
  },
  {
    path: 'usage',
    name: 'admin-usage',
    component: () => import('../views/admin/UsageView.vue'),
    meta: { title: '用量与运营', requiresAuth: true },
  },
]

const userChildren: RouteRecordRaw[] = [
  { path: '', redirect: '/user/home' },
  {
    path: 'home',
    name: 'user-home',
    component: () => import('../views/user/HomeView.vue'),
    meta: { title: '官网首页' },
  },
  {
    path: 'features',
    name: 'user-features',
    component: () => import('../views/user/FeaturesView.vue'),
    meta: { title: '功能价值' },
  },
  {
    path: 'pricing',
    name: 'user-pricing',
    component: () => import('../views/user/PricingView.vue'),
    meta: { title: '订阅定价' },
  },
  {
    path: 'auth/login',
    name: 'user-auth-login',
    component: () => import('../views/user/AuthLoginView.vue'),
    meta: { title: '登录账号' },
  },
  {
    path: 'auth/register',
    name: 'user-auth-register',
    component: () => import('../views/user/AuthRegisterView.vue'),
    meta: { title: '免费注册' },
  },
  {
    path: 'onboarding',
    name: 'user-onboarding',
    component: () => import('../views/user/OnboardingView.vue'),
    meta: { title: '新手引导' },
  },
  {
    path: 'workspace',
    name: 'user-workspace',
    component: () => import('../views/user/WorkspaceView.vue'),
    meta: { title: '创作工作台' },
  },
  {
    path: 'result-delivery',
    name: 'user-result-delivery',
    component: () => import('../views/user/ResultDeliveryView.vue'),
    meta: { title: '结果交付' },
  },
  {
    path: 'history',
    name: 'user-history',
    component: () => import('../views/user/HistoryView.vue'),
    meta: { title: '历史记录' },
  },
  {
    path: 'checkout',
    name: 'user-checkout',
    component: () => import('../views/user/CheckoutView.vue'),
    meta: { title: '支付页' },
  },
  {
    path: 'payment-success',
    name: 'user-payment-success',
    component: () => import('../views/user/PaymentSuccessView.vue'),
    meta: { title: '支付成功' },
  },
  {
    path: 'subscription',
    name: 'user-subscription',
    component: () => import('../views/user/SubscriptionView.vue'),
    meta: { title: '订阅中心' },
  },
  {
    path: 'profile',
    name: 'user-profile',
    component: () => import('../views/user/ProfileView.vue'),
    meta: { title: '个人中心' },
  },
  {
    path: 'referral',
    name: 'user-referral',
    component: () => import('../views/user/ReferralView.vue'),
    meta: { title: '邀请返利' },
  },
  {
    path: 'help',
    name: 'user-help',
    component: () => import('../views/user/HelpView.vue'),
    meta: { title: '帮助中心' },
  },
  {
    path: ':pathMatch(.*)*',
    name: 'user-not-found',
    component: () => import('../views/user/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/user/home' },
  {
    path: '/admin/auth/login',
    name: 'admin-auth-login',
    component: () => import('../views/admin/AuthLoginView.vue'),
    meta: { title: '登录', guestOnly: true },
  },
  {
    path: '/admin/auth/register',
    name: 'admin-auth-register',
    component: () => import('../views/admin/AuthRegisterView.vue'),
    meta: { title: '注册', guestOnly: true },
  },
  {
    path: '/admin',
    component: () => import('../layouts/admin/AppShell.vue'),
    meta: { requiresAuth: true },
    children: adminChildren,
  },
  {
    path: '/user',
    component: () => import('../layouts/user/UserShell.vue'),
    children: userChildren,
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/admin/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

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

export default router