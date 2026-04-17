<template>
  <section class="user-page">
    <header class="user-toolbar">
      <div>
        <h2 class="user-title">用户管理</h2>
        <p class="user-subtitle">统一管理管理端账户，支持筛选检索、创建、编辑、启用/禁用与密码重置。</p>
      </div>
      <div class="user-toolbar__actions">
        <a-button :loading="pageState.refreshing" @click="onRefreshClick">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button type="primary" @click="onOpenCreateModal">新建用户</a-button>
      </div>
    </header>

    <a-alert v-if="pageState.errorMessage" class="user-error" type="error" show-icon :message="pageState.errorMessage">
      <template #action>
        <a-button size="small" @click="onRetryClick">重试</a-button>
      </template>
    </a-alert>

    <a-card class="user-card" :bordered="false">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :xs="24" :md="12" :lg="8">
            <a-form-item label="关键词">
              <a-input
                v-model:value="filters.keyword"
                allow-clear
                placeholder="输入用户名或邮箱"
                @pressEnter="onSearchClick"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
            <a-form-item label="角色">
              <a-select v-model:value="filters.role" :options="roleFilterOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="6" :lg="4">
            <a-form-item label="状态">
              <a-select v-model:value="filters.status" :options="statusFilterOptions" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :lg="8">
            <a-form-item label=" ">
              <a-space>
                <a-button type="primary" :loading="pageState.refreshing" @click="onSearchClick">查询</a-button>
                <a-button @click="onResetClick">重置</a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <a-card class="user-card" :bordered="false">
      <a-skeleton v-if="pageState.loadingInitial && !pageState.hasData" active :paragraph="{ rows: 6 }" />
      <template v-else>
        <a-table
          :columns="tableColumns"
          :data-source="pageState.records"
          :loading="tableLoading"
          :pagination="false"
          row-key="id"
          :scroll="{ x: 920 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'role'">
              <a-tag :color="resolveRoleMeta(record.role).color">
                {{ resolveRoleMeta(record.role).label }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'ACTIVE' ? 'success' : 'default'">
                {{ record.status === 'ACTIVE' ? '启用' : '禁用' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'createdTime'">
              {{ formatDateTime(record.createdTime) }}
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="onOpenEditModal(record)">编辑</a-button>
                <a-button type="link" size="small" :loading="pageState.submitting" @click="onToggleStatus(record)">
                  {{ record.status === 'ACTIVE' ? '禁用' : '启用' }}
                </a-button>
                <a-button type="link" size="small" @click="onOpenPasswordModal(record)">重置密码</a-button>
              </a-space>
            </template>
          </template>
        </a-table>

        <div class="user-pagination">
          <a-pagination
            :current="pageState.page"
            :page-size="pageState.pageSize"
            :total="pageState.total"
            :show-total="(total: number) => `共 ${total} 条`"
            show-size-changer
            :page-size-options="['10', '20', '50']"
            @change="onPageChange"
          />
        </div>
      </template>
    </a-card>

    <a-modal
      v-model:open="userModal.open"
      :title="userModal.mode === 'create' ? '新建用户' : '编辑用户'"
      :confirm-loading="pageState.submitting"
      @ok="onSubmitUser"
    >
      <a-form layout="vertical">
        <a-form-item label="用户名" required>
          <a-input v-model:value="userModal.form.username" :maxlength="30" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="邮箱" required>
          <a-input v-model:value="userModal.form.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="角色" required>
          <a-select v-model:value="userModal.form.role" :options="roleFormOptions" />
        </a-form-item>
        <a-form-item label="状态" required>
          <a-select v-model:value="userModal.form.status" :options="statusFormOptions" />
        </a-form-item>
        <a-form-item v-if="userModal.mode === 'create'" label="初始密码" required>
          <a-input-password
            v-model:value="userModal.form.password"
            :maxlength="10"
            placeholder="请输入 6-10 位密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="passwordModal.open" title="重置密码" :confirm-loading="pageState.submitting" @ok="onSubmitPassword">
      <a-form layout="vertical">
        <a-form-item label="用户">
          <a-input :value="passwordModal.username" disabled />
        </a-form-item>
        <a-form-item label="新密码" required>
          <a-input-password
            v-model:value="passwordModal.password"
            :maxlength="10"
            placeholder="请输入 6-10 位新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import {
  createAdminUserApi,
  getAdminUserDetailApi,
  getAdminUserPageApi,
  resetAdminUserPasswordApi,
  updateAdminUserApi,
  updateAdminUserStatusApi,
  type AdminUserCreatePayload,
  type AdminUserItem,
  type AdminUserRole,
  type AdminUserStatus,
  type AdminUserUpdatePayload,
} from '../../api/admin/UserApi'

type UserModalMode = 'create' | 'edit'

// 页面状态
const pageState = reactive({
  records: [] as AdminUserItem[],
  total: 0,
  page: 1,
  pageSize: 10,
  loadingInitial: false,
  refreshing: false,
  submitting: false,
  errorMessage: '',
  hasData: computed(() => pageState.records.length > 0),
})

// 筛选条件
const filters = reactive({
  keyword: '',
  role: '' as '' | AdminUserRole,
  status: '' as '' | AdminUserStatus,
})

// 用户弹窗
const userModal = reactive({
  open: false,
  mode: 'create' as UserModalMode,
  editingId: '',
  form: {
    username: '',
    email: '',
    role: 'user' as AdminUserRole,
    status: 'ACTIVE' as AdminUserStatus,
    password: '',
  },
})

// 密码弹窗
const passwordModal = reactive({
  open: false,
  userId: '',
  username: '',
  password: '',
})

const tableColumns = [
  { title: '用户名', dataIndex: 'username', key: 'username', width: 150 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 240 },
  { title: '角色', dataIndex: 'role', key: 'role', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime', width: 190 },
  { title: '操作', key: 'actions', width: 220 },
]

const ROLE_META: Record<AdminUserRole, { label: string; color: string }> = {
  super_admin: { label: '超管', color: 'red' },
  admin: { label: '管理员', color: 'processing' },
  user: { label: '用户', color: 'default' },
}

const resolveRoleMeta = (role: AdminUserRole) => ROLE_META[role]

const roleFilterOptions: Array<{ label: string; value: '' | AdminUserRole }> = [
  { label: '全部角色', value: '' },
  { label: '超管', value: 'super_admin' },
  { label: '管理员', value: 'admin' },
  { label: '用户', value: 'user' },
]

const statusFilterOptions: Array<{ label: string; value: '' | AdminUserStatus }> = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '禁用', value: 'INACTIVE' },
]

const roleFormOptions: Array<{ label: string; value: AdminUserRole }> = [
  { label: '超管', value: 'super_admin' },
  { label: '管理员', value: 'admin' },
  { label: '用户', value: 'user' },
]

const statusFormOptions: Array<{ label: string; value: AdminUserStatus }> = [
  { label: '启用', value: 'ACTIVE' },
  { label: '禁用', value: 'INACTIVE' },
]

const tableLoading = computed(() => pageState.loadingInitial || pageState.refreshing)

const isValidEmail = (email: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)

const formatDateTime = (value: string) => {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const resetUserForm = () => {
  userModal.form.username = ''
  userModal.form.email = ''
  userModal.form.role = 'user'
  userModal.form.status = 'ACTIVE'
  userModal.form.password = ''
}

const validateUserForm = () => {
  const username = userModal.form.username.trim()
  const email = userModal.form.email.trim()

  if (!username) {
    message.warning('请输入用户名')
    return false
  }
  if (!email || !isValidEmail(email)) {
    message.warning('请输入合法邮箱')
    return false
  }
  if (userModal.mode === 'create') {
    const password = userModal.form.password.trim()
    if (!password || password.length < 6 || password.length > 10) {
      message.warning('初始密码长度需为 6-10 位')
      return false
    }
  }
  return true
}

// 获取列表
const fetchList = async (mode: 'initial' | 'refresh' = 'refresh') => {
  pageState.errorMessage = ''
  if (mode === 'initial') pageState.loadingInitial = true
  if (mode === 'refresh') pageState.refreshing = true

  try {
    const data = await getAdminUserPageApi({
      page: pageState.page,
      pageSize: pageState.pageSize,
      keyword: filters.keyword || undefined,
      role: filters.role || undefined,
      status: filters.status || undefined,
    })
    pageState.records = data.records
    pageState.total = data.total
    pageState.page = data.page
    pageState.pageSize = data.pageSize

    // 自动翻页处理
    if (pageState.total > 0 && pageState.records.length === 0 && pageState.page > 1) {
      pageState.page -= 1
      await fetchList('refresh')
    }
  } catch (error) {
    pageState.errorMessage = error instanceof Error ? error.message : '用户列表加载失败，请稍后重试'
  } finally {
    pageState.loadingInitial = false
    pageState.refreshing = false
  }
}

const onRefreshClick = async () => {
  await fetchList('refresh')
  if (!pageState.errorMessage) {
    message.success('用户列表已刷新')
  }
}

const onRetryClick = async () => {
  await fetchList('refresh')
}

const onSearchClick = async () => {
  pageState.page = 1
  await fetchList('refresh')
}

const onResetClick = async () => {
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  pageState.page = 1
  await fetchList('refresh')
}

const onPageChange = async (page: number, pageSize: number) => {
  pageState.page = page
  if (pageSize > 0) pageState.pageSize = pageSize
  await fetchList('refresh')
}

const onOpenCreateModal = () => {
  userModal.mode = 'create'
  userModal.editingId = ''
  resetUserForm()
  userModal.open = true
}

const onOpenEditModal = async (record: AdminUserItem) => {
  try {
    const detail = await getAdminUserDetailApi(record.id)
    userModal.mode = 'edit'
    userModal.editingId = detail.id
    userModal.form.username = detail.username
    userModal.form.email = detail.email
    userModal.form.role = detail.role
    userModal.form.status = detail.status
    userModal.form.password = ''
    userModal.open = true
  } catch (error) {
    message.error(error instanceof Error ? error.message : '用户详情加载失败，请稍后重试')
  }
}

const onSubmitUser = async () => {
  if (!validateUserForm()) return

  try {
    pageState.submitting = true
    if (userModal.mode === 'create') {
      await createAdminUserApi({
        username: userModal.form.username.trim(),
        email: userModal.form.email.trim(),
        role: userModal.form.role,
        password: userModal.form.password.trim(),
        status: userModal.form.status,
      })
      message.success('用户创建成功')
    } else {
      await updateAdminUserApi(userModal.editingId, {
        username: userModal.form.username.trim(),
        email: userModal.form.email.trim(),
        role: userModal.form.role,
        status: userModal.form.status,
      })
      message.success('用户信息已更新')
    }
    userModal.open = false
    await fetchList('refresh')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '提交失败，请稍后重试')
  } finally {
    pageState.submitting = false
  }
}

const onToggleStatus = (record: AdminUserItem) => {
  const nextStatus: AdminUserStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const nextText = nextStatus === 'ACTIVE' ? '启用' : '禁用'

  Modal.confirm({
    title: `确认${nextText}该用户？`,
    content: `用户：${record.username}`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await updateAdminUserStatusApi(record.id, nextStatus)
        await fetchList('refresh')
        message.success(`用户已${nextText}`)
      } catch (error) {
        message.error(error instanceof Error ? error.message : '状态更新失败，请稍后重试')
      }
    },
  })
}

