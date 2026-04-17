<template>
  <section class="auth-view">
    <article class="auth-intro">
      <p class="auth-intro__tag">Creator Workspace</p>
      <h1>欢迎回来，继续你的创作增长计划</h1>
      <p class="auth-intro__desc">
        登录后可进入用户工作台，继续管理内容生产、交付记录和订阅计划。
      </p>
      <ul class="auth-intro__list">
        <li>多场景文案工作流</li>
        <li>可追踪的交付历史</li>
        <li>团队协作与权限共享</li>
      </ul>
    </article>

    <article class="auth-card-wrap">
      <a-card class="auth-card" :bordered="false">
        <template #title>邮箱登录</template>

        <a-alert
          class="auth-card__mode"
          type="info"
          show-icon
          :message="`当前数据源：${apiModeText}`"
          description="可通过 VITE_USER_API_MODE 切换 mock 与真实接口"
        />

        <a-form layout="vertical" :model="formState" :rules="rules" @finish="onFinish">
          <a-form-item label="邮箱" name="email">
            <a-input
              v-model:value="formState.email"
              placeholder="请输入邮箱"
              autocomplete="email"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="密码" name="password">
            <a-input-password
              v-model:value="formState.password"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </a-form-item>

          <a-form-item>
            <a-button type="primary" html-type="submit" block :loading="submitting">登录并进入工作台</a-button>
          </a-form-item>
        </a-form>

        <div class="auth-card__footer">
          还没有账号？
          <RouterLink :to="registerLink">立即注册</RouterLink>
        </div>
      </a-card>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { apiConfig } from '../../../config/api'
import { useUserAuthStore } from '../../../stores/user/auth'

const router = useRouter()
const route = useRoute()
const userAuthStore = useUserAuthStore()

const formState = reactive({
  email: '',
  password: '',
})

const submitting = ref(false)

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
    { max: 10, message: '密码长度不能超过 10 位', trigger: 'blur' },
  ],
}

const apiModeText = computed(() => (apiConfig.mode === 'mock' ? 'Mock API' : '真实 API'))

const registerLink = computed(() => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  if (redirect) {
    return { path: '/user/auth/register', query: { redirect } }
  }
  return { path: '/user/auth/register' }
})

const onFinish = async () => {
  submitting.value = true
  try {
    await userAuthStore.login({
      email: formState.email.trim(),
      password: formState.password,
    })
    message.success('登录成功')
    await router.replace('/user/workspace')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('登录失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await userAuthStore.hydrate()
  if (userAuthStore.isLoggedIn) {
    await router.replace('/user/workspace')
  }
})
</script>

<style scoped>
.auth-view {
  --auth-bg: var(--user-bg-base);
  --auth-panel: var(--user-surface);
  --auth-panel-soft: var(--user-surface-soft);
  --auth-border: var(--user-border);
  --auth-text-main: var(--user-text-main);
  --auth-text-sub: var(--user-text-sub);
  min-height: calc(100vh - 180px);
  display: grid;
  grid-template-columns: minmax(320px, 1fr) minmax(320px, 460px);
  gap: 18px;
  padding: 8px;
  background:
    radial-gradient(circle at 8% 10%, var(--user-gradient-a), transparent 38%),
    radial-gradient(circle at 92% 90%, var(--user-gradient-b), transparent 35%),
  var(--auth-bg);
  border: 1px solid var(--auth-border);
  border-radius: 20px;
}

.auth-intro,
.auth-card-wrap {
  background: var(--auth-panel);
  border: 1px solid var(--auth-border);
  border-radius: 16px;
}

.auth-intro {
  padding: 28px;
}

.auth-intro__tag {
  display: inline-flex;
  border: 1px solid var(--auth-border);
  border-radius: 999px;
  padding: 6px 10px;
  background: var(--auth-panel-soft);
  color: var(--user-accent-deep);
  font-size: 12px;
}

.auth-intro h1 {
  margin: 12px 0 0;
  font-size: clamp(30px, 4.2vw, 44px);
  line-height: 1.15;
  letter-spacing: -0.02em;
  color: var(--auth-text-main);
}

.auth-intro__desc {
  margin: 14px 0 0;
  color: var(--auth-text-sub);
  line-height: 1.75;
}

.auth-intro__list {
  margin: 18px 0 0;
  padding-left: 18px;
  display: grid;
  gap: 10px;
  color: var(--auth-text-sub);
}

.auth-card-wrap {
  padding: 14px;
  display: flex;
  align-items: center;
}

.auth-card {
  width: 100%;
  border-radius: 14px;
}

.auth-card__mode {
  margin-bottom: 12px;
}

.auth-card__footer {
  margin-top: 10px;
  text-align: center;
  color: var(--user-text-minor);
}

.auth-card :deep(.ant-form-item:last-child) {
  margin-bottom: 8px;
}

@media (max-width: 980px) {
  .auth-view {
    grid-template-columns: 1fr;
    padding: 10px;
  }

  .auth-intro,
  .auth-card-wrap {
    padding: 16px;
  }

  .auth-intro h1 {
    font-size: clamp(26px, 8vw, 36px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-view,
  .auth-intro,
  .auth-card-wrap {
    transition: none;
  }
}
</style>
