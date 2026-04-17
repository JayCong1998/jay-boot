<template>
  <UserPageSkeleton
    title="创作工作台"
    description="映射工作台骨架，后续接入生成参数、版本结果与编辑能力。"
    prototype-page="workspace"
    :hints="['参数输入', '多版本结果', '交付动作']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../../components/user/UserPageSkeleton.vue'
</script>
