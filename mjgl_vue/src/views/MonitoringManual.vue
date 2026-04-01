<template>
  <div class="monitoring-container">
    <!-- 全局左侧边栏 -->
    <AppSidebar />

    <!-- 右侧主区域 -->
    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">监测与异常 - 人工巡检</div>
        <div class="top-subtitle">录入并查看温度、润滑等人工巡检数据</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">新建巡检记录</h3>
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

                <form class="form-grid" @submit.prevent="handleSubmit">
                <div class="form-row">
                    <div class="form-item">
                      <label>巡检类型</label>
                      <select v-model="form.type" class="form-input">
                        <option value="temperature">温度</option>
                        <option value="lubrication">润滑</option>
                      </select>
                    </div>
                    <div class="form-item">
                      <label>模具ID</label>
                      <input
                        v-model="form.moldId"
                        type="text"
                        class="form-input"
                        placeholder="请输入模具ID（后续可扩展为选择器）"
                      />
                    </div>
                    <div class="form-item" v-if="form.type === 'temperature'">
                      <label>测点位置</label>
                      <input
                        v-model="form.sensorLocation"
                        type="text"
                        class="form-input"
                        placeholder="如：动模温度传感器1"
                      />
                    </div>
                  </div>

                  <div class="form-row">
                    <div class="form-item" v-if="form.type === 'temperature'">
                      <label>温度 (℃)</label>
                      <input
                        v-model.number="form.temperature"
                        type="number"
                        step="0.1"
                        class="form-input"
                        placeholder="请输入温度"
                      />
                    </div>
                    <div class="form-item" v-if="form.type === 'lubrication'">
                      <label>油位 (%)</label>
                      <input
                        v-model.number="form.oilLevelPercent"
                        type="number"
                        step="0.1"
                        class="form-input"
                        placeholder="0 - 100"
                      />
                    </div>
                    <div class="form-item" v-if="form.type === 'lubrication'">
                      <label>压力 (kPa)</label>
                      <input
                        v-model.number="form.pressureKpa"
                        type="number"
                        step="0.1"
                        class="form-input"
                        placeholder="可选"
                      />
                    </div>
                    <div class="form-item">
                      <label>巡检时间</label>
                      <input
                        v-model="form.operationTime"
                        type="datetime-local"
                        class="form-input"
                      />
                    </div>
                  </div>

                  <div class="form-row">
                    <div class="form-item full-width">
                      <label>备注</label>
                      <textarea
                        v-model="form.description"
                        class="form-input"
                        rows="2"
                        placeholder="可填写异常情况、现场照片说明等"
                      />
                    </div>
                  </div>

                  <div class="form-row">
                    <div class="form-item">
                      <label>
                        <input v-model="form.reportAbnormal" type="checkbox" />
                        该次巡检发现异常，进行异常上报
                      </label>
                    </div>
                    <div v-if="form.reportAbnormal" class="form-item">
                      <label>异常类型</label>
                      <select v-model.number="form.abnormalType" class="form-input">
                        <option :value="1">温度异常</option>
                        <option :value="2">润滑异常</option>
                        <option :value="3">其它异常</option>
                      </select>
                    </div>
                    <div v-if="form.reportAbnormal" class="form-item">
                      <label>阈值/期望范围</label>
                      <input
                        v-model="form.thresholdValue"
                        type="text"
                        class="form-input"
                        placeholder="如：>110℃, 油位30%~70%"
                      />
                    </div>
                  </div>

                  <div class="form-actions">
                    <button type="submit" class="primary-btn">
                      提交巡检记录
                    </button>
                    <button type="button" class="secondary-btn" @click="resetForm">
                      重置
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </section>

          <section style="margin-top: 20px">
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">巡检记录列表</h3>
              </div>
              <div class="card-body">
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
                      <label>类型</label>
                      <select v-model="query.type" class="form-input query-input">
                        <option value="temperature">温度</option>
                        <option value="lubrication">润滑</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>操作人</label>
                      <select v-model="query.operatorId" class="form-input query-input">
                        <option value="">全部</option>
                        <option
                          v-for="u in operators"
                          :key="u.id"
                          :value="u.id"
                        >
                          {{ u.realName || u.username }} ({{ u.username }})
                        </option>
                      </select>
                    </div>
                  </div>
                  <div class="query-row">
                    <div class="query-item">
                      <label>开始日期</label>
                      <input
                        v-model="query.startDate"
                        type="date"
                        class="form-input query-input"
                      />
                    </div>
                    <div class="query-item">
                      <label>结束日期</label>
                      <input
                        v-model="query.endDate"
                        type="date"
                        class="form-input query-input"
                      />
                    </div>
                  </div>
                  <div class="query-actions">
                    <button type="button" class="primary-btn" @click="handleQuery">
                      查询
                    </button>
                    <button type="button" class="secondary-btn" @click="handleResetQuery">
                      重置
                    </button>
                  </div>
                </div>

                <div v-if="listLoading" class="table-loading">巡检记录加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th>类型</th>
                          <th>模具编号</th>
                          <th>模具名称</th>
                          <th v-if="query.type === 'temperature'">测点位置</th>
                          <th v-if="query.type === 'temperature'">温度(℃)</th>
                          <th v-if="query.type === 'lubrication'">油位(%)</th>
                          <th v-if="query.type === 'lubrication'">压力(kPa)</th>
                          <th>操作人</th>
                          <th>巡检时间</th>
                          <th>备注</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!page.list || page.list.length === 0">
                          <td colspan="9" class="empty-cell">暂无巡检记录</td>
                        </tr>
                        <tr v-for="item in page.list" :key="item.id">
                          <td>{{ query.type === 'temperature' ? '温度' : '润滑' }}</td>
                          <td>{{ item.moldCode || '-' }}</td>
                          <td>{{ item.moldName || '-' }}</td>
                          <td v-if="query.type === 'temperature'">
                            {{ item.sensorLocation || '-' }}
                          </td>
                          <td v-if="query.type === 'temperature'">
                            {{ item.temperature ?? '-' }}
                          </td>
                          <td v-if="query.type === 'lubrication'">
                            {{ item.oilLevelPercent ?? '-' }}
                          </td>
                          <td v-if="query.type === 'lubrication'">
                            {{ item.pressureKpa ?? '-' }}
                          </td>
                          <td>{{ item.operatorName || '-' }}</td>
                          <td>{{ formatDate(item.operationTime) }}</td>
                          <td>{{ item.description || '-' }}</td>
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
                    <input
                      v-model.number="pageInput"
                      type="number"
                      class="page-input"
                      min="1"
                      :max="page.pages"
                      @keyup.enter="handlePageJump"
                    />
                    <button type="button" class="page-btn small" @click="handlePageJump">
                      跳转
                    </button>
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
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMoldOptions } from '@/composables/useMoldOptions'
import AppSidebar from '@/components/AppSidebar.vue'
import {
  createTemperatureLog,
  createLubricationLog,
  queryTemperatureLogs,
  queryLubricationLogs,
} from '@/api/monitoringLogs'
import { createManualAbnormal } from '@/api/monitoringLogs'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const showOpsChildren = ref(true)
const showMonitoringChildren = ref(true)

const successMessage = ref('')
const errorMessage = ref('')
const listLoading = ref(false)

const form = reactive({
  type: 'temperature',
  moldId: '',
  sensorLocation: '',
  temperature: null,
  oilLevelPercent: null,
  pressureKpa: null,
  operationTime: '',
  description: '',
  reportAbnormal: false,
  abnormalType: 1,
  thresholdValue: '',
})

const { moldOptions } = useMoldOptions()

const query = reactive({
  type: 'temperature',
  moldId: '',
  keyword: '',
  startDate: '',
  endDate: '',
  operatorId: '',
  sensorLocation: '',
})

const pageNum = ref(1)
const pageInput = ref(1)
const pageSize = ref(10)
const page = reactive({
  list: [],
  total: 0,
  pages: 0,
})

const operators = computed(() =>
  (authStore.users || []).filter((u) => u.role !== 'ADMIN')
)

const buildQueryParam = () => {
  const normalize = (v) =>
    v === null || v === undefined || v === '' ? null : v
  const startTime = query.startDate ? new Date(query.startDate) : null
  const endTime = query.endDate ? new Date(query.endDate) : null
  const keywordVal = query.moldId
    ? (moldOptions.value.find((m) => m.id === query.moldId)?.moldCode ?? query.keyword?.trim())
    : query.keyword?.trim()
  return {
    keyword: keywordVal || null,
    moldId: null,
    operatorId: normalize(query.operatorId),
    startTime,
    endTime,
    sensorLocation: query.type === 'temperature'
      ? normalize(query.sensorLocation)
      : null,
  }
}

const loadList = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const param = buildQueryParam()
    const api =
      query.type === 'temperature' ? queryTemperatureLogs : queryLubricationLogs
    const res = await api(param, pageNum.value, pageSize.value)
    const data = res.data || {}
    page.list = data.list ?? []
    page.total = data.total ?? 0
    page.pages = data.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载巡检记录失败'
  } finally {
    listLoading.value = false
  }
}

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  if (!form.moldId) {
    errorMessage.value = '请填写模具ID'
    return
  }

  try {
    const payload = {
      moldId: form.moldId,
      operationTime: form.operationTime ? new Date(form.operationTime) : null,
      description: form.description || null,
    }
    let api
    let measuredValue = ''
    if (form.type === 'temperature') {
      payload.sensorLocation = form.sensorLocation || null
      payload.temperature = form.temperature
      api = createTemperatureLog
      measuredValue = form.temperature != null ? `${form.temperature}℃` : ''
    } else {
      payload.oilLevelPercent = form.oilLevelPercent
      payload.pressureKpa = form.pressureKpa
      api = createLubricationLog
      const oilPart =
        form.oilLevelPercent != null ? `${form.oilLevelPercent}%` : ''
      const pressurePart =
        form.pressureKpa != null ? `, ${form.pressureKpa}kPa` : ''
      measuredValue = `${oilPart}${pressurePart}`
    }
    const res = await api(payload)
    const logId = res && typeof res === 'object' ? res.data : null

    if (form.reportAbnormal) {
      const abnormalPayload = {
        moldId: form.moldId,
        abnormalType: form.abnormalType,
        measuredValue: measuredValue || null,
        thresholdValue: form.thresholdValue || null,
        description: form.description || null,
        occurredAt: form.operationTime ? new Date(form.operationTime) : null,
        sourceType: 4,
        linkedLogId: logId || null,
      }
      await createManualAbnormal(abnormalPayload)
    }
    successMessage.value = '巡检记录已保存'
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
    resetForm()
    loadList()
  } catch (e) {
    errorMessage.value = e.message || '保存巡检记录失败'
  }
}

const resetForm = () => {
  form.moldId = ''
  form.sensorLocation = ''
  form.temperature = null
  form.oilLevelPercent = null
  form.pressureKpa = null
  form.operationTime = ''
  form.description = ''
   form.reportAbnormal = false
   form.abnormalType = 1
   form.thresholdValue = ''
}

const handleQuery = () => {
  pageNum.value = 1
  pageInput.value = 1
  loadList()
}

const handleResetQuery = () => {
  query.moldId = ''
  query.keyword = ''
  query.startDate = ''
  query.endDate = ''
  query.operatorId = ''
  pageNum.value = 1
  pageInput.value = 1
  loadList()
}

const changePage = (newPage) => {
  if (newPage < 1 || (page.pages && newPage > page.pages)) return
  pageNum.value = newPage
  pageInput.value = newPage
  loadList()
}

const handlePageJump = () => {
  const target = Number(pageInput.value) || 0
  if (!page.pages || target < 1 || target > page.pages) {
    pageInput.value = pageNum.value
    return
  }
  if (target === pageNum.value) return
  changePage(target)
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

onMounted(async () => {
  if (route.query.moldKeyword) {
    query.keyword = String(route.query.moldKeyword)
  }
  if (authStore.isAdmin) {
    await authStore.loadUsers()
  }
  loadList()
})
</script>

<style scoped>
.monitoring-container {
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

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 140px;
  flex: 1;
  max-width: 260px;
}

.form-item.full-width {
  max-width: none;
}

.form-item label {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #2a5298;
  box-shadow: 0 0 0 2px rgba(42, 82, 152, 0.15);
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
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

.pagination {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
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
  cursor: not-allowed;
  opacity: 0.6;
}

.page-info {
  font-size: 13px;
  color: #4b5563;
}

@media (max-width: 768px) {
  .monitoring-container {
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

