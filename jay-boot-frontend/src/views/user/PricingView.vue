<template>
  <section class="pricing-view">
    <a-alert v-if="loadError" type="error" show-icon :message="loadError">
      <template #action>
        <a-button type="link" @click="retryLoad">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="pageLoading && !overview" active :paragraph="{ rows: 10 }" />

    <template v-if="overview">
      <section class="hero-card">
        <div class="hero-meta">
          <span class="eyebrow">{{ overview.hero.eyebrow }}</span>
          <span class="source-tag">数据源：{{ apiSourceText }}</span>
        </div>
        <h1>{{ overview.hero.title }}</h1>
        <p class="hero-desc">{{ overview.hero.description }}</p>

        <div class="hero-actions">
          <a-button size="large" @click="goHelp">{{ overview.hero.primaryActionText }}</a-button>
          <a-button type="primary" size="large" class="btn-main" @click="goCheckout">
            {{ overview.hero.secondaryActionText }}
          </a-button>
        </div>
      </section>

      <section class="section-block section-block--soft">
        <div class="section-head">
          <h2>套餐选择</h2>
          <small>更新于：{{ updatedAtText }}</small>
        </div>

        <div class="cycle-switch" role="group" aria-label="计费周期切换">
          <button
            v-for="option in cycleOptions"
            :key="option.key"
            type="button"
            class="cycle-item"
            :class="{ active: activeCycle === option.key }"
            :disabled="cycleSwitching"
            @click="changeCycle(option.key)"
          >
            <strong>{{ option.label }}</strong>
            <small>{{ option.hint }}</small>
          </button>
        </div>

        <a-empty v-if="planCards.length === 0" description="当前周期暂无可售套餐" />

        <div v-else class="plan-grid">
          <article v-for="plan in planCards" :key="plan.code" class="plan-card" :class="{ recommended: plan.recommended }">
            <div class="plan-top">
              <a-tag v-if="plan.tag" color="blue">{{ plan.tag }}</a-tag>
              <span class="plan-code">{{ plan.code }}</span>
            </div>

            <h3>{{ plan.name }}</h3>
            <p class="plan-price">
              {{ plan.priceText }}
              <span>{{ plan.billingCycleText }}</span>
            </p>
            <p class="plan-fit">{{ plan.fitFor }}</p>

            <ul>
              <li v-for="item in plan.highlights" :key="item">{{ item }}</li>
            </ul>

            <a-button
              class="plan-action"
              :type="plan.contactSales ? 'default' : 'primary'"
              :ghost="plan.contactSales"
              @click="handlePlanAction(plan)"
            >
              {{ plan.contactSales ? '联系销售' : '选择套餐' }}
            </a-button>
          </article>
        </div>
      </section>

      <section class="section-block">
        <h2>核心权益对比</h2>
        <div class="comparison-wrap">
          <table>
            <thead>
              <tr>
                <th>权益项</th>
                <th v-for="plan in planCards" :key="`head-${plan.code}`">{{ plan.name }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in overview.comparisonRows" :key="row.id">
                <td>
                  <strong>{{ row.feature }}</strong>
                  <small>{{ row.description }}</small>
                </td>
                <td v-for="plan in planCards" :key="`${row.id}-${plan.code}`">
                  {{ comparisonValue(row, plan.code) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="section-block">
        <h2>定价 FAQ</h2>
        <div class="faq-list">
          <article v-for="faq in overview.faqList" :key="faq.id" class="faq-item">
            <h3>{{ faq.question }}</h3>
            <p>{{ faq.answer }}</p>
          </article>
        </div>
      </section>

      <section class="final-cta">
        <h2>{{ overview.finalCta.title }}</h2>
        <p>{{ overview.finalCta.description }}</p>
        <div class="hero-actions">
          <a-button type="primary" size="large" class="btn-main" @click="goCheckout">
            {{ overview.finalCta.primaryActionText }}
          </a-button>
          <a-button size="large" @click="goHelp">{{ overview.finalCta.secondaryActionText }}</a-button>
        </div>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  getPricingOverviewApi,
  type PricingBillingCycle,
  type PricingComparisonRow,
  type PricingCycleOption,
  type PricingOverviewResponse,
  type PricingPlanCard,
} from '../../api/user/PricingApi'
import { apiConfig } from '../../config/api'

const PRICING_CYCLE_CACHE_KEY = 'jay_boot_user_pricing_cycle'

const router = useRouter()
const overview = ref<PricingOverviewResponse | null>(null)
const pageLoading = ref(false)
const cycleSwitching = ref(false)
const loadError = ref('')
const activeCycle = ref<PricingBillingCycle>('MONTHLY')

const apiSourceText = computed(() => (apiConfig.mode === 'mock' ? 'Mock API' : '真实 API'))

const cycleOptions = computed<PricingCycleOption[]>(() => overview.value?.cycleOptions ?? [])
const planCards = computed<PricingPlanCard[]>(() => overview.value?.planCards ?? [])

const updatedAtText = computed(() => {
  const updatedAt = overview.value?.updatedAt
  if (!updatedAt) {
    return '--'
  }
  const date = new Date(updatedAt)
  if (Number.isNaN(date.getTime())) {
    return '--'
  }
  return date.toLocaleString('zh-CN', { hour12: false })
})

const normalizeCycle = (value: unknown): PricingBillingCycle => {
  if (typeof value !== 'string') {
    return 'MONTHLY'
  }
  const normalized = value.trim().toUpperCase()
  return normalized === 'YEARLY' ? 'YEARLY' : 'MONTHLY'
}

const readCachedCycle = (): PricingBillingCycle => normalizeCycle(localStorage.getItem(PRICING_CYCLE_CACHE_KEY))

const writeCachedCycle = (cycle: PricingBillingCycle) => {
  localStorage.setItem(PRICING_CYCLE_CACHE_KEY, cycle)
}

const loadOverview = async (cycle: PricingBillingCycle, triggeredByCycleSwitch = false) => {
  if (triggeredByCycleSwitch) {
    cycleSwitching.value = true
  } else {
    pageLoading.value = true
  }
  loadError.value = ''

  try {
    const data = await getPricingOverviewApi({ billingCycle: cycle })
    overview.value = data
    activeCycle.value = data.selectedCycle
    writeCachedCycle(data.selectedCycle)
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '定价页数据加载失败'
  } finally {
    pageLoading.value = false
    cycleSwitching.value = false
  }
}

const changeCycle = (cycle: PricingBillingCycle) => {
  if (cycle === activeCycle.value && overview.value) {
    return
  }
  void loadOverview(cycle, true)
}

const retryLoad = () => {
  void loadOverview(activeCycle.value, Boolean(overview.value))
}

const comparisonValue = (row: PricingComparisonRow, planCode: string) => {
  return row.cells.find((cell) => cell.planCode === planCode)?.value ?? '--'
}

const goCheckout = () => {
  router.push('/user/checkout')
}

const goHelp = () => {
  router.push('/user/help')
}

const handlePlanAction = (plan: PricingPlanCard) => {
  if (plan.contactSales) {
    goHelp()
    return
  }
  router.push({
    path: '/user/checkout',
    query: {
      planCode: plan.code,
      billingCycle: activeCycle.value,
    },
  })
}

onMounted(() => {
  activeCycle.value = readCachedCycle()
  void loadOverview(activeCycle.value)
})
</script>

<style scoped>
.pricing-view {
  --pricing-border: var(--user-border);
  --pricing-main: var(--user-text-main);
  --pricing-sub: var(--user-text-sub);
  --pricing-minor: var(--user-text-minor);
  --pricing-surface: var(--user-surface);
  --pricing-soft: var(--user-surface-soft);
  display: grid;
  gap: 14px;
  font-family: 'Plus Jakarta Sans', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background:
    radial-gradient(circle at 4% 2%, var(--user-gradient-a), transparent 36%),
    radial-gradient(circle at 100% 0%, var(--user-gradient-b), transparent 30%),
    var(--user-bg-base);
}

.hero-card {
  border: 1px solid var(--pricing-border);
  border-radius: 22px;
  padding: 28px 24px;
  background:
    radial-gradient(circle at 96% 10%, var(--user-gradient-hero-a), transparent 34%),
    radial-gradient(circle at 8% 100%, var(--user-gradient-hero-b), transparent 32%),
    var(--pricing-surface);
  box-shadow: var(--user-shadow-md);
  animation: rise-in 320ms ease-out both;
}

.hero-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.eyebrow,
.source-tag {
  display: inline-flex;
  border: 1px solid var(--pricing-border);
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
  background: var(--pricing-soft);
}

.eyebrow {
  color: var(--user-accent-deep);
}

.source-tag {
  color: var(--pricing-minor);
}

.hero-card h1 {
  margin: 12px 0 0;
  font-size: clamp(30px, 4.8vw, 42px);
  line-height: 1.18;
  color: var(--pricing-main);
  letter-spacing: -0.02em;
}

.hero-desc {
  margin: 12px 0 0;
  color: var(--pricing-sub);
  line-height: 1.75;
  max-width: 680px;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.btn-main.ant-btn-primary) {
  border-color: var(--user-accent);
  background: var(--user-accent);
}

:deep(.btn-main.ant-btn-primary:hover),
:deep(.btn-main.ant-btn-primary:focus) {
  border-color: var(--user-accent-hover);
  background: var(--user-accent-hover);
}

.section-block {
  border: 1px solid var(--pricing-border);
  border-radius: 16px;
  padding: 18px;
  background: var(--pricing-surface);
  animation: rise-in 380ms ease-out both;
}

.section-block--soft {
  background: linear-gradient(160deg, var(--user-bg-base), var(--user-surface-soft));
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.section-head small {
  color: var(--pricing-minor);
}

.section-block h2 {
  margin: 0;
  font-size: 26px;
  color: var(--pricing-main);
}

.cycle-switch {
  margin-top: 12px;
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.cycle-item {
  border: 1px solid var(--pricing-border);
  border-radius: 12px;
  padding: 12px;
  text-align: left;
  background: var(--pricing-surface);
  cursor: pointer;
  display: grid;
  gap: 4px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.cycle-item strong {
  color: var(--pricing-main);
}

.cycle-item small {
  color: var(--pricing-minor);
}

.cycle-item.active {
  border-color: var(--user-accent);
  box-shadow: 0 10px 18px rgba(var(--user-shadow-rgb), 0.14);
  transform: translateY(-1px);
}

.cycle-item:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.plan-grid {
  margin-top: 14px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.plan-card {
  border: 1px solid var(--pricing-border);
  border-radius: 14px;
  padding: 14px;
  background: var(--pricing-surface);
  display: grid;
  align-content: start;
  gap: 8px;
}

.plan-card.recommended {
  border-color: var(--user-accent);
  box-shadow: 0 12px 20px rgba(var(--user-shadow-rgb), 0.16);
}

.plan-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.plan-code {
  font-size: 12px;
  color: var(--pricing-minor);
}

.plan-card h3 {
  margin: 0;
  color: var(--pricing-main);
  font-size: 21px;
}

.plan-price {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  color: var(--pricing-main);
}

.plan-price span {
  margin-left: 6px;
  font-size: 13px;
  font-weight: 400;
  color: var(--pricing-minor);
}

.plan-fit {
  margin: 0;
  color: var(--pricing-sub);
}

.plan-card ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  color: var(--pricing-sub);
}

.plan-action {
  margin-top: 4px;
}

.comparison-wrap {
  margin-top: 12px;
  overflow-x: auto;
  border: 1px solid var(--pricing-border);
  border-radius: 12px;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 620px;
}

thead th {
  background: var(--pricing-soft);
  color: var(--pricing-main);
  font-weight: 700;
}

th,
td {
  border-bottom: 1px solid var(--pricing-border);
  padding: 12px;
  text-align: left;
  color: var(--pricing-sub);
}

tbody tr:last-child td {
  border-bottom: 0;
}

td strong {
  display: block;
  color: var(--pricing-main);
}

td small {
  color: var(--pricing-minor);
}

.faq-list {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.faq-item {
  border: 1px solid var(--pricing-border);
  border-radius: 12px;
  padding: 12px;
  background: var(--pricing-surface);
}

.faq-item h3 {
  margin: 0;
  color: var(--pricing-main);
}

.faq-item p {
  margin: 8px 0 0;
  color: var(--pricing-sub);
  line-height: 1.75;
}

.final-cta {
  border: 1px solid var(--pricing-border);
  border-radius: 20px;
  padding: 24px;
  background: linear-gradient(130deg, var(--user-bg-soft), var(--user-bg-muted));
  box-shadow: var(--user-shadow-lg);
  animation: rise-in 430ms ease-out both;
}

.final-cta h2 {
  margin: 0;
  font-size: clamp(26px, 4vw, 34px);
  color: var(--pricing-main);
}

.final-cta p {
  margin: 10px 0 0;
  color: var(--pricing-sub);
  line-height: 1.75;
}

@media (max-width: 1100px) {
  .plan-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-card,
  .section-block,
  .final-cta {
    padding: 16px;
  }

  .section-block h2 {
    font-size: 22px;
  }

  .cycle-switch {
    grid-template-columns: 1fr;
  }
}

@keyframes rise-in {
  from {
    transform: translateY(6px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-card,
  .section-block,
  .final-cta {
    animation: none;
  }

  .cycle-item {
    transition: none;
  }

  .cycle-item.active {
    transform: none;
  }
}
</style>
