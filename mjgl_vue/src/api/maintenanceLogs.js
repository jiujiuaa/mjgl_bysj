import request from '@/utils/request'

// 按条件分页查询保养记录（与后端 MaintenanceLogQueryParam 对应）
export const queryMaintenanceLogs = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/mainlog/query', param || {}, {
    params: { pageNum, pageSize },
  })

// 新建保养记录
export const createMaintenanceLog = (data) =>
  request.post('/api/mainlog/create', data)

// 更新保养记录
export const updateMaintenanceLog = (data) =>
  request.put('/api/mainlog/edit', data)

// 删除保养记录
export const deleteMaintenanceLog = (id) =>
  request.delete(`/api/mainlog/deletelog/${id}`)

// 批量删除保养记录
export const batchDeleteMaintenanceLogs = (ids) =>
  request.post('/api/mainlog/batch-delete', { ids: ids || [] })

// 保养记录合理性审批（仅 ADMIN）
export const approveMaintenanceLog = (id, data) =>
  request.post(`/api/mainlog/log/${id}/approval`, data)

