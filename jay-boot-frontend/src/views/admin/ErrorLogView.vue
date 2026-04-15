<template>
  <section class="log-page">
    <header class="log-toolbar">
      <div>
        <h2 class="log-title">异常日志</h2>
        <p class="log-subtitle">查看系统异常错误记录，包含完整堆栈信息便于排查问题。</p>
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
                placeholder="异常类名或消息"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="8">
            <a-form-item label="请求路径">
              <a-input
                v-model:value="filters.requestPath"
                allow-clear
                placeholder="输入请求路径"
                @pressEnter="onSearchClick"
              />
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
          :scroll="{ x: 1200 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'requestId'">
              <a-typography-text copyable style="max-width: 100px">{{ record.requestId || '-' }}</a-typography-text>
            </template>
            <template v-if="column.key === 'requestPath'">
              <a-tooltip :title="record.requestPath">
                <span style="max-width: 200px; display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ record.requestPath || '-' }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'exceptionClass'">
              <a-tooltip :title="record.exceptionClass">
                <span style="max-width: 200px; display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ getSimpleClassName(record.exceptionClass) }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'exceptionMessage'">
              <a-tooltip :title="record.exceptionMessage">
                <span style="max-width: 200px; display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ record.exceptionMessage || '-' }}</span>
              </a-tooltip>
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

    <a-modal v-model:open="detailModal.open" title="异常日志详情" :footer="null" width="900px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="请求ID">{{ detailModal.data?.requestId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="用户">{{ detailModal.data?.username || '-' }}</a-descriptions-item>
        <a-descriptions-item label="请求路径" :span="2">{{ detailModal.data?.requestPath || '-' }}</a-descriptions-item>
        <a-descriptions-item label="异常类名" :span="2">{{ detailModal.data?.exceptionClass }}</a-descriptions-item>
        <a-descriptions-item label="异常消息" :span="2">{{ detailModal.data?.exceptionMessage || '-' }}</a-descriptions-item>
        <a-descriptions-item label="客户端IP">{{ detailModal.data?.clientIp || '-' }}</a-descriptions-item>
        <a-descriptions-item label="发生时间">{{ formatDateTime(detailModal.data?.createdTime) }}</a-descriptions-item>
        <a-descriptions-item label="请求参数" :span="2">
          <pre style="margin: 0; white-space: pre-wrap; word-break: break-all; max-height: 150px; overflow: auto;">{{ formatJson(detailModal.data?.requestParams) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
      
      <div style="margin-top: 16px;">
        <div style="font-weight: 500; margin-bottom: 8px;">堆栈信息</div>
        <pre style="margin: 0; padding: 12px; background: #f5f5f5; border-radius: 6px; white-space: pre-wrap; word-break: break-all; max-height: 400px; overflow: auto; font-size: 12px;">{{ detailModal.data?.stackTrace || '-' }}</pre>
      </div>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import type { ErrorLogItem } from '../../api/admin/LogApi'
import { useErrorLogStore } from '../../stores/admin/errorLog'

const logStore = useErrorLogStore()

const filters = reactive({
  keyword: '',
  requestPath: '',
})

const detailModal = reactive({
  open: false,
  data: null as ErrorLogItem | null,
})

const tableColumns = [
  { title: '请求ID', dataIndex: 'requestId', key: 'requestId', width: 120 },
  { title: '请求路径', dataIndex: 'requestPath', key: 'requestPath', width: 200 },
  { title: '异常类型', dataIndex: 'exceptionClass', key: 'exceptionClass', width: 180 },
  { title: '异常消息', dataIndex: 'exceptionMessage', key: 'exceptionMessage', width: 200 },
  { title: '用户', dataIndex: 'username', key: 'username', width: 100 },
  { title: '时间', dataIndex: 'createdTime', key: 'createdTime', width: 170 },
  { title: '操作', key: 'actions', width: 120 },
]

const tableLoading = computed(() => logStore.loadingInitial || logStore.refreshing)

const getSimpleClassName = (fullClassName: string) => {
  if (!fullClassName) return '-'
  const parts = fullClassName.split('.')
  return parts[parts.length - 1]
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
    message.success('异常日志已刷新')
  }
}

const onRetryClick = async () => {
  await logStore.fetchList('refresh')
}

const onSearchClick = async () => {
  logStore.setFilters({
    keyword: filters.keyword.trim(),
    requestPath: filters.requestPath.trim(),
    userId: '',
    startTime: '',
    endTime: '',
  })
  await logStore.searchWithCurrentFilters()
}

const onResetClick = async () => {
  filters.keyword = ''
  filters.requestPath = ''
  logStore.resetFilters()
  await logStore.fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  await logStore.changePage(page, pageSize)
}

const onViewDetail = (record: ErrorLogItem) => {
  detailModal.data = record
  detailModal.open = true
}

const onDeleteOne = (record: ErrorLogItem) => {
  Modal.confirm({
    title: '确认删除该异常日志？',
    content: `异常类型: ${getSimpleClassName(record.exceptionClass)}`,
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
  background: linear-gradient(130deg, #fef2f2 0%, #fff1f2 56%, #f8fafc 100%);
  border: 1px solid #fecaca;
}

.log-title {
  margin: 0;
  font-size: 24px;
  color: #7f1d1d;
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
