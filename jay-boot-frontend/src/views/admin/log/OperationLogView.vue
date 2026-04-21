<template>
  <section class="log-page">
    <header class="log-toolbar">
      <div>
        <h2 class="log-title">操作日志</h2>
        <p class="log-subtitle">查看用户关键业务操作记录，支持按模块、用户和时间范围筛选。</p>
      </div>
      <div class="log-toolbar__actions">
        <a-button :loading="pageState.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </div>
    </header>

    <a-alert v-if="pageState.errorMessage" class="log-error" type="error" show-icon :message="pageState.errorMessage">
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-card class="log-card" :bordered="false">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="8" :lg="6">
            <a-form-item label="模块">
              <a-input
                v-model:value="filters.module"
                allow-clear
                placeholder="请输入模块名"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8" :lg="6">
            <a-form-item label="操作用户ID">
              <a-input-number
                v-model:value="filters.userId"
                style="width: 100%"
                :min="1"
                :precision="0"
                placeholder="请输入用户ID"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8" :lg="6">
            <a-form-item label="时间范围">
              <a-range-picker
                v-model:value="filters.dateRange"
                style="width: 100%"
                :allow-clear="true"
                :placeholder="['开始日期', '结束日期']"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="24" :lg="6">
            <a-form-item label=" ">
              <a-space>
                <a-button type="primary" :loading="pageState.refreshing" @click="onSearchClick">查询</a-button>
                <a-button @click="onResetClick">重置</a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <a-card class="log-card" :bordered="false">
      <a-skeleton v-if="pageState.loadingInitial && !hasData" active :paragraph="{ rows: 6 }" />
      <template v-else>
        <a-table
          :columns="tableColumns"
          :data-source="pageState.records"
          :loading="tableLoading"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 1000 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'module'">
              <a-tag color="blue">{{ record.module }}</a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-tag color="green">{{ record.action }}</a-tag>
            </template>
            <template v-else-if="column.key === 'detail'">
              <a-tooltip :title="record.detail">
                <span class="detail-text">{{ record.detail || '-' }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'createdTime'">
              {{ formatDateTime(record.createdTime) }}
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-button type="link" size="small" @click="onViewDetail(record)">详情</a-button>
            </template>
          </template>
        </a-table>

        <div class="log-pagination">
          <a-pagination
            :current="pageState.page"
            :page-size="pageState.pageSize"
            :total="pageState.total"
            :show-total="showTotal"
            show-size-changer
            :page-size-options="['20', '50', '100']"
            @change="onPageChange"
          />
        </div>
      </template>
    </a-card>

    <a-modal v-model:open="detailModal.open" title="操作日志详情" :footer="null" width="720px" @cancel="onCloseDetail">
      <a-spin :spinning="detailModal.loading">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="模块">{{ detailModal.data?.module || '-' }}</a-descriptions-item>
          <a-descriptions-item label="操作">{{ detailModal.data?.action || '-' }}</a-descriptions-item>
          <a-descriptions-item label="详情" :span="2">
            <pre class="detail-pre">{{ detailModal.data?.detail || '-' }}</pre>
          </a-descriptions-item>
          <a-descriptions-item label="操作用户">{{ detailModal.data?.username || '-' }}</a-descriptions-item>
          <a-descriptions-item label="用户ID">{{ detailModal.data?.userId ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="客户端IP">{{ detailModal.data?.clientIp || '-' }}</a-descriptions-item>
          <a-descriptions-item label="请求ID">{{ detailModal.data?.requestId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="操作时间" :span="2">
            {{ formatDateTime(detailModal.data?.createdTime) }}
          </a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import type { Dayjs } from 'dayjs'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import {
  getOperationLogDetailApi,
  getOperationLogPageApi,
  type OperationLogItem,
  type OperationLogPageParams,
} from '../../../api/admin/LogApi'

const pageState = reactive({
  records: [] as OperationLogItem[],
  total: 0,
  page: 1,
  pageSize: 20,
  loadingInitial: false,
  refreshing: false,
  errorMessage: '',
})

const filters = reactive({
  module: '',
  userId: null as number | null,
  dateRange: null as [Dayjs, Dayjs] | null,
})

const detailModal = reactive({
  open: false,
  loading: false,
  data: null as OperationLogItem | null,
})

const hasData = computed(() => pageState.records.length > 0)
const tableLoading = computed(() => pageState.loadingInitial || pageState.refreshing)
const showTotal = (total: number) => `共 ${total} 条`

const tableColumns = [
  { title: '模块', dataIndex: 'module', key: 'module', width: 130 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 140 },
  { title: '详情', dataIndex: 'detail', key: 'detail', width: 280 },
  { title: '操作用户', dataIndex: 'username', key: 'username', width: 130 },
  { title: '客户端IP', dataIndex: 'clientIp', key: 'clientIp', width: 140 },
  { title: '操作时间', dataIndex: 'createdTime', key: 'createdTime', width: 170 },
  { title: '操作', key: 'actions', width: 90, fixed: 'right' },
]

const formatDateTime = (value: string | null | undefined) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const fetchList = async (mode: 'initial' | 'refresh' = 'refresh') => {
  pageState.errorMessage = ''
  if (mode === 'initial') pageState.loadingInitial = true
  if (mode === 'refresh') pageState.refreshing = true

  try {
    const params: OperationLogPageParams = {
      page: pageState.page,
      pageSize: pageState.pageSize,
    }
    if (filters.module.trim()) params.module = filters.module.trim()
    if (filters.userId !== null) params.userId = filters.userId
    if (filters.dateRange) {
      params.startTime = filters.dateRange[0].format('YYYY-MM-DD 00:00:00')
      params.endTime = filters.dateRange[1].format('YYYY-MM-DD 23:59:59')
    }

    const data = await getOperationLogPageApi(params)
    pageState.records = data.records
    pageState.total = data.total
    pageState.page = data.page
    pageState.pageSize = data.pageSize

    if (pageState.total > 0 && pageState.records.length === 0 && pageState.page > 1) {
      pageState.page -= 1
      await fetchList('refresh')
    }
  } catch (error) {
    pageState.errorMessage = error instanceof Error ? error.message : '操作日志加载失败，请稍后重试'
  } finally {
    pageState.loadingInitial = false
    pageState.refreshing = false
  }
}

const onRefreshClick = async () => {
  await fetchList('refresh')
  if (!pageState.errorMessage) {
    message.success('操作日志已刷新')
  }
}

const onRetryClick = async () => {
  await fetchList('refresh')
}

const onSearchClick = async () => {
  pageState.page = 1
  await fetchList('refresh')
}

const onResetClick = async () => {
  filters.module = ''
  filters.userId = null
  filters.dateRange = null
  pageState.page = 1
  await fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  pageState.page = page
  if (pageSize > 0) pageState.pageSize = pageSize
  await fetchList('refresh')
}

const onCloseDetail = () => {
  detailModal.data = null
}

const onViewDetail = async (record: OperationLogItem) => {
  detailModal.open = true
  detailModal.loading = true
  detailModal.data = null
  try {
    const detail = await getOperationLogDetailApi(record.id)
    if (!detail) {
      message.warning('未找到该日志详情')
      detailModal.open = false
      return
    }
    detailModal.data = detail
  } catch (error) {
    detailModal.open = false
    message.error(error instanceof Error ? error.message : '操作日志详情加载失败，请稍后重试')
  } finally {
    detailModal.loading = false
  }
}

onMounted(() => {
  void fetchList('initial')
})
</script>

<style scoped>
.log-page {
  display: grid;
  gap: 16px;
}

.log-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #eef2ff 0%, #f5f3ff 56%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.log-title {
  margin: 0;
  font-size: 24px;
  color: #312e81;
}

.log-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.log-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.log-error {
  margin-top: -6px;
}

.log-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.log-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.detail-text {
  max-width: 260px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 220px;
  overflow: auto;
}

@media (max-width: 1100px) {
  .log-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .log-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .log-pagination {
    justify-content: center;
  }
}
</style>
