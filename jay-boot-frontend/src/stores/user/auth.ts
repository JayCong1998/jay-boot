import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  userLoginApi,
  userLogoutApi,
  userMeApi,
  userRegisterApi,
  type UserAuthSession,
  type UserAuthUser,
  type UserLoginPayload,
  type UserRegisterPayload,
} from '../../api/user/AuthApi'

const USER_TOKEN_STORAGE_KEY = 'jay_boot_user_auth_token'
const USER_INFO_STORAGE_KEY = 'jay_boot_user_auth_user'

const readStorageToken = (): string => localStorage.getItem(USER_TOKEN_STORAGE_KEY) ?? ''

const readStorageUser = (): UserAuthUser | null => {
  const raw = localStorage.getItem(USER_INFO_STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as UserAuthUser
  } catch {
    return null
  }
}

const writeStorageSession = (session: UserAuthSession) => {
  localStorage.setItem(USER_TOKEN_STORAGE_KEY, session.token)
  localStorage.setItem(USER_INFO_STORAGE_KEY, JSON.stringify(session.user))
}

const clearStorageSession = () => {
  localStorage.removeItem(USER_TOKEN_STORAGE_KEY)
  localStorage.removeItem(USER_INFO_STORAGE_KEY)
}

export const useUserAuthStore = defineStore('user-auth', () => {
  const token = ref('')
  const user = ref<UserAuthUser | null>(null)
  const hydrated = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))
  const displayName = computed(() => user.value?.username || '未登录用户')

  const setSession = (session: UserAuthSession) => {
    token.value = session.token
    user.value = session.user
    writeStorageSession(session)
  }

  const clearSession = () => {
    token.value = ''
    user.value = null
    clearStorageSession()
  }

  const hydrate = async () => {
    if (hydrated.value) {
      return
    }
    hydrated.value = true
    token.value = readStorageToken()
    user.value = readStorageUser()

    if (!token.value || user.value) {
      return
    }

    try {
      const profile = await userMeApi(token.value)
      user.value = profile
      localStorage.setItem(USER_INFO_STORAGE_KEY, JSON.stringify(profile))
    } catch {
      clearSession()
    }
  }

  const login = async (payload: UserLoginPayload) => {
    const session = await userLoginApi(payload)
    setSession(session)
    return session
  }

  const register = async (payload: UserRegisterPayload) => {
    const session = await userRegisterApi(payload)
    setSession(session)
    return session
  }

  const logout = async () => {
    const currentToken = token.value
    clearSession()
    if (!currentToken) {
      return
    }
    try {
      await userLogoutApi(currentToken)
    } catch {
      // Mock 场景下失败不阻断前端登出流程
    }
  }

  return {
    token,
    user,
    hydrated,
    isLoggedIn,
    displayName,
    setSession,
    clearSession,
    hydrate,
    login,
    register,
    logout,
  }
})
