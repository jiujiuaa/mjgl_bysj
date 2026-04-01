import request from '@/utils/request'

// 创建模具报废申请
export const createMoldScrapApplication = (data) =>
  request.post('/api/moldscrap/create', data)

// 审批报废申请（status: 2=已批准, 3=已拒绝）
export const approveMoldScrapApplication = (id, data) =>
  request.put(`/api/moldscrap/approve/${id}`, data)

// 执行报废
export const executeMoldScrapApplication = (id, data) =>
  request.put(`/api/moldscrap/execute/${id}`, data)

// 获取报废申请详情（含时间线）
export const fetchMoldScrapApplicationDetail = (id) =>
  request.get(`/api/moldscrap/detail/${id}`)

// 条件列表查询
export const fetchMoldScrapApplications = (params) =>
  request.get('/api/moldscrap/list', { params: params || {} })

