<template>
  <div class="mold-scrap-applications-container">
    <AppSidebar />

    <div class="layout-main">
      <header class="top-header">
        <div class="top-title">模具报废申请</div>
        <div class="top-subtitle">领用/归还之外的报废申请：申请原因、审批/处理人、时间线</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">报废申请列表</h3>
                <div class="card-header-actions">
                  <button type="button" class="primary-btn" @click="handleShowCreateDialog">
                    新建申请
                  </button>
                </div>
              </div>

              <div class="card-body">
                <div v-if="errorMessage" class="error-message">
                  {{ errorMessage }}
                </div>

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
                      <label>状态</label>
                      <select v-model="query.status" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">待审批</option>
                        <option :value="2">已批准</option>
                        <option :value="3">已拒绝</option>
                        <option :value="4">已执行</option>
                      </select>
                    </div>
                  </div>

                  <div class="query-actions">
                    <button type="button" class="primary-btn" @click="loadRecords">查询</button>
                    <button type="button" class="secondary-btn" @click="handleReset">重置</button>
                  </div>
                </div>

                <div class="table-wrapper">
                  <table class="mold-table">
                    <thead>
                      <tr>
                        <th>模具</th>
                        <th>申请原因</th>
                        <th>状态</th>
                        <th>申请人</th>
                        <th>审批/处理人</th>
                        <th>关键时间</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-if="records.length === 0">
                        <td colspan="7" class="empty-cell">暂无数据</td>
                      </tr>
                      <tr v-for="r in records" :key="r.id">
                        <td>
                          <div>{{ r.moldCode || '-' }}</div>
                          <div class="muted">{{ r.moldName || '-' }}</div>
                        </td>
                        <td class="cell-wrap">{{ r.reason || '-' }}</td>
                        <td>
                          <span class="status-normal" v-if="r.status === 4">已执行</span>
                          <span v-else class="status-warning">{{ r.statusDesc || '-' }}</span>
                        </td>
                        <td>{{ r.applicantName || '-' }}</td>
                        <td>
                          <div>{{ r.approverName || '-' }}</div>
                          <div class="muted">{{ r.handlerName || '-' }}</div>
                        </td>
                        <td>
                          <div>{{ formatDate(r.createdAt) }}</div>
                          <div class="muted" v-if="r.approvedAt">{{ formatDate(r.approvedAt) }}</div>
                          <div class="muted" v-if="r.handledAt">{{ formatDate(r.handledAt) }}</div>
                        </td>
                        <td>
                          <div class="action-buttons">
                            <button class="action-btn edit-btn" type="button" @click="openDetail(r.id)">
                              详情
                            </button>


                            <button
                              v-if="canExecute && r.status === 2"
                              class="action-btn edit-btn"
                              type="button"
                              @click="openExecute(r)"
                            >
                              执行报废
                            </button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

              </div>
            </div>
          </section>
        </div>
      </main>
    </div>

    <!-- 新建申请对话框 -->
    <div v-if="showCreateDialog" class="dialog-overlay" @click="closeCreateDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>新建报废申请</h3>
          <button class="dialog-close" @click="closeCreateDialog">×</button>
        </div>
        <div class="dialog-body">
          <form class="mold-form" @submit.prevent="handleCreate">
            <div class="form-row">
              <div class="form-group">
                <label>模具</label>
                <select v-model="createForm.moldId" class="form-input" required>
                  <option value="">请选择模具</option>
                  <option v-for="m in moldOptions" :key="m.id" :value="m.id">
                    {{ m.moldCode }} - {{ m.name }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label> </label>
                <div class="muted">创建后模具将进入“待报废”，禁止新增使用/保养</div>
              </div>
            </div>

            <div class="form-group">
              <label>申请原因 *</label>
              <textarea v-model="createForm.reason" class="form-input" rows="4" required placeholder="请输入报废原因"></textarea>
            </div>

            <div v-if="createDialogErrorMessage" class="error-message">
              {{ createDialogErrorMessage }}
            </div>

            <div class="dialog-actions">
              <button type="submit" class="submit-button" :disabled="createLoading">
                {{ createLoading ? '提交中...' : '提交申请' }}
              </button>
              <button type="button" class="cancel-button" @click="closeCreateDialog">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 审批对话框 -->
    <div v-if="showApprovalDialog" class="dialog-overlay" @click="closeApprovalDialog">
      <div class="dialog-content dialog-content-sm" @click.stop>
        <div class="dialog-header">
          <h3>报废申请审批</h3>
          <button class="dialog-close" @click="closeApprovalDialog">×</button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleApprovalSubmit">
            <div class="form-group">
              <label>审批结果</label>
              <select v-model.number="approvalForm.status" class="form-input">
                <option :value="2">通过</option>
                <option :value="3">拒绝</option>
              </select>
            </div>
            <div class="form-group">
              <label>审批意见</label>
              <input v-model="approvalForm.comment" class="form-input" type="text" placeholder="可选" />
            </div>
            <div v-if="approvalDialogError" class="error-message">
              {{ approvalDialogError }}
            </div>
            <div class="dialog-actions">
              <button type="submit" class="submit-button" :disabled="approvalLoading">
                {{ approvalLoading ? '提交中...' : '提交' }}
              </button>
              <button type="button" class="cancel-button" @click="closeApprovalDialog">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 执行对话框 -->
    <div v-if="showExecuteDialog" class="dialog-overlay" @click="closeExecuteDialog">
      <div class="dialog-content dialog-content-sm" @click.stop>
        <div class="dialog-header">
          <h3>执行报废</h3>
          <button class="dialog-close" @click="closeExecuteDialog">×</button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleExecuteSubmit">
            <div class="form-group">
              <label>执行备注/处理意见</label>
              <input v-model="executeForm.comment" class="form-input" type="text" placeholder="可选" />
            </div>
            <div v-if="executeDialogError" class="error-message">
              {{ executeDialogError }}
            </div>
            <div class="dialog-actions">
              <button type="submit" class="submit-button" :disabled="executeLoading">
                {{ executeLoading ? '提交中...' : '确认执行' }}
              </button>
              <button type="button" class="cancel-button" @click="closeExecuteDialog">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click="closeDetailDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>报废申请详情</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body" v-if="detail">
          <div class="form-row">
            <div class="form-group">
              <label>模具</label>
              <div class="form-input readonly-text">{{ detail.application.moldCode || '-' }} - {{ detail.application.moldName || '-' }}</div>
            </div>
            <div class="form-group">
              <label>状态</label>
              <div class="form-input readonly-text">{{ detail.application.statusDesc || '-' }}</div>
            </div>
          </div>

          <div class="form-group">
            <label>申请原因</label>
            <div class="form-input readonly-text cell-wrap">{{ detail.application.reason || '-' }}</div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>申请人</label>
              <div class="form-input readonly-text">{{ detail.application.applicantName || '-' }}</div>
            </div>
            <div class="form-group">
              <label>审批人</label>
              <div class="form-input readonly-text">{{ detail.application.approverName || '-' }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>处理人</label>
              <div class="form-input readonly-text">{{ detail.application.handlerName || '-' }}</div>
            </div>
            <div class="form-group">
              <label>关键时间线</label>
              <div class="form-input readonly-text">
                申请：{{ formatDate(detail.application.createdAt) }}<br />
                审批：{{ formatDate(detail.application.approvedAt) }}<br />
                执行：{{ formatDate(detail.application.handledAt) }}
              </div>
            </div>
          </div>

          <div class="form-section-title">时间线</div>
          <div class="timeline-list">
            <div v-for="t in detail.timeline" :key="t.eventType + t.eventTime" class="timeline-item">
              <div class="timeline-title">{{ t.eventTypeDesc || '-' }}</div>
              <div class="timeline-meta">
                {{ t.operatorName || '-' }} / {{ formatDate(t.eventTime) }}
              </div>
              <div v-if="t.comment" class="timeline-comment">{{ t.comment }}</div>
            </div>
          </div>

          <div class="dialog-actions">
            <button type="button" class="cancel-button" @click="closeDetailDialog">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/AppSidebar.vue'
import { LIST_PAGE_SIZE_MAX } from '@/constants/appConfig'
import { fetchMolds } from '@/api/molds'
import {
  approveMoldScrapApplication,
  createMoldScrapApplication,
  executeMoldScrapApplication,
  fetchMoldScrapApplicationDetail,
  fetchMoldScrapApplications,
} from '@/api/moldScrapApplications'

const router = useRouter()
const authStore = useAuthStore()

const records = ref([])
const errorMessage = ref('')

const moldOptions = ref([])
const moldOptionsLoaded = ref(false)

const query = reactive({
  moldId: '',
  status: null,
})

const showCreateDialog = ref(false)
const createLoading = ref(false)
const createDialogErrorMessage = ref('')
const createForm = reactive({
  moldId: '',
  reason: '',
})

const showApprovalDialog = ref(false)
const approvalLoading = ref(false)
const approvalDialogError = ref('')
const approvalForm = reactive({
  id: '',
  status: 2,
  comment: '',
})

const showExecuteDialog = ref(false)
const executeLoading = ref(false)
const executeDialogError = ref('')
const executeForm = reactive({
  id: '',
  comment: '',
})

const showDetailDialog = ref(false)
const detail = ref(null)

const isAdmin = computed(() => authStore.role === 'ADMIN')
const canExecute = computed(() => authStore.role === 'ADMIN' || authStore.role === 'INSPECTOR')

const formatDate = (val) => {
  if (!val) return '-'
  const date = new Date(typeof val === 'string' ? val.replace(' ', 'T') : val)
  if (Number.isNaN(date.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  const y = date.getFullYear()
  const M = pad(date.getMonth() + 1)
  const d = pad(date.getDate())
  const h = pad(date.getHours())
  const m = pad(date.getMinutes())
  return `${y}/${M}/${d} ${h}:${m}`
}

const loadMoldOptions = async () => {
  if (moldOptionsLoaded.value) return
  const res = await fetchMolds(1, LIST_PAGE_SIZE_MAX)
  moldOptions.value = res.data?.list || []
  moldOptionsLoaded.value = true
}

const loadRecords = async () => {
  errorMessage.value = ''
  try {
    const params = {}
    if (query.moldId) params.moldId = query.moldId
    if (query.status != null) params.status = query.status
    const res = await fetchMoldScrapApplications(params)
    records.value = res.data || []
  } catch (e) {
    errorMessage.value = e.message || '加载报废申请失败'
  }
}

const handleReset = () => {
  query.moldId = ''
  query.status = null
  loadRecords()
}

const handleShowCreateDialog = async () => {
  await loadMoldOptions()
  createForm.moldId = ''
  createForm.reason = ''
  createDialogErrorMessage.value = ''
  showCreateDialog.value = true
}

const closeCreateDialog = () => {
  showCreateDialog.value = false
}

const handleCreate = async () => {
  createDialogErrorMessage.value = ''
  if (!createForm.moldId) {
    createDialogErrorMessage.value = '请选择模具'
    return
  }
  if (!createForm.reason || !String(createForm.reason).trim()) {
    createDialogErrorMessage.value = '申请原因不能为空'
    return
  }
  createLoading.value = true
  try {
    await createMoldScrapApplication({
      moldId: createForm.moldId,
      reason: createForm.reason,
    })
    showCreateDialog.value = false
    await loadRecords()
  } catch (e) {
    createDialogErrorMessage.value = e.message || '提交失败'
  } finally {
    createLoading.value = false
  }
}

const openApproval = (r) => {
  approvalForm.id = r.id
  approvalForm.status = 2
  approvalForm.comment = ''
  approvalDialogError.value = ''
  showApprovalDialog.value = true
}

const closeApprovalDialog = () => {
  showApprovalDialog.value = false
}

const handleApprovalSubmit = async () => {
  approvalDialogError.value = ''
  approvalLoading.value = true
  try {
    await approveMoldScrapApplication(approvalForm.id, {
      status: approvalForm.status,
      comment: approvalForm.comment || null,
    })
    showApprovalDialog.value = false
    await loadRecords()
  } catch (e) {
    approvalDialogError.value = e.message || '审批失败'
  } finally {
    approvalLoading.value = false
  }
}

const openExecute = (r) => {
  executeForm.id = r.id
  executeForm.comment = ''
  executeDialogError.value = ''
  showExecuteDialog.value = true
}

const closeExecuteDialog = () => {
  showExecuteDialog.value = false
}

const handleExecuteSubmit = async () => {
  executeDialogError.value = ''
  executeLoading.value = true
  try {
    await executeMoldScrapApplication(executeForm.id, {
      comment: executeForm.comment || null,
    })
    showExecuteDialog.value = false
    await loadRecords()
  } catch (e) {
    executeDialogError.value = e.message || '执行失败'
  } finally {
    executeLoading.value = false
  }
}

const openDetail = async (id) => {
  try {
    const res = await fetchMoldScrapApplicationDetail(id)
    detail.value = res.data
    showDetailDialog.value = true
  } catch (e) {
    errorMessage.value = e.message || '加载详情失败'
  }
}

const closeDetailDialog = () => {
  showDetailDialog.value = false
  detail.value = null
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await loadMoldOptions()
  await loadRecords()
})
</script>

<style scoped>
.mold-scrap-applications-container {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  flex-direction: row;
}
.layout-main {
  flex: 1;
  padding: 24px;
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
  max-width: 1300px;
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
.card-header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.card-body {
  padding: 24px;
}
.primary-btn {
  padding: 10px 18px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}
.secondary-btn {
  padding: 10px 18px;
  background: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}
.error-message {
  margin-bottom: 12px;
  color: #991b1b;
  background: #fee2e2;
  border: 1px solid #fecaca;
  padding: 10px 12px;
  border-radius: 6px;
}
.query-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}
.query-row {
  display: flex;
  gap: 16px;
  align-items: flex-end;
}
.query-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 220px;
}
label {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}
.form-input {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: #ffffff;
}
.query-actions {
  display: flex;
  gap: 12px;
  margin-top: 14px;
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
  padding: 10px 10px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}
.mold-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}
.empty-cell {
  text-align: center;
  color: #9ca3af;
}
.muted {
  color: #6b7280;
  font-size: 12px;
  margin-top: 2px;
}
.cell-wrap {
  max-width: 240px;
  white-space: normal;
  word-break: break-word;
}
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.action-btn {
  padding: 6px 10px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.edit-btn {
  background: #3b82f6;
  color: #ffffff;
}
.status-btn {
  background: #e5e7eb;
  color: #111827;
}
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.dialog-content {
  background: #ffffff;
  border-radius: 12px;
  width: 92%;
  max-width: 920px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
}
.dialog-content-sm {
  max-width: 520px;
}
.dialog-header {
  padding: 18px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}
.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}
.dialog-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  cursor: pointer;
  font-size: 20px;
}
.dialog-body {
  padding: 20px 22px;
  overflow-y: auto;
}
.mold-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.dialog-actions {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}
.submit-button {
  flex: 1;
  padding: 10px 14px;
  background: #1e3c72;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.cancel-button {
  flex: 1;
  padding: 10px 14px;
  background: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
}
.readonly-text {
  background: #f9fafb;
}
.form-section-title {
  margin-top: 10px;
  font-weight: 600;
  color: #4b5563;
  font-size: 14px;
}
.timeline-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.timeline-item {
  padding: 12px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}
.timeline-title {
  font-weight: 600;
  color: #111827;
}
.timeline-meta {
  margin-top: 6px;
  color: #6b7280;
  font-size: 12px;
}
.timeline-comment {
  margin-top: 6px;
  color: #374151;
  word-break: break-word;
}

.status-normal {
  color: #10b981;
  font-weight: 500;
}

.status-warning {
  color: #f59e0b;
  font-weight: 500;
}
</style>

