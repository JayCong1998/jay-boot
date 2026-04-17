<template>
  <UserPageSkeleton
    title="个人中心"
    description="映射个人中心骨架，后续接入资料维护与账号安全设置。"
    prototype-page="profile"
    :hints="['资料维护', '安全设置', '设备管理']"
  />
</template>

<script setup lang="ts">
import UserPageSkeleton from '../../../components/user/UserPageSkeleton.vue'
</script>
