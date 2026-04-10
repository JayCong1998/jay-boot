export type MockMethod = 'GET' | 'POST'

export interface MockSuccessResponse<T = unknown> {
  error: 0
  body: T
  message: string
  success: true
}

export interface MockErrorResponse {
  error: number
  body: null
  message: string
  success: false
}

export type MockResponse<T = unknown> = MockSuccessResponse<T> | MockErrorResponse
type MockHandler = (payload?: Record<string, unknown>) => MockResponse | Promise<MockResponse>

interface MockUserRecord {
  id: string
  username: string
  email: string
  password: string
  createdAt: string
}

interface PublicUserProfile {
  id: string
  username: string
  email: string
  createdAt: string
}

interface AuthResultBody {
  token: string
  user: PublicUserProfile
}

type DashboardRange = '24h' | '7d' | '30d'
type DashboardTrendDirection = 'up' | 'down' | 'flat'
type DashboardAlertLevel = 'info' | 'warn' | 'error'
type DashboardEventStatus = 'success' | 'retrying' | 'failed'

interface DashboardOverviewBody {
  range: DashboardRange
  updatedAt: string
  kpis: Array<{ key: string; title: string; valueText: string; trendText: string; trendDirection: DashboardTrendDirection }>
  alerts: Array<{ id: string; module: string; level: DashboardAlertLevel; message: string; time: string }>
  events: Array<{ id: string; time: string; module: string; event: string; traceId: string; status: DashboardEventStatus }>
}

type RbacPermissionDecision = 'allow' | 'approval' | 'deny'
type RbacChangeType = 'create' | 'update' | 'delete'

interface RbacPermissionMatrixRow {
  permissionCode: string
  super_admin: RbacPermissionDecision
  admin: RbacPermissionDecision
  user: RbacPermissionDecision
}

interface RbacRecentChangeItem {
  id: string
  time: string
  title: string
  detail: string
  type: RbacChangeType
}

interface RbacOverviewBody {
  roleTemplates: Array<{ id: string; code: string; description: string }>
  permissionMatrix: RbacPermissionMatrixRow[]
  policyNotes: Array<{ id: string; content: string }>
  recentChanges: RbacRecentChangeItem[]
  updatedAt: string
}

type BillingPlanCode = 'FREE' | 'PRO_MONTHLY' | 'TEAM_MONTHLY'
type BillingSubscriptionStatus = 'trialing' | 'active' | 'past_due' | 'canceled'
type BillingCallbackStatus = 'healthy' | 'degraded'
type BillingOrderStatus = 'paid' | 'pending' | 'failed' | 'refunded'
type BillingInvoiceStatus = 'generated' | 'sent' | 'none'

interface BillingOrderItem {
  id: string
  orderNo: string
  period: string
  amountText: string
  channel: string
  status: BillingOrderStatus
  invoiceStatus: BillingInvoiceStatus
}

interface BillingOverviewBody {
  plans: Array<{
    code: BillingPlanCode
    name: string
    priceText: string
    billingCycleText: string
    featureList: string[]
    contactSales: boolean
    highlighted: boolean
  }>
  currentSubscription: {
    planCode: BillingPlanCode
    planName: string
    status: BillingSubscriptionStatus
    renewAt: string
    trialEndAt: string
    amountText: string
  }
  statusStages: Array<{ key: BillingSubscriptionStatus; title: string; description: string }>
  callbackHealth: Array<{ id: string; provider: string; successRate: number; status: BillingCallbackStatus; lastCheckedAt: string }>
  orders: BillingOrderItem[]
  updatedAt: string
}

type ApiKeyStatus = 'active' | 'pending_disable' | 'disabled'

interface ApiKeyItem {
  id: string
  name: string
  keyPrefix: string
  scope: string
  limitPolicy: string
  lastUsedText: string
  status: ApiKeyStatus
}

interface ApiKeyOverviewBody {
  keys: ApiKeyItem[]
  scopeOptions: string[]
  limitOptions: string[]
  quotaReminders: Array<{ id: string; label: string; percent: number }>
  securityTips: string[]
  latestSecret: string
  updatedAt: string
}

type AiGatewayRouteStatus = 'available' | 'high_load' | 'degraded'
type AiGatewayBillingUnit = 'token' | 'request'
type AiGatewayTestStatus = 'success' | 'retrying' | 'failed'

interface AiGatewayRoutePolicyItem {
  id: string
  endpoint: string
  defaultModel: string
  timeoutPolicy: string
  billingUnit: AiGatewayBillingUnit
  status: AiGatewayRouteStatus
}

interface AiGatewayOverviewBody {
  activeProviderId: string
  providers: Array<{ id: string; name: string; status: 'online' | 'warning' | 'offline'; detail: string }>
  routePolicies: AiGatewayRoutePolicyItem[]
  costTrend: Array<{ id: string; label: string; percent: number; costText: string }>
  fallbackSteps: Array<{ id: string; title: string; detail: string }>
  latestTestResult: { traceId: string; endpoint: string; status: AiGatewayTestStatus; message: string; time: string } | null
  updatedAt: string
}

type UsageJobStatus = 'success' | 'retrying' | 'failed'
type UsageNotifyStatus = 'normal' | 'timeout'

interface UsageJobItem {
  id: string
  jobId: string
  type: string
  status: UsageJobStatus
  retryCount: number
  nextRetryAt: string
}

interface UsageOverviewBody {
  kpis: Array<{ key: string; title: string; valueText: string; hintText: string; tone: 'normal' | 'warning' }>
  jobs: UsageJobItem[]
  notifyChannels: Array<{ id: string; name: string; status: UsageNotifyStatus; detail: string }>
  metricBars: Array<{ id: string; label: string; percent: number; valueText: string }>
  adviceItems: Array<{ id: string; content: string }>
  latestReportId: string
  updatedAt: string
}

const USER_STORAGE_KEY = 'jay_boot_mock_auth_users'
const mockApiRegistry = new Map<string, MockHandler>()
let initialized = false

let rbacMatrix: RbacPermissionMatrixRow[] = [
  { permissionCode: 'billing.subscription.update', super_admin: 'allow', admin: 'approval', user: 'deny' },
  { permissionCode: 'apikey.create', super_admin: 'allow', admin: 'allow', user: 'deny' },
  { permissionCode: 'member.invite', super_admin: 'allow', admin: 'allow', user: 'deny' },
]
let rbacChanges: RbacRecentChangeItem[] = [
  { id: 'change_1', time: '10:00', title: 'matrix initialized', detail: 'default matrix loaded', type: 'create' },
]

let currentPlanCode: BillingPlanCode = 'PRO_MONTHLY'
let currentStatus: BillingSubscriptionStatus = 'active'
let billingOrders: BillingOrderItem[] = [
  {
    id: 'order_1',
    orderNo: 'ord_20260410001',
    period: '2026-04',
    amountText: '299',
    channel: 'Stripe',
    status: 'paid',
    invoiceStatus: 'generated',
  },
]

let apiKeys: ApiKeyItem[] = [
  {
    id: 'key_1',
    name: 'server-prod',
    keyPrefix: 'sk_live_abcd...',
    scope: '/ai/*',
    limitPolicy: '120 QPS',
    lastUsedText: 'just now',
    status: 'active',
  },
]
let apiKeySeq = 2

let aiProviderId = 'openai'
let aiRoutePolicies: AiGatewayRoutePolicyItem[] = [
  {
    id: 'route_chat',
    endpoint: '/ai/chat',
    defaultModel: 'gpt-4.1-mini',
    timeoutPolicy: '8s + 2 retries',
    billingUnit: 'token',
    status: 'available',
  },
]
let aiLatestTest: AiGatewayOverviewBody['latestTestResult'] = null

let usageJobs: UsageJobItem[] = [
  { id: 'job_1', jobId: 'job_001', type: 'REPORT_GENERATE', status: 'success', retryCount: 0, nextRetryAt: '-' },
  { id: 'job_2', jobId: 'job_002', type: 'EMAIL_SEND', status: 'retrying', retryCount: 1, nextRetryAt: '2026-04-10 17:00' },
]
let usageNotifyChannels: UsageOverviewBody['notifyChannels'] = [
  { id: 'ch_1', name: 'in-app', status: 'normal', detail: 'ok' },
  { id: 'ch_2', name: 'webhook', status: 'timeout', detail: '2 timeouts' },
]
let latestReportId = 'report_20260410_01'

const buildRegistryKey = (method: MockMethod, url: string) => `${method.toUpperCase()} ${url}`
const nowIso = () => new Date().toISOString()

const readUsers = (): MockUserRecord[] => {
  const raw = localStorage.getItem(USER_STORAGE_KEY)
  if (!raw) {
    return []
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return []
    }
    return parsed.map((item) => ({
      ...(item as Record<string, unknown>),
      id: String((item as Record<string, unknown>).id ?? ''),
    })) as MockUserRecord[]
  } catch {
    return []
  }
}

const writeUsers = (users: MockUserRecord[]) => {
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(users))
}

const ensureDefaultUsers = () => {
  const users = readUsers()
  if (users.length > 0) {
    return
  }
  writeUsers([
    { id: '1', username: 'admin', email: 'admin@jayboot.local', password: 'admin123', createdAt: nowIso() },
  ])
}

const issueToken = (userId: string) => `mock-token-${userId}-${Date.now()}`

const parseUserIdFromToken = (token: string): string | null => {
  if (!token.startsWith('mock-token-')) {
    return null
  }
  const parts = token.split('-')
  if (parts.length < 4) {
    return null
  }
  const id = String(parts[2] ?? '').trim()
  if (!id) {
    return null
  }
  return id
}

const toPublicUser = (user: MockUserRecord): PublicUserProfile => ({
  id: user.id,
  username: user.username,
  email: user.email,
  createdAt: user.createdAt,
})

export const createSuccessResponse = <T>(body: T, message = 'ok'): MockSuccessResponse<T> => ({
  error: 0,
  body,
  message,
  success: true,
})

export const createErrorResponse = (message: string, error = 1): MockErrorResponse => ({
  error,
  body: null,
  message,
  success: false,
})

const registerMockApi = (method: MockMethod, url: string, handler: MockHandler) => {
  mockApiRegistry.set(buildRegistryKey(method, url), handler)
}

const planName = (code: BillingPlanCode) => (code === 'FREE' ? 'Free' : code === 'PRO_MONTHLY' ? 'Pro' : 'Team')
const planAmount = (code: BillingPlanCode) => (code === 'FREE' ? '0' : code === 'PRO_MONTHLY' ? '299' : '899')

const billingOverview = (): BillingOverviewBody => ({
  plans: [
    { code: 'FREE', name: 'Free', priceText: '0', billingCycleText: 'monthly', featureList: ['basic'], contactSales: false, highlighted: false },
    { code: 'PRO_MONTHLY', name: 'Pro', priceText: '299', billingCycleText: 'monthly', featureList: ['pro'], contactSales: false, highlighted: true },
    { code: 'TEAM_MONTHLY', name: 'Team', priceText: '899', billingCycleText: 'monthly', featureList: ['team'], contactSales: true, highlighted: false },
  ],
  currentSubscription: {
    planCode: currentPlanCode,
    planName: planName(currentPlanCode),
    status: currentStatus,
    renewAt: nowIso(),
    trialEndAt: nowIso(),
    amountText: planAmount(currentPlanCode),
  },
  statusStages: [
    { key: 'trialing', title: 'trialing', description: 'trial period' },
    { key: 'active', title: 'active', description: 'active subscription' },
    { key: 'past_due', title: 'past_due', description: 'payment required' },
    { key: 'canceled', title: 'canceled', description: 'subscription canceled' },
  ],
  callbackHealth: [
    { id: 'cb_1', provider: 'Stripe', successRate: 99.2, status: 'healthy', lastCheckedAt: nowIso() },
    { id: 'cb_2', provider: 'Alipay', successRate: 97.1, status: 'degraded', lastCheckedAt: nowIso() },
  ],
  orders: billingOrders,
  updatedAt: nowIso(),
})

const apiKeyOverview = (latestSecret = ''): ApiKeyOverviewBody => ({
  keys: apiKeys,
  scopeOptions: ['/ai/chat', '/ai/*', '/ai/image'],
  limitOptions: ['20 QPS', '60 QPS', '120 QPS'],
  quotaReminders: [
    { id: 'q_1', label: 'daily requests', percent: 42 },
    { id: 'q_2', label: 'monthly tokens', percent: 58 },
  ],
  securityTips: ['rotate on leak', 'split by env', 'sign high-risk calls'],
  latestSecret,
  updatedAt: nowIso(),
})

const aiGatewayOverview = (): AiGatewayOverviewBody => ({
  activeProviderId: aiProviderId,
  providers: [
    { id: 'openai', name: 'OpenAI', status: 'online', detail: 'online' },
    { id: 'anthropic', name: 'Anthropic', status: 'online', detail: 'online' },
    { id: 'local', name: 'Local', status: 'warning', detail: 'high load' },
  ],
  routePolicies: aiRoutePolicies,
  costTrend: [
    { id: 'cost_1', label: 'OpenAI', percent: 54, costText: '1280' },
    { id: 'cost_2', label: 'Anthropic', percent: 32, costText: '760' },
    { id: 'cost_3', label: 'Local', percent: 14, costText: '320' },
  ],
  fallbackSteps: [
    { id: 'fb_1', title: 'primary failed', detail: 'retry once' },
    { id: 'fb_2', title: 'fallback route', detail: 'switch provider' },
  ],
  latestTestResult: aiLatestTest,
  updatedAt: nowIso(),
})

const usageOverview = (): UsageOverviewBody => ({
  kpis: [
    { key: 'req', title: 'requests', valueText: '12480', hintText: '+4.1%', tone: 'normal' },
    { key: 'fail', title: 'failed jobs', valueText: '3', hintText: 'check retries', tone: 'warning' },
  ],
  jobs: usageJobs,
  notifyChannels: usageNotifyChannels,
  metricBars: [
    { id: 'm_1', label: 'conversion', percent: 31, valueText: '31%' },
    { id: 'm_2', label: 'renewal', percent: 82, valueText: '82%' },
  ],
  adviceItems: [
    { id: 'a_1', content: 'open ticket when failures exceed threshold' },
    { id: 'a_2', content: 'raise concurrency for peak period APIs' },
  ],
  latestReportId,
  updatedAt: nowIso(),
})

const dashboardOverview = (range: DashboardRange): DashboardOverviewBody => ({
  range,
  updatedAt: nowIso(),
  kpis: [
    { key: 'req', title: 'requests', valueText: range === '24h' ? '12480' : range === '7d' ? '78200' : '332000', trendText: '+4.1%', trendDirection: 'up' },
    { key: 'token', title: 'token usage', valueText: range === '24h' ? '1.8M' : range === '7d' ? '9.7M' : '41.2M', trendText: '+2.3%', trendDirection: 'up' },
    { key: 'err', title: 'error rate', valueText: '0.27%', trendText: '-0.04%', trendDirection: 'down' },
    { key: 'lat', title: 'p95 latency', valueText: '420ms', trendText: 'flat', trendDirection: 'flat' },
  ],
  alerts: [
    { id: 'al_1', module: 'AI Gateway', level: 'warn', message: 'image endpoint high load', time: '10:42' },
    { id: 'al_2', module: 'Billing', level: 'info', message: 'callback recovered', time: '10:18' },
  ],
  events: [
    { id: 'ev_1', time: '10:45', module: 'Auth', event: 'admin login success', traceId: 'evt_1001', status: 'success' },
    { id: 'ev_2', time: '10:40', module: 'Usage', event: 'report retry', traceId: 'evt_1002', status: 'retrying' },
  ],
})

