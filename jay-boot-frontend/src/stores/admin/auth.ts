import { defineStore } from 'pinia'
import { loginApi, logoutApi, meApi, registerApi, type AuthSession, type AuthUser, type LoginPayload, type RegisterPayload } from '../../api/admin/AuthApi'

const TOKEN_STORAGE_KEY = 'jay_boot_auth_token'
const USER_STORAGE_KEY = 'jay_boot_auth_user'

const readStorageToken = (): string => localStorage.getItem(TOKEN_STORAGE_KEY) ?? ''

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

const writeStorageSession = (session: AuthSession) => {
  localStorage.setItem(TOKEN_STORAGE_KEY, session.token)
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(session.user))
}

const clearStorageSession = () => {
  localStorage.removeItem(TOKEN_STORAGE_KEY)
  localStorage.removeItem(USER_STORAGE_KEY)
}

export const hasStorageSession = () => Boolean(readStorageToken())

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null as AuthUser | null,
    hydrated: false,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => state.user?.username || '未登录用户',
  },
  actions: {
    setSession(session: AuthSession) {
      this.token = session.token
      this.user = session.user
      writeStorageSession(session)
    },
    clearSession() {
      this.token = ''
      this.user = null
      clearStorageSession()
    },
    async hydrate() {
      if (this.hydrated) {
        return
      }
      this.hydrated = true
      this.token = readStorageToken()
      this.user = readStorageUser()

      if (!this.token) {
        return
      }
      if (this.user) {
        return
      }

      try {
        const user = await meApi(this.token)
        this.user = user
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))
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
      const currentToken = this.token
      this.clearSession()
      if (!currentToken) {
        return
      }
      try {
        await logoutApi(currentToken)
      } catch {
        // Mock 场景下退出失败不阻断前端登出流程
      }
    },
  },
})

