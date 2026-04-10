import { defineStore } from 'pinia'
import {
  createAdminUserApi,
  getAdminUserPageApi,
  resetAdminUserPasswordApi,
  updateAdminUserApi,
  updateAdminUserStatusApi,
  type AdminUserCreatePayload,
  type AdminUserItem,
  type AdminUserRole,
  type AdminUserStatus,
  type AdminUserUpdatePayload,
} from '../../api/admin/UserApi'
import { useAuthStore } from './auth'

type FetchMode = 'initial' | 'refresh'

export const useAdminUserManagementStore = defineStore('admin-user-management', {
  state: () => ({
    records: [] as AdminUserItem[],
    total: 0,
    page: 1,
    pageSize: 10,
    keyword: '',
    role: '' as '' | AdminUserRole,
    status: '' as '' | AdminUserStatus,
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
        const data = await getAdminUserPageApi(token, {
          page: this.page,
          pageSize: this.pageSize,
          keyword: this.keyword || undefined,
          role: this.role || undefined,
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
          this.errorMessage = '用户列表加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    setFilters(payload: {
      keyword: string
      role: '' | AdminUserRole
      status: '' | AdminUserStatus
    }) {
      this.keyword = payload.keyword
      this.role = payload.role
      this.status = payload.status
    },
    resetFilters() {
      this.keyword = ''
      this.role = ''
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
    async createUser(payload: AdminUserCreatePayload) {
      await this.runMutation(async (token) => createAdminUserApi(token, payload))
    },
    async updateUser(id: string, payload: AdminUserUpdatePayload) {
      await this.runMutation(async (token) => updateAdminUserApi(token, id, payload))
    },
    async updateUserStatus(id: string, status: AdminUserStatus) {
      await this.runMutation(async (token) => updateAdminUserStatusApi(token, id, status))
    },
    async resetUserPassword(id: string, newPassword: string) {
      await this.runMutation(async (token) => resetAdminUserPasswordApi(token, id, newPassword))
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

