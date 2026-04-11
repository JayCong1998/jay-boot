<template>
  <section class="plans-page">
    <header class="plans-toolbar">
      <div>
        <h2 class="plans-title">套餐管理</h2>
        <p class="plans-subtitle">维护套餐定义、配额规则与启停状态，支持编码检索和周期筛选。</p>
      </div>
      <div class="plans-toolbar__actions">
        <a-button :loading="plansStore.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button type="primary" @click="onOpenCreateModal">新建套餐</a-button>
      </div>
    </header>

    <a-alert v-if="plansStore.errorMessage" class="plans-error" type="error" show-icon :message="plansStore.errorMessage">
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-card class="plans-card" :bordered="false">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12" :lg="8">
            <a-form-item label="关键词">
              <a-input
                v-model:value="filters.keyword"
                allow-clear
                placeholder="输入套餐编码或名称"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
            <a-form-item label="计费周期">
              <a-select v-model:value="filters.billingCycle" :options="billingCycleFilterOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
            <a-form-item label="状态">
              <a-select v-model:value="filters.status" :options="statusFilterOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :lg="8">
            <a-form-item label=" ">
              <a-space>
                <a-button type="primary" :loading="plansStore.refreshing" @click="onSearchClick">查询</a-button>
                <a-button @click="onResetClick">重置</a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <a-card class="plans-card" :bordered="false">
      <a-skeleton v-if="plansStore.loadingInitial && !plansStore.hasData" active :paragraph="{ rows: 6 }" />
      <template v-else>
        <a-table
          :columns="tableColumns"
          :data-source="plansStore.records"
          :loading="tableLoading"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 1020 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'billingCycle'">
              {{ resolveBillingCycleLabel(record.billingCycle) }}
            </template>
            <template v-else-if="column.key === 'price'">
              {{ formatPrice(record.price) }}
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'ACTIVE' ? 'success' : 'default'">
                {{ record.status === 'ACTIVE' ? '启用' : '停用' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'updatedTime'">
              {{ formatDateTime(record.updatedTime) }}
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="onOpenEditModal(record)">编辑</a-button>
                <a-button type="link" size="small" :loading="plansStore.submitting" @click="onToggleStatus(record)">
                  {{ record.status === 'ACTIVE' ? '停用' : '启用' }}
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>

        <div class="plans-pagination">
          <a-pagination
            :current="plansStore.page"
            :page-size="plansStore.pageSize"
            :total="plansStore.total"
            :show-total="(total: number) => `共 ${total} 条`"
            show-size-changer
            :page-size-options="['10', '20', '50']"
            @change="onPageChange"
          />
        </div>
      </template>
    </a-card>

    <a-modal
      v-model:open="planModal.open"
      :title="planModal.mode === 'create' ? '新建套餐' : '编辑套餐'"
      :confirm-loading="plansStore.submitting"
      :width="760"
      @ok="onSubmitPlan"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12">
            <a-form-item label="套餐编码" required>
              <a-input
                v-model:value="planModal.form.code"
                :disabled="planModal.mode === 'edit'"
                :maxlength="64"
                placeholder="例如：PRO_MONTHLY"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="套餐名称" required>
              <a-input v-model:value="planModal.form.name" :maxlength="64" placeholder="请输入套餐名称" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :xs="24" :md="8">
            <a-form-item label="计费周期" required>
              <a-select v-model:value="planModal.form.billingCycle" :options="billingCycleFormOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="价格（分）" required>
              <a-input-number
                v-model:value="planModal.form.price"
                :min="0"
                :precision="0"
                :controls="false"
                class="plans-full-width"
                placeholder="请输入价格"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="状态" required>
              <a-select v-model:value="planModal.form.status" :options="statusFormOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="配额 JSON" required>
          <a-textarea
            v-model:value="planModal.form.quotaJson"
            :rows="7"
            :maxlength="4000"
            placeholder='例如：{"seats":10,"tokensMonthly":500000}'
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import type { AdminPlanItem, PlanBillingCycle, PlanStatus } from '../../api/admin/PlansApi'
import { useAdminPlansStore } from '../../stores/admin/plans'

type PlanModalMode = 'create' | 'edit'

const plansStore = useAdminPlansStore()

const filters = reactive({
  keyword: '',
  billingCycle: '' as '' | PlanBillingCycle,
  status: '' as '' | PlanStatus,
})

const planModal = reactive({
  open: false,
  mode: 'create' as PlanModalMode,
  editingId: '',
  form: {
    code: '',
    name: '',
    billingCycle: 'MONTHLY' as PlanBillingCycle,
    price: 0,
    status: 'ACTIVE' as PlanStatus,
    quotaJson: '',
  },
})

const tableColumns = [
  { title: '套餐编码', dataIndex: 'code', key: 'code', width: 170 },
  { title: '套餐名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '计费周期', dataIndex: 'billingCycle', key: 'billingCycle', width: 120 },
  { title: '价格（元）', dataIndex: 'price', key: 'price', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '更新时间', dataIndex: 'updatedTime', key: 'updatedTime', width: 200 },
  { title: '操作', key: 'actions', width: 160 },
]

const billingCycleLabelMap: Record<PlanBillingCycle, string> = {
  MONTHLY: '月付',
  YEARLY: '年付',
}

const resolveBillingCycleLabel = (value: unknown) => {
  if (value === 'MONTHLY' || value === 'YEARLY') {
    return billingCycleLabelMap[value]
  }
  return '--'
}

const billingCycleFilterOptions: Array<{ label: string; value: '' | PlanBillingCycle }> = [
  { label: '全部周期', value: '' },
  { label: '月付', value: 'MONTHLY' },
  { label: '年付', value: 'YEARLY' },
]

const statusFilterOptions: Array<{ label: string; value: '' | PlanStatus }> = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
]

const billingCycleFormOptions: Array<{ label: string; value: PlanBillingCycle }> = [
  { label: '月付', value: 'MONTHLY' },
  { label: '年付', value: 'YEARLY' },
]

const statusFormOptions: Array<{ label: string; value: PlanStatus }> = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
]

const tableLoading = computed(() => plansStore.loadingInitial || plansStore.refreshing)

const syncFiltersFromStore = () => {
  filters.keyword = plansStore.keyword
  filters.billingCycle = plansStore.billingCycle
  filters.status = plansStore.status
}

const resetPlanForm = () => {
  planModal.form.code = ''
  planModal.form.name = ''
  planModal.form.billingCycle = 'MONTHLY'
  planModal.form.price = 0
  planModal.form.status = 'ACTIVE'
  planModal.form.quotaJson = ''
}

const normalizeCode = (value: string) => value.trim().toUpperCase()

const isValidCode = (value: string) => /^[A-Z0-9_]+$/.test(value)

const isValidJson = (value: string) => {
  try {
    JSON.parse(value)
    return true
  } catch {
    return false
  }
}

const validatePlanForm = () => {
  const code = normalizeCode(planModal.form.code)
  const name = planModal.form.name.trim()
  const quotaJson = planModal.form.quotaJson.trim()

  if (!code) {
    message.warning('请输入套餐编码')
    return false
  }
  if (!isValidCode(code)) {
    message.warning('套餐编码仅支持大写字母、数字和下划线')
    return false
  }
  if (!name) {
    message.warning('请输入套餐名称')
    return false
  }
  if (!Number.isFinite(planModal.form.price)) {
    message.warning('请输入有效价格')
    return false
  }
  if (!Number.isInteger(planModal.form.price)) {
    message.warning('价格必须为整数分')
    return false
  }
  if (planModal.form.price < 0) {
    message.warning('价格不能小于0')
    return false
  }
  if (!quotaJson) {
    message.warning('请输入配额JSON')
    return false
  }
  if (!isValidJson(quotaJson)) {
    message.warning('配额JSON格式不合法')
    return false
  }
  return true
}

const formatDateTime = (value: string) => {
  if (!value) {
    return '--'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString('zh-CN', { hour12: false })
}

const formatPrice = (price: number) => {
  if (Number.isNaN(price)) {
    return '--'
  }
  return (price / 100).toFixed(2)
}

const onRefreshClick = async () => {
  await plansStore.fetchList('refresh')
  if (!plansStore.errorMessage) {
    message.success('套餐列表已刷新')
  }
}

const onRetryClick = async () => {
  await plansStore.fetchList('refresh')
}

const onSearchClick = async () => {
  plansStore.setFilters({
    keyword: filters.keyword.trim(),
    billingCycle: filters.billingCycle,
    status: filters.status,
  })
  await plansStore.searchWithCurrentFilters()
}

const onResetClick = async () => {
  plansStore.resetFilters()
  syncFiltersFromStore()
  await plansStore.fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  await plansStore.changePage(page, pageSize)
}

const onOpenCreateModal = () => {
  planModal.mode = 'create'
  planModal.editingId = ''
  resetPlanForm()
  planModal.open = true
}

const onOpenEditModal = (record: AdminPlanItem) => {
  planModal.mode = 'edit'
  planModal.editingId = record.id
  planModal.form.code = record.code
  planModal.form.name = record.name
  planModal.form.billingCycle = record.billingCycle
  planModal.form.price = record.price
  planModal.form.status = record.status
  planModal.form.quotaJson = record.quotaJson
  planModal.open = true
}

const onSubmitPlan = async () => {
  if (!validatePlanForm()) {
    return
  }

  try {
    const normalizedCode = normalizeCode(planModal.form.code)
    const payload = {
      name: planModal.form.name.trim(),
      billingCycle: planModal.form.billingCycle,
      quotaJson: planModal.form.quotaJson.trim(),
      price: planModal.form.price,
      status: planModal.form.status,
    }
    if (planModal.mode === 'create') {
      await plansStore.createPlan({
        code: normalizedCode,
        ...payload,
      })
      message.success('套餐创建成功')
    } else {
      await plansStore.updatePlan(planModal.editingId, payload)
      message.success('套餐更新成功')
    }

    planModal.open = false
    await plansStore.fetchList('refresh')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('提交失败，请稍后重试')
    }
  }
}

const onToggleStatus = (record: AdminPlanItem) => {
  const targetStatus: PlanStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const actionText = targetStatus === 'ACTIVE' ? '启用' : '停用'
  Modal.confirm({
    title: `确认${actionText}该套餐？`,
    content: `套餐：${record.code} / ${record.name}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await plansStore.updatePlanStatus(record.id, targetStatus)
        await plansStore.fetchList('refresh')
        message.success(`套餐已${actionText}`)
      } catch (error) {
        if (error instanceof Error) {
          message.error(error.message)
        } else {
          message.error('状态更新失败，请稍后重试')
        }
      }
    },
  })
}

onMounted(async () => {
  await plansStore.initialize()
  syncFiltersFromStore()
})
</script>

<style scoped>
.plans-page {
  display: grid;
  gap: 16px;
}

.plans-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ecfeff 0%, #eff6ff 58%, #f8fafc 100%);
  border: 1px solid #cfe5ee;
}

.plans-title {
  margin: 0;
  font-size: 24px;
  color: #134e4a;
}

.plans-subtitle {
  margin: 8px 0 0;
  color: #3f4956;
  font-size: 14px;
}

.plans-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.plans-error {
  margin-top: -6px;
}

.plans-card {
  border-radius: 14px;
  border: 1px solid #d4e5ee;
}

.plans-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.plans-full-width {
  width: 100%;
}

@media (max-width: 1100px) {
  .plans-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .plans-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .plans-pagination {
    justify-content: center;
  }
}
</style>
