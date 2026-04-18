<template>
  <section class="auth-view">
    <article class="auth-visual">
      <div class="auth-visual__content">
        <p class="auth-visual__tag">Creator Workspace</p>
        <h1>欢迎回来，继续你的创作增长计划</h1>
        <p class="auth-visual__desc">
        登录后可进入用户工作台，继续管理内容生产、交付记录和订阅计划。
        </p>
        <ul class="auth-visual__list">
          <li>多场景文案工作流</li>
          <li>可追踪的交付历史</li>
          <li>团队协作与权限共享</li>
        </ul>
      </div>
      <div class="auth-visual__image-wrap">
        <img class="auth-visual__image" :src="heroImage" alt="创作空间视觉插画" />
      </div>
    </article>

    <article class="auth-form-panel">
      <a-card class="auth-card" :bordered="false">
        <template #title>登录你的账号</template>

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
          <span>还没有账号？</span>
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
import heroImage from '../../../assets/hero.png'
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
@import './auth-split-layout.css';
</style>
