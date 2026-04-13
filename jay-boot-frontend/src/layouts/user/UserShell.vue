<template>
  <div class="user-shell">
    <header class="site-header">
      <div class="shell topbar">
        <RouterLink class="brand" to="/user/home">
          <span class="brand-logo">JC</span>
          <span class="brand-text">
            <strong>JayBoot Creator</strong>
            <small>用户端体验骨架</small>
          </span>
        </RouterLink>

        <nav class="top-nav" aria-label="用户端导航">
          <RouterLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="top-nav__link"
            :aria-current="isActive(item.path) ? 'page' : undefined"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="top-actions">
          <RouterLink v-if="!userAuthStore.isLoggedIn" class="action-link" to="/user/auth/login">
            登录
          </RouterLink>
          <a-popover v-else placement="bottomRight" trigger="hover">
            <template #content>
              <div class="user-popover">
                <strong class="user-popover__name">{{ userName }}</strong>
                <p class="user-popover__email">{{ userEmail }}</p>
                <button type="button" class="user-popover__logout" @click="onLogout">退出登录</button>
              </div>
            </template>
            <button type="button" class="user-avatar" :aria-label="`当前用户 ${userName}`">
              {{ avatarInitial }}
            </button>
          </a-popover>
        </div>
      </div>
    </header>

    <main class="user-main">
      <div class="shell content-wrap">
        <RouterView />
      </div>
    </main>

    <footer class="site-footer">
      <div class="shell site-footer__inner">
        <span>© 2026 JayBoot Creator</span>
        <span>Vue 用户端骨架（映射 product_ui）</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useUserAuthStore } from '../../stores/user/auth'

const route = useRoute()
const router = useRouter()
const userAuthStore = useUserAuthStore()

const navItems = [
  { path: '/user/home', label: '首页' },
  // { path: '/user/features', label: '功能' },
  { path: '/user/pricing', label: '定价' },
  { path: '/user/workspace', label: '工作台' },
  // { path: '/user/history', label: '历史' },
  // { path: '/user/subscription', label: '订阅' },
  // { path: '/user/help', label: '帮助' },
]

const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)

const userName = computed(() => userAuthStore.user?.username || 'User')
const userEmail = computed(() => userAuthStore.user?.email || '')
const avatarInitial = computed(() => {
  const first = userName.value.trim().charAt(0)
  return (first || 'U').toUpperCase()
})

const onLogout = async () => {
  await userAuthStore.logout()
  await router.push('/user/home')
}

onMounted(async () => {
  await userAuthStore.hydrate()
})
</script>

<style scoped>
.user-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at 0 0, var(--user-gradient-a), transparent 34%),
    radial-gradient(circle at 100% 0, var(--user-gradient-b), transparent 28%),
    var(--user-bg-base);
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(8px);
  background: rgba(255, 250, 252, 0.92);
  border-bottom: 1px solid var(--user-border);
}

.shell {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
}

.topbar {
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: inherit;
}

.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #f8fafc;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.04em;
  background: linear-gradient(140deg, var(--user-accent-deep), var(--user-text-minor));
  box-shadow: 0 10px 20px rgba(var(--user-shadow-rgb), 0.28);
}

.brand-text {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.brand-text strong {
  font-size: 15px;
  line-height: 1.2;
  color: var(--user-text-main);
}

.brand-text small {
  font-size: 12px;
  color: var(--user-text-minor);
}

.top-nav {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.top-nav__link {
  border: 1px solid transparent;
  border-radius: 10px;
  padding: 8px 11px;
  font-size: 13px;
  text-decoration: none;
  color: var(--user-text-sub);
  transition: 0.2s ease;
  white-space: nowrap;
}

.top-nav__link:hover,
.top-nav__link[aria-current='page'] {
  color: var(--user-text-main);
  border-color: var(--user-border-strong);
  background: var(--user-surface-soft);
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-link {
  border: 1px solid var(--user-border-strong);
  border-radius: 10px;
  padding: 8px 12px;
  text-decoration: none;
  font-size: 13px;
  color: var(--user-text-main);
  background: var(--user-surface);
  transition: border-color 0.2s ease, color 0.2s ease, background-color 0.2s ease;
}

.action-link:hover {
  border-color: var(--user-accent);
  color: var(--user-accent-deep);
  background: var(--user-surface-soft);
}

.user-avatar {
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.03em;
  cursor: pointer;
  color: #f8fafc;
  background: linear-gradient(135deg, var(--user-accent), var(--user-accent-hover));
  box-shadow: 0 10px 22px rgba(var(--user-shadow-rgb), 0.26);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.user-avatar:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 24px rgba(var(--user-shadow-rgb), 0.32);
}

.user-avatar:focus-visible {
  outline: 2px solid var(--user-accent);
  outline-offset: 2px;
}

.user-popover {
  min-width: 180px;
  display: grid;
  gap: 8px;
}

.user-popover__name {
  font-size: 14px;
  line-height: 1.2;
  color: var(--user-text-main);
}

.user-popover__email {
  margin: 0;
  font-size: 12px;
  color: var(--user-text-sub);
  word-break: break-all;
}

.user-popover__logout {
  border: 1px solid var(--user-border-strong);
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 12px;
  color: var(--user-text-main);
  background: var(--user-surface);
  cursor: pointer;
  transition: border-color 0.2s ease, color 0.2s ease, background-color 0.2s ease;
}

.user-popover__logout:hover {
  border-color: var(--user-accent);
  color: var(--user-accent-deep);
  background: var(--user-surface-soft);
}

.user-main {
  flex: 1;
  padding: 26px 0 40px;
}

.content-wrap {
  display: grid;
  gap: 14px;
}

.site-footer {
  border-top: 1px solid var(--user-border);
  background: var(--user-surface);
}

.site-footer__inner {
  min-height: 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: var(--user-text-minor);
}

@media (max-width: 980px) {
  .shell {
    width: min(1180px, calc(100% - 24px));
  }

  .topbar {
    min-height: auto;
    padding: 10px 0;
    flex-wrap: wrap;
  }

  .top-nav {
    width: 100%;
    order: 3;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .top-actions {
    margin-left: auto;
  }
}

@media (max-width: 768px) {
  .shell {
    width: min(1180px, calc(100% - 20px));
  }

  .topbar {
    gap: 10px;
  }

  .brand-text small {
    display: none;
  }

  .top-nav {
    scrollbar-width: thin;
  }

  .top-nav__link {
    padding: 7px 10px;
  }

  .user-main {
    padding: 18px 0 28px;
  }

  .site-footer__inner {
    min-height: auto;
    padding: 12px 0;
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 575px) {
  .brand {
    max-width: calc(100vw - 160px);
  }

  .brand-text strong {
    font-size: 14px;
  }

  .top-actions {
    gap: 6px;
  }

  .action-link {
    padding: 7px 10px;
  }

  .user-main {
    padding: 14px 0 22px;
  }
}
</style>