const initAuthApis = () => {
  registerMockApi('POST', '/api/admin/auth/register', (payload) => {
    const username = String(payload?.username ?? '').trim()
    const email = String(payload?.email ?? '').trim().toLowerCase()
    const password = String(payload?.password ?? '')
    if (!username || !email || !password) {
      return createErrorResponse('invalid register payload')
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      return createErrorResponse('invalid email')
    }
    if (password.length < 6 || password.length > 10) {
      return createErrorResponse('password length must be 6-10')
    }

    const users = readUsers()
    if (users.some((u) => u.email === email || u.username === username)) {
      return createErrorResponse('username or email exists')
    }
    const nextId = `${Date.now()}${Math.floor(Math.random() * 1000)}`
    const user: MockUserRecord = { id: nextId, username, email, password, createdAt: nowIso() }
    writeUsers([...users, user])
    const result: AuthResultBody = { token: issueToken(user.id), user: toPublicUser(user) }
    return createSuccessResponse(result, 'register success')
  })

  registerMockApi('POST', '/api/admin/auth/login', (payload) => {
    const email = String(payload?.email ?? '').trim().toLowerCase()
    const password = String(payload?.password ?? '')
    if (!email || !password) {
      return createErrorResponse('email and password required')
    }
    if (password.length < 6 || password.length > 10) {
      return createErrorResponse('password length must be 6-10')
    }
    const user = readUsers().find((u) => u.email === email)
    if (!user || user.password !== password) {
      return createErrorResponse('invalid credentials')
    }
    const result: AuthResultBody = { token: issueToken(user.id), user: toPublicUser(user) }
    return createSuccessResponse(result, 'login success')
  })

  registerMockApi('GET', '/api/admin/auth/me', (payload) => {
    const token = String(payload?.token ?? '').trim()
    const userId = parseUserIdFromToken(token)
    if (!userId) {
      return createErrorResponse('invalid token', 401)
    }
    const user = readUsers().find((u) => u.id === userId)
    if (!user) {
      return createErrorResponse('user not found', 404)
    }
    return createSuccessResponse(toPublicUser(user), 'me success')
  })

  registerMockApi('POST', '/api/admin/auth/logout', () => createSuccessResponse(null, 'logout success'))
}

const initDashboardApis = () => {
  registerMockApi('GET', '/api/admin/dashboard/overview', (payload) => {
    const rawRange = payload?.range
    const range: DashboardRange = rawRange === '24h' || rawRange === '30d' ? rawRange : '7d'
    return createSuccessResponse(dashboardOverview(range), 'dashboard success')
  })
}

const initRbacApis = () => {
  const roleTemplates = [
    { id: 'role_1', code: 'super_admin', description: 'super admin permissions' },
    { id: 'role_2', code: 'admin', description: 'admin permissions' },
    { id: 'role_3', code: 'user', description: 'user permissions' },
  ]
  const policyNotes = [
    { id: 'note_1', content: 'all changes are audited' },
    { id: 'note_2', content: 'approval required for risky actions' },
  ]

  const overview = (): RbacOverviewBody => ({
    roleTemplates,
    permissionMatrix: rbacMatrix,
    policyNotes,
    recentChanges: rbacChanges,
    updatedAt: nowIso(),
  })

  registerMockApi('GET', '/api/admin/rbac/overview', () => createSuccessResponse(overview(), 'rbac success'))

  registerMockApi('POST', '/api/admin/rbac/permission-matrix/save', (payload) => {
    const matrix = payload?.permissionMatrix
    if (!Array.isArray(matrix) || matrix.length === 0) {
      return createErrorResponse('permission matrix required')
    }
    rbacMatrix = matrix as RbacPermissionMatrixRow[]
    rbacChanges = [
      {
        id: `change_${Date.now()}`,
        time: new Date().toLocaleTimeString('zh-CN', { hour12: false, hour: '2-digit', minute: '2-digit' }),
        title: 'matrix updated',
        detail: `updated ${rbacMatrix.length} rows`,
        type: 'update' as RbacChangeType,
      },
      ...rbacChanges,
    ] as RbacRecentChangeItem[]
    rbacChanges = rbacChanges.slice(0, 20)
    return createSuccessResponse(overview(), 'save success')
  })
}

