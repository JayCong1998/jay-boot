<template>
  <UserPageSkeleton
    title="邀请返利"
    description="映射邀请返利骨架，后续接入分享链路与返利结算。"
    prototype-page="referral"
    :hints="['邀请链接', '返利统计', '流水明细']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../components/user/UserPageSkeleton.vue'
</script>