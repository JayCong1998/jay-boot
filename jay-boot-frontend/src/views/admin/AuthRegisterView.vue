<template>
  <section class="auth-page">
    <div class="auth-page__intro">
      <h1>创建你的 Jay Boot 工作空间</h1>
      <p>完成注册后会自动登录并进入控制台，你可以立即开始配置权限与计费能力。</p>
    </div>

    <a-card class="auth-card" :bordered="false">
      <template #title>账号注册</template>
      <a-form layout="vertical" :model="formState" :rules="rules" @finish="onFinish">
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" placeholder="请输入常用邮箱" autocomplete="email" />
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
          <a-button type="primary" html-type="submit" block :loading="submitting">注册并登录</a-button>
        </a-form-item>
      </a-form>
      <div class="auth-card__footer">
        已有账号？
        <RouterLink :to="loginLink">去登录</RouterLink>
      </div>
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
  email: '',
  password: '',
  confirmPassword: '',
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
  confirmPassword: [
    {
      required: true,
      message: '请再次输入密码',
      trigger: 'blur',
    },
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

const loginLink = computed(() => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  if (redirect) {
    return { path: '/admin/auth/login', query: { redirect } }
  }
  return { path: '/admin/auth/login' }
})

const onFinish = async () => {
  submitting.value = true
  try {
    await authStore.register({
      email: formState.email.trim(),
      password: formState.password,
    })
    message.success('注册成功，已自动登录')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/dashboard'
    await router.replace(redirect)
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
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(320px, 1fr) minmax(320px, 500px);
  gap: 24px;
  align-items: center;
  padding: 32px;
  background: linear-gradient(140deg, #eef2ff 0%, #f5f3ff 45%, #f8fafc 100%);
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
