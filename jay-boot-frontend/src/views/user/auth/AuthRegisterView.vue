<template>
  <section class="auth-view">
    <article class="auth-visual">
      <div class="auth-visual__content">
        <p class="auth-visual__tag">New Creator Onboarding</p>
        <h1>创建你的 JayBoot 创作空间</h1>
        <p class="auth-visual__desc">
          完成注册后将自动进入新手引导，快速建立你的内容生产与交付工作流。
        </p>
        <ul class="auth-visual__list">
          <li>按平台拆分创作模板</li>
          <li>沉淀团队资产与最佳实践</li>
          <li>按阶段跟踪转化效果</li>
        </ul>
      </div>
      <div class="auth-visual__image-wrap">
        <img class="auth-visual__image" :src="heroImage" alt="创作空间视觉插画" />
      </div>
    </article>

    <article class="auth-form-panel">
      <a-card class="auth-card" :bordered="false">
        <template #title>创建免费账号</template>

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
          <span>已有账号？</span>
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
import heroImage from '../../../assets/hero.png'
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
@import './auth-split-layout.css';
</style>
