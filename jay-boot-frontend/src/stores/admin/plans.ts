import { defineStore } from 'pinia'
import {
  createAdminPlanApi,
  getAdminPlanPageApi,
  updateAdminPlanApi,
  updateAdminPlanStatusApi,
  type AdminPlanCreatePayload,
  type AdminPlanItem,
  type AdminPlanUpdatePayload,
  type PlanBillingCycle,
  type PlanStatus,
} from '../../api/admin/PlansApi'
import { useAuthStore } from './auth'

type FetchMode = 'initial' | 'refresh'

export const useAdminPlansStore = defineStore('admin-plans-management', {
  state: () => ({
    records: [] as AdminPlanItem[],
    total: 0,
    page: 1,
    pageSize: 10,
    keyword: '',
    billingCycle: '' as '' | PlanBillingCycle,
    status: '' as '' | PlanStatus,
    loadingInitial: false,
    refreshing: false,
    submitting: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  getters: {
    hasData: (state) => state.records.length > 0,
  },
  actions: {
    async initialize() {
      if (this.loadedOnce) {
        return
      }
      await this.fetchList('initial')
    },
    async fetchList(mode: FetchMode = 'refresh') {
      const token = await this.requireToken()
      if (!token) {
        this.errorMessage = '用户未登录'
        this.records = []
        this.total = 0
        return
      }

      this.errorMessage = ''
      if (mode === 'initial') {
        this.loadingInitial = true
      }
      if (mode === 'refresh') {
        this.refreshing = true
      }

      try {
        const data = await getAdminPlanPageApi(token, {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.keyword || undefined,
          billingCycle: this.billingCycle || undefined,
          status: this.status || undefined,
        })
        this.records = data.records
        this.total = data.total
        this.page = data.page
        this.pageSize = data.pageSize
        this.loadedOnce = true

        if (this.total > 0 && this.records.length === 0 && this.page > 1) {
          this.page -= 1
          await this.fetchList('refresh')
        }
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = '套餐列表加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    setFilters(payload: {
      keyword: string
      billingCycle: '' | PlanBillingCycle
      status: '' | PlanStatus
    }) {
      this.keyword = payload.keyword
      this.billingCycle = payload.billingCycle
      this.status = payload.status
    },
    resetFilters() {
      this.keyword = ''
      this.billingCycle = ''
      this.status = ''
      this.page = 1
    },
    async searchWithCurrentFilters() {
      this.page = 1
      await this.fetchList('refresh')
    },
    async changePage(page: number, pageSize?: number) {
      this.page = page
      if (pageSize && pageSize > 0) {
        this.pageSize = pageSize
      }
      await this.fetchList('refresh')
    },
    async createPlan(payload: AdminPlanCreatePayload) {
      await this.runMutation(async (token) => createAdminPlanApi(token, payload))
    },
    async updatePlan(id: string, payload: AdminPlanUpdatePayload) {
      await this.runMutation(async (token) => updateAdminPlanApi(token, id, payload))
    },
    async updatePlanStatus(id: string, status: PlanStatus) {
      await this.runMutation(async (token) => updateAdminPlanStatusApi(token, id, status))
    },
    async runMutation(action: (token: string) => Promise<unknown>) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      this.submitting = true
      try {
        await action(token)
      } finally {
        this.submitting = false
      }
    },
    async requireToken() {
      const authStore = useAuthStore()
      await authStore.hydrate()
      return authStore.token || ''
    },
  },
})

