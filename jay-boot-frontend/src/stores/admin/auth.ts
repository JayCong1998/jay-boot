import { defineStore } from 'pinia'
import { loginApi, logoutApi, meApi, registerApi, type AuthSession, type AuthUser, type LoginPayload, type RegisterPayload } from '../../api/admin/AuthApi'
import { getToken, setToken, clearToken } from '../tokenStore'

const USER_STORAGE_KEY = 'jay_boot_auth_user'

const readStorageUser = (): AuthUser | null => {
  const raw = localStorage.getItem(USER_STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

const writeStorageUser = (user: AuthUser) => {
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))
}

const clearStorageUser = () => {
  localStorage.removeItem(USER_STORAGE_KEY)
}

export const hasStorageSession = () => Boolean(getToken())

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null as AuthUser | null,
    hydrated: false,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => {
      const username = state.user?.username?.trim()
      if (username) {
        return username
      }
      const email = state.user?.email?.trim()
      if (!email) {
        return '未登录用户'
      }
      const [prefix] = email.split('@')
      return prefix || email
    },
  },
  actions: {
    setSession(session: AuthSession) {
      this.token = session.token
      this.user = session.user
      setToken(session.token)
      writeStorageUser(session.user)
    },
    clearSession() {
      this.token = ''
      this.user = null
      clearToken()
      clearStorageUser()
    },
    async hydrate() {
      if (this.hydrated) {
        return
      }
      this.hydrated = true
      this.token = getToken()
      this.user = readStorageUser()

      if (!this.token) {
        return
      }
      if (this.user) {
        return
      }

      try {
        const user = await meApi()
        this.user = user
        writeStorageUser(user)
      } catch {
        this.clearSession()
      }
    },
    async login(payload: LoginPayload) {
      const session = await loginApi(payload)
      this.setSession(session)
      return session
    },
    async register(payload: RegisterPayload) {
      const session = await registerApi(payload)
      this.setSession(session)
      return session
    },
    async logout() {
      this.clearSession()
      try {
        await logoutApi()
      } catch {
        // Mock 场景下退出失败不阻断前端登出流程
      }
    },
  },
})
