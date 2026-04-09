<template>
  <section class="usage-page">
    <header class="usage-toolbar">
      <div>
        <h2 class="usage-title">用量、任务与通知中心</h2>
        <p class="usage-subtitle">覆盖异步任务系统与通知系统，支持运营可观测与告警联动。</p>
      </div>
      <div class="usage-toolbar__actions">
        <a-button :loading="usageStore.generatingReport" @click="onGenerateReportClick">生成日报</a-button>
        <a-button type="primary" :loading="usageStore.syncingAlerts" @click="onSyncAlertsClick">同步告警</a-button>
      </div>
    </header>

    <div class="usage-meta">最后更新：{{ updatedAtText }}</div>

    <a-alert
      v-if="usageStore.latestReportId"
      class="usage-report-info"
      type="info"
      show-icon
      :message="`最新日报：${usageStore.latestReportId}`"
    />

    <a-alert
      v-if="usageStore.errorMessage"
      class="usage-error"
      type="error"
      show-icon
      :message="usageStore.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="usageStore.loadingInitial && !usageStore.hasAnyData" active :paragraph="{ rows: 8 }" />

    <template v-else>
      <section class="usage-kpi-grid">
        <a-card v-for="item in usageStore.kpis" :key="item.key" class="usage-kpi-card" :bordered="false">
          <p class="usage-kpi-card__label">{{ item.title }}</p>
          <p class="usage-kpi-card__value">{{ item.valueText }}</p>
          <p class="usage-kpi-card__hint" :class="{ 'usage-kpi-card__hint--warning': item.tone === 'warning' }">
            {{ item.hintText }}
          </p>
        </a-card>
      </section>

      <a-row :gutter="16" class="usage-section-row">
        <a-col :xs="24" :xl="16">
          <a-card class="usage-card" :bordered="false" title="异步任务队列">
            <a-table
              v-if="usageStore.hasJobData"
              :columns="jobColumns"
              :data-source="usageStore.jobs"
              :pagination="false"
              row-key="id"
              size="small"
              :scroll="{ x: 860 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'jobId'">
                  <code>{{ record.jobId }}</code>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="getJobStatusColor(record.status)">{{ getJobStatusText(record.status) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button
                    type="link"
                    size="small"
                    :disabled="usageStore.retryingJob"
                    @click="onJobActionClick(record.jobId, record.status)"
                  >
                    {{ record.status === 'success' ? '明细' : '立即重试' }}
                  </a-button>
                </template>
              </template>
            </a-table>
            <a-empty v-else description="暂无任务数据" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="8">
          <a-card class="usage-card" :bordered="false" title="通知通道">
            <a-list v-if="usageStore.hasNotifyChannelData" :data-source="usageStore.notifyChannels" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="channel-item">
                  <div class="channel-item__header">
                    <strong>{{ item.name }}</strong>
                    <a-tag :color="getChannelColor(item.status)">{{ getChannelText(item.status) }}</a-tag>
                  </div>
                  <p class="channel-item__detail">{{ item.detail }}</p>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无通道数据" />

            <a-button block class="usage-channel-test-btn" :loading="usageStore.testingNotify" @click="onSendTestNotifyClick">
              发送测试通知
            </a-button>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" class="usage-section-row">
        <a-col :xs="24" :xl="12">
          <a-card class="usage-card" :bordered="false" title="运营报表面板">
            <a-list v-if="usageStore.hasMetricBarData" :data-source="usageStore.metricBars" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="metric-item">
                  <div class="metric-item__header">
                    <strong>{{ item.label }}</strong>
                    <span>{{ item.valueText }}</span>
                  </div>
                  <a-progress :percent="item.percent" size="small" :show-info="false" />
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无报表数据" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card class="usage-card" :bordered="false" title="告警治理建议">
            <a-list v-if="usageStore.hasAdviceData" :data-source="usageStore.adviceItems" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="advice-item">{{ item.content }}</a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无治理建议" />
          </a-card>
        </a-col>
      </a-row>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { UsageJobStatus, UsageNotifyStatus } from '../../api/admin/UsageApi'
import { useUsageStore } from '../../stores/admin/usage'

const usageStore = useUsageStore()

const jobColumns = [
  { title: '任务ID', dataIndex: 'jobId', key: 'jobId', width: 170 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 170 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '重试次数', dataIndex: 'retryCount', key: 'retryCount', width: 100 },
  { title: 'next_retry_at', dataIndex: 'nextRetryAt', key: 'nextRetryAt', width: 180 },
  { title: '处理', key: 'action', width: 110 },
]

const jobStatusTextMap: Record<UsageJobStatus, string> = {
  success: 'SUCCESS',
  retrying: 'RETRYING',
  failed: 'FAILED',
}

const jobStatusColorMap: Record<UsageJobStatus, string> = {
  success: 'success',
  retrying: 'gold',
  failed: 'error',
}

const channelTextMap: Record<UsageNotifyStatus, string> = {
  normal: '正常',
  timeout: '超时',
}

const channelColorMap: Record<UsageNotifyStatus, string> = {
  normal: 'success',
  timeout: 'warning',
}

const updatedAtText = computed(() => {
  if (!usageStore.updatedAt) {
    return '--'
  }
  return new Date(usageStore.updatedAt).toLocaleString('zh-CN', { hour12: false })
})

const getJobStatusText = (status: UsageJobStatus) => jobStatusTextMap[status]
const getJobStatusColor = (status: UsageJobStatus) => jobStatusColorMap[status]
const getChannelText = (status: UsageNotifyStatus) => channelTextMap[status]
const getChannelColor = (status: UsageNotifyStatus) => channelColorMap[status]

const onGenerateReportClick = async () => {
  try {
    const reportId = await usageStore.generateReport()
    message.success(reportId ? `日报生成成功：${reportId}` : '日报生成成功')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('日报生成失败，请稍后重试')
    }
  }
}

const onSyncAlertsClick = async () => {
  try {
    await usageStore.syncAlerts()
    message.success('告警同步完成')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('告警同步失败，请稍后重试')
    }
  }
}

