<template>
  <UserPageSkeleton
    title="订阅定价"
    description="映射定价与权益对比页，后续接入真实套餐与支付链路。"
    prototype-page="pricing"
    :hints="['套餐卡片', '权益对比', 'FAQ']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../components/user/UserPageSkeleton.vue'
</script>