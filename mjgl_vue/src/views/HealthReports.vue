<template>
  <div class="health-reports-container">
    <AppSidebar />

    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">健康评估与决策支持</div>
        <div class="top-subtitle">周期性健康报告生成、综合评分与 PDF 归档</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">健康报告列表</h3>
                <div class="header-actions">
                  <button
                    v-if="isAdmin"
                    type="button"
                    class="secondary-btn delete-btn"
                    :disabled="reportsBatchDeleting || selectedIds.length === 0"
                    @click="handleBatchDeleteReports"
                  >
                    {{ reportsBatchDeleting ? '删除中...' : `批量删除 (${selectedIds.length})` }}
                  </button>
                  <button
                    v-if="isAdmin"
                    type="button"
                    class="primary-btn"
                    :disabled="generating"
                    @click="handleGenerate"
                  >
                    {{ generating ? '生成中...' : '生成并导出' }}
                  </button>
                </div>
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
                      <label>报告类型（用于生成）</label>
                      <select v-model="periodType" class="form-input query-input" @change="handlePresetRange">
                        <option value="WEEKLY">周报</option>
                        <option value="MONTHLY">月报</option>
                        <option value="QUARTERLY">季报</option>
                      </select>
                    </div>

                    <div class="query-item">
                      <label>模具</label>
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
                        <option :value="1">草稿</option>
                        <option :value="2">已生成</option>
                        <option :value="3">已导出</option>
                      </select>
                    </div>

                    <div class="query-item">
                      <label>统计开始日期</label>
                      <input
                        v-model="query.periodStart"
                        type="date"
                        class="form-input query-input"
                        @change="handleDateTouched"
                      />
                    </div>
                    <div class="query-item">
                      <label>统计结束日期</label>
                      <input
                        v-model="query.periodEnd"
                        type="date"
                        class="form-input query-input"
                        @change="handleDateTouched"
                      />
                    </div>

                    <div class="query-item">
                      <label>健康分范围（最小）</label>
                      <input
                        v-model.number="query.minHealthScore"
                        type="number"
                        min="0"
                        max="100"
                        step="1"
                        class="form-input query-input"
                        placeholder="不填"
                      />
                    </div>
                    <div class="query-item">
                      <label>健康分范围（最大）</label>
                      <input
                        v-model.number="query.maxHealthScore"
                        type="number"
                        min="0"
                        max="100"
                        step="1"
                        class="form-input query-input"
                        placeholder="不填"
                      />
                    </div>
                  </div>

                  <div class="query-actions">
                    <button type="button" class="primary-btn" @click="handleQuery">查询</button>
                    <button type="button" class="secondary-btn" @click="handleResetQuery">重置</button>
                  </div>
                </div>

                <div v-if="listLoading" class="table-loading">健康报告加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th v-if="isAdmin" class="select-col">
                            <input
                              type="checkbox"
                              :checked="isAllPageSelected()"
                              @change="toggleSelectAllPage($event.target.checked)"
                              title="全选本页"
                            />
                          </th>
                          <th>报告ID</th>
                          <th>模具</th>
                          <th>周期</th>
                          <th>健康分</th>
                          <th>风险分级</th>
                          <th>保养完成率</th>
                          <th>故障率 (F/U)</th>
                          <th>单位成本 (C/T)</th>
                          <th>状态</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!page.list || page.list.length === 0">
                          <td :colspan="isAdmin ? 11 : 10" class="empty-cell">暂无健康报告数据</td>
                        </tr>
                        <tr v-for="item in page.list" :key="item.id">
                          <td v-if="isAdmin" class="select-col">
                            <input
                              type="checkbox"
                              :checked="isSelected(item.id)"
                              @change="toggleRow(item.id)"
                            />
                          </td>
                          <td class="mono">{{ item.id }}</td>
                          <td>
                            <div class="cell-multi">
                              <div class="cell-title">{{ item.moldCode || '-' }}</div>
                              <div class="cell-sub">{{ item.moldName || '-' }}</div>
                            </div>
                          </td>
                          <td>
                            {{ formatDate(item.reportPeriodStart) }} ~
                            {{ formatDate(item.reportPeriodEnd) }}
                          </td>
                          <td>
                            <span :class="getHealthScoreClass(item.healthScore)">{{ item.healthScore ?? '-' }}</span>
                          </td>
                          <td>
                            <span :class="getRiskClass(item.riskLevel)">{{ item.riskLevel || '-' }}</span>
                          </td>
                          <td>{{ item.maintenanceRate ?? '-' }}%</td>
                          <td>{{ formatBig(item.faultRate) }}</td>
                          <td>{{ formatBig(item.costRate) }}</td>
                          <td>
                            <span :class="getStatusClass(item.status)">
                              {{ formatStatus(item.status) }}
                            </span>
                          </td>
                          <td class="col-actions">
                            <div class="action-buttons">
                              <button
                                class="action-btn"
                                type="button"
                                @click="handleViewActions(item)"
                              >
                                查看建议
                              </button>
                              <button
                                v-if="isAdmin && item.status !== 3"
                                class="action-btn edit-btn"
                                type="button"
                                @click="handleExportPdf(item)"
                              >
                                导出PDF
                              </button>
                              <button
                                v-else-if="item.pdfUrl"
                                class="action-btn"
                                type="button"
                                @click="handleOpenPdf(item)"
                              >
                                打开PDF
                              </button>
                              <button
                                v-if="isAdmin"
                                class="action-btn delete-btn"
                                type="button"
                                @click="handleDeleteReport(item)"
                              >
                                删除报告
                              </button>
                            </div>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div v-if="page.pages && page.pages > 1" class="pagination">
                    <button class="page-btn" :disabled="pageNum === 1" @click="changePage(pageNum - 1)">
                      上一页
                    </button>
                    <span class="page-info">
                      第 {{ pageNum }} / {{ page.pages }} 页，共 {{ page.total || 0 }} 条
                    </span>
                    <button class="page-btn" :disabled="pageNum === page.pages" @click="changePage(pageNum + 1)">
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

    <!-- 建议详情对话框 -->
    <div v-if="showActionsDialog" class="dialog-overlay" @click="showActionsDialog = false">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>管理层建议</h3>
          <button class="dialog-close" @click="showActionsDialog = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="detail-section">
            <div class="detail-title">模具</div>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">编号</span>
                <span class="value">{{ actionsDialogData.moldCode || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">名称</span>
                <span class="value">{{ actionsDialogData.moldName || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-title">风险分级与健康分</div>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">健康分</span>
                <span class="value">{{ actionsDialogData.healthScore ?? '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">风险分级</span>
                <span class="value">{{ actionsDialogData.riskLevel || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-title">建议要点</div>
            <ul v-if="actionsDialogData.recommendedActions?.length" class="recommend-list">
              <li v-for="(a, idx) in actionsDialogData.recommendedActions" :key="idx">{{ a }}</li>
            </ul>
            <div v-else class="empty-cell">暂无建议</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/AppSidebar.vue'
import { useMoldOptions } from '@/composables/useMoldOptions'
import { useTableMultiSelect } from '@/composables/useTableMultiSelect'
import {
  generateHealthReports,
  queryHealthReports,
  exportHealthReportPdf,
  deleteHealthReport,
  batchDeleteHealthReports,
} from '@/api/healthReports'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const isAdmin = computed(() => authStore.isAdmin)

const periodType = ref('MONTHLY')
const dateTouched = ref(false)

const { moldOptions, loadMoldOptions } = useMoldOptions({ immediate: true })

const pageNum = ref(1)
const pageSize = ref(10)
const page = reactive({
  list: [],
  total: 0,
  pages: 0,
})

const generating = ref(false)
const listLoading = ref(false)
const reportsBatchDeleting = ref(false)
const { selectedIds, isSelected, toggleRow, isAllPageSelected, toggleSelectAllPage, clearSelection } =
  useTableMultiSelect(() => page.list)

const successMessage = ref('')
const errorMessage = ref('')

const query = reactive({
  moldId: '',
  status: null,
  periodStart: '',
  periodEnd: '',
  minHealthScore: null,
  maxHealthScore: null,
})

const normalize = (v) => (v === '' || v === undefined || v === null ? null : v)

const presetRange = (type) => {
  const pad = (n) => String(n).padStart(2, '0')
  const toYmd = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  const today = new Date()

  // clone helper
  const clone = (d) => new Date(d.getTime())

  if (type === 'WEEKLY') {
    const getMonday = (dt) => {
      const d = clone(dt)
      const day = (d.getDay() + 6) % 7 // Mon=0
      d.setDate(d.getDate() - day)
      d.setHours(0, 0, 0, 0)
      return d
    }
    const thisWeekMonday = getMonday(today)
    const lastWeekMonday = clone(thisWeekMonday)
    lastWeekMonday.setDate(lastWeekMonday.getDate() - 7)
    const end = clone(lastWeekMonday)
    end.setDate(end.getDate() + 6)
    return { start: toYmd(lastWeekMonday), end: toYmd(end) }
  }

  if (type === 'QUARTERLY') {
    const q = Math.floor(today.getMonth() / 3) // 0..3
    let prevQ = q - 1
    let year = today.getFullYear()
    if (prevQ < 0) {
      prevQ = 3
      year = year - 1
    }
    const startMonth = prevQ * 3
    const start = new Date(year, startMonth, 1)
    const end = new Date(year, startMonth + 3, 0) // last day of prev quarter
    return { start: toYmd(start), end: toYmd(end) }
  }

  // MONTHLY
  const firstDayThisMonth = new Date(today.getFullYear(), today.getMonth(), 1)
  const lastDayPrevMonth = new Date(firstDayThisMonth.getTime() - 24 * 60 * 60 * 1000)
  const start = new Date(lastDayPrevMonth.getFullYear(), lastDayPrevMonth.getMonth(), 1)
  return { start: toYmd(start), end: toYmd(lastDayPrevMonth) }
}

const handlePresetRange = () => {
  if (dateTouched.value) return
  const { start, end } = presetRange(periodType.value)
  query.periodStart = start
  query.periodEnd = end
}

const handleDateTouched = () => {
  dateTouched.value = true
}

watch(
  () => periodType.value,
  () => {
    handlePresetRange()
  },
)

const handleResetQuery = () => {
  query.moldId = ''
  query.status = null
  query.minHealthScore = null
  query.maxHealthScore = null
  dateTouched.value = false
  handlePresetRange()
  pageNum.value = 1
  loadReports()
}

const buildQueryParam = () => {
  return {
    moldId: normalize(query.moldId),
    status: normalize(query.status),
    periodStart: normalize(query.periodStart),
    periodEnd: normalize(query.periodEnd),
    minHealthScore: normalize(query.minHealthScore),
    maxHealthScore: normalize(query.maxHealthScore),
  }
}

const loadReports = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const param = buildQueryParam()
    const res = await queryHealthReports(param, pageNum.value, pageSize.value)
    const data = res.data || {}
    page.list = data.list ?? []
    page.total = data.total ?? 0
    page.pages = data.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载健康报告失败'
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  loadReports()
}

const changePage = (newPage) => {
  if (newPage < 1 || (page.pages && newPage > page.pages)) return
  pageNum.value = newPage
  loadReports()
}

const handleExportPdf = async (item) => {
  if (!item?.id) return
  const ok = window.confirm(`确定为该模具该周期报告导出 PDF 并归档？\nreportId: ${item.id}`)
  if (!ok) return
  try {
    await exportHealthReportPdf(item.id)
    successMessage.value = 'PDF 导出成功'
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
    loadReports()
  } catch (e) {
    errorMessage.value = e.message || '导出 PDF 失败'
  }
}

const handleDeleteReport = async (item) => {
  if (!item?.id) return
  const ok = window.confirm(`确定删除该健康报告及其 PDF 归档文件（如存在）？\nreportId: ${item.id}`)
  if (!ok) return
  try {
    await deleteHealthReport(item.id)
    successMessage.value = '删除成功'
    clearSelection()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
    loadReports()
  } catch (e) {
    errorMessage.value = e.message || '删除失败'
  }
}

const handleBatchDeleteReports = async () => {
  if (!isAdmin.value) return
  const ids = [...selectedIds.value]
  if (!ids.length) return
  const ok = window.confirm(`确定批量删除选中的 ${ids.length} 份健康报告及其 PDF（如存在）？`)
  if (!ok) return
  reportsBatchDeleting.value = true
  errorMessage.value = ''
  try {
    await batchDeleteHealthReports(ids)
    successMessage.value = '批量删除成功'
    clearSelection()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
    loadReports()
  } catch (e) {
    errorMessage.value = e.message || '批量删除失败'
  } finally {
    reportsBatchDeleting.value = false
  }
}

const actionsDialogData = reactive({
  id: '',
  moldCode: '',
  moldName: '',
  healthScore: null,
  riskLevel: '',
  recommendedActions: [],
})
const showActionsDialog = ref(false)

const handleViewActions = (item) => {
  if (!item) return
  actionsDialogData.id = item.id
  actionsDialogData.moldCode = item.moldCode || ''
  actionsDialogData.moldName = item.moldName || ''
  actionsDialogData.healthScore = item.healthScore ?? null
  actionsDialogData.riskLevel = item.riskLevel || ''
  actionsDialogData.recommendedActions = item.recommendedActions || []
  showActionsDialog.value = true
}

const handleOpenPdf = (item) => {
  if (!item?.pdfUrl) return
  window.open(item.pdfUrl, '_blank')
}

const handleGenerate = async () => {
  if (!isAdmin.value) return
  generating.value = true
  errorMessage.value = ''
  try {
    // 生成参数：periodType + 自定义区间（这里由 preset 填充，允许用户手动改）
    const payload = {
      periodType: periodType.value,
      periodStart: query.periodStart || null,
      periodEnd: query.periodEnd || null,
      moldId: normalize(query.moldId),
      exportPdf: true,
    }
    if (!payload.periodStart || !payload.periodEnd) {
      throw new Error('请先选择统计周期开始/结束日期（或保持预填不修改）')
    }
    await generateHealthReports(payload)
    successMessage.value = '健康报告生成请求已提交（若数据量较大可能需稍后刷新）'
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
    loadReports()
  } catch (e) {
    errorMessage.value = e.message || '生成健康报告失败'
  } finally {
    generating.value = false
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await loadMoldOptions()

  // 初始化时间窗口（上一周/月/季）
  handlePresetRange()

  // 从查询参数恢复分页（可选）
  const queryPageNum = Number(route?.query?.pageNum || 1)
  const queryPageSize = Number(route?.query?.pageSize || 10)
  pageNum.value = Number.isNaN(queryPageNum) ? 1 : queryPageNum
  pageSize.value = Number.isNaN(queryPageSize) ? 10 : queryPageSize
  loadReports()
})

// ---------- UI Helper ----------
const formatDate = (val) => {
  if (!val) return '-'
  // 有些后端返回 datetime 字符串（包含空格），保持原样
  if (typeof val === 'string' && val.includes(' ')) return val
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const formatBig = (v) => {
  if (v === null || v === undefined || v === '') return '-'
  const num = Number(v)
  if (Number.isNaN(num)) return String(v)
  // 给出更直观的显示：故障率/单位成本通常不需要太多小数
  return num.toFixed(4)
}

const formatStatus = (status) => {
  if (!status) return '-'
  const map = { 1: '草稿', 2: '已生成', 3: '已导出' }
  return map[status] || String(status)
}

const getStatusClass = (status) => {
  if (!status) return ''
  if (status === 1) return 'status-warning'
  if (status === 2) return 'status-normal'
  if (status === 3) return 'status-normal'
  return ''
}

const getHealthScoreClass = (score) => {
  const s = Number(score)
  if (Number.isNaN(s)) return ''
  if (s >= 85) return 'health-score-ok'
  if (s >= 70) return 'health-score-mid'
  return 'health-score-bad'
}

const getRiskClass = (riskLevel) => {
  switch (riskLevel) {
    case '优良':
      return 'risk-ok'
    case '关注':
      return 'risk-attention'
    case '风险':
      return 'risk-risk'
    case '紧急':
      return 'risk-emergency'
    default:
      return ''
  }
}
</script>

<style scoped>
.health-reports-container {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  flex-direction: row;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
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
}

.card-body {
  padding: 24px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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
  background: #ffffff;
}

.query-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
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
}

.primary-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(30, 60, 114, 0.2);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  vertical-align: top;
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

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  background: #ffffff;
  border: 1px solid #d1d5db;
}

.edit-btn {
  background: #3b82f6;
  border-color: #3b82f6;
  color: #ffffff;
}

.select-col {
  width: 40px;
  text-align: center;
}

.delete-btn {
  background: #ef4444;
  border-color: #ef4444;
  color: #ffffff;
}

.status-normal {
  color: #10b981;
  font-weight: 500;
}
.status-warning {
  color: #f59e0b;
  font-weight: 500;
}
.status-danger {
  color: #ef4444;
  font-weight: 500;
}

.pagination {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-btn {
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  cursor: pointer;
  font-size: 13px;
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.page-info {
  font-size: 13px;
  color: #6b7280;
}

.col-actions {
  width: 240px;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.cell-multi {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cell-title {
  font-weight: 600;
}

.cell-sub {
  color: #6b7280;
  font-size: 12px;
}

.recommend-list {
  padding-left: 18px;
  margin: 0;
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background: #ffffff;
  border-radius: 12px;
  width: 90%;
  max-width: 720px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
}

.dialog-header {
  padding: 20px 24px;
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
  background: rgba(255, 255, 255, 0.15);
  border: none;
  font-size: 20px;
  color: #ffffff;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 6px;
}

.dialog-body {
  padding: 20px 24px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.detail-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
}

.detail-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 10px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-item .label {
  font-size: 12px;
  color: #6b7280;
}
.detail-item .value {
  font-size: 14px;
  color: #111827;
}

.health-score-ok {
  color: #10b981;
  font-weight: 600;
}
.health-score-mid {
  color: #f59e0b;
  font-weight: 600;
}
.health-score-bad {
  color: #ef4444;
  font-weight: 600;
}

.risk-ok {
  color: #10b981;
  font-weight: 600;
}
.risk-attention {
  color: #f59e0b;
  font-weight: 600;
}
.risk-risk {
  color: #f97316;
  font-weight: 600;
}
.risk-emergency {
  color: #ef4444;
  font-weight: 600;
}
</style>

