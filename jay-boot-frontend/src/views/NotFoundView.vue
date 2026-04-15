<template>
  <section class="page-placeholder">
    <h1 class="page-placeholder__title">404 - 页面不存在</h1>
    <p class="page-placeholder__desc">{{ description }}</p>
    <a-button type="primary" style="margin-top: 16px" @click="goBack">返回{{ isAdmin ? '控制台' : '首页' }}</a-button>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isAdmin = computed(() => route.path.startsWith('/admin'))

const description = computed(() =>
  isAdmin.value ? '当前路由未匹配，请返回控制台继续操作。' : '您访问的页面不存在，请返回首页继续浏览。'
)

const goBack = () => {
  router.push(isAdmin.value ? '/admin/dashboard' : '/user/home')
}
</script>
