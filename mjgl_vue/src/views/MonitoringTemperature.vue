<template>
  <div class="monitoring-container">
    <!-- 全局左侧边栏 -->
    <AppSidebar />

    <!-- 右侧主区域 -->
    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">监测与异常 - 温度巡检</div>
        <div class="top-subtitle">录入并查看模具温度类人工巡检数据</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">温度巡检记录列表</h3>
                <button type="button" class="primary-btn" @click="openCreateModal">
                  新建温度巡检记录
                </button>
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
                    <div class="query-item">
                      <label>测点位置</label>
                      <input
                        v-model="query.sensorLocation"
                        type="text"
                        class="form-input query-input"
                        placeholder="如：动模温度传感器1"
                      />
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

                <div v-if="listLoading" class="table-loading">温度巡检记录加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th>模具编号</th>
                          <th>模具名称</th>
                          <th>测点位置</th>
                          <th>温度(℃)</th>
                          <th>操作人</th>
                          <th>巡检时间</th>
                          <th>备注</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!page.list || page.list.length === 0">
                          <td colspan="7" class="empty-cell">暂无温度巡检记录</td>
                        </tr>
                        <tr v-for="item in page.list" :key="item.id">
                          <td>{{ item.moldCode || '-' }}</td>
                          <td>{{ item.moldName || '-' }}</td>
                          <td>{{ item.sensorLocation || '-' }}</td>
                          <td>{{ item.temperature ?? '-' }}</td>
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

          <!-- 新建温度巡检记录弹窗 -->
          <div v-if="showCreateModal" class="modal-mask">
            <div class="modal-wrapper">
              <div class="modal-container">
                <div class="modal-header">
                  <h3>新建温度巡检记录</h3>
                  <button class="modal-close" type="button" @click="closeCreateModal">
                    ×
                  </button>
                </div>
                <div class="modal-body">
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
                        <label>模具</label>
                        <select v-model="form.moldId" class="form-input">
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
                      <div class="form-item">
                        <label>温度 (℃)</label>
                        <input
                          v-model.number="form.temperature"
                          type="number"
                          step="0.1"
                          class="form-input"
                          placeholder="请输入温度"
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
                          placeholder="可填写异常情况、巡检说明等"
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
import { createTemperatureLog, queryTemperatureLogs } from '@/api/monitoringLogs'
import { fetchMolds } from '@/api/molds'
import { uploadBizFiles } from '@/api/files'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const successMessage = ref('')
const errorMessage = ref('')
const listLoading = ref(false)
const showCreateModal = ref(false)
const selectedFiles = ref([])

const form = reactive({
  moldId: '',
  sensorLocation: '',
  temperature: null,
  operationTime: '',
  description: '',
})

const query = reactive({
  moldId: '',
  keyword: '',
  startDate: '',
  endDate: '',
  operatorId: '',
  sensorLocation: '',
})

const pageNum = ref(1)
const pageSize = ref(10)
const page = reactive({
  list: [],
  total: 0,
  pages: 0,
})

const molds = ref([])

const operators = computed(() =>
  (authStore.users || []).filter((u) => u.role !== 'ADMIN')
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
    operatorId: normalize(query.operatorId),
    startTime,
    endTime,
    sensorLocation: normalize(query.sensorLocation),
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

const openCreateModal = () => {
  resetForm()
  clearSelectedFiles()
  errorMessage.value = ''
  successMessage.value = ''
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

const loadMolds = async () => {
  try {
    const res = await fetchMolds(1, 1000)
    molds.value = res.data?.list ?? []
    if (!form.moldId && route.query.moldId) {
      const id = String(route.query.moldId)
      if (molds.value.some((m) => String(m.id) === id)) {
        form.moldId = id
      }
    }
  } catch (e) {
    // 加载模具失败时不影响主要功能
    console.error(e)
  }
}

const loadList = async () => {
  listLoading.value = true
  errorMessage.value = ''
  try {
    const param = buildQueryParam()
    const res = await queryTemperatureLogs(param, pageNum.value, pageSize.value)
    const data = res.data || {}
    page.list = data.list ?? []
    page.total = data.total ?? 0
    page.pages = data.pages ?? 0
  } catch (e) {
    errorMessage.value = e.message || '加载温度巡检记录失败'
  } finally {
    listLoading.value = false
  }
}

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  if (!form.moldId) {
    errorMessage.value = '请选择模具'
    return
  }
  if (form.temperature == null) {
    errorMessage.value = '请填写温度'
    return
  }

  try {
    const payload = {
      moldId: form.moldId,
      sensorLocation: form.sensorLocation || null,
      temperature: form.temperature,
      operationTime: form.operationTime ? new Date(form.operationTime) : null,
      description: form.description || null,
    }
    const res = await createTemperatureLog(payload)
    const recordId = res && typeof res === 'object' ? res.data : null
    if (recordId && selectedFiles.value.length) {
      await uploadBizFiles(
        recordId,
        selectedFiles.value.map((item) => item.file),
        {
        bizType: 'temperature_log',
        fileType: 'inspection_photo',
        description: '温度巡检记录图片',
        },
      )
    }
    successMessage.value = '温度巡检记录已保存'
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
    resetForm()
    loadList()
  } catch (e) {
    errorMessage.value = e.message || '保存温度巡检记录失败'
  }
}

const resetForm = () => {
  form.moldId = ''
  form.sensorLocation = ''
  form.temperature = null
  form.operationTime = ''
  form.description = ''
}

const handleQuery = () => {
  pageNum.value = 1
  loadList()
}

const handleResetQuery = () => {
  query.moldId = ''
  query.keyword = ''
  query.startDate = ''
  query.endDate = ''
  query.operatorId = ''
  query.sensorLocation = ''
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

onMounted(async () => {
  if (route.query.moldKeyword) {
    query.keyword = String(route.query.moldKeyword)
  }
  await loadMolds()
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

.modal-close:hover {
  color: #ffffff;
}

.modal-body {
  padding: 20px 20px 16px;
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

.selected-files {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-file-item {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  background: #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

.selected-file-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.selected-file-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 18px;
  height: 18px;
  border-radius: 999px;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
}

.message-icon {
  font-size: 16px;
  font-weight: bold;
}

/* 与模具/维修页面保持一致的文件列表样式，用于“预览/删除”按钮 */
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

