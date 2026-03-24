import request from '@/utils/request'

// 按条件分页查询保养计划（与后端 MaintenancePlanQueryParam 对应，直接返回带模具信息 VO）
export const queryMaintenancePlans = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/maintenanceplan/query', param || {}, {
    params: { pageNum, pageSize },
  })

// 新建保养计划
export const createMaintenancePlan = (data) =>
  request.post('/api/maintenanceplan/add', data)

// 批量新建保养计划
export const createMaintenancePlansBatch = (dataList) =>
  request.post('/api/maintenanceplan/addbatch', dataList || [])

// 更新保养计划
export const updateMaintenancePlan = (data) =>
  request.put('/api/maintenanceplan/edit', data)

// 删除保养计划
export const deleteMaintenancePlan = (id) =>
  request.delete(`/api/maintenanceplan/plan/${id}`)

// 批量删除保养计划
export const batchDeleteMaintenancePlans = (ids) =>
  request.post('/api/maintenanceplan/plan/batch-delete', { ids: ids || [] })

// 启用保养计划
export const enableMaintenancePlan = (id) =>
  request.put(`/api/maintenanceplan/enable/${id}`)

// 停用保养计划
export const disableMaintenancePlan = (id) =>
  request.put(`/api/maintenanceplan/disable/${id}`)

