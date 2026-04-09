import { defineStore } from 'pinia'
import {
  billingInvoiceActionApi,
  changeBillingPlanApi,
  getBillingOverviewApi,
  triggerBillingReconcileApi,
  type BillingCallbackHealthItem,
  type BillingCurrentSubscription,
  type BillingInvoiceAction,
  type BillingOrderItem,
  type BillingPlanCard,
  type BillingPlanCode,
  type BillingStatusStage,
} from '../../api/admin/BillingApi'

type FetchMode = 'initial' | 'refresh'

export const useBillingStore = defineStore('billing', {
  state: () => ({
    plans: [] as BillingPlanCard[],
    currentSubscription: null as BillingCurrentSubscription | null,
    statusStages: [] as BillingStatusStage[],
    callbackHealth: [] as BillingCallbackHealthItem[],
    orders: [] as BillingOrderItem[],
    updatedAt: '',
    loadingInitial: false,
    refreshing: false,
    switchingPlan: false,
    reconciling: false,
    actioningInvoice: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  getters: {
    hasAnyData: (state) =>
      state.plans.length > 0 ||
      Boolean(state.currentSubscription) ||
      state.statusStages.length > 0 ||
      state.callbackHealth.length > 0 ||
      state.orders.length > 0,
    hasPlanData: (state) => state.plans.length > 0,
    hasStatusStageData: (state) => state.statusStages.length > 0,
    hasCallbackHealthData: (state) => state.callbackHealth.length > 0,
    hasOrderData: (state) => state.orders.length > 0,
    pendingOrderCount: (state) => state.orders.filter((item) => item.status === 'pending').length,
  },
  actions: {
    applyOverview(data: {
      plans: BillingPlanCard[]
      currentSubscription: BillingCurrentSubscription
      statusStages: BillingStatusStage[]
      callbackHealth: BillingCallbackHealthItem[]
      orders: BillingOrderItem[]
      updatedAt: string
    }) {
      this.plans = data.plans
      this.currentSubscription = data.currentSubscription
      this.statusStages = data.statusStages
      this.callbackHealth = data.callbackHealth
      this.orders = data.orders
      this.updatedAt = data.updatedAt
      this.loadedOnce = true
    },
    async initialize() {
      if (this.loadedOnce) {
        return
      }
      await this.fetchOverview('initial')
    },
    async refresh() {
      await this.fetchOverview('refresh')
    },
    async retry() {
      await this.fetchOverview('refresh')
    },
    async fetchOverview(mode: FetchMode) {
      this.errorMessage = ''
      if (mode === 'initial') {
        this.loadingInitial = true
      }
      if (mode === 'refresh') {
        this.refreshing = true
      }
      try {
        const data = await getBillingOverviewApi()
        this.applyOverview(data)
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = '订阅计费信息加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    async changePlan(planCode: BillingPlanCode) {
      if (!this.currentSubscription) {
        throw new Error('当前订阅信息不存在，无法切换套餐')
      }
      if (this.currentSubscription.planCode === planCode) {
        return false
      }
      this.switchingPlan = true
      try {
        const data = await changeBillingPlanApi({ planCode })
        this.applyOverview(data)
        return true
      } finally {
        this.switchingPlan = false
      }
    },
    async triggerReconcile() {
      this.reconciling = true
      try {
        const data = await triggerBillingReconcileApi({ triggerBy: 'billing_view' })
        this.applyOverview(data)
      } finally {
        this.reconciling = false
      }
    },
    async invoiceAction(orderNo: string, action: BillingInvoiceAction) {
      this.actioningInvoice = true
      try {
        const data = await billingInvoiceActionApi({ orderNo, action })
        this.applyOverview(data)
      } finally {
        this.actioningInvoice = false
      }
    },
  },
})
