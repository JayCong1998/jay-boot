<template>
  <section class="mail-log-page">
    <header class="mail-log-toolbar">
      <div>
        <h2 class="mail-log-title">邮件发送日志</h2>
        <p class="mail-log-subtitle">查询邮件发送过程和失败原因，支持失败记录一键重试。</p>
      </div>
      <div class="mail-log-toolbar__actions">
        <a-button :loading="pageState.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </div>
    </header>

    <a-alert
      v-if="pageState.errorMessage"
      class="mail-log-error"
      type="error"
      show-icon
      :message="pageState.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-card class="mail-log-card" :bordered="false">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12" :lg="4">
            <a-form-item label="业务类型">
              <a-select v-model:value="filters.bizType" :options="bizTypeFilterOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="4">
            <a-form-item label="状态">
              <a-select v-model:value="filters.status" :options="statusFilterOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="4">
            <a-form-item label="场景码">
              <a-input
                v-model:value="filters.sceneCode"
                allow-clear
                placeholder="例如：REGISTER"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="4">
            <a-form-item label="模板编码">
              <a-input
                v-model:value="filters.templateCode"
                allow-clear
                placeholder="例如：verify_code_register"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="4">
            <a-form-item label="收件邮箱">
              <a-input
                v-model:value="filters.recipientEmail"
                allow-clear
                placeholder="例如：foo@example.com"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12" :lg="4">
            <a-form-item label="时间范围">
              <a-range-picker
                v-model:value="filters.dateRange"
                style="width: 100%"
                :placeholder="['开始日期', '结束日期']"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24">
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

    <a-card class="mail-log-card" :bordered="false">
      <a-skeleton v-if="pageState.loadingInitial && !hasData" active :paragraph="{ rows: 6 }" />
      <template v-else>
        <a-table
          :columns="tableColumns"
          :data-source="pageState.records"
          :loading="tableLoading"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 1450 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'bizType'">
              <a-tag :color="resolveBizTypeMeta(record.bizType).color">
                {{ resolveBizTypeMeta(record.bizType).label }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="resolveStatusMeta(record.status).color">
                {{ resolveStatusMeta(record.status).label }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'createdTime'">
              {{ formatDateTime(record.createdTime) }}
            </template>
            <template v-else-if="column.key === 'sentTime'">
              {{ formatDateTime(record.sentTime) }}
            </template>
            <template v-else-if="column.key === 'recipientEmail'">
              <a-tooltip :title="record.recipientEmail">
                <span class="mail-log-ellipsis">{{ record.recipientEmail }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="onViewDetail(record)">详情</a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="record.status !== 'FAILED'"
                  :loading="pageState.retryingId === record.id"
                  @click="onRetrySend(record)"
                >
                  重试发送
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>

        <div class="mail-log-pagination">
          <a-pagination
            :current="pageState.page"
            :page-size="pageState.pageSize"
            :total="pageState.total"
            :show-total="(total: number) => `共 ${total} 条`"
            show-size-changer
            :page-size-options="['10', '20', '50']"
            @change="onPageChange"
          />
        </div>
      </template>
    </a-card>

    <a-modal v-model:open="detailModal.open" title="邮件日志详情" :footer="null" width="980px">
      <a-spin :spinning="detailModal.loading">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="日志ID">{{ detailModal.data?.id || '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag v-if="detailModal.data" :color="resolveStatusMeta(detailModal.data.status).color">
              {{ resolveStatusMeta(detailModal.data.status).label }}
            </a-tag>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="业务类型">{{ detailModal.data ? resolveBizTypeMeta(detailModal.data.bizType).label : '-' }}</a-descriptions-item>
          <a-descriptions-item label="场景码">{{ detailModal.data?.sceneCode || '-' }}</a-descriptions-item>
          <a-descriptions-item label="模板编码">{{ detailModal.data?.templateCode || '-' }}</a-descriptions-item>
          <a-descriptions-item label="通道编码">{{ detailModal.data?.channelCode || '-' }}</a-descriptions-item>
          <a-descriptions-item label="收件邮箱">{{ detailModal.data?.recipientEmail || '-' }}</a-descriptions-item>
          <a-descriptions-item label="幂等键">{{ detailModal.data?.bizKey || '-' }}</a-descriptions-item>
          <a-descriptions-item label="追踪ID">{{ detailModal.data?.traceId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="错误码">{{ detailModal.data?.errorCode || '-' }}</a-descriptions-item>
          <a-descriptions-item label="错误信息" :span="2">{{ detailModal.data?.errorMessage || '-' }}</a-descriptions-item>
          <a-descriptions-item label="重试次数">{{ detailModal.data?.retryCount ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="最大重试">{{ detailModal.data?.maxRetryCount ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="下次重试时间">{{ formatDateTime(detailModal.data?.nextRetryTime) }}</a-descriptions-item>
          <a-descriptions-item label="发送时间">{{ formatDateTime(detailModal.data?.sentTime) }}</a-descriptions-item>
          <a-descriptions-item label="创建时间" :span="2">{{ formatDateTime(detailModal.data?.createdTime) }}</a-descriptions-item>
          <a-descriptions-item label="渲染主题" :span="2">{{ detailModal.data?.subjectRendered || '-' }}</a-descriptions-item>
          <a-descriptions-item label="渲染正文" :span="2">
            <pre class="mail-log-body">{{ detailModal.data?.bodyRendered || '-' }}</pre>
          </a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import {
  getMailSendLogDetailApi,
  getMailSendLogPageApi,
  retryMailSendLogApi,
  type MailBizType,
  type MailSendLogItem,
  type MailSendStatus,
} from '../../../api/admin/MailApi'

type TagMeta = { label: string; color: string }

const BIZ_TYPE_META: Record<MailBizType, TagMeta> = {
  VERIFY_CODE: { label: '验证码', color: 'gold' },
  SYSTEM_NOTICE: { label: '系统通知', color: 'geekblue' },
}
const STATUS_META: Record<MailSendStatus, TagMeta> = {
  PENDING: { label: '待发送', color: 'processing' },
  SUCCESS: { label: '发送成功', color: 'success' },
  FAILED: { label: '发送失败', color: 'error' },
}

const pageState = reactive({
  records: [] as MailSendLogItem[],
  total: 0,
  page: 1,
  pageSize: 10,
  loadingInitial: false,
  refreshing: false,
  retryingId: '',
  errorMessage: '',
})
const hasData = computed(() => pageState.records.length > 0)
const tableLoading = computed(() => pageState.loadingInitial || pageState.refreshing)

const filters = reactive({
  bizType: '' as '' | MailBizType,
  sceneCode: '',
  templateCode: '',
  recipientEmail: '',
  status: '' as '' | MailSendStatus,
  dateRange: null as [Dayjs, Dayjs] | null,
})

const detailModal = reactive({
  open: false,
  loading: false,
  data: null as MailSendLogItem | null,
})

const bizTypeFilterOptions = [
  { label: '全部类型', value: '' },
  { label: BIZ_TYPE_META.VERIFY_CODE.label, value: 'VERIFY_CODE' as MailBizType },
  { label: BIZ_TYPE_META.SYSTEM_NOTICE.label, value: 'SYSTEM_NOTICE' as MailBizType },
]
const statusFilterOptions = [
  { label: '全部状态', value: '' },
  { label: STATUS_META.PENDING.label, value: 'PENDING' as MailSendStatus },
  { label: STATUS_META.SUCCESS.label, value: 'SUCCESS' as MailSendStatus },
  { label: STATUS_META.FAILED.label, value: 'FAILED' as MailSendStatus },
]

const tableColumns = [
  { title: '日志ID', dataIndex: 'id', key: 'id', width: 160 },
  { title: '业务类型', dataIndex: 'bizType', key: 'bizType', width: 120 },
  { title: '场景码', dataIndex: 'sceneCode', key: 'sceneCode', width: 120 },
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 180 },
  { title: '收件邮箱', dataIndex: 'recipientEmail', key: 'recipientEmail', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '重试', dataIndex: 'retryCount', key: 'retryCount', width: 90 },
  { title: '发送时间', dataIndex: 'sentTime', key: 'sentTime', width: 180 },
  { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime', width: 180 },
  { title: '操作', key: 'actions', width: 160 },
]

const resolveBizTypeMeta = (bizType: MailBizType) => BIZ_TYPE_META[bizType]
const resolveStatusMeta = (status: MailSendStatus) => STATUS_META[status]

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
    const data = await getMailSendLogPageApi({
      page: pageState.page,
      pageSize: pageState.pageSize,
      bizType: filters.bizType || undefined,
      sceneCode: filters.sceneCode.trim() ? filters.sceneCode.trim().toUpperCase() : undefined,
      templateCode: filters.templateCode.trim() ? filters.templateCode.trim().toLowerCase() : undefined,
      recipientEmail: filters.recipientEmail.trim() ? filters.recipientEmail.trim().toLowerCase() : undefined,
      status: filters.status || undefined,
      startTime: filters.dateRange ? filters.dateRange[0].format('YYYY-MM-DD') : undefined,
      endTime: filters.dateRange ? filters.dateRange[1].format('YYYY-MM-DD') : undefined,
    })
    pageState.records = data.records
    pageState.total = data.total
    pageState.page = data.page
    pageState.pageSize = data.pageSize

    if (pageState.total > 0 && pageState.records.length === 0 && pageState.page > 1) {
      pageState.page -= 1
      await fetchList('refresh')
    }
  } catch (error) {
    pageState.errorMessage = error instanceof Error ? error.message : '邮件日志加载失败，请稍后重试'
  } finally {
    pageState.loadingInitial = false
    pageState.refreshing = false
  }
}

const onRefreshClick = async () => {
  await fetchList('refresh')
  if (!pageState.errorMessage) {
    message.success('邮件日志列表已刷新')
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
  filters.bizType = ''
  filters.sceneCode = ''
  filters.templateCode = ''
  filters.recipientEmail = ''
  filters.status = ''
  filters.dateRange = null
  pageState.page = 1
  await fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  pageState.page = page
  if (pageSize > 0) pageState.pageSize = pageSize
  await fetchList('refresh')
}

const onViewDetail = async (record: MailSendLogItem) => {
  detailModal.open = true
  detailModal.loading = true
  detailModal.data = null
  try {
    detailModal.data = await getMailSendLogDetailApi(record.id)
  } catch (error) {
    detailModal.open = false
    message.error(error instanceof Error ? error.message : '邮件日志详情加载失败，请稍后重试')
  } finally {
    detailModal.loading = false
  }
}

const onRetrySend = (record: MailSendLogItem) => {
  if (record.status !== 'FAILED') {
    return
  }

  Modal.confirm({
    title: '确认重试发送该邮件？',
    content: `${record.templateCode} -> ${record.recipientEmail}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        pageState.retryingId = record.id
        await retryMailSendLogApi(record.id)
        await fetchList('refresh')
        message.success('重试请求已提交')
      } catch (error) {
        message.error(error instanceof Error ? error.message : '重试失败，请稍后重试')
      } finally {
        pageState.retryingId = ''
      }
    },
  })
}

onMounted(() => {
  void fetchList('initial')
})
</script>

<style scoped>
.mail-log-page {
  display: grid;
  gap: 16px;
}

.mail-log-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #ecfccb 0%, #dcfce7 52%, #f8fafc 100%);
  border: 1px solid #bef264;
}

.mail-log-title {
  margin: 0;
  font-size: 24px;
  color: #365314;
}

.mail-log-subtitle {
  margin: 8px 0 0;
  color: #334155;
  font-size: 14px;
}

.mail-log-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mail-log-error {
  margin-top: -6px;
}

.mail-log-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.mail-log-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.mail-log-ellipsis {
  max-width: 190px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mail-log-body {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

@media (max-width: 1100px) {
  .mail-log-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .mail-log-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .mail-log-pagination {
    justify-content: center;
  }
}
</style>
