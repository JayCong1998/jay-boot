<template>
  <section class="home-view">
    <a-alert v-if="loadError" type="error" show-icon :message="loadError">
      <template #action>
        <a-button type="link" @click="loadOverview">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="overviewLoading && !overview" active :paragraph="{ rows: 10 }" />

    <template v-if="overview">
      <section class="hero-card">
        <div class="hero-meta">
          <span class="eyebrow">{{ overview.hero.eyebrow }}</span>
          <span class="source-tag">数据源：{{ apiSourceText }}</span>
        </div>
        <h1>{{ overview.hero.title }}</h1>
        <p>{{ overview.hero.description }}</p>

        <div class="hero-actions">
          <a-button type="primary" size="large" @click="goRegister">{{ overview.hero.primaryActionText }}</a-button>
          <a-button size="large" @click="showCases">{{ overview.hero.secondaryActionText }}</a-button>
        </div>

        <div class="trust-strip">
          <span v-for="item in overview.trustBadges" :key="item" class="trust-badge">{{ item }}</span>
        </div>
      </section>

      <section class="section-block">
        <div class="section-head">
          <h2>{{ overview.sectionTitles.features }}</h2>
          <small>更新于：{{ updatedAtText }}</small>
        </div>
        <div class="feature-grid">
          <article
            v-for="card in overview.featureCards"
            :key="card.id"
            class="feature-card"
            :class="{ soft: card.soft }"
          >
            <h3>{{ card.title }}</h3>
            <ul>
              <li v-for="point in card.points" :key="point">{{ point }}</li>
            </ul>
          </article>
        </div>
      </section>

      <section class="section-block">
        <h2>{{ overview.sectionTitles.socialProof }}</h2>
        <div class="kpi-grid">
          <article v-for="kpi in overview.kpiCards" :key="kpi.id" class="kpi-card" :class="{ soft: kpi.soft }">
            <p class="kpi-label">{{ kpi.label }}</p>
            <strong class="kpi-value">{{ kpi.value }}</strong>
            <p class="kpi-desc">{{ kpi.desc }}</p>
          </article>
        </div>
      </section>

      <section class="section-block">
        <h2>{{ overview.sectionTitles.faq }}</h2>
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

    <a-modal v-model:open="caseModalOpen" title="真实案例" :footer="null" :width="760" destroy-on-close>
      <a-spin :spinning="caseLoading">
        <div v-if="caseList.length > 0" class="case-grid">
          <article v-for="item in caseList" :key="item.id" class="case-card">
            <div class="case-card__top">
              <h3>{{ item.title }}</h3>
              <a-tag color="blue">{{ item.industry }}</a-tag>
            </div>
            <p class="case-gain">{{ item.gain }}</p>
            <p class="case-summary">{{ item.summary }}</p>
          </article>
        </div>
        <a-empty v-else description="暂无案例数据" />
      </a-spin>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getHomeCasesApi, getHomeOverviewApi, type HomeCaseItem, type HomeOverviewResponse } from '../../api/user/HomeApi'
import { userApiConfig } from '../../config/api'

const router = useRouter()

const overview = ref<HomeOverviewResponse | null>(null)
const caseList = ref<HomeCaseItem[]>([])
const overviewLoading = ref(false)
const caseLoading = ref(false)
const caseModalOpen = ref(false)
const loadError = ref('')

const apiSourceText = computed(() => (userApiConfig.mode === 'mock' ? 'Mock API' : '真实 API'))

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

const loadOverview = async () => {
  overviewLoading.value = true
  loadError.value = ''
  try {
    overview.value = await getHomeOverviewApi()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '首页数据加载失败'
  } finally {
    overviewLoading.value = false
  }
}

const loadCases = async () => {
  caseLoading.value = true
  try {
    caseList.value = await getHomeCasesApi({ limit: 3 })
  } catch (error) {
    message.error(error instanceof Error ? error.message : '案例数据加载失败')
  } finally {
    caseLoading.value = false
  }
}

const goRegister = () => {
  router.push('/user/auth/register')
}

const goPricing = () => {
  router.push('/user/pricing')
}

const showCases = async () => {
  caseModalOpen.value = true
  if (caseList.value.length > 0 || caseLoading.value) {
    return
  }
  await loadCases()
  if (caseList.value.length > 0) {
    message.success(`已加载 ${caseList.value.length} 个案例（${apiSourceText.value}）`)
  }
}

onMounted(() => {
  void loadOverview()
})
</script>

<style scoped>
.home-view {
  --home-border: #d6e2f0;
  --home-text-main: #0f172a;
  --home-text-sub: #334155;
  --home-text-minor: #475569;
  --home-surface: #ffffff;
  --home-surface-soft: #f1f6fd;
  display: grid;
  gap: 16px;
  font-family: 'Plus Jakarta Sans', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.hero-card {
  border: 1px solid var(--home-border);
  border-radius: 20px;
  padding: 28px;
  background:
    radial-gradient(circle at 96% 4%, rgba(202, 138, 4, 0.16), transparent 34%),
    radial-gradient(circle at 0 100%, rgba(30, 58, 138, 0.12), transparent 32%),
    var(--home-surface);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
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
  border: 1px solid var(--home-border);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  background: var(--home-surface-soft);
}

.eyebrow {
  color: #1e3a8a;
}

.source-tag {
  color: #1d4ed8;
}

.hero-card h1 {
  margin: 12px 0 0;
  font-size: clamp(30px, 5vw, 46px);
  line-height: 1.16;
  color: var(--home-text-main);
  letter-spacing: -0.02em;
}

.hero-card p {
  margin: 12px 0 0;
  color: var(--home-text-sub);
  line-height: 1.8;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.trust-strip {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.trust-badge {
  border: 1px solid var(--home-border);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  color: var(--home-text-sub);
  background: var(--home-surface);
}

.section-block {
  border: 1px solid var(--home-border);
  border-radius: 16px;
  padding: 18px;
  background: var(--home-surface);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.section-head small {
  color: var(--home-text-minor);
}

.section-block h2 {
  margin: 0;
  font-size: 26px;
  color: var(--home-text-main);
}

.feature-grid,
.kpi-grid {
  margin-top: 12px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.feature-card,
.kpi-card {
  border: 1px solid var(--home-border);
  border-radius: 14px;
  padding: 14px;
  background: var(--home-surface);
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
}

.feature-card:hover,
.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.08);
}

.feature-card.soft,
.kpi-card.soft {
  background: var(--home-surface-soft);
}

.feature-card h3 {
  margin: 0;
  font-size: 19px;
  color: var(--home-text-main);
}

.feature-card ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: var(--home-text-sub);
  display: grid;
  gap: 8px;
}

.kpi-label {
  margin: 0;
  font-size: 13px;
  color: var(--home-text-minor);
}

.kpi-value {
  display: block;
  margin-top: 8px;
  font-size: 34px;
  color: var(--home-text-main);
}

.kpi-desc {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--home-text-minor);
}

.faq-list {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.faq-item {
  border: 1px solid var(--home-border);
  border-radius: 12px;
  padding: 12px;
  background: var(--home-surface);
}

.faq-item h3 {
  margin: 0;
  font-size: 17px;
  color: var(--home-text-main);
}

.faq-item p {
  margin: 6px 0 0;
  color: var(--home-text-sub);
  line-height: 1.7;
}

.final-cta {
  border: 1px solid var(--home-border);
  border-radius: 20px;
  padding: 24px;
  background: linear-gradient(120deg, #f8fbff, #f5efe0);
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
}

.final-cta h2 {
  margin: 0;
  font-size: clamp(26px, 4vw, 34px);
  color: var(--home-text-main);
}

.final-cta p {
  margin: 10px 0 0;
  color: var(--home-text-sub);
  line-height: 1.75;
}

.case-grid {
  display: grid;
  gap: 10px;
}

.case-card {
  border: 1px solid var(--home-border);
  border-radius: 12px;
  padding: 12px;
  background: var(--home-surface);
}

.case-card__top {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}

.case-card__top h3 {
  margin: 0;
  color: var(--home-text-main);
  font-size: 16px;
}

.case-gain {
  margin: 8px 0 0;
  color: #166534;
  font-weight: 600;
}

.case-summary {
  margin: 6px 0 0;
  color: var(--home-text-sub);
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .feature-grid,
  .kpi-grid {
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
}

@media (prefers-reduced-motion: reduce) {
  .feature-card,
  .kpi-card {
    transition: none;
  }

  .feature-card:hover,
  .kpi-card:hover {
    transform: none;
    box-shadow: none;
  }
}
</style>
