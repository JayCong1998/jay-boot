<template>
  <section class="home-view">
    <a-alert v-if="loadError" type="error" show-icon :message="loadError">
      <template #action>
        <a-button type="link" @click="loadOverview">重试</a-button>
      </template>
    </a-alert>

    <a-skeleton v-if="overviewLoading && !overview" active :paragraph="{ rows: 6 }" />

    <template v-if="overview">
      <section class="hero-card">
        <div class="hero-meta" v-once>
          <span class="eyebrow">{{ overview.hero.eyebrow }}</span>
          <span class="source-tag">{{ apiSourceText }}</span>
        </div>
        <h1>{{ overview.hero.title }}</h1>
        <p class="hero-desc">{{ overview.hero.description }}</p>

        <div class="hero-actions">
          <a-button type="primary" size="large" class="btn-main" @click="goExperience">立即体验</a-button>
          <a-button size="large" class="btn-sub" @click="goRegister">免费注册</a-button>
        </div>
      </section>

      <section class="section-block section-block--soft">
        <div class="section-title">
          <h2>{{ overview.sectionTitles.features }}</h2>
          <small>{{ updatedAtText }}</small>
        </div>
        <div class="feature-grid" v-memo="[overview.updatedAt]">
          <article v-for="card in conciseFeatureCards" :key="card.id" class="feature-card" :class="{ soft: card.soft }">
            <h3>{{ card.title }}</h3>
            <p>{{ card.points[0] || '快速上手，流畅创作。' }}</p>
          </article>
        </div>
      </section>

      <section class="section-block">
        <h2 class="kpi-title">{{ overview.sectionTitles.socialProof }}</h2>
        <div class="kpi-grid">
          <article v-for="kpi in conciseKpiCards" :key="kpi.id" class="kpi-card" :class="{ soft: kpi.soft }">
            <p class="kpi-label">{{ kpi.label }}</p>
            <strong class="kpi-value">{{ kpi.value }}</strong>
            <p class="kpi-desc">{{ kpi.desc }}</p>
          </article>
        </div>
      </section>

      <section class="final-cta">
        <h2>{{ overview.finalCta.title }}</h2>
        <a-button type="primary" size="large" class="btn-main" @click="goExperience">立即体验</a-button>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeOverviewApi, type HomeOverviewResponse } from '../../api/user/HomeApi'
import { userApiConfig } from '../../config/api'

const router = useRouter()

const overview = ref<HomeOverviewResponse | null>(null)
const overviewLoading = ref(false)
const loadError = ref('')

const apiSourceText = computed(() => (userApiConfig.mode === 'mock' ? 'Mock API' : '生产 API'))

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

const conciseFeatureCards = computed(() => (overview.value?.featureCards ?? []).slice(0, 3))
const conciseKpiCards = computed(() => (overview.value?.kpiCards ?? []).slice(0, 3))

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

const goRegister = () => {
  router.push('/user/auth/register')
}

const goExperience = () => {
  router.push('/user/workspace')
}

onMounted(() => {
  void loadOverview()
})
</script>

<style scoped>
.home-view {
  --home-border: var(--user-border);
  --home-text-main: var(--user-text-main);
  --home-text-sub: var(--user-text-sub);
  --home-text-minor: var(--user-text-minor);
  --home-surface: var(--user-surface);
  --home-surface-soft: var(--user-surface-soft);
  display: grid;
  gap: 14px;
  font-family: 'Plus Jakarta Sans', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background:
    radial-gradient(circle at 0% 0%, var(--user-gradient-a), transparent 35%),
    radial-gradient(circle at 100% 0%, var(--user-gradient-b), transparent 30%),
    var(--user-bg-base);
}

.hero-card {
  border: 1px solid var(--home-border);
  border-radius: 22px;
  padding: 30px 26px;
  background:
    radial-gradient(circle at 95% 8%, var(--user-gradient-hero-a), transparent 35%),
    radial-gradient(circle at 12% 100%, var(--user-gradient-hero-b), transparent 34%),
    var(--home-surface);
  box-shadow: var(--user-shadow-md);
  animation: rise-in 360ms ease-out both;
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
  padding: 5px 10px;
  font-size: 12px;
  background: var(--home-surface-soft);
}

.eyebrow {
  color: var(--user-accent-deep);
}

.source-tag {
  color: var(--home-text-minor);
}

.hero-card h1 {
  margin: 14px 0 0;
  font-size: clamp(28px, 4.6vw, 42px);
  line-height: 1.18;
  color: var(--home-text-main);
  letter-spacing: -0.02em;
}

.hero-desc {
  margin: 12px 0 0;
  color: var(--home-text-sub);
  line-height: 1.65;
  max-width: 640px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hero-actions {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
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

:deep(.btn-sub.ant-btn) {
  border-color: var(--user-border-strong);
  color: var(--user-text-sub);
  background: var(--user-surface);
}

:deep(.btn-sub.ant-btn:hover),
:deep(.btn-sub.ant-btn:focus) {
  border-color: var(--user-accent);
  color: var(--user-accent-deep);
}

.section-block {
  border: 1px solid var(--home-border);
  border-radius: 16px;
  padding: 16px;
  background: var(--home-surface);
  animation: rise-in 420ms ease-out both;
}

.section-block--soft {
  background: linear-gradient(160deg, var(--user-bg-base), var(--user-surface-soft));
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.section-title small {
  font-size: 12px;
  color: var(--home-text-minor);
}

.section-block h2 {
  margin: 0;
  font-size: 22px;
  color: var(--home-text-main);
}

.feature-grid,
.kpi-grid {
  margin-top: 10px;
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.feature-card,
.kpi-card {
  border: 1px solid var(--home-border);
  border-radius: 14px;
  padding: 13px;
  background: var(--home-surface);
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
}

.feature-card:hover,
.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--user-shadow-sm);
}

.feature-card.soft,
.kpi-card.soft {
  background: var(--home-surface-soft);
}

.feature-card h3 {
  margin: 0;
  font-size: 17px;
  color: var(--home-text-main);
}

.feature-card p {
  margin: 8px 0 0;
  color: var(--home-text-sub);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.kpi-label {
  margin: 0;
  font-size: 13px;
  color: var(--home-text-minor);
}

.kpi-value {
  display: block;
  margin-top: 6px;
  font-size: 30px;
  color: var(--home-text-main);
  line-height: 1.25;
}

.kpi-desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--home-text-minor);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.final-cta {
  border: 1px solid var(--home-border);
  border-radius: 20px;
  padding: 20px;
  background: linear-gradient(130deg, var(--user-bg-soft), var(--user-bg-muted));
  box-shadow: var(--user-shadow-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  animation: rise-in 480ms ease-out both;
}

.final-cta h2 {
  margin: 0;
  font-size: clamp(23px, 3.2vw, 30px);
  color: var(--home-text-main);
}

.kpi-title {
  margin: 0;
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

  .final-cta {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-block h2 {
    font-size: 20px;
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