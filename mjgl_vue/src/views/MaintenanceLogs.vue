<template>
  <div class="maintenance-logs-container">
    <!-- 全局左侧边栏 -->
    <AppSidebar />

    <!-- 右侧主区域 -->
    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">模具保养记录</div>
        <div class="top-subtitle">查看按计划执行的保养历史</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">保养记录列表</h3>
                <div class="card-header-actions">
                  <button
                    type="button"
                    class="secondary-btn delete-outline-btn"
                    :disabled="logsBatchDeleting || selectedIds.length === 0"
                    @click="handleBatchDelete"
                  >
                    {{ logsBatchDeleting ? '删除中...' : `批量删除 (${selectedIds.length})` }}
                  </button>
                  <button
                    class="primary-btn"
                    type="button"
                    @click="handleShowCreateDialog"
                  >
                    新建保养记录
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

                <div v-if="wsError" class="error-message">
                  <span class="message-icon">⚠</span>
                  {{ wsError }}
                </div>

                <div v-if="latestAlert" class="realtime-alert">
                  <div class="realtime-alert-title">
                    实时告警：{{ latestAlert.type }} - {{ latestAlert.title }}
                  </div>
                  <div class="realtime-alert-content">
                    {{ latestAlert.content }}
                    <span class="realtime-alert-meta">
                      模具ID: {{ latestAlert.id }}
                      | 业务: {{ latestAlert.biz_type }}
                      | 推送人: {{ latestAlert.senderName || latestAlert.senderId || '系统' }}
                      | 时间: {{ latestAlert.time }}
                    </span>
                  </div>
                </div>

                <!-- 查询条件：与 MaintenanceLogQueryParam 对应 -->
                <div class="query-form">
                  <div class="query-row">
                    <div class="query-item">
                      <label>计划名称/描述</label>
                      <input
                        v-model="query.keyword"
                        type="text"
                        class="form-input query-input"
                        placeholder="支持名称或描述模糊匹配（按关联保养计划）"
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
                        placeholder="按保养计划关联的模具名称或编号模糊查询"
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
                      <label>保养人ID</label>
                      <input
                        v-model="query.maintainerId"
                        type="text"
                        class="form-input query-input"
                      />
                    </div>
                  </div>
                  <div class="query-row">
                    <div class="query-item">
                      <label>实际开始时间(起)</label>
                      <input
                        v-model="query.startActualTime"
                        type="datetime-local"
                        class="form-input query-input"
                      />
                    </div>
                    <div class="query-item">
                      <label>实际结束时间(止)</label>
                      <input
                        v-model="query.endActualTime"
                        type="datetime-local"
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

                <div v-if="listLoading" class="table-loading">保养记录加载中...</div>
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
                          <th>模具编号</th>
                          <th>模具名称</th>
                          <th>关联保养计划</th>
                          <th>保养类型</th>
                          <th>保养人</th>
                          <th>开始时间</th>
                          <th>结束时间</th>
                          <th>费用(元)</th>
                          <th>合理性审批</th>
                          <th>创建时间</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!page.list || page.list.length === 0">
                          <td colspan="12" class="empty-cell">暂无保养记录</td>
                        </tr>
                        <tr v-for="log in page.list" :key="log.id">
                          <td class="select-col">
                            <input
                              type="checkbox"
                              :checked="isSelected(log.id)"
                              @change="toggleRow(log.id)"
                            />
                          </td>
                          <td>{{ log.moldCode || '-' }}</td>
                          <td>{{ log.moldName || '-' }}</td>
                          <td>{{ log.planName || '-' }}</td>
                          <td>{{ log.maintenanceType || '-' }}</td>
                          <td>{{ log.maintainerName || '-' }}</td>
                          <td>{{ formatDate(log.actualStartTime) }}</td>
                          <td>{{ formatDate(log.actualEndTime) }}</td>
                          <td>{{ log.cost ?? '-' }}</td>
                          <td>
                            <span
                              v-if="log.approvalStatus === 1"
                              class="status-normal"
                            >
                              合理
                            </span>
                            <span
                              v-else-if="log.approvalStatus === 2"
                              class="status-danger"
                            >
                              存在问题
                            </span>
                            <span v-else>-</span>
                          </td>
                          <td>{{ formatDate(log.createdAt) }}</td>
                          <td>
                            <div class="action-buttons">
                              <button
                                class="action-btn delete-btn"
                                @click="handleDelete(log)"
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

    <!-- 合理性审批对话框 -->
    <div v-if="showApprovalDialog" class="dialog-overlay" @click="closeApprovalDialog">
      <div class="dialog-content dialog-content-sm" @click.stop>
        <div class="dialog-header">
          <h3>合理性审批</h3>
          <button class="dialog-close" @click="closeApprovalDialog">×</button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleApprovalSubmit">
            <div class="form-group">
              <label for="approvalStatus">审批结果</label>
              <select
                id="approvalStatus"
                v-model.number="approvalForm.approvalStatus"
                class="form-input"
              >
                <option :value="0">未审核</option>
                <option :value="1">合理</option>
                <option :value="2">存在问题</option>
              </select>
            </div>
            <div class="form-group">
              <label for="approvalComment">审批意见</label>
              <input
                id="approvalComment"
                v-model="approvalForm.approvalComment"
                type="text"
                class="form-input"
                placeholder="可填写原因分析或改进建议"
              />
            </div>
            <div v-if="approvalDialogError" class="error-message">
              <span class="message-icon">⚠</span>
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

    <!-- 新建保养记录对话框 -->
    <div v-if="showCreateDialog" class="dialog-overlay" @click="closeCreateDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>新建保养记录</h3>
          <button class="dialog-close" @click="closeCreateDialog">×</button>
        </div>
        <div class="dialog-body">
          <form class="mold-form" @submit.prevent="handleCreate">
            <div class="form-section-title">基础信息</div>
            <div class="form-row">
              <div class="form-group">
                <label for="createMoldId">模具 *</label>
                <select
                  id="createMoldId"
                  v-model="createForm.moldId"
                  class="form-input"
                  required
                  @change="handleMoldChange"
                >
                  <option value="">请选择模具</option>
                  <option
                    v-for="m in molds"
                    :key="m.id"
                    :value="m.id"
                  >
                    {{ m.name }}（{{ m.moldCode }}）
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label for="createPlanId">关联保养计划 *</label>
                <select
                  id="createPlanId"
                  v-model="createForm.planId"
                  class="form-input"
                  required
                >
                  <option value="">请选择计划</option>
                  <option
                    v-for="p in plans"
                    :key="p.id"
                    :value="p.id"
                  >
                    {{ p.name }}
                  </option>
                </select>
                <small v-if="createForm.moldId && !plans.length" class="field-hint">
                  当前模具暂无可用保养计划，请先在“保养计划”页面为该模具创建计划
                </small>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="createMaintenanceType">保养类型</label>
                <input
                  id="createMaintenanceType"
                  v-model="createForm.maintenanceType"
                  type="text"
                  class="form-input"
                  placeholder="例如：润滑、清洁"
                />
              </div>
              <div class="form-group">
                <label for="createCost">费用(元)</label>
                <input
                  id="createCost"
                  v-model.number="createForm.cost"
                  type="number"
                  min="0"
                  step="0.01"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-section-title">时间信息</div>
            <div class="form-row">
              <div class="form-group">
                <label for="createStart">实际开始时间</label>
                <input
                  id="createStart"
                  v-model="createForm.actualStartTime"
                  type="datetime-local"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label for="createEnd">实际结束时间</label>
                <input
                  id="createEnd"
                  v-model="createForm.actualEndTime"
                  type="datetime-local"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-section-title">说明</div>
            <div class="form-group">
              <label for="createDetails">保养内容说明</label>
              <textarea
                id="createDetails"
                v-model="createForm.details"
                rows="3"
                class="form-input"
                placeholder="简要描述本次保养的主要内容"
              ></textarea>
            </div>

            <div class="form-section-title">保养图片（可选）</div>
            <div class="form-group">
              <label for="createFiles">上传图片</label>
              <input
                id="createFiles"
                type="file"
                multiple
                accept="image/*"
                class="form-input"
                @change="handleCreateFilesChange"
              />
              <div v-if="createSelectedFiles.length" class="file-list selected-file-list">
                <div
                  v-for="file in createSelectedFiles"
                  :key="file.name + file.size + file.lastModified"
                  class="file-item"
                >
                  <div class="file-main">
                    <span class="file-name">{{ file.name }}</span>
                    <span class="file-meta">
                      {{ (file.size / 1024).toFixed(1) }} KB
                    </span>
                  </div>
                  <button
                    type="button"
                    class="file-preview-btn"
                    @click="previewSelectedFile(file)"
                  >
                    预览
                  </button>
                  <button
                    type="button"
                    class="file-delete-btn"
                    @click="removeSelectedFile(file)"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>

            <div v-if="createErrorMessage" class="error-message">
              <span class="message-icon">⚠</span>
              {{ createErrorMessage }}
            </div>

            <div class="dialog-actions">
              <button
                type="submit"
                class="submit-button"
                :disabled="createLoading"
              >
                {{ createLoading ? '保存中...' : '保存' }}
              </button>
              <button
                type="button"
                class="cancel-button"
                @click="closeCreateDialog"
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
import { onMounted, onBeforeUnmount, reactive, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/AppSidebar.vue'
import { useTableMultiSelect } from '@/composables/useTableMultiSelect'
import {
  queryMaintenanceLogs,
  deleteMaintenanceLog,
  batchDeleteMaintenanceLogs,
  createMaintenanceLog,
  approveMaintenanceLog,
} from '@/api/maintenanceLogs'
import { LIST_PAGE_SIZE_MAX } from '@/constants/appConfig'
import { fetchMolds } from '@/api/molds'
import { queryMaintenancePlans } from '@/api/maintenancePlans'
import { uploadBizFiles, getFilePreviewUrl, fetchBizFiles, deleteFiles } from '@/api/files'
import { connectWebSocket, disconnectWebSocket } from '@/utils/wsClient'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const showOpsChildren = ref(true)
const showMonitoringChildren = ref(true)

const listLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const wsConnected = ref(false)
const wsError = ref('')
const alertMessages = ref([])
const latestAlert = computed(() =>
  alertMessages.value.length ? alertMessages.value[0] : null,
)

const pageNum = ref(1)
const pageInput = ref(1)
const pageSize = ref(10)
const page = reactive({
  list: [],
  total: 0,
  pages: 0,
})

const logsBatchDeleting = ref(false)
const { selectedIds, isSelected, toggleRow, isAllPageSelected, toggleSelectAllPage, clearSelection } =
  useTableMultiSelect(() => page.list)

const query = reactive({
  moldId: '',
  keyword: '',
  specificMoldId: '',
  maintenanceType: '',
  maintainerId: '',
  startActualTime: '',
  endActualTime: '',
})

const isAdmin = computed(() => authStore.role === 'ADMIN')

const showCreateDialog = ref(false)
const createLoading = ref(false)
const createErrorMessage = ref('')

const showApprovalDialog = ref(false)
const approvalForm = reactive({
  id: '',
  approvalStatus: 0,
  approvalComment: '',
})
const approvalLoading = ref(false)
const approvalDialogError = ref('')

const createForm = reactive({
  moldId: '',
  planId: '',
  maintenanceType: '',
  details: '',
  actualStartTime: '',
  actualEndTime: '',
  cost: null,
})

// 模具和计划下拉
const molds = ref([])
const plans = ref([])

// 新建时选择的图片
const createSelectedFiles = ref([])
const createUploadDescription = ref('')

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
    maintainerId: query.maintainerId?.trim() || undefined,
    startActualTime: normalize(query.startActualTime),
    endActualTime: normalize(query.endActualTime),
  }
}

const loadLogs = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const res = await queryMaintenanceLogs(buildQueryParam(), pageNum.value, pageSize.value)
    const data = res.data || {}
    page.list = data.list ?? []
    page.total = data.total ?? 0
    page.pages = data.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载保养记录失败'
  } finally {
    listLoading.value = false
  }
}

const loadMolds = async () => {
  try {
    const res = await fetchMolds(1, LIST_PAGE_SIZE_MAX)
    molds.value = res.data?.list ?? []
  } catch (e) {
    console.error(e)
  }
}

const loadPlansForMold = async (moldId) => {
  if (!moldId) {
    plans.value = []
    createForm.planId = ''
    return
  }
  try {
    const res = await queryMaintenancePlans(
      { bindMoldId: moldId },
      1,
      LIST_PAGE_SIZE_MAX,
    )
    const data = res.data || {}
    plans.value = data.list ?? []
    if (!plans.value.some((p) => p.id === createForm.planId)) {
      createForm.planId = ''
    }
    if (plans.value.length === 0) {
      createErrorMessage.value = '当前模具暂无可用保养计划，请先在“保养计划”页面为该模具创建计划'
    } else {
      createErrorMessage.value = ''
    }
  } catch (e) {
    console.error(e)
  }
}

const handleQuery = () => {
  pageNum.value = 1
  pageInput.value = 1
  loadLogs()
}

const handleReset = () => {
  query.moldId = ''
  query.keyword = ''
  query.specificMoldId = ''
  query.maintenanceType = ''
  query.maintainerId = ''
  query.startActualTime = ''
  query.endActualTime = ''
  pageNum.value = 1
  pageInput.value = 1
  loadLogs()
}

const changePage = (newPage) => {
  if (newPage < 1 || (page.pages && newPage > page.pages)) return
  pageNum.value = newPage
  pageInput.value = newPage
  loadLogs()
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
  // 从模具管理页跳转过来时，优先使用路由中的 moldId 作为默认模具
  if (route.query.moldId) {
    createForm.moldId = String(route.query.moldId)
  } else {
    createForm.moldId = ''
  }
  createForm.planId = ''
  createForm.maintenanceType = ''
  createForm.details = ''
  createForm.actualStartTime = ''
  createForm.actualEndTime = ''
  createForm.cost = null
  plans.value = []
  createSelectedFiles.value = []
  createUploadDescription.value = ''
  createErrorMessage.value = ''
  showCreateDialog.value = true
}

const closeCreateDialog = () => {
  showCreateDialog.value = false
}

const handleCreateFilesChange = (event) => {
  const files = Array.from(event.target.files || [])
  createSelectedFiles.value = files
}

const removeSelectedFile = (file) => {
  if (!file) return
  createSelectedFiles.value = createSelectedFiles.value.filter(
    (f) =>
      !(
        f.name === file.name &&
        f.size === file.size &&
        f.lastModified === file.lastModified
      ),
  )
}

const previewSelectedFile = (file) => {
  if (!file) return
  const url = URL.createObjectURL(file)
  window.open(url, '_blank')
  // 延迟释放，避免立即关闭预览
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
}

const handleMoldChange = (event) => {
  const value = event?.target?.value ?? createForm.moldId
  loadPlansForMold(value)
}

const handleCreate = async () => {
  createErrorMessage.value = ''
  if (!createForm.moldId) {
    createErrorMessage.value = '请选择模具'
    return
  }
  if (!createForm.planId) {
    createErrorMessage.value = '请选择关联保养计划'
    return
  }

  const normalizeDateTime = (s) => {
    if (!s) return null
    // datetime-local: yyyy-MM-ddTHH:mm，补秒为 yyyy-MM-ddTHH:mm:ss，保持 ISO8601，方便后端按默认规则解析
    if (s.includes('T')) {
      return s.length === 16 ? `${s}:00` : s
    }
    // 如果是空格分隔的，兜底转成带 T 的格式
    return s.replace(' ', 'T')
  }

  const payload = {
    moldId: createForm.moldId,
    planId: createForm.planId,
    maintenanceType: createForm.maintenanceType || null,
    details: createForm.details || null,
    actualStartTime: normalizeDateTime(createForm.actualStartTime),
    actualEndTime: normalizeDateTime(createForm.actualEndTime),
    cost: createForm.cost,
  }

  createLoading.value = true
  try {
    const res = await createMaintenanceLog(payload)
    const recordId = res?.data

    // 上传附件图片（如果选择了文件）
    if (recordId && createSelectedFiles.value.length) {
      await uploadBizFiles(recordId, createSelectedFiles.value, {
        bizType: 'maintenance_log',
        fileType: 'maintenance_photo',
        description: createUploadDescription.value || '保养记录图片',
      })
    }
    showCreateDialog.value = false
    successMessage.value = '新建保养记录成功'
    await loadLogs()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    createErrorMessage.value = e.message || '新建保养记录失败'
  } finally {
    createLoading.value = false
  }
}

const handleDelete = async (log) => {
  if (!log || !log.id) return
  const ok = window.confirm('确定要删除该保养记录吗？此操作不可恢复！')
  if (!ok) return
  try {
    await deleteMaintenanceLog(log.id)
    successMessage.value = '删除保养记录成功'
    clearSelection()
    await loadLogs()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '删除保养记录失败'
  }
}

const handleBatchDelete = async () => {
  const ids = [...selectedIds.value]
  if (!ids.length) return
  const ok = window.confirm(`确定批量删除选中的 ${ids.length} 条保养记录吗？此操作不可恢复！`)
  if (!ok) return
  logsBatchDeleting.value = true
  errorMessage.value = ''
  try {
    await batchDeleteMaintenanceLogs(ids)
    successMessage.value = '批量删除成功'
    clearSelection()
    await loadLogs()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    errorMessage.value = e.message || '批量删除失败'
  } finally {
    logsBatchDeleting.value = false
  }
}

const openApprovalDialog = (log) => {
  approvalForm.id = log.id
  approvalForm.approvalStatus = log.approvalStatus ?? 0
  const rawComment = log.approvalComment
  approvalForm.approvalComment = (rawComment == null || rawComment === 'null' || String(rawComment).trim() === '') ? '' : String(rawComment)
  approvalDialogError.value = ''
  showApprovalDialog.value = true
}

const closeApprovalDialog = () => {
  showApprovalDialog.value = false
  approvalForm.id = ''
  approvalForm.approvalStatus = 0
  approvalForm.approvalComment = ''
  approvalDialogError.value = ''
}

const handleApprovalSubmit = async () => {
  if (!approvalForm.id) return
  approvalDialogError.value = ''
  approvalLoading.value = true
  try {
    await approveMaintenanceLog(approvalForm.id, {
      status: approvalForm.approvalStatus ?? 0,
      comment: approvalForm.approvalComment || null,
    })
    successMessage.value = '审批已提交'
    showApprovalDialog.value = false
    await loadLogs()
    setTimeout(() => { successMessage.value = '' }, 3000)
  } catch (e) {
    approvalDialogError.value = e.message || '审批提交失败'
  } finally {
    approvalLoading.value = false
  }
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
  // 如果从模具管理页跳转过来，则按该模具的编号进行默认筛选
  if (route.query.moldCode) {
    query.specificMoldId = String(route.query.moldCode)
  }
  loadMolds()
  loadLogs()

  connectWebSocket(
    (msg) => {
      alertMessages.value = [msg, ...alertMessages.value].slice(0, 20)
    },
    authStore.userId,
  )
    .then(() => {
      wsConnected.value = true
      wsError.value = ''
    })
    .catch((e) => {
      wsConnected.value = false
      wsError.value = e?.message || '实时告警连接失败'
    })
})

onBeforeUnmount(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.maintenance-logs-container {
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

.realtime-alert {
  padding: 10px 12px;
  border-radius: 6px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  margin-bottom: 16px;
}

.realtime-alert-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d4ed8;
  margin-bottom: 4px;
}

.realtime-alert-content {
  font-size: 13px;
  color: #1f2937;
}

.realtime-alert-meta {
  margin-left: 8px;
  font-size: 12px;
  color: #6b7280;
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

.delete-btn {
  background: #ef4444;
  color: #ffffff;
}

.delete-btn:hover {
  background: #dc2626;
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

.field-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #f97316;
}

.selected-files {
  margin-top: 4px;
  font-size: 12px;
  color: #374151;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.selected-file-item {
  padding: 2px 6px;
  background: #e5e7eb;
  border-radius: 999px;
}

/* 统一的“文件名 + 预览/删除”列表样式，跟维修/模具详情保持一致 */
.selected-file-list {
  margin-top: 8px;
}

.file-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.file-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 14px;
  color: #111827;
}

.file-meta {
  font-size: 12px;
  color: #6b7280;
}

.file-preview-btn {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  color: #1d4ed8;
  transition: all 0.2s;
}

.file-preview-btn:hover {
  background: #eff6ff;
  border-color: #93c5fd;
}

.file-delete-btn {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  color: #b91c1c;
  transition: all 0.2s;
}

.file-delete-btn:hover {
  background: #fee2e2;
}

/* 新建保养记录对话框样式，复用与保养计划相同的风格 */
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

.dialog-content-sm {
  max-width: 420px;
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
  .maintenance-logs-container {
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

