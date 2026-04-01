import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 侧边栏折叠状态，跨路由保持（避免切换页面后分组自动展开）
 */
export const useSidebarStore = defineStore('sidebar', () => {
  // 默认不展开：避免进入页面时“运维管理/监测与异常”自动下拉
  const showOpsChildren = ref(false)
  const showMonitoringChildren = ref(false)

  function toggleOps() {
    showOpsChildren.value = !showOpsChildren.value
  }

  function toggleMonitoring() {
    showMonitoringChildren.value = !showMonitoringChildren.value
  }

  return {
    showOpsChildren,
    showMonitoringChildren,
    toggleOps,
    toggleMonitoring,
  }
})
