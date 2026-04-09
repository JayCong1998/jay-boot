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
          <RouterLink class="action-link" to="/admin/dashboard">管理端</RouterLink>
          <RouterLink class="action-link" to="/user/auth/login">登录</RouterLink>
          <RouterLink class="action-link action-link--primary" to="/user/auth/register">免费试用</RouterLink>
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
import { RouterLink, RouterView, useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { path: '/user/home', label: '首页' },
  { path: '/user/features', label: '功能' },
  { path: '/user/pricing', label: '定价' },
  { path: '/user/workspace', label: '工作台' },
  { path: '/user/history', label: '历史' },
  { path: '/user/subscription', label: '订阅' },
  { path: '/user/help', label: '帮助' },
]

const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)
</script>

<style scoped>
.user-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at 0 0, rgba(30, 58, 138, 0.1), transparent 34%),
    radial-gradient(circle at 100% 0, rgba(202, 138, 4, 0.1), transparent 28%),
    #f4f8fd;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(8px);
  background: rgba(248, 251, 255, 0.92);
  border-bottom: 1px solid #d6e2f0;
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
  background: linear-gradient(140deg, #0f172a, #1e3a8a);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.28);
}

.brand-text {
  display: grid;
  gap: 2px;
}

.brand-text strong {
  font-size: 15px;
  line-height: 1.2;
  color: #0f172a;
}

.brand-text small {
  font-size: 12px;
  color: #64748b;
}

.top-nav {
  display: flex;
  align-items: center;
  gap: 6px;
}

.top-nav__link {
  border: 1px solid transparent;
  border-radius: 10px;
  padding: 8px 11px;
  font-size: 13px;
  text-decoration: none;
  color: #475569;
  transition: 0.2s ease;
}

.top-nav__link:hover,
.top-nav__link[aria-current='page'] {
  color: #0f172a;
  border-color: #bfd2e8;
  background: #edf3fb;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-link {
  border: 1px solid #d2dcea;
  border-radius: 10px;
  padding: 8px 12px;
  text-decoration: none;
  font-size: 13px;
  color: #0f172a;
  background: #fff;
}

.action-link--primary {
  border-color: transparent;
  color: #fff;
  background: linear-gradient(135deg, #ca8a04, #e0a21a);
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
  border-top: 1px solid #d6e2f0;
  background: #ffffff;
}

.site-footer__inner {
  min-height: 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

@media (max-width: 980px) {
  .topbar {
    min-height: auto;
    padding: 10px 0;
    flex-wrap: wrap;
  }

  .top-nav {
    width: 100%;
    order: 3;
    overflow-x: auto;
  }
}
</style>