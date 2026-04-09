import { defineStore } from 'pinia'
import {
  generateUsageReportApi,
  getUsageOverviewApi,
  retryUsageJobApi,
  sendUsageTestNotifyApi,
  syncUsageAlertsApi,
  type UsageAdviceItem,
  type UsageJobItem,
  type UsageKpiItem,
  type UsageMetricBarItem,
  type UsageNotifyChannelItem,
  type UsageOverviewResponse,
} from '../../api/admin/UsageApi'

type FetchMode = 'initial' | 'refresh'

export const useUsageStore = defineStore('usage', {
  state: () => ({
    kpis: [] as UsageKpiItem[],
    jobs: [] as UsageJobItem[],
    notifyChannels: [] as UsageNotifyChannelItem[],
    metricBars: [] as UsageMetricBarItem[],
    adviceItems: [] as UsageAdviceItem[],
    latestReportId: '',
    updatedAt: '',
    loadingInitial: false,
    refreshing: false,
    generatingReport: false,
    syncingAlerts: false,
    retryingJob: false,
    testingNotify: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  getters: {
    hasAnyData: (state) =>
      state.kpis.length > 0 ||
      state.jobs.length > 0 ||
      state.notifyChannels.length > 0 ||
      state.metricBars.length > 0 ||
      state.adviceItems.length > 0,
    hasKpiData: (state) => state.kpis.length > 0,
    hasJobData: (state) => state.jobs.length > 0,
    hasNotifyChannelData: (state) => state.notifyChannels.length > 0,
    hasMetricBarData: (state) => state.metricBars.length > 0,
    hasAdviceData: (state) => state.adviceItems.length > 0,
  },
  actions: {
    applyOverview(data: UsageOverviewResponse) {
      this.kpis = data.kpis
      this.jobs = data.jobs
      this.notifyChannels = data.notifyChannels
      this.metricBars = data.metricBars
      this.adviceItems = data.adviceItems
      this.latestReportId = data.latestReportId
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
        const data = await getUsageOverviewApi()
        this.applyOverview(data)
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = '用量与运营数据加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    async generateReport() {
      this.generatingReport = true
      try {
        const data = await generateUsageReportApi()
        this.applyOverview(data)
        return data.latestReportId
      } finally {
        this.generatingReport = false
      }
    },
    async syncAlerts() {
      this.syncingAlerts = true
      try {
        const data = await syncUsageAlertsApi()
        this.applyOverview(data)
      } finally {
        this.syncingAlerts = false
      }
    },
    async retryJob(jobId: string) {
      this.retryingJob = true
      try {
        const data = await retryUsageJobApi({ jobId })
        this.applyOverview(data)
      } finally {
        this.retryingJob = false
      }
    },
    async sendTestNotify() {
      this.testingNotify = true
      try {
        const data = await sendUsageTestNotifyApi()
        this.applyOverview(data)
      } finally {
        this.testingNotify = false
      }
    },
  },
})
