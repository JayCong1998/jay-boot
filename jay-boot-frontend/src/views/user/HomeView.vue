<template>
  <section class="home-view">
    <section class="hero-card">
      <div class="hero-layout">
        <div class="hero-main">
          <div class="hero-meta" v-once>
            <span class="eyebrow">{{ homeData.hero.eyebrow }}</span>
          </div>
          <h1>{{ homeData.hero.title }}</h1>
          <p class="hero-desc">{{ homeData.hero.description }}</p>

          <div class="hero-actions">
            <a-button type="primary" size="large" class="btn-main" @click="goExperience">立即体验</a-button>
            <a-button size="large" class="btn-sub" @click="goRegister">免费注册</a-button>
          </div>
        </div>

        <div class="hero-side">
          <article v-for="card in conciseFeatureCards" :key="`hero_${card.id}`" class="hero-side-card">
            <h3>{{ card.title }}</h3>
            <p>{{ card.points[0] }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="section-block section-block--soft">
      <div class="section-title">
        <h2>{{ homeData.sectionTitles.features }}</h2>
      </div>
      <div class="feature-grid">
        <article v-for="card in conciseFeatureCards" :key="card.id" class="feature-card">
          <h3>{{ card.title }}</h3>
          <p>{{ card.points[0] || '快速上手，流畅创作。' }}</p>
        </article>
      </div>
    </section>

    <section class="section-block">
      <h2 class="kpi-title">{{ homeData.sectionTitles.socialProof }}</h2>
      <div class="kpi-grid">
        <article v-for="kpi in conciseKpiCards" :key="kpi.id" class="kpi-card">
          <p class="kpi-label">{{ kpi.label }}</p>
          <strong class="kpi-value">{{ kpi.value }}</strong>
          <p class="kpi-desc">{{ kpi.desc }}</p>
        </article>
      </div>
    </section>

    <section class="final-cta">
      <h2>{{ homeData.finalCta.title }}</h2>
      <a-button type="primary" size="large" class="btn-main" @click="goExperience">立即体验</a-button>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

interface HomeFeatureCard {
  id: string
  title: string
  points: string[]
  soft: boolean
}

interface HomeKpiCard {
  id: string
  label: string
  value: string
  desc: string
  soft: boolean
}

interface HomeData {
  hero: {
    eyebrow: string
    title: string
    description: string
  }
  sectionTitles: {
    features: string
    socialProof: string
  }
  featureCards: HomeFeatureCard[]
  kpiCards: HomeKpiCard[]
  finalCta: {
    title: string
  }
}

const homeData: HomeData = {
  hero: {
    eyebrow: '面向内容创业者的增长引擎',
    title: '10 秒生成可发布文案',
    description: '从选题、生成、迭代到交付，帮助你把 AI 创作能力直接转化为稳定收益。首页目标是“看完即理解，点击就试用”。',
  },
  sectionTitles: {
    features: '核心价值',
    socialProof: '社证数据',
  },
  featureCards: [
    {
      id: 'feature_1',
      title: '高转化模板',
      points: ['按平台和场景生成文案结构', '支持热门选题拆解与复刻', '减少从 0 到 1 的思考成本'],
      soft: false,
    },
    {
      id: 'feature_2',
      title: '多版本并行',
      points: ['一次生成多个风格版本', '便于 A/B 测试提升转化', '保留历史用于持续复盘'],
      soft: false,
    },
    {
      id: 'feature_3',
      title: '结果可交付',
      points: ['支持链接交付给客户或团队', '支持导出 Markdown 与图片文档', '形成“创作-交付-复购”闭环'],
      soft: true,
    },
  ],
  kpiCards: [
    {
      id: 'kpi_1',
      label: '累计生成内容',
      value: '2,900 万+',
      desc: '覆盖营销、私域、直播、电商等场景',
      soft: false,
    },
    {
      id: 'kpi_2',
      label: '7 日留存',
      value: '74%',
      desc: '核心用户持续使用工作台',
      soft: false,
    },
    {
      id: 'kpi_3',
      label: '付费转化',
      value: '12.8%',
      desc: '通过案例页进入的用户转化更高',
      soft: true,
    },
  ],
  finalCta: {
    title: '准备好把创作效率变成盈利能力了吗？',
  },
}

const router = useRouter()

const conciseFeatureCards = computed(() => homeData.featureCards.slice(0, 3))
const conciseKpiCards = computed(() => homeData.kpiCards.slice(0, 3))

const goRegister = () => {
  router.push('/user/auth/register')
}

const goExperience = () => {
  router.push('/user/workspace')
}
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
  gap: 18px;
  min-height: calc(100vh - 168px);
  font-family: 'Plus Jakarta Sans', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: var(--user-bg-base);
}

.hero-card {
  border: 1px solid var(--home-border);
  border-radius: 18px;
  padding: 28px 24px;
  background: var(--home-surface);
  box-shadow: var(--user-shadow-sm);
  animation: rise-in 360ms ease-out both;
}

.hero-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(300px, 0.92fr);
  gap: 18px;
  align-items: stretch;
}

.hero-main {
  min-width: 0;
}

.hero-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.eyebrow {
  display: inline-flex;
  border: 1px solid var(--home-border);
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
  background: var(--home-surface-soft);
  color: var(--user-accent-deep);
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
}

.hero-side {
  min-width: 0;
  display: grid;
  gap: 10px;
  grid-template-columns: 1fr;
}

.hero-side-card {
  border: 1px solid var(--home-border);
  border-radius: 14px;
  padding: 14px;
  min-height: 92px;
  background: var(--home-surface);
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
}

.hero-side-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--user-shadow-sm);
}

.hero-side-card h3 {
  margin: 0;
  font-size: 16px;
  color: var(--home-text-main);
}

.hero-side-card p {
  margin: 8px 0 0;
  color: var(--home-text-sub);
  line-height: 1.55;
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
  padding: 18px;
  background: var(--home-surface);
  animation: rise-in 420ms ease-out both;
}

.section-block--soft {
  background: var(--home-surface);
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
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.feature-card,
.kpi-card {
  border: 1px solid var(--home-border);
  border-radius: 14px;
  padding: 14px;
  min-height: 116px;
  background: var(--home-surface);
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
}

.feature-card:hover,
.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--user-shadow-sm);
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
  line-height: 1.55;
  color: var(--home-text-minor);
}

.final-cta {
  border: 1px solid var(--home-border);
  border-radius: 16px;
  padding: 22px;
  background: var(--home-surface);
  box-shadow: var(--user-shadow-sm);
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
  .hero-layout {
    grid-template-columns: 1fr;
  }

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

  .hero-actions :deep(.ant-btn) {
    flex: 1 1 calc(50% - 8px);
    min-width: 120px;
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
  .kpi-card,
  .hero-side-card {
    transition: none;
  }

  .feature-card:hover,
  .kpi-card:hover,
  .hero-side-card:hover {
    transform: none;
    box-shadow: none;
  }
}
</style>
