<template>
  <section class="dashboard-page">
    <header class="dashboard-toolbar">
      <div>
        <h2 class="dashboard-title">SaaS 运营控制台</h2>
        <p class="dashboard-subtitle">聚焦核心指标、告警与关键事件流，帮助团队快速判断系统状态。</p>
      </div>
      <div class="dashboard-toolbar__actions">
        <a-segmented :options="rangeOptions" :value="pageState.range" @change="onRangeChange" />
        <a-button type="primary" :loading="pageState.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          局部刷新
        </a-button>
      </div>
    </header>

    <div class="dashboard-meta">最后更新：{{ updatedAtText }}</div>

    <a-alert
      v-if="pageState.errorMessage"
      class="dashboard-error"
      type="error"
      show-icon
      :message="pageState.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <section class="dashboard-block">
      <h3 class="dashboard-block__title">核心指标</h3>
      <a-row :gutter="16">
        <a-col v-for="kpi in pageState.kpis" :key="kpi.key" :xs="24" :sm="12" :xl="6" class="dashboard-col">
          <a-card class="kpi-card" :bordered="false">
            <p class="kpi-title">{{ kpi.title }}</p>
            <p class="kpi-value">{{ kpi.valueText }}</p>
            <p class="kpi-trend" :class="getKpiTrendClass(kpi.trendDirection)">{{ kpi.trendText }}</p>
          </a-card>
        </a-col>
      </a-row>
      <a-empty v-if="!hasKpiData" description="暂无核心指标数据" />
    </section>

    <a-row :gutter="16" class="dashboard-section-row">
      <a-col :xs="24" :xl="10">
        <a-card title="今日告警" class="dashboard-panel" :bordered="false">
          <a-list v-if="hasAlertData" :data-source="pageState.alerts" :split="false">
            <template #renderItem="{ item }">
              <a-list-item class="alert-item">
                <div class="alert-item__header">
                  <a-space>
                    <a-tag :color="getAlertColor(item.level)">{{ getAlertText(item.level) }}</a-tag>
                    <span class="alert-module">{{ item.module }}</span>
                  </a-space>
                  <span class="alert-time">{{ item.time }}</span>
                </div>
                <p class="alert-message">{{ item.message }}</p>
              </a-list-item>
            </template>
          </a-list>
          <a-empty v-else description="暂无告警" />
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="14">
        <a-card title="关键事件流" class="dashboard-panel" :bordered="false">
          <a-table
            v-if="hasEventData"
            :columns="eventColumns"
            :data-source="pageState.events"
            :pagination="false"
            row-key="id"
            size="small"
            :scroll="{ x: 680 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'traceId'">
                <code>{{ record.traceId }}</code>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-tag :color="getEventStatusColor(record.status)">{{ getEventStatusText(record.status) }}</a-tag>
              </template>
            </template>
          </a-table>
          <a-empty v-else description="暂无关键事件" />
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import {
  getDashboardOverviewApi,
  type DashboardAlertItem,
  type DashboardAlertLevel,
  type DashboardEventItem,
  type DashboardEventStatus,
  type DashboardKpiItem,
  type DashboardRange,
  type DashboardTrendDirection,
} from '../../api/admin/DashboardApi'

// 页面状态
const pageState = reactive({
  range: '7d' as DashboardRange,
  updatedAt: '',
  kpis: [] as DashboardKpiItem[],
  alerts: [] as DashboardAlertItem[],
  events: [] as DashboardEventItem[],
  loadingInitial: false,
  refreshing: false,
  errorMessage: '',
})

// 计算属性
const hasKpiData = computed(() => pageState.kpis.length > 0)
const hasAlertData = computed(() => pageState.alerts.length > 0)
const hasEventData = computed(() => pageState.events.length > 0)

const updatedAtText = computed(() => {
  if (!pageState.updatedAt) return '--'
  return new Date(pageState.updatedAt).toLocaleString('zh-CN', { hour12: false })
})

const rangeOptions: Array<{ label: string; value: DashboardRange }> = [
  { label: '近 24 小时', value: '24h' },
  { label: '近 7 天', value: '7d' },
  { label: '近 30 天', value: '30d' },
]

