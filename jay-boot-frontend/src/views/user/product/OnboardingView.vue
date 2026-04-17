<template>
  <UserPageSkeleton
    title="新手引导"
    description="映射引导页骨架，后续接入分步配置与推荐策略逻辑。"
    prototype-page="onboarding"
    :hints="['步骤引导', '偏好配置', '首稿成功率']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../../components/user/UserPageSkeleton.vue'
</script>
