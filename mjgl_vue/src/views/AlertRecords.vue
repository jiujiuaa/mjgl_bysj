<template>
  <div class="alert-records-container">
    <AppSidebar />

    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">报警管理</div>
        <div class="top-subtitle">智能预警与报警状态闭环（活跃 / 已解决 / 已忽略）</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">报警记录列表</h3>
                <button
                  type="button"
                  class="secondary-btn"
                  :disabled="runRulesLoading"
                  @click="handleRunRules"
                >
                  {{ runRulesLoading ? '执行中...' : '执行规则引擎' }}
                </button>
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
                        placeholder="支持模糊匹配"
                      />
                    </div>
                    <div class="query-item">
                      <label>报警类型</label>
                      <select v-model="query.alertType" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">故障频发</option>
                        <option :value="2">保养超期</option>
                        <option :value="3">温度异常</option>
                        <option :value="4">润滑异常</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>状态</label>
                      <select v-model="query.status" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">活跃</option>
                        <option :value="2">已解决</option>
                        <option :value="3">已忽略</option>
                      </select>
                    </div>
                    <div class="query-actions">
                      <button type="button" class="primary-btn" @click="handleQuery">查询</button>
                      <button type="button" class="secondary-btn" @click="handleReset">重置</button>
                    </div>
                  </div>
                </div>

                <div v-if="listLoading" class="table-loading">加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th>模具编号</th>
                          <th>模具名称</th>
                          <th>报警类型</th>
                          <th>触发条件</th>
                          <th>提示消息</th>
                          <th>严重等级</th>
                          <th>状态</th>
                          <th>生成时间</th>
                          <th>处理人/时间</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!alertPage.list || alertPage.list.length === 0">
                          <td colspan="10" class="empty-cell">暂无报警记录</td>
                        </tr>
                        <tr v-for="row in alertPage.list" :key="row.id">
                          <td>{{ row.moldCode || '-' }}</td>
                          <td>{{ row.moldName || '-' }}</td>
                          <td>{{ alertTypeDesc(row.alertType) }}</td>
                          <td>{{ row.triggerCondition || '-' }}</td>
                          <td class="message-cell">{{ row.message || '-' }}</td>
                          <td>{{ severityDesc(row.severity) }}</td>
                          <td>
                            <span :class="statusClass(row.status)">{{ statusDesc(row.status) }}</span>
                          </td>
                          <td>{{ formatDate(row.createdAt) }}</td>
                          <td>
                            <template v-if="row.status === 2">
                              {{ row.resolvedByName || '-' }} / {{ formatDate(row.resolvedAt) }}
                            </template>
                            <span v-else>-</span>
                          </td>
                          <td>
                            <div v-if="row.status === 1" class="action-buttons">
                              <button
                                class="action-btn status-btn"
                                :disabled="rowLoadingId === row.id"
                                @click="handleResolve(row)"
                              >
                                已解决
                              </button>
                              <button
                                class="action-btn delete-btn"
                                :disabled="rowLoadingId === row.id"
                                @click="handleIgnore(row)"
                              >
                                已忽略
                              </button>
                            </div>
                            <span v-else>-</span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div v-if="alertPage.pages && alertPage.pages > 1" class="pagination">
                    <button
                      class="page-btn"
                      :disabled="pageNum === 1"
                      @click="changePage(pageNum - 1)"
                    >
                      上一页
                    </button>
                    <span class="page-info">
                      第 {{ pageNum }} / {{ alertPage.pages }} 页，共 {{ alertPage.total ?? 0 }} 条
                    </span>
                    <input
                      v-model.number="pageInput"
                      type="number"
                      class="page-input"
                      min="1"
                      :max="alertPage.pages"
                      @keyup.enter="handlePageJump"
                    />
                    <button type="button" class="page-btn small" @click="handlePageJump">
                      跳转
                    </button>
                    <button
                      class="page-btn"
                      :disabled="pageNum === alertPage.pages"
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

    <!-- 解决/忽略 备注弹窗 -->
    <div v-if="showRemarkDialog" class="dialog-overlay" @click="closeRemarkDialog">
      <div class="dialog-content small" @click.stop>
        <div class="dialog-header">
          <h3>{{ remarkAction === 'resolve' ? '标记为已解决' : '标记为已忽略' }}</h3>
          <button class="dialog-close" @click="closeRemarkDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>备注（可选）</label>
            <input
              v-model="remarkText"
              type="text"
              class="form-input"
              placeholder="可填写处理说明"
            />
          </div>
          <div class="dialog-footer">
            <button type="button" class="secondary-btn" @click="closeRemarkDialog">取消</button>
            <button type="button" class="primary-btn" :disabled="remarkSubmitting" @click="submitRemark">
              {{ remarkSubmitting ? '提交中...' : '确定' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import AppSidebar from '@/components/AppSidebar.vue'
import { useMoldOptions } from '@/composables/useMoldOptions'
import { queryAlerts, resolveAlert, ignoreAlert, runAlertRules } from '@/api/alerts'

const { moldOptions } = useMoldOptions()

const successMessage = ref('')
const errorMessage = ref('')
const listLoading = ref(false)
const runRulesLoading = ref(false)
const rowLoadingId = ref(null)
const pageNum = ref(1)
const pageInput = ref(1)
const pageSize = 10

const query = reactive({
  moldId: '',
  keyword: '',
  status: null,
  alertType: null,
})

const alertPage = ref({
  list: [],
  total: 0,
  pages: 0,
})

const showRemarkDialog = ref(false)
const remarkAction = ref('resolve')
const remarkText = ref('')
const remarkSubmitting = ref(false)
const remarkTargetId = ref(null)

function alertTypeDesc(v) {
  const map = { 1: '故障频发', 2: '保养超期', 3: '温度异常', 4: '润滑异常' }
  return map[v] ?? '-'
}

function severityDesc(v) {
  const map = { 1: '低', 2: '中', 3: '高' }
  return map[v] ?? '-'
}

function statusDesc(v) {
  const map = { 1: '活跃', 2: '已解决', 3: '已忽略' }
  return map[v] ?? '-'
}

function statusClass(v) {
  if (v === 1) return 'status-warning'
  if (v === 2) return 'status-normal'
  return 'status-ignored'
}

function formatDate(val) {
  if (!val) return '-'
  const d = new Date(val)
  return isNaN(d.getTime()) ? val : d.toLocaleString('zh-CN')
}

function showMsg(success, msg) {
  if (success) {
    successMessage.value = msg
    errorMessage.value = ''
  } else {
    errorMessage.value = msg
    successMessage.value = ''
  }
  setTimeout(() => {
    successMessage.value = ''
    errorMessage.value = ''
  }, 3000)
}

async function loadList() {
  listLoading.value = true
  try {
    const keywordVal = query.moldId
      ? (moldOptions.value.find((m) => m.id === query.moldId)?.moldCode ?? query.keyword)
      : query.keyword
    const res = await queryAlerts(
      {
        keyword: keywordVal || undefined,
        status: query.status ?? undefined,
        alertType: query.alertType ?? undefined,
      },
      pageNum.value,
      pageSize
    )
    const data = res?.data
    alertPage.value = {
      list: data?.list ?? [],
      total: data?.total ?? 0,
      pages: data?.pages ?? 0,
    }
  } catch (e) {
    showMsg(false, e?.message || '加载报警列表失败')
    alertPage.value = { list: [], total: 0, pages: 0 }
  } finally {
    listLoading.value = false
  }
}

function handleQuery() {
  pageNum.value = 1
  pageInput.value = 1
  loadList()
}

function handleReset() {
  query.moldId = ''
  query.keyword = ''
  query.status = null
  query.alertType = null
  pageNum.value = 1
  pageInput.value = 1
  loadList()
}

function changePage(p) {
  if (p < 1 || (alertPage.value.pages && p > alertPage.value.pages)) return
  pageNum.value = p
  pageInput.value = p
  loadList()
}

function handlePageJump() {
  const totalPages = alertPage.value.pages || 0
  const target = Number(pageInput.value) || 0
  if (!totalPages || target < 1 || target > totalPages) {
    pageInput.value = pageNum.value
    return
  }
  if (target === pageNum.value) return
  changePage(target)
}

async function handleRunRules() {
  runRulesLoading.value = true
  try {
    await runAlertRules()
    showMsg(true, '规则已执行，请刷新列表查看新报警')
    loadList()
  } catch (e) {
    showMsg(false, e?.message || '执行规则失败')
  } finally {
    runRulesLoading.value = false
  }
}

function openRemarkDialog(action, id) {
  remarkAction.value = action
  remarkTargetId.value = id
  remarkText.value = ''
  showRemarkDialog.value = true
}

function closeRemarkDialog() {
  showRemarkDialog.value = false
  remarkTargetId.value = null
}

async function submitRemark() {
  if (!remarkTargetId.value) return
  remarkSubmitting.value = true
  try {
    if (remarkAction.value === 'resolve') {
      await resolveAlert(remarkTargetId.value, remarkText.value || undefined)
      showMsg(true, '已标记为已解决')
    } else {
      await ignoreAlert(remarkTargetId.value, remarkText.value || undefined)
      showMsg(true, '已标记为已忽略')
    }
    closeRemarkDialog()
    loadList()
  } catch (e) {
    showMsg(false, e?.message || '操作失败')
  } finally {
    remarkSubmitting.value = false
  }
}

function handleResolve(row) {
  rowLoadingId.value = row.id
  openRemarkDialog('resolve', row.id)
  rowLoadingId.value = null
}

function handleIgnore(row) {
  rowLoadingId.value = row.id
  openRemarkDialog('ignore', row.id)
  rowLoadingId.value = null
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.alert-records-container {
  min-height: 100vh;
  display: flex;
  background: #f3f4f6;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.top-header {
  padding: 20px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.top-title {
  font-size: 20px;
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
  padding: 20px 24px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.card-body {
  padding: 20px;
}

.success-message,
.error-message {
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.success-message {
  background: #d1fae5;
  color: #065f46;
}

.error-message {
  background: #fee2e2;
  color: #991b1b;
}

.query-form {
  margin-bottom: 20px;
}

.query-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 16px;
}

.query-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.query-item label {
  font-size: 13px;
  color: #374151;
}

.query-input {
  width: 160px;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.query-actions {
  display: flex;
  gap: 8px;
  margin-left: 8px;
}

.primary-btn,
.secondary-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.primary-btn {
  background: #2563eb;
  color: #fff;
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.secondary-btn {
  background: #fff;
  color: #374151;
  border: 1px solid #d1d5db;
}

.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.table-loading {
  padding: 24px;
  text-align: center;
  color: #6b7280;
}

.table-wrapper {
  overflow-x: auto;
}

.mold-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.mold-table th,
.mold-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.mold-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.mold-table tbody tr:hover {
  background: #f9fafb;
}

.empty-cell {
  color: #9ca3af;
  text-align: center;
  padding: 24px;
}

.message-cell {
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-warning {
  color: #d97706;
  font-weight: 500;
}

.status-normal {
  color: #059669;
}

.status-ignored {
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 4px 10px;
  font-size: 13px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  background: #e5e7eb;
  color: #374151;
}

.action-btn.status-btn {
  background: #d1fae5;
  color: #065f46;
}

.action-btn.delete-btn {
  background: #fee2e2;
  color: #991b1b;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
}

.page-btn.small {
  padding: 4px 10px;
  font-size: 12px;
}

.page-input {
  width: 60px;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  font-size: 13px;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #6b7280;
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background: #fff;
  border-radius: 8px;
  min-width: 400px;
  max-width: 90vw;
}

.dialog-content.small {
  min-width: 360px;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  line-height: 1;
}

.dialog-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  color: #374151;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
