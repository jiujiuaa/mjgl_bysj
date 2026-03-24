<template>
  <div class="monitoring-container">
    <AppSidebar />

    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">监测与异常 - 异常记录</div>
        <div class="top-subtitle">查看人工录入及监控产生的异常记录</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">异常记录列表</h3>
                <div class="card-header-actions">
                  <button
                    type="button"
                    class="secondary-btn delete-outline-btn"
                    :disabled="abnormalBatchDeleting || selectedIds.length === 0"
                    @click="handleBatchDelete"
                  >
                    {{ abnormalBatchDeleting ? '删除中...' : `批量删除 (${selectedIds.length})` }}
                  </button>
                  <button type="button" class="primary-btn" @click="openCreateModal">
                    新建异常记录
                  </button>
                </div>
              </div>
              <div class="card-body">
                <div class="query-form">
                  <div class="query-row">
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
                      <label>模具名称/编号</label>
                      <input
                        v-model="query.keyword"
                        type="text"
                        class="form-input query-input"
                        placeholder="支持名称或编号模糊匹配"
                      />
                    </div>
                    <div class="query-item">
                      <label>异常类型</label>
                      <select v-model.number="query.abnormalType" class="form-input query-input">
                        <option :value="0">全部</option>
                        <option :value="1">温度异常</option>
                        <option :value="2">润滑异常</option>
                        <option :value="3">其它异常</option>
                      </select>
                    </div>
                    <div class="query-item">
                      <label>来源</label>
                      <select v-model.number="query.sourceType" class="form-input query-input">
                        <option :value="0">全部</option>
                        <option :value="1">自动监控</option>
                        <option :value="2">温度巡检</option>
                        <option :value="3">润滑巡检</option>
                        <option :value="4">人工录入</option>
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

                <div v-if="listLoading" class="table-loading">异常记录加载中...</div>
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
                          <th>异常类型</th>
                          <th>实际值</th>
                          <th>阈值/范围</th>
                          <th>来源</th>
                          <th>操作人</th>
                          <th>异常时间</th>
                          <th>创建时间</th>
                          <th>描述</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!page.list || page.list.length === 0">
                          <td colspan="12" class="empty-cell">暂无异常记录</td>
                        </tr>
                        <tr v-for="item in page.list" :key="item.id">
                          <td class="select-col">
                            <input
                              type="checkbox"
                              :checked="isSelected(item.id)"
                              @change="toggleRow(item.id)"
                            />
                          </td>
                          <td>{{ item.moldCode || '-' }}</td>
                          <td>{{ item.moldName || '-' }}</td>
                          <td>{{ renderAbnormalType(item.abnormalType) }}</td>
                          <td>{{ item.measuredValue || '-' }}</td>
                          <td>{{ item.thresholdValue || '-' }}</td>
                          <td>{{ renderSourceType(item.sourceType) }}</td>
                          <td>{{ item.operatorName || '-' }}</td>
                          <td>{{ formatDate(item.occurredAt) }}</td>
                          <td>{{ formatDate(item.createdAt) }}</td>
                          <td>{{ item.description || '-' }}</td>
                          <td>
                            <button
                              type="button"
                              class="table-link table-link-danger"
                              @click="handleDelete(item)"
                            >
                              删除
                            </button>
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

          <!-- 新建异常记录弹窗（人工录入） -->
          <div v-if="showCreateModal" class="modal-mask">
            <div class="modal-wrapper">
              <div class="modal-container">
                <div class="modal-header">
                  <h3>新建异常记录</h3>
                  <button class="modal-close" type="button" @click="closeCreateModal">
                    ×
                  </button>
                </div>
                <div class="modal-body">
                  <div v-if="createErrorMessage" class="error-message">
                    <span class="message-icon">⚠</span>
                    {{ createErrorMessage }}
                  </div>
                  <div v-if="createSuccessMessage" class="success-message">
                    <span class="message-icon">✓</span>
                    {{ createSuccessMessage }}
                  </div>

                  <form class="form-grid" @submit.prevent="handleCreate">
                    <div class="form-row">
                      <div class="form-item">
                        <label>模具</label>
                        <select v-model="createForm.moldId" class="form-input">
                          <option value="">请选择模具</option>
                          <option
                            v-for="m in molds"
                            :key="m.id"
                            :value="m.id"
                          >
                            {{ m.moldCode }} - {{ m.name }}
                          </option>
                        </select>
                      </div>
                      <div class="form-item">
                        <label>异常类型</label>
                        <select v-model.number="createForm.abnormalType" class="form-input">
                          <option :value="1">温度异常</option>
                          <option :value="2">润滑异常</option>
                          <option :value="3">其它异常</option>
                        </select>
                      </div>
                    </div>

                    <div class="form-row">
                      <div class="form-item">
                        <label>实际测量值</label>
                        <input
                          v-model="createForm.measuredValue"
                          type="text"
                          class="form-input"
                          placeholder="如：120℃ 或 30%, 3.5kPa"
                        />
                      </div>
                      <div class="form-item">
                        <label>阈值/期望范围</label>
                        <input
                          v-model="createForm.thresholdValue"
                          type="text"
                          class="form-input"
                          placeholder="如：>110℃, 油位30%~70%"
                        />
                      </div>
                    </div>

                    <div class="form-row">
                      <div class="form-item">
                        <label>异常时间</label>
                        <input
                          v-model="createForm.occurredAt"
                          type="datetime-local"
                          class="form-input"
                        />
                      </div>
                    </div>

                    <div class="form-row">
                      <div class="form-item full-width">
                        <label>描述</label>
                        <textarea
                          v-model="createForm.description"
                          class="form-input"
                          rows="2"
                          placeholder="可填写异常原因、处理建议等"
                        />
                      </div>
                    </div>

                    <div class="form-row">
                      <div class="form-item full-width">
                        <label>上传图片（可选）</label>
                        <input
                          type="file"
                          multiple
                          accept="image/*"
                          class="form-input"
                          @change="handleFileChange"
                        />
                        <div v-if="selectedFiles.length" class="file-list selected-file-list">
                          <div
                            v-for="item in selectedFiles"
                            :key="item.id"
                            class="file-item"
                          >
                            <div class="file-main">
                              <span class="file-name">{{ item.file.name }}</span>
                              <span class="file-meta">
                                {{ (item.file.size / 1024).toFixed(1) }} KB
                              </span>
                            </div>
                            <button
                              type="button"
                              class="file-preview-btn"
                              @click="previewImage(item.url)"
                            >
                              预览
                            </button>
                            <button
                              type="button"
                              class="file-delete-btn"
                              @click="removeSelectedFile(item.id)"
                            >
                              删除
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div class="form-actions">
                      <button type="submit" class="primary-btn">
                        提交
                      </button>
                      <button type="button" class="secondary-btn" @click="closeCreateModal">
                        取消
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import AppSidebar from '@/components/AppSidebar.vue'
import { useTableMultiSelect } from '@/composables/useTableMultiSelect'
import { queryAbnormalRecords, deleteAbnormalRecord, batchDeleteAbnormalRecords } from '@/api/abnormalRecords'
import { createManualAbnormal } from '@/api/monitoringLogs'
import { fetchMolds } from '@/api/molds'
import { uploadBizFiles } from '@/api/files'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const listLoading = ref(false)
const errorMessage = ref('')
const showCreateModal = ref(false)
const createErrorMessage = ref('')
const createSuccessMessage = ref('')

const query = reactive({
  moldId: '',
  keyword: '',
  abnormalType: 0,
  sourceType: 0,
  operatorId: '',
  startDate: '',
  endDate: '',
})

const pageNum = ref(1)
const pageSize = ref(10)
const page = reactive({
  list: [],
  total: 0,
  pages: 0,
})

const abnormalBatchDeleting = ref(false)
const { selectedIds, isSelected, toggleRow, isAllPageSelected, toggleSelectAllPage, clearSelection } =
  useTableMultiSelect(() => page.list)

const molds = ref([])
const selectedFiles = ref([])

const createForm = reactive({
  moldId: '',
  abnormalType: 1,
  measuredValue: '',
  thresholdValue: '',
  occurredAt: '',
  description: '',
})

const operators = computed(() =>
  (authStore.users || []).filter((u) => u.role !== 'ADMIN'),
)

const buildQueryParam = () => {
  const normalize = (v) =>
    v === null || v === undefined || v === '' ? null : v
  const startTime = query.startDate ? new Date(query.startDate) : null
  const endTime = query.endDate ? new Date(query.endDate) : null
  const keywordVal = query.moldId
    ? (molds.value.find((m) => m.id === query.moldId)?.moldCode ?? query.keyword?.trim())
    : query.keyword?.trim()
  return {
    keyword: keywordVal || null,
    moldId: null,
    abnormalType: query.abnormalType && query.abnormalType !== 0 ? query.abnormalType : null,
    operatorId: normalize(query.operatorId),
    sourceType: query.sourceType && query.sourceType !== 0 ? query.sourceType : null,
    startTime,
    endTime,
  }
}

const loadList = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const param = buildQueryParam()
    const res = await queryAbnormalRecords(param, pageNum.value, pageSize.value)
    const data = res.data || {}
    page.list = data.list ?? []
    page.total = data.total ?? 0
    page.pages = data.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载异常记录失败'
     
    console.error(e)
  } finally {
    listLoading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  loadList()
}

const handleResetQuery = () => {
  query.moldId = ''
  query.keyword = ''
  query.abnormalType = 0
  query.sourceType = 0
  query.operatorId = ''
  query.startDate = ''
  query.endDate = ''
  pageNum.value = 1
  loadList()
}

const changePage = (newPage) => {
  if (newPage < 1 || (page.pages && newPage > page.pages)) return
  pageNum.value = newPage
  loadList()
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

const renderAbnormalType = (t) => {
  if (t === 1) return '温度异常'
  if (t === 2) return '润滑异常'
  if (t === 3) return '其它异常'
  return '-'
}

const renderSourceType = (t) => {
  if (t === 1) return '自动监控'
  if (t === 2) return '温度巡检'
  if (t === 3) return '润滑巡检'
  if (t === 4) return '人工录入'
  return '-'
}

const handleDelete = async (item) => {
  if (!item || !item.id) return
  // 简单确认，后续可换成全局对话框
  if (!window.confirm('确认删除该异常记录？')) {
    return
  }
  try {
    await deleteAbnormalRecord(item.id)
    clearSelection()
    loadList()
  } catch (e) {
     
    console.error(e)
    alert(e.message || '删除失败')
  }
}

const handleBatchDelete = async () => {
  const ids = [...selectedIds.value]
  if (!ids.length) return
  if (!window.confirm(`确认批量删除选中的 ${ids.length} 条异常记录？`)) return
  abnormalBatchDeleting.value = true
  try {
    await batchDeleteAbnormalRecords(ids)
    clearSelection()
    loadList()
  } catch (e) {
    console.error(e)
    alert(e.message || '批量删除失败')
  } finally {
    abnormalBatchDeleting.value = false
  }
}

const clearSelectedFiles = () => {
  selectedFiles.value.forEach((item) => {
    if (item.url) {
      URL.revokeObjectURL(item.url)
    }
  })
  selectedFiles.value = []
}

const resetCreateForm = () => {
  createForm.moldId = ''
  createForm.abnormalType = 1
  createForm.measuredValue = ''
  createForm.thresholdValue = ''
  createForm.occurredAt = ''
  createForm.description = ''
}

const openCreateModal = () => {
  resetCreateForm()
  clearSelectedFiles()
  createErrorMessage.value = ''
  createSuccessMessage.value = ''
  showCreateModal.value = true
}

const closeCreateModal = () => {
  clearSelectedFiles()
  showCreateModal.value = false
}

const handleFileChange = (event) => {
  clearSelectedFiles()
  const files = Array.from(event.target.files || [])
  selectedFiles.value = files.map((file) => ({
    file,
    url: URL.createObjectURL(file),
    id: `${file.name}-${file.size}-${file.lastModified}`,
  }))
}

const removeSelectedFile = (id) => {
  const idx = selectedFiles.value.findIndex((item) => item.id === id)
  if (idx !== -1) {
    const [removed] = selectedFiles.value.splice(idx, 1)
    if (removed?.url) {
      URL.revokeObjectURL(removed.url)
    }
  }
}

const previewImage = (url) => {
  if (url) {
    window.open(url, '_blank')
  }
}

const handleCreate = async () => {
  createErrorMessage.value = ''
  createSuccessMessage.value = ''
  if (!createForm.moldId) {
    createErrorMessage.value = '请填写模具ID'
    return
  }
  try {
    const payload = {
      moldId: createForm.moldId,
      abnormalType: createForm.abnormalType,
      measuredValue: createForm.measuredValue || null,
      thresholdValue: createForm.thresholdValue || null,
      description: createForm.description || null,
      occurredAt: createForm.occurredAt ? new Date(createForm.occurredAt) : null,
      sourceType: 4,
    }
    const res = await createManualAbnormal(payload)
    const recordId = res && typeof res === 'object' ? res.data : null
    if (recordId && selectedFiles.value.length) {
      await uploadBizFiles(
        recordId,
        selectedFiles.value.map((item) => item.file),
        {
          bizType: 'abnormal_record',
          fileType: 'abnormal_photo',
          description: '异常记录图片',
        },
      )
    }
    createSuccessMessage.value = '异常记录已保存'
    setTimeout(() => {
      createSuccessMessage.value = ''
    }, 3000)
    showCreateModal.value = false
    loadList()
  } catch (e) {
     
    console.error(e)
    createErrorMessage.value = e.message || '保存异常记录失败'
  }
}

onMounted(async () => {
  if (route.query.moldKeyword) {
    query.keyword = String(route.query.moldKeyword)
  }
  try {
    const res = await fetchMolds(1, 1000)
    molds.value = res.data?.list ?? []
  } catch (e) {
     
    console.error(e)
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

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
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

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.page-info {
  font-size: 13px;
  color: #4b5563;
}

.table-link {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  background: #ffffff;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}

.table-link-danger {
  border-color: #fecaca;
  color: #b91c1c;
  background: #fef2f2;
}

.table-link-danger:hover {
  background: #fee2e2;
}

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

.modal-mask {
  position: fixed;
  z-index: 999;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-wrapper {
  width: 100%;
  max-width: 640px;
  padding: 16px;
}

.modal-container {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.25);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
}

.modal-close {
  border: none;
  background: rgba(255, 255, 255, 0.15);
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  color: #ffffff;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.modal-body {
  padding: 20px 20px 16px;
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

@media (max-width: 768px) {
  .monitoring-container {
    flex-direction: column;
  }

  .main-content {
    padding: 16px;
  }

  .card {
    margin-bottom: 16px;
  }
}
</style>

