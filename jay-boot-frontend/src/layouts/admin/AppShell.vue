<template>
  <a-layout class="app-layout">
    <a-layout-sider :width="240" class="app-sider" theme="light">
      <div class="app-brand">
        <span class="app-brand__logo">JB</span>
        <div>
          <strong>Jay Boot</strong>
          <p>SaaS Console</p>
        </div>
      </div>
      <a-menu mode="inline" :items="menuItems" :selected-keys="selectedKeys" @click="onMenuClick" />
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="app-header">
        <div class="app-header__title">{{ pageTitle }}</div>
        <div class="app-header__actions">
          <span class="app-header__user">{{ authStore.displayName }}</span>
          <a-button type="link" @click="onLogout">退出登录</a-button>
        </div>
      </a-layout-header>
      <a-layout-content class="app-content">
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import { useAuthStore } from '../../stores/admin/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const menuItems: MenuProps['items'] = [
  { key: '/admin/dashboard', label: '控制台总览' },
  { key: '/admin/rbac', label: '角色权限' },
  { key: '/admin/billing', label: '订阅计费' },
  { key: '/admin/apikey', label: 'API Key 管理' },
  { key: '/admin/ai-gateway', label: 'AI Gateway' },
  { key: '/admin/usage', label: '用量与运营' },
]

const selectedKeys = computed(() => {
  const availablePathSet = new Set(menuItems?.map((item) => String(item?.key)))
  if (availablePathSet.has(route.path)) {
    return [route.path]
  }
  return ['/admin/dashboard']
})

const pageTitle = computed(() => {
  if (typeof route.meta.title === 'string') {
    return route.meta.title
  }
  return 'Jay Boot Frontend'
})

const onMenuClick: MenuProps['onClick'] = ({ key }) => {
  router.push(String(key))
}

const onLogout = async () => {
  await authStore.logout()
  await router.replace('/admin/auth/login')
}

onMounted(() => {
  void authStore.hydrate()
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-sider {
  border-right: 1px solid #e8ecf3;
}

.app-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
}

.app-brand__logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #6366f1;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.app-brand strong {
  display: block;
  font-size: 14px;
  color: #1f2d3d;
}

.app-brand p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #6f7a88;
}

.app-header {
  height: auto;
  line-height: normal;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8ecf3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.app-header__title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.app-header__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-header__user {
  font-size: 13px;
  color: #354052;
}

.app-content {
  padding: 24px;
}
</style>
