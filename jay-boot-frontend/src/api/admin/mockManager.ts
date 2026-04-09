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
  id: number
  username: string
  email: string
  password: string
  tenantName: string
  createdAt: string
}

interface PublicUserProfile {
  id: number
  username: string
  email: string
  tenantName: string
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

interface DashboardKpiItem {
  key: string
  title: string
  valueText: string
  trendText: string
  trendDirection: DashboardTrendDirection
}

interface DashboardAlertItem {
  id: string
  module: string
  level: DashboardAlertLevel
  message: string
  time: string
}

interface DashboardEventItem {
  id: string
  time: string
  module: string
  event: string
  traceId: string
  status: DashboardEventStatus
}

interface DashboardOverviewBody {
  range: DashboardRange
  updatedAt: string
  kpis: DashboardKpiItem[]
  alerts: DashboardAlertItem[]
  events: DashboardEventItem[]
}

type TenantMemberStatus = 'active' | 'pending' | 'disabled'
type TenantIsolationStatus = 'pass' | 'risk'

interface TenantCurrentRecord {
  tenantId: string
  name: string
  planCode: 'FREE' | 'PRO_MONTHLY' | 'TEAM_MONTHLY' | 'TEAM_YEARLY'
  planName: string
  region: string
  ownerUserId: number
  ownerEmail: string
  memberCount: number
  memberLimit: number
  storageUsagePercent: number
  isolationPolicy: string
  isolationStatus: TenantIsolationStatus
}

interface TenantMemberRecord {
  id: number
  name: string
  email: string
  role: string
  lastActiveText: string
  status: TenantMemberStatus
}

interface TenantOverviewBody {
  tenant: TenantCurrentRecord
  members: TenantMemberRecord[]
  updatedAt: string
}

type RbacPermissionDecision = 'allow' | 'approval' | 'deny'
type RbacChangeType = 'create' | 'update' | 'delete'

interface RbacRoleTemplateItem {
  id: string
  code: string
  description: string
}

interface RbacPermissionMatrixRow {
  permissionCode: string
  owner: RbacPermissionDecision
  admin: RbacPermissionDecision
  developer: RbacPermissionDecision
  finance: RbacPermissionDecision
}

interface RbacPolicyNoteItem {
  id: string
  content: string
}

interface RbacRecentChangeItem {
  id: string
  time: string
  title: string
  detail: string
  type: RbacChangeType
}

interface RbacOverviewBody {
  roleTemplates: RbacRoleTemplateItem[]
  permissionMatrix: RbacPermissionMatrixRow[]
  policyNotes: RbacPolicyNoteItem[]
  recentChanges: RbacRecentChangeItem[]
  updatedAt: string
}

type BillingPlanCode = 'FREE' | 'PRO_MONTHLY' | 'TEAM_MONTHLY'
type BillingSubscriptionStatus = 'trialing' | 'active' | 'past_due' | 'canceled'
type BillingCallbackStatus = 'healthy' | 'degraded'
type BillingOrderStatus = 'paid' | 'pending' | 'failed' | 'refunded'
type BillingInvoiceStatus = 'generated' | 'sent' | 'none'

interface BillingPlanCard {
  code: BillingPlanCode
  name: string
  priceText: string
  billingCycleText: string
  featureList: string[]
  contactSales: boolean
  highlighted: boolean
}

interface BillingCurrentSubscription {
  planCode: BillingPlanCode
  planName: string
  status: BillingSubscriptionStatus
  renewAt: string
  trialEndAt: string
  amountText: string
}

interface BillingStatusStage {
  key: BillingSubscriptionStatus
  title: string
  description: string
}

interface BillingCallbackHealthItem {
  id: string
  provider: string
  successRate: number
  status: BillingCallbackStatus
  lastCheckedAt: string
}

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
  plans: BillingPlanCard[]
  currentSubscription: BillingCurrentSubscription
  statusStages: BillingStatusStage[]
  callbackHealth: BillingCallbackHealthItem[]
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

interface ApiKeyQuotaItem {
  id: string
  label: string
  percent: number
}

interface ApiKeyOverviewBody {
  keys: ApiKeyItem[]
  scopeOptions: string[]
  limitOptions: string[]
  quotaReminders: ApiKeyQuotaItem[]
  securityTips: string[]
  latestSecret: string
  updatedAt: string
}

type AiGatewayProviderStatus = 'online' | 'warning' | 'offline'
type AiGatewayRouteStatus = 'available' | 'high_load' | 'degraded'
type AiGatewayBillingUnit = 'token' | 'request'
type AiGatewayTestStatus = 'success' | 'retrying' | 'failed'

interface AiGatewayProviderItem {
  id: string
  name: string
  status: AiGatewayProviderStatus
  detail: string
}

interface AiGatewayRoutePolicyItem {
  id: string
  endpoint: string
  defaultModel: string
  timeoutPolicy: string
  billingUnit: AiGatewayBillingUnit
  status: AiGatewayRouteStatus
}

interface AiGatewayCostItem {
  id: string
  label: string
  percent: number
  costText: string
}

interface AiGatewayFallbackStep {
  id: string
  title: string
  detail: string
}

interface AiGatewayTestResult {
  traceId: string
  endpoint: string
  status: AiGatewayTestStatus
  message: string
  time: string
}

interface AiGatewayOverviewBody {
  activeProviderId: string
  providers: AiGatewayProviderItem[]
  routePolicies: AiGatewayRoutePolicyItem[]
  costTrend: AiGatewayCostItem[]
  fallbackSteps: AiGatewayFallbackStep[]
  latestTestResult: AiGatewayTestResult | null
  updatedAt: string
}

type UsageKpiTone = 'normal' | 'warning'
type UsageJobStatus = 'success' | 'retrying' | 'failed'
type UsageNotifyStatus = 'normal' | 'timeout'

interface UsageKpiItem {
  key: string
  title: string
  valueText: string
  hintText: string
  tone: UsageKpiTone
}

interface UsageJobItem {
  id: string
  jobId: string
  type: string
  status: UsageJobStatus
  retryCount: number
  nextRetryAt: string
}

interface UsageNotifyChannelItem {
  id: string
  name: string
  status: UsageNotifyStatus
  detail: string
}

interface UsageMetricBarItem {
  id: string
  label: string
  percent: number
  valueText: string
}

interface UsageAdviceItem {
  id: string
  content: string
}

interface UsageOverviewBody {
  kpis: UsageKpiItem[]
  jobs: UsageJobItem[]
  notifyChannels: UsageNotifyChannelItem[]
  metricBars: UsageMetricBarItem[]
  adviceItems: UsageAdviceItem[]
  latestReportId: string
  updatedAt: string
}

const MOCK_USER_STORAGE_KEY = 'jay_boot_mock_auth_users'
const MOCK_USER_ID_STORAGE_KEY = 'jay_boot_mock_auth_user_id'
const MOCK_DASHBOARD_REFRESH_COUNT_KEY = 'jay_boot_mock_dashboard_refresh_count'
const MOCK_TENANT_CURRENT_KEY = 'jay_boot_mock_tenant_current'
const MOCK_TENANT_MEMBERS_KEY = 'jay_boot_mock_tenant_members'
const MOCK_RBAC_MATRIX_KEY = 'jay_boot_mock_rbac_matrix'
const MOCK_RBAC_CHANGES_KEY = 'jay_boot_mock_rbac_changes'
const MOCK_BILLING_PLAN_KEY = 'jay_boot_mock_billing_plan'
const MOCK_BILLING_STATUS_KEY = 'jay_boot_mock_billing_status'
const MOCK_BILLING_ORDERS_KEY = 'jay_boot_mock_billing_orders'
const MOCK_BILLING_RECONCILE_COUNT_KEY = 'jay_boot_mock_billing_reconcile_count'
const MOCK_APIKEY_LIST_KEY = 'jay_boot_mock_apikey_list'
const MOCK_APIKEY_SEQ_KEY = 'jay_boot_mock_apikey_seq'
const MOCK_AIGW_ACTIVE_PROVIDER_KEY = 'jay_boot_mock_aigw_active_provider'
const MOCK_AIGW_ROUTE_POLICIES_KEY = 'jay_boot_mock_aigw_route_policies'
const MOCK_AIGW_TEST_COUNT_KEY = 'jay_boot_mock_aigw_test_count'
const MOCK_USAGE_JOBS_KEY = 'jay_boot_mock_usage_jobs'
const MOCK_USAGE_CHANNELS_KEY = 'jay_boot_mock_usage_channels'
const MOCK_USAGE_REPORT_COUNT_KEY = 'jay_boot_mock_usage_report_count'
const MOCK_USAGE_ALERT_SYNC_COUNT_KEY = 'jay_boot_mock_usage_alert_sync_count'
const mockApiRegistry = new Map<string, MockHandler>()
let initialized = false

const defaultRbacRoleTemplates: RbacRoleTemplateItem[] = [
  { id: 'role_owner', code: 'tenant_owner', description: '全量权限 + 账单管理' },
  { id: 'role_admin', code: 'tenant_admin', description: '用户、Key、告警管理' },
  { id: 'role_dev', code: 'developer', description: 'API Key 与 AI 调用配置' },
  { id: 'role_finance', code: 'finance_manager', description: '订阅、订单、发票只读' },
]

const defaultRbacPermissionMatrix: RbacPermissionMatrixRow[] = [
  {
    permissionCode: 'billing.subscription.update',
    owner: 'allow',
    admin: 'approval',
    developer: 'deny',
    finance: 'approval',
  },
  {
    permissionCode: 'apikey.create',
    owner: 'allow',
    admin: 'allow',
    developer: 'allow',
    finance: 'deny',
  },
  {
    permissionCode: 'ai.provider.config',
    owner: 'allow',
    admin: 'approval',
    developer: 'allow',
    finance: 'deny',
  },
  {
    permissionCode: 'tenant.member.invite',
    owner: 'allow',
    admin: 'allow',
    developer: 'approval',
    finance: 'deny',
  },
]

const defaultRbacPolicyNotes: RbacPolicyNoteItem[] = [
  { id: 'note_1', content: '所有 API 鉴权统一基于 Sa-Token + 权限码校验。' },
  { id: 'note_2', content: '角色变更写入审计日志，并保留变更前快照。' },
  { id: 'note_3', content: '跨租户管理权限仅开放给平台 super_admin。' },
]

const defaultRbacRecentChanges: RbacRecentChangeItem[] = [
  {
    id: 'change_1',
    time: '09:14',
    title: '新增角色 billing_manager',
    detail: '创建者：founder@demo.io',
    type: 'create',
  },
  {
    id: 'change_2',
    time: '10:02',
    title: '修改 ai.provider.config',
    detail: 'developer 从审批后调整为允许',
    type: 'update',
  },
  {
    id: 'change_3',
    time: '10:28',
    title: '删除过期测试角色',
    detail: '审计 ID: acl_0428_09',
    type: 'delete',
  },
]

const defaultBillingPlans: BillingPlanCard[] = [
  {
    code: 'FREE',
    name: 'Free',
    priceText: '¥0',
    billingCycleText: '按月',
    featureList: ['每月 10 万次 API 调用', '基础 AI 网关路由', '社区支持'],
    contactSales: false,
    highlighted: false,
  },
  {
    code: 'PRO_MONTHLY',
    name: 'Pro',
    priceText: '¥299',
    billingCycleText: '按月',
    featureList: ['每月 200 万次 API 调用', '高级模型路由策略', 'Webhook 回调通知'],
    contactSales: false,
    highlighted: true,
  },
  {
    code: 'TEAM_MONTHLY',
    name: 'Team',
    priceText: '¥899',
    billingCycleText: '按月',
    featureList: ['多成员协作与审批', '专属限流与审计策略', 'SLA 与优先支持'],
    contactSales: true,
    highlighted: false,
  },
]

const defaultBillingStatusStages: BillingStatusStage[] = [
  { key: 'trialing', title: 'trialing', description: '新注册租户默认试用 14 天' },
  { key: 'active', title: 'active', description: '支付完成后生效，按账期自动续费' },
  { key: 'past_due', title: 'past_due', description: '扣费失败进入宽限期，并限制写操作' },
  { key: 'canceled', title: 'canceled', description: '主动取消或宽限期结束后终止' },
]

const defaultBillingOrders: BillingOrderItem[] = [
  {
    id: 'bill_order_1',
    orderNo: 'ord_20260408001',
    period: '2026-04',
    amountText: '¥299',
    channel: 'Stripe',
    status: 'paid',
    invoiceStatus: 'generated',
  },
  {
    id: 'bill_order_2',
    orderNo: 'ord_20260308020',
    period: '2026-03',
    amountText: '¥299',
    channel: '支付宝',
    status: 'paid',
    invoiceStatus: 'sent',
  },
]

const apiKeyScopeOptions = ['/ai/chat', '/ai/*', '/ai/image']
const apiKeyLimitOptions = ['标准 60 QPS', '高配 120 QPS', '低配 20 QPS']

const defaultApiKeyItems: ApiKeyItem[] = [
  {
    id: 'key_1',
    name: 'server-prod',
    keyPrefix: 'sk_live_a2f9...',
    scope: '/ai/*',
    limitPolicy: '120 QPS / burst 240',
    lastUsedText: '刚刚',
    status: 'active',
  },
  {
    id: 'key_2',
    name: 'integration-ci',
    keyPrefix: 'sk_ci_b82d...',
    scope: '/ai/chat',
    limitPolicy: '30 QPS / burst 60',
    lastUsedText: '35 分钟前',
    status: 'active',
  },
  {
    id: 'key_3',
    name: 'legacy-script',
    keyPrefix: 'sk_old_1ad8...',
    scope: '/ai/image',
    limitPolicy: '10 QPS / burst 20',
    lastUsedText: '2 天前',
    status: 'pending_disable',
  },
]

const defaultApiKeyQuotaReminders: ApiKeyQuotaItem[] = [
  { id: 'quota_1', label: '本日请求', percent: 66 },
  { id: 'quota_2', label: '本月 Token', percent: 74 },
  { id: 'quota_3', label: '成本预算', percent: 59 },
]

const defaultApiKeySecurityTips: string[] = [
  'Key 泄露时一键禁用并触发轮换流程。',
  '按环境拆分 Key：prod、staging、dev。',
  '高风险请求启用 IP 白名单与签名校验。',
]

const defaultAiGatewayProviders: AiGatewayProviderItem[] = [
  { id: 'openai', name: 'OpenAI', status: 'online', detail: '在线' },
  { id: 'anthropic', name: 'Anthropic', status: 'online', detail: '在线' },
  { id: 'local', name: 'Local LLM', status: 'warning', detail: '容量预警' },
]

const defaultAiGatewayRoutePolicies: AiGatewayRoutePolicyItem[] = [
  {
    id: 'route_chat',
    endpoint: '/ai/chat',
    defaultModel: 'gpt-4.1-mini',
    timeoutPolicy: '8s + 2次重试',
    billingUnit: 'token',
    status: 'available',
  },
  {
    id: 'route_embedding',
    endpoint: '/ai/embedding',
    defaultModel: 'text-embedding-3-large',
    timeoutPolicy: '5s + 1次重试',
    billingUnit: 'request',
    status: 'available',
  },
  {
    id: 'route_image',
    endpoint: '/ai/image',
    defaultModel: 'gpt-image-1',
    timeoutPolicy: '15s + 降级本地模型',
    billingUnit: 'request',
    status: 'high_load',
  },
]

const defaultAiGatewayFallbackSteps: AiGatewayFallbackStep[] = [
  { id: 'fallback_1', title: '主模型失败', detail: '首次失败后立即重试一次' },
  { id: 'fallback_2', title: '备用模型切换', detail: '切换到次优 provider 并打标记' },
  { id: 'fallback_3', title: '超阈值熔断', detail: '进入保护态并返回友好错误码' },
  { id: 'fallback_4', title: '异步告警', detail: '通知到站内信 + Webhook' },
]

const defaultUsageJobs: UsageJobItem[] = [
  {
    id: 'usage_job_1',
    jobId: 'job_2408_001',
    type: 'EMAIL_SEND',
    status: 'success',
    retryCount: 0,
    nextRetryAt: '-',
  },
  {
    id: 'usage_job_2',
    jobId: 'job_2408_014',
    type: 'REPORT_GENERATE',
    status: 'retrying',
    retryCount: 2,
    nextRetryAt: '2026-04-08 12:10',
  },
  {
    id: 'usage_job_3',
    jobId: 'job_2408_021',
    type: 'AI_LONG_TASK',
    status: 'failed',
    retryCount: 3,
    nextRetryAt: '-',
  },
]

const defaultUsageNotifyChannels: UsageNotifyChannelItem[] = [
  { id: 'channel_1', name: '站内通知', status: 'normal', detail: '正常' },
  { id: 'channel_2', name: '邮件通道', status: 'normal', detail: '正常' },
  { id: 'channel_3', name: 'Webhook', status: 'timeout', detail: '2 次超时' },
]

const defaultUsageMetricBars: UsageMetricBarItem[] = [
  { id: 'metric_1', label: '新增租户', percent: 44, valueText: '44' },
  { id: 'metric_2', label: '付费转化', percent: 31, valueText: '31%' },
  { id: 'metric_3', label: '续费率', percent: 82, valueText: '82%' },
]

const defaultUsageAdviceItems: UsageAdviceItem[] = [
  { id: 'advice_1', content: 'Webhook 失败达到 3 次自动切换备用地址。' },
  { id: 'advice_2', content: '任务失败超过阈值后自动创建工单并抄送 owner。' },
  { id: 'advice_3', content: '成本预测超过预算 80% 时触发套餐升级提醒。' },
]

const buildRegistryKey = (method: MockMethod, url: string) => `${method.toUpperCase()} ${url}`

const toPublicUserProfile = (record: MockUserRecord): PublicUserProfile => ({
  id: record.id,
  username: record.username,
  email: record.email,
  tenantName: record.tenantName,
  createdAt: record.createdAt,
})

const clonePermissionMatrix = (rows: RbacPermissionMatrixRow[]) => rows.map((row) => ({ ...row }))
const cloneRecentChanges = (changes: RbacRecentChangeItem[]) => changes.map((item) => ({ ...item }))
const cloneBillingOrders = (orders: BillingOrderItem[]) => orders.map((item) => ({ ...item }))
const cloneApiKeyItems = (items: ApiKeyItem[]) => items.map((item) => ({ ...item }))
const cloneAiGatewayRoutePolicies = (items: AiGatewayRoutePolicyItem[]) => items.map((item) => ({ ...item }))
const cloneUsageJobs = (items: UsageJobItem[]) => items.map((item) => ({ ...item }))
const cloneUsageNotifyChannels = (items: UsageNotifyChannelItem[]) => items.map((item) => ({ ...item }))

const readUsers = (): MockUserRecord[] => {
  const raw = localStorage.getItem(MOCK_USER_STORAGE_KEY)
  if (!raw) {
    return []
  }
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? (parsed as MockUserRecord[]) : []
  } catch {
    return []
  }
}

