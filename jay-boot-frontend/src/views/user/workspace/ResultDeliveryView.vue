<template>
  <UserPageSkeleton
    title="结果交付"
    description="映射结果交付骨架，后续接入导出与分享能力。"
    prototype-page="result-delivery"
    :hints="['导出交付', '分享链接', '版本归档']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../../components/user/UserPageSkeleton.vue'
</script>
