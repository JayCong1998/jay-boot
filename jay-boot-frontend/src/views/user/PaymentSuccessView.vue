<template>
  <UserPageSkeleton
    title="支付成功"
    description="映射支付成功页骨架，后续接入订单状态与权益开通回执。"
    prototype-page="payment-success"
    :hints="['订单回执', '权益开通', '后续动作']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../components/user/UserPageSkeleton.vue'
</script>