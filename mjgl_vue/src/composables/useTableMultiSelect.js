import { ref } from 'vue'

/**
 * 表格多选：按行 id 收集，支持当前页全选/取消。
 * @param {() => Array<{ id?: string }>} getPageRows 返回当前页行数据（需含 id）
 */
export function useTableMultiSelect(getPageRows) {
  const selectedIds = ref([])

  const isSelected = (id) => id != null && selectedIds.value.includes(id)

  const toggleRow = (id) => {
    if (id == null || id === '') return
    const i = selectedIds.value.indexOf(id)
    if (i >= 0) {
      selectedIds.value = selectedIds.value.filter((x) => x !== id)
    } else {
      selectedIds.value = [...selectedIds.value, id]
    }
  }

  const pageRowIds = () =>
    (getPageRows() || [])
      .map((r) => r?.id)
      .filter((id) => id != null && String(id).trim() !== '')

  const isAllPageSelected = () => {
    const ids = pageRowIds()
    return ids.length > 0 && ids.every((id) => selectedIds.value.includes(id))
  }

  const toggleSelectAllPage = (checked) => {
    const ids = pageRowIds()
    if (checked) {
      const merged = new Set(selectedIds.value)
      ids.forEach((id) => merged.add(id))
      selectedIds.value = [...merged]
    } else {
      const drop = new Set(ids)
      selectedIds.value = selectedIds.value.filter((id) => !drop.has(id))
    }
  }

  const clearSelection = () => {
    selectedIds.value = []
  }

  return {
    selectedIds,
    isSelected,
    toggleRow,
    isAllPageSelected,
    toggleSelectAllPage,
    clearSelection,
  }
}
