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

interface HomeHero {
  eyebrow: string
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

interface HomeFeatureCard {
  id: string
  title: string
  points: string[]
  soft: boolean
}

interface HomeKpiCard {
  id: string
  label: string
  value: string
  desc: string
  soft: boolean
}

interface HomeFaqItem {
  id: string
  question: string
  answer: string
}

interface HomeFinalCta {
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

interface HomeOverviewBody {
  hero: HomeHero
  trustBadges: string[]
  sectionTitles: {
    features: string
    socialProof: string
    faq: string
  }
  featureCards: HomeFeatureCard[]
  kpiCards: HomeKpiCard[]
  faqList: HomeFaqItem[]
  finalCta: HomeFinalCta
  updatedAt: string
}

interface HomeCaseItem {
  id: string
  title: string
  industry: string
  gain: string
  summary: string
}

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

interface AuthSessionBody {
  token: string
  user: PublicUserProfile
}

type FeaturePlanCode = 'FREE' | 'PRO' | 'TEAM'
type FeatureSupportLevel = 'supported' | 'partial' | 'unsupported'

interface FeaturesHero {
  eyebrow: string
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

interface FeaturesPlanCard {
  code: FeaturePlanCode
  name: string
  priceText: string
  billingCycleText: string
  fitFor: string
  highlights: string[]
  recommended: boolean
}

interface FeaturesComparisonItem {
  key: string
  name: string
  description: string
  levels: {
    free: FeatureSupportLevel
    pro: FeatureSupportLevel
    team: FeatureSupportLevel
  }
}

interface FeaturesComparisonGroup {
  id: string
  title: string
  items: FeaturesComparisonItem[]
}

interface FeaturesFaqItem {
  id: string
  question: string
  answer: string
}

interface FeaturesFinalCta {
  title: string
  description: string
  primaryActionText: string
  secondaryActionText: string
}

interface FeaturesOverviewBody {
  hero: FeaturesHero
  planCards: FeaturesPlanCard[]
  comparisonGroups: FeaturesComparisonGroup[]
  faqList: FeaturesFaqItem[]
  finalCta: FeaturesFinalCta
  updatedAt: string
}

interface FeaturesRoleProfile {
  key: string
  label: string
  recommendedPlan: FeaturePlanCode
  highlights: string[]
  advice: string
}

interface FeaturesRoleFitBody {
  defaultRoleKey: string
  roles: FeaturesRoleProfile[]
}

const USER_MOCK_STORAGE_KEY = 'jay_boot_user_mock_auth_users'
const USER_MOCK_ID_SEED_KEY = 'jay_boot_user_mock_auth_id_seed'

const mockApiRegistry = new Map<string, MockHandler>()
let initialized = false

const buildRegistryKey = (method: MockMethod, url: string) => `${method} ${url}`

const homeCases: HomeCaseItem[] = [
  {
    id: 'case_1',
    title: '教育训练营：7 天起号专题',
    industry: '教育培训',
    gain: '单周咨询线索 +129%',
    summary: '用多版本文案 + 结构化话题清单，2 人团队在 7 天内完成 42 条内容上线。',
  },
  {
    id: 'case_2',
    title: '本地生活门店：团购拉新脚本',
    industry: '本地生活',
    gain: '门店到店率 +37%',
    summary: '按门店地理位置与客群画像自动生成短视频脚本，减少重复改稿时间。',
  },
  {
    id: 'case_3',
    title: '品牌电商：上新周活动页',
    industry: '电商零售',
    gain: '活动页转化率 +18%',
    summary: '基于同款商品历史数据生成 A/B 文案版本，并自动输出交付清单。',
  },
  {
    id: 'case_4',
    title: '知识博主：私域课程首发',
    industry: '内容创作',
    gain: '预约转化 +26%',
    summary: '通过“选题-大纲-发布”的流水线模板，一周稳定产出 15 条可发布内容。',
  },
]

const createHomeOverview = (): HomeOverviewBody => ({
  hero: {
    eyebrow: '面向内容创业者的增长引擎',
    title: '10 秒生成可发布文案',
    description:
      '从选题、生成、迭代到交付，帮助你把 AI 创作能力直接转化为稳定收益。首页目标是“看完即理解，点击就试用”。',
    primaryActionText: '立即免费试用',
    secondaryActionText: '查看真实案例',
  },
  trustBadges: ['4.2 万创作者正在使用', '平均节省写作时间 68%', '支持小红书/公众号/短视频'],
  sectionTitles: {
    features: '核心价值',
    socialProof: '社证数据',
    faq: '常见问题',
  },
  featureCards: [
    {
      id: 'feature_1',
      title: '高转化模板',
      soft: false,
      points: ['按平台和场景生成文案结构', '支持热门选题拆解与复刻', '减少从 0 到 1 的思考成本'],
    },
    {
      id: 'feature_2',
      title: '多版本并行',
      soft: false,
      points: ['一次生成多个风格版本', '便于 A/B 测试提升转化', '保留历史用于持续复盘'],
    },
    {
      id: 'feature_3',
      title: '结果可交付',
      soft: true,
      points: ['支持链接交付给客户或团队', '支持导出 Markdown 与图片文档', '形成“创作-交付-复购”闭环'],
    },
  ],
  kpiCards: [
    {
      id: 'kpi_1',
      label: '累计生成内容',
      value: '2,900 万+',
      desc: '覆盖营销、私域、直播、电商等场景',
      soft: false,
    },
    {
      id: 'kpi_2',
      label: '7 日留存',
      value: '74%',
      desc: '核心用户持续使用工作台',
      soft: false,
    },
    {
      id: 'kpi_3',
      label: '付费转化',
      value: '12.8%',
      desc: '通过案例页进入的用户转化更高',
      soft: true,
    },
  ],
  faqList: [
    {
      id: 'faq_1',
      question: '适合哪些人群？',
      answer: '个体创作者、内容团队、带货操盘手都可直接使用。',
    },
    {
      id: 'faq_2',
      question: '免费试用包含什么？',
      answer: '可体验核心模板与基础生成能力，不限登录设备。',
    },
    {
      id: 'faq_3',
      question: '是否支持团队协作？',
      answer: '支持多人协作、任务共享和结果交付链接。',
    },
  ],
  finalCta: {
    title: '准备好把创作效率变成盈利能力了吗？',
    description: '现在注册即可开启试用，并获得首月订阅优惠。',
    primaryActionText: '免费开始',
    secondaryActionText: '查看套餐',
  },
  updatedAt: new Date().toISOString(),
})

const createFeaturesOverview = (): FeaturesOverviewBody => ({
  hero: {
    eyebrow: '按阶段选择最适合的功能方案',
    title: '把功能差异讲清楚，让选型一步到位',
    description:
      '围绕创作、协作、交付、数据与支持五个维度，快速比较 Free、Pro、Team 的能力差异，减少试错成本。',
    primaryActionText: '开始免费试用',
    secondaryActionText: '查看套餐价格',
  },
  planCards: [
    {
      code: 'FREE',
      name: 'Free',
      priceText: '¥0',
      billingCycleText: '按月',
      fitFor: '刚起步的个人创作者',
      highlights: ['基础创作模板', '单人空间', '基础导出能力'],
      recommended: false,
    },
    {
      code: 'PRO',
      name: 'Pro',
      priceText: '¥299',
      billingCycleText: '按月',
      fitFor: '稳定生产内容的创作者',
      highlights: ['高级模板与批量生成', '数据看板与复盘', '多渠道交付配置'],
      recommended: true,
    },
    {
      code: 'TEAM',
      name: 'Team',
      priceText: '¥899',
      billingCycleText: '按月',
      fitFor: '多人协作团队',
      highlights: ['多成员权限体系', '协作审批流', '专属成功经理支持'],
      recommended: false,
    },
  ],
  comparisonGroups: [
    {
      id: 'group_creation',
      title: '创作能力',
      items: [
        {
          key: 'creation_templates',
          name: '模板数量',
          description: '可使用的官方创作模板范围',
          levels: { free: 'partial', pro: 'supported', team: 'supported' },
        },
        {
          key: 'creation_batch',
          name: '批量生成',
          description: '一次生成多版本文案并支持对比',
          levels: { free: 'unsupported', pro: 'supported', team: 'supported' },
        },
      ],
    },
    {
      id: 'group_collaboration',
      title: '协作能力',
      items: [
        {
          key: 'collab_members',
          name: '成员协作',
          description: '多人共享工作区与任务看板',
          levels: { free: 'unsupported', pro: 'partial', team: 'supported' },
        },
        {
          key: 'collab_permission',
          name: '权限管理',
          description: '按角色分配操作权限',
          levels: { free: 'unsupported', pro: 'partial', team: 'supported' },
        },
      ],
    },
    {
      id: 'group_delivery',
      title: '交付与增长',
      items: [
        {
          key: 'delivery_link',
          name: '交付链接',
          description: '可共享给客户或团队的交付页',
          levels: { free: 'partial', pro: 'supported', team: 'supported' },
        },
        {
          key: 'delivery_analytics',
          name: '转化分析',
          description: '对交付结果的阅读与转化追踪',
          levels: { free: 'unsupported', pro: 'supported', team: 'supported' },
        },
      ],
    },
    {
      id: 'group_support',
      title: '支持服务',
      items: [
        {
          key: 'support_sla',
          name: '服务响应',
          description: '问题响应时效与服务等级',
          levels: { free: 'partial', pro: 'partial', team: 'supported' },
        },
        {
          key: 'support_success',
          name: '成功顾问',
          description: '是否有专属顾问协助落地',
          levels: { free: 'unsupported', pro: 'unsupported', team: 'supported' },
        },
      ],
    },
  ],
  faqList: [
    {
      id: 'faq_1',
      question: '可以先用 Free 再升级到 Pro/Team 吗？',
      answer: '支持随时升级，历史内容与配置会自动保留，不影响已有工作流。',
    },
    {
      id: 'faq_2',
      question: 'Team 套餐是否支持多角色管理？',
      answer: '支持按角色分配权限，并提供成员协作、审批与操作记录能力。',
    },
    {
      id: 'faq_3',
      question: '怎么判断我该选 Pro 还是 Team？',
      answer: '如果你以个人为主且需要高频产出，选 Pro；若多人协作且要权限治理，建议 Team。',
    },
  ],
  finalCta: {
    title: '还在犹豫选哪档？先试用再决定',
    description: '从 Free 开始，按业务增长平滑升级，降低前期决策风险。',
    primaryActionText: '立即注册试用',
    secondaryActionText: '查看定价页',
  },
  updatedAt: new Date().toISOString(),
})

const createFeaturesRoleFit = (): FeaturesRoleFitBody => ({
  defaultRoleKey: 'creator',
  roles: [
    {
      key: 'creator',
      label: '个人创作者',
      recommendedPlan: 'PRO',
      highlights: ['高频内容生成', '多版本 A/B 测试', '交付转化追踪'],
      advice: '建议先用 Pro 建立稳定产出节奏，再按协作需求升级 Team。',
    },
    {
      key: 'team_lead',
      label: '团队负责人',
      recommendedPlan: 'TEAM',
      highlights: ['多人协同与权限治理', '任务分配与审批流', '统一资产沉淀'],
      advice: '如果团队成员超过 3 人，优先选择 Team，避免后期迁移成本。',
    },
    {
      key: 'operator',
      label: '运营负责人',
      recommendedPlan: 'PRO',
      highlights: ['活动内容批量生产', '渠道转化复盘', '标准化交付模板'],
      advice: '先用 Pro 跑通运营节奏，若跨部门协作增加再升级 Team。',
    },
  ],
})

const toCasesLimit = (payload?: Record<string, unknown>) => {
  const parsed = Number(payload?.limit)
  if (!Number.isFinite(parsed)) {
    return 3
  }
  return Math.min(Math.max(1, Math.floor(parsed)), homeCases.length)
}

const toPublicUserProfile = (record: MockUserRecord): PublicUserProfile => ({
  id: record.id,
  username: record.username,
  email: record.email,
  tenantName: record.tenantName,
  createdAt: record.createdAt,
})

const readAuthUsers = (): MockUserRecord[] => {
  const raw = localStorage.getItem(USER_MOCK_STORAGE_KEY)
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

const writeAuthUsers = (users: MockUserRecord[]) => {
  localStorage.setItem(USER_MOCK_STORAGE_KEY, JSON.stringify(users))
}

const readAuthIdSeed = (): number => {
  const raw = localStorage.getItem(USER_MOCK_ID_SEED_KEY)
  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 1) {
    return 2
  }
  return parsed
}

const writeAuthIdSeed = (nextId: number) => {
  localStorage.setItem(USER_MOCK_ID_SEED_KEY, String(nextId))
}

const ensureDefaultAuthUsers = () => {
  const users = readAuthUsers()
  if (users.length > 0) {
    return
  }
  const now = new Date().toISOString()
  const defaultUser: MockUserRecord = {
    id: 1,
    username: 'creator',
    email: 'creator@jayboot.local',
    password: 'creator123',
    tenantName: 'JayBoot Creator',
    createdAt: now,
  }
  writeAuthUsers([defaultUser])
  writeAuthIdSeed(2)
}

const issueMockToken = (userId: number) => `user-mock-token-${userId}-${Date.now()}`

const parseUserIdFromToken = (token: string): number | null => {
  if (!token.startsWith('user-mock-token-')) {
    return null
  }
  const segments = token.split('-')
  if (segments.length < 5) {
    return null
  }
  const id = Number(segments[3])
  if (!Number.isFinite(id) || id < 1) {
    return null
  }
  return id
}

const findUserByAccount = (users: MockUserRecord[], account: string) => {
  const normalized = account.trim().toLowerCase()
  return users.find(
    (item) => item.username.trim().toLowerCase() === normalized || item.email.trim().toLowerCase() === normalized,
  )
}

const initializeAuthMockApis = () => {
  registerMockApi('POST', '/api/user/auth/register', (payload) => {
    const username = String(payload?.username ?? '').trim()
    const email = String(payload?.email ?? '').trim().toLowerCase()
    const password = String(payload?.password ?? '')
    const tenantName = String(payload?.tenantName ?? '').trim()

    if (!username || !email || !password || !tenantName) {
      return createErrorResponse('请完整填写注册信息')
    }
    if (username.length < 3 || username.length > 24) {
      return createErrorResponse('用户名长度需在 3-24 个字符之间')
    }
    if (!email.includes('@')) {
      return createErrorResponse('邮箱格式不正确')
    }
    if (password.length < 6) {
      return createErrorResponse('密码长度不能少于 6 位')
    }

    const users = readAuthUsers()
    const duplicated = users.some(
      (item) => item.username.trim().toLowerCase() === username.toLowerCase() || item.email.trim().toLowerCase() === email,
    )
    if (duplicated) {
      return createErrorResponse('用户名或邮箱已存在，请更换后重试')
    }

    const nextId = readAuthIdSeed()
    const newUser: MockUserRecord = {
      id: nextId,
      username,
      email,
      password,
      tenantName,
      createdAt: new Date().toISOString(),
    }
    writeAuthUsers([newUser, ...users])
    writeAuthIdSeed(nextId + 1)

    const session: AuthSessionBody = {
      token: issueMockToken(newUser.id),
      user: toPublicUserProfile(newUser),
    }

    return createSuccessResponse(session, '注册成功')
  })

  registerMockApi('POST', '/api/user/auth/login', (payload) => {
    const account = String(payload?.account ?? '').trim()
    const password = String(payload?.password ?? '')

    if (!account || !password) {
      return createErrorResponse('请输入账号和密码')
    }

    const users = readAuthUsers()
    const user = findUserByAccount(users, account)
    if (!user) {
      return createErrorResponse('账号不存在，请先注册')
    }
    if (user.password !== password) {
      return createErrorResponse('密码错误，请重新输入')
    }

    const session: AuthSessionBody = {
      token: issueMockToken(user.id),
      user: toPublicUserProfile(user),
    }

    return createSuccessResponse(session, '登录成功')
  })

  registerMockApi('GET', '/api/user/auth/me', (payload) => {
    const token = String(payload?.token ?? '').trim()
    if (!token) {
      return createErrorResponse('缺少登录凭证', 401)
    }

    const userId = parseUserIdFromToken(token)
    if (!userId) {
      return createErrorResponse('登录凭证无效，请重新登录', 401)
    }

    const user = readAuthUsers().find((item) => item.id === userId)
    if (!user) {
      return createErrorResponse('用户不存在，请重新登录', 404)
    }

    return createSuccessResponse(toPublicUserProfile(user), '获取当前用户成功')
  })

  registerMockApi('POST', '/api/user/auth/logout', (payload) => {
    const token = String(payload?.token ?? '').trim()
    if (!token) {
      return createErrorResponse('缺少登录凭证', 401)
    }
    return createSuccessResponse(null, '退出成功')
  })
}

const initializeHomeMockApis = () => {
  registerMockApi('GET', '/api/user/home/overview', () => {
    return createSuccessResponse(createHomeOverview(), '获取首页数据成功')
  })

  registerMockApi('GET', '/api/user/home/cases', (payload) => {
    const limit = toCasesLimit(payload)
    return createSuccessResponse(homeCases.slice(0, limit), '获取案例列表成功')
  })
}

const initializeFeaturesMockApis = () => {
  registerMockApi('GET', '/api/user/features/overview', () => {
    return createSuccessResponse(createFeaturesOverview(), '获取 features 概览成功')
  })

  registerMockApi('GET', '/api/user/features/role-fit', () => {
    return createSuccessResponse(createFeaturesRoleFit(), '获取角色适配数据成功')
  })
}

/**
 * 注册 Mock API
 * 功能描述：将指定 method + url 映射到处理器，供前端 API 调用
 * 入参：method 请求方式、url 接口路径、handler 处理函数
 * 返回参数：无
 * url地址：Mock 内存注册表
 * 请求方式：GET/POST
 */
export const registerMockApi = (method: MockMethod, url: string, handler: MockHandler) => {
  mockApiRegistry.set(buildRegistryKey(method, url), handler)
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
 * 初始化 Mock API
 * 功能描述：注册 user 模块相关 Mock 接口
 * 入参：无
 * 返回参数：无
 * url地址：N/A
 * 请求方式：GET/POST
 */
export const initializeMockApis = () => {
  if (initialized) {
    return
  }

  ensureDefaultAuthUsers()
  initializeAuthMockApis()
  initializeHomeMockApis()
  initializeFeaturesMockApis()

  initialized = true
}

/**
 * 请求 Mock API
 * 功能描述：按 method + url 路由到已注册的 Mock 处理器
 * 入参：method 请求方式、url 接口路径、payload 请求参数
 * 返回参数：统一响应结构
 * url地址：已注册的 Mock URL
 * 请求方式：GET/POST
 */
export const requestMockApi = async (
  method: MockMethod,
  url: string,
  payload?: Record<string, unknown>,
): Promise<MockResponse> => {
  const handler = mockApiRegistry.get(buildRegistryKey(method, url))
  if (!handler) {
    return createErrorResponse(`未找到 Mock 接口：${method} ${url}`, 404)
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
