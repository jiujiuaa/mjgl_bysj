import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 侧边栏折叠状态，跨路由保持（避免切换页面后分组自动展开）
 */
export const useSidebarStore = defineStore('sidebar', () => {
  const showOpsChildren = ref(true)
  const showMonitoringChildren = ref(true)

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