const onJobActionClick = async (jobId: string, status: UsageJobStatus) => {
  if (status === 'success') {
    message.info('Mock 版本可在后续扩展任务明细抽屉')
    return
  }
  try {
    await usageStore.retryJob(jobId)
    message.success('任务重试已触发')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('任务重试失败，请稍后重试')
    }
  }
}

const onSendTestNotifyClick = async () => {
  try {
    await usageStore.sendTestNotify()
    message.success('测试通知已发送')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('测试通知发送失败，请稍后重试')
    }
  }
}

const onRetryClick = async () => {
  await usageStore.retry()
}

onMounted(() => {
  void usageStore.initialize()
})
</script>

<style scoped>
.usage-page {
  display: grid;
  gap: 16px;
}

.usage-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #f2f8ff 0%, #f8fafc 60%, #eefbf7 100%);
  border: 1px solid #dce6f0;
}

.usage-title {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.usage-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.usage-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.usage-meta {
  font-size: 12px;
  color: #64748b;
}

.usage-report-info {
  margin-top: -2px;
}

.usage-error {
  margin-top: -6px;
}

.usage-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.usage-kpi-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.usage-kpi-card__label {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.usage-kpi-card__value {
  margin: 10px 0 6px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.usage-kpi-card__hint {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}

.usage-kpi-card__hint--warning {
  color: #d97706;
}

.usage-section-row {
  margin-top: 2px;
}

.usage-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.channel-item {
  display: grid;
  gap: 6px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.channel-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.channel-item__detail {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}

.usage-channel-test-btn {
  margin-top: 8px;
}

.metric-item {
  display: grid;
  gap: 8px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.metric-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.advice-item {
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
  color: #334155;
}

@media (max-width: 1100px) {
  .usage-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .usage-kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .usage-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .usage-kpi-grid {
    grid-template-columns: 1fr;
  }
}
</style>