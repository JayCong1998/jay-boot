<template>
  <section class="mail-channel-page">
    <header class="mail-channel-toolbar">
      <div>
        <h2 class="mail-channel-title">邮件通道管理</h2>
        <p class="mail-channel-subtitle">统一维护 SMTP 通道配置，支持筛选、创建、编辑、启停和删除。</p>
      </div>
      <div class="mail-channel-toolbar__actions">
        <a-button :loading="pageState.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button type="primary" @click="onOpenCreateModal">新建通道</a-button>
      </div>
    </header>

    <a-alert
      v-if="pageState.errorMessage"
      class="mail-channel-error"
      type="error"
      show-icon
      :message="pageState.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-card class="mail-channel-card" :bordered="false">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12" :lg="8">
            <a-form-item label="关键字">
              <a-input
                v-model:value="filters.keyword"
                allow-clear
                placeholder="输入通道编码/名称/发件邮箱"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
            <a-form-item label="状态">
              <a-select v-model:value="filters.status" :options="statusFilterOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :lg="12">
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

    <a-card class="mail-channel-card" :bordered="false">
      <a-skeleton v-if="pageState.loadingInitial && !hasData" active :paragraph="{ rows: 6 }" />
      <template v-else>
        <a-table
          :columns="tableColumns"
          :data-source="pageState.records"
          :loading="tableLoading"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 1300 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'tlsMode'">
              <a-tag :color="resolveTlsMeta(record.tlsMode).color">
                {{ resolveTlsMeta(record.tlsMode).label }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="resolveStatusMeta(record.status).color">
                {{ resolveStatusMeta(record.status).label }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'updatedTime'">
              {{ formatDateTime(record.updatedTime) }}
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="onOpenEditModal(record)">编辑</a-button>
                <a-button type="link" size="small" @click="onToggleStatus(record)">
                  {{ record.status === 'ENABLED' ? '禁用' : '启用' }}
                </a-button>
                <a-button type="link" size="small" danger @click="onDeleteOne(record)">删除</a-button>
              </a-space>
            </template>
          </template>
        </a-table>

        <div class="mail-channel-pagination">
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

    <a-modal
      v-model:open="channelModal.open"
      :title="channelModal.mode === 'create' ? '新建邮件通道' : '编辑邮件通道'"
      :confirm-loading="pageState.submitting"
      :width="760"
      @ok="onSubmitChannel"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12">
            <a-form-item label="通道编码" required>
              <a-input
                v-model:value="channelModal.form.channelCode"
                :disabled="channelModal.mode === 'edit'"
                :maxlength="64"
                placeholder="例如：default_smtp"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="通道名称" required>
              <a-input v-model:value="channelModal.form.channelName" :maxlength="64" placeholder="请输入通道名称" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :xs="24" :md="12">
            <a-form-item label="SMTP 主机" required>
              <a-input v-model:value="channelModal.form.smtpHost" :maxlength="128" placeholder="smtp.example.com" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="SMTP 端口" required>
              <a-input-number
                v-model:value="channelModal.form.smtpPort"
                :min="1"
                :max="65535"
                :precision="0"
                :controls="false"
                class="mail-channel-full-width"
                placeholder="587"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :xs="24" :md="12">
            <a-form-item label="SMTP 用户名" required>
              <a-input v-model:value="channelModal.form.smtpUsername" :maxlength="128" placeholder="请输入 SMTP 用户名" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item :label="channelModal.mode === 'create' ? 'SMTP 密码' : 'SMTP 密码（留空表示不修改）'" :required="channelModal.mode === 'create'">
              <a-input-password
                v-model:value="channelModal.form.smtpPassword"
                :maxlength="256"
                placeholder="请输入 SMTP 密码"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :xs="24" :md="12">
            <a-form-item label="发件人名称" required>
              <a-input v-model:value="channelModal.form.fromName" :maxlength="128" placeholder="Jay Boot" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="发件人邮箱" required>
              <a-input v-model:value="channelModal.form.fromEmail" :maxlength="128" placeholder="no-reply@example.com" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :xs="24" :md="8">
            <a-form-item label="TLS 模式" required>
              <a-select v-model:value="channelModal.form.tlsMode" :options="tlsFormOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="优先级" required>
              <a-input-number
                v-model:value="channelModal.form.priority"
                :precision="0"
                :controls="false"
                class="mail-channel-full-width"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="状态" required>
              <a-select v-model:value="channelModal.form.status" :options="statusFormOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注">
          <a-textarea v-model:value="channelModal.form.remark" :rows="3" :maxlength="255" placeholder="可选，最多 255 字符" />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import {
  createMailChannelApi,
  deleteMailChannelApi,
  getMailChannelDetailApi,
  getMailChannelPageApi,
  updateMailChannelApi,
  updateMailChannelStatusApi,
  type MailChannelItem,
  type MailStatus,
  type MailTlsMode,
} from '../../../api/admin/MailApi'

type ChannelModalMode = 'create' | 'edit'
type TagMeta = { label: string; color: string }

const STATUS_META: Record<MailStatus, TagMeta> = {
  ENABLED: { label: '启用', color: 'success' },
  DISABLED: { label: '禁用', color: 'default' },
}
const TLS_META: Record<MailTlsMode, TagMeta> = {
  NONE: { label: '无', color: 'default' },
  STARTTLS: { label: 'STARTTLS', color: 'processing' },
  SSL: { label: 'SSL', color: 'purple' },
}

const pageState = reactive({
  records: [] as MailChannelItem[],
  total: 0,
  page: 1,
  pageSize: 10,
  loadingInitial: false,
  refreshing: false,
  submitting: false,
  errorMessage: '',
})
const hasData = computed(() => pageState.records.length > 0)
const tableLoading = computed(() => pageState.loadingInitial || pageState.refreshing)

const filters = reactive({
  keyword: '',
  status: '' as '' | MailStatus,
})

const channelModal = reactive({
  open: false,
  mode: 'create' as ChannelModalMode,
  editingId: '',
  form: {
    channelCode: '',
    channelName: '',
    smtpHost: '',
    smtpPort: 587,
    smtpUsername: '',
    smtpPassword: '',
    tlsMode: 'STARTTLS' as MailTlsMode,
    fromName: '',
    fromEmail: '',
    priority: 0,
    status: 'ENABLED' as MailStatus,
    remark: '',
  },
})

const statusFormOptions = [
  { label: STATUS_META.ENABLED.label, value: 'ENABLED' as MailStatus },
  { label: STATUS_META.DISABLED.label, value: 'DISABLED' as MailStatus },
]
const statusFilterOptions = [
  { label: '全部状态', value: '' },
  ...statusFormOptions,
]
const tlsFormOptions = [
  { label: TLS_META.NONE.label, value: 'NONE' as MailTlsMode },
  { label: TLS_META.STARTTLS.label, value: 'STARTTLS' as MailTlsMode },
  { label: TLS_META.SSL.label, value: 'SSL' as MailTlsMode },
]

const tableColumns = [
  { title: '通道编码', dataIndex: 'channelCode', key: 'channelCode', width: 150 },
  { title: '通道名称', dataIndex: 'channelName', key: 'channelName', width: 160 },
  { title: 'SMTP 主机', dataIndex: 'smtpHost', key: 'smtpHost', width: 200 },
  { title: '端口', dataIndex: 'smtpPort', key: 'smtpPort', width: 80 },
  { title: 'SMTP 用户名', dataIndex: 'smtpUsername', key: 'smtpUsername', width: 170 },
  { title: '发件邮箱', dataIndex: 'fromEmail', key: 'fromEmail', width: 180 },
  { title: 'TLS', dataIndex: 'tlsMode', key: 'tlsMode', width: 100 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '更新时间', dataIndex: 'updatedTime', key: 'updatedTime', width: 180 },
  { title: '操作', key: 'actions', width: 180 },
]

const resolveStatusMeta = (status: MailStatus) => STATUS_META[status]
const resolveTlsMeta = (tlsMode: MailTlsMode) => TLS_META[tlsMode]

const isValidEmail = (email: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
const normalizeChannelCode = (value: string) => value.trim().toLowerCase()
const isValidChannelCode = (value: string) => /^[a-z0-9_]+$/.test(value)

const formatDateTime = (value: string | null | undefined) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const resetChannelForm = () => {
  channelModal.form.channelCode = ''
  channelModal.form.channelName = ''
  channelModal.form.smtpHost = ''
  channelModal.form.smtpPort = 587
  channelModal.form.smtpUsername = ''
  channelModal.form.smtpPassword = ''
  channelModal.form.tlsMode = 'STARTTLS'
  channelModal.form.fromName = ''
  channelModal.form.fromEmail = ''
  channelModal.form.priority = 0
  channelModal.form.status = 'ENABLED'
  channelModal.form.remark = ''
}

const validateChannelForm = () => {
  const channelCode = normalizeChannelCode(channelModal.form.channelCode)
  const channelName = channelModal.form.channelName.trim()
  const smtpHost = channelModal.form.smtpHost.trim()
  const smtpUsername = channelModal.form.smtpUsername.trim()
  const smtpPassword = channelModal.form.smtpPassword.trim()
  const fromName = channelModal.form.fromName.trim()
  const fromEmail = channelModal.form.fromEmail.trim().toLowerCase()

  if (!channelCode) {
    message.warning('请输入通道编码')
    return false
  }
  if (!isValidChannelCode(channelCode)) {
    message.warning('通道编码仅支持小写字母、数字和下划线')
    return false
  }
  if (!channelName) {
    message.warning('请输入通道名称')
    return false
  }
  if (!smtpHost) {
    message.warning('请输入 SMTP 主机')
    return false
  }
  if (!Number.isInteger(channelModal.form.smtpPort) || channelModal.form.smtpPort < 1 || channelModal.form.smtpPort > 65535) {
    message.warning('SMTP 端口必须为 1-65535 的整数')
    return false
  }
  if (!smtpUsername) {
    message.warning('请输入 SMTP 用户名')
    return false
  }
  if (channelModal.mode === 'create' && !smtpPassword) {
    message.warning('请输入 SMTP 密码')
    return false
  }
  if (!fromName) {
    message.warning('请输入发件人名称')
    return false
  }
  if (!fromEmail || !isValidEmail(fromEmail)) {
    message.warning('请输入合法的发件人邮箱')
    return false
  }
  if (!Number.isInteger(channelModal.form.priority)) {
    message.warning('优先级必须为整数')
    return false
  }
  return true
}

const fetchList = async (mode: 'initial' | 'refresh' = 'refresh') => {
  pageState.errorMessage = ''
  if (mode === 'initial') pageState.loadingInitial = true
  if (mode === 'refresh') pageState.refreshing = true

  try {
    const data = await getMailChannelPageApi({
      page: pageState.page,
      pageSize: pageState.pageSize,
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
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
    pageState.errorMessage = error instanceof Error ? error.message : '邮件通道列表加载失败，请稍后重试'
  } finally {
    pageState.loadingInitial = false
    pageState.refreshing = false
  }
}

const onRefreshClick = async () => {
  await fetchList('refresh')
  if (!pageState.errorMessage) {
    message.success('邮件通道列表已刷新')
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
  filters.status = ''
  pageState.page = 1
  await fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  pageState.page = page
  if (pageSize > 0) pageState.pageSize = pageSize
  await fetchList('refresh')
}

const onOpenCreateModal = () => {
  channelModal.mode = 'create'
  channelModal.editingId = ''
  resetChannelForm()
  channelModal.open = true
}

const onOpenEditModal = async (record: MailChannelItem) => {
  try {
    const detail = await getMailChannelDetailApi(record.id)
    channelModal.mode = 'edit'
    channelModal.editingId = detail.id
    channelModal.form.channelCode = detail.channelCode
    channelModal.form.channelName = detail.channelName
    channelModal.form.smtpHost = detail.smtpHost
    channelModal.form.smtpPort = detail.smtpPort
    channelModal.form.smtpUsername = detail.smtpUsername
    channelModal.form.smtpPassword = ''
    channelModal.form.tlsMode = detail.tlsMode
    channelModal.form.fromName = detail.fromName
    channelModal.form.fromEmail = detail.fromEmail
    channelModal.form.priority = detail.priority
    channelModal.form.status = detail.status
    channelModal.form.remark = detail.remark || ''
    channelModal.open = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '邮件通道详情加载失败，请稍后重试')
  }
}

const onSubmitChannel = async () => {
  if (!validateChannelForm()) return

  try {
    pageState.submitting = true
    const normalizedCode = normalizeChannelCode(channelModal.form.channelCode)
    const smtpPassword = channelModal.form.smtpPassword.trim()
    const payload = {
      channelName: channelModal.form.channelName.trim(),
      smtpHost: channelModal.form.smtpHost.trim(),
      smtpPort: channelModal.form.smtpPort,
      smtpUsername: channelModal.form.smtpUsername.trim(),
      smtpPassword: smtpPassword || undefined,
      tlsMode: channelModal.form.tlsMode,
      fromName: channelModal.form.fromName.trim(),
      fromEmail: channelModal.form.fromEmail.trim().toLowerCase(),
      priority: channelModal.form.priority,
      status: channelModal.form.status,
      remark: channelModal.form.remark.trim() || undefined,
    }

    if (channelModal.mode === 'create') {
      await createMailChannelApi({
        channelCode: normalizedCode,
        ...payload,
        smtpPassword,
      })
      message.success('邮件通道创建成功')
    } else {
      await updateMailChannelApi(channelModal.editingId, payload)
      message.success('邮件通道更新成功')
    }

    channelModal.open = false
    await fetchList('refresh')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '提交失败，请稍后重试')
  } finally {
    pageState.submitting = false
  }
}

const onToggleStatus = (record: MailChannelItem) => {
  const nextStatus: MailStatus = record.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const actionText = nextStatus === 'ENABLED' ? '启用' : '禁用'
  Modal.confirm({
    title: `确认${actionText}该通道？`,
    content: `${record.channelCode} / ${record.channelName}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await updateMailChannelStatusApi(record.id, nextStatus)
        await fetchList('refresh')
        message.success(`邮件通道已${actionText}`)
      } catch (error) {
        message.error(error instanceof Error ? error.message : '状态更新失败，请稍后重试')
      }
    },
  })
}

const onDeleteOne = (record: MailChannelItem) => {
  Modal.confirm({
    title: '确认删除该邮件通道？',
    content: `${record.channelCode} / ${record.channelName}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await deleteMailChannelApi(record.id)
        await fetchList('refresh')
        message.success('邮件通道已删除')
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
.mail-channel-page {
  display: grid;
  gap: 16px;
}

.mail-channel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e0f2fe 0%, #ecfeff 55%, #f8fafc 100%);
  border: 1px solid #bae6fd;
}

.mail-channel-title {
  margin: 0;
  font-size: 24px;
  color: #0c4a6e;
}

.mail-channel-subtitle {
  margin: 8px 0 0;
  color: #334155;
  font-size: 14px;
}

.mail-channel-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mail-channel-error {
  margin-top: -6px;
}

.mail-channel-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.mail-channel-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.mail-channel-full-width {
  width: 100%;
}

@media (max-width: 1100px) {
  .mail-channel-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .mail-channel-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .mail-channel-pagination {
    justify-content: center;
  }
}
</style>
