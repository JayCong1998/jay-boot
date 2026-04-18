<template>
  <section class="mail-template-page">
    <header class="mail-template-toolbar">
      <div>
        <h2 class="mail-template-title">邮件模板管理</h2>
        <p class="mail-template-subtitle">维护模板编码、场景和渲染内容，支持预览、启停与删除。</p>
      </div>
      <div class="mail-template-toolbar__actions">
        <a-button :loading="pageState.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button type="primary" @click="onOpenCreateModal">新建模板</a-button>
      </div>
    </header>

    <a-alert
      v-if="pageState.errorMessage"
      class="mail-template-error"
      type="error"
      show-icon
      :message="pageState.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-card class="mail-template-card" :bordered="false">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12" :lg="8">
            <a-form-item label="关键字">
              <a-input
                v-model:value="filters.keyword"
                allow-clear
                placeholder="输入模板编码/名称/场景码"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
            <a-form-item label="业务类型">
              <a-select v-model:value="filters.bizType" :options="bizTypeFilterOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
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

    <a-card class="mail-template-card" :bordered="false">
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
            <template v-if="column.key === 'bizType'">
              <a-tag :color="resolveBizTypeMeta(record.bizType).color">
                {{ resolveBizTypeMeta(record.bizType).label }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'bodyType'">
              <a-tag :color="resolveBodyTypeMeta(record.bodyType).color">
                {{ resolveBodyTypeMeta(record.bodyType).label }}
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
            <template v-else-if="column.key === 'subjectTemplate'">
              <a-tooltip :title="record.subjectTemplate">
                <span class="mail-template-ellipsis">{{ record.subjectTemplate }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="onOpenEditModal(record)">编辑</a-button>
                <a-button type="link" size="small" @click="onOpenPreviewModal(record)">预览</a-button>
                <a-button type="link" size="small" @click="onToggleStatus(record)">
                  {{ record.status === 'ENABLED' ? '禁用' : '启用' }}
                </a-button>
                <a-button type="link" size="small" danger @click="onDeleteOne(record)">删除</a-button>
              </a-space>
            </template>
          </template>
        </a-table>

        <div class="mail-template-pagination">
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
      v-model:open="templateModal.open"
      :title="templateModal.mode === 'create' ? '新建邮件模板' : '编辑邮件模板'"
      :confirm-loading="pageState.submitting"
      :width="860"
      @ok="onSubmitTemplate"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12">
            <a-form-item label="模板编码" required>
              <a-input
                v-model:value="templateModal.form.templateCode"
                :disabled="templateModal.mode === 'edit'"
                :maxlength="64"
                placeholder="例如：verify_code_register"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="模板名称" required>
              <a-input v-model:value="templateModal.form.templateName" :maxlength="64" placeholder="请输入模板名称" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :xs="24" :md="8">
            <a-form-item label="业务类型" required>
              <a-select v-model:value="templateModal.form.bizType" :options="bizTypeFormOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="场景码" required>
              <a-input v-model:value="templateModal.form.sceneCode" :maxlength="64" placeholder="例如：REGISTER" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="正文类型" required>
              <a-select v-model:value="templateModal.form.bodyType" :options="bodyTypeFormOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="主题模板" required>
          <a-input v-model:value="templateModal.form.subjectTemplate" :maxlength="255" placeholder="例如：您的验证码是 {{code}}" />
        </a-form-item>

        <a-form-item label="正文模板" required>
          <a-textarea
            v-model:value="templateModal.form.bodyTemplate"
            :rows="7"
            placeholder="支持使用 {{变量名}} 占位符"
          />
        </a-form-item>

        <a-form-item label="变量 Schema JSON">
          <a-textarea
            v-model:value="templateModal.form.varsSchemaJson"
            :rows="4"
            placeholder='可选，例如：{"code":"string","expireMinutes":"number"}'
          />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :xs="24" :md="8">
            <a-form-item label="状态" required>
              <a-select v-model:value="templateModal.form.status" :options="statusFormOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="16">
            <a-form-item label="备注">
              <a-input v-model:value="templateModal.form.remark" :maxlength="255" placeholder="可选，最多 255 字符" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="previewModal.open" title="模板预览" :confirm-loading="previewModal.loading" :width="860" @ok="onSubmitPreview">
      <a-form layout="vertical">
        <a-form-item label="模板编码" required>
          <a-input v-model:value="previewModal.templateCode" placeholder="请输入已启用的模板编码" />
        </a-form-item>
        <a-form-item label="渲染变量 JSON">
          <a-textarea
            v-model:value="previewModal.variablesText"
            :rows="5"
            placeholder='例如：{"code":"123456","expireMinutes":10}'
          />
        </a-form-item>
      </a-form>

      <a-divider />

      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="渲染主题">{{ previewModal.resultSubject || '-' }}</a-descriptions-item>
        <a-descriptions-item label="渲染正文">
          <pre class="mail-template-preview-body">{{ previewModal.resultBody || '-' }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import {
  createMailTemplateApi,
  deleteMailTemplateApi,
  getMailTemplateDetailApi,
  getMailTemplatePageApi,
  previewMailTemplateApi,
  updateMailTemplateApi,
  updateMailTemplateStatusApi,
  type MailBodyType,
  type MailBizType,
  type MailStatus,
  type MailTemplateItem,
} from '../../../api/admin/MailApi'

type TemplateModalMode = 'create' | 'edit'
type TagMeta = { label: string; color: string }

const STATUS_META: Record<MailStatus, TagMeta> = {
  ENABLED: { label: '启用', color: 'success' },
  DISABLED: { label: '禁用', color: 'default' },
}
const BIZ_TYPE_META: Record<MailBizType, TagMeta> = {
  VERIFY_CODE: { label: '验证码', color: 'gold' },
  SYSTEM_NOTICE: { label: '系统通知', color: 'geekblue' },
}
const BODY_TYPE_META: Record<MailBodyType, TagMeta> = {
  HTML: { label: 'HTML', color: 'processing' },
  TEXT: { label: '文本', color: 'default' },
}

const pageState = reactive({
  records: [] as MailTemplateItem[],
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
  bizType: '' as '' | MailBizType,
  sceneCode: '',
  status: '' as '' | MailStatus,
})

const templateModal = reactive({
  open: false,
  mode: 'create' as TemplateModalMode,
  editingId: '',
  form: {
    templateCode: '',
    templateName: '',
    bizType: 'VERIFY_CODE' as MailBizType,
    sceneCode: 'REGISTER',
    subjectTemplate: '',
    bodyTemplate: '',
    bodyType: 'HTML' as MailBodyType,
    varsSchemaJson: '',
    status: 'ENABLED' as MailStatus,
    remark: '',
  },
})

const previewModal = reactive({
  open: false,
  loading: false,
  templateCode: '',
  variablesText: '{}',
  resultSubject: '',
  resultBody: '',
})

const statusFormOptions = [
  { label: STATUS_META.ENABLED.label, value: 'ENABLED' as MailStatus },
  { label: STATUS_META.DISABLED.label, value: 'DISABLED' as MailStatus },
]
const statusFilterOptions = [
  { label: '全部状态', value: '' },
  ...statusFormOptions,
]
const bizTypeFormOptions = [
  { label: BIZ_TYPE_META.VERIFY_CODE.label, value: 'VERIFY_CODE' as MailBizType },
  { label: BIZ_TYPE_META.SYSTEM_NOTICE.label, value: 'SYSTEM_NOTICE' as MailBizType },
]
const bizTypeFilterOptions = [
  { label: '全部类型', value: '' },
  ...bizTypeFormOptions,
]
const bodyTypeFormOptions = [
  { label: BODY_TYPE_META.HTML.label, value: 'HTML' as MailBodyType },
  { label: BODY_TYPE_META.TEXT.label, value: 'TEXT' as MailBodyType },
]

const tableColumns = [
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 180 },
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName', width: 170 },
  { title: '业务类型', dataIndex: 'bizType', key: 'bizType', width: 110 },
  { title: '场景码', dataIndex: 'sceneCode', key: 'sceneCode', width: 150 },
  { title: '正文类型', dataIndex: 'bodyType', key: 'bodyType', width: 100 },
  { title: '主题模板', dataIndex: 'subjectTemplate', key: 'subjectTemplate', width: 220 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updatedTime', key: 'updatedTime', width: 180 },
  { title: '操作', key: 'actions', width: 240 },
]

const resolveStatusMeta = (status: MailStatus) => STATUS_META[status]
const resolveBizTypeMeta = (bizType: MailBizType) => BIZ_TYPE_META[bizType]
const resolveBodyTypeMeta = (bodyType: MailBodyType) => BODY_TYPE_META[bodyType]

const normalizeTemplateCode = (value: string) => value.trim().toLowerCase()
const normalizeSceneCode = (value: string) => value.trim().toUpperCase()
const isValidTemplateCode = (value: string) => /^[a-z0-9_]+$/.test(value)
const isValidSceneCode = (value: string) => /^[A-Z0-9_]+$/.test(value)

const formatDateTime = (value: string | null | undefined) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const isValidJson = (value: string) => {
  try {
    JSON.parse(value)
    return true
  } catch {
    return false
  }
}

const parseJsonObject = (value: string) => {
  const normalized = value.trim()
  if (!normalized) return {}
  const parsed = JSON.parse(normalized) as unknown
  if (parsed === null || Array.isArray(parsed) || typeof parsed !== 'object') {
    return null
  }
  return parsed as Record<string, unknown>
}

const resetTemplateForm = () => {
  templateModal.form.templateCode = ''
  templateModal.form.templateName = ''
  templateModal.form.bizType = 'VERIFY_CODE'
  templateModal.form.sceneCode = 'REGISTER'
  templateModal.form.subjectTemplate = ''
  templateModal.form.bodyTemplate = ''
  templateModal.form.bodyType = 'HTML'
  templateModal.form.varsSchemaJson = ''
  templateModal.form.status = 'ENABLED'
  templateModal.form.remark = ''
}

const validateTemplateForm = () => {
  const templateCode = normalizeTemplateCode(templateModal.form.templateCode)
  const templateName = templateModal.form.templateName.trim()
  const sceneCode = normalizeSceneCode(templateModal.form.sceneCode)
  const subjectTemplate = templateModal.form.subjectTemplate.trim()
  const bodyTemplate = templateModal.form.bodyTemplate.trim()
  const varsSchemaJson = templateModal.form.varsSchemaJson.trim()

  if (!templateCode) {
    message.warning('请输入模板编码')
    return false
  }
  if (!isValidTemplateCode(templateCode)) {
    message.warning('模板编码仅支持小写字母、数字和下划线')
    return false
  }
  if (!templateName) {
    message.warning('请输入模板名称')
    return false
  }
  if (!sceneCode) {
    message.warning('请输入场景码')
    return false
  }
  if (!isValidSceneCode(sceneCode)) {
    message.warning('场景码仅支持大写字母、数字和下划线')
    return false
  }
  if (!subjectTemplate) {
    message.warning('请输入主题模板')
    return false
  }
  if (!bodyTemplate) {
    message.warning('请输入正文模板')
    return false
  }
  if (varsSchemaJson && !isValidJson(varsSchemaJson)) {
    message.warning('变量 Schema JSON 格式不合法')
    return false
  }
  return true
}

const fetchList = async (mode: 'initial' | 'refresh' = 'refresh') => {
  pageState.errorMessage = ''
  if (mode === 'initial') pageState.loadingInitial = true
  if (mode === 'refresh') pageState.refreshing = true

  try {
    const data = await getMailTemplatePageApi({
      page: pageState.page,
      pageSize: pageState.pageSize,
      keyword: filters.keyword || undefined,
      bizType: filters.bizType || undefined,
      sceneCode: filters.sceneCode ? normalizeSceneCode(filters.sceneCode) : undefined,
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
    pageState.errorMessage = error instanceof Error ? error.message : '邮件模板列表加载失败，请稍后重试'
  } finally {
    pageState.loadingInitial = false
    pageState.refreshing = false
  }
}

const onRefreshClick = async () => {
  await fetchList('refresh')
  if (!pageState.errorMessage) {
    message.success('邮件模板列表已刷新')
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
  filters.bizType = ''
  filters.sceneCode = ''
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
  templateModal.mode = 'create'
  templateModal.editingId = ''
  resetTemplateForm()
  templateModal.open = true
}

const onOpenEditModal = async (record: MailTemplateItem) => {
  try {
    const detail = await getMailTemplateDetailApi(record.id)
    templateModal.mode = 'edit'
    templateModal.editingId = detail.id
    templateModal.form.templateCode = detail.templateCode
    templateModal.form.templateName = detail.templateName
    templateModal.form.bizType = detail.bizType
    templateModal.form.sceneCode = detail.sceneCode
    templateModal.form.subjectTemplate = detail.subjectTemplate
    templateModal.form.bodyTemplate = detail.bodyTemplate
    templateModal.form.bodyType = detail.bodyType
    templateModal.form.varsSchemaJson = detail.varsSchemaJson || ''
    templateModal.form.status = detail.status
    templateModal.form.remark = detail.remark || ''
    templateModal.open = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '邮件模板详情加载失败，请稍后重试')
  }
}

const onSubmitTemplate = async () => {
  if (!validateTemplateForm()) return

  try {
    pageState.submitting = true
    const payload = {
      templateName: templateModal.form.templateName.trim(),
      bizType: templateModal.form.bizType,
      sceneCode: normalizeSceneCode(templateModal.form.sceneCode),
      subjectTemplate: templateModal.form.subjectTemplate.trim(),
      bodyTemplate: templateModal.form.bodyTemplate.trim(),
      bodyType: templateModal.form.bodyType,
      varsSchemaJson: templateModal.form.varsSchemaJson.trim() || undefined,
      status: templateModal.form.status,
      remark: templateModal.form.remark.trim() || undefined,
    }

    if (templateModal.mode === 'create') {
      await createMailTemplateApi({
        templateCode: normalizeTemplateCode(templateModal.form.templateCode),
        ...payload,
      })
      message.success('邮件模板创建成功')
    } else {
      await updateMailTemplateApi(templateModal.editingId, payload)
      message.success('邮件模板更新成功')
    }

    templateModal.open = false
    await fetchList('refresh')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '提交失败，请稍后重试')
  } finally {
    pageState.submitting = false
  }
}

const onOpenPreviewModal = (record: MailTemplateItem) => {
  previewModal.open = true
  previewModal.templateCode = record.templateCode
  previewModal.variablesText = '{}'
  previewModal.resultSubject = ''
  previewModal.resultBody = ''
}

const onSubmitPreview = async () => {
  const templateCode = normalizeTemplateCode(previewModal.templateCode)
  if (!templateCode) {
    message.warning('请输入模板编码')
    return
  }
  if (!isValidTemplateCode(templateCode)) {
    message.warning('模板编码仅支持小写字母、数字和下划线')
    return
  }
  if (!isValidJson(previewModal.variablesText || '{}')) {
    message.warning('渲染变量 JSON 格式不合法')
    return
  }
  const variables = parseJsonObject(previewModal.variablesText || '{}')
  if (variables === null) {
    message.warning('渲染变量必须是 JSON 对象')
    return
  }

  try {
    previewModal.loading = true
    const data = await previewMailTemplateApi({ templateCode, variables })
    previewModal.resultSubject = data.subject
    previewModal.resultBody = data.body
    message.success('模板预览成功')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '模板预览失败，请稍后重试')
  } finally {
    previewModal.loading = false
  }
}

const onToggleStatus = (record: MailTemplateItem) => {
  const nextStatus: MailStatus = record.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const actionText = nextStatus === 'ENABLED' ? '启用' : '禁用'

  Modal.confirm({
    title: `确认${actionText}该模板？`,
    content: `${record.templateCode} / ${record.templateName}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await updateMailTemplateStatusApi(record.id, nextStatus)
        await fetchList('refresh')
        message.success(`邮件模板已${actionText}`)
      } catch (error) {
        message.error(error instanceof Error ? error.message : '状态更新失败，请稍后重试')
      }
    },
  })
}

const onDeleteOne = (record: MailTemplateItem) => {
  Modal.confirm({
    title: '确认删除该邮件模板？',
    content: `${record.templateCode} / ${record.templateName}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await deleteMailTemplateApi(record.id)
        await fetchList('refresh')
        message.success('邮件模板已删除')
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
.mail-template-page {
  display: grid;
  gap: 16px;
}

.mail-template-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(140deg, #fdf2f8 0%, #fefce8 56%, #f8fafc 100%);
  border: 1px solid #f5d0fe;
}

.mail-template-title {
  margin: 0;
  font-size: 24px;
  color: #831843;
}

.mail-template-subtitle {
  margin: 8px 0 0;
  color: #334155;
  font-size: 14px;
}

.mail-template-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mail-template-error {
  margin-top: -6px;
}

.mail-template-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.mail-template-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.mail-template-ellipsis {
  max-width: 200px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mail-template-preview-body {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

@media (max-width: 1100px) {
  .mail-template-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .mail-template-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .mail-template-pagination {
    justify-content: center;
  }
}
</style>
