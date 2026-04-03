const parsePositiveInt = (raw, fallback) => {
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? Math.floor(n) : fallback
}

/** 下拉/全量拉取模具列表时的每页上限（与后端分页配合） */
export const LIST_PAGE_SIZE_MAX = parsePositiveInt(import.meta.env.VITE_LIST_PAGE_SIZE_MAX, 1000)

/** 审批看板一次拉取的日志条数上限 */
export const APPROVAL_DASHBOARD_PAGE_SIZE = parsePositiveInt(import.meta.env.VITE_APPROVAL_DASHBOARD_PAGE_SIZE, 200)
