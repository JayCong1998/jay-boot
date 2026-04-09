<template>
  <section class="apikey-page">
    <header class="apikey-toolbar">
      <div>
        <h2 class="apikey-title">API Key 生命周期管理</h2>
        <p class="apikey-subtitle">支持创建、轮换、启用/禁用与限流策略绑定，密钥仅展示一次。</p>
      </div>
      <div class="apikey-toolbar__actions">
        <a-button :loading="apiKeyStore.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button type="primary" :loading="apiKeyStore.rotating" @click="onRotateActiveClick">
          <template #icon>
            <SyncOutlined />
          </template>
          轮换密钥
        </a-button>
      </div>
    </header>

    <div class="apikey-meta">最后更新：{{ updatedAtText }}</div>

    <a-alert
      v-if="apiKeyStore.errorMessage"
      class="apikey-error"
      type="error"
      show-icon
      :message="apiKeyStore.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="apiKeyStore.loadingInitial && !apiKeyStore.hasAnyData" active :paragraph="{ rows: 8 }" />

    <template v-else>
      <section class="apikey-kpi-grid">
        <a-card class="apikey-kpi-card" :bordered="false">
          <p class="apikey-kpi-card__label">活跃密钥</p>
          <p class="apikey-kpi-card__value">{{ apiKeyStore.activeKeyCount }}</p>
          <p class="apikey-kpi-card__hint">已启用的 Key 可参与调用</p>
        </a-card>
        <a-card class="apikey-kpi-card" :bordered="false">
          <p class="apikey-kpi-card__label">总密钥数</p>
          <p class="apikey-kpi-card__value">{{ apiKeyStore.keys.length }}</p>
          <p class="apikey-kpi-card__hint">建议按环境拆分管理</p>
        </a-card>
      </section>

      <a-row :gutter="16" class="apikey-section-row">
        <a-col :xs="24" :xl="16">
          <a-card class="apikey-card" :bordered="false" title="Key 列表">
            <a-table
              v-if="apiKeyStore.hasKeyData"
              :columns="keyColumns"
              :data-source="apiKeyStore.keys"
              :pagination="false"
              row-key="id"
              size="small"
              :scroll="{ x: 920 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'keyPrefix'">
                  <code>{{ record.keyPrefix }}</code>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space size="small">
                    <a-button
                      type="link"
                      size="small"
                      :disabled="apiKeyStore.rotating || record.status === 'disabled'"
                      @click="onRotateRecordClick(record.id)"
                    >
                      轮换
                    </a-button>
                    <a-button
                      type="link"
                      size="small"
                      :disabled="apiKeyStore.updatingStatus"
                      @click="onToggleStatusClick(record.id, record.status)"
                    >
                      {{ getToggleStatusText(record.status) }}
                    </a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
            <a-empty v-else description="暂无 Key 数据" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="8">
          <a-card class="apikey-card" :bordered="false" title="新建 Key">
            <a-form layout="vertical">
              <a-form-item label="Key 名称" required>
                <a-input v-model:value="nameInput" :maxlength="40" placeholder="例如：public-gateway" />
              </a-form-item>
              <a-form-item label="权限范围" required>
                <a-select v-model:value="scopeInput" :options="scopeOptions" placeholder="请选择权限范围" />
              </a-form-item>
              <a-form-item label="限流方案" required>
                <a-select v-model:value="limitPolicyInput" :options="limitOptions" placeholder="请选择限流方案" />
              </a-form-item>
              <a-alert class="apikey-create-note" type="info" show-icon message="创建后仅展示一次明文，请及时复制保存。" />
              <a-button block type="primary" :loading="apiKeyStore.creating" @click="onCreateClick">生成 API Key</a-button>
            </a-form>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" class="apikey-section-row">
        <a-col :xs="24" :xl="12">
          <a-card class="apikey-card" :bordered="false" title="配额提醒">
            <a-list v-if="apiKeyStore.hasQuotaData" :data-source="apiKeyStore.quotaReminders" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="quota-item">
                  <div class="quota-item__header">
                    <strong>{{ item.label }}</strong>
                    <span>{{ item.percent }}%</span>
                  </div>
                  <a-progress :percent="item.percent" size="small" :show-info="false" />
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无配额数据" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card class="apikey-card" :bordered="false" title="安全建议">
            <a-list v-if="apiKeyStore.hasSecurityTips" :data-source="apiKeyStore.securityTips" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="tip-item">{{ item }}</a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无安全建议" />
          </a-card>
        </a-col>
      </a-row>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ReloadOutlined, SyncOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import type { ApiKeyStatus } from '../../api/admin/ApiKeyApi'
import { useApiKeyStore } from '../../stores/admin/apikey'

const apiKeyStore = useApiKeyStore()

const nameInput = ref('')
const scopeInput = ref('')
const limitPolicyInput = ref('')

const keyColumns = [
  { title: '名称', dataIndex: 'name', key: 'name', width: 140 },
  { title: 'Key 前缀', dataIndex: 'keyPrefix', key: 'keyPrefix', width: 170 },
  { title: '权限范围', dataIndex: 'scope', key: 'scope', width: 130 },
  { title: '限流策略', dataIndex: 'limitPolicy', key: 'limitPolicy', width: 180 },
  { title: '最近使用', dataIndex: 'lastUsedText', key: 'lastUsedText', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '操作', key: 'action', width: 160 },
]

