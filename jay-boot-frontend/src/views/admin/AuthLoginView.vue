<template>
  <section class="auth-page">
    <div class="auth-page__intro">
      <h1>欢迎使用 Jay Boot</h1>
      <p>登录后可进入控制台，管理租户、权限、计费与 API Key。</p>
    </div>

    <a-card class="auth-card" :bordered="false">
      <template #title>账号登录</template>
      <a-form layout="vertical" :model="formState" :rules="rules" @finish="onFinish">
        <a-form-item label="账号" name="account">
          <a-input
            v-model:value="formState.account"
            placeholder="请输入用户名或邮箱"
            autocomplete="username"
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
          <a-button type="primary" html-type="submit" block :loading="submitting">登录</a-button>
        </a-form-item>
      </a-form>
      <div class="auth-card__footer">
        还没有账号？
        <RouterLink :to="registerLink">立即注册</RouterLink>
      </div>
      <a-alert
        class="auth-card__tip"
        type="info"
        show-icon
        message="Mock 演示账号"
        description="用户名：admin 或邮箱：admin@jayboot.local，密码：admin123"
      />
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/admin/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formState = reactive({
  account: '',
  password: '',
})

const submitting = ref(false)

const rules = {
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
}

const registerLink = computed(() => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  if (redirect) {
    return { path: '/admin/auth/register', query: { redirect } }
  }
  return { path: '/admin/auth/register' }
})

const onFinish = async () => {
  submitting.value = true
  try {
    await authStore.login({
      account: formState.account.trim(),
      password: formState.password,
    })
    message.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/dashboard'
    await router.replace(redirect)
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
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(320px, 1fr) minmax(320px, 460px);
  gap: 24px;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #f5f3ff 0%, #eef2ff 45%, #f8fafc 100%);
}

.auth-page__intro {
  max-width: 620px;
}

.auth-page__intro h1 {
  margin: 0;
  color: #1e1b4b;
  font-size: clamp(30px, 5vw, 48px);
  line-height: 1.2;
}

.auth-page__intro p {
  margin: 16px 0 0;
  color: #475569;
  font-size: 16px;
  line-height: 1.7;
}

.auth-card {
  border-radius: 16px;
  border: 1px solid #d9def0;
}

.auth-card__footer {
  margin-top: 8px;
  text-align: center;
  color: #475569;
}

.auth-card__tip {
  margin-top: 16px;
}

@media (max-width: 1024px) {
  .auth-page {
    grid-template-columns: 1fr;
    padding: 20px;
  }

  .auth-page__intro {
    max-width: none;
  }
}
</style>
