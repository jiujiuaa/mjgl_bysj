<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { sendAlertApi, sendAlertToUserApi } from '@/api/ws'
import { fetchMolds } from '@/api/molds'
import { fetchAllNotifications } from '@/api/notifications'

const authStore = useAuthStore()
const router = useRouter()

const usersLoading = ref(false)
const moldsLoading = ref(false)
const historyList = ref([])
const historyLoading = ref(false)
const pageSize = ref(5)
const currentPage = ref(1)

const totalPages = computed(() => {
  if (!historyList.value.length || !pageSize.value) return 1
  return Math.max(1, Math.ceil(historyList.value.length / pageSize.value))
})

const pagedHistoryList = computed(() => {
  if (!historyList.value.length) return []
  const size = pageSize.value || 10
  const page = Math.min(Math.max(currentPage.value, 1), totalPages.value)
  const start = (page - 1) * size
  return historyList.value.slice(start, start + size)
})

// ALL | SINGLE | MULTI
const targetMode = ref('ALL')

const form = ref({
  title: '',
  content: '',
  type: 'INFO',
  singleUserId: '',
  multiUserIds: [],
  id: '',
  biz_type: 'SYSTEM',
})

const userOptions = computed(
  () => authStore.users?.map((u) => ({ value: u.id, label: u.username || u.realName || u.id })) || [],
)

const molds = ref([])
const bizTypeOptions = ref([
  { value: 'MAINTENANCE', label: '保养相关' },
  { value: 'REPAIR', label: '维修相关' },
  { value: 'USAGE', label: '使用记录相关' },
  { value: 'SYSTEM', label: '系统消息' },
])

const moldIdNameMap = computed(() =>
  molds.value.reduce((acc, m) => {
    if (m.id != null) {
      acc[m.id] = m.moldName || m.name || `模具 ${m.id}`
    }
    return acc
  }, {}),
)

const getMoldName = (id) => {
  if (id == null || id === '') return '无'
  return moldIdNameMap.value[id] || `模具 ${id}`
}

const resolveMoldNameFromNotification = (n) => {
  // 首选：直接使用通知里带的 moldId（适用于使用记录/维修等场景）
  if (n?.moldId != null && n.moldId !== '') {
    return getMoldName(n.moldId)
  }
  // 保养提醒场景：从文案里解析 "模具ID xxx" 作为兜底
  const biz = n?.bizType || n?.biz_type
  if (biz === 'MAINTENANCE_REMINDER' && typeof n?.content === 'string') {
    const match = n.content.match(/模具ID\s+([^\s]+)\s/)
    const rawId = match?.[1]
    if (rawId) {
      const mold =
        molds.value.find((m) => m.id === rawId) ||
        molds.value.find((m) => m.moldCode === rawId)
      if (mold) {
        return mold.moldName || mold.name || mold.moldCode || `模具 ${rawId}`
      }
      return `模具 ${rawId}`
    }
  }
  return '无'
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/home')
}

const sendAlert = async () => {
  try {
    if (!form.value.title || !form.value.content) {
      alert('请填写标题和内容')
      return
    }

    const moldIdRaw = form.value.id
    const moldIdNumber =
      moldIdRaw === '' || moldIdRaw === null || moldIdRaw === undefined
        ? null
        : Number(moldIdRaw)

    const payload = {
      title: form.value.title,
      content: form.value.content,
      type: form.value.type,
      // 后端 DTO 中 id 是 Long，这里只在是有效数字时才传，避免字符串导致 400
      id: Number.isFinite(moldIdNumber) ? moldIdNumber : null,
      biz_type: form.value.biz_type,
    }

    if (targetMode.value === 'ALL') {
      await sendAlertApi(payload)
    } else if (targetMode.value === 'SINGLE') {
      if (!form.value.singleUserId) {
        alert('请选择一个目标用户')
        return
      }
      await sendAlertToUserApi({
        userId: form.value.singleUserId,
        ...payload,
      })
    } else if (targetMode.value === 'MULTI') {
      if (!form.value.multiUserIds || form.value.multiUserIds.length === 0) {
        alert('请至少选择一个目标用户')
        return
      }
      await sendAlertToUserApi({
        userId: form.value.multiUserIds.join(','),
        ...payload,
      })
    }

    alert('消息已发送')

    await loadHistory()

    // 清空标题与内容，其他维度保持，方便连续发送
    form.value = {
      ...form.value,
      title: '',
      content: '',
    }
  } catch (e) {
    alert(e?.message || '发送失败')
  }
}

const loadUsersForSelect = async () => {
  if (!authStore.isAuthenticated) return
  usersLoading.value = true
  try {
    await authStore.loadUsers()
  } finally {
    usersLoading.value = false
  }
}

const loadMoldsForSelect = async () => {
  moldsLoading.value = true
  try {
    const res = await fetchMolds(1, 1000)
    molds.value = res?.data?.list ?? []
  } catch (e) {
    // ignore
  } finally {
    moldsLoading.value = false
  }
}

const formatDateTime = (val) => {
  if (!val) return '-'
  const date = new Date(typeof val === 'string' ? val.replace(' ', 'T') : val)
  if (Number.isNaN(date.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes(),
  )}`
}

const loadHistory = async () => {
  if (!authStore.isAuthenticated) {
    historyList.value = []
    return
  }
  historyLoading.value = true
  try {
    const res = await fetchAllNotifications()
    historyList.value = res.data || []
    currentPage.value = 1
  } catch (e) {
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

onMounted(() => {
  loadUsersForSelect()
  loadMoldsForSelect()
  loadHistory()
})
</script>

<template>
  <div class="alert-page">
    <div class="alert-card">
      <div class="alert-card-header">
        <div>
          <h2 class="alert-title">发送告警 / 通知消息</h2>
          <p class="alert-subtitle">支持发送给全部用户、单个用户或多个指定用户</p>
        </div>
        <button type="button" class="back-btn" @click="goBack">返回</button>
      </div>

      <div class="alert-form">
        <div class="form-row">
          <label>发送目标</label>
          <div class="target-modes">
            <label class="radio-label">
              <input v-model="targetMode" type="radio" value="ALL" />
              全部用户
            </label>
            <label class="radio-label">
              <input v-model="targetMode" type="radio" value="SINGLE" />
              单个用户
            </label>
            <label class="radio-label">
              <input v-model="targetMode" type="radio" value="MULTI" />
              多个用户
            </label>
          </div>
        </div>

        <div v-if="targetMode === 'SINGLE'" class="form-row">
          <label>目标用户</label>
          <select v-model="form.singleUserId">
            <option value="">请选择用户</option>
            <option v-for="u in userOptions" :key="u.value" :value="u.value">
              {{ u.label }}
            </option>
          </select>
        </div>

        <div v-if="targetMode === 'MULTI'" class="form-row">
          <label>目标用户</label>
          <div class="multi-users">
            <div class="multi-actions">
              <button
                type="button"
                class="btn-mini"
                @click="form.multiUserIds = userOptions.map((u) => u.value)"
              >
                全选
              </button>
              <button
                type="button"
                class="btn-mini"
                @click="form.multiUserIds = []"
              >
                清空
              </button>
            </div>
            <div class="multi-list">
              <label
                v-for="u in userOptions"
                :key="u.value"
                class="checkbox-label"
              >
                <input
                  v-model="form.multiUserIds"
                  type="checkbox"
                  :value="u.value"
                />
                <span>{{ u.label }}</span>
              </label>
            </div>
          </div>
        </div>

        <div class="form-row">
          <label>标题</label>
          <input v-model="form.title" type="text" placeholder="请输入消息标题" />
        </div>

        <div class="form-row">
          <label>内容</label>
          <textarea
            v-model="form.content"
            rows="3"
            placeholder="请输入要发送的详细内容"
          />
        </div>

        <div class="form-row">
          <label>类型</label>
          <select v-model="form.type">
            <option value="ERROR">ERROR（严重告警）</option>
            <option value="WARNING">WARNING（预警）</option>
            <option value="INFO">INFO（普通通知）</option>
          </select>
        </div>

        <div class="form-row">
          <label>关联模具</label>
          <select v-model="form.id">
            <option value="">不关联模具</option>
            <option v-for="m in molds" :key="m.id" :value="m.id">
              {{ m.moldName || m.name || `模具 ${m.id}` }}
            </option>
          </select>
        </div>

        <div class="form-row">
          <label>业务类型</label>
          <select v-model="form.biz_type">
            <option v-for="b in bizTypeOptions" :key="b.value" :value="b.value">
              {{ b.label }} ({{ b.value }})
            </option>
          </select>
        </div>

        <div class="form-actions">
          <button class="btn-primary" type="button" @click="sendAlert">
            发送消息
          </button>
        </div>
      </div>

      <div class="history-section">
        <div class="history-header">
          <div class="history-title-wrap">
            <h3 class="history-title">历史消息</h3>
            <span v-if="historyList.length" class="history-count">
              共 {{ historyList.length }} 条
            </span>
          </div>
          <button type="button" class="history-refresh" @click="loadHistory">
            刷新
          </button>
        </div>

        <div v-if="historyLoading" class="history-empty">历史消息加载中...</div>
        <div v-else-if="historyList.length === 0" class="history-empty">
          暂无历史消息
        </div>
        <div v-else class="history-table-wrapper">
          <table class="history-table">
            <thead>
              <tr>
                <th style="width: 80px">类型</th>
                <th style="width: 80px">状态</th>
                <th>标题 / 内容</th>
                <th style="width: 200px">业务 / 模具</th>
                <th style="width: 150px">推送人</th>
                <th style="width: 150px">时间</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="n in pagedHistoryList"
                :key="n.id"
                class="history-row"
                :class="n.readFlag === 0 ? 'history-item-unread' : 'history-item-read'"
              >
                <td>
                  <span
                    class="history-item-type"
                    :class="`history-type-${(n.type || 'INFO').toLowerCase()}`"
                  >
                    {{ n.type || 'INFO' }}
                  </span>
                </td>
                <td>
                  <span
                    class="history-badge"
                    :class="n.readFlag === 0 ? 'history-badge-unread' : 'history-badge-read'"
                  >
                    {{ n.readFlag === 0 ? '未读' : '已读' }}
                  </span>
                </td>
                <td class="history-cell-main">
                  <div class="history-item-title">{{ n.title }}</div>
                  <div class="history-item-content">{{ n.content }}</div>
                </td>
                <td>
                  <div class="history-item-meta">
                    <div>业务: {{ n.bizType || n.biz_type || '-' }}</div>
                    <div>模具: {{ resolveMoldNameFromNotification(n) }}</div>
                  </div>
                </td>
                <td>
                  <span>{{ n.senderName || n.senderId || '系统' }}</span>
                </td>
                <td>
                  <span class="history-item-time">{{ formatDateTime(n.createdAt) }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="historyList.length > 0" class="history-pagination">
          <button
            type="button"
            class="pager-btn"
            :disabled="currentPage <= 1"
            @click="currentPage = Math.max(1, currentPage - 1)"
          >
            上一页
          </button>
          <span class="pager-info">
            第 {{ currentPage }} / {{ totalPages }} 页
          </span>
          <button
            type="button"
            class="pager-btn"
            :disabled="currentPage >= totalPages"
            @click="currentPage = Math.min(totalPages, currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.alert-page {
  min-height: calc(100vh - 56px);
  padding: 32px 16px;
  background: #f3f4f6;
  display: flex;
  justify-content: center;
}

.alert-card {
  width: 100%;
  max-width: 880px;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

.alert-card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #1e3a8a 0%, #1d4ed8 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.alert-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.alert-subtitle {
  font-size: 13px;
  opacity: 0.9;
}

.back-btn {
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.alert-form {
  padding: 20px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.form-row + .form-row {
  margin-top: 4px;
}

.form-row label {
  width: 90px;
  font-size: 13px;
  color: #4b5563;
  padding-top: 8px;
}

.form-row input,
.form-row select,
.form-row textarea {
  flex: 1;
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 13px;
  transition: border-color 0.2s, box-shadow 0.2s, background-color 0.2s;
  background-color: #f9fafb;
}

.form-row textarea {
  resize: vertical;
  min-height: 80px;
}

.form-row input:focus,
.form-row select:focus,
.form-row textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.25);
  background-color: #ffffff;
}

.form-row input::placeholder,
.form-row textarea::placeholder {
  color: #9ca3af;
}

.target-modes {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 4px;
}

.radio-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #374151;
}

.multi-users {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.multi-actions {
  display: flex;
  gap: 8px;
}

.btn-mini {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, transform 0.1s;
}

.btn-mini:hover {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.btn-mini:active {
  transform: translateY(1px);
}

.multi-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  max-height: 140px;
  overflow-y: auto;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px dashed #e5e7eb;
  background: #f9fafb;
}

.checkbox-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #374151;
}

.form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.btn-primary {
  padding: 8px 24px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.35);
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.15s;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.45);
}

.btn-primary:active {
  transform: translateY(0);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.35);
}

.history-section {
  border-top: 1px solid #e5e7eb;
  padding: 14px 24px 20px;
  background: #f9fafb;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.history-title-wrap {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.history-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.history-count {
  font-size: 12px;
  color: #6b7280;
}

.history-refresh {
  border: none;
  background: transparent;
  font-size: 12px;
  color: #2563eb;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 999px;
  transition: background 0.2s, color 0.2s;
}

.history-refresh:hover {
  background: rgba(37, 99, 235, 0.08);
}

.history-empty {
  padding: 8px 0 4px;
  font-size: 13px;
  color: #6b7280;
}

.history-table-wrapper {
  margin-top: 6px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  background: #ffffff;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.history-table thead {
  background: #f3f4f6;
}

.history-table th,
.history-table td {
  padding: 8px 10px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  vertical-align: top;
}

.history-table th {
  font-weight: 600;
  color: #374151;
}

.history-row.history-item-unread {
  background: #fef2f2;
}

.history-row.history-item-read {
  background: #ffffff;
}

.history-row:hover {
  background: #eff6ff;
}

.history-item-type {
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  display: inline-block;
}

.history-type-error {
  background: #fee2e2;
  color: #b91c1c;
}

.history-type-warning {
  background: #fef3c7;
  color: #92400e;
}

.history-type-info {
  background: #dbeafe;
  color: #1d4ed8;
}

.history-type-default {
  background: #e5e7eb;
  color: #374151;
}

.history-item-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.history-badge {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
}

.history-badge-unread {
  background: #fee2e2;
  color: #b91c1c;
}

.history-badge-read {
  background: #e5e7eb;
  color: #4b5563;
}

.history-item-content {
  font-size: 13px;
  color: #374151;
  margin-bottom: 2px;
}

.history-item-meta {
  font-size: 12px;
  color: #6b7280;
}

.history-divider {
  margin: 0 4px;
  color: #d1d5db;
}

.history-pagination {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  font-size: 12px;
  color: #4b5563;
}

.pager-btn {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.2s, border-color 0.2s, color 0.2s, transform 0.1s;
}

.pager-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.pager-btn:not(:disabled):hover {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #1d4ed8;
}

.pager-info {
  min-width: 100px;
  text-align: center;
}

@media (max-width: 640px) {
  .alert-card {
    border-radius: 0;
    max-width: 100%;
  }

  .alert-card-header {
    padding: 16px 18px;
  }

  .alert-form {
    padding: 16px 18px 18px;
  }

  .form-row {
    flex-direction: column;
  }

  .form-row label {
    width: 100%;
    padding-top: 0;
  }

  .form-actions {
    justify-content: stretch;
  }

  .btn-primary {
    width: 100%;
  }

  .history-section {
    padding: 12px 18px 16px;
  }
}
</style>