const initBillingApis = () => {
  registerMockApi('GET', '/api/admin/billing/overview', () => createSuccessResponse(billingOverview(), 'billing success'))

  registerMockApi('POST', '/api/admin/billing/subscription/change-plan', (payload) => {
    const code = payload?.planCode
    if (code !== 'FREE' && code !== 'PRO_MONTHLY' && code !== 'TEAM_MONTHLY') {
      return createErrorResponse('invalid plan code')
    }
    currentPlanCode = code
    currentStatus = 'active'
    billingOrders = [
      {
        id: `order_${Date.now()}`,
        orderNo: `ord_${Date.now()}`,
        period: new Date().toISOString().slice(0, 7),
        amountText: planAmount(code),
        channel: code === 'FREE' ? 'system' : 'Stripe',
        status: (code === 'FREE' ? 'paid' : 'pending') as BillingOrderStatus,
        invoiceStatus: (code === 'FREE' ? 'none' : 'generated') as BillingInvoiceStatus,
      },
      ...billingOrders,
    ].slice(0, 20)
    return createSuccessResponse(billingOverview(), 'plan changed')
  })

  registerMockApi('POST', '/api/admin/billing/reconciliation/trigger', () => {
    billingOrders = billingOrders.map((item) => {
      if (item.status !== 'pending') {
        return item
      }
      return { ...item, status: 'paid', invoiceStatus: item.amountText === '0' ? 'none' : 'generated' }
    })
    return createSuccessResponse(billingOverview(), 'reconcile triggered')
  })

  registerMockApi('POST', '/api/admin/billing/order/invoice-action', () =>
    createSuccessResponse(billingOverview(), 'invoice action done'),
  )
}

const initApiKeyApis = () => {
  registerMockApi('GET', '/api/admin/apikey/overview', () => createSuccessResponse(apiKeyOverview(), 'apikey success'))

  registerMockApi('POST', '/api/admin/apikey/create', (payload) => {
    const name = String(payload?.name ?? '').trim()
    const scope = String(payload?.scope ?? '')
    const limitPolicy = String(payload?.limitPolicy ?? '')
    if (!name || !scope || !limitPolicy) {
      return createErrorResponse('invalid apikey payload')
    }
    const secret = `sk_live_${Math.random().toString(36).slice(2, 12)}`
    apiKeys = [
      {
        id: `key_${apiKeySeq++}`,
        name,
        keyPrefix: `${secret.slice(0, 10)}...`,
        scope,
        limitPolicy,
        lastUsedText: 'just now',
        status: 'active',
      },
      ...apiKeys,
    ]
    return createSuccessResponse(apiKeyOverview(secret), 'apikey created')
  })

  registerMockApi('POST', '/api/admin/apikey/rotate', (payload) => {
    const id = String(payload?.id ?? '').trim()
    const idx = apiKeys.findIndex((item) => item.id === id)
    if (idx < 0) {
      return createErrorResponse('key not found', 404)
    }
    const secret = `sk_rotate_${Math.random().toString(36).slice(2, 12)}`
    apiKeys[idx] = { ...apiKeys[idx], keyPrefix: `${secret.slice(0, 10)}...`, lastUsedText: 'just now' }
    return createSuccessResponse(apiKeyOverview(secret), 'apikey rotated')
  })

  registerMockApi('POST', '/api/admin/apikey/update-status', (payload) => {
    const id = String(payload?.id ?? '').trim()
    const target = payload?.targetStatus
    if (target !== 'active' && target !== 'pending_disable' && target !== 'disabled') {
      return createErrorResponse('invalid status')
    }
    apiKeys = apiKeys.map((item) => (item.id === id ? { ...item, status: target } : item))
    return createSuccessResponse(apiKeyOverview(), 'status updated')
  })
}

