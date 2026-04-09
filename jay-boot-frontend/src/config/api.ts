export type UserApiMode = 'mock' | 'real'

const normalizeMode = (mode: unknown): UserApiMode => {
  if (typeof mode !== 'string') {
    return 'mock'
  }
  return mode.trim().toLowerCase() === 'real' ? 'real' : 'mock'
}

const normalizeBaseUrl = (baseUrl: unknown): string => {
  if (typeof baseUrl !== 'string') {
    return ''
  }
  return baseUrl.trim().replace(/\/+$/, '')
}

const normalizeDelay = (delay: unknown): number => {
  const parsed = Number(delay)
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 180
  }
  return parsed
}

export const userApiConfig = {
  mode: normalizeMode(import.meta.env.VITE_USER_API_MODE),
  baseUrl: normalizeBaseUrl(import.meta.env.VITE_USER_API_BASE_URL),
  mockDelayMs: normalizeDelay(import.meta.env.VITE_USER_MOCK_DELAY_MS),
}
