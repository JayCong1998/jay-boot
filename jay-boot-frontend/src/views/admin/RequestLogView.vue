<template>
  <section class="log-page">
    <header class="log-toolbar">
      <div>
        <h2 class="log-title">请求日志</h2>
        <p class="log-subtitle">查看系统所有接口请求记录，支持筛选检索与详情查看。</p>
      </div>
      <div class="log-toolbar__actions">
        <a-button :loading="logStore.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </div>
    </header>

    <a-alert v-if="logStore.errorMessage" class="log-error" type="error" show-icon :message="logStore.errorMessage">
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
                <a-button type="primary" :loading="logStore.refreshing" @click="onSearchClick">查询</a-button>
                <a-button @click="onResetClick">重置</a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <a-card class="log-card" :bordered="false">
      <a-skeleton v-if="logStore.loadingInitial && !logStore.hasData" active :paragraph="{ rows: 6 }" />
      <template v-else>
        <a-table
          :columns="tableColumns"
          :data-source="logStore.records"
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
            :current="logStore.page"
            :page-size="logStore.pageSize"
            :total="logStore.total"
            :show-total="(total: number) => `共 ${total} 条`"
            show-size-changer
            :page-size-options="['20', '50', '100']"
            @change="onPageChange"
          />
        </div>
      </template>
    </a-card>

    <a-modal v-model:open="detailModal.open" title="请求日志详情" :footer="null" width="800px">
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
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import type { RequestLogItem } from '../../api/admin/LogApi'
import { useRequestLogStore } from '../../stores/admin/requestLog'

const logStore = useRequestLogStore()

const filters = reactive({
  keyword: '',
  method: '' as '' | 'GET' | 'POST' | 'PUT' | 'DELETE',
  statusCode: null as number | null,
})

const detailModal = reactive({
  open: false,
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

const tableLoading = computed(() => logStore.loadingInitial || logStore.refreshing)

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

const onRefreshClick = async () => {
  await logStore.fetchList('refresh')
  if (!logStore.errorMessage) {
    message.success('请求日志已刷新')
  }
}

const onRetryClick = async () => {
  await logStore.fetchList('refresh')
}

const onSearchClick = async () => {
  logStore.setFilters({
    keyword: filters.keyword.trim(),
    method: filters.method,
    statusCode: filters.statusCode,
    userId: '',
    startTime: '',
    endTime: '',
  })
  await logStore.searchWithCurrentFilters()
}

const onResetClick = async () => {
  filters.keyword = ''
  filters.method = ''
  filters.statusCode = null
  logStore.resetFilters()
  await logStore.fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  await logStore.changePage(page, pageSize)
}

const onViewDetail = (record: RequestLogItem) => {
  detailModal.data = record
  detailModal.open = true
}

const onDeleteOne = (record: RequestLogItem) => {
  Modal.confirm({
    title: '确认删除该请求日志？',
    content: `请求ID: ${record.requestId}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await logStore.deleteOne(record.id)
        await logStore.fetchList('refresh')
        message.success('删除成功')
      } catch (error) {
        if (error instanceof Error) {
          message.error(error.message)
        } else {
          message.error('删除失败，请稍后重试')
        }
      }
    },
  })
}

onMounted(async () => {
  await logStore.initialize()
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