const initAiGatewayApis = () => {
  registerMockApi('GET', '/api/admin/ai-gateway/overview', () =>
    createSuccessResponse(aiGatewayOverview(), 'ai gateway success'),
  )

  registerMockApi('POST', '/api/admin/ai-gateway/provider/switch', (payload) => {
    const providerId = String(payload?.providerId ?? '').trim()
    if (!providerId) {
      return createErrorResponse('provider id required')
    }
    aiProviderId = providerId
    return createSuccessResponse(aiGatewayOverview(), 'provider switched')
  })

  registerMockApi('POST', '/api/admin/ai-gateway/route-policy/save', (payload) => {
    const routePolicies = payload?.routePolicies
    if (!Array.isArray(routePolicies) || routePolicies.length === 0) {
      return createErrorResponse('route policies required')
    }
    aiRoutePolicies = routePolicies as AiGatewayRoutePolicyItem[]
    return createSuccessResponse(aiGatewayOverview(), 'route policies saved')
  })

  registerMockApi('POST', '/api/admin/ai-gateway/test-call', (payload) => {
    const endpoint = String(payload?.endpoint ?? '').trim()
    if (!endpoint) {
      return createErrorResponse('endpoint required')
    }
    aiLatestTest = {
      traceId: `aigw_${Date.now()}`,
      endpoint,
      status: 'success',
      message: 'test call success',
      time: new Date().toLocaleTimeString('zh-CN', { hour12: false }),
    }
    return createSuccessResponse(aiGatewayOverview(), 'test call done')
  })
}

const initUsageApis = () => {
  registerMockApi('GET', '/api/admin/usage/overview', () => createSuccessResponse(usageOverview(), 'usage success'))

  registerMockApi('POST', '/api/admin/usage/report/generate', () => {
    latestReportId = `report_${Date.now()}`
    usageJobs = [
      {
        id: `job_${Date.now()}`,
        jobId: `job_report_${Date.now()}`,
        type: 'REPORT_GENERATE',
        status: 'success' as UsageJobStatus,
        retryCount: 0,
        nextRetryAt: '-',
      },
      ...usageJobs,
    ].slice(0, 30)
    return createSuccessResponse(usageOverview(), 'report generated')
  })

  registerMockApi('POST', '/api/admin/usage/alerts/sync', () => {
    usageNotifyChannels = usageNotifyChannels.map((item) => ({ ...item, status: 'normal', detail: 'ok' }))
    return createSuccessResponse(usageOverview(), 'alerts synced')
  })

  registerMockApi('POST', '/api/admin/usage/job/retry', (payload) => {
    const jobId = String(payload?.jobId ?? '').trim()
    usageJobs = usageJobs.map((item) => {
      if (item.jobId !== jobId || item.status === 'success') {
        return item
      }
      return { ...item, status: 'success', retryCount: item.retryCount + 1, nextRetryAt: '-' }
    })
    return createSuccessResponse(usageOverview(), 'job retry triggered')
  })

  registerMockApi('POST', '/api/admin/usage/notify/test', () =>
    createSuccessResponse(usageOverview(), 'test notify sent'),
  )
}

export const initializeMockApis = () => {
  if (initialized) {
    return
  }
  ensureDefaultUsers()
  initAuthApis()
  initDashboardApis()
  initRbacApis()
  initBillingApis()
  initApiKeyApis()
  initAiGatewayApis()
  initUsageApis()
  initialized = true
}

export const requestMockApi = async (
  method: MockMethod,
  url: string,
  payload?: Record<string, unknown>,
): Promise<MockResponse> => {
  const handler = mockApiRegistry.get(buildRegistryKey(method, url))
  if (!handler) {
    return createErrorResponse(`mock endpoint not found: ${method} ${url}`, 404)
  }
  try {
    return await handler(payload)
  } catch (error) {
    if (error instanceof Error) {
      return createErrorResponse(error.message, 500)
    }
    return createErrorResponse('unknown error', 500)
  }
}