const writeUsers = (users: MockUserRecord[]) => {
  localStorage.setItem(MOCK_USER_STORAGE_KEY, JSON.stringify(users))
}

const readUserIdSeed = (): number => {
  const raw = localStorage.getItem(MOCK_USER_ID_STORAGE_KEY)
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 1) {
    return 2
  }
  return parsed
}

const writeUserIdSeed = (nextId: number) => {
  localStorage.setItem(MOCK_USER_ID_STORAGE_KEY, String(nextId))
}

const issueMockToken = (userId: number) => `mock-token-${userId}-${Date.now()}`

const parseUserIdFromToken = (token: string): number | null => {
  if (!token.startsWith('mock-token-')) {
    return null
  }
  const segments = token.split('-')
  if (segments.length < 4) {
    return null
  }
  const id = Number(segments[2])
  if (!Number.isFinite(id) || id < 1) {
    return null
  }
  return id
}

const ensureDefaultUsers = () => {
  const users = readUsers()
  if (users.length > 0) {
    return
  }
  const now = new Date().toISOString()
  const defaultUser: MockUserRecord = {
    id: 1,
    username: 'admin',
    email: 'admin@jayboot.local',
    password: 'admin123',
    tenantName: 'Jay Boot',
    createdAt: now,
  }
  writeUsers([defaultUser])
  writeUserIdSeed(2)
}

const readDashboardRefreshCount = (): number => {
  const raw = localStorage.getItem(MOCK_DASHBOARD_REFRESH_COUNT_KEY)
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 0
  }
  return parsed
}

const writeDashboardRefreshCount = (count: number) => {
  localStorage.setItem(MOCK_DASHBOARD_REFRESH_COUNT_KEY, String(count))
}

const toDashboardRange = (range: string): DashboardRange => {
  if (range === '24h' || range === '30d') {
    return range
  }
  return '7d'
}

const createDashboardKpis = (range: DashboardRange, refreshCount: number): DashboardKpiItem[] => {
  const amplitude = 1 + (refreshCount % 4)
  const baseByRange = {
    '24h': { requests: 12480, tokensInMillions: 1.8, activeKeys: 19, failedJobs: 1, requestTrend: 4.1, tokenTrend: 2.8 },
    '7d': { requests: 89620, tokensInMillions: 12.7, activeKeys: 21, failedJobs: 5, requestTrend: 8.6, tokenTrend: 5.3 },
    '30d': { requests: 312880, tokensInMillions: 42.6, activeKeys: 24, failedJobs: 13, requestTrend: 12.8, tokenTrend: 7.2 },
  }[range]

  const requests = baseByRange.requests + amplitude * 640
  const tokensInMillions = Number((baseByRange.tokensInMillions + Number((amplitude * 0.45).toFixed(1))).toFixed(1))
  const activeKeys = baseByRange.activeKeys + (refreshCount % 2)
  const failedJobs = baseByRange.failedJobs + (refreshCount % 3 === 0 ? 1 : 0)

  return [
    { key: 'api_requests', title: 'API 请求量', valueText: requests.toLocaleString(), trendText: `+${baseByRange.requestTrend.toFixed(1)}%`, trendDirection: 'up' },
    { key: 'ai_tokens', title: 'AI Token 消耗', valueText: `${tokensInMillions}M`, trendText: `+${baseByRange.tokenTrend.toFixed(1)}%`, trendDirection: 'up' },
    { key: 'active_api_keys', title: '活跃 API Key', valueText: String(activeKeys), trendText: `高频 Key ${Math.max(2, Math.floor(activeKeys / 7))} 个`, trendDirection: 'flat' },
    { key: 'failed_jobs', title: '失败任务数', valueText: String(failedJobs), trendText: failedJobs <= 3 ? '风险可控' : '需优先处理', trendDirection: failedJobs <= 3 ? 'flat' : 'down' },
  ]
}

const createDashboardAlerts = (range: DashboardRange, refreshCount: number): DashboardAlertItem[] => {
  const templateByRange = {
    '24h': [
      { module: 'Billing', level: 'warn', message: 'Stripe webhook 出现延迟，建议检查重试队列' },
      { module: 'AI Gateway', level: 'info', message: '模型路由策略已更新，待观察效果' },
      { module: 'Jobs', level: 'error', message: '报表任务重试次数超过阈值' },
    ],
    '7d': [
      { module: 'Billing', level: 'warn', message: '发票生成任务在高峰时段耗时偏高' },
      { module: 'AI Gateway', level: 'warn', message: '高峰时段 Token 成本上升明显' },
      { module: 'Auth', level: 'info', message: '异常登录告警较上周下降 18%' },
    ],
    '30d': [
      { module: 'Billing', level: 'warn', message: '月末账单结算量激增，建议扩容队列' },
      { module: 'AI Gateway', level: 'error', message: '模型降级策略触发次数高于预期' },
      { module: 'Tenant', level: 'info', message: '新增租户趋势稳定，激活率保持增长' },
    ],
  }[range]

  return templateByRange.map((item, index) => ({
    id: `${range}_alert_${index + 1}`,
    module: item.module,
    level: item.level as DashboardAlertLevel,
    message: item.message,
    time: `${String(9 + index).padStart(2, '0')}:${String((refreshCount + 11 * (index + 1)) % 60).padStart(2, '0')}`,
  }))
}

const createDashboardEvents = (range: DashboardRange, refreshCount: number): DashboardEventItem[] => {
  const templates = {
    '24h': [
      { module: 'Billing', event: '订阅升级到 Pro（月付）', status: 'success' },
      { module: 'AI Gateway', event: '/ai/chat 响应超时自动重试', status: 'retrying' },
      { module: 'Jobs', event: '日报任务推送站内通知', status: 'success' },
      { module: 'Auth', event: '租户管理员完成安全设置', status: 'success' },
    ],
    '7d': [
      { module: 'Tenant', event: '新租户完成初始化配置', status: 'success' },
      { module: 'Billing', event: '账单对账任务失败待复核', status: 'failed' },
      { module: 'AI Gateway', event: '模型 A/B 路由切换生效', status: 'success' },
      { module: 'API Key', event: '高风险 Key 已自动限流', status: 'retrying' },
    ],
    '30d': [
      { module: 'Billing', event: '月度账单归档完成', status: 'success' },
      { module: 'AI Gateway', event: '成本阈值告警自动升级', status: 'failed' },
      { module: 'Tenant', event: '租户套餐批量续费任务完成', status: 'success' },
      { module: 'Auth', event: '多因素认证启用率达到目标', status: 'success' },
    ],
  }[range]

  return templates.map((item, index) => {
    const minute = (refreshCount * 7 + index * 9 + 13) % 60
    const traceTail = String(refreshCount + index + 1).padStart(4, '0')
    return {
      id: `${range}_event_${index + 1}`,
      time: `${String(10 + index).padStart(2, '0')}:${String(minute).padStart(2, '0')}`,
      module: item.module,
      event: item.event,
      traceId: `evt_${range}_${traceTail}`,
      status: item.status as DashboardEventStatus,
    }
  })
}

const readTenantCurrent = (): TenantCurrentRecord | null => {
  const raw = localStorage.getItem(MOCK_TENANT_CURRENT_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as TenantCurrentRecord
  } catch {
    return null
  }
}

const writeTenantCurrent = (tenant: TenantCurrentRecord) => {
  localStorage.setItem(MOCK_TENANT_CURRENT_KEY, JSON.stringify(tenant))
}

const readTenantMembers = (): TenantMemberRecord[] => {
  const raw = localStorage.getItem(MOCK_TENANT_MEMBERS_KEY)
  if (!raw) {
    return []
  }
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? (parsed as TenantMemberRecord[]) : []
  } catch {
    return []
  }
}

const writeTenantMembers = (members: TenantMemberRecord[]) => {
  localStorage.setItem(MOCK_TENANT_MEMBERS_KEY, JSON.stringify(members))
}

const ensureDefaultTenantData = () => {
  const existingCurrent = readTenantCurrent()
  const existingMembers = readTenantMembers()
  if (existingCurrent && existingMembers.length > 0) {
    return
  }

  const users = readUsers()
  const owner = users[0]
  const members: TenantMemberRecord[] = [
    { id: 1, name: 'Founder', email: owner?.email ?? 'founder@demo.io', role: 'tenant_owner', lastActiveText: '2 分钟前', status: 'active' },
    { id: 2, name: 'Ops', email: 'ops@demo.io', role: 'tenant_admin', lastActiveText: '21 分钟前', status: 'active' },
    { id: 3, name: 'Finance', email: 'finance@demo.io', role: 'billing_manager', lastActiveText: '1 天前', status: 'pending' },
  ]

  const currentTenant: TenantCurrentRecord = {
    tenantId: 'tenant_10001',
    name: owner?.tenantName || 'Starter Space',
    planCode: 'PRO_MONTHLY',
    planName: 'Pro 月付',
    region: '中国-上海',
    ownerUserId: owner?.id ?? 1,
    ownerEmail: owner?.email ?? 'founder@demo.io',
    memberCount: members.length,
    memberLimit: 20,
    storageUsagePercent: 63,
    isolationPolicy: '所有业务请求默认注入 tenant_id；跨租户操作需 super_admin 显式授权并记录审计。',
    isolationStatus: 'pass',
  }

  writeTenantCurrent(currentTenant)
  writeTenantMembers(members)
}

const isRbacPermissionDecision = (value: unknown): value is RbacPermissionDecision =>
  value === 'allow' || value === 'approval' || value === 'deny'

const readRbacPermissionMatrix = (): RbacPermissionMatrixRow[] => {
  const raw = localStorage.getItem(MOCK_RBAC_MATRIX_KEY)
  if (!raw) {
    return clonePermissionMatrix(defaultRbacPermissionMatrix)
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return clonePermissionMatrix(defaultRbacPermissionMatrix)
    }
    const normalized = parsed
      .filter(
        (row) =>
          typeof row?.permissionCode === 'string' &&
          isRbacPermissionDecision(row?.owner) &&
          isRbacPermissionDecision(row?.admin) &&
          isRbacPermissionDecision(row?.developer) &&
          isRbacPermissionDecision(row?.finance),
      )
      .map((row) => ({
        permissionCode: String(row.permissionCode),
        owner: row.owner as RbacPermissionDecision,
        admin: row.admin as RbacPermissionDecision,
        developer: row.developer as RbacPermissionDecision,
        finance: row.finance as RbacPermissionDecision,
      }))
    return normalized.length > 0 ? normalized : clonePermissionMatrix(defaultRbacPermissionMatrix)
  } catch {
    return clonePermissionMatrix(defaultRbacPermissionMatrix)
  }
}

const writeRbacPermissionMatrix = (matrix: RbacPermissionMatrixRow[]) => {
  localStorage.setItem(MOCK_RBAC_MATRIX_KEY, JSON.stringify(matrix))
}

const readRbacRecentChanges = (): RbacRecentChangeItem[] => {
  const raw = localStorage.getItem(MOCK_RBAC_CHANGES_KEY)
  if (!raw) {
    return cloneRecentChanges(defaultRbacRecentChanges)
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return cloneRecentChanges(defaultRbacRecentChanges)
    }
    const normalized = parsed
      .filter(
        (item) =>
          typeof item?.id === 'string' &&
          typeof item?.time === 'string' &&
          typeof item?.title === 'string' &&
          typeof item?.detail === 'string' &&
          (item?.type === 'create' || item?.type === 'update' || item?.type === 'delete'),
      )
      .map((item) => ({
        id: String(item.id),
        time: String(item.time),
        title: String(item.title),
        detail: String(item.detail),
        type: item.type as RbacChangeType,
      }))
    return normalized.length > 0 ? normalized : cloneRecentChanges(defaultRbacRecentChanges)
  } catch {
    return cloneRecentChanges(defaultRbacRecentChanges)
  }
}

const writeRbacRecentChanges = (changes: RbacRecentChangeItem[]) => {
  localStorage.setItem(MOCK_RBAC_CHANGES_KEY, JSON.stringify(changes))
}

const ensureDefaultRbacData = () => {
  if (!localStorage.getItem(MOCK_RBAC_MATRIX_KEY)) {
    writeRbacPermissionMatrix(clonePermissionMatrix(defaultRbacPermissionMatrix))
  }
  if (!localStorage.getItem(MOCK_RBAC_CHANGES_KEY)) {
    writeRbacRecentChanges(cloneRecentChanges(defaultRbacRecentChanges))
  }
}

const isBillingPlanCode = (value: unknown): value is BillingPlanCode =>
  value === 'FREE' || value === 'PRO_MONTHLY' || value === 'TEAM_MONTHLY'

const isBillingSubscriptionStatus = (value: unknown): value is BillingSubscriptionStatus =>
  value === 'trialing' || value === 'active' || value === 'past_due' || value === 'canceled'

const isBillingOrderStatus = (value: unknown): value is BillingOrderStatus =>
  value === 'paid' || value === 'pending' || value === 'failed' || value === 'refunded'

const isBillingInvoiceStatus = (value: unknown): value is BillingInvoiceStatus =>
  value === 'generated' || value === 'sent' || value === 'none'

const getBillingPlanByCode = (planCode: BillingPlanCode) =>
  defaultBillingPlans.find((plan) => plan.code === planCode) ?? defaultBillingPlans[0]

const readBillingPlanCode = (): BillingPlanCode => {
  const raw = localStorage.getItem(MOCK_BILLING_PLAN_KEY)
  return isBillingPlanCode(raw) ? raw : 'PRO_MONTHLY'
}

const writeBillingPlanCode = (planCode: BillingPlanCode) => {
  localStorage.setItem(MOCK_BILLING_PLAN_KEY, planCode)
}

const readBillingStatus = (): BillingSubscriptionStatus => {
  const raw = localStorage.getItem(MOCK_BILLING_STATUS_KEY)
  return isBillingSubscriptionStatus(raw) ? raw : 'active'
}

const writeBillingStatus = (status: BillingSubscriptionStatus) => {
  localStorage.setItem(MOCK_BILLING_STATUS_KEY, status)
}

const readBillingReconcileCount = (): number => {
  const raw = localStorage.getItem(MOCK_BILLING_RECONCILE_COUNT_KEY)
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 0
  }
  return parsed
}

const writeBillingReconcileCount = (count: number) => {
  localStorage.setItem(MOCK_BILLING_RECONCILE_COUNT_KEY, String(count))
}

const readBillingOrders = (): BillingOrderItem[] => {
  const raw = localStorage.getItem(MOCK_BILLING_ORDERS_KEY)
  if (!raw) {
    return cloneBillingOrders(defaultBillingOrders)
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return cloneBillingOrders(defaultBillingOrders)
    }
    const normalized = parsed
      .filter(
        (item) =>
          typeof item?.id === 'string' &&
          typeof item?.orderNo === 'string' &&
          typeof item?.period === 'string' &&
          typeof item?.amountText === 'string' &&
          typeof item?.channel === 'string' &&
          isBillingOrderStatus(item?.status) &&
          isBillingInvoiceStatus(item?.invoiceStatus),
      )
      .map((item) => ({
        id: String(item.id),
        orderNo: String(item.orderNo),
        period: String(item.period),
        amountText: String(item.amountText),
        channel: String(item.channel),
        status: item.status as BillingOrderStatus,
        invoiceStatus: item.invoiceStatus as BillingInvoiceStatus,
      }))
    return normalized.length > 0 ? normalized : cloneBillingOrders(defaultBillingOrders)
  } catch {
    return cloneBillingOrders(defaultBillingOrders)
  }
}

const writeBillingOrders = (orders: BillingOrderItem[]) => {
  localStorage.setItem(MOCK_BILLING_ORDERS_KEY, JSON.stringify(orders))
}

const ensureDefaultBillingData = () => {
  if (!localStorage.getItem(MOCK_BILLING_PLAN_KEY)) {
    writeBillingPlanCode('PRO_MONTHLY')
  }
  if (!localStorage.getItem(MOCK_BILLING_STATUS_KEY)) {
    writeBillingStatus('active')
  }
  if (!localStorage.getItem(MOCK_BILLING_ORDERS_KEY)) {
    writeBillingOrders(cloneBillingOrders(defaultBillingOrders))
  }
  if (!localStorage.getItem(MOCK_BILLING_RECONCILE_COUNT_KEY)) {
    writeBillingReconcileCount(0)
  }
}

const isApiKeyStatus = (value: unknown): value is ApiKeyStatus =>
  value === 'active' || value === 'pending_disable' || value === 'disabled'

const readApiKeyList = (): ApiKeyItem[] => {
  const raw = localStorage.getItem(MOCK_APIKEY_LIST_KEY)
  if (!raw) {
    return cloneApiKeyItems(defaultApiKeyItems)
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return cloneApiKeyItems(defaultApiKeyItems)
    }
    const normalized = parsed
      .filter(
        (item) =>
          typeof item?.id === 'string' &&
          typeof item?.name === 'string' &&
          typeof item?.keyPrefix === 'string' &&
          typeof item?.scope === 'string' &&
          typeof item?.limitPolicy === 'string' &&
          typeof item?.lastUsedText === 'string' &&
          isApiKeyStatus(item?.status),
      )
      .map((item) => ({
        id: String(item.id),
        name: String(item.name),
        keyPrefix: String(item.keyPrefix),
        scope: String(item.scope),
        limitPolicy: String(item.limitPolicy),
        lastUsedText: String(item.lastUsedText),
        status: item.status as ApiKeyStatus,
      }))
    return normalized.length > 0 ? normalized : cloneApiKeyItems(defaultApiKeyItems)
  } catch {
    return cloneApiKeyItems(defaultApiKeyItems)
  }
}

const writeApiKeyList = (items: ApiKeyItem[]) => {
  localStorage.setItem(MOCK_APIKEY_LIST_KEY, JSON.stringify(items))
}

const readApiKeySeq = (): number => {
  const raw = localStorage.getItem(MOCK_APIKEY_SEQ_KEY)
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 4) {
    return 4
  }
  return parsed
}

const writeApiKeySeq = (value: number) => {
  localStorage.setItem(MOCK_APIKEY_SEQ_KEY, String(value))
}

const ensureDefaultApiKeyData = () => {
  if (!localStorage.getItem(MOCK_APIKEY_LIST_KEY)) {
    writeApiKeyList(cloneApiKeyItems(defaultApiKeyItems))
  }
  if (!localStorage.getItem(MOCK_APIKEY_SEQ_KEY)) {
    writeApiKeySeq(4)
  }
}

const createApiKeySecret = (kind: 'live' | 'rotate') => {
  const randomPart = Math.random().toString(36).replace('.', '').slice(0, 12)
  return `sk_${kind}_${randomPart}${Date.now().toString().slice(-4)}`
}

const toKeyPrefix = (secret: string) => `${secret.slice(0, 12)}...`

const createApiKeyOverview = (latestSecret = ''): ApiKeyOverviewBody => ({
  keys: readApiKeyList(),
  scopeOptions: apiKeyScopeOptions,
  limitOptions: apiKeyLimitOptions,
  quotaReminders: defaultApiKeyQuotaReminders,
  securityTips: defaultApiKeySecurityTips,
  latestSecret,
  updatedAt: new Date().toISOString(),
})

const isAiGatewayRouteStatus = (value: unknown): value is AiGatewayRouteStatus =>
  value === 'available' || value === 'high_load' || value === 'degraded'

const isAiGatewayBillingUnit = (value: unknown): value is AiGatewayBillingUnit =>
  value === 'token' || value === 'request'

const readAiGatewayActiveProviderId = (): string => {
  const raw = localStorage.getItem(MOCK_AIGW_ACTIVE_PROVIDER_KEY)
  const found = defaultAiGatewayProviders.find((item) => item.id === raw)
  return found ? found.id : 'openai'
}

const writeAiGatewayActiveProviderId = (providerId: string) => {
  localStorage.setItem(MOCK_AIGW_ACTIVE_PROVIDER_KEY, providerId)
}

const readAiGatewayRoutePolicies = (): AiGatewayRoutePolicyItem[] => {
  const raw = localStorage.getItem(MOCK_AIGW_ROUTE_POLICIES_KEY)
  if (!raw) {
    return cloneAiGatewayRoutePolicies(defaultAiGatewayRoutePolicies)
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return cloneAiGatewayRoutePolicies(defaultAiGatewayRoutePolicies)
    }
    const normalized = parsed
      .filter(
        (item) =>
          typeof item?.id === 'string' &&
          typeof item?.endpoint === 'string' &&
          typeof item?.defaultModel === 'string' &&
          typeof item?.timeoutPolicy === 'string' &&
          isAiGatewayBillingUnit(item?.billingUnit) &&
          isAiGatewayRouteStatus(item?.status),
      )
      .map((item) => ({
        id: String(item.id),
        endpoint: String(item.endpoint),
        defaultModel: String(item.defaultModel),
        timeoutPolicy: String(item.timeoutPolicy),
        billingUnit: item.billingUnit as AiGatewayBillingUnit,
        status: item.status as AiGatewayRouteStatus,
      }))
    return normalized.length > 0 ? normalized : cloneAiGatewayRoutePolicies(defaultAiGatewayRoutePolicies)
  } catch {
    return cloneAiGatewayRoutePolicies(defaultAiGatewayRoutePolicies)
  }
}

const writeAiGatewayRoutePolicies = (items: AiGatewayRoutePolicyItem[]) => {
  localStorage.setItem(MOCK_AIGW_ROUTE_POLICIES_KEY, JSON.stringify(items))
}

const readAiGatewayTestCount = (): number => {
  const raw = localStorage.getItem(MOCK_AIGW_TEST_COUNT_KEY)
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 0
  }
  return parsed
}

const writeAiGatewayTestCount = (value: number) => {
  localStorage.setItem(MOCK_AIGW_TEST_COUNT_KEY, String(value))
}

const ensureDefaultAiGatewayData = () => {
  if (!localStorage.getItem(MOCK_AIGW_ACTIVE_PROVIDER_KEY)) {
    writeAiGatewayActiveProviderId('openai')
  }
  if (!localStorage.getItem(MOCK_AIGW_ROUTE_POLICIES_KEY)) {
    writeAiGatewayRoutePolicies(cloneAiGatewayRoutePolicies(defaultAiGatewayRoutePolicies))
  }
  if (!localStorage.getItem(MOCK_AIGW_TEST_COUNT_KEY)) {
    writeAiGatewayTestCount(0)
  }
}

const createAiGatewayCostTrend = (testCount: number): AiGatewayCostItem[] => {
  const delta = testCount % 6
  const base = [
    { id: 'cost_mon', label: 'Mon', amount: 128, percent: 48 },
    { id: 'cost_tue', label: 'Tue', amount: 149, percent: 56 },
    { id: 'cost_wed', label: 'Wed', amount: 168, percent: 63 },
    { id: 'cost_thu', label: 'Thu', amount: 188, percent: 71 },
    { id: 'cost_fri', label: 'Fri', amount: 175, percent: 66 },
  ]
  return base.map((item, index) => {
    const nextAmount = item.amount + (index % 2 === 0 ? delta : Math.max(0, delta - 1)) * 2
    const nextPercent = Math.min(88, item.percent + (index % 2 === 0 ? delta : Math.max(0, delta - 1)))
    return {
      id: item.id,
      label: item.label,
      percent: nextPercent,
      costText: `¥${nextAmount}`,
    }
  })
}

const createAiGatewayOverview = (latestTestResult: AiGatewayTestResult | null = null): AiGatewayOverviewBody => ({
  activeProviderId: readAiGatewayActiveProviderId(),
  providers: defaultAiGatewayProviders,
  routePolicies: readAiGatewayRoutePolicies(),
  costTrend: createAiGatewayCostTrend(readAiGatewayTestCount()),
  fallbackSteps: defaultAiGatewayFallbackSteps,
  latestTestResult,
  updatedAt: new Date().toISOString(),
})

const isUsageJobStatus = (value: unknown): value is UsageJobStatus =>
  value === 'success' || value === 'retrying' || value === 'failed'

const isUsageNotifyStatus = (value: unknown): value is UsageNotifyStatus => value === 'normal' || value === 'timeout'

const readUsageJobs = (): UsageJobItem[] => {
  const raw = localStorage.getItem(MOCK_USAGE_JOBS_KEY)
  if (!raw) {
    return cloneUsageJobs(defaultUsageJobs)
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return cloneUsageJobs(defaultUsageJobs)
    }
    const normalized = parsed
      .filter(
        (item) =>
          typeof item?.id === 'string' &&
          typeof item?.jobId === 'string' &&
          typeof item?.type === 'string' &&
          isUsageJobStatus(item?.status) &&
          Number.isFinite(Number(item?.retryCount)) &&
          typeof item?.nextRetryAt === 'string',
      )
      .map((item) => ({
        id: String(item.id),
        jobId: String(item.jobId),
        type: String(item.type),
        status: item.status as UsageJobStatus,
        retryCount: Number(item.retryCount),
        nextRetryAt: String(item.nextRetryAt),
      }))
    return normalized.length > 0 ? normalized : cloneUsageJobs(defaultUsageJobs)
  } catch {
    return cloneUsageJobs(defaultUsageJobs)
  }
}

const writeUsageJobs = (items: UsageJobItem[]) => {
  localStorage.setItem(MOCK_USAGE_JOBS_KEY, JSON.stringify(items))
}

const readUsageNotifyChannels = (): UsageNotifyChannelItem[] => {
  const raw = localStorage.getItem(MOCK_USAGE_CHANNELS_KEY)
  if (!raw) {
    return cloneUsageNotifyChannels(defaultUsageNotifyChannels)
  }
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return cloneUsageNotifyChannels(defaultUsageNotifyChannels)
    }
    const normalized = parsed
      .filter(
        (item) =>
          typeof item?.id === 'string' &&
          typeof item?.name === 'string' &&
          isUsageNotifyStatus(item?.status) &&
          typeof item?.detail === 'string',
      )
      .map((item) => ({
        id: String(item.id),
        name: String(item.name),
        status: item.status as UsageNotifyStatus,
        detail: String(item.detail),
      }))
    return normalized.length > 0 ? normalized : cloneUsageNotifyChannels(defaultUsageNotifyChannels)
  } catch {
    return cloneUsageNotifyChannels(defaultUsageNotifyChannels)
  }
}

const writeUsageNotifyChannels = (items: UsageNotifyChannelItem[]) => {
  localStorage.setItem(MOCK_USAGE_CHANNELS_KEY, JSON.stringify(items))
}

const readUsageReportCount = (): number => {
  const raw = localStorage.getItem(MOCK_USAGE_REPORT_COUNT_KEY)
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 0
  }
  return parsed
}

const writeUsageReportCount = (value: number) => {
  localStorage.setItem(MOCK_USAGE_REPORT_COUNT_KEY, String(value))
}

const readUsageAlertSyncCount = (): number => {
  const raw = localStorage.getItem(MOCK_USAGE_ALERT_SYNC_COUNT_KEY)
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 0
  }
  return parsed
}

const writeUsageAlertSyncCount = (value: number) => {
  localStorage.setItem(MOCK_USAGE_ALERT_SYNC_COUNT_KEY, String(value))
}

const ensureDefaultUsageData = () => {
  if (!localStorage.getItem(MOCK_USAGE_JOBS_KEY)) {
    writeUsageJobs(cloneUsageJobs(defaultUsageJobs))
  }
  if (!localStorage.getItem(MOCK_USAGE_CHANNELS_KEY)) {
    writeUsageNotifyChannels(cloneUsageNotifyChannels(defaultUsageNotifyChannels))
  }
  if (!localStorage.getItem(MOCK_USAGE_REPORT_COUNT_KEY)) {
    writeUsageReportCount(0)
  }
  if (!localStorage.getItem(MOCK_USAGE_ALERT_SYNC_COUNT_KEY)) {
    writeUsageAlertSyncCount(0)
  }
}

const createUsageKpis = (): UsageKpiItem[] => {
  const reportCount = readUsageReportCount()
  const syncCount = readUsageAlertSyncCount()
  const jobs = readUsageJobs()
  const notifyChannels = readUsageNotifyChannels()

  const successCount = jobs.filter((item) => item.status === 'success').length
  const taskSuccessRate = jobs.length === 0 ? 0 : (successCount / jobs.length) * 100
  const normalChannelCount = notifyChannels.filter((item) => item.status === 'normal').length
  const channelRate = notifyChannels.length === 0 ? 0 : (normalChannelCount / notifyChannels.length) * 100
  const requests = 18920 + reportCount * 140 + syncCount * 60
  const dailyCost = 372 + reportCount * 8 + syncCount * 5

  return [
    {
      key: 'today_requests',
      title: '今日请求数',
      valueText: requests.toLocaleString(),
      hintText: '峰值 1,240 / 分钟',
      tone: 'normal',
    },
    {
      key: 'task_success_rate',
      title: '任务成功率',
      valueText: `${taskSuccessRate.toFixed(1)}%`,
      hintText: `失败 ${jobs.filter((item) => item.status === 'failed').length} 条`,
      tone: 'normal',
    },
    {
      key: 'notify_delivery_rate',
      title: '通知到达率',
      valueText: `${Math.max(95, channelRate).toFixed(1)}%`,
      hintText: '邮件 + Webhook',
      tone: 'normal',
    },
    {
      key: 'today_cost',
      title: '本日成本',
      valueText: `¥${dailyCost}`,
      hintText: dailyCost >= 420 ? '接近预算阈值' : '成本在可控区间',
      tone: dailyCost >= 420 ? 'warning' : 'normal',
    },
  ]
}

const createUsageOverview = (latestReportId = ''): UsageOverviewBody => ({
  kpis: createUsageKpis(),
  jobs: readUsageJobs(),
  notifyChannels: readUsageNotifyChannels(),
  metricBars: defaultUsageMetricBars,
  adviceItems: defaultUsageAdviceItems,
  latestReportId,
  updatedAt: new Date().toISOString(),
})

const getBillingAmountText = (planCode: BillingPlanCode) => {
  const plan = getBillingPlanByCode(planCode)
  return plan.priceText
}

const getPlanNameByCode = (planCode: BillingPlanCode) => {
  const plan = getBillingPlanByCode(planCode)
  return `${plan.name} ${plan.billingCycleText}`
}

const toOrderNo = () => {
  const now = new Date()
  const datePart = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
  const serial = String(now.getTime()).slice(-5)
  return `ord_${datePart}${serial}`
}

const getCurrentPeriodText = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const syncTenantPlan = (planCode: BillingPlanCode) => {
  const current = readTenantCurrent()
  if (!current) {
    return
  }
  const nextCurrent: TenantCurrentRecord = {
    ...current,
    planCode,
    planName: getPlanNameByCode(planCode),
  }
  writeTenantCurrent(nextCurrent)
}

const createBillingCurrentSubscription = (
  planCode: BillingPlanCode,
  status: BillingSubscriptionStatus,
): BillingCurrentSubscription => {
  const now = new Date()
  const renewAtDate = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)
  const trialEndDate = new Date(now.getTime() + 14 * 24 * 60 * 60 * 1000)
  return {
    planCode,
    planName: getPlanNameByCode(planCode),
    status,
    renewAt: renewAtDate.toISOString(),
    trialEndAt: status === 'trialing' ? trialEndDate.toISOString() : '',
    amountText: getBillingAmountText(planCode),
  }
}

const createBillingCallbackHealth = (reconcileCount: number): BillingCallbackHealthItem[] => {
  const tick = reconcileCount % 5
  const now = new Date().toLocaleTimeString('zh-CN', { hour12: false, hour: '2-digit', minute: '2-digit' })
  const rates = [
    { provider: '微信支付', rate: 92 + tick },
    { provider: '支付宝', rate: 95 + (tick % 3) },
    { provider: 'Stripe', rate: 90 + ((tick + 2) % 4) },
  ]

  return rates.map((item, index) => ({
    id: `callback_${index + 1}`,
    provider: item.provider,
    successRate: Math.min(item.rate, 99),
    status: item.rate >= 94 ? 'healthy' : 'degraded',
    lastCheckedAt: now,
  }))
}

const createBillingOverview = (): BillingOverviewBody => {
  const planCode = readBillingPlanCode()
  const status = readBillingStatus()
  const reconcileCount = readBillingReconcileCount()
  return {
    plans: defaultBillingPlans,
    currentSubscription: createBillingCurrentSubscription(planCode, status),
    statusStages: defaultBillingStatusStages,
    callbackHealth: createBillingCallbackHealth(reconcileCount),
    orders: readBillingOrders(),
    updatedAt: new Date().toISOString(),
  }
}

const createRbacOverview = (): RbacOverviewBody => ({
  roleTemplates: defaultRbacRoleTemplates,
  permissionMatrix: readRbacPermissionMatrix(),
  policyNotes: defaultRbacPolicyNotes,
  recentChanges: readRbacRecentChanges(),
  updatedAt: new Date().toISOString(),
})

const initializeAuthMockApis = () => {
  registerMockApi('POST', '/api/admin/auth/register', (payload) => {
    const username = String(payload?.username ?? '').trim()
    const email = String(payload?.email ?? '').trim().toLowerCase()
    const password = String(payload?.password ?? '')
    const tenantName = String(payload?.tenantName ?? '').trim()

    if (!username || !email || !password || !tenantName) {
      return createErrorResponse('注册信息不完整，请检查后重试')
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      return createErrorResponse('邮箱格式不正确')
    }
    if (password.length < 6) {
      return createErrorResponse('密码长度不能少于 6 位')
    }

    const users = readUsers()
    const duplicated = users.some((user) => user.username === username || user.email === email)
    if (duplicated) {
      return createErrorResponse('用户名或邮箱已存在')
    }

    const nextId = readUserIdSeed()
    const newUser: MockUserRecord = {
      id: nextId,
      username,
      email,
      password,
      tenantName,
      createdAt: new Date().toISOString(),
    }

    users.push(newUser)
    writeUsers(users)
    writeUserIdSeed(nextId + 1)

    const result: AuthResultBody = {
      token: issueMockToken(newUser.id),
      user: toPublicUserProfile(newUser),
    }
    return createSuccessResponse(result, '注册成功')
  })

  registerMockApi('POST', '/api/admin/auth/login', (payload) => {
    const account = String(payload?.account ?? '').trim().toLowerCase()
    const password = String(payload?.password ?? '')
    if (!account || !password) {
      return createErrorResponse('请输入账号和密码')
    }

    const users = readUsers()
    const matched = users.find(
      (user) => user.username.toLowerCase() === account || user.email.toLowerCase() === account,
    )
    if (!matched || matched.password !== password) {
      return createErrorResponse('账号或密码错误')
    }

    const result: AuthResultBody = {
      token: issueMockToken(matched.id),
      user: toPublicUserProfile(matched),
    }
    return createSuccessResponse(result, '登录成功')
  })

  registerMockApi('GET', '/api/admin/auth/me', (payload) => {
    const token = String(payload?.token ?? '')
    if (!token) {
      return createErrorResponse('登录状态已失效，请重新登录', 401)
    }

    const userId = parseUserIdFromToken(token)
    if (!userId) {
      return createErrorResponse('登录状态无效，请重新登录', 401)
    }

    const users = readUsers()
    const matched = users.find((user) => user.id === userId)
    if (!matched) {
      return createErrorResponse('用户不存在，请重新登录', 404)
    }
    return createSuccessResponse(toPublicUserProfile(matched), '获取用户信息成功')
  })

  registerMockApi('POST', '/api/admin/auth/logout', () => createSuccessResponse(null, '已退出登录'))
}

const initializeDashboardMockApis = () => {
  registerMockApi('GET', '/api/admin/dashboard/overview', (payload) => {
    const range = toDashboardRange(String(payload?.range ?? '7d'))
    const nextRefreshCount = readDashboardRefreshCount() + 1
    writeDashboardRefreshCount(nextRefreshCount)

    const overview: DashboardOverviewBody = {
      range,
      updatedAt: new Date().toISOString(),
      kpis: createDashboardKpis(range, nextRefreshCount),
      alerts: createDashboardAlerts(range, nextRefreshCount),
      events: createDashboardEvents(range, nextRefreshCount),
    }

    return createSuccessResponse(overview, '获取仪表盘数据成功')
  })
}

const initializeTenantMockApis = () => {
  registerMockApi('GET', '/api/admin/tenant/overview', () => {
    const current = readTenantCurrent()
    const members = readTenantMembers()
    if (!current) {
      return createErrorResponse('租户信息不存在，请稍后重试', 404)
    }

    const normalizedCurrent: TenantCurrentRecord = {
      ...current,
      memberCount: members.length,
    }
    writeTenantCurrent(normalizedCurrent)

    const overview: TenantOverviewBody = {
      tenant: normalizedCurrent,
      members,
      updatedAt: new Date().toISOString(),
    }

    return createSuccessResponse(overview, '获取租户信息成功')
  })

  registerMockApi('POST', '/api/admin/tenant/current/update-name', (payload) => {
    const name = String(payload?.name ?? '').trim()
    if (!name) {
      return createErrorResponse('工作区名称不能为空')
    }
    if (name.length < 2 || name.length > 64) {
      return createErrorResponse('工作区名称长度需在 2-64 个字符之间')
    }

    const current = readTenantCurrent()
    if (!current) {
      return createErrorResponse('租户信息不存在，请稍后重试', 404)
    }

    const nextCurrent: TenantCurrentRecord = {
      ...current,
      name,
    }
    writeTenantCurrent(nextCurrent)

    return createSuccessResponse(nextCurrent, '租户名称更新成功')
  })
}

const initializeBillingMockApis = () => {
  registerMockApi('GET', '/api/admin/billing/overview', () => {
    return createSuccessResponse(createBillingOverview(), '获取 Billing 总览成功')
  })

  registerMockApi('POST', '/api/admin/billing/subscription/change-plan', (payload) => {
    const planCode = payload?.planCode
    if (!isBillingPlanCode(planCode)) {
      return createErrorResponse('套餐编码无效，请检查后重试')
    }

    const currentPlanCode = readBillingPlanCode()
    if (currentPlanCode === planCode) {
      return createErrorResponse('当前已是该套餐，无需重复切换')
    }

    writeBillingPlanCode(planCode)
    writeBillingStatus('active')
    syncTenantPlan(planCode)

    const plan = getBillingPlanByCode(planCode)
    const status: BillingOrderStatus = planCode === 'FREE' ? 'paid' : 'pending'
    const invoiceStatus: BillingInvoiceStatus =
      status === 'paid' && planCode !== 'FREE' ? 'generated' : 'none'
    const channel = planCode === 'FREE' ? '系统结转' : planCode === 'PRO_MONTHLY' ? 'Stripe' : '支付宝'

    const newOrder: BillingOrderItem = {
      id: `bill_order_${Date.now()}`,
      orderNo: toOrderNo(),
      period: getCurrentPeriodText(),
      amountText: plan.priceText,
      channel,
      status,
      invoiceStatus,
    }

    const orders = readBillingOrders()
    writeBillingOrders([newOrder, ...orders].slice(0, 20))

    return createSuccessResponse(createBillingOverview(), '套餐切换成功')
  })

  registerMockApi('POST', '/api/admin/billing/reconciliation/trigger', () => {
    const nextCount = readBillingReconcileCount() + 1
    writeBillingReconcileCount(nextCount)

    const orders = readBillingOrders()
    const pendingIndex = orders.findIndex((item) => item.status === 'pending')
    if (pendingIndex >= 0) {
      const pendingOrder = orders[pendingIndex]
      orders[pendingIndex] = {
        ...pendingOrder,
        status: 'paid',
        invoiceStatus: pendingOrder.amountText === '¥0' ? 'none' : 'generated',
      }
      writeBillingOrders(orders)
    }

    if (readBillingStatus() === 'past_due') {
      writeBillingStatus('active')
    }

    return createSuccessResponse(createBillingOverview(), '对账任务已触发')
  })

  registerMockApi('POST', '/api/admin/billing/order/invoice-action', (payload) => {
    const orderNo = String(payload?.orderNo ?? '').trim()
    const action = payload?.action
    if (!orderNo) {
      return createErrorResponse('订单号不能为空')
    }
    if (action !== 'download' && action !== 'send') {
      return createErrorResponse('发票动作不支持')
    }

    const orders = readBillingOrders()
    const index = orders.findIndex((item) => item.orderNo === orderNo)
    if (index < 0) {
      return createErrorResponse('订单不存在，请刷新后重试', 404)
    }

    const current = orders[index]
    if (current.invoiceStatus === 'none') {
      return createErrorResponse('该订单暂无可用发票')
    }

    if (action === 'send') {
      orders[index] = {
        ...current,
        invoiceStatus: 'sent',
      }
      writeBillingOrders(orders)
    }

    return createSuccessResponse(
      createBillingOverview(),
      action === 'download' ? '发票下载任务已创建' : '发票已通过邮件发送',
    )
  })
}

const initializeApiKeyMockApis = () => {
  registerMockApi('GET', '/api/admin/apikey/overview', () => {
    return createSuccessResponse(createApiKeyOverview(), '获取 API Key 总览成功')
  })

  registerMockApi('POST', '/api/admin/apikey/create', (payload) => {
    const name = String(payload?.name ?? '').trim()
    const scope = String(payload?.scope ?? '')
    const limitPolicy = String(payload?.limitPolicy ?? '')

    if (!name) {
      return createErrorResponse('Key 名称不能为空')
    }
    if (name.length < 2 || name.length > 40) {
      return createErrorResponse('Key 名称长度需在 2-40 个字符之间')
    }
    if (!apiKeyScopeOptions.includes(scope)) {
      return createErrorResponse('权限范围无效，请重新选择')
    }
    if (!apiKeyLimitOptions.includes(limitPolicy)) {
      return createErrorResponse('限流策略无效，请重新选择')
    }

    const list = readApiKeyList()
    const duplicated = list.some((item) => item.name.toLowerCase() === name.toLowerCase())
    if (duplicated) {
      return createErrorResponse('Key 名称已存在，请使用其他名称')
    }

    const seq = readApiKeySeq()
    const secret = createApiKeySecret('live')
    const nextItem: ApiKeyItem = {
      id: `key_${seq}`,
      name,
      keyPrefix: toKeyPrefix(secret),
      scope,
      limitPolicy,
      lastUsedText: '刚刚',
      status: 'active',
    }
    writeApiKeyList([nextItem, ...list].slice(0, 50))
    writeApiKeySeq(seq + 1)

    return createSuccessResponse(createApiKeyOverview(secret), 'API Key 创建成功')
  })

  registerMockApi('POST', '/api/admin/apikey/rotate', (payload) => {
    const id = String(payload?.id ?? '').trim()
    if (!id) {
      return createErrorResponse('密钥 ID 不能为空')
    }

    const list = readApiKeyList()
    const index = list.findIndex((item) => item.id === id)
    if (index < 0) {
      return createErrorResponse('API Key 不存在，请刷新后重试', 404)
    }
    if (list[index].status === 'disabled') {
      return createErrorResponse('已禁用的 Key 不允许轮换')
    }

    const secret = createApiKeySecret('rotate')
    list[index] = {
      ...list[index],
      keyPrefix: toKeyPrefix(secret),
      lastUsedText: '刚刚',
    }
    writeApiKeyList(list)

    return createSuccessResponse(createApiKeyOverview(secret), 'API Key 轮换成功')
  })

  registerMockApi('POST', '/api/admin/apikey/update-status', (payload) => {
    const id = String(payload?.id ?? '').trim()
    const targetStatus = payload?.targetStatus
    if (!id) {
      return createErrorResponse('密钥 ID 不能为空')
    }
    if (!isApiKeyStatus(targetStatus)) {
      return createErrorResponse('目标状态无效')
    }

    const list = readApiKeyList()
    const index = list.findIndex((item) => item.id === id)
    if (index < 0) {
      return createErrorResponse('API Key 不存在，请刷新后重试', 404)
    }

    list[index] = {
      ...list[index],
      status: targetStatus,
      lastUsedText: targetStatus === 'active' ? '刚刚' : list[index].lastUsedText,
    }
    writeApiKeyList(list)

    return createSuccessResponse(createApiKeyOverview(), 'API Key 状态更新成功')
  })
}

const initializeAiGatewayMockApis = () => {
  registerMockApi('GET', '/api/admin/ai-gateway/overview', () => {
    return createSuccessResponse(createAiGatewayOverview(), '获取 AI Gateway 总览成功')
  })

  registerMockApi('POST', '/api/admin/ai-gateway/provider/switch', (payload) => {
    const providerId = String(payload?.providerId ?? '').trim()
    const exists = defaultAiGatewayProviders.some((item) => item.id === providerId)
    if (!exists) {
      return createErrorResponse('Provider 不存在，请重新选择')
    }
    writeAiGatewayActiveProviderId(providerId)
    return createSuccessResponse(createAiGatewayOverview(), 'Provider 切换成功')
  })

  registerMockApi('POST', '/api/admin/ai-gateway/route-policy/save', (payload) => {
    const routePolicies = payload?.routePolicies
    if (!Array.isArray(routePolicies) || routePolicies.length === 0) {
      return createErrorResponse('路由策略不能为空')
    }

    const normalized = routePolicies
      .filter(
        (item) =>
          typeof item?.id === 'string' &&
          typeof item?.endpoint === 'string' &&
          typeof item?.defaultModel === 'string' &&
          typeof item?.timeoutPolicy === 'string' &&
          isAiGatewayBillingUnit(item?.billingUnit) &&
          isAiGatewayRouteStatus(item?.status),
      )
      .map((item) => ({
        id: String(item.id),
        endpoint: String(item.endpoint),
        defaultModel: String(item.defaultModel),
        timeoutPolicy: String(item.timeoutPolicy),
        billingUnit: item.billingUnit as AiGatewayBillingUnit,
        status: item.status as AiGatewayRouteStatus,
      }))

    if (normalized.length !== routePolicies.length) {
      return createErrorResponse('路由策略数据格式不正确')
    }

    writeAiGatewayRoutePolicies(normalized)
    return createSuccessResponse(createAiGatewayOverview(), '路由策略保存成功')
  })

  registerMockApi('POST', '/api/admin/ai-gateway/test-call', (payload) => {
    const endpoint = String(payload?.endpoint ?? '').trim()
    if (!endpoint) {
      return createErrorResponse('试调用接口不能为空')
    }

    const routePolicies = readAiGatewayRoutePolicies()
    const index = routePolicies.findIndex((item) => item.endpoint === endpoint)
    if (index < 0) {
      return createErrorResponse('目标接口不存在，请刷新后重试', 404)
    }

    const activeProviderId = readAiGatewayActiveProviderId()
    const nextTestCount = readAiGatewayTestCount() + 1
    writeAiGatewayTestCount(nextTestCount)

    let status: AiGatewayTestStatus = 'success'
    let message = '试调用成功'

    if (activeProviderId === 'local' && endpoint === '/ai/image') {
      status = 'retrying'
      message = '主模型超时，已触发本地降级重试'
      routePolicies[index] = {
        ...routePolicies[index],
        status: 'high_load',
      }
      writeAiGatewayRoutePolicies(routePolicies)
    } else if (nextTestCount % 5 === 0) {
      status = 'failed'
      message = '试调用失败，已触发异步告警'
      routePolicies[index] = {
        ...routePolicies[index],
        status: 'degraded',
      }
      writeAiGatewayRoutePolicies(routePolicies)
    } else {
      routePolicies[index] = {
        ...routePolicies[index],
        status: 'available',
      }
      writeAiGatewayRoutePolicies(routePolicies)
    }

    const result: AiGatewayTestResult = {
      traceId: `aigw_${Date.now()}`,
      endpoint,
      status,
      message,
      time: new Date().toLocaleTimeString('zh-CN', { hour12: false, hour: '2-digit', minute: '2-digit' }),
    }

    return createSuccessResponse(createAiGatewayOverview(result), '接口试调用完成')
  })
}

const initializeUsageMockApis = () => {
  registerMockApi('GET', '/api/admin/usage/overview', () => {
    return createSuccessResponse(createUsageOverview(), '获取用量与运营总览成功')
  })

  registerMockApi('POST', '/api/admin/usage/report/generate', () => {
    const nextCount = readUsageReportCount() + 1
    writeUsageReportCount(nextCount)

    const reportId = `report_${new Date().toISOString().slice(0, 10).replace(/-/g, '')}_${String(nextCount).padStart(2, '0')}`
    const jobs = readUsageJobs()
    const nextJob: UsageJobItem = {
      id: `usage_report_job_${Date.now()}`,
      jobId: `job_report_${Date.now().toString().slice(-6)}`,
      type: 'REPORT_GENERATE',
      status: 'success',
      retryCount: 0,
      nextRetryAt: '-',
    }
    writeUsageJobs([nextJob, ...jobs].slice(0, 30))

    return createSuccessResponse(createUsageOverview(reportId), '运营日报已生成')
  })

  registerMockApi('POST', '/api/admin/usage/alerts/sync', () => {
    const nextCount = readUsageAlertSyncCount() + 1
    writeUsageAlertSyncCount(nextCount)

    const channels = readUsageNotifyChannels().map((item) => {
      if (item.name === 'Webhook') {
        return {
          ...item,
          status: 'normal' as UsageNotifyStatus,
          detail: '正常',
        }
      }
      return item
    })
    writeUsageNotifyChannels(channels)

    return createSuccessResponse(createUsageOverview(), '告警状态同步完成')
  })

  registerMockApi('POST', '/api/admin/usage/job/retry', (payload) => {
    const jobId = String(payload?.jobId ?? '').trim()
    if (!jobId) {
      return createErrorResponse('任务 ID 不能为空')
    }

    const jobs = readUsageJobs()
    const index = jobs.findIndex((item) => item.jobId === jobId)
    if (index < 0) {
      return createErrorResponse('任务不存在，请刷新后重试', 404)
    }

    const current = jobs[index]
    if (current.status === 'success') {
      return createErrorResponse('成功任务无需重试')
    }

    if (current.retryCount >= 3) {
      jobs[index] = {
        ...current,
        status: 'failed',
        nextRetryAt: '-',
      }
    } else {
      const nextRetryAt = new Date(Date.now() + 10 * 60 * 1000).toLocaleString('zh-CN', { hour12: false })
      jobs[index] = {
        ...current,
        status: current.status === 'failed' ? 'retrying' : 'success',
        retryCount: current.retryCount + 1,
        nextRetryAt: current.status === 'failed' ? nextRetryAt : '-',
      }
    }

    writeUsageJobs(jobs)
    return createSuccessResponse(createUsageOverview(), '任务重试已触发')
  })

  registerMockApi('POST', '/api/admin/usage/notify/test', () => {
    const channels = readUsageNotifyChannels().map((item) => ({
      ...item,
      status: 'normal' as UsageNotifyStatus,
      detail: '正常',
    }))
    writeUsageNotifyChannels(channels)
    return createSuccessResponse(createUsageOverview(), '测试通知已发送')
  })
}

const initializeRbacMockApis = () => {
  registerMockApi('GET', '/api/admin/rbac/overview', () => {
    return createSuccessResponse(createRbacOverview(), '获取 RBAC 总览成功')
  })

  registerMockApi('POST', '/api/admin/rbac/permission-matrix/save', (payload) => {
    const matrix = payload?.permissionMatrix
    if (!Array.isArray(matrix) || matrix.length === 0) {
      return createErrorResponse('权限矩阵不能为空')
    }

    const normalized = matrix
      .filter(
        (row) =>
          typeof row?.permissionCode === 'string' &&
          isRbacPermissionDecision(row?.owner) &&
          isRbacPermissionDecision(row?.admin) &&
          isRbacPermissionDecision(row?.developer) &&
          isRbacPermissionDecision(row?.finance),
      )
      .map((row) => ({
        permissionCode: String(row.permissionCode),
        owner: row.owner as RbacPermissionDecision,
        admin: row.admin as RbacPermissionDecision,
        developer: row.developer as RbacPermissionDecision,
        finance: row.finance as RbacPermissionDecision,
      }))

    if (normalized.length !== matrix.length) {
      return createErrorResponse('权限矩阵数据格式不正确')
    }

    writeRbacPermissionMatrix(normalized)

    const time = new Date().toLocaleTimeString('zh-CN', { hour12: false, hour: '2-digit', minute: '2-digit' })
    const changes = readRbacRecentChanges()
    const newChange: RbacRecentChangeItem = {
      id: `change_${Date.now()}`,
      time,
      title: '更新权限策略矩阵',
      detail: `本次更新权限项 ${normalized.length} 条`,
      type: 'update',
    }
    const nextChanges: RbacRecentChangeItem[] = [newChange, ...changes].slice(0, 20)
    writeRbacRecentChanges(nextChanges)

    return createSuccessResponse(createRbacOverview(), '权限策略保存成功')
  })
}

/**
 * 注册 Mock API
 * 功能描述：将指定 method + url 映射到处理器，供前端 API 调用
 * 入参：method 请求方式、url 接口路径、handler 处理函数
 * 返回参数：无
 * url地址：Mock内存注册表
 * 请求方式：GET/POST
 */
export const registerMockApi = (method: MockMethod, url: string, handler: MockHandler) => {
  const key = buildRegistryKey(method, url)
  mockApiRegistry.set(key, handler)
}

/**
 * 创建成功响应
 * 功能描述：统一生成成功结构响应
 * 入参：body 响应体、message 响应消息
 * 返回参数：统一成功响应结构
 * url地址：N/A
 * 请求方式：GET/POST
 */
export const createSuccessResponse = <T>(body: T, message = '操作成功'): MockSuccessResponse<T> => ({
  error: 0,
  body,
  message,
  success: true,
})

/**
 * 创建失败响应
 * 功能描述：统一生成失败结构响应
 * 入参：message 错误消息、error 错误码
 * 返回参数：统一失败响应结构
 * url地址：N/A
 * 请求方式：GET/POST
 */
export const createErrorResponse = (message: string, error = 1): MockErrorResponse => ({
  error,
  body: null,
  message,
  success: false,
})

/**
 * 初始化所有 Mock API
 * 功能描述：初始化并注册系统内所有 Mock 接口
 * 入参：无
 * 返回参数：无
 * url地址：N/A
 * 请求方式：GET/POST
 */
export const initializeMockApis = () => {
  if (initialized) {
    return
  }
  ensureDefaultUsers()
  ensureDefaultTenantData()
  ensureDefaultRbacData()
  ensureDefaultBillingData()
  ensureDefaultApiKeyData()
  ensureDefaultAiGatewayData()
  ensureDefaultUsageData()
  initializeAuthMockApis()
  initializeDashboardMockApis()
  initializeTenantMockApis()
  initializeBillingMockApis()
  initializeApiKeyMockApis()
  initializeAiGatewayMockApis()
  initializeUsageMockApis()
  initializeRbacMockApis()
  initialized = true
}

/**
 * 请求 Mock API
 * 功能描述：按 method + url 路由到已注册的 Mock 处理器
 * 入参：method 请求方式、url 接口路径、payload 请求参数
 * 返回参数：统一响应结构
 * url地址：已注册的Mock URL
 * 请求方式：GET/POST
 */
export const requestMockApi = async (
  method: MockMethod,
  url: string,
  payload?: Record<string, unknown>,
): Promise<MockResponse> => {
  const key = buildRegistryKey(method, url)
  const handler = mockApiRegistry.get(key)
  if (!handler) {
    return createErrorResponse(`未找到 Mock 接口：${key}`, 404)
  }
  try {
    return await handler(payload)
  } catch (error) {
    if (error instanceof Error) {
      return createErrorResponse(error.message, 500)
    }
    return createErrorResponse('未知异常', 500)
  }
}