const eventColumns = [
  { title: '时间', dataIndex: 'time', key: 'time', width: 90 },
  { title: '模块', dataIndex: 'module', key: 'module', width: 110 },
  { title: '事件', dataIndex: 'event', key: 'event' },
  { title: '追踪 ID', dataIndex: 'traceId', key: 'traceId', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
]

const alertLevelTextMap: Record<DashboardAlertLevel, string> = {
  info: '信息',
  warn: '告警',
  error: '严重',
}

const alertColorMap: Record<DashboardAlertLevel, string> = {
  info: 'blue',
  warn: 'orange',
  error: 'red',
}

const eventStatusTextMap: Record<DashboardEventStatus, string> = {
  success: '完成',
  retrying: '重试中',
  failed: '失败',
}

const eventStatusColorMap: Record<DashboardEventStatus, string> = {
  success: 'success',
  retrying: 'gold',
  failed: 'error',
}

const isDashboardRange = (value: unknown): value is DashboardRange => {
  return value === '24h' || value === '7d' || value === '30d'
}

const getKpiTrendClass = (trendDirection: DashboardTrendDirection) => {
  if (trendDirection === 'up') return 'kpi-trend--up'
  if (trendDirection === 'down') return 'kpi-trend--down'
  return 'kpi-trend--flat'
}

const getAlertText = (level: DashboardAlertLevel) => alertLevelTextMap[level]
const getAlertColor = (level: DashboardAlertLevel) => alertColorMap[level]
const getEventStatusText = (status: DashboardEventStatus) => eventStatusTextMap[status]
const getEventStatusColor = (status: DashboardEventStatus) => eventStatusColorMap[status]

// 获取数据
const fetchOverview = async (mode: 'initial' | 'range-change' | 'refresh') => {
  pageState.errorMessage = ''

  if (mode === 'initial') pageState.loadingInitial = true
  if (mode === 'refresh') pageState.refreshing = true

  try {
    const data = await getDashboardOverviewApi({ range: pageState.range })
    pageState.range = data.range
    pageState.updatedAt = data.updatedAt
    pageState.kpis = data.kpis
    pageState.alerts = data.alerts
    pageState.events = data.events
  } catch (error) {
    pageState.errorMessage = error instanceof Error ? error.message : '加载仪表盘失败，请稍后重试'
  } finally {
    pageState.loadingInitial = false
    pageState.refreshing = false
  }
}

const onRangeChange = async (value: string | number) => {
  if (!isDashboardRange(value) || pageState.range === value) return
  pageState.range = value
  await fetchOverview('range-change')
}

const onRefreshClick = async () => {
  await fetchOverview('refresh')
}

const onRetryClick = async () => {
  await fetchOverview('range-change')
}

onMounted(() => {
  void fetchOverview('initial')
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 16px;
}

.dashboard-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #f5f3ff 0%, #eef2ff 56%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.dashboard-title {
  margin: 0;
  font-size: 24px;
  color: #1e1b4b;
}

.dashboard-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.dashboard-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dashboard-meta {
  font-size: 12px;
  color: #64748b;
}

.dashboard-error {
  margin-top: -6px;
}

.dashboard-block {
  border-radius: 14px;
  border: 1px solid #dce6f0;
  background: #ffffff;
  padding: 16px;
}

.dashboard-block__title {
  margin: 0 0 14px;
  font-size: 18px;
  color: #0f172a;
}

.dashboard-col {
  margin-bottom: 12px;
}

.kpi-card {
  border-radius: 12px;
  border: 1px solid #e5eaf3;
  background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
}

.kpi-title {
  margin: 0;
  font-size: 13px;
  color: #475569;
}

.kpi-value {
  margin: 10px 0 6px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.kpi-trend {
  margin: 0;
  font-size: 12px;
}

.kpi-trend--up {
  color: #16a34a;
}

.kpi-trend--down {
  color: #dc2626;
}

.kpi-trend--flat {
  color: #64748b;
}

.dashboard-section-row {
  margin-top: 2px;
}

.dashboard-panel {
  min-height: 356px;
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.alert-item {
  display: grid;
  gap: 6px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.alert-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.alert-module {
  color: #1f2937;
  font-size: 13px;
  font-weight: 600;
}

.alert-time {
  color: #64748b;
  font-size: 12px;
}

.alert-message {
  margin: 0;
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 1100px) {
  .dashboard-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .dashboard-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
