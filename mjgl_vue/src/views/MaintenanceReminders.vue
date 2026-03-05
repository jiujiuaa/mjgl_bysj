<template>
  <div class="maintenance-reminders-container">
    <!-- 全局左侧边栏 -->
    <AppSidebar />

    <!-- 右侧主区域 -->
    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">保养智能提醒</div>
        <div class="top-subtitle">查看各模具的下一次保养提醒与处理状态</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">保养提醒列表</h3>
              </div>
              <div class="card-body">
                <div v-if="successMessage" class="success-message">
                  <span class="message-icon">✓</span>
                  {{ successMessage }}
                </div>
                <div v-if="errorMessage" class="error-message">
                  <span class="message-icon">⚠</span>
                  {{ errorMessage }}
                </div>

                <!-- 查询条件 -->
                <div class="query-form">
                  <div class="query-row">
                    <div class="query-item">
                      <label>选择模具</label>
                      <select v-model="query.moldId" class="form-input query-input">
                        <option value="">全部</option>
                        <option v-for="m in moldOptions" :key="m.id" :value="m.id">
                          {{ (m.moldCode || '') + (m.name ? ` - ${m.name}` : '') || m.id }}
                        </option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>模具名称/编号</label>
                      <input
                        v-model="query.keyword"
                        type="text"
                        class="form-input query-input"
                        placeholder="支持名称或编号模糊匹配"
                      />
                    </div>
                    <div class="query-item">
                      <label>提醒类型</label>
                      <select v-model="query.reminderType" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">按时间周期</option>
                        <option :value="2">按使用次数</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>状态</label>
                      <select v-model="query.status" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">待处理</option>
                        <option :value="2">已提醒</option>
                        <option :value="3">已完成</option>
                        <option :value="4">已忽略</option>
                      </select>
                    </div>
                  </div>
                  <div class="query-actions">
                    <button type="button" class="primary-btn" @click="handleQuery">
                      查询
                    </button>
                    <button type="button" class="secondary-btn" @click="handleReset">
                      重置
                    </button>
                  </div>
                </div>

                <div v-if="listLoading" class="table-loading">保养提醒加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th>模具编号</th>
                          <th>模具名称</th>
                          <th>保养计划</th>
                          <th>提醒类型</th>
                          <th>周期/次数</th>
                          <th>下次保养日期</th>
                          <th>下次保养模次</th>
                          <th>状态</th>
                          <th>自上次保养后的提醒次数</th>
                          <th>最后推送时间</th>
                          <th>处理人</th>
                          <th>备注</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!page.list || page.list.length === 0">
                          <td colspan="12" class="empty-cell">暂无保养提醒</td>
                        </tr>
                        <tr v-for="item in page.list" :key="item.id">
                          <td>{{ item.moldCode || '-' }}</td>
                          <td>{{ item.moldName || '-' }}</td>
                          <td>{{ item.planName || '-' }}</td>
                          <td>{{ formatReminderType(item.reminderType) }}</td>
                          <td>
                            <span v-if="item.intervalValue != null">
                              {{ item.intervalValue }}
                              <span v-if="item.reminderType === 1">天</span>
                              <span v-else-if="item.reminderType === 2">模次</span>
                            </span>
                            <span v-else>-</span>
                          </td>
                          <td>{{ formatDate(item.nextDueDate) }}</td>
                          <td>{{ item.nextDueCycles ?? '-' }}</td>
                          <td>
                            <span :class="getStatusClass(item.status)">
                              {{ item.statusDesc || formatStatus(item.status) }}
                            </span>
                          </td>
                          <td>
                            {{ (item.reminderCount ?? 0) + ' 次' }}
                          </td>
                          <td>{{ formatDate(item.sentAt) }}</td>
                          <td>{{ item.operatorName || '-' }}</td>
                          <td>{{ item.notes || '-' }}</td>
                          <td class="col-actions">
                            <div
                              v-if="isAdmin && (item.status === 1 || item.status === 2)"
                              class="action-btns"
                            >
                              <button
                                type="button"
                                class="btn-action btn-send"
                                @click="handleSendReminder(item)"
                              >
                                发送提醒
                              </button>
                              <button
                                type="button"
                                class="btn-action btn-ignore"
                                @click="handleIgnoreReminder(item)"
                              >
                                忽略
                              </button>
                            </div>
                            <span v-else class="action-placeholder">—</span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div v-if="page.pages && page.pages > 1" class="pagination">
                    <button
                      class="page-btn"
                      :disabled="pageNum === 1"
                      @click="changePage(pageNum - 1)"
                    >
                      上一页
                    </button>
                    <span class="page-info">
                      第 {{ pageNum }} / {{ page.pages }} 页，共 {{ page.total || 0 }} 条
                    </span>
                    <button
                      class="page-btn"
                      :disabled="pageNum === page.pages"
                      @click="changePage(pageNum + 1)"
                    >
                      下一页
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/AppSidebar.vue'
import { queryMaintenanceReminders, sendMaintenanceReminder, ignoreMaintenanceReminder } from '@/api/maintenanceReminders'
import { useMoldOptions } from '@/composables/useMoldOptions'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const showOpsChildren = ref(true)
const showMonitoringChildren = ref(true)

const listLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const pageNum = ref(1)
const pageSize = ref(10)
const page = reactive({
  list: [],
  total: 0,
  pages: 0,
})

const { moldOptions } = useMoldOptions()

const query = reactive({
  moldId: '',
  keyword: '',
  reminderType: null,
  status: null,
})

const isAdmin = computed(() => authStore.role === 'ADMIN')

const buildQueryParam = () => {
  const normalize = (v) =>
    v === null || v === undefined || v === '' ? null : v
  const keywordByMold = query.moldId
    ? (moldOptions.value.find((m) => m.id === query.moldId)?.moldCode ?? query.keyword?.trim())
    : query.keyword?.trim()
  return {
    keyword: keywordByMold || null,
    reminderType: normalize(query.reminderType),
    status: normalize(query.status),
  }
}

const loadReminders = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const res = await queryMaintenanceReminders(
      buildQueryParam(),
      pageNum.value,
      pageSize.value,
    )
    const data = res.data || {}
    page.list = data.list ?? []
    page.total = data.total ?? 0
    page.pages = data.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载保养提醒失败'
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  loadReminders()
}

const handleReset = () => {
  query.moldId = ''
  query.keyword = ''
  query.reminderType = null
  query.status = null
  pageNum.value = 1
  loadReminders()
}

const changePage = (newPage) => {
  if (newPage < 1 || (page.pages && newPage > page.pages)) return
  pageNum.value = newPage
  loadReminders()
}

const handleSendReminder = async (item) => {
  if (!item || !item.id) return
  const ok = window.confirm(`确定要向管理员发送该保养提醒吗？\n模具：${item.moldCode || ''} ${item.moldName || ''}\n计划：${item.planName || ''}`)
  if (!ok) return
  try {
    await sendMaintenanceReminder(item.id)
    successMessage.value = '保养提醒已发送'
    setTimeout(() => { successMessage.value = '' }, 3000)
    loadReminders()
  } catch (e) {
    errorMessage.value = e.message || '发送保养提醒失败'
  }
}

const handleIgnoreReminder = async (item) => {
  if (!item || !item.id) return
  const ok = window.confirm(`确定要忽略该保养提醒吗？忽略后定时任务将不再处理。\n模具：${item.moldCode || ''} ${item.moldName || ''}\n计划：${item.planName || ''}`)
  if (!ok) return
  try {
    await ignoreMaintenanceReminder(item.id)
    successMessage.value = '已忽略'
    setTimeout(() => { successMessage.value = '' }, 3000)
    loadReminders()
  } catch (e) {
    errorMessage.value = e.message || '忽略失败'
  }
}

