import request from '@/utils/request'

// 分页查询模具列表（全部）
export const fetchMolds = (pageNum = 1, pageSize = 10) =>
  request.get('/api/molds/allmolds', {
    params: { pageNum, pageSize },
  })

// 按条件分页查询模具
export const queryMolds = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/molds/query', param, {
    params: { pageNum, pageSize },
  })

// 创建模具（主表 + 技术参数 + 二维码类型）
export const createMold = (data) =>
  request.post('/api/molds/create', data)

// 更新模具
export const updateMold = (data) =>
  request.put('/api/molds/update', data)

// 删除模具
export const deleteMold = (id) =>
  request.delete(`/api/molds/${id}`)

