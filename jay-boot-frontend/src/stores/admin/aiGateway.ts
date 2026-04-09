import { defineStore } from 'pinia'
import {
  getAiGatewayOverviewApi,
  runAiGatewayTestCallApi,
  saveAiGatewayRoutePoliciesApi,
  switchAiGatewayProviderApi,
  type AiGatewayCostItem,
  type AiGatewayFallbackStep,
  type AiGatewayOverviewResponse,
  type AiGatewayProviderItem,
  type AiGatewayRoutePolicyItem,
  type AiGatewayTestResult,
} from '../../api/admin/AiGatewayApi'

type FetchMode = 'initial' | 'refresh'

const cloneRoutePolicies = (items: AiGatewayRoutePolicyItem[]) => items.map((item) => ({ ...item }))

export const useAiGatewayStore = defineStore('aiGateway', {
  state: () => ({
    activeProviderId: '',
    providers: [] as AiGatewayProviderItem[],
    routePolicies: [] as AiGatewayRoutePolicyItem[],
    draftRoutePolicies: [] as AiGatewayRoutePolicyItem[],
    costTrend: [] as AiGatewayCostItem[],
    fallbackSteps: [] as AiGatewayFallbackStep[],
    latestTestResult: null as AiGatewayTestResult | null,
    updatedAt: '',
    loadingInitial: false,
    refreshing: false,
    switchingProvider: false,
    savingPolicies: false,
    testingCall: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  getters: {
    hasAnyData: (state) =>
      state.providers.length > 0 ||
      state.routePolicies.length > 0 ||
      state.costTrend.length > 0 ||
      state.fallbackSteps.length > 0,
    hasProviderData: (state) => state.providers.length > 0,
    hasRoutePolicyData: (state) => state.draftRoutePolicies.length > 0,
    hasCostTrendData: (state) => state.costTrend.length > 0,
    hasFallbackData: (state) => state.fallbackSteps.length > 0,
    activeProvider: (state) => state.providers.find((item) => item.id === state.activeProviderId) ?? null,
    isRoutePolicyDirty: (state) =>
      JSON.stringify(state.draftRoutePolicies) !== JSON.stringify(state.routePolicies),
  },
  actions: {
    applyOverview(data: AiGatewayOverviewResponse) {
      this.activeProviderId = data.activeProviderId
      this.providers = data.providers
      this.routePolicies = cloneRoutePolicies(data.routePolicies)
      this.draftRoutePolicies = cloneRoutePolicies(data.routePolicies)
      this.costTrend = data.costTrend
      this.fallbackSteps = data.fallbackSteps
      this.latestTestResult = data.latestTestResult
      this.updatedAt = data.updatedAt
      this.loadedOnce = true
    },
    setRouteDefaultModel(id: string, value: string) {
      const index = this.draftRoutePolicies.findIndex((item) => item.id === id)
      if (index < 0) {
        return
      }
      this.draftRoutePolicies[index] = {
        ...this.draftRoutePolicies[index],
        defaultModel: value,
      }
    },
    setRouteTimeoutPolicy(id: string, value: string) {
      const index = this.draftRoutePolicies.findIndex((item) => item.id === id)
      if (index < 0) {
        return
      }
      this.draftRoutePolicies[index] = {
        ...this.draftRoutePolicies[index],
        timeoutPolicy: value,
      }
    },
    resetRoutePolicies() {
      this.draftRoutePolicies = cloneRoutePolicies(this.routePolicies)
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
        const data = await getAiGatewayOverviewApi()
        this.applyOverview(data)
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = 'AI Gateway 数据加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    async switchProvider(providerId: string) {
      if (!providerId || providerId === this.activeProviderId) {
        return false
      }
      this.switchingProvider = true
      try {
        const data = await switchAiGatewayProviderApi({ providerId })
        this.applyOverview(data)
        return true
      } finally {
        this.switchingProvider = false
      }
    },
    async saveRoutePolicies() {
      if (!this.isRoutePolicyDirty) {
        return false
      }
      this.savingPolicies = true
      try {
        const data = await saveAiGatewayRoutePoliciesApi({
          routePolicies: this.draftRoutePolicies,
        })
        this.applyOverview(data)
        return true
      } finally {
        this.savingPolicies = false
      }
    },
    async runTestCall(endpoint: string) {
      this.testingCall = true
      try {
        const data = await runAiGatewayTestCallApi({ endpoint })
        this.applyOverview(data)
        return data.latestTestResult
      } finally {
        this.testingCall = false
      }
    },
  },
})
