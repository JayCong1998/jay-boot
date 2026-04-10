export type UserApiMode = 'mock' | 'real'
export type AdminAuthApiMode = 'mock' | 'real'

const normalizeMode = <TMode extends 'mock' | 'real'>(mode: unknown, fallback: TMode): TMode => {
  if (typeof mode !== 'string') {
    return fallback
  }
  return (mode.trim().toLowerCase() === 'real' ? 'real' : 'mock') as TMode
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
  mode: normalizeMode<UserApiMode>(import.meta.env.VITE_USER_API_MODE, 'mock'),
  baseUrl: normalizeBaseUrl(import.meta.env.VITE_USER_API_BASE_URL),
  mockDelayMs: normalizeDelay(import.meta.env.VITE_USER_MOCK_DELAY_MS),
}

export const adminAuthApiConfig = {
  mode: normalizeMode<AdminAuthApiMode>(import.meta.env.VITE_ADMIN_AUTH_API_MODE, 'real'),
  baseUrl: normalizeBaseUrl(import.meta.env.VITE_ADMIN_AUTH_API_BASE_URL),
  mockDelayMs: normalizeDelay(import.meta.env.VITE_ADMIN_AUTH_MOCK_DELAY_MS),
}