const onOpenPasswordModal = (record: AdminUserItem) => {
  passwordModal.open = true
  passwordModal.userId = record.id
  passwordModal.username = record.username
  passwordModal.password = ''
}

const onSubmitPassword = async () => {
  const password = passwordModal.password.trim()
  if (password.length < 6 || password.length > 10) {
    message.warning('新密码长度需为 6-10 位')
    return
  }

  try {
    pageState.submitting = true
    await resetAdminUserPasswordApi(passwordModal.userId, password)
    passwordModal.open = false
    message.success('密码重置成功')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '密码重置失败，请稍后重试')
  } finally {
    pageState.submitting = false
  }
}

onMounted(() => {
  void fetchList('initial')
})
</script>

<style scoped>
.user-page {
  display: grid;
  gap: 16px;
}

.user-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(130deg, #eef2ff 0%, #f5f3ff 56%, #f8fafc 100%);
  border: 1px solid #dce6f0;
}

.user-title {
  margin: 0;
  font-size: 24px;
  color: #312e81;
}

.user-subtitle {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.user-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-error {
  margin-top: -6px;
}

.user-card {
  border-radius: 14px;
  border: 1px solid #dce6f0;
}

.user-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1100px) {
  .user-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .user-toolbar__actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .user-pagination {
    justify-content: center;
  }
}
</style>
