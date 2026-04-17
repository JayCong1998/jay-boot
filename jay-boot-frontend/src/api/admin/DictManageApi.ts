import { get, post } from '../index'

export type DictStatus = 'ENABLED' | 'DISABLED'

export interface AdminDictTypeItem {
  id: string
  typeCode: string
  typeName: string
  status: DictStatus
  description: string | null
  updatedTime: string | null
}

export interface AdminDictItem {
  id: string
  typeCode: string
  itemCode: string
  itemLabel: string
  itemValue: string
  sort: number
  color: string | null
  extJson: string | null
  status: DictStatus
  updatedTime: string | null
}

export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface DictTypePageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  keyword?: string
  status?: DictStatus
}

export interface DictTypeCreatePayload extends Record<string, unknown> {
  typeCode: string
  typeName: string
  status: DictStatus
  description?: string
}

export interface DictTypeUpdatePayload extends Record<string, unknown> {
  typeName: string
  status: DictStatus
  description?: string
}

export interface DictItemPageParams extends Record<string, unknown> {
  page?: number
  pageSize?: number
  typeCode?: string
  keyword?: string
  status?: DictStatus
}

export interface DictItemCreatePayload extends Record<string, unknown> {
  typeCode: string
  itemCode: string
  itemLabel: string
  itemValue: string
  sort: number
  color?: string
  extJson?: string
  status: DictStatus
}

export interface DictItemUpdatePayload extends Record<string, unknown> {
  itemLabel: string
  itemValue: string
  sort: number
  color?: string
  extJson?: string
  status: DictStatus
}

interface DictStatusPayload extends Record<string, unknown> {
  status: DictStatus
}

interface DictSortPayload extends Record<string, unknown> {
  sort: number
}

interface DictBatchIdsPayload extends Record<string, unknown> {
  ids: string[]
}

interface DictItemBatchStatusPayload extends DictBatchIdsPayload {
  status: DictStatus
}

interface DictItemBatchSortAdjustPayload extends DictBatchIdsPayload {
  delta: number
}

/**
 * 获取字典类型分页列表
 * 功能描述：按分页和筛选条件查询字典类型
 * 入参：分页参数、筛选参数
 * 返回参数：字典类型分页结果
 * url地址：/api/admin/dicts/types/page
 * 请求方式：GET
 */
export const getAdminDictTypePageApi = (params: DictTypePageParams) =>
  get<PageResponse<AdminDictTypeItem>>('/api/admin/dicts/types/page', params)

/**
 * 获取字典类型详情
 * 功能描述：根据字典类型ID查询详情
 * 入参：字典类型ID
 * 返回参数：字典类型详情
 * url地址：/api/admin/dicts/types/{id}
 * 请求方式：GET
 */
export const getAdminDictTypeDetailApi = (id: string) =>
  get<AdminDictTypeItem>(`/api/admin/dicts/types/${id}`)

/**
 * 创建字典类型
 * 功能描述：创建新的字典类型
 * 入参：字典类型创建参数
 * 返回参数：空
 * url地址：/api/admin/dicts/types
 * 请求方式：POST
 */
export const createAdminDictTypeApi = (payload: DictTypeCreatePayload) =>
  post<null>('/api/admin/dicts/types', payload)

/**
 * 更新字典类型
 * 功能描述：更新指定字典类型
 * 入参：字典类型ID、更新参数
 * 返回参数：空
 * url地址：/api/admin/dicts/types/{id}
 * 请求方式：POST
 */
export const updateAdminDictTypeApi = (id: string, payload: DictTypeUpdatePayload) =>
  post<null>(`/api/admin/dicts/types/${id}`, payload)

/**
 * 更新字典类型状态
 * 功能描述：启用或禁用指定字典类型
 * 入参：字典类型ID、目标状态
 * 返回参数：空
 * url地址：/api/admin/dicts/types/{id}/status
 * 请求方式：POST
 */
export const updateAdminDictTypeStatusApi = (id: string, status: DictStatus) =>
  post<null>(`/api/admin/dicts/types/${id}/status`, { status } as DictStatusPayload)

/**
 * 删除字典类型
 * 功能描述：删除指定字典类型
 * 入参：字典类型ID
 * 返回参数：空
 * url地址：/api/admin/dicts/types/{id}/delete
 * 请求方式：POST
 */