const formatDate = (val) => {
  if (!val) return '-'
  if (typeof val === 'string' && val.includes(' ')) {
    return val
  }
  const date = new Date(val)
  if (Number.isNaN(date.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  const y = date.getFullYear()
  const M = pad(date.getMonth() + 1)
  const d = pad(date.getDate())
  const h = pad(date.getHours())
  const m = pad(date.getMinutes())
  return `${y}/${M}/${d} ${h}:${m}`
}

const formatReminderType = (t) => {
  if (t === 1) return '按时间周期'
  if (t === 2) return '按使用次数'
  return '-'
}

const formatStatus = (s) => {
  if (s === 1) return '待处理'
  if (s === 2) return '已提醒'
  if (s === 3) return '已完成'
  if (s === 4) return '已忽略'
  return '-'
}

const getStatusClass = (s) => {
  if (s === 1) return 'status-warning'
  if (s === 2) return 'status-info'
  if (s === 3) return 'status-normal'
  if (s === 4) return 'status-muted'
  return ''
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const goUserManagement = () => {
  router.push('/user-management')
}

const goMoldManagement = () => {
  router.push('/mold-management')
}

const goUseRecords = () => {
  router.push('/mold-use-records')
}

const goRepairRecords = () => {
  router.push('/repair-records')
}

const goMaintenancePlans = () => {
  router.push('/maintenance-plans')
}

const goMaintenanceLogs = () => {
  router.push('/maintenance-logs')
}

const goMaintenanceReminders = () => {
  router.push('/maintenance-reminders')
}

const goMonitoringManual = () => {
  router.push('/monitoring-manual')
}

onMounted(() => {
  if (route.query.moldKeyword) {
    query.keyword = String(route.query.moldKeyword)
  }
  loadReminders()
})
</script>

<style scoped>
.maintenance-reminders-container {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  flex-direction: row;
}

.sidebar {
  width: 220px;
  background: #1e3c72;
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
}

.sidebar-header {
  padding: 12px 8px 20px;
}

.sidebar-logo {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.sidebar-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: #e5e7eb;
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.parent-item {
  font-weight: 600;
  justify-content: flex-start;
}

.child-item {
  padding-left: 28px;
  font-size: 13px;
}

.submenu-arrow {
  font-size: 12px;
}

.menu-item .menu-icon {
  width: 18px;
  text-align: center;
}

.menu-item.active {
  background: #2563eb;
}

.menu-item:not(.active):hover {
  background: rgba(148, 163, 184, 0.25);
}

.menu-item.disabled {
  opacity: 0.6;
  cursor: default;
}

.sidebar-footer {
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.4);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-username {
  font-size: 13px;
}

.sidebar-logout {
  padding: 6px 10px;
  background: transparent;
  color: #e5e7eb;
  border-radius: 4px;
  border: 1px solid rgba(148, 163, 184, 0.8);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.sidebar-logout:hover {
  background: rgba(148, 163, 184, 0.3);
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.top-header {
  padding: 16px 24px 8px;
}

.top-title {
  font-size: 22px;
  font-weight: 600;
  color: #111827;
}

.top-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.main-content {
  flex: 1;
  padding: 24px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.content-wrapper {
  width: 100%;
}

.card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.card-body {
  padding: 24px;
}

.query-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.query-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 20px;
  align-items: flex-end;
}

.query-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 140px;
  flex: 1;
  max-width: 220px;
}

.query-item label {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.query-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.query-input:focus {
  outline: none;
  border-color: #2a5298;
  box-shadow: 0 0 0 2px rgba(42, 82, 152, 0.15);
}

.query-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.primary-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(30, 60, 114, 0.2);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.3);
}

.secondary-btn {
  padding: 10px 20px;
  background: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary-btn:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.success-message,
.error-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
}

.success-message {
  color: #065f46;
  background: #d1fae5;
  border: 1px solid #a7f3d0;
}

.error-message {
  color: #991b1b;
  background: #fee2e2;
  border: 1px solid #fecaca;
}

.message-icon {
  font-size: 16px;
  font-weight: bold;
}

.table-loading {
  text-align: center;
  padding: 20px 0;
  color: #6b7280;
  font-size: 14px;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.mold-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.mold-table th,
.mold-table td {
  padding: 8px 10px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

.mold-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.mold-table tbody tr:hover {
  background: #f3f4f6;
}

.empty-cell {
  text-align: center;
  color: #9ca3af;
}

.col-actions {
  white-space: nowrap;
  vertical-align: middle;
}

.action-btns {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.btn-action {
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
  border: 1px solid transparent;
  white-space: nowrap;
}

.btn-send {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
  border-color: #1e3c72;
  box-shadow: 0 1px 3px rgba(30, 60, 114, 0.25);
}

.btn-send:hover {
  background: linear-gradient(135deg, #2a5298 0%, #3d6cb8 100%);
  box-shadow: 0 2px 6px rgba(30, 60, 114, 0.35);
}

.btn-ignore {
  background: #fff;
  color: #6b7280;
  border-color: #d1d5db;
}

.btn-ignore:hover {
  background: #f9fafb;
  color: #374151;
  border-color: #9ca3af;
}

.action-placeholder {
  color: #9ca3af;
  font-size: 13px;
}

.status-normal {
  color: #166534;
  background: #dcfce7;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
}

.status-warning {
  color: #92400e;
  background: #fef3c7;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
}

.status-muted {
  color: #4b5563;
  background: #e5e7eb;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
}

@media (max-width: 768px) {
  .maintenance-reminders-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .sidebar-menu {
    flex-direction: row;
  }

  .main-content {
    padding: 16px;
  }

  .card {
    margin-bottom: 16px;
  }
}
</style>
