import request from '@/utils/request'

// 获取所有模具使用记录（按创建时间倒序，后端已处理排序）
export const fetchAllUseRecords = () =>
  request.get('/api/molduse/getall')

// 根据模具ID获取该模具的所有使用记录
export const fetchUseRecordsByMoldId = (moldId) =>
  request.get(`/api/molduse/record/mold/${moldId}`)

// 根据记录ID获取单条使用记录
export const fetchUseRecordById = (id) =>
  request.get(`/api/molduse/record/${id}`)

// 创建模具使用/借出记录
export const createUseRecord = (data) =>
  request.post('/api/molduse/create', data)

// 更新模具使用记录（实际时间、归还信息等）
export const updateUseRecord = (data) =>
  request.put('/api/molduse/updateRecord', data)

// 更新使用记录状态（例如开始使用、归还完成等，具体状态含义由后端枚举控制）
export const updateUseRecordStatus = (id, status) =>
  request.put(`/api/molduse/record/${id}/status`, { status })

// 删除使用记录（同时由后端根据需要同步更新模具状态）
export const deleteUseRecord = (id) =>
  request.delete(`/api/molduse/record/${id}`)

// 合理性审批：status 1=合理,2=存在问题
export const approveUseRecord = (id, data) =>
  request.post(`/api/molduse/record/${id}/approval`, data)