export const deleteAdminDictTypeApi = (id: string) =>
  post<null>(`/api/admin/dicts/types/${id}/delete`)

/**
 * 获取字典项分页列表
 * 功能描述：按分页和筛选条件查询字典项
 * 入参：分页参数、筛选参数
 * 返回参数：字典项分页结果
 * url地址：/api/admin/dicts/items/page
 * 请求方式：GET
 */
export const getAdminDictItemPageApi = (params: DictItemPageParams) =>
  get<PageResponse<AdminDictItem>>('/api/admin/dicts/items/page', params)

/**
 * 获取字典项详情
 * 功能描述：根据字典项ID查询详情
 * 入参：字典项ID
 * 返回参数：字典项详情
 * url地址：/api/admin/dicts/items/{id}
 * 请求方式：GET
 */
export const getAdminDictItemDetailApi = (id: string) =>
  get<AdminDictItem>(`/api/admin/dicts/items/${id}`)

/**
 * 创建字典项
 * 功能描述：创建新的字典项
 * 入参：字典项创建参数
 * 返回参数：空
 * url地址：/api/admin/dicts/items
 * 请求方式：POST
 */
export const createAdminDictItemApi = (payload: DictItemCreatePayload) =>
  post<null>('/api/admin/dicts/items', payload)

/**
 * 更新字典项
 * 功能描述：更新指定字典项
 * 入参：字典项ID、更新参数
 * 返回参数：空
 * url地址：/api/admin/dicts/items/{id}
 * 请求方式：POST
 */
export const updateAdminDictItemApi = (id: string, payload: DictItemUpdatePayload) =>
  post<null>(`/api/admin/dicts/items/${id}`, payload)

/**
 * 更新字典项状态
 * 功能描述：启用或禁用指定字典项
 * 入参：字典项ID、目标状态
 * 返回参数：空
 * url地址：/api/admin/dicts/items/{id}/status
 * 请求方式：POST
 */
export const updateAdminDictItemStatusApi = (id: string, status: DictStatus) =>
  post<null>(`/api/admin/dicts/items/${id}/status`, { status } as DictStatusPayload)

/**
 * 更新字典项排序
 * 功能描述：更新指定字典项排序值
 * 入参：字典项ID、排序值
 * 返回参数：空
 * url地址：/api/admin/dicts/items/{id}/sort
 * 请求方式：POST
 */
export const updateAdminDictItemSortApi = (id: string, sort: number) =>
  post<null>(`/api/admin/dicts/items/${id}/sort`, { sort } as DictSortPayload)

/**
 * 批量更新字典项状态
 * 功能描述：批量启用或禁用字典项
 * 入参：字典项ID列表、目标状态
 * 返回参数：空
 * url地址：/api/admin/dicts/items/batch-status
 * 请求方式：POST
 */
export const batchUpdateAdminDictItemStatusApi = (ids: string[], status: DictStatus) =>
  post<null>('/api/admin/dicts/items/batch-status', { ids, status } as DictItemBatchStatusPayload)

/**
 * 批量删除字典项
 * 功能描述：批量删除字典项
 * 入参：字典项ID列表
 * 返回参数：空
 * url地址：/api/admin/dicts/items/batch-delete
 * 请求方式：POST
 */
export const batchDeleteAdminDictItemsApi = (ids: string[]) =>
  post<null>('/api/admin/dicts/items/batch-delete', { ids } as DictBatchIdsPayload)

/**
 * 批量调整字典项排序
 * 功能描述：按增量批量调整字典项排序值
 * 入参：字典项ID列表、排序增量
 * 返回参数：空
 * url地址：/api/admin/dicts/items/batch-sort-adjust
 * 请求方式：POST
 */
export const batchAdjustAdminDictItemSortApi = (ids: string[], delta: number) =>
  post<null>('/api/admin/dicts/items/batch-sort-adjust', { ids, delta } as DictItemBatchSortAdjustPayload)

/**
 * 删除字典项
 * 功能描述：删除指定字典项
 * 入参：字典项ID
 * 返回参数：空
 * url地址：/api/admin/dicts/items/{id}/delete
 * 请求方式：POST
 */
export const deleteAdminDictItemApi = (id: string) =>
  post<null>(`/api/admin/dicts/items/${id}/delete`)
