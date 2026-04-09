import { defineStore } from 'pinia'
import {
  createApiKeyApi,
  getApiKeyOverviewApi,
  rotateApiKeyApi,
  updateApiKeyStatusApi,
  type ApiKeyItem,
  type ApiKeyOverviewResponse,
  type ApiKeyQuotaItem,
  type ApiKeyStatus,
} from '../../api/admin/ApiKeyApi'

type FetchMode = 'initial' | 'refresh'

export const useApiKeyStore = defineStore('apikey', {
  state: () => ({
    keys: [] as ApiKeyItem[],
    scopeOptions: [] as string[],
    limitOptions: [] as string[],
    quotaReminders: [] as ApiKeyQuotaItem[],
    securityTips: [] as string[],
    latestSecret: '',
    updatedAt: '',
    loadingInitial: false,
    refreshing: false,
    creating: false,
    rotating: false,
    updatingStatus: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  getters: {
    hasAnyData: (state) =>
      state.keys.length > 0 ||
      state.scopeOptions.length > 0 ||
      state.limitOptions.length > 0 ||
      state.quotaReminders.length > 0 ||
      state.securityTips.length > 0,
    hasKeyData: (state) => state.keys.length > 0,
    hasQuotaData: (state) => state.quotaReminders.length > 0,
    hasSecurityTips: (state) => state.securityTips.length > 0,
    activeKeyCount: (state) => state.keys.filter((item) => item.status === 'active').length,
    firstActiveKeyId: (state) => state.keys.find((item) => item.status === 'active')?.id ?? '',
  },
  actions: {
    applyOverview(data: ApiKeyOverviewResponse) {
      this.keys = data.keys
      this.scopeOptions = data.scopeOptions
      this.limitOptions = data.limitOptions
      this.quotaReminders = data.quotaReminders
      this.securityTips = data.securityTips
      this.latestSecret = data.latestSecret
      this.updatedAt = data.updatedAt
      this.loadedOnce = true
    },
    clearLatestSecret() {
      this.latestSecret = ''
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
        const data = await getApiKeyOverviewApi()
        this.applyOverview(data)
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = 'API Key 数据加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    async createKey(payload: { name: string; scope: string; limitPolicy: string }) {
      this.creating = true
      try {
        const data = await createApiKeyApi(payload)
        this.applyOverview(data)
        return data.latestSecret
      } finally {
        this.creating = false
      }
    },
    async rotateKey(id: string) {
      this.rotating = true
      try {
        const data = await rotateApiKeyApi({ id })
        this.applyOverview(data)
        return data.latestSecret
      } finally {
        this.rotating = false
      }
    },
    async updateKeyStatus(id: string, targetStatus: ApiKeyStatus) {
      this.updatingStatus = true
      try {
        const data = await updateApiKeyStatusApi({ id, targetStatus })
        this.applyOverview(data)
      } finally {
        this.updatingStatus = false
      }
    },
  },
})
