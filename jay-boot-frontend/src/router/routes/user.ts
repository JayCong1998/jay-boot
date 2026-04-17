import type { RouteRecordRaw } from 'vue-router'

const userShellChildren: RouteRecordRaw[] = [
  { path: '', redirect: '/user/home' },
  {
    path: 'home',
    name: 'user-home',
    component: () => import('../../views/user/HomeView.vue'),
    meta: { title: '官网首页' },
  },
  {
    path: 'features',
    name: 'user-features',
    component: () => import('../../views/user/product/FeaturesView.vue'),
    meta: { title: '功能价签' },
  },
  {
    path: 'pricing',
    name: 'user-pricing',
    component: () => import('../../views/user/subscription/PricingView.vue'),
    meta: { title: '订阅定价' },
  },
  {
    path: 'onboarding',
    name: 'user-onboarding',
    component: () => import('../../views/user/product/OnboardingView.vue'),
    meta: { title: '新手引导' },
  },
  {
    path: 'workspace',
    name: 'user-workspace',
    component: () => import('../../views/user/workspace/WorkspaceView.vue'),
    meta: { title: '创作工作台', requiresAuth: true },
  },
  {
    path: 'result-delivery',
    name: 'user-result-delivery',
    component: () => import('../../views/user/workspace/ResultDeliveryView.vue'),
    meta: { title: '结果交付', requiresAuth: true },
  },
  {
    path: 'history',
    name: 'user-history',
    component: () => import('../../views/user/workspace/HistoryView.vue'),
    meta: { title: '历史记录', requiresAuth: true },
  },
  {
    path: 'checkout',
    name: 'user-checkout',
    component: () => import('../../views/user/payment/CheckoutView.vue'),
    meta: { title: '支付页', requiresAuth: true },
  },
  {
    path: 'payment-success',
    name: 'user-payment-success',
    component: () => import('../../views/user/payment/PaymentSuccessView.vue'),
    meta: { title: '支付成功', requiresAuth: true },
  },
  {
    path: 'subscription',
    name: 'user-subscription',
    component: () => import('../../views/user/subscription/SubscriptionView.vue'),
    meta: { title: '订阅中心', requiresAuth: true },
  },
  {
    path: 'profile',
    name: 'user-profile',
    component: () => import('../../views/user/account/ProfileView.vue'),
    meta: { title: '个人中心', requiresAuth: true },
  },
  {
    path: 'referral',
    name: 'user-referral',
    component: () => import('../../views/user/account/ReferralView.vue'),
    meta: { title: '邀请返利', requiresAuth: true },
  },
  {
    path: 'help',
    name: 'user-help',
    component: () => import('../../views/user/product/HelpView.vue'),
    meta: { title: '帮助中心' },
  },
]

export const userRoutes: RouteRecordRaw[] = [
  {
    path: '/user/auth/login',
    name: 'user-auth-login',
    component: () => import('../../views/user/auth/AuthLoginView.vue'),
    meta: { title: '登录账号', guestOnly: true, defaultRedirect: '/user/workspace' },
  },
  {
    path: '/user/auth/register',
    name: 'user-auth-register',
    component: () => import('../../views/user/auth/AuthRegisterView.vue'),
    meta: { title: '免费注册', guestOnly: true, defaultRedirect: '/user/workspace' },
  },
  {
    path: '/user',
    component: () => import('../../layouts/user/UserShell.vue'),
    children: userShellChildren,
  },
]
