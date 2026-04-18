<template>
  <section class="auth-view">
    <article class="auth-intro">
      <p class="auth-intro__tag">New Creator Onboarding</p>
      <h1>创建你的 JayBoot 创作空间</h1>
      <p class="auth-intro__desc">
        完成注册后将自动进入新手引导，快速建立你的内容生产与交付工作流。
      </p>
      <ul class="auth-intro__list">
        <li>按平台拆分创作模板</li>
        <li>沉淀团队资产与最佳实践</li>
        <li>按阶段跟踪转化效果</li>
      </ul>
    </article>

    <article class="auth-card-wrap">
      <a-card class="auth-card" :bordered="false">
        <template #title>免费注册</template>

        <a-alert
          class="auth-card__mode"
          type="info"
          show-icon
          :message="`当前数据源：${apiModeText}`"
          description="可通过 VITE_USER_API_MODE 切换 mock 与真实接口"
        />

        <a-form layout="vertical" :model="formState" :rules="rules" @finish="onFinish">
          <a-form-item label="用户名" name="username">
            <a-input
              v-model:value="formState.username"
              placeholder="请输入用户名（3-24 位）"
              autocomplete="username"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="邮箱" name="email">
            <a-input
              v-model:value="formState.email"
              placeholder="请输入常用邮箱"
              autocomplete="email"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="密码" name="password">
            <a-input-password
              v-model:value="formState.password"
              placeholder="请输入密码（6-10 位）"
              autocomplete="new-password"
            />
          </a-form-item>

          <a-form-item label="确认密码" name="confirmPassword">
            <a-input-password
              v-model:value="formState.confirmPassword"
              placeholder="请再次输入密码"
              autocomplete="new-password"
            />
          </a-form-item>

          <a-form-item>
            <a-button type="primary" html-type="submit" block :loading="submitting">注册并进入新手引导</a-button>
          </a-form-item>
        </a-form>

        <div class="auth-card__footer">
          已有账号？
          <RouterLink :to="loginLink">去登录</RouterLink>
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
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const submitting = ref(false)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能少于 3 位', trigger: 'blur' },
    { max: 24, message: '用户名长度不能超过 24 位', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
    { max: 10, message: '密码长度不能超过 10 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      trigger: 'blur',
      validator: (_rule: unknown, value: string) => {
        if (!value) {
          return Promise.reject(new Error('请再次输入密码'))
        }
        if (value !== formState.password) {
          return Promise.reject(new Error('两次输入的密码不一致'))
        }
        return Promise.resolve()
      },
    },
  ],
}

const apiModeText = computed(() => (apiConfig.mode === 'mock' ? 'Mock API' : '真实 API'))

const loginLink = computed(() => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  if (redirect) {
    return { path: '/user/auth/login', query: { redirect } }
  }
  return { path: '/user/auth/login' }
})

const onFinish = async () => {
  submitting.value = true
  try {
    await userAuthStore.register({
      username: formState.username.trim(),
      email: formState.email.trim(),
      password: formState.password,
    })
    message.success('注册成功')
    await router.replace('/user/onboarding')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    } else {
      message.error('注册失败，请稍后重试')
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
  width: min(100%, 1480px);
  margin: 0 auto;
  min-height: 100vh;
  padding: clamp(20px, 2vw, 36px);
  display: flex;
  align-items: center;
  gap: clamp(16px, 1.8vw, 24px);
  background:
    radial-gradient(circle at 6% 12%, var(--user-gradient-a), transparent 36%),
    radial-gradient(circle at 94% 88%, var(--user-gradient-b), transparent 35%);
}

.auth-intro {
  background: var(--auth-panel);
  border: 1px solid var(--auth-border);
  border-radius: 16px;
}

.auth-intro {
  flex: 1 1 0;
  min-width: 0;
  padding: clamp(22px, 2vw, 32px);
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
  font-size: clamp(30px, 2.4vw, 44px);
  line-height: 1.15;
  letter-spacing: -0.02em;
  color: var(--auth-text-main);
}

.auth-intro__desc {
  margin: 14px 0 0;
  max-width: 72ch;
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
  flex: 0 0 clamp(360px, 30vw, 520px);
  width: clamp(360px, 30vw, 520px);
}

.auth-card {
  width: 100%;
  border: 1px solid var(--auth-border);
  background: var(--auth-panel);
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

@media (max-width: 1200px) {
  .auth-card-wrap {
    flex-basis: clamp(320px, 38vw, 460px);
    width: clamp(320px, 38vw, 460px);
  }
}

@media (max-width: 980px) {
  .auth-view {
    min-height: 0;
    display: block;
    padding: 20px;
  }

  .auth-intro {
    margin-bottom: 12px;
    padding: 16px;
  }

  .auth-intro h1 {
    font-size: clamp(26px, 8vw, 36px);
  }

  .auth-card-wrap {
    width: 100%;
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
