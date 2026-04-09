<template>
  <section class="tenant-page">
    <header class="tenant-toolbar">
      <div>
        <h2 class="tenant-title">Tenant 隔离与 Workspace 管理</h2>
        <p class="tenant-subtitle">维护当前工作区核心配置，确保 tenant_id 边界清晰和成员权限可追踪。</p>
      </div>
      <div class="tenant-toolbar__actions">
        <a-button :loading="tenantStore.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button @click="onSwitchWorkspace">切换 Workspace</a-button>
        <a-button
          type="primary"
          :loading="tenantStore.savingName"
          :disabled="!tenantStore.hasTenantData || !tenantStore.isNameDirty"
          @click="onSaveClick"
        >
          保存配置
        </a-button>
      </div>
    </header>

    <div class="tenant-meta">最后更新：{{ updatedAtText }}</div>

    <a-alert
      v-if="tenantStore.errorMessage"
      class="tenant-error"
      type="error"
      show-icon
      :message="tenantStore.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="tenantStore.loadingInitial && !tenantStore.hasTenantData" active :paragraph="{ rows: 6 }" />

    <template v-else-if="tenantStore.hasTenantData && tenantStore.tenant">
      <a-row :gutter="16" class="tenant-section-row">
        <a-col :xs="24" :xl="16">
          <a-card class="tenant-card" :bordered="false" title="Workspace 基础设置">
            <a-form layout="vertical">
              <a-row :gutter="12">
                <a-col :xs="24" :sm="12">
                  <a-form-item label="Workspace 名称" required>
                    <a-input :value="tenantStore.nameInput" @input="onNameInputChange" placeholder="请输入工作区名称" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12">
                  <a-form-item label="当前套餐">
                    <a-input :value="tenantStore.tenant.planName" disabled />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12">
                  <a-form-item label="数据区域">
                    <a-input :value="tenantStore.tenant.region" disabled />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :sm="12">
                  <a-form-item label="Owner">
                    <a-input :value="tenantStore.tenant.ownerEmail" disabled />
                  </a-form-item>
                </a-col>
                <a-col :xs="24">
                  <a-form-item label="隔离策略说明">
                    <a-textarea :value="tenantStore.tenant.isolationPolicy" :auto-size="{ minRows: 2, maxRows: 4 }" disabled />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="8">
          <a-card class="tenant-card tenant-health-card" :bordered="false" title="租户健康">
            <ul class="tenant-health-list">
              <li>
                <span>租户ID</span>
                <code>{{ tenantStore.tenant.tenantId }}</code>
              </li>
              <li>
                <span>成员数</span>
                <strong>{{ tenantStore.tenant.memberCount }} / {{ tenantStore.tenant.memberLimit }}</strong>
              </li>
              <li>
                <span>存储配额</span>
                <strong>{{ tenantStore.tenant.storageUsagePercent }}%</strong>
              </li>
              <li>
                <span>隔离审计</span>
                <a-tag :color="tenantStore.tenant.isolationStatus === 'pass' ? 'success' : 'error'">
                  {{ tenantStore.tenant.isolationStatus === 'pass' ? '通过' : '风险' }}
                </a-tag>
              </li>
            </ul>
          </a-card>
        </a-col>
      </a-row>

      <a-card class="tenant-card" :bordered="false" title="成员与角色">
        <a-table
          v-if="tenantStore.hasMemberData"
          :columns="memberColumns"
          :data-source="tenantStore.members"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 720 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="getMemberStatusColor(record.status)">{{ getMemberStatusText(record.status) }}</a-tag>
            </template>
          </template>
        </a-table>
        <a-empty v-else description="暂无成员数据" />
      </a-card>
    </template>

    <a-empty v-else description="暂无租户信息" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { TenantMemberStatus } from '../../api/admin/TenantApi'
import { useTenantStore } from '../../stores/admin/tenant'

const tenantStore = useTenantStore()

const memberColumns = [
  { title: '成员', dataIndex: 'name', key: 'name', width: 120 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 220 },
  { title: '租户角色', dataIndex: 'role', key: 'role', width: 160 },
  { title: '最近活跃', dataIndex: 'lastActiveText', key: 'lastActiveText', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
]

const memberStatusTextMap: Record<TenantMemberStatus, string> = {
  active: '活跃',
  pending: '待确认',
  disabled: '已禁用',
}

const memberStatusColorMap: Record<TenantMemberStatus, string> = {
  active: 'success',
  pending: 'gold',
  disabled: 'default',
}

const updatedAtText = computed(() => {
  if (!tenantStore.updatedAt) {
    return '--'
  }
  return new Date(tenantStore.updatedAt).toLocaleString('zh-CN', { hour12: false })
})

const isMemberStatus = (value: unknown): value is TenantMemberStatus => {
  return value === 'active' || value === 'pending' || value === 'disabled'
}

const getMemberStatusText = (status: unknown) => {
  if (!isMemberStatus(status)) {
    return memberStatusTextMap.pending
  }
  return memberStatusTextMap[status]
}

const getMemberStatusColor = (status: unknown) => {
  if (!isMemberStatus(status)) {
    return memberStatusColorMap.pending
  }
  return memberStatusColorMap[status]
}

const onNameInputChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  tenantStore.setNameInput(target.value)
}

const onRefreshClick = async () => {
  await tenantStore.refresh()
  if (!tenantStore.errorMessage) {
    message.success('租户信息已刷新')
  }
}

const onSwitchWorkspace = () => {
  message.info('Mock 版本暂不支持多工作区切换')
}

const onRetryClick = async () => {
  await tenantStore.retry()
}

const onSaveClick = async () => {
  try {
    const changed = await tenantStore.saveTenantName()
    if (changed) {
      message.success('租户配置已保存')
    } else {
      message.info('名称未变化，无需保存')
    }
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('保存失败，请稍后重试')
    }
  }
}

onMounted(() => {
  void tenantStore.initialize()
})
</script>

<style scoped>
.tenant-page {
  display: grid;
  gap: 16px;
}

.tenant-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #f5f3ff 0%, #eef2ff 56%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.tenant-title {
  margin: 0;
  font-size: 24px;
  color: #1e1b4b;
}

.tenant-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.tenant-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tenant-meta {
  font-size: 12px;
  color: #64748b;
}

.tenant-error {
  margin-top: -6px;
}

.tenant-section-row {
  margin-top: 2px;
}

.tenant-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.tenant-health-card {
  min-height: 100%;
}

.tenant-health-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.tenant-health-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
}

.tenant-health-list span {
  color: #475569;
  font-size: 13px;
}

.tenant-health-list strong {
  color: #0f172a;
}

.tenant-health-list code {
  color: #2563eb;
}

@media (max-width: 1100px) {
  .tenant-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .tenant-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>