import { defineStore } from 'pinia'
import {
  getErrorLogPageApi,
  getErrorLogDetailApi,
  deleteErrorLogApi,
  batchDeleteErrorLogApi,
  type ErrorLogItem,
  type ErrorLogPageParams,
} from '../../api/admin/LogApi'
import { useAuthStore } from './auth'

type FetchMode = 'initial' | 'refresh'

export const useErrorLogStore = defineStore('admin-error-log', {
  state: () => ({
    records: [] as ErrorLogItem[],
    total: 0,
    page: 1,
    pageSize: 20,
    keyword: '',
    requestPath: '',
    userId: '',
    startTime: '',
    endTime: '',
    loadingInitial: false,
    refreshing: false,
    loadedOnce: false,
    errorMessage: '',
    detailItem: null as ErrorLogItem | null,
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
        const params: ErrorLogPageParams = {
          page: this.page,
          pageSize: this.pageSize,
        }
        if (this.keyword) params.keyword = this.keyword
        if (this.requestPath) params.requestPath = this.requestPath
        if (this.userId) params.userId = this.userId
        if (this.startTime) params.startTime = this.startTime
        if (this.endTime) params.endTime = this.endTime

        const data = await getErrorLogPageApi(token, params)
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
          this.errorMessage = '异常日志加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    setFilters(payload: {
      keyword: string
      requestPath: string
      userId: string
      startTime: string
      endTime: string
    }) {
      this.keyword = payload.keyword
      this.requestPath = payload.requestPath
      this.userId = payload.userId
      this.startTime = payload.startTime
      this.endTime = payload.endTime
    },
    resetFilters() {
      this.keyword = ''
      this.requestPath = ''
      this.userId = ''
      this.startTime = ''
      this.endTime = ''
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
    async fetchDetail(id: string) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      this.detailItem = await getErrorLogDetailApi(token, id)
      return this.detailItem
    },
    async deleteOne(id: string) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      await deleteErrorLogApi(token, id)
    },
    async batchDelete(ids: string[]) {
      const token = await this.requireToken()
      if (!token) {
        throw new Error('用户未登录')
      }
      await batchDeleteErrorLogApi(token, ids)
    },
    async requireToken() {
      const authStore = useAuthStore()
      await authStore.hydrate()
      return authStore.token || ''
    },
  },
})
