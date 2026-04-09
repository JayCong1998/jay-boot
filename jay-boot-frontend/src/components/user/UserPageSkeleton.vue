<template>
  <section class="user-page-skeleton">
    <a-card :bordered="false" class="hero-card">
      <div class="eyebrow">{{ eyebrow }}</div>
      <h1 class="hero-title">{{ title }}</h1>
      <p class="hero-desc">{{ description }}</p>

      <div class="tag-list" v-if="hints.length > 0">
        <a-tag v-for="hint in hints" :key="hint" color="blue">{{ hint }}</a-tag>
      </div>

      <div class="hero-actions">
        <a-button type="primary">接入业务逻辑</a-button>
        <a-button>补充交互细节</a-button>
      </div>
    </a-card>

    <a-card class="mapping-card">
      <template #title>原型映射</template>
      <a-descriptions :column="1" size="small" bordered>
        <a-descriptions-item label="product_ui 页面">{{ prototypeFile }}</a-descriptions-item>
        <a-descriptions-item label="当前路由">{{ route.path }}</a-descriptions-item>
        <a-descriptions-item label="状态">已完成 Vue 骨架映射</a-descriptions-item>
      </a-descriptions>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const props = withDefaults(
  defineProps<{
    title: string
    description: string
    prototypePage: string
    eyebrow?: string
    hints?: string[]
  }>(),
  {
    eyebrow: '品牌质感官网页面骨架',
    hints: () => [],
  },
)

const route = useRoute()

const prototypeFile = computed(() => `product_ui/pages/${props.prototypePage}.html`)
</script>

<style scoped>
.user-page-skeleton {
  display: grid;
  gap: 12px;
}

.hero-card {
  border-radius: 16px;
  border: 1px solid #d6e2f0;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d6e2f0;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  color: #1e3a8a;
  background: #f1f6fd;
}

.hero-title {
  margin: 12px 0 0;
  font-size: clamp(28px, 4vw, 36px);
  line-height: 1.2;
  color: #0f172a;
}

.hero-desc {
  margin: 10px 0 0;
  color: #475569;
  line-height: 1.75;
}

.tag-list {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-actions {
  margin-top: 14px;
  display: flex;
  gap: 8px;
}

.mapping-card {
  border-radius: 14px;
  border: 1px solid #d6e2f0;
}
</style>