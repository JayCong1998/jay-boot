<template>
  <section class="features-view">
    <a-alert v-if="loadError" type="error" show-icon :message="loadError">
      <template #action>
        <a-button type="link" @click="loadPageData">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="pageLoading && (!overview || !roleFit)" active :paragraph="{ rows: 12 }" />

    <template v-if="overview && roleFit">
      <section class="hero-card">
        <div class="hero-meta">
          <span class="eyebrow">{{ overview.hero.eyebrow }}</span>
          <span class="source-tag">数据源：{{ apiSourceText }}</span>
        </div>
        <h1>{{ overview.hero.title }}</h1>
        <p>{{ overview.hero.description }}</p>

        <div class="hero-actions">
          <a-button type="primary" size="large" @click="goRegister">{{ overview.hero.primaryActionText }}</a-button>
          <a-button size="large" @click="goPricing">{{ overview.hero.secondaryActionText }}</a-button>
        </div>
      </section>

      <section class="section-block">
        <div class="section-head">
          <h2>套餐能力对比</h2>
          <small>更新于：{{ updatedAtText }}</small>
        </div>

        <div class="plan-grid">
          <article
            v-for="plan in overview.planCards"
            :key="plan.code"
            class="plan-card"
            :class="{ recommended: plan.recommended }"
          >
            <p class="plan-code">{{ plan.code }}</p>
            <h3>{{ plan.name }}</h3>
            <p class="plan-price">{{ plan.priceText }}<span>{{ plan.billingCycleText }}</span></p>
            <p class="plan-fit">{{ plan.fitFor }}</p>
            <ul>
              <li v-for="item in plan.highlights" :key="item">{{ item }}</li>
            </ul>
          </article>
        </div>

        <div class="comparison-list">
          <section v-for="group in overview.comparisonGroups" :key="group.id" class="comparison-group">
            <h3>{{ group.title }}</h3>
            <div class="comparison-table" role="table" :aria-label="group.title">
              <div class="comparison-row comparison-row--head" role="row">
                <div role="columnheader">能力项</div>
                <div role="columnheader">Free</div>
                <div role="columnheader">Pro</div>
                <div role="columnheader">Team</div>
              </div>

              <div v-for="item in group.items" :key="item.key" class="comparison-row" role="row">
                <div class="feature-info" role="cell">
                  <strong>{{ item.name }}</strong>
                  <small>{{ item.description }}</small>
                </div>

                <div class="support-cell" role="cell">
                  <component :is="supportMeta(item.levels.free).icon" />
                  <span>{{ supportMeta(item.levels.free).label }}</span>
                </div>

                <div class="support-cell" role="cell">
                  <component :is="supportMeta(item.levels.pro).icon" />
                  <span>{{ supportMeta(item.levels.pro).label }}</span>
                </div>

                <div class="support-cell" role="cell">
                  <component :is="supportMeta(item.levels.team).icon" />
                  <span>{{ supportMeta(item.levels.team).label }}</span>
                </div>
              </div>
            </div>
          </section>
        </div>
      </section>

      <section class="section-block">
        <h2>角色适配建议</h2>
        <a-tabs v-model:activeKey="activeRoleKey" class="role-tabs">
          <a-tab-pane v-for="role in roleFit.roles" :key="role.key" :tab="role.label" />
        </a-tabs>

        <article v-if="activeRole" class="role-card">
          <div class="role-card__head">
            <p>推荐套餐</p>
            <a-tag color="blue">{{ activeRole.recommendedPlan }}</a-tag>
          </div>
          <ul>
            <li v-for="item in activeRole.highlights" :key="item">{{ item }}</li>
          </ul>
          <p class="role-advice">{{ activeRole.advice }}</p>
        </article>
      </section>

      <section class="section-block">
        <h2>选型常见问题</h2>
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
          <a-button type="primary" size="large" @click="goRegister">{{ overview.finalCta.primaryActionText }}</a-button>
          <a-button size="large" @click="goPricing">{{ overview.finalCta.secondaryActionText }}</a-button>
        </div>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, type Component } from 'vue'
import { useRouter } from 'vue-router'
import { CheckCircleFilled, CloseCircleFilled, MinusCircleFilled } from '@ant-design/icons-vue'
import {
  getFeaturesOverviewApi,
  getFeaturesRoleFitApi,
  type FeatureSupportLevel,
  type FeaturesOverviewResponse,
  type FeaturesRoleFitResponse,
} from '../../api/user/FeaturesApi'
import { apiConfig } from '../../config/api'

const router = useRouter()

const overview = ref<FeaturesOverviewResponse | null>(null)
const roleFit = ref<FeaturesRoleFitResponse | null>(null)
const activeRoleKey = ref('')
const pageLoading = ref(false)
const loadError = ref('')

const apiSourceText = computed(() => (apiConfig.mode === 'mock' ? 'Mock API' : '真实 API'))

const updatedAtText = computed(() => {
  if (!overview.value?.updatedAt) {
    return '--'
  }
  const date = new Date(overview.value.updatedAt)
  if (Number.isNaN(date.getTime())) {
    return '--'
  }
  return date.toLocaleString('zh-CN', { hour12: false })
})

const activeRole = computed(() => {
  if (!roleFit.value) {
    return null
  }
  return roleFit.value.roles.find((item) => item.key === activeRoleKey.value) ?? null
})

const supportMap: Record<
  FeatureSupportLevel,
  {
    label: string
    icon: Component
  }
> = {
  supported: {
    label: '支持',
    icon: CheckCircleFilled,
  },
  partial: {
    label: '部分支持',
    icon: MinusCircleFilled,
  },
  unsupported: {
    label: '不支持',
    icon: CloseCircleFilled,
  },
}

const supportMeta = (level: FeatureSupportLevel) => supportMap[level]

const loadPageData = async () => {
  pageLoading.value = true
  loadError.value = ''
  try {
    const [overviewData, roleFitData] = await Promise.all([getFeaturesOverviewApi(), getFeaturesRoleFitApi()])
    overview.value = overviewData
    roleFit.value = roleFitData
    activeRoleKey.value = roleFitData.defaultRoleKey || roleFitData.roles[0]?.key || ''
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '功能对比数据加载失败'
  } finally {
    pageLoading.value = false
  }
}

const goRegister = () => {
  router.push('/user/auth/register')
}

const goPricing = () => {
  router.push('/user/pricing')
}

onMounted(() => {
  void loadPageData()
})
</script>

