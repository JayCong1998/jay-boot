import { get } from '../index'

export interface DictOptionItem {
  itemCode: string
  value: string
  label: string
  sort: number
  color: string | null
  extJson: string | null
}

export interface DictTypeOptions {
  typeCode: string
  options: DictOptionItem[]
}

/**
 * 获取单个字典类型选项
 * 功能描述：根据字典类型编码查询当前可用的字典项列表
 * 入参：typeCode 字典类型编码
 * 返回参数：字典选项列表
 * url地址：/api/public/dict/options
 * 请求方式：GET
 */
export const getDictOptionsApi = (typeCode: string) =>
  get<DictOptionItem[]>('/api/public/dict/options', { typeCode })

/**
 * 批量获取多个字典类型选项
 * 功能描述：一次请求返回多个字典类型下的选项集合
 * 入参：typeCodes 字典类型编码数组
 * 返回参数：按字典类型分组的选项结果
 * url地址：/api/public/dict/options/batch
 * 请求方式：GET
 */
export const getDictBatchOptionsApi = (typeCodes: string[]) =>
  get<DictTypeOptions[]>('/api/public/dict/options/batch', { typeCodes: typeCodes.join(',') })

