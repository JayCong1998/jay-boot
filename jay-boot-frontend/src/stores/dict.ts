import { defineStore } from 'pinia'
import { getDictBatchOptionsApi, type DictOptionItem } from '../api/admin/DictApi'

type DictOptionsMap = Record<string, DictOptionItem[]>

const normalizeTypeCode = (value: string) => value.trim().toLowerCase()

export const useDictStore = defineStore('dict', {
  state: () => ({
    optionsMap: {} as DictOptionsMap,
  }),
  getters: {
    optionsByType: (state) => (typeCode: string) => {
      const normalized = normalizeTypeCode(typeCode)
      return state.optionsMap[normalized] ?? []
    },
  },
  actions: {
    async fetchBatch(typeCodes: string[], force = false) {
      const normalizedCodes = [...new Set(typeCodes.map(normalizeTypeCode).filter(Boolean))]
      if (normalizedCodes.length === 0) {
        return
      }

      const targetCodes = force
        ? normalizedCodes
        : normalizedCodes.filter((code) => this.optionsMap[code] === undefined)

      if (targetCodes.length === 0) {
        return
      }

      const groups = await getDictBatchOptionsApi(targetCodes)
      const nextMap: DictOptionsMap = { ...this.optionsMap }
      targetCodes.forEach((code) => {
        nextMap[code] = []
      })

      groups.forEach((group) => {
        const normalizedCode = normalizeTypeCode(group.typeCode)
        nextMap[normalizedCode] = [...group.options].sort((a, b) => {
          const left = Number.isFinite(a.sort) ? a.sort : 0
          const right = Number.isFinite(b.sort) ? b.sort : 0
          return left - right
        })
      })
      this.optionsMap = nextMap
    },
  },
})