const statusTextMap: Record<ApiKeyStatus, string> = {
  active: '启用',
  pending_disable: '待禁用',
  disabled: '已禁用',
}

const statusColorMap: Record<ApiKeyStatus, string> = {
  active: 'success',
  pending_disable: 'gold',
  disabled: 'default',
}

const scopeOptions = computed(() => apiKeyStore.scopeOptions.map((item) => ({ label: item, value: item })))
const limitOptions = computed(() => apiKeyStore.limitOptions.map((item) => ({ label: item, value: item })))

const updatedAtText = computed(() => {
  if (!apiKeyStore.updatedAt) {
    return '--'
  }
  return new Date(apiKeyStore.updatedAt).toLocaleString('zh-CN', { hour12: false })
})

const getStatusText = (status: ApiKeyStatus) => statusTextMap[status]
const getStatusColor = (status: ApiKeyStatus) => statusColorMap[status]

const getToggleStatusText = (status: ApiKeyStatus) => {
  if (status === 'active') {
    return '设为待禁用'
  }
  if (status === 'pending_disable') {
    return '直接禁用'
  }
  return '重新启用'
}

const getNextStatus = (status: ApiKeyStatus): ApiKeyStatus => {
  if (status === 'active') {
    return 'pending_disable'
  }
  if (status === 'pending_disable') {
    return 'disabled'
  }
  return 'active'
}

const syncCreateFormDefaults = (force = false) => {
  if ((force || !scopeInput.value) && apiKeyStore.scopeOptions.length > 0) {
    scopeInput.value = apiKeyStore.scopeOptions[0]
  }
  if ((force || !limitPolicyInput.value) && apiKeyStore.limitOptions.length > 0) {
    limitPolicyInput.value = apiKeyStore.limitOptions[0]
  }
}

const showSecret = (secret: string) => {
  if (!secret) {
    return
  }
  Modal.info({
    title: '新密钥（仅展示一次）',
    content: secret,
    okText: '我已复制',
  })
}

const onRefreshClick = async () => {
  await apiKeyStore.refresh()
  syncCreateFormDefaults(false)
  if (!apiKeyStore.errorMessage) {
    message.success('API Key 数据已刷新')
  }
}

const onRetryClick = async () => {
  await apiKeyStore.retry()
  syncCreateFormDefaults(false)
}

const onCreateClick = async () => {
  const name = nameInput.value.trim()
  if (!name) {
    message.warning('请输入 Key 名称')
    return
  }
  if (!scopeInput.value) {
    message.warning('请选择权限范围')
    return
  }
  if (!limitPolicyInput.value) {
    message.warning('请选择限流方案')
    return
  }

  try {
    const secret = await apiKeyStore.createKey({
      name,
      scope: scopeInput.value,
      limitPolicy: limitPolicyInput.value,
    })
    nameInput.value = ''
    showSecret(secret)
    apiKeyStore.clearLatestSecret()
    message.success('API Key 创建成功')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('创建失败，请稍后重试')
    }
  }
}

const onRotateRecordClick = async (id: string) => {
  try {
    const secret = await apiKeyStore.rotateKey(id)
    showSecret(secret)
    apiKeyStore.clearLatestSecret()
    message.success('API Key 已轮换')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('轮换失败，请稍后重试')
    }
  }
}

const onRotateActiveClick = async () => {
  const activeId = apiKeyStore.firstActiveKeyId
  if (!activeId) {
    message.info('当前没有可轮换的启用密钥')
    return
  }
  await onRotateRecordClick(activeId)
}

const onToggleStatusClick = async (id: string, status: ApiKeyStatus) => {
  try {
    await apiKeyStore.updateKeyStatus(id, getNextStatus(status))
    message.success('API Key 状态已更新')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('状态更新失败，请稍后重试')
    }
  }
}

onMounted(async () => {
  await apiKeyStore.initialize()
  syncCreateFormDefaults(true)
})
</script>

<style scoped>
.apikey-page {
  display: grid;
  gap: 16px;
}

.apikey-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #eef7ff 0%, #f3f9ff 54%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.apikey-title {
  margin: 0;
  font-size: 24px;
  color: #0c4a6e;
}

.apikey-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.apikey-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.apikey-meta {
  font-size: 12px;
  color: #64748b;
}

.apikey-error {
  margin-top: -6px;
}

.apikey-kpi-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.apikey-kpi-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.apikey-kpi-card__label {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.apikey-kpi-card__value {
  margin: 10px 0 6px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.apikey-kpi-card__hint {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}

.apikey-section-row {
  margin-top: 2px;
}

.apikey-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.apikey-create-note {
  margin-bottom: 14px;
}

.quota-item {
  display: grid;
  gap: 8px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.quota-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.tip-item {
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
  color: #334155;
}

@media (max-width: 1100px) {
  .apikey-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .apikey-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .apikey-kpi-grid {
    grid-template-columns: 1fr;
  }
}
</style>