import request from '@/utils/request'

// 按条件分页查询保养提醒（与后端 MaintenanceReminderQueryParam 对应）
export const queryMaintenanceReminders = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/maintenance-reminder/query', param || {}, {
    params: { pageNum, pageSize },
  })

// 按模具ID查询该模具的保养提醒列表
export const fetchRemindersByMoldId = (moldId) =>
  request.get(`/api/maintenance-reminder/list/mold/${moldId}`)

// 根据ID获取单条保养提醒
export const fetchReminderById = (id) =>
  request.get(`/api/maintenance-reminder/${id}`)

// 主动发送保养提醒消息
export const sendMaintenanceReminder = (id) =>
  request.post(`/api/maintenance-reminder/${id}/send`)

// 忽略该条保养提醒（置为已忽略）
export const ignoreMaintenanceReminder = (id) =>
  request.post(`/api/maintenance-reminder/${id}/ignore`)