<style scoped>
.features-view {
  --feature-border: var(--user-border);
  --feature-main: var(--user-text-main);
  --feature-sub: var(--user-text-sub);
  --feature-minor: var(--user-text-minor);
  --feature-surface: var(--user-surface);
  --feature-soft: var(--user-surface-soft);
  display: grid;
  gap: 16px;
  font-family: 'Plus Jakarta Sans', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.hero-card {
  border: 1px solid var(--feature-border);
  border-radius: 20px;
  padding: 28px;
  background:
    radial-gradient(circle at 96% 4%, var(--user-gradient-hero-a), transparent 34%),
    radial-gradient(circle at 0 100%, var(--user-gradient-hero-b), transparent 32%),
    var(--feature-surface);
  box-shadow: var(--user-shadow-md);
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
  border: 1px solid var(--feature-border);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  background: var(--feature-soft);
}

.eyebrow {
  color: var(--user-accent-deep);
}

.source-tag {
  color: var(--user-accent);
}

.hero-card h1 {
  margin: 12px 0 0;
  font-size: clamp(30px, 5vw, 44px);
  line-height: 1.16;
  color: var(--feature-main);
  letter-spacing: -0.02em;
}

.hero-card p {
  margin: 12px 0 0;
  color: var(--feature-sub);
  line-height: 1.8;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.section-block {
  border: 1px solid var(--feature-border);
  border-radius: 16px;
  padding: 18px;
  background: var(--feature-surface);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.section-head small {
  color: var(--feature-minor);
}

.section-block h2 {
  margin: 0;
  font-size: 26px;
  color: var(--feature-main);
}

.plan-grid {
  margin-top: 12px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.plan-card {
  border: 1px solid var(--feature-border);
  border-radius: 14px;
  padding: 14px;
  background: var(--feature-surface);
}

.plan-card.recommended {
  border-color: var(--user-accent);
  box-shadow: 0 10px 20px rgba(var(--user-shadow-rgb), 0.14);
}

.plan-code {
  margin: 0;
  font-size: 12px;
  color: var(--user-accent);
  font-weight: 700;
}

.plan-card h3 {
  margin: 8px 0 0;
  color: var(--feature-main);
}

.plan-price {
  margin: 10px 0 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--feature-main);
}

.plan-price span {
  margin-left: 6px;
  font-size: 13px;
  font-weight: 400;
  color: var(--feature-minor);
}

.plan-fit {
  margin: 8px 0 0;
  color: var(--feature-sub);
}

.plan-card ul {
  margin: 10px 0 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  color: var(--feature-sub);
}

.comparison-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.comparison-group {
  border: 1px solid var(--feature-border);
  border-radius: 12px;
  background: var(--feature-surface);
  overflow: hidden;
}

.comparison-group h3 {
  margin: 0;
  padding: 12px 14px;
  color: var(--feature-main);
  border-bottom: 1px solid var(--feature-border);
  background: var(--feature-soft);
}

.comparison-table {
  display: grid;
}

.comparison-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  border-bottom: 1px solid var(--feature-border);
}

.comparison-row:last-child {
  border-bottom: 0;
}

.comparison-row > div {
  padding: 10px 12px;
}

.comparison-row--head {
  font-weight: 700;
  color: var(--feature-main);
  background: var(--user-surface-soft);
}

.feature-info {
  display: grid;
  gap: 4px;
}

.feature-info strong {
  color: var(--feature-main);
}

.feature-info small {
  color: var(--feature-minor);
}

.support-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--feature-sub);
}

.role-tabs {
  margin-top: 12px;
}

.role-card {
  border: 1px solid var(--feature-border);
  border-radius: 12px;
  padding: 12px;
  background: var(--feature-soft);
}

.role-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.role-card__head p {
  margin: 0;
  color: var(--feature-main);
  font-weight: 600;
}

.role-card ul {
  margin: 10px 0 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  color: var(--feature-sub);
}

.role-advice {
  margin: 10px 0 0;
  color: var(--feature-minor);
}

.faq-list {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.faq-item {
  border: 1px solid var(--feature-border);
  border-radius: 12px;
  padding: 12px;
  background: var(--feature-surface);
}

.faq-item h3 {
  margin: 0;
  color: var(--feature-main);
}

.faq-item p {
  margin: 8px 0 0;
  color: var(--feature-sub);
  line-height: 1.75;
}

.final-cta {
  border: 1px solid var(--feature-border);
  border-radius: 20px;
  padding: 24px;
  background: linear-gradient(130deg, var(--user-bg-soft), var(--user-bg-muted));
  box-shadow: var(--user-shadow-lg);
}

.final-cta h2 {
  margin: 0;
  font-size: clamp(26px, 4vw, 34px);
  color: var(--feature-main);
}

.final-cta p {
  margin: 10px 0 0;
  color: var(--feature-sub);
  line-height: 1.75;
}

@media (max-width: 1100px) {
  .plan-grid {
    grid-template-columns: 1fr;
  }

  .comparison-row {
    grid-template-columns: 1.5fr 1fr 1fr 1fr;
  }
}

@media (max-width: 860px) {
  .comparison-row,
  .comparison-row--head {
    grid-template-columns: 1fr;
  }

  .comparison-row--head > div:not(:first-child) {
    display: none;
  }

  .support-cell {
    border-top: 1px dashed var(--feature-border);
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
}

@media (prefers-reduced-motion: reduce) {
  .features-view * {
    transition: none !important;
    animation: none !important;
  }
}
</style>
