<template>
  <section class="dict-page">
    <header class="dict-toolbar">
      <div>
        <h2 class="dict-title">字典管理</h2>
        <p class="dict-subtitle">统一维护字典类型与字典项，供管理页和业务页下拉选项复用。</p>
      </div>
      <div class="dict-toolbar__actions">
        <a-button :loading="isRefreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button v-if="activeTab === 'type'" type="primary" @click="onOpenCreateTypeModal">新建类型</a-button>
        <a-button v-else type="primary" @click="onOpenCreateItemModal">新建字典项</a-button>
      </div>
    </header>

    <a-alert v-if="currentErrorMessage" class="dict-error" type="error" show-icon :message="currentErrorMessage">
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-card class="dict-card" :bordered="false">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="type" tab="字典类型">
          <a-form layout="vertical">
            <a-row :gutter="12">
              <a-col :xs="24" :md="8">
                <a-form-item label="关键词">
                  <a-input
                    v-model:value="typeFilters.keyword"
                    allow-clear
                    placeholder="输入类型编码或名称"
                    @pressEnter="onSearchClick"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="6">
                <a-form-item label="状态">
                  <a-select v-model:value="typeFilters.status" :options="dictStatusFilterOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="10">
                <a-form-item label=" ">
                  <a-space>
                    <a-button type="primary" :loading="isRefreshing" @click="onSearchClick">查询</a-button>
                    <a-button @click="onResetClick">重置</a-button>
                  </a-space>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <a-skeleton v-if="typeState.loadingInitial && !hasTypeData" active :paragraph="{ rows: 6 }" />
          <template v-else>
            <a-table
              :columns="typeColumns"
              :data-source="typeState.records"
              :loading="typeTableLoading"
              :pagination="false"
              row-key="id"
              :scroll="{ x: 980 }"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="resolveStatusMeta(record.status).color">
                    {{ resolveStatusMeta(record.status).label }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'updatedTime'">
                  {{ formatDateTime(record.updatedTime) }}
                </template>
                <template v-else-if="column.key === 'actions'">
                  <a-space size="small">
                    <a-button type="link" size="small" @click="onOpenEditTypeModal(record)">编辑</a-button>
                    <a-button type="link" size="small" @click="onToggleTypeStatus(record)">
                      {{ record.status === 'ENABLED' ? '禁用' : '启用' }}
                    </a-button>
                    <a-button danger type="link" size="small" @click="onDeleteType(record)">删除</a-button>
                  </a-space>
                </template>
              </template>
            </a-table>

            <div class="dict-pagination">
              <a-pagination
                :current="typeState.page"
                :page-size="typeState.pageSize"
                :total="typeState.total"
                :show-total="(total: number) => `共 ${total} 条`"
                show-size-changer
                :page-size-options="['10', '20', '50']"
                @change="onTypePageChange"
              />
            </div>
          </template>
        </a-tab-pane>

        <a-tab-pane key="item" tab="字典项">
          <a-form layout="vertical">
            <a-row :gutter="12">
              <a-col :xs="24" :md="6">
                <a-form-item label="字典类型">
                  <a-select v-model:value="itemFilters.typeCode" :options="dictTypeFilterOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="关键词">
                  <a-input
                    v-model:value="itemFilters.keyword"
                    allow-clear
                    placeholder="输入编码/名称/值"
                    @pressEnter="onSearchClick"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="6">
                <a-form-item label="状态">
                  <a-select v-model:value="itemFilters.status" :options="dictStatusFilterOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="4">
                <a-form-item label=" ">
                  <a-space>
                    <a-button type="primary" :loading="isRefreshing" @click="onSearchClick">查询</a-button>
                    <a-button @click="onResetClick">重置</a-button>
                  </a-space>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <div class="dict-batch-toolbar">
            <div class="dict-batch-toolbar__summary">已选 {{ selectedItemIds.length }} 项</div>
            <a-space :wrap="true" size="small">
              <a-button :disabled="!hasSelectedItems" @click="onBatchUpdateItemStatus('ENABLED')">批量启用</a-button>
              <a-button :disabled="!hasSelectedItems" @click="onBatchUpdateItemStatus('DISABLED')">批量禁用</a-button>
              <a-input-number
                v-model:value="itemBatchSortDelta"
                :disabled="!hasSelectedItems"
                :controls="false"
                :precision="0"
                placeholder="排序增量"
                style="width: 120px"
              />
              <a-button :disabled="!hasSelectedItems" @click="onBatchAdjustItemSort">批量调整排序</a-button>
              <a-button danger :disabled="!hasSelectedItems" @click="onBatchDeleteItems">批量删除</a-button>
              <a-button :disabled="!hasSelectedItems" @click="clearSelectedItems">清空选择</a-button>
            </a-space>
          </div>

          <a-skeleton v-if="itemState.loadingInitial && !hasItemData" active :paragraph="{ rows: 6 }" />
          <template v-else>
            <a-table
              :columns="itemColumns"
              :data-source="itemState.records"
              :loading="itemTableLoading"
              :pagination="false"
              :row-selection="itemRowSelection"
              row-key="id"
              :scroll="{ x: 1140 }"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="resolveStatusMeta(record.status).color">
                    {{ resolveStatusMeta(record.status).label }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'updatedTime'">
                  {{ formatDateTime(record.updatedTime) }}
                </template>
                <template v-else-if="column.key === 'actions'">
                  <a-space size="small">
                    <a-button type="link" size="small" @click="onOpenEditItemModal(record)">编辑</a-button>
                    <a-button type="link" size="small" @click="onToggleItemStatus(record)">
                      {{ record.status === 'ENABLED' ? '禁用' : '启用' }}
                    </a-button>
                    <a-button type="link" size="small" @click="onMoveItemSort(record, -10)">上移</a-button>
                    <a-button type="link" size="small" @click="onMoveItemSort(record, 10)">下移</a-button>
                    <a-button danger type="link" size="small" @click="onDeleteItem(record)">删除</a-button>
                  </a-space>
                </template>
              </template>
            </a-table>

            <div class="dict-pagination">
              <a-pagination
                :current="itemState.page"
                :page-size="itemState.pageSize"
                :total="itemState.total"
                :show-total="(total: number) => `共 ${total} 条`"
                show-size-changer
                :page-size-options="['10', '20', '50']"
                @change="onItemPageChange"
              />
            </div>
          </template>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal
      v-model:open="typeModal.open"
      :title="typeModal.mode === 'create' ? '新建字典类型' : '编辑字典类型'"
      :confirm-loading="typeState.submitting"
      @ok="onSubmitType"
    >
      <a-form layout="vertical">
        <a-form-item label="类型编码" required>
          <a-input
            v-model:value="typeModal.form.typeCode"
            :disabled="typeModal.mode === 'edit'"
            :maxlength="64"
            placeholder="例如：plan_status"
          />
        </a-form-item>
        <a-form-item label="类型名称" required>
          <a-input v-model:value="typeModal.form.typeName" :maxlength="64" placeholder="例如：套餐状态" />
        </a-form-item>
        <a-form-item label="状态" required>
          <a-select v-model:value="typeModal.form.status" :options="dictStatusFormOptions" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="typeModal.form.description" :rows="3" :maxlength="255" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="itemModal.open"
      :title="itemModal.mode === 'create' ? '新建字典项' : '编辑字典项'"
      :confirm-loading="itemState.submitting"
      :width="720"
      @ok="onSubmitItem"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12">
            <a-form-item label="字典类型" required>
              <a-select
                v-model:value="itemModal.form.typeCode"
                :disabled="itemModal.mode === 'edit'"
                :options="dictTypeFormOptions"
                placeholder="请选择字典类型"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="字典项编码" required>
              <a-input
                v-model:value="itemModal.form.itemCode"
                :disabled="itemModal.mode === 'edit'"
                :maxlength="64"
                placeholder="例如：ACTIVE"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :xs="24" :md="12">
            <a-form-item label="字典项名称" required>
              <a-input v-model:value="itemModal.form.itemLabel" :maxlength="128" placeholder="例如：启用" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="字典项值" required>
              <a-input v-model:value="itemModal.form.itemValue" :maxlength="128" placeholder="例如：ACTIVE" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :xs="24" :md="8">
            <a-form-item label="排序值" required>
              <a-input-number v-model:value="itemModal.form.sort" :controls="false" :precision="0" class="dict-full-width" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="颜色">
              <a-input v-model:value="itemModal.form.color" :maxlength="32" placeholder="例如：success" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="状态" required>
              <a-select v-model:value="itemModal.form.status" :options="dictStatusFormOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="扩展 JSON">
          <a-textarea v-model:value="itemModal.form.extJson" :rows="4" :maxlength="4000" />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import {
  batchAdjustAdminDictItemSortApi,
  batchDeleteAdminDictItemsApi,
  batchUpdateAdminDictItemStatusApi,
  createAdminDictItemApi,
  createAdminDictTypeApi,
  deleteAdminDictItemApi,
  deleteAdminDictTypeApi,
  getAdminDictItemDetailApi,
  getAdminDictItemPageApi,
  getAdminDictTypeDetailApi,
  getAdminDictTypePageApi,
  updateAdminDictItemApi,
  updateAdminDictItemSortApi,
  updateAdminDictItemStatusApi,
  updateAdminDictTypeApi,
  updateAdminDictTypeStatusApi,
  type AdminDictItem,
  type AdminDictTypeItem,
  type DictStatus,
} from '../../../api/admin/DictManageApi'

type ActiveTab = 'type' | 'item'
type TypeModalMode = 'create' | 'edit'
type ItemModalMode = 'create' | 'edit'
type TagMeta = { label: string; color: string }

const STATUS_META: Record<DictStatus, TagMeta> = {
  ENABLED: { label: '启用', color: 'success' },
  DISABLED: { label: '禁用', color: 'default' },
}
const dictStatusFormOptions: Array<{ label: string; value: DictStatus }> = [
  { label: '启用', value: 'ENABLED' },
  { label: '禁用', value: 'DISABLED' },
]
const dictStatusFilterOptions: Array<{ label: string; value: '' | DictStatus }> = [
  { label: '全部状态', value: '' },
  ...dictStatusFormOptions,
]

const activeTab = ref<ActiveTab>('type')

const typeState = reactive({
  records: [] as AdminDictTypeItem[],
  total: 0,
  page: 1,
  pageSize: 10,
  loadingInitial: false,
  refreshing: false,
  submitting: false,
  errorMessage: '',
})
const hasTypeData = computed(() => typeState.records.length > 0)
const typeTableLoading = computed(() => typeState.loadingInitial || typeState.refreshing)

const itemState = reactive({
  records: [] as AdminDictItem[],
  total: 0,
  page: 1,
  pageSize: 10,
  loadingInitial: false,
  refreshing: false,
  submitting: false,
  errorMessage: '',
})
const hasItemData = computed(() => itemState.records.length > 0)
const itemTableLoading = computed(() => itemState.loadingInitial || itemState.refreshing)
const selectedItemIds = ref<string[]>([])
const itemBatchSortDelta = ref<number>(10)
const hasSelectedItems = computed(() => selectedItemIds.value.length > 0)
const itemRowSelection = computed(() => ({
  selectedRowKeys: selectedItemIds.value,
  onChange: (keys: Array<string | number>) => {
    selectedItemIds.value = keys.map((key) => String(key))
  },
}))

const isRefreshing = computed(() => activeTab.value === 'type' ? typeState.refreshing : itemState.refreshing)
const currentErrorMessage = computed(() => activeTab.value === 'type' ? typeState.errorMessage : itemState.errorMessage)

const typeFilters = reactive({
  keyword: '',
  status: '' as '' | DictStatus,
})

const itemFilters = reactive({
  typeCode: '',
  keyword: '',
  status: '' as '' | DictStatus,
})

const typeSelectOptions = ref<Array<{ label: string; value: string }>>([])
const dictTypeFilterOptions = computed<Array<{ label: string; value: string }>>(() => [
  { label: '全部类型', value: '' },
  ...typeSelectOptions.value,
])
const dictTypeFormOptions = computed(() => typeSelectOptions.value)

const typeModal = reactive({
  open: false,
  mode: 'create' as TypeModalMode,
  editingId: '',
  form: {
    typeCode: '',
    typeName: '',
    status: 'ENABLED' as DictStatus,
    description: '',
  },
})

const itemModal = reactive({
  open: false,
  mode: 'create' as ItemModalMode,
  editingId: '',
  form: {
    typeCode: '',
    itemCode: '',
    itemLabel: '',
    itemValue: '',
    sort: 10,
    color: '',
    extJson: '',
    status: 'ENABLED' as DictStatus,
  },
})

const typeColumns = [
  { title: '类型编码', dataIndex: 'typeCode', key: 'typeCode', width: 220 },
  { title: '类型名称', dataIndex: 'typeName', key: 'typeName', width: 220 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '描述', dataIndex: 'description', key: 'description', width: 240 },
  { title: '更新时间', dataIndex: 'updatedTime', key: 'updatedTime', width: 190 },
  { title: '操作', key: 'actions', width: 240 },
]

const itemColumns = [
  { title: '类型编码', dataIndex: 'typeCode', key: 'typeCode', width: 160 },
  { title: '字典项编码', dataIndex: 'itemCode', key: 'itemCode', width: 180 },
  { title: '名称', dataIndex: 'itemLabel', key: 'itemLabel', width: 140 },
  { title: '值', dataIndex: 'itemValue', key: 'itemValue', width: 140 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 100 },
  { title: '颜色', dataIndex: 'color', key: 'color', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '更新时间', dataIndex: 'updatedTime', key: 'updatedTime', width: 190 },
  { title: '操作', key: 'actions', width: 320 },
]

const resolveStatusMeta = (status: DictStatus) => STATUS_META[status] ?? STATUS_META.DISABLED

const formatDateTime = (value: string | null | undefined) => {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const normalizeTypeCode = (value: string) => value.trim().toLowerCase()
const normalizeText = (value: string) => value.trim()
const normalizeOptionalText = (value: string) => {
  const normalized = value.trim()
  return normalized || undefined
}

const clearSelectedItems = () => {
  selectedItemIds.value = []
}

const resetTypeModalForm = () => {
  typeModal.form.typeCode = ''
  typeModal.form.typeName = ''
  typeModal.form.status = 'ENABLED'
  typeModal.form.description = ''
}

const resetItemModalForm = () => {
  itemModal.form.typeCode = ''
  itemModal.form.itemCode = ''
  itemModal.form.itemLabel = ''
  itemModal.form.itemValue = ''
  itemModal.form.sort = 10
  itemModal.form.color = ''
  itemModal.form.extJson = ''
  itemModal.form.status = 'ENABLED'
}

const validateTypeForm = () => {
  const typeCode = normalizeTypeCode(typeModal.form.typeCode)
  const typeName = normalizeText(typeModal.form.typeName)
  if (!typeCode) {
    message.warning('请输入类型编码')
    return false
  }
  if (!/^[a-z0-9_]+$/.test(typeCode)) {
    message.warning('类型编码仅支持小写字母、数字和下划线')
    return false
  }
  if (!typeName) {
    message.warning('请输入类型名称')
    return false
  }
  return true
}

const validateItemForm = () => {
  const typeCode = normalizeTypeCode(itemModal.form.typeCode)
  const itemCode = normalizeText(itemModal.form.itemCode)
  const itemLabel = normalizeText(itemModal.form.itemLabel)
  const itemValue = normalizeText(itemModal.form.itemValue)

  if (!typeCode) {
    message.warning('请选择字典类型')
    return false
  }
  if (!itemCode) {
    message.warning('请输入字典项编码')
    return false
  }
  if (!/^[A-Za-z0-9_]+$/.test(itemCode)) {
    message.warning('字典项编码仅支持字母、数字和下划线')
    return false
  }
  if (!itemLabel) {
    message.warning('请输入字典项名称')
    return false
  }
  if (!itemValue) {
    message.warning('请输入字典项值')
    return false
  }
  if (!Number.isFinite(itemModal.form.sort) || !Number.isInteger(itemModal.form.sort)) {
    message.warning('排序值必须为整数')
    return false
  }
  const extJson = normalizeText(itemModal.form.extJson)
  if (extJson) {
    try {
      JSON.parse(extJson)
    } catch {
      message.warning('扩展 JSON 格式不合法')
      return false
    }
  }
  return true
}

const fetchTypeSelectOptions = async () => {
  const result = await getAdminDictTypePageApi({ page: 1, pageSize: 200 })
  typeSelectOptions.value = result.records.map((item) => ({
    label: `${item.typeName} (${item.typeCode})`,
    value: item.typeCode,
  }))
}

const fetchTypeList = async (mode: 'initial' | 'refresh' = 'refresh') => {
  typeState.errorMessage = ''
  if (mode === 'initial') typeState.loadingInitial = true
  if (mode === 'refresh') typeState.refreshing = true
  try {
    const data = await getAdminDictTypePageApi({
      page: typeState.page,
      pageSize: typeState.pageSize,
      keyword: typeFilters.keyword || undefined,
      status: typeFilters.status || undefined,
    })
    typeState.records = data.records
    typeState.total = data.total
    typeState.page = data.page
    typeState.pageSize = data.pageSize
    if (typeState.total > 0 && typeState.records.length === 0 && typeState.page > 1) {
      typeState.page -= 1
      await fetchTypeList('refresh')
      return
    }
    await fetchTypeSelectOptions()
  } catch (error) {
    typeState.errorMessage = error instanceof Error ? error.message : '字典类型加载失败，请稍后重试'
  } finally {
    typeState.loadingInitial = false
    typeState.refreshing = false
  }
}

const fetchItemList = async (mode: 'initial' | 'refresh' = 'refresh') => {
  itemState.errorMessage = ''
  if (mode === 'initial') itemState.loadingInitial = true
  if (mode === 'refresh') itemState.refreshing = true
  try {
    const data = await getAdminDictItemPageApi({
      page: itemState.page,
      pageSize: itemState.pageSize,
      typeCode: itemFilters.typeCode || undefined,
      keyword: itemFilters.keyword || undefined,
      status: itemFilters.status || undefined,
    })
    itemState.records = data.records
    const currentIds = new Set(data.records.map((item) => item.id))
    selectedItemIds.value = selectedItemIds.value.filter((id) => currentIds.has(id))
    itemState.total = data.total
    itemState.page = data.page
    itemState.pageSize = data.pageSize
    if (itemState.total > 0 && itemState.records.length === 0 && itemState.page > 1) {
      itemState.page -= 1
      await fetchItemList('refresh')
    }
  } catch (error) {
    itemState.errorMessage = error instanceof Error ? error.message : '字典项加载失败，请稍后重试'
  } finally {
    itemState.loadingInitial = false
    itemState.refreshing = false
  }
}

const onRefreshClick = async () => {
  if (activeTab.value === 'type') {
    await fetchTypeList('refresh')
    if (!typeState.errorMessage) message.success('字典类型已刷新')
  } else {
    await fetchItemList('refresh')
    if (!itemState.errorMessage) message.success('字典项已刷新')
  }
}

const onRetryClick = async () => {
  if (activeTab.value === 'type') {
    await fetchTypeList('refresh')
  } else {
    await fetchItemList('refresh')
  }
}

const onSearchClick = async () => {
  if (activeTab.value === 'type') {
    typeState.page = 1
    await fetchTypeList('refresh')
  } else {
    itemState.page = 1
    clearSelectedItems()
    await fetchItemList('refresh')
  }
}

const onResetClick = async () => {
  if (activeTab.value === 'type') {
    typeFilters.keyword = ''
    typeFilters.status = ''
    typeState.page = 1
    await fetchTypeList('refresh')
  } else {
    itemFilters.typeCode = ''
    itemFilters.keyword = ''
    itemFilters.status = ''
    itemState.page = 1
    clearSelectedItems()
    await fetchItemList('refresh')
  }
}

const onTypePageChange = async (page: number, pageSize: number) => {
  typeState.page = page
  if (pageSize > 0) typeState.pageSize = pageSize
  await fetchTypeList('refresh')
}

const onItemPageChange = async (page: number, pageSize: number) => {
  itemState.page = page
  if (pageSize > 0) itemState.pageSize = pageSize
  await fetchItemList('refresh')
}

const onOpenCreateTypeModal = () => {
  typeModal.mode = 'create'
  typeModal.editingId = ''
  resetTypeModalForm()
  typeModal.open = true
}

const onOpenEditTypeModal = async (record: AdminDictTypeItem) => {
  try {
    const detail = await getAdminDictTypeDetailApi(record.id)
    typeModal.mode = 'edit'
    typeModal.editingId = detail.id
    typeModal.form.typeCode = detail.typeCode
    typeModal.form.typeName = detail.typeName
    typeModal.form.status = detail.status
    typeModal.form.description = detail.description || ''
    typeModal.open = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '字典类型详情加载失败，请稍后重试')
  }
}

const onSubmitType = async () => {
  if (!validateTypeForm()) return
  try {
    typeState.submitting = true
    const payload = {
      typeName: normalizeText(typeModal.form.typeName),
      status: typeModal.form.status,
      description: normalizeOptionalText(typeModal.form.description),
    }
    if (typeModal.mode === 'create') {
      await createAdminDictTypeApi({
        typeCode: normalizeTypeCode(typeModal.form.typeCode),
        ...payload,
      })
      message.success('字典类型创建成功')
    } else {
      await updateAdminDictTypeApi(typeModal.editingId, payload)
      message.success('字典类型更新成功')
    }
    typeModal.open = false
    await fetchTypeList('refresh')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '提交失败，请稍后重试')
  } finally {
    typeState.submitting = false
  }
}

const onToggleTypeStatus = (record: AdminDictTypeItem) => {
  const nextStatus: DictStatus = record.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const actionText = nextStatus === 'ENABLED' ? '启用' : '禁用'
  Modal.confirm({
    title: `确认${actionText}该字典类型？`,
    content: `类型：${record.typeName} (${record.typeCode})`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await updateAdminDictTypeStatusApi(record.id, nextStatus)
        await fetchTypeList('refresh')
        if (activeTab.value === 'item') {
          await fetchItemList('refresh')
        }
        message.success(`字典类型已${actionText}`)
      } catch (error) {
        message.error(error instanceof Error ? error.message : '状态更新失败，请稍后重试')
      }
    },
  })
}

const onDeleteType = (record: AdminDictTypeItem) => {
  Modal.confirm({
    title: '确认删除该字典类型？',
    content: `类型：${record.typeName} (${record.typeCode})`,
    okText: '确认删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        await deleteAdminDictTypeApi(record.id)
        if (itemFilters.typeCode === record.typeCode) {
          itemFilters.typeCode = ''
        }
        await fetchTypeList('refresh')
        await fetchItemList('refresh')
        message.success('字典类型已删除')
      } catch (error) {
        message.error(error instanceof Error ? error.message : '删除失败，请稍后重试')
      }
    },
  })
}

const onOpenCreateItemModal = () => {
  if (dictTypeFormOptions.value.length === 0) {
    message.warning('请先创建字典类型')
    return
  }
  itemModal.mode = 'create'
  itemModal.editingId = ''
  resetItemModalForm()
  itemModal.form.typeCode = itemFilters.typeCode || dictTypeFormOptions.value[0].value
  itemModal.open = true
}

const onOpenEditItemModal = async (record: AdminDictItem) => {
  try {
    const detail = await getAdminDictItemDetailApi(record.id)
    itemModal.mode = 'edit'
    itemModal.editingId = detail.id
    itemModal.form.typeCode = detail.typeCode
    itemModal.form.itemCode = detail.itemCode
    itemModal.form.itemLabel = detail.itemLabel
    itemModal.form.itemValue = detail.itemValue
    itemModal.form.sort = detail.sort
    itemModal.form.color = detail.color || ''
    itemModal.form.extJson = detail.extJson || ''
    itemModal.form.status = detail.status
    itemModal.open = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '字典项详情加载失败，请稍后重试')
  }
}

const onSubmitItem = async () => {
  if (!validateItemForm()) return
  try {
    itemState.submitting = true
    const payload = {
      itemLabel: normalizeText(itemModal.form.itemLabel),
      itemValue: normalizeText(itemModal.form.itemValue),
      sort: itemModal.form.sort,
      color: normalizeOptionalText(itemModal.form.color),
      extJson: normalizeOptionalText(itemModal.form.extJson),
      status: itemModal.form.status,
    }
    if (itemModal.mode === 'create') {
      await createAdminDictItemApi({
        typeCode: normalizeTypeCode(itemModal.form.typeCode),
        itemCode: normalizeText(itemModal.form.itemCode),
        ...payload,
      })
      message.success('字典项创建成功')
    } else {
      await updateAdminDictItemApi(itemModal.editingId, payload)
      message.success('字典项更新成功')
    }
    itemModal.open = false
    await fetchItemList('refresh')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '提交失败，请稍后重试')
  } finally {
    itemState.submitting = false
  }
}

const onToggleItemStatus = (record: AdminDictItem) => {
  const nextStatus: DictStatus = record.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const actionText = nextStatus === 'ENABLED' ? '启用' : '禁用'
  Modal.confirm({
    title: `确认${actionText}该字典项？`,
    content: `字典项：${record.itemLabel} (${record.itemCode})`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await updateAdminDictItemStatusApi(record.id, nextStatus)
        await fetchItemList('refresh')
        message.success(`字典项已${actionText}`)
      } catch (error) {
        message.error(error instanceof Error ? error.message : '状态更新失败，请稍后重试')
      }
    },
  })
}

const ensureSelectedItemIds = () => {
  if (selectedItemIds.value.length === 0) {
    message.warning('请先选择字典项')
    return false
  }
  return true
}

const onBatchUpdateItemStatus = (status: DictStatus) => {
  if (!ensureSelectedItemIds()) return
  const actionText = status === 'ENABLED' ? '启用' : '禁用'
  Modal.confirm({
    title: `确认批量${actionText}已选字典项？`,
    content: `已选 ${selectedItemIds.value.length} 项`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await batchUpdateAdminDictItemStatusApi(selectedItemIds.value, status)
        await fetchItemList('refresh')
        clearSelectedItems()
        message.success(`已批量${actionText}`)
      } catch (error) {
        message.error(error instanceof Error ? error.message : '批量状态更新失败，请稍后重试')
      }
    },
  })
}

const onBatchDeleteItems = () => {
  if (!ensureSelectedItemIds()) return
  Modal.confirm({
    title: '确认批量删除已选字典项？',
    content: `已选 ${selectedItemIds.value.length} 项，删除后不可恢复`,
    okText: '确认删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        await batchDeleteAdminDictItemsApi(selectedItemIds.value)
        await fetchItemList('refresh')
        clearSelectedItems()
        message.success('批量删除成功')
      } catch (error) {
        message.error(error instanceof Error ? error.message : '批量删除失败，请稍后重试')
      }
    },
  })
}

const onBatchAdjustItemSort = () => {
  if (!ensureSelectedItemIds()) return
  if (!Number.isInteger(itemBatchSortDelta.value) || itemBatchSortDelta.value === 0) {
    message.warning('请输入非0整数作为排序增量')
    return
  }
  Modal.confirm({
    title: '确认批量调整已选字典项排序？',
    content: `已选 ${selectedItemIds.value.length} 项，排序增量 ${itemBatchSortDelta.value}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await batchAdjustAdminDictItemSortApi(selectedItemIds.value, itemBatchSortDelta.value)
        await fetchItemList('refresh')
        clearSelectedItems()
        message.success('批量排序已调整')
      } catch (error) {
        message.error(error instanceof Error ? error.message : '批量排序调整失败，请稍后重试')
      }
    },
  })
}

const onDeleteItem = (record: AdminDictItem) => {
  Modal.confirm({
    title: '确认删除该字典项？',
    content: `字典项：${record.itemLabel} (${record.itemCode})`,
    okText: '确认删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        await deleteAdminDictItemApi(record.id)
        await fetchItemList('refresh')
        message.success('字典项已删除')
      } catch (error) {
        message.error(error instanceof Error ? error.message : '删除失败，请稍后重试')
      }
    },
  })
}

const onMoveItemSort = async (record: AdminDictItem, delta: number) => {
  const nextSort = record.sort + delta
  try {
    await updateAdminDictItemSortApi(record.id, nextSort)
    await fetchItemList('refresh')
    message.success('排序已更新')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '排序更新失败，请稍后重试')
  }
}

watch(activeTab, async (tab) => {
  if (tab === 'type') {
    clearSelectedItems()
  }
  if (tab === 'item' && itemState.records.length === 0 && !itemState.loadingInitial) {
    await fetchItemList('initial')
  }
})

onMounted(() => {
  void (async () => {
    await fetchTypeList('initial')
  })()
})
</script>

<style scoped>
.dict-page {
  display: grid;
  gap: 16px;
}

.dict-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f0fdf4 0%, #f0f9ff 58%, #f8fafc 100%);
  border: 1px solid #d7ece0;
}

.dict-title {
  margin: 0;
  font-size: 24px;
  color: #14532d;
}

.dict-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.dict-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dict-error {
  margin-top: -6px;
}

.dict-card {
  border-radius: 14px;
  border: 1px solid #dbe9f2;
}

.dict-batch-toolbar {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px dashed #b7d7c3;
  background: #f8fffb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.dict-batch-toolbar__summary {
  font-size: 13px;
  color: #166534;
  white-space: nowrap;
}

.dict-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.dict-full-width {
  width: 100%;
}

@media (max-width: 1100px) {
  .dict-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .dict-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .dict-batch-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .dict-pagination {
    justify-content: center;
  }
}
</style>
