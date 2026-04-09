<template>
  <section class="page-placeholder">
    <h1 class="page-placeholder__title">404 - 页面不存在</h1>
    <p class="page-placeholder__desc">当前路由未匹配，请返回控制台继续操作。</p>
    <a-button type="primary" style="margin-top: 16px" @click="goDashboard">返回控制台</a-button>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goDashboard = () => {
  router.push('/admin/dashboard')
}
</script>
