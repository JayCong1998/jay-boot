<template>
  <a-layout class="app-layout" :class="{ 'app-layout--sider-open': isSiderOpen }">
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

    <button
      v-if="isMobile && isSiderOpen"
      type="button"
      class="app-sider-mask"
      aria-label="关闭侧边导航"
      @click="closeSider"
    />

    <a-layout>
      <a-layout-header class="app-header">
        <div class="app-header__left">
          <a-button class="app-header__menu-btn" type="text" aria-label="切换侧边导航" @click="toggleSider">
            <MenuOutlined />
          </a-button>
          <div class="app-header__title">{{ pageTitle }}</div>
        </div>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { MenuOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { useAuthStore } from '../../stores/admin/auth'

const MOBILE_BREAKPOINT = 992

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isMobile = ref(false)
const isSiderOpen = ref(false)

const menuItems: MenuProps['items'] = [
  { key: '/admin/dashboard', label: '控制台总览' },
  { key: '/admin/users', label: '用户管理' },
  { key: '/admin/plans', label: '套餐管理' },
  // { key: '/admin/rbac', label: '角色权限' },
  // { key: '/admin/billing', label: '订阅计费' },
  // { key: '/admin/apikey', label: 'API Key 管理' },
  // { key: '/admin/ai-gateway', label: 'AI Gateway' },
  // { key: '/admin/usage', label: '用量与运营' },
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

const closeSider = () => {
  isSiderOpen.value = false
}

const toggleSider = () => {
  if (!isMobile.value) {
    return
  }
  isSiderOpen.value = !isSiderOpen.value
}

const updateViewportState = () => {
  const nextIsMobile = window.innerWidth < MOBILE_BREAKPOINT
  isMobile.value = nextIsMobile

  if (!nextIsMobile) {
    isSiderOpen.value = false
  }
}

const onMenuClick: MenuProps['onClick'] = ({ key }) => {
  router.push(String(key))
  if (isMobile.value) {
    closeSider()
  }
}

const onLogout = async () => {
  await authStore.logout()
  await router.replace('/admin/auth/login')
}

onMounted(() => {
  void authStore.hydrate()
  updateViewportState()
  window.addEventListener('resize', updateViewportState)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportState)
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  position: relative;
  overflow-x: clip;
}

.app-sider {
  border-right: 1px solid #e8ecf3;
}

.app-sider-mask {
  position: fixed;
  inset: 0;
  border: none;
  padding: 0;
  margin: 0;
  z-index: 1000;
  background: rgba(15, 23, 42, 0.35);
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

.app-header__left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.app-header__menu-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
}

.app-header__title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
  line-height: 1.3;
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

@media (max-width: 991px) {
  .app-sider {
    position: fixed !important;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1001;
    height: 100dvh;
    transform: translateX(-100%);
    transition: transform 0.24s ease;
    box-shadow: 0 18px 28px rgba(15, 23, 42, 0.2);
  }

  .app-layout--sider-open .app-sider {
    transform: translateX(0);
  }

  .app-header {
    padding: 12px 16px;
  }

  .app-header__menu-btn {
    display: inline-flex;
  }

  .app-header__title {
    font-size: 16px;
  }

  .app-content {
    padding: 16px;
  }
}

@media (max-width: 575px) {
  .app-header {
    flex-wrap: wrap;
    gap: 6px;
  }

  .app-header__actions {
    margin-left: auto;
  }

  .app-header__user {
    display: none;
  }

  .app-content {
    padding: 12px;
  }
}
</style>
