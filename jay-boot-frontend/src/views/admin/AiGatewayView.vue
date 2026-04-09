<template>
  <section class="aigw-page">
    <header class="aigw-toolbar">
      <div>
        <h2 class="aigw-title">AI Gateway 中台</h2>
        <p class="aigw-subtitle">统一接入模型供应商，管理路由策略、调用成本和失败降级路径。</p>
      </div>
      <div class="aigw-toolbar__actions">
        <a-button :loading="aiGatewayStore.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button
          type="primary"
          :loading="aiGatewayStore.savingPolicies"
          :disabled="!aiGatewayStore.isRoutePolicyDirty"
          @click="onSavePoliciesClick"
        >
          保存策略
        </a-button>
      </div>
    </header>

    <div class="aigw-meta">最后更新：{{ updatedAtText }}</div>

    <a-alert
      v-if="aiGatewayStore.errorMessage"
      class="aigw-error"
      type="error"
      show-icon
      :message="aiGatewayStore.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="aiGatewayStore.loadingInitial && !aiGatewayStore.hasAnyData" active :paragraph="{ rows: 8 }" />

    <template v-else>
      <section class="aigw-action-panel">
        <a-space :size="12" wrap>
          <a-select v-model:value="selectedProviderId" :options="providerOptions" style="width: 180px" />
          <a-button :loading="aiGatewayStore.switchingProvider" @click="onSwitchProviderClick">切换 Provider</a-button>
          <a-select v-model:value="testEndpoint" :options="endpointOptions" style="width: 180px" />
          <a-button type="primary" :loading="aiGatewayStore.testingCall" @click="onTestCallClick">接口试调用</a-button>
          <a-button :disabled="!aiGatewayStore.isRoutePolicyDirty" @click="onResetPoliciesClick">重置改动</a-button>
        </a-space>
      </section>

      <a-alert
        v-if="aiGatewayStore.latestTestResult"
        class="aigw-test-result"
        show-icon
        :type="getTestAlertType(aiGatewayStore.latestTestResult.status)"
        :message="`[${aiGatewayStore.latestTestResult.time}] ${aiGatewayStore.latestTestResult.message}`"
        :description="`traceId: ${aiGatewayStore.latestTestResult.traceId} | endpoint: ${aiGatewayStore.latestTestResult.endpoint}`"
      />

      <a-row :gutter="16" class="aigw-section-row">
        <a-col :xs="24" :xl="8">
          <a-card class="aigw-card" :bordered="false" title="Provider 状态">
            <a-list v-if="aiGatewayStore.hasProviderData" :data-source="aiGatewayStore.providers" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="provider-item">
                  <div class="provider-item__header">
                    <strong>{{ item.name }}</strong>
                    <a-tag :color="getProviderColor(item.status)">{{ getProviderText(item.status) }}</a-tag>
                  </div>
                  <div class="provider-item__footer">
                    <span>{{ item.detail }}</span>
                    <a-tag v-if="item.id === aiGatewayStore.activeProviderId" color="processing">当前</a-tag>
                  </div>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无 Provider 数据" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="16">
          <a-card class="aigw-card" :bordered="false" title="统一接口策略">
            <a-table
              v-if="aiGatewayStore.hasRoutePolicyData"
              :columns="routeColumns"
              :data-source="aiGatewayStore.draftRoutePolicies"
              :pagination="false"
              row-key="id"
              size="small"
              :scroll="{ x: 920 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'endpoint'">
                  <code>{{ record.endpoint }}</code>
                </template>
                <template v-else-if="column.key === 'defaultModel'">
                  <a-select
                    size="small"
                    style="width: 190px"
                    :value="record.defaultModel"
                    :options="getModelOptions(record.endpoint)"
                    @change="(value: unknown) => onModelChange(record.id, value)"
                  />
                </template>
                <template v-else-if="column.key === 'timeoutPolicy'">
                  <a-input
                    size="small"
                    :value="record.timeoutPolicy"
                    @change="(event: Event) => onTimeoutChange(record.id, event)"
                  />
                </template>
                <template v-else-if="column.key === 'billingUnit'">
                  <a-tag color="blue">{{ record.billingUnit }}</a-tag>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="getRouteColor(record.status)">{{ getRouteText(record.status) }}</a-tag>
                </template>
              </template>
            </a-table>
            <a-empty v-else description="暂无路由策略" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" class="aigw-section-row">
        <a-col :xs="24" :xl="12">
          <a-card class="aigw-card" :bordered="false" title="本周 AI 成本趋势">
            <a-list v-if="aiGatewayStore.hasCostTrendData" :data-source="aiGatewayStore.costTrend" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="cost-item">
                  <div class="cost-item__header">
                    <strong>{{ item.label }}</strong>
                    <span>{{ item.costText }}</span>
                  </div>
                  <a-progress :percent="item.percent" size="small" :show-info="false" />
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无成本趋势" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card class="aigw-card" :bordered="false" title="失败降级路径">
            <a-timeline v-if="aiGatewayStore.hasFallbackData">
              <a-timeline-item v-for="item in aiGatewayStore.fallbackSteps" :key="item.id" color="blue">
                <strong>{{ item.title }}</strong>
                <p class="fallback-item__detail">{{ item.detail }}</p>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else description="暂无降级路径" />
          </a-card>
        </a-col>
      </a-row>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { AiGatewayProviderStatus, AiGatewayRouteStatus, AiGatewayTestStatus } from '../../api/admin/AiGatewayApi'
