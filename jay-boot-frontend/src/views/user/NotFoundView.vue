<template>
  <UserPageSkeleton
    title="页面不存在"
    description="映射 404 页骨架，后续接入异常监控与路径恢复建议。"
    prototype-page="not-found"
    :hints="['恢复路径', '常用入口', '问题反馈']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../components/user/UserPageSkeleton.vue'
</script>