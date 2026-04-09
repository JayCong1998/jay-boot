<template>
  <section class="billing-page">
    <header class="billing-toolbar">
      <div>
        <h2 class="billing-title">Billing 订阅中心</h2>
        <p class="billing-subtitle">管理套餐、订阅状态机、支付回调健康与订单发票记录。</p>
      </div>
      <div class="billing-toolbar__actions">
        <a-button :loading="billingStore.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button type="primary" :loading="billingStore.reconciling" @click="onReconcileClick">
          <template #icon>
            <SyncOutlined />
          </template>
          立即对账
        </a-button>
      </div>
    </header>

    <div class="billing-meta">最后更新：{{ updatedAtText }}</div>

    <a-alert
      v-if="billingStore.errorMessage"
      class="billing-error"
      type="error"
      show-icon
      :message="billingStore.errorMessage"
    >
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="billingStore.loadingInitial && !billingStore.hasAnyData" active :paragraph="{ rows: 8 }" />

    <template v-else>
      <section class="billing-kpi-grid">
        <a-card v-for="item in summaryCards" :key="item.key" class="billing-kpi-card" :bordered="false">
          <p class="billing-kpi-card__label">{{ item.label }}</p>
          <p class="billing-kpi-card__value">{{ item.value }}</p>
          <p class="billing-kpi-card__hint">{{ item.hint }}</p>
        </a-card>
      </section>

      <a-card class="billing-card" :bordered="false" title="套餐与订阅管理">
        <a-row v-if="billingStore.hasPlanData" :gutter="12">
          <a-col v-for="plan in billingStore.plans" :key="plan.code" :xs="24" :md="8">
            <article class="plan-card" :class="getPlanClass(plan.code, plan.highlighted)">
              <div class="plan-card__header">
                <h3>{{ plan.name }}</h3>
                <a-tag v-if="isCurrentPlan(plan.code)" color="success">当前方案</a-tag>
                <a-tag v-else-if="plan.highlighted" color="blue">推荐</a-tag>
              </div>
              <p class="plan-card__price">
                {{ plan.priceText }}
                <span>/ {{ plan.billingCycleText }}</span>
              </p>
              <ul class="plan-card__features">
                <li v-for="feature in plan.featureList" :key="feature">{{ feature }}</li>
              </ul>
              <a-button
                block
                :type="isCurrentPlan(plan.code) ? 'default' : 'primary'"
                :disabled="billingStore.switchingPlan || isCurrentPlan(plan.code)"
                :loading="billingStore.switchingPlan && pendingPlanCode === plan.code"
                @click="onPlanActionClick(plan.code, plan.contactSales)"
              >
                {{ getPlanActionText(plan.code, plan.contactSales) }}
              </a-button>
            </article>
          </a-col>
        </a-row>
        <a-empty v-else description="暂无套餐数据" />
      </a-card>

      <a-row :gutter="16" class="billing-section-row">
        <a-col :xs="24" :xl="12">
          <a-card class="billing-card" :bordered="false" title="订阅状态机">
            <a-timeline v-if="billingStore.hasStatusStageData">
              <a-timeline-item
                v-for="stage in billingStore.statusStages"
                :key="stage.key"
                :color="getStageColor(stage.key)"
              >
                <div class="stage-header">
                  <strong>{{ stage.title }}</strong>
                  <a-tag v-if="isCurrentStage(stage.key)" color="processing">当前状态</a-tag>
                </div>
                <p class="stage-desc">{{ stage.description }}</p>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else description="暂无状态机数据" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card class="billing-card" :bordered="false" title="支付回调健康">
            <a-list v-if="billingStore.hasCallbackHealthData" :data-source="billingStore.callbackHealth" :split="false">
              <template #renderItem="{ item }">
                <a-list-item class="callback-item">
                  <div class="callback-item__header">
                    <strong>{{ item.provider }}</strong>
                    <a-tag :color="item.status === 'healthy' ? 'success' : 'warning'">
                      {{ item.status === 'healthy' ? '健康' : '波动' }}
                    </a-tag>
                  </div>
                  <a-progress :percent="item.successRate" size="small" :show-info="false" />
                  <div class="callback-item__footer">
                    <span>成功率：{{ item.successRate }}%</span>
                    <span>校验时间：{{ item.lastCheckedAt }}</span>
                  </div>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无回调数据" />
          </a-card>
        </a-col>
      </a-row>

      <a-card class="billing-card" :bordered="false" title="订单与发票记录">
        <a-table
          v-if="billingStore.hasOrderData"
          :columns="orderColumns"
          :data-source="billingStore.orders"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 820 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'orderNo'">
              <code>{{ record.orderNo }}</code>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="getOrderStatusColor(record.status)">
                {{ getOrderStatusText(record.status) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'invoiceStatus'">
              <a-tag :color="getInvoiceStatusColor(record.invoiceStatus)">
                {{ getInvoiceStatusText(record.invoiceStatus) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space size="small">
                <a-button
                  type="link"
                  size="small"
                  :disabled="record.invoiceStatus === 'none' || billingStore.actioningInvoice"
                  @click="onInvoiceAction(record.orderNo, 'download')"
                >
                  下载发票
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="record.invoiceStatus === 'none' || billingStore.actioningInvoice"
                  @click="onInvoiceAction(record.orderNo, 'send')"
                >
                  邮件发送
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
        <a-empty v-else description="暂无订单记录" />
      </a-card>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ReloadOutlined, SyncOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type {
  BillingInvoiceAction,
  BillingInvoiceStatus,
  BillingOrderStatus,
  BillingPlanCode,
  BillingSubscriptionStatus,
} from '../../api/admin/BillingApi'
import { useBillingStore } from '../../stores/admin/billing'

const billingStore = useBillingStore()
const pendingPlanCode = ref<BillingPlanCode | ''>('')

const orderColumns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
  { title: '账期', dataIndex: 'period', key: 'period', width: 110 },
  { title: '金额', dataIndex: 'amountText', key: 'amountText', width: 120 },
  { title: '支付渠道', dataIndex: 'channel', key: 'channel', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '发票', dataIndex: 'invoiceStatus', key: 'invoiceStatus', width: 120 },
  { title: '操作', key: 'action', width: 180 },
]

const subscriptionStatusTextMap: Record<BillingSubscriptionStatus, string> = {
  trialing: '试用中',
  active: '生效中',
  past_due: '待补扣',
  canceled: '已取消',
}

const subscriptionStatusColorMap: Record<BillingSubscriptionStatus, string> = {
  trialing: 'gold',
  active: 'success',
  past_due: 'warning',
  canceled: 'default',
}

const orderStatusTextMap: Record<BillingOrderStatus, string> = {
  paid: '已支付',
  pending: '待支付',
  failed: '失败',
  refunded: '已退款',
}

const orderStatusColorMap: Record<BillingOrderStatus, string> = {
  paid: 'success',
  pending: 'processing',
  failed: 'error',
  refunded: 'default',
}

const invoiceStatusTextMap: Record<BillingInvoiceStatus, string> = {
  generated: '可下载',
  sent: '已发送',
  none: '无',
}

const invoiceStatusColorMap: Record<BillingInvoiceStatus, string> = {
  generated: 'blue',
  sent: 'green',
  none: 'default',
}

const updatedAtText = computed(() => {
  if (!billingStore.updatedAt) {
    return '--'
  }
  return new Date(billingStore.updatedAt).toLocaleString('zh-CN', { hour12: false })
})

const summaryCards = computed(() => {
  const subscription = billingStore.currentSubscription
  const callbackAvg =
    billingStore.callbackHealth.length === 0
      ? '--'
      : `${Math.round(
          billingStore.callbackHealth.reduce((acc, item) => acc + item.successRate, 0) /
            billingStore.callbackHealth.length,
        )}%`

  return [
    {
      key: 'current_plan',
      label: '当前套餐',
      value: subscription?.planName ?? '--',
      hint: `当前月费：${subscription?.amountText ?? '--'}`,
    },
    {
      key: 'subscription_status',
      label: '订阅状态',
      value: subscription ? subscriptionStatusTextMap[subscription.status] : '--',
      hint: subscription?.renewAt ? `续费时间：${subscription.renewAt.slice(0, 10)}` : '暂无续费计划',
    },
    {
      key: 'pending_orders',
      label: '待处理订单',
      value: String(billingStore.pendingOrderCount),
      hint: billingStore.pendingOrderCount > 0 ? '建议及时执行对账' : '当前无待处理项',
    },
    {
      key: 'callback_health',
      label: '回调成功率',
      value: callbackAvg,
      hint: '覆盖微信支付 / 支付宝 / Stripe',
    },
  ]
})

const isCurrentPlan = (planCode: BillingPlanCode) => {
  return billingStore.currentSubscription?.planCode === planCode
}

const getPlanClass = (planCode: BillingPlanCode, highlighted: boolean) => {
  return {
    'plan-card--current': isCurrentPlan(planCode),
    'plan-card--highlighted': highlighted && !isCurrentPlan(planCode),
  }
}

const getPlanActionText = (planCode: BillingPlanCode, contactSales: boolean) => {
  if (isCurrentPlan(planCode)) {
    return '当前方案'
  }
  if (contactSales) {
    return '联系销售'
  }
  return '升级套餐'
}

const getStageColor = (stage: BillingSubscriptionStatus) => {
  return subscriptionStatusColorMap[stage]
}

const isCurrentStage = (stage: BillingSubscriptionStatus) => {
  return billingStore.currentSubscription?.status === stage
}

const getOrderStatusText = (status: BillingOrderStatus) => orderStatusTextMap[status]
const getOrderStatusColor = (status: BillingOrderStatus) => orderStatusColorMap[status]
const getInvoiceStatusText = (status: BillingInvoiceStatus) => invoiceStatusTextMap[status]
const getInvoiceStatusColor = (status: BillingInvoiceStatus) => invoiceStatusColorMap[status]

const onRefreshClick = async () => {
  await billingStore.refresh()
  if (!billingStore.errorMessage) {
    message.success('Billing 数据已刷新')
  }
}

const onRetryClick = async () => {
  await billingStore.retry()
}

const onReconcileClick = async () => {
  try {
    await billingStore.triggerReconcile()
    message.success('对账任务已触发')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('对账触发失败，请稍后重试')
    }
  }
}

const onPlanActionClick = async (planCode: BillingPlanCode, contactSales: boolean) => {
  if (contactSales) {
    message.info('Mock 版本请联系销售：sales@jayboot.local')
    return
  }
  pendingPlanCode.value = planCode
  try {
    const changed = await billingStore.changePlan(planCode)
    if (changed) {
      message.success('套餐切换成功')
    } else {
      message.info('当前已是该套餐')
    }
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('套餐切换失败，请稍后重试')
    }
  } finally {
    pendingPlanCode.value = ''
  }
}

const onInvoiceAction = async (orderNo: string, action: BillingInvoiceAction) => {
  try {
    await billingStore.invoiceAction(orderNo, action)
    if (action === 'download') {
      message.success('发票下载任务已创建')
    } else {
      message.success('发票已通过邮件发送')
    }
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('发票操作失败，请稍后重试')
    }
  }
}

