<template>
  <UserPageSkeleton
    title="帮助中心"
    description="映射帮助中心骨架，后续接入知识库检索与工单服务。"
    prototype-page="help"
    :hints="['FAQ 搜索', '分类支持', '客服入口']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../components/user/UserPageSkeleton.vue'
</script>