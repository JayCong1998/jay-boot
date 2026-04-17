<template>
  <UserPageSkeleton
    title="支付页"
    description="映射支付页骨架，后续接入订单确认与支付方式联动。"
    prototype-page="checkout"
    :hints="['订单总览', '支付方式', '发票信息']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../../components/user/UserPageSkeleton.vue'
</script>
