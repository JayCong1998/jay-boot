<template>
  <section class="log-page">
    <header class="log-toolbar">
      <div>
        <h2 class="log-title">请求日志</h2>
        <p class="log-subtitle">查看系统所有接口请求记录，支持筛选检索与详情查看。</p>
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
          <a-col :xs="24" :md="12" :lg="8">
            <a-form-item label="关键词">
              <a-input
                v-model:value="filters.keyword"
                allow-clear
                placeholder="输入请求路径"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
            <a-form-item label="请求方法">
              <a-select v-model:value="filters.method" :options="methodOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
            <a-form-item label="状态码">
              <a-input-number v-model:value="filters.statusCode" :min="100" :max="599" placeholder="状态码" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :lg="8">
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
      <a-skeleton v-if="pageState.loadingInitial && !pageState.hasData" active :paragraph="{ rows: 6 }" />
      <template v-else>
        <a-table
          :columns="tableColumns"
          :data-source="pageState.records"
          :loading="tableLoading"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 1100 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'requestId'">
              <a-typography-text copyable style="max-width: 100px">{{ record.requestId }}</a-typography-text>
            </template>
            <template v-else-if="column.key === 'path'">
              <a-tooltip :title="record.path">
                <span style="max-width: 200px; display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ record.path }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'method'">
              <a-tag :color="getMethodColor(record.method)">{{ record.method }}</a-tag>
            </template>
            <template v-else-if="column.key === 'statusCode'">
              <a-tag :color="getStatusColor(record.statusCode)">{{ record.statusCode }}</a-tag>
            </template>
            <template v-else-if="column.key === 'durationMs'">
              <span :style="{ color: record.durationMs > 1000 ? '#ff4d4f' : 'inherit' }">{{ record.durationMs }}ms</span>
            </template>
            <template v-else-if="column.key === 'createdTime'">
              {{ formatDateTime(record.createdTime) }}
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="onViewDetail(record)">详情</a-button>
                <a-button type="link" size="small" danger @click="onDeleteOne(record)">删除</a-button>
              </a-space>
            </template>
          </template>
        </a-table>

        <div class="log-pagination">
          <a-pagination
            :current="pageState.page"
            :page-size="pageState.pageSize"
            :total="pageState.total"
            :show-total="(total: number) => `共 ${total} 条`"
            show-size-changer
            :page-size-options="['20', '50', '100']"
            @change="onPageChange"
          />
        </div>
      </template>
    </a-card>

    <a-modal v-model:open="detailModal.open" title="请求日志详情" :footer="null" width="800px">
      <a-spin :spinning="detailModal.loading">
        <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="请求ID">{{ detailModal.data?.requestId }}</a-descriptions-item>
        <a-descriptions-item label="请求方法">{{ detailModal.data?.method }}</a-descriptions-item>
        <a-descriptions-item label="请求路径" :span="2">{{ detailModal.data?.path }}</a-descriptions-item>
        <a-descriptions-item label="查询参数" :span="2">{{ detailModal.data?.queryString || '-' }}</a-descriptions-item>
        <a-descriptions-item label="请求参数" :span="2">
          <pre style="margin: 0; white-space: pre-wrap; word-break: break-all; max-height: 200px; overflow: auto;">{{ formatJson(detailModal.data?.requestParams) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="状态码">{{ detailModal.data?.statusCode }}</a-descriptions-item>
        <a-descriptions-item label="耗时">{{ detailModal.data?.durationMs }}ms</a-descriptions-item>
        <a-descriptions-item label="用户">{{ detailModal.data?.username || '-' }}</a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ detailModal.data?.userId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="客户端IP">{{ detailModal.data?.clientIp || '-' }}</a-descriptions-item>
        <a-descriptions-item label="请求时间">{{ formatDateTime(detailModal.data?.createdTime) }}</a-descriptions-item>
        <a-descriptions-item label="User-Agent" :span="2">{{ detailModal.data?.userAgent || '-' }}</a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import {
  getRequestLogDetailApi,
  getRequestLogPageApi,
  deleteRequestLogApi,
  type RequestLogItem,
  type RequestLogPageParams,
} from '../../api/admin/LogApi'

// 页面状态
const pageState = reactive({
  records: [] as RequestLogItem[],
  total: 0,
  page: 1,
  pageSize: 20,
  loadingInitial: false,
  refreshing: false,
  errorMessage: '',
  hasData: computed(() => pageState.records.length > 0),
})

// 筛选条件
const filters = reactive({
  keyword: '',
  method: '' as '' | 'GET' | 'POST' | 'PUT' | 'DELETE',
  statusCode: null as number | null,
})

// 详情弹窗
const detailModal = reactive({
  open: false,
  loading: false,
  data: null as RequestLogItem | null,
})

const tableColumns = [
  { title: '请求ID', dataIndex: 'requestId', key: 'requestId', width: 120 },
  { title: '路径', dataIndex: 'path', key: 'path', width: 200 },
  { title: '方法', dataIndex: 'method', key: 'method', width: 80 },
  { title: '状态码', dataIndex: 'statusCode', key: 'statusCode', width: 90 },
  { title: '耗时', dataIndex: 'durationMs', key: 'durationMs', width: 100 },
  { title: '用户', dataIndex: 'username', key: 'username', width: 100 },
  { title: '时间', dataIndex: 'createdTime', key: 'createdTime', width: 170 },
  { title: '操作', key: 'actions', width: 120 },
]

const methodOptions = [
  { label: '全部方法', value: '' },
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
]

const tableLoading = computed(() => pageState.loadingInitial || pageState.refreshing)

const getMethodColor = (method: string) => {
  const colors: Record<string, string> = {
    GET: 'green',
    POST: 'blue',
    PUT: 'orange',
    DELETE: 'red',
  }
  return colors[method] || 'default'
}

const getStatusColor = (status: number) => {
  if (status >= 200 && status < 300) return 'success'
  if (status >= 400 && status < 500) return 'warning'
  if (status >= 500) return 'error'
  return 'default'
}

const formatDateTime = (value: string | null | undefined) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const formatJson = (value: string | null | undefined) => {
  if (!value) return '-'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

// 获取列表
const fetchList = async (mode: 'initial' | 'refresh' = 'refresh') => {
  pageState.errorMessage = ''
  if (mode === 'initial') pageState.loadingInitial = true
  if (mode === 'refresh') pageState.refreshing = true

  try {
    const params: RequestLogPageParams = {
      page: pageState.page,
      pageSize: pageState.pageSize,
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.method) params.method = filters.method
    if (filters.statusCode !== null) params.statusCode = filters.statusCode

    const data = await getRequestLogPageApi(params)
    pageState.records = data.records
    pageState.total = data.total
    pageState.page = data.page
    pageState.pageSize = data.pageSize

    // 自动翻页处理
    if (pageState.total > 0 && pageState.records.length === 0 && pageState.page > 1) {
      pageState.page -= 1
      await fetchList('refresh')
    }
  } catch (error) {
    pageState.errorMessage = error instanceof Error ? error.message : '请求日志加载失败，请稍后重试'
  } finally {
    pageState.loadingInitial = false
    pageState.refreshing = false
  }
}

const onRefreshClick = async () => {
  await fetchList('refresh')
  if (!pageState.errorMessage) {
    message.success('请求日志已刷新')
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
  filters.keyword = ''
  filters.method = ''
  filters.statusCode = null
  pageState.page = 1
  await fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  pageState.page = page
  if (pageSize > 0) pageState.pageSize = pageSize
  await fetchList('refresh')
}

const onViewDetail = async (record: RequestLogItem) => {
  detailModal.open = true
  detailModal.loading = true
  detailModal.data = null
  try {
    detailModal.data = await getRequestLogDetailApi(record.id)
  } catch (error) {
    detailModal.open = false
    message.error(error instanceof Error ? error.message : '请求日志详情加载失败，请稍后重试')
  } finally {
    detailModal.loading = false
  }
}

const onDeleteOne = (record: RequestLogItem) => {
  Modal.confirm({
    title: '确认删除该请求日志？',
    content: `请求ID: ${record.requestId}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await deleteRequestLogApi(record.id)
        await fetchList('refresh')
        message.success('删除成功')
      } catch (error) {
        message.error(error instanceof Error ? error.message : '删除失败，请稍后重试')
      }
    },
  })
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
