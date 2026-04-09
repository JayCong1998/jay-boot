import { get, post } from './index'

export type RbacPermissionDecision = 'allow' | 'approval' | 'deny'
export type RbacChangeType = 'create' | 'update' | 'delete'
export type RbacMatrixRoleKey = 'owner' | 'admin' | 'developer' | 'finance'

export interface RbacRoleTemplateItem {
  id: string
  code: string
  description: string
}

export interface RbacPermissionMatrixRow {
  permissionCode: string
  owner: RbacPermissionDecision
  admin: RbacPermissionDecision
  developer: RbacPermissionDecision
  finance: RbacPermissionDecision
}

export interface RbacPolicyNoteItem {
  id: string
  content: string
}

export interface RbacRecentChangeItem {
  id: string
  time: string
  title: string
  detail: string
  type: RbacChangeType
}

export interface RbacOverviewResponse {
  roleTemplates: RbacRoleTemplateItem[]
  permissionMatrix: RbacPermissionMatrixRow[]
  policyNotes: RbacPolicyNoteItem[]
  recentChanges: RbacRecentChangeItem[]
  updatedAt: string
}

interface SaveRbacPermissionMatrixPayload extends Record<string, unknown> {
  permissionMatrix: RbacPermissionMatrixRow[]
}

/**
 * 获取 RBAC 总览
 * 功能描述：获取角色模板、权限矩阵、策略说明和最近权限变更
 * 入参：无
 * 返回参数：RBAC 总览数据
 * url地址：/api/admin/rbac/overview
 * 请求方式：GET
 */
export const getRbacOverviewApi = () => get<RbacOverviewResponse>('/api/admin/rbac/overview')

/**
 * 保存 RBAC 权限策略
 * 功能描述：保存角色权限矩阵并返回最新总览数据
 * 入参：permissionMatrix 权限矩阵
 * 返回参数：RBAC 总览数据
 * url地址：/api/admin/rbac/permission-matrix/save
 * 请求方式：POST
 */
export const saveRbacPermissionMatrixApi = (payload: SaveRbacPermissionMatrixPayload) =>
  post<RbacOverviewResponse>('/api/admin/rbac/permission-matrix/save', payload)
