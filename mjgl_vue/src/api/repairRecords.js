import request from '@/utils/request'

// 获取所有模具维修记录（按创建时间倒序）
export const fetchAllRepairRecords = () =>
  request.get('/api/repair/getAll')

// 按条件分页查询维修记录（与后端 RepairQueryParam 对应）
export const queryRepairRecords = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/repair/query', param || {}, {
    params: { pageNum, pageSize },
  })

// 创建维修记录（待处理状态）
export const createRepairRecord = (data) =>
  request.post('/api/repair/create', data)

// 更新维修记录（含状态流转）
export const updateRepairRecord = (data) =>
  request.put('/api/repair/update', data)

// 删除维修记录
export const deleteRepairRecord = (id) =>
  request.delete(`/api/repair/record/${id}`)

// 批量删除维修记录
export const batchDeleteRepairRecords = (ids) =>
  request.post('/api/repair/record/batch-delete', { ids: ids || [] })

// 维修记录合理性审批（仅 ADMIN）
export const approveRepairRecord = (id, data) =>
  request.post(`/api/repair/record/${id}/approval`, data)