import { useAiGatewayStore } from '../../stores/admin/aiGateway'

const aiGatewayStore = useAiGatewayStore()

const selectedProviderId = ref('')
const testEndpoint = ref('')

const routeColumns = [
  { title: '接口', dataIndex: 'endpoint', key: 'endpoint', width: 180 },
  { title: '默认模型', dataIndex: 'defaultModel', key: 'defaultModel', width: 220 },
  { title: '超时策略', dataIndex: 'timeoutPolicy', key: 'timeoutPolicy', width: 230 },
  { title: '计费单位', dataIndex: 'billingUnit', key: 'billingUnit', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
]

const providerTextMap: Record<AiGatewayProviderStatus, string> = {
  online: '在线',
  warning: '预警',
  offline: '离线',
}

const providerColorMap: Record<AiGatewayProviderStatus, string> = {
  online: 'success',
  warning: 'gold',
  offline: 'error',
}

const routeTextMap: Record<AiGatewayRouteStatus, string> = {
  available: '可用',
  high_load: '高负载',
  degraded: '降级',
}

const routeColorMap: Record<AiGatewayRouteStatus, string> = {
  available: 'success',
  high_load: 'gold',
  degraded: 'error',
}

const modelOptionsMap: Record<string, Array<{ label: string; value: string }>> = {
  '/ai/chat': [
    { label: 'gpt-4.1-mini', value: 'gpt-4.1-mini' },
    { label: 'gpt-4o-mini', value: 'gpt-4o-mini' },
    { label: 'claude-3-5-sonnet', value: 'claude-3-5-sonnet' },
  ],
  '/ai/embedding': [
    { label: 'text-embedding-3-large', value: 'text-embedding-3-large' },
    { label: 'text-embedding-3-small', value: 'text-embedding-3-small' },
  ],
  '/ai/image': [
    { label: 'gpt-image-1', value: 'gpt-image-1' },
    { label: 'sdxl-local', value: 'sdxl-local' },
  ],
}

const providerOptions = computed(() => aiGatewayStore.providers.map((item) => ({ label: item.name, value: item.id })))

const endpointOptions = computed(() =>
  aiGatewayStore.draftRoutePolicies.map((item) => ({ label: item.endpoint, value: item.endpoint })),
)

const updatedAtText = computed(() => {
  if (!aiGatewayStore.updatedAt) {
    return '--'
  }
  return new Date(aiGatewayStore.updatedAt).toLocaleString('zh-CN', { hour12: false })
})

const syncSelections = (force = false) => {
  if ((force || !selectedProviderId.value) && aiGatewayStore.activeProviderId) {
    selectedProviderId.value = aiGatewayStore.activeProviderId
  }
  if ((force || !testEndpoint.value) && aiGatewayStore.draftRoutePolicies.length > 0) {
    testEndpoint.value = aiGatewayStore.draftRoutePolicies[0].endpoint
  }
}

const getProviderText = (status: AiGatewayProviderStatus) => providerTextMap[status]
const getProviderColor = (status: AiGatewayProviderStatus) => providerColorMap[status]
const getRouteText = (status: AiGatewayRouteStatus) => routeTextMap[status]
const getRouteColor = (status: AiGatewayRouteStatus) => routeColorMap[status]

const getModelOptions = (endpoint: string) => {
  return modelOptionsMap[endpoint] ?? [{ label: 'custom-model', value: 'custom-model' }]
}

const getTestAlertType = (status: AiGatewayTestStatus) => {
  if (status === 'success') {
    return 'success'
  }
  if (status === 'retrying') {
    return 'warning'
  }
  return 'error'
}

const onModelChange = (id: string, value: unknown) => {
  if (typeof value !== 'string' || !value) {
    return
  }
  aiGatewayStore.setRouteDefaultModel(id, value)
}

const onTimeoutChange = (id: string, event: Event) => {
  const target = event.target as HTMLInputElement
  aiGatewayStore.setRouteTimeoutPolicy(id, target.value)
}

const onRefreshClick = async () => {
  await aiGatewayStore.refresh()
  syncSelections(false)
  if (!aiGatewayStore.errorMessage) {
    message.success('AI Gateway 数据已刷新')
  }
}

const onRetryClick = async () => {
  await aiGatewayStore.retry()
  syncSelections(false)
}

const onSwitchProviderClick = async () => {
  if (!selectedProviderId.value) {
    message.warning('请选择 Provider')
    return
  }
  try {
    const changed = await aiGatewayStore.switchProvider(selectedProviderId.value)
    syncSelections(false)
    if (changed) {
      message.success('Provider 切换成功')
    } else {
      message.info('当前已是该 Provider')
    }
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('Provider 切换失败，请稍后重试')
    }
  }
}

const onSavePoliciesClick = async () => {
  try {
    const changed = await aiGatewayStore.saveRoutePolicies()
    if (changed) {
      message.success('路由策略保存成功')
    } else {
      message.info('未检测到策略变更')
    }
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('策略保存失败，请稍后重试')
    }
  }
}

const onResetPoliciesClick = () => {
  aiGatewayStore.resetRoutePolicies()
  message.success('已恢复为最新策略')
}

const onTestCallClick = async () => {
  if (!testEndpoint.value) {
    message.warning('请选择试调用接口')
    return
  }
  try {
    const result = await aiGatewayStore.runTestCall(testEndpoint.value)
    if (result?.status === 'success') {
      message.success(result.message)
    } else if (result?.status === 'retrying') {
      message.warning(result.message)
    } else if (result?.status === 'failed') {
      message.error(result.message)
    }
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('接口试调用失败，请稍后重试')
    }
  }
}

onMounted(async () => {
  await aiGatewayStore.initialize()
  syncSelections(true)
})
</script>

<style scoped>
.aigw-page {
  display: grid;
  gap: 16px;
}

.aigw-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #eff6ff 0%, #f0f9ff 56%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.aigw-title {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.aigw-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.aigw-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.aigw-meta {
  font-size: 12px;
  color: #64748b;
}

.aigw-error {
  margin-top: -6px;
}

.aigw-action-panel {
  padding: 12px;
  border: 1px solid #dce6f0;
  border-radius: 12px;
  background: #ffffff;
}

.aigw-test-result {
  margin-top: -2px;
}

.aigw-section-row {
  margin-top: 2px;
}

.aigw-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.provider-item {
  display: grid;
  gap: 8px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.provider-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.provider-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #64748b;
  font-size: 12px;
}

.cost-item {
  display: grid;
  gap: 8px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.cost-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.fallback-item__detail {
  margin: 6px 0 0;
  color: #64748b;
}

@media (max-width: 1100px) {
  .aigw-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .aigw-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>