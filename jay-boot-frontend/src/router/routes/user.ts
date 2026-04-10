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
    component: () => import('../../views/user/FeaturesView.vue'),
    meta: { title: '功能价签' },
  },
  {
    path: 'pricing',
    name: 'user-pricing',
    component: () => import('../../views/user/PricingView.vue'),
    meta: { title: '订阅定价' },
  },
  {
    path: 'onboarding',
    name: 'user-onboarding',
    component: () => import('../../views/user/OnboardingView.vue'),
    meta: { title: '新手引导' },
  },
  {
    path: 'workspace',
    name: 'user-workspace',
    component: () => import('../../views/user/WorkspaceView.vue'),
    meta: { title: '创作工作台' },
  },
  {
    path: 'result-delivery',
    name: 'user-result-delivery',
    component: () => import('../../views/user/ResultDeliveryView.vue'),
    meta: { title: '结果交付' },
  },
  {
    path: 'history',
    name: 'user-history',
    component: () => import('../../views/user/HistoryView.vue'),
    meta: { title: '历史记录' },
  },
  {
    path: 'checkout',
    name: 'user-checkout',
    component: () => import('../../views/user/CheckoutView.vue'),
    meta: { title: '支付页' },
  },
  {
    path: 'payment-success',
    name: 'user-payment-success',
    component: () => import('../../views/user/PaymentSuccessView.vue'),
    meta: { title: '支付成功' },
  },
  {
    path: 'subscription',
    name: 'user-subscription',
    component: () => import('../../views/user/SubscriptionView.vue'),
    meta: { title: '订阅中心' },
  },
  {
    path: 'profile',
    name: 'user-profile',
    component: () => import('../../views/user/ProfileView.vue'),
    meta: { title: '个人中心' },
  },
  {
    path: 'referral',
    name: 'user-referral',
    component: () => import('../../views/user/ReferralView.vue'),
    meta: { title: '邀请返利' },
  },
  {
    path: 'help',
    name: 'user-help',
    component: () => import('../../views/user/HelpView.vue'),
    meta: { title: '帮助中心' },
  },
  {
    path: ':pathMatch(.*)*',
    name: 'user-not-found',
    component: () => import('../../views/user/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

export const userRoutes: RouteRecordRaw[] = [
  {
    path: '/user/auth/login',
    name: 'user-auth-login',
    component: () => import('../../views/user/AuthLoginView.vue'),
    meta: { title: '登录账号' },
  },
  {
    path: '/user/auth/register',
    name: 'user-auth-register',
    component: () => import('../../views/user/AuthRegisterView.vue'),
    meta: { title: '免费注册' },
  },
  {
    path: '/user',
    component: () => import('../../layouts/user/UserShell.vue'),
    children: userShellChildren,
  },
]