onMounted(() => {
  void billingStore.initialize()
})
</script>

<style scoped>
.billing-page {
  display: grid;
  gap: 16px;
}

.billing-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #eef6ff 0%, #effbf7 55%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.billing-title {
  margin: 0;
  font-size: 24px;
  color: #1e1b4b;
}

.billing-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.billing-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.billing-meta {
  font-size: 12px;
  color: #64748b;
}

.billing-error {
  margin-top: -6px;
}

.billing-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.billing-kpi-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.billing-kpi-card__label {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.billing-kpi-card__value {
  margin: 10px 0 6px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.billing-kpi-card__hint {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}

.billing-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.plan-card {
  display: grid;
  gap: 10px;
  min-height: 100%;
  border: 1px solid #e5eaf3;
  border-radius: 12px;
  padding: 14px;
  background: #ffffff;
}

.plan-card--current {
  border-color: #86efac;
  background: linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%);
}

.plan-card--highlighted {
  border-color: #bfdbfe;
  background: linear-gradient(180deg, #eff6ff 0%, #ffffff 100%);
}

.plan-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.plan-card__header h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.plan-card__price {
  margin: 0;
  font-size: 28px;
  color: #0f172a;
  font-weight: 700;
  line-height: 1.2;
}

.plan-card__price span {
  font-size: 13px;
  font-weight: 400;
  color: #64748b;
}

.plan-card__features {
  margin: 0;
  padding-left: 18px;
  color: #334155;
  font-size: 13px;
  line-height: 1.7;
  display: grid;
  gap: 2px;
}

.billing-section-row {
  margin-top: 2px;
}

.stage-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.stage-desc {
  margin: 6px 0 0;
  color: #64748b;
}

.callback-item {
  display: grid;
  gap: 8px;
  border: 1px solid #e5eaf3;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.callback-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.callback-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #64748b;
  font-size: 12px;
}

@media (max-width: 1100px) {
  .billing-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .billing-kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .billing-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .billing-kpi-grid {
    grid-template-columns: 1fr;
  }

  .callback-item__footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>