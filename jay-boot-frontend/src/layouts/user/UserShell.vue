<template>
  <div class="user-shell">
    <header class="site-header">
      <div class="shell shell--wide topbar">
        <div class="topbar__left">
          <RouterLink class="brand" to="/user/home" aria-label="JayBoot 首页">
            <span class="brand-logo">JB</span>
            <span class="brand-text">JayBoot</span>
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
        </div>

        <div class="topbar__right">
          <label class="header-search" aria-label="站内搜索">
            <input type="search" placeholder="搜索功能、页面或关键字..." />
          </label>

          <RouterLink v-if="!userAuthStore.isLoggedIn" class="action-link action-link--ghost" to="/user/auth/login">
            登录
          </RouterLink>
          <RouterLink v-if="!userAuthStore.isLoggedIn" class="action-link action-link--solid" to="/user/auth/register">
            注册
          </RouterLink>

          <template v-else>
            <RouterLink class="action-link action-link--ghost" to="/user/workspace">工作台</RouterLink>
            <a-popover placement="bottomRight" trigger="hover">
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
          </template>
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
        <div class="site-footer__meta">© 2026 JayBoot, Inc.</div>
        <nav class="site-footer__links" aria-label="页脚导航">
          <RouterLink to="/user/home">首页</RouterLink>
          <RouterLink to="/user/features">解决方案</RouterLink>
          <RouterLink to="/user/pricing">定价</RouterLink>
          <RouterLink to="/user/help">帮助中心</RouterLink>
        </nav>
        <div class="site-footer__locale">简体中文</div>
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
  { path: '/user/home', label: '平台' },
  { path: '/user/features', label: '解决方案' },
  { path: '/user/help', label: '资源' },
  { path: '/user/onboarding', label: '开放能力' },
  { path: '/user/pricing', label: '定价' },
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
  background: #f6f8fa;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: #0d1117;
  border-bottom: 1px solid #30363d;
}

.shell {
  width: min(1680px, calc(100% - clamp(20px, 3vw, 48px)));
  margin: 0 auto;
}

.shell--wide {
  width: calc(100% - 48px);
  max-width: none;
}

.topbar {
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 14px;
}

.topbar__left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  flex: 1 1 auto;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #f0f6fc;
}

.brand-logo {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f0f6fc;
  color: #0d1117;
  font-size: 11px;
  font-weight: 700;
}

.brand-text {
  font-size: 15px;
  font-weight: 600;
  color: #f0f6fc;
}

.top-nav {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 2px;
}

.top-nav__link {
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 13px;
  text-decoration: none;
  color: #c9d1d9;
  white-space: nowrap;
}

.top-nav__link:hover,
.top-nav__link[aria-current='page'] {
  color: #f0f6fc;
  border-color: #30363d;
  background: rgba(177, 186, 196, 0.12);
}

.topbar__right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-left: auto;
  flex: 0 0 auto;
}

.header-search {
  display: inline-flex;
  align-items: center;
}

.header-search input {
  width: min(300px, 30vw);
  height: 32px;
  border: 1px solid #3d444d;
  border-radius: 8px;
  background: #0d1117;
  color: #c9d1d9;
  padding: 0 12px;
  font-size: 12px;
}

.header-search input::placeholder {
  color: #8b949e;
}

.header-search input:focus {
  outline: none;
  border-color: #58a6ff;
  box-shadow: 0 0 0 2px rgba(56, 139, 253, 0.22);
}

.action-link {
  border-radius: 8px;
  padding: 7px 12px;
  font-size: 13px;
  text-decoration: none;
}

.action-link--ghost {
  border: 1px solid transparent;
  color: #c9d1d9;
}

.action-link--ghost:hover {
  border-color: #30363d;
  color: #f0f6fc;
}

.action-link--solid {
  border: 1px solid #8b949e;
  color: #f0f6fc;
}

.action-link--solid:hover {
  border-color: #c9d1d9;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border: 1px solid #8b949e;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.03em;
  cursor: pointer;
  color: #f0f6fc;
  background: #1f6feb;
}

.user-avatar:hover {
  filter: brightness(1.08);
}

.user-avatar:focus-visible {
  outline: 2px solid #58a6ff;
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
  color: #1f2328;
}

.user-popover__email {
  margin: 0;
  font-size: 12px;
  color: #57606a;
  word-break: break-all;
}

.user-popover__logout {
  border: 1px solid #d0d7de;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 12px;
  color: #1f2328;
  background: #ffffff;
  cursor: pointer;
}

.user-popover__logout:hover {
  border-color: #afb8c1;
}

.user-main {
  flex: 1;
  padding: 24px 0 36px;
}

.content-wrap {
  width: 100%;
  min-width: 0;
  max-width: none;
}

.site-footer {
  border-top: 1px solid #d0d7de;
  background: #ffffff;
}

.site-footer__inner {
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.site-footer__meta,
.site-footer__locale {
  font-size: 12px;
  color: #57606a;
  white-space: nowrap;
}

.site-footer__links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.site-footer__links a {
  color: #57606a;
  text-decoration: none;
  font-size: 12px;
}

.site-footer__links a:hover {
  color: #0969da;
}

@media (max-width: 1024px) {
  .header-search {
    display: none;
  }
}

@media (max-width: 980px) {
  .shell--wide {
    width: calc(100% - 24px);
  }

  .shell {
    width: min(1680px, calc(100% - 24px));
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

  .topbar__right {
    margin-left: auto;
  }
}

@media (max-width: 768px) {
  .shell--wide {
    width: calc(100% - 20px);
  }

  .shell {
    width: min(1680px, calc(100% - 20px));
  }

  .brand-text {
    display: none;
  }

  .topbar__right {
    gap: 6px;
  }

  .action-link {
    padding: 6px 10px;
  }

  .site-footer__inner {
    min-height: auto;
    padding: 12px 0;
    flex-direction: column;
    align-items: flex-start;
  }

  .site-footer__links {
    flex-wrap: wrap;
    justify-content: flex-start;
    gap: 10px;
  }
}
</style>
