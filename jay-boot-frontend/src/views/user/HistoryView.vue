<template>
  <UserPageSkeleton
    title="历史记录"
    description="映射历史记录骨架，后续接入筛选、复盘与资产复用。"
    prototype-page="history"
    :hints="['任务筛选', '效果复盘', '模板沉淀']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../components/user/UserPageSkeleton.vue'
</script>