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
import { getToken, setToken, clearToken } from '../tokenStore'

const USER_INFO_STORAGE_KEY = 'jay_boot_auth_user'

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

const writeStorageUser = (user: UserAuthUser) => {
  localStorage.setItem(USER_INFO_STORAGE_KEY, JSON.stringify(user))
}

const clearStorageUser = () => {
  localStorage.removeItem(USER_INFO_STORAGE_KEY)
}

export const hasUserStorageSession = () => Boolean(getToken())

export const useUserAuthStore = defineStore('user-auth', () => {
  const token = ref('')
  const user = ref<UserAuthUser | null>(null)
  const hydrated = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))
  const displayName = computed(() => user.value?.username || '未登录用户')

  const setSession = (session: UserAuthSession) => {
    token.value = session.token
    user.value = session.user
    setToken(session.token)
    writeStorageUser(session.user)
  }

  const clearSession = () => {
    token.value = ''
    user.value = null
    clearToken()
    clearStorageUser()
  }

  const hydrate = async () => {
    if (hydrated.value) {
      return
    }
    hydrated.value = true
    token.value = getToken()
    user.value = readStorageUser()

    if (!token.value || user.value) {
      return
    }

    try {
      const profile = await userMeApi()
      user.value = profile
      writeStorageUser(profile)
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
    clearSession()
    try {
      await userLogoutApi()
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
