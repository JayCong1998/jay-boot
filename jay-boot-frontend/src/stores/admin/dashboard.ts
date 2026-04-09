import { defineStore } from 'pinia'
import {
  getDashboardOverviewApi,
  type DashboardAlertItem,
  type DashboardEventItem,
  type DashboardKpiItem,
  type DashboardRange,
} from '../../api/admin/DashboardApi'

type FetchMode = 'initial' | 'range-change' | 'refresh'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    range: '7d' as DashboardRange,
    updatedAt: '',
    kpis: [] as DashboardKpiItem[],
    alerts: [] as DashboardAlertItem[],
    events: [] as DashboardEventItem[],
    loadingInitial: false,
    loadingKpis: false,
    loadingAlerts: false,
    loadingEvents: false,
    refreshing: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  getters: {
    hasKpiData: (state) => state.kpis.length > 0,
    hasAlertData: (state) => state.alerts.length > 0,
    hasEventData: (state) => state.events.length > 0,
    hasAnyData: (state) => state.kpis.length > 0 || state.alerts.length > 0 || state.events.length > 0,
  },
  actions: {
    async initialize() {
      if (this.loadedOnce) {
        return
      }
      await this.fetchOverview('initial', this.range)
    },
    async changeRange(nextRange: DashboardRange) {
      if (this.range === nextRange) {
        return
      }
      this.range = nextRange
      await this.fetchOverview('range-change', nextRange)
    },
    async refreshPartials() {
      await this.fetchOverview('refresh', this.range)
    },
    async retryCurrentRange() {
      await this.fetchOverview('range-change', this.range)
    },
    async fetchOverview(mode: FetchMode, range: DashboardRange) {
      this.errorMessage = ''
      this.loadingKpis = true
      this.loadingAlerts = true
      this.loadingEvents = true

      if (mode === 'initial') {
        this.loadingInitial = true
      }
      if (mode === 'refresh') {
        this.refreshing = true
      }

      try {
        const data = await getDashboardOverviewApi({ range })
        this.range = data.range
        this.updatedAt = data.updatedAt
        this.kpis = data.kpis
        this.alerts = data.alerts
        this.events = data.events
        this.loadedOnce = true
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = '加载仪表盘失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.loadingKpis = false
        this.loadingAlerts = false
        this.loadingEvents = false
        this.refreshing = false
      }
    },
  },
})

