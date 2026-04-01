<template>
  <div class="maintenance-plans-container">
    <!-- 全局左侧边栏 -->
    <AppSidebar />

    <!-- 右侧主区域 -->
    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">保养计划管理</div>
        <div class="top-subtitle">配置模具的周期保养与日历保养计划</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">保养计划列表</h3>
                <div class="card-header-actions">
                  <button
                    type="button"
                    class="secondary-btn delete-outline-btn"
                    :disabled="plansBatchDeleting || selectedIds.length === 0"
                    @click="handleBatchDelete"
                  >
                    {{ plansBatchDeleting ? '删除中...' : `批量删除 (${selectedIds.length})` }}
                  </button>
                  <button class="primary-btn" type="button" @click="handleShowCreateDialog">
                    <span class="btn-icon">+</span>
                    新建计划
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

                <!-- 查询条件：与 MaintenancePlanQueryParam 对应 -->
                <div class="query-form">
                  <div class="query-row">
                    <div class="query-item">
                      <label>计划名称/描述</label>
                      <input
                        v-model="query.keyword"
                        type="text"
                        class="form-input query-input"
                        placeholder="支持名称或描述模糊匹配"
                      />
                    </div>
                    <div class="query-item">
                      <label>选择模具</label>
                      <select v-model="query.moldId" class="form-input query-input">
                        <option value="">全部</option>
                        <option v-for="m in molds" :key="m.id" :value="m.id">
                          {{ (m.moldCode || '') + (m.name ? ` - ${m.name}` : '') || m.id }}
                        </option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>指定模具（名称/编号模糊）</label>
                      <input
                        v-model="query.specificMoldId"
                        type="text"
                        class="form-input query-input"
                        placeholder="支持按模具名称或模具编号模糊查询"
                      />
                    </div>
                    <div class="query-item">
                      <label>保养类型</label>
                      <input
                        v-model="query.maintenanceType"
                        type="text"
                        class="form-input query-input"
                        placeholder="如：润滑、清洁"
                      />
                    </div>
                    <div class="query-item">
                      <label>启用状态</label>
                      <select v-model="query.isActive" class="form-input query-input">
                        <option :value="null">全部</option>
                        <option :value="1">启用</option>
                        <option :value="0">停用</option>
                      </select>
                    </div>
                  </div>
                  <div class="query-row">
                    <div class="query-item">
                      <label>创建开始时间</label>
                      <input
                        v-model="query.startCreatedAt"
                        type="datetime-local"
                        class="form-input query-input"
                      />
                    </div>
                    <div class="query-item">
                      <label>创建结束时间</label>
                      <input
                        v-model="query.endCreatedAt"
                        type="datetime-local"
                        class="form-input query-input"
                      />
                    </div>
                    <div class="query-item">
                      <label>创建人ID</label>
                      <input
                        v-model="query.createdBy"
                        type="text"
                        class="form-input query-input"
                      />
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

                <div v-if="listLoading" class="table-loading">保养计划加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th class="select-col">
                            <input
                              type="checkbox"
                              :checked="isAllPageSelected()"
                              @change="toggleSelectAllPage($event.target.checked)"
                              title="全选本页"
                            />
                          </th>
                          <th>计划名称</th>
                          <th>模具类型</th>
                          <th>指定模具</th>
                          <th>保养类型</th>
                          <th>执行模式</th>
                          <th>间隔小时</th>
                          <th>每月固定日</th>
                          <th>是否启用</th>
                          <th>创建人</th>
                          <th>创建时间</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!page.list || page.list.length === 0">
                          <td colspan="12" class="empty-cell">暂无保养计划</td>
                        </tr>
                        <tr v-for="plan in page.list" :key="plan.id">
                          <td class="select-col">
                            <input
                              type="checkbox"
                              :checked="isSelected(plan.id)"
                              @change="toggleRow(plan.id)"
                            />
                          </td>
                          <td>{{ plan.name }}</td>
                          <td>{{ plan.moldCategory || '-' }}</td>
                          <td>{{ plan.moldName || '-' }}</td>
                          <td>{{ plan.maintenanceType || '-' }}</td>
                          <td>{{ formatMode(plan) }}</td>
                          <td>{{ plan.intervalHours ?? '-' }}</td>
                          <td>{{ plan.scheduledDayOfMonth ?? '-' }}</td>
                          <td>
                            <span :class="plan.isActive === 1 ? 'status-normal' : 'status-danger'">
                              {{ plan.isActive === 1 ? '启用' : '停用' }}
                            </span>
                          </td>
                          <td>{{ plan.createdByName || '-' }}</td>
                          <td>{{ formatDate(plan.createdAt) }}</td>
                          <td>
                            <div class="action-buttons">
                              <button
                                class="action-btn"
                                @click="openEditDialog(plan)"
                              >
                                编辑
                              </button>
                              <button
                                class="action-btn"
                                :class="plan.isActive === 1 ? 'delete-btn' : 'edit-btn'"
                                @click="toggleActive(plan)"
                              >
                                {{ plan.isActive === 1 ? '停用' : '启用' }}
                              </button>
                              <button
                                class="action-btn delete-btn"
                                @click="handleDelete(plan)"
                              >
                                删除
                              </button>
                            </div>
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

    <!-- 新建 / 编辑计划对话框 -->
    <div v-if="showEditDialog" class="dialog-overlay" @click="closeEditDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>{{ editingPlan && editingPlan.id ? '编辑保养计划' : '新建保养计划' }}</h3>
          <button class="dialog-close" @click="closeEditDialog">×</button>
        </div>
        <div class="dialog-body">
          <form class="mold-form" @submit.prevent="handleSave">
            <div class="form-section-title">基础信息</div>
            <div class="form-row">
              <div class="form-group">
                <label for="planName">计划名称 *</label>
                <input
                  id="planName"
                  v-model="form.name"
                  type="text"
                  class="form-input"
                  required
                  placeholder="例如：月度润滑保养"
                />
              </div>
              <div class="form-group">
                <label for="maintenanceType">保养类型</label>
                <input
                  id="maintenanceType"
                  v-model="form.maintenanceType"
                  type="text"
                  class="form-input"
                  placeholder="例如：润滑、清洁"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="specificMoldId">指定模具 *</label>
                <!-- 新建时可以选择模具，编辑时只读展示已绑定的模具 -->
                <template v-if="editingPlan && editingPlan.id">
                  <div class="form-input readonly-text">
                    {{ getMoldDisplayName(form.specificMoldId) }}
                  </div>
                </template>
                <select
                  v-else
                  id="specificMoldId"
                  v-model="form.specificMoldId"
                  class="form-input"
                  required
                >
                  <option value="">请选择模具</option>
                  <option
                    v-for="mold in molds"
                    :key="mold.id"
                    :value="mold.id"
                  >
                    {{ mold.name }}（{{ mold.moldCode }}）
                  </option>
                </select>
              </div>
            </div>

            <div class="form-section-title">执行策略（两者只能选其一）</div>
            <div class="form-row">
              <div class="form-group">
                <label for="intervalHours">按运行间隔（小时）</label>
                <input
                  id="intervalHours"
                  v-model.number="form.intervalHours"
                  type="number"
                  min="0"
                  class="form-input"
                  placeholder="例如：500（表示每运行500小时保养一次）"
                />
              </div>
              <div class="form-group">
                <label for="scheduledDayOfMonth">按每月固定日（1-31）</label>
                <input
                  id="scheduledDayOfMonth"
                  v-model.number="form.scheduledDayOfMonth"
                  type="number"
                  min="1"
                  max="31"
                  class="form-input"
                  placeholder="例如：15 表示每月15号"
                />
              </div>
            </div>

            <div class="form-section-title">说明</div>
            <div class="form-group">
              <label for="description">计划描述</label>
              <textarea
                id="description"
                v-model="form.description"
                rows="3"
                class="form-input"
              ></textarea>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="estimatedDurationHours">预计耗时(小时)</label>
                <input
                  id="estimatedDurationHours"
                  v-model.number="form.estimatedDurationHours"
                  type="number"
                  min="0"
                  step="0.1"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label for="isActive">是否启用</label>
                <select
                  id="isActive"
                  v-model.number="form.isActive"
                  class="form-input"
                >
                  <option :value="1">启用</option>
                  <option :value="0">停用</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label for="standardProcedures">标准操作步骤（JSON / 文本）</label>
              <textarea
                id="standardProcedures"
                v-model="form.standardProcedures"
                rows="3"
                class="form-input"
                placeholder="可直接填写文字步骤说明，例如：1. 断电锁定；2. 拆卸清理；3. 润滑关键部位"
              ></textarea>
            </div>

            <div class="form-group">
              <label for="requiredMaterials">所需物料清单（文本）</label>
              <textarea
                id="requiredMaterials"
                v-model="form.requiredMaterials"
                rows="3"
                class="form-input"
                placeholder="可直接填写文字，例如：润滑油 1 瓶；棉纱 2 卷；扳手 1 套"
              ></textarea>
            </div>

            <div v-if="dialogErrorMessage" class="error-message">
              <span class="message-icon">⚠</span>
              {{ dialogErrorMessage }}
            </div>

            <div class="dialog-actions">
              <button
                type="submit"
                class="submit-button"
                :disabled="saveLoading"
              >
                {{ saveLoading ? '保存中...' : '保存' }}
              </button>
              <button
                type="button"
                class="cancel-button"
                @click="closeEditDialog"
              >
                取消
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/AppSidebar.vue'
import { useTableMultiSelect } from '@/composables/useTableMultiSelect'
import {
  queryMaintenancePlans,
  createMaintenancePlan,
  updateMaintenancePlan,
  deleteMaintenancePlan,
  batchDeleteMaintenancePlans,
  enableMaintenancePlan,
  disableMaintenancePlan,
} from '@/api/maintenancePlans'
import { fetchMolds } from '@/api/molds'

const router = useRouter()
const authStore = useAuthStore()

const showOpsChildren = ref(true)
const showMonitoringChildren = ref(true)

const listLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const pageNum = ref(1)
const pageInput = ref(1)
const pageSize = ref(10)
const page = reactive({
  list: [],
  total: 0,
  pages: 0,
})

const plansBatchDeleting = ref(false)
const { selectedIds, isSelected, toggleRow, isAllPageSelected, toggleSelectAllPage, clearSelection } =
  useTableMultiSelect(() => page.list)

const query = reactive({
  moldId: '',
  keyword: '',
  moldTypeId: '',
  specificMoldId: '',
  maintenanceType: '',
  isActive: null,
  startCreatedAt: '',
  endCreatedAt: '',
  createdBy: '',
})

const showEditDialog = ref(false)
const saveLoading = ref(false)
const dialogErrorMessage = ref('')
const editingPlan = ref(null)

const emptyForm = () => ({
  id: null,
  name: '',
  specificMoldId: '',
  intervalHours: null,
  scheduledDayOfMonth: null,
  maintenanceType: '',
  description: '',
  standardProcedures: '',
  estimatedDurationHours: null,
  requiredMaterials: '',
  isActive: 1,
})

const form = ref(emptyForm())

// 模具列表，用于下拉选择 existing molds
const molds = ref([])

const loadMolds = async () => {
  try {
    const res = await fetchMolds(1, 1000)
    molds.value = res.data?.list ?? []
  } catch (e) {
    // 静默失败，不影响主流程
    console.error(e)
  }
}

const getMoldDisplayName = (id) => {
  if (!id) return '-'
  const mold = molds.value.find((m) => m.id === id)
  if (!mold) return id
  return `${mold.name || ''}（${mold.moldCode || ''}）`
}

const buildQueryParam = () => {
  const normalize = (s) => {
    if (!s) return undefined
    const v = s.replace('T', ' ').slice(0, 19)
    return v.length <= 16 ? `${v}:00` : v
  }
  const specificMoldIdVal = query.moldId
    ? (molds.value.find((m) => m.id === query.moldId)?.moldCode ?? query.specificMoldId?.trim())
    : query.specificMoldId?.trim()
  return {
    keyword: query.keyword?.trim() || undefined,
    specificMoldId: specificMoldIdVal || undefined,
    maintenanceType: query.maintenanceType?.trim() || undefined,
    isActive: query.isActive ?? undefined,
    startCreatedAt: normalize(query.startCreatedAt),
    endCreatedAt: normalize(query.endCreatedAt),
    createdBy: query.createdBy?.trim() || undefined,
  }
}

const loadPlans = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const res = await queryMaintenancePlans(buildQueryParam(), pageNum.value, pageSize.value)
    const data = res.data || {}
    page.list = data.list ?? []
    page.total = data.total ?? 0
    page.pages = data.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载保养计划失败'
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  pageInput.value = 1
  loadPlans()
}

const handleReset = () => {
  query.moldId = ''
  query.keyword = ''
  query.specificMoldId = ''
  query.maintenanceType = ''
  query.isActive = null
  query.startCreatedAt = ''
  query.endCreatedAt = ''
  query.createdBy = ''
  pageNum.value = 1
  pageInput.value = 1
  loadPlans()
}

const changePage = (newPage) => {
  if (newPage < 1 || (page.pages && newPage > page.pages)) return
  pageNum.value = newPage
  pageInput.value = newPage
  loadPlans()
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

const handleShowCreateDialog = () => {
  editingPlan.value = null
  form.value = emptyForm()
  dialogErrorMessage.value = ''
  showEditDialog.value = true
}

const openEditDialog = (plan) => {
  editingPlan.value = plan
  form.value = {
    id: plan.id,
    name: plan.name,
    specificMoldId: plan.specificMoldId,
    intervalHours: plan.intervalHours,
    scheduledDayOfMonth: plan.scheduledDayOfMonth,
    maintenanceType: plan.maintenanceType,
    description: plan.description,
    standardProcedures: plan.standardProcedures,
    estimatedDurationHours: plan.estimatedDurationHours,
    requiredMaterials: plan.requiredMaterials,
    isActive: plan.isActive ?? 1,
  }
  dialogErrorMessage.value = ''
  showEditDialog.value = true
}

const closeEditDialog = () => {
  showEditDialog.value = false
}

const validateForm = () => {
  if (!form.value.name || !form.value.name.trim()) {
    dialogErrorMessage.value = '请填写计划名称'
    return false
  }
  const hasInterval = form.value.intervalHours != null && form.value.intervalHours > 0
  const hasScheduledDay =
    form.value.scheduledDayOfMonth != null && form.value.scheduledDayOfMonth > 0
  if (hasInterval && hasScheduledDay) {
    dialogErrorMessage.value = '不能同时指定间隔小时和每月固定日'
    return false
  }
  if (!hasInterval && !hasScheduledDay) {
    dialogErrorMessage.value = '请至少选择一种执行策略：间隔小时或每月固定日'
    return false
  }
  if (hasScheduledDay) {
    const day = form.value.scheduledDayOfMonth
    if (day < 1 || day > 31) {
      dialogErrorMessage.value = '每月固定日需在 1-31 范围内'
      return false
    }
  }
  if (!form.value.specificMoldId || !form.value.specificMoldId.trim()) {
    dialogErrorMessage.value = '请选择绑定的模具（后端也有校验）'
    return false
  }
  dialogErrorMessage.value = ''
  return true
}

const handleSave = async () => {
  if (!validateForm()) return
  saveLoading.value = true
  try {
    const payload = {
      ...form.value,
    }
    if (payload.id) {
      await updateMaintenancePlan(payload)
      successMessage.value = '更新保养计划成功'
    } else {
      await createMaintenancePlan(payload)
      successMessage.value = '创建保养计划成功'
    }
    showEditDialog.value = false
    await loadPlans()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    dialogErrorMessage.value = e.message || '保存保养计划失败'
  } finally {
    saveLoading.value = false
  }
}

const handleDelete = async (plan) => {
  if (!plan || !plan.id) return
  const ok = window.confirm(`确定要删除保养计划 "${plan.name}" 吗？此操作不可恢复！`)
  if (!ok) return
  try {
    await deleteMaintenancePlan(plan.id)
    successMessage.value = '删除保养计划成功'
    clearSelection()
    await loadPlans()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '删除保养计划失败'
  }
}

const handleBatchDelete = async () => {
  const ids = [...selectedIds.value]
  if (!ids.length) return
  const ok = window.confirm(`确定批量删除选中的 ${ids.length} 个保养计划吗？此操作不可恢复！`)
  if (!ok) return
  plansBatchDeleting.value = true
  errorMessage.value = ''
  try {
    await batchDeleteMaintenancePlans(ids)
    successMessage.value = '批量删除成功'
    clearSelection()
    await loadPlans()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '批量删除失败'
  } finally {
    plansBatchDeleting.value = false
  }
}

const toggleActive = async (plan) => {
  if (!plan || !plan.id) return
  try {
    if (plan.isActive === 1) {
      await disableMaintenancePlan(plan.id)
      successMessage.value = '已停用保养计划'
    } else {
      await enableMaintenancePlan(plan.id)
      successMessage.value = '已启用保养计划'
    }
    await loadPlans()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '切换启用状态失败'
  }
}

const formatMode = (plan) => {
  const hasInterval = plan.intervalHours != null
  const hasScheduledDay = plan.scheduledDayOfMonth != null
  if (hasInterval && !hasScheduledDay) return '按间隔小时'
  if (!hasInterval && hasScheduledDay) return '按每月固定日'
  return '-'
}

const formatDate = (val) => {
  if (!val) return '-'
  if (typeof val === 'string' && val.includes(' ')) {
    return val
  }
  const date = new Date(val)
  if (Number.isNaN(date.getTime())) return val
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
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
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  loadMolds()
  loadPlans()
})
</script>

<style scoped>
.maintenance-plans-container {
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

.card-header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.delete-outline-btn {
  color: #b91c1c;
  border-color: #fecaca;
}

.select-col {
  width: 40px;
  text-align: center;
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

.btn-icon {
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
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
  transition: all 0.2s;
}

.edit-btn {
  background: #3b82f6;
  color: #ffffff;
}

.edit-btn:hover {
  background: #2563eb;
}

.delete-btn {
  background: #ef4444;
  color: #ffffff;
}

.delete-btn:hover {
  background: #dc2626;
}

.status-normal {
  color: #10b981;
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
  background: #fff;
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

.page-btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.page-info {
  font-size: 13px;
  color: #6b7280;
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
  max-width: 800px;
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
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.dialog-body {
  padding: 20px 24px;
  overflow-y: auto;
}

.mold-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
  margin-top: 4px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  color: #374151;
}

.form-input {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: #ffffff;
  transition: all 0.2s;
}

.readonly-text {
  background-color: #f9fafb;
  color: #4b5563;
}

.form-input:focus {
  outline: none;
  border-color: #1e3c72;
  box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.1);
}

.dialog-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.submit-button {
  flex: 1;
  padding: 10px 16px;
  background: #1e3c72;
  color: #ffffff;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-button {
  flex: 1;
  padding: 10px 16px;
  background: #ffffff;
  color: #374151;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.cancel-button:hover {
  background: #f9fafb;
}

@media (max-width: 768px) {
  .maintenance-plans-container {
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

  .form-row {
    grid-template-columns: 1fr;
  }

  .dialog-content {
    width: 95%;
    max-width: none;
  }
}
</style>

