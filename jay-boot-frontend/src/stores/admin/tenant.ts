import { defineStore } from 'pinia'
import {
  getTenantOverviewApi,
  updateCurrentTenantNameApi,
  type TenantCurrent,
  type TenantMember,
} from '../../api/admin/TenantApi'

export const useTenantStore = defineStore('tenant', {
  state: () => ({
    tenant: null as TenantCurrent | null,
    members: [] as TenantMember[],
    updatedAt: '',
    nameInput: '',
    loadingInitial: false,
    refreshing: false,
    savingName: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  getters: {
    hasTenantData: (state) => Boolean(state.tenant),
    hasMemberData: (state) => state.members.length > 0,
    isNameDirty: (state) => {
      if (!state.tenant) {
        return false
      }
      return state.nameInput.trim() !== state.tenant.name
    },
  },
  actions: {
    setNameInput(value: string) {
      this.nameInput = value
    },
    syncNameInput(force = false) {
      if (!this.tenant) {
        this.nameInput = ''
        return
      }
      if (force || !this.isNameDirty) {
        this.nameInput = this.tenant.name
      }
    },
    async initialize() {
      if (this.loadedOnce) {
        return
      }
      await this.loadOverview('initial')
    },
    async refresh() {
      await this.loadOverview('refresh')
    },
    async retry() {
      await this.loadOverview('refresh')
    },
    async loadOverview(mode: 'initial' | 'refresh') {
      this.errorMessage = ''
      if (mode === 'initial') {
        this.loadingInitial = true
      }
      if (mode === 'refresh') {
        this.refreshing = true
      }

      try {
        const data = await getTenantOverviewApi()
        this.tenant = data.tenant
        this.members = data.members
        this.updatedAt = data.updatedAt
        this.syncNameInput(false)
        this.loadedOnce = true
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = '租户信息加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    validateNameInput() {
      const normalized = this.nameInput.trim()
      if (!normalized) {
        throw new Error('工作区名称不能为空')
      }
      if (normalized.length < 2 || normalized.length > 64) {
        throw new Error('工作区名称长度需在 2-64 个字符之间')
      }
      return normalized
    },
    async saveTenantName() {
      if (!this.tenant) {
        throw new Error('租户信息不存在，无法保存')
      }
      const normalized = this.validateNameInput()
      if (normalized === this.tenant.name) {
        return false
      }

      this.savingName = true
      try {
        const tenant = await updateCurrentTenantNameApi({ name: normalized })
        this.tenant = tenant
        this.nameInput = tenant.name
        this.updatedAt = new Date().toISOString()
        return true
      } finally {
        this.savingName = false
      }
    },
  },
})