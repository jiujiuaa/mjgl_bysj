<template>
  <div class="approval-center-container">
    <AppSidebar />
    <div class="layout-main">
      <header class="top-header">
        <div class="top-title">审批中心</div>
        <div class="top-subtitle">统一处理使用、保养、维修、报废相关审批</div>
      </header>

      <main class="main-content">
        <div class="summary-grid">
          <div class="summary-card">
            <div class="summary-label">使用待审批</div>
            <div class="summary-value">{{ pendingUseRecords.length }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-label">保养待审批</div>
            <div class="summary-value">{{ pendingMaintenanceLogs.length }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-label">维修待审批</div>
            <div class="summary-value">{{ pendingRepairRecords.length }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-label">报废待审批</div>
            <div class="summary-value">{{ pendingScrapApplications.length }}</div>
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

        <div class="card-grid">
          <section class="card">
            <div class="card-header">
              <h3>使用记录审批</h3>
              <div class="card-header-actions">
                <button class="secondary-btn" type="button" @click="toggleSelectAll('use')">
                  {{ isAllSelected('use') ? '取消全选' : '全选' }}
                </button>
                <button class="batch-pass-btn" type="button" :disabled="!selectedCount.use || batchLoading.use" @click="handleBatchApprove('use', 1)">
                  {{ batchLoading.use ? '处理中...' : `一键通过(${selectedCount.use})` }}
                </button>
                <button class="batch-reject-btn" type="button" :disabled="!selectedCount.use || batchLoading.use" @click="handleBatchApprove('use', 2)">
                  一键驳回
                </button>
              </div>
            </div>
            <div class="card-body">
              <table class="mold-table">
                <thead>
                  <tr>
                    <th class="select-col">选</th>
                    <th>模具</th>
                    <th>用途</th>
                    <th>申请人</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="pendingUseRecords.length === 0">
                    <td colspan="6" class="empty-cell">暂无待审批</td>
                  </tr>
                  <tr v-for="item in pendingUseRecords" :key="item.id">
                    <td class="select-col">
                      <input type="checkbox" :checked="isSelected('use', item.id)" @change="toggleItem('use', item.id)" />
                    </td>
                    <td>{{ item.moldCode || '-' }} / {{ item.moldName || '-' }}</td>
                    <td>{{ item.purpose || '-' }}</td>
                    <td>{{ item.applicantName || '-' }}</td>
                    <td><span class="status-tag">{{ item.statusDesc || '-' }}</span></td>
                    <td>
                      <button class="primary-btn small" type="button" @click="openDialog('use', item.id, 1)">审批</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section class="card">
            <div class="card-header">
              <h3>保养记录审批</h3>
              <div class="card-header-actions">
                <button class="secondary-btn" type="button" @click="toggleSelectAll('maintenance')">
                  {{ isAllSelected('maintenance') ? '取消全选' : '全选' }}
                </button>
                <button class="batch-pass-btn" type="button" :disabled="!selectedCount.maintenance || batchLoading.maintenance" @click="handleBatchApprove('maintenance', 1)">
                  {{ batchLoading.maintenance ? '处理中...' : `一键通过(${selectedCount.maintenance})` }}
                </button>
                <button class="batch-reject-btn" type="button" :disabled="!selectedCount.maintenance || batchLoading.maintenance" @click="handleBatchApprove('maintenance', 2)">
                  一键驳回
                </button>
              </div>
            </div>
            <div class="card-body">
              <table class="mold-table">
                <thead>
                  <tr>
                    <th class="select-col">选</th>
                    <th>模具</th>
                    <th>保养类型</th>
                    <th>保养人</th>
                    <th>时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="pendingMaintenanceLogs.length === 0">
                    <td colspan="6" class="empty-cell">暂无待审批</td>
                  </tr>
                  <tr v-for="item in pendingMaintenanceLogs" :key="item.id">
                    <td class="select-col">
                      <input type="checkbox" :checked="isSelected('maintenance', item.id)" @change="toggleItem('maintenance', item.id)" />
                    </td>
                    <td>{{ item.moldCode || '-' }} / {{ item.moldName || '-' }}</td>
                    <td>{{ item.maintenanceType || '-' }}</td>
                    <td>{{ item.maintainerName || '-' }}</td>
                    <td>{{ formatDate(item.createdAt) }}</td>
                    <td>
                      <button class="primary-btn small" type="button" @click="openDialog('maintenance', item.id, 1)">审批</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section class="card">
            <div class="card-header">
              <h3>维修记录审批</h3>
              <div class="card-header-actions">
                <button class="secondary-btn" type="button" @click="toggleSelectAll('repair')">
                  {{ isAllSelected('repair') ? '取消全选' : '全选' }}
                </button>
                <button class="batch-pass-btn" type="button" :disabled="!selectedCount.repair || batchLoading.repair" @click="handleBatchApprove('repair', 1)">
                  {{ batchLoading.repair ? '处理中...' : `一键通过(${selectedCount.repair})` }}
                </button>
                <button class="batch-reject-btn" type="button" :disabled="!selectedCount.repair || batchLoading.repair" @click="handleBatchApprove('repair', 2)">
                  一键驳回
                </button>
              </div>
            </div>
            <div class="card-body">
              <table class="mold-table">
                <thead>
                  <tr>
                    <th class="select-col">选</th>
                    <th>模具</th>
                    <th>故障原因</th>
                    <th>维修人</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="pendingRepairRecords.length === 0">
                    <td colspan="6" class="empty-cell">暂无待审批</td>
                  </tr>
                  <tr v-for="item in pendingRepairRecords" :key="item.id">
                    <td class="select-col">
                      <input type="checkbox" :checked="isSelected('repair', item.id)" @change="toggleItem('repair', item.id)" />
                    </td>
                    <td>{{ item.moldCode || '-' }} / {{ item.moldName || '-' }}</td>
                    <td>{{ item.repairReason || '-' }}</td>
                    <td>{{ item.maintainerName || '-' }}</td>
                    <td><span class="status-tag">{{ item.statusDesc || '-' }}</span></td>
                    <td>
                      <button class="primary-btn small" type="button" @click="openDialog('repair', item.id, 1)">审批</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section class="card">
            <div class="card-header">
              <h3>报废申请审批</h3>
              <div class="card-header-actions">
                <button class="secondary-btn" type="button" @click="toggleSelectAll('scrap')">
                  {{ isAllSelected('scrap') ? '取消全选' : '全选' }}
                </button>
                <button class="batch-pass-btn" type="button" :disabled="!selectedCount.scrap || batchLoading.scrap" @click="handleBatchApprove('scrap', 2)">
                  {{ batchLoading.scrap ? '处理中...' : `一键通过(${selectedCount.scrap})` }}
                </button>
                <button class="batch-reject-btn" type="button" :disabled="!selectedCount.scrap || batchLoading.scrap" @click="handleBatchApprove('scrap', 3)">
                  一键驳回
                </button>
              </div>
            </div>
            <div class="card-body">
              <table class="mold-table">
                <thead>
                  <tr>
                    <th class="select-col">选</th>
                    <th>模具</th>
                    <th>申请原因</th>
                    <th>申请人</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="pendingScrapApplications.length === 0">
                    <td colspan="6" class="empty-cell">暂无待审批</td>
                  </tr>
                  <tr v-for="item in pendingScrapApplications" :key="item.id">
                    <td class="select-col">
                      <input type="checkbox" :checked="isSelected('scrap', item.id)" @change="toggleItem('scrap', item.id)" />
                    </td>
                    <td>{{ item.moldCode || '-' }} / {{ item.moldName || '-' }}</td>
                    <td>{{ item.reason || '-' }}</td>
                    <td>{{ item.applicantName || '-' }}</td>
                    <td><span class="status-tag">{{ item.statusDesc || '-' }}</span></td>
                    <td>
                      <button class="primary-btn small" type="button" @click="openDialog('scrap', item.id, 2)">审批</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </main>
    </div>

    <div v-if="showDialog" class="dialog-overlay" @click="closeDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>{{ dialogTitle }}</h3>
          <button class="dialog-close" @click="closeDialog">×</button>
        </div>
        <div class="dialog-body">
          <form @submit.prevent="handleSubmitApproval">
            <div class="form-group">
              <label>审批结果</label>
              <select v-model.number="approvalForm.status" class="form-input">
                <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>审批意见</label>
              <input v-model="approvalForm.comment" class="form-input" type="text" placeholder="可选" />
            </div>
            <div v-if="dialogError" class="error-message">{{ dialogError }}</div>
            <div class="dialog-actions">
              <button type="submit" class="primary-btn" :disabled="submitLoading">{{ submitLoading ? '提交中...' : '提交审批' }}</button>
              <button type="button" class="secondary-btn" @click="closeDialog">取消</button>
            </div>
          </form>
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
import { useApprovalDashboard } from '@/composables/useApprovalDashboard'

const router = useRouter()
const authStore = useAuthStore()

const {
  pendingUseRecords,
  pendingMaintenanceLogs,
  pendingRepairRecords,
  pendingScrapApplications,
  errorMessage,
  loadApprovalDashboard,
  approveByType,
} = useApprovalDashboard()

const showDialog = ref(false)
const dialogType = ref('')
const approvalForm = reactive({
  id: '',
  status: 0,
  comment: '',
})
const dialogError = ref('')
const submitLoading = ref(false)
const selectedIds = reactive({
  use: [],
  maintenance: [],
  repair: [],
  scrap: [],
})
const batchLoading = reactive({
  use: false,
  maintenance: false,
  repair: false,
  scrap: false,
})

const selectedCount = computed(() => ({
  use: selectedIds.use.length,
  maintenance: selectedIds.maintenance.length,
  repair: selectedIds.repair.length,
  scrap: selectedIds.scrap.length,
}))

const dialogTitle = computed(() => {
  const map = {
    use: '使用记录审批',
    maintenance: '保养记录审批',
    repair: '维修记录审批',
    scrap: '报废申请审批',
  }
  return map[dialogType.value] || '审批'
})

const statusOptions = computed(() => {
  const map = {
    use: [
      { value: 1, label: '合理' },
      { value: 2, label: '存在问题' },
    ],
    maintenance: [
      { value: 1, label: '合理' },
      { value: 2, label: '存在问题' },
    ],
    repair: [
      { value: 1, label: '合理' },
      { value: 2, label: '存在问题' },
    ],
    scrap: [
      { value: 2, label: '通过' },
      { value: 3, label: '拒绝' },
    ],
  }
  return map[dialogType.value] || []
})

const formatDate = (val) => {
  if (!val) return '-'
  const date = new Date(typeof val === 'string' ? val.replace(' ', 'T') : val)
  if (Number.isNaN(date.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}/${pad(date.getMonth() + 1)}/${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const listMap = computed(() => ({
  use: pendingUseRecords.value,
  maintenance: pendingMaintenanceLogs.value,
  repair: pendingRepairRecords.value,
  scrap: pendingScrapApplications.value,
}))

const uniqueIds = (arr) => [...new Set(arr.filter((id) => !!id))]

const isSelected = (type, id) => selectedIds[type].includes(id)

const toggleItem = (type, id) => {
  selectedIds[type] = isSelected(type, id)
    ? selectedIds[type].filter((itemId) => itemId !== id)
    : uniqueIds([...selectedIds[type], id])
}

const isAllSelected = (type) => {
  const list = listMap.value[type] || []
  if (!list.length) return false
  return list.every((item) => selectedIds[type].includes(item.id))
}

const toggleSelectAll = (type) => {
  const list = listMap.value[type] || []
  selectedIds[type] = isAllSelected(type) ? [] : list.map((item) => item.id)
}

const clearSelection = () => {
  selectedIds.use = []
  selectedIds.maintenance = []
  selectedIds.repair = []
  selectedIds.scrap = []
}

const loadAll = async () => {
  await loadApprovalDashboard()
  clearSelection()
}

const openDialog = (type, id, defaultStatus) => {
  dialogType.value = type
  approvalForm.id = id
  approvalForm.status = defaultStatus
  approvalForm.comment = ''
  dialogError.value = ''
  showDialog.value = true
}

const closeDialog = () => {
  showDialog.value = false
}

const handleSubmitApproval = async () => {
  if (!approvalForm.id || !dialogType.value) return
  dialogError.value = ''
  submitLoading.value = true
  try {
    await approveByType(dialogType.value, approvalForm.id, approvalForm.status, approvalForm.comment)
    showDialog.value = false
    await loadAll()
  } catch (e) {
    dialogError.value = e.message || '审批提交失败'
  } finally {
    submitLoading.value = false
  }
}

const handleBatchApprove = async (type, status) => {
  const ids = selectedIds[type]
  if (!ids.length) return
  const confirmText = status === 2 || status === 3 ? '驳回' : '通过'
  if (!window.confirm(`确定要批量${confirmText}选中的 ${ids.length} 条记录吗？`)) return
  batchLoading[type] = true
  errorMessage.value = ''
  try {
    const results = await Promise.allSettled(ids.map((id) => approveByType(type, id, status, null)))
    const failed = results.filter((item) => item.status === 'rejected').length
    if (failed > 0) {
      errorMessage.value = `批量审批完成，成功 ${ids.length - failed} 条，失败 ${failed} 条`
    }
    await loadAll()
  } catch (e) {
    errorMessage.value = e.message || '批量审批失败'
  } finally {
    batchLoading[type] = false
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await loadAll()
})
</script>

<style scoped>
.approval-center-container { min-height: 100vh; background: #eef2ff; display: flex; }
.layout-main { flex: 1; padding: 24px; }
.top-header { padding: 8px 20px 14px; }
.top-title { font-size: 24px; font-weight: 700; color: #1e3a8a; }
.top-subtitle { font-size: 13px; color: #64748b; margin-top: 4px; }
.main-content { width: 100%; max-width: 1500px; }

.summary-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; margin-bottom: 16px; }
.summary-card { background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); border: 1px solid #dbeafe; border-radius: 12px; padding: 14px 16px; box-shadow: 0 4px 12px rgba(30, 64, 175, 0.08); }
.summary-label { font-size: 12px; color: #475569; }
.summary-value { margin-top: 6px; font-size: 26px; color: #1d4ed8; font-weight: 700; }

.card-grid { display: grid; grid-template-columns: 1fr; gap: 16px; }
.card { background: #fff; border-radius: 12px; box-shadow: 0 6px 20px rgba(15, 23, 42, 0.08); overflow: hidden; border: 1px solid #e2e8f0; }
.card-header { padding: 14px 18px; border-bottom: 1px solid #e5e7eb; background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.card-header h3 { margin: 0; font-size: 16px; color: #1f2937; }
.card-header-actions { display: flex; align-items: center; gap: 8px; }
.card-body { padding: 12px 16px 14px; }

.mold-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.mold-table th, .mold-table td { padding: 8px 10px; border-bottom: 1px solid #e5e7eb; text-align: left; vertical-align: middle; }
.mold-table th { background: #f8fafc; font-weight: 600; color: #334155; }
.select-col { width: 42px; text-align: center; }
.empty-cell { text-align: center; color: #94a3b8; padding: 20px; }
.status-tag { display: inline-block; padding: 2px 8px; border-radius: 999px; background: #eff6ff; color: #1d4ed8; font-size: 12px; }

.primary-btn { padding: 8px 14px; background: linear-gradient(135deg, #1e40af 0%, #2563eb 100%); color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.primary-btn.small { padding: 4px 10px; font-size: 12px; }
.secondary-btn { padding: 8px 14px; background: #fff; color: #374151; border: 1px solid #d1d5db; border-radius: 6px; cursor: pointer; }
.batch-pass-btn { padding: 8px 12px; border: none; border-radius: 6px; cursor: pointer; background: #16a34a; color: #fff; }
.batch-reject-btn { padding: 8px 12px; border: none; border-radius: 6px; cursor: pointer; background: #dc2626; color: #fff; }
.batch-pass-btn:disabled, .batch-reject-btn:disabled, .primary-btn:disabled { opacity: .55; cursor: not-allowed; }

.error-message { margin-bottom: 12px; color: #991b1b; background: #fee2e2; border: 1px solid #fecaca; padding: 10px 12px; border-radius: 6px; }
.dialog-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.6); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog-content { background: #fff; border-radius: 12px; width: 92%; max-width: 460px; overflow: hidden; }
.dialog-header { padding: 16px 18px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); }
.dialog-header h3 { margin: 0; font-size: 16px; color: #fff; }
.dialog-close { border: none; background: rgba(255,255,255,.15); color: #fff; width: 28px; height: 28px; border-radius: 4px; cursor: pointer; font-size: 18px; }
.dialog-body { padding: 16px 18px; }
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.form-input { padding: 8px 10px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; }
.dialog-actions { display: flex; gap: 10px; }

@media (max-width: 1200px) { .summary-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 768px) {
  .layout-main { padding: 16px; }
  .summary-grid { grid-template-columns: 1fr; }
  .card-header { flex-direction: column; align-items: flex-start; }
  .card-header-actions { flex-wrap: wrap; }
}
</style>
