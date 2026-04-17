<template>
  <UserPageSkeleton
    title="订阅中心"
    description="映射订阅中心骨架，后续接入额度消耗与账单管理。"
    prototype-page="subscription"
    :hints="['套餐状态', '额度用量', '账单记录']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../../components/user/UserPageSkeleton.vue'
</script>
