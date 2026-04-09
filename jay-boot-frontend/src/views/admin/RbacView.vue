<template>
  <section class="rbac-page">
    <header class="rbac-toolbar">
      <div>
        <h2 class="rbac-title">RBAC 权限编排</h2>
        <p class="rbac-subtitle">按 User-Role-Permission-API 结构维护权限矩阵，支持可视化编辑与策略保存。</p>
      </div>
      <div class="rbac-toolbar__actions">
        <a-button :loading="rbacStore.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button :disabled="!rbacStore.isMatrixDirty" @click="onResetClick">重置改动</a-button>
        <a-button type="primary" :loading="rbacStore.saving" :disabled="!rbacStore.isMatrixDirty" @click="onSaveClick">
          保存权限策略
        </a-button>
      </div>
    </header>

    <div class="rbac-meta">最后更新：{{ updatedAtText }}</div>

    <a-alert
      v-if="rbacStore.errorMessage"
      class="rbac-error"
      type="error"
      show-icon
      :message="rbacStore.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="rbacStore.loadingInitial && !rbacStore.hasAnyData" active :paragraph="{ rows: 8 }" />

    <template v-else>
      <a-row :gutter="16" class="rbac-section-row">
        <a-col :xs="24" :xl="8">
          <a-card class="rbac-card" :bordered="false" title="角色模板">
            <a-list v-if="rbacStore.hasRoleTemplates" :data-source="rbacStore.roleTemplates" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="role-item">
                  <p class="role-item__code">{{ item.code }}</p>
                  <p class="role-item__desc">{{ item.description }}</p>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无角色模板" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="16">
          <a-card class="rbac-card" :bordered="false" title="API 权限矩阵（可编辑）">
            <a-table
              v-if="rbacStore.hasPermissionMatrix"
              :columns="matrixColumns"
              :data-source="rbacStore.draftPermissionMatrix"
              :pagination="false"
              row-key="permissionCode"
              :scroll="{ x: 860 }"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'permissionCode'">
                  <code>{{ record.permissionCode }}</code>
                </template>
                <template v-else-if="column.key === 'owner'">
                  <a-select
                    size="small"
                    class="decision-select"
                    :value="record.owner"
                    :options="decisionOptions"
                    @change="(value: unknown) => onDecisionChange(record.permissionCode, 'owner', value)"
                  />
                </template>
                <template v-else-if="column.key === 'admin'">
                  <a-select
                    size="small"
                    class="decision-select"
                    :value="record.admin"
                    :options="decisionOptions"
                    @change="(value: unknown) => onDecisionChange(record.permissionCode, 'admin', value)"
                  />
                </template>
                <template v-else-if="column.key === 'developer'">
                  <a-select
                    size="small"
                    class="decision-select"
                    :value="record.developer"
                    :options="decisionOptions"
                    @change="(value: unknown) => onDecisionChange(record.permissionCode, 'developer', value)"
                  />
                </template>
                <template v-else-if="column.key === 'finance'">
                  <a-select
                    size="small"
                    class="decision-select"
                    :value="record.finance"
                    :options="decisionOptions"
                    @change="(value: unknown) => onDecisionChange(record.permissionCode, 'finance', value)"
                  />
                </template>
              </template>
            </a-table>
            <a-empty v-else description="暂无权限矩阵" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" class="rbac-section-row">
        <a-col :xs="24" :xl="12">
          <a-card class="rbac-card" :bordered="false" title="策略说明">
            <a-list v-if="rbacStore.hasPolicyNotes" :data-source="rbacStore.policyNotes" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="note-item">{{ item.content }}</a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无策略说明" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card class="rbac-card" :bordered="false" title="最近权限变更">
            <a-timeline v-if="rbacStore.hasRecentChanges" class="change-timeline">
              <a-timeline-item v-for="item in rbacStore.recentChanges" :key="item.id" color="blue">
                <div class="change-item__header">
                  <strong>{{ item.time }} {{ item.title }}</strong>
                  <a-tag :color="getChangeColor(item.type)">{{ getChangeText(item.type) }}</a-tag>
                </div>
                <p class="change-item__detail">{{ item.detail }}</p>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else description="暂无变更记录" />
          </a-card>
        </a-col>
      </a-row>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { RbacChangeType, RbacMatrixRoleKey, RbacPermissionDecision } from '../../api/admin/RbacApi'
import { useRbacStore } from '../../stores/admin/rbac'

const rbacStore = useRbacStore()

const matrixColumns = [
  { title: '权限码', dataIndex: 'permissionCode', key: 'permissionCode', width: 260 },
  { title: 'owner', dataIndex: 'owner', key: 'owner', width: 130 },
  { title: 'admin', dataIndex: 'admin', key: 'admin', width: 130 },
  { title: 'developer', dataIndex: 'developer', key: 'developer', width: 130 },
  { title: 'finance', dataIndex: 'finance', key: 'finance', width: 130 },
]

const decisionOptions: Array<{ label: string; value: RbacPermissionDecision }> = [
  { label: '允许', value: 'allow' },
  { label: '审批后', value: 'approval' },
  { label: '禁止', value: 'deny' },
]

const updatedAtText = computed(() => {
  if (!rbacStore.updatedAt) {
    return '--'
  }
  return new Date(rbacStore.updatedAt).toLocaleString('zh-CN', { hour12: false })
})

const isPermissionDecision = (value: unknown): value is RbacPermissionDecision => {
  return value === 'allow' || value === 'approval' || value === 'deny'
}

const getChangeText = (type: RbacChangeType) => {
  if (type === 'create') {
    return '新增'
  }
  if (type === 'update') {
    return '更新'
  }
  return '删除'
}

const getChangeColor = (type: RbacChangeType) => {
  if (type === 'create') {
    return 'success'
  }
  if (type === 'update') {
    return 'processing'
  }
  return 'default'
}

const onDecisionChange = (permissionCode: string, roleKey: RbacMatrixRoleKey, value: unknown) => {
  if (!isPermissionDecision(value)) {
    return
  }
  rbacStore.setPermissionDecision(permissionCode, roleKey, value)
}

const onRefreshClick = async () => {
  await rbacStore.refresh()
  if (!rbacStore.errorMessage) {
    message.success('RBAC 数据已刷新')
  }
}

const onResetClick = () => {
  rbacStore.resetDraftMatrix()
  message.success('已恢复为最新保存版本')
}

const onSaveClick = async () => {
  try {
    const changed = await rbacStore.savePermissionMatrix()
    if (changed) {
      message.success('权限策略保存成功')
    } else {
      message.info('未检测到变更，无需保存')
    }
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('保存失败，请稍后重试')
    }
  }
}

const onRetryClick = async () => {
  await rbacStore.retry()
}

onMounted(() => {
  void rbacStore.initialize()
})
</script>

<style scoped>
.rbac-page {
  display: grid;
  gap: 16px;
}

.rbac-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #f5f3ff 0%, #eef2ff 56%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.rbac-title {
  margin: 0;
  font-size: 24px;
  color: #1e1b4b;
}

.rbac-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.rbac-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rbac-meta {
  font-size: 12px;
  color: #64748b;
}

.rbac-error {
  margin-top: -6px;
}

.rbac-section-row {
  margin-top: 2px;
}

.rbac-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.role-item {
  display: grid;
  gap: 4px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.role-item__code {
  margin: 0;
  color: #1f2937;
  font-weight: 600;
  font-size: 13px;
}

.role-item__desc {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.decision-select {
  width: 100%;
}

.note-item {
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
  color: #334155;
}

.change-timeline {
  margin-top: 8px;
}

.change-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.change-item__detail {
  margin: 6px 0 0;
  color: #64748b;
}

@media (max-width: 1100px) {
  .rbac-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .rbac-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
