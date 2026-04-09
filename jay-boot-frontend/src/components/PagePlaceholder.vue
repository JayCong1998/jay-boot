<template>
  <section class="page-placeholder">
    <h1 class="page-placeholder__title">{{ title }}</h1>
    <p class="page-placeholder__desc">{{ description }}</p>
    <a-alert
      style="margin-top: 16px"
      type="info"
      show-icon
      message="当前仅完成脚手架与路由初始化"
      description="该页面后续接入业务组件与 API 调用。"
    />
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description: string
}>()
</script>
