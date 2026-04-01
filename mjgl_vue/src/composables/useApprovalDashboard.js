import { ref } from 'vue'
import { fetchAllUseRecords, approveUseRecord } from '@/api/useRecords'
import { queryMaintenanceLogs, approveMaintenanceLog } from '@/api/maintenanceLogs'
import { queryRepairRecords, approveRepairRecord } from '@/api/repairRecords'
import { fetchMoldScrapApplications, approveMoldScrapApplication } from '@/api/moldScrapApplications'

export const useApprovalDashboard = () => {
  const pendingUseRecords = ref([])
  const pendingMaintenanceLogs = ref([])
  const pendingRepairRecords = ref([])
  const pendingScrapApplications = ref([])
  const loading = ref(false)
  const errorMessage = ref('')

  const loadApprovalDashboard = async () => {
    loading.value = true
    errorMessage.value = ''
    try {
      const [useRes, maintenanceRes, repairRes, scrapRes] = await Promise.all([
        fetchAllUseRecords(),
        queryMaintenanceLogs({}, 1, 200),
        queryRepairRecords({}, 1, 200),
        fetchMoldScrapApplications({ status: 1 }),
      ])

      pendingUseRecords.value = (useRes.data || []).filter((r) => r.status === 3)
      pendingMaintenanceLogs.value = (maintenanceRes.data?.list || []).filter((r) => (r.approvalStatus ?? 0) === 0)
      pendingRepairRecords.value = (repairRes.data?.list || []).filter((r) => r.status === 4)
      pendingScrapApplications.value = scrapRes.data || []
    } catch (e) {
      errorMessage.value = e.message || '加载审批数据失败'
    } finally {
      loading.value = false
    }
  }

  const approveByType = (type, id, status, comment) => {
    const payload = { status, comment: comment || null }
    const handlers = {
      use: () => approveUseRecord(id, payload),
      maintenance: () => approveMaintenanceLog(id, payload),
      repair: () => approveRepairRecord(id, payload),
      scrap: () => approveMoldScrapApplication(id, payload),
    }
    return handlers[type]?.()
  }

  return {
    pendingUseRecords,
    pendingMaintenanceLogs,
    pendingRepairRecords,
    pendingScrapApplications,
    loading,
    errorMessage,
    loadApprovalDashboard,
    approveByType,
  }
}

