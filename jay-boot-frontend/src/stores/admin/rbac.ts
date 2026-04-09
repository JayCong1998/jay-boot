import { defineStore } from 'pinia'
import {
  getRbacOverviewApi,
  saveRbacPermissionMatrixApi,
  type RbacChangeType,
  type RbacMatrixRoleKey,
  type RbacPermissionDecision,
  type RbacPermissionMatrixRow,
  type RbacPolicyNoteItem,
  type RbacRecentChangeItem,
  type RbacRoleTemplateItem,
} from '../../api/admin/RbacApi'

const clonePermissionMatrix = (rows: RbacPermissionMatrixRow[]) => rows.map((row) => ({ ...row }))

const isPermissionDecision = (value: unknown): value is RbacPermissionDecision => {
  return value === 'allow' || value === 'approval' || value === 'deny'
}

export const useRbacStore = defineStore('rbac', {
  state: () => ({
    roleTemplates: [] as RbacRoleTemplateItem[],
    permissionMatrix: [] as RbacPermissionMatrixRow[],
    draftPermissionMatrix: [] as RbacPermissionMatrixRow[],
    policyNotes: [] as RbacPolicyNoteItem[],
    recentChanges: [] as RbacRecentChangeItem[],
    updatedAt: '',
    loadingInitial: false,
    refreshing: false,
    saving: false,
    loadedOnce: false,
    errorMessage: '',
  }),
  getters: {
    hasRoleTemplates: (state) => state.roleTemplates.length > 0,
    hasPermissionMatrix: (state) => state.draftPermissionMatrix.length > 0,
    hasPolicyNotes: (state) => state.policyNotes.length > 0,
    hasRecentChanges: (state) => state.recentChanges.length > 0,
    hasAnyData: (state) => {
      return (
        state.roleTemplates.length > 0 ||
        state.draftPermissionMatrix.length > 0 ||
        state.policyNotes.length > 0 ||
        state.recentChanges.length > 0
      )
    },
    isMatrixDirty: (state) => {
      return JSON.stringify(state.draftPermissionMatrix) !== JSON.stringify(state.permissionMatrix)
    },
  },
  actions: {
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
        const data = await getRbacOverviewApi()
        this.roleTemplates = data.roleTemplates
        this.permissionMatrix = clonePermissionMatrix(data.permissionMatrix)
        this.draftPermissionMatrix = clonePermissionMatrix(data.permissionMatrix)
        this.policyNotes = data.policyNotes
        this.recentChanges = data.recentChanges
        this.updatedAt = data.updatedAt
        this.loadedOnce = true
      } catch (error) {
        if (error instanceof Error) {
          this.errorMessage = error.message
        } else {
          this.errorMessage = 'RBAC 信息加载失败，请稍后重试'
        }
      } finally {
        this.loadingInitial = false
        this.refreshing = false
      }
    },
    setPermissionDecision(permissionCode: string, roleKey: RbacMatrixRoleKey, decision: unknown) {
      if (!isPermissionDecision(decision)) {
        return
      }
      const index = this.draftPermissionMatrix.findIndex((row) => row.permissionCode === permissionCode)
      if (index < 0) {
        return
      }
      const current = this.draftPermissionMatrix[index]
      this.draftPermissionMatrix[index] = {
        ...current,
        [roleKey]: decision,
      }
    },
    resetDraftMatrix() {
      this.draftPermissionMatrix = clonePermissionMatrix(this.permissionMatrix)
    },
    async savePermissionMatrix() {
      if (!this.isMatrixDirty) {
        return false
      }
      this.saving = true
      try {
        const data = await saveRbacPermissionMatrixApi({
          permissionMatrix: this.draftPermissionMatrix,
        })
        this.roleTemplates = data.roleTemplates
        this.permissionMatrix = clonePermissionMatrix(data.permissionMatrix)
        this.draftPermissionMatrix = clonePermissionMatrix(data.permissionMatrix)
        this.policyNotes = data.policyNotes
        this.recentChanges = data.recentChanges
        this.updatedAt = data.updatedAt
        return true
      } finally {
        this.saving = false
      }
    },
    getRecentChangeTagColor(type: RbacChangeType) {
      if (type === 'create') {
        return 'success'
      }
      if (type === 'update') {
        return 'processing'
      }
      return 'default'
    },
  },
})