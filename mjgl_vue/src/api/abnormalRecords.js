import request from '@/utils/request'

// 异常记录 - 条件分页查询（VO）
export const queryAbnormalRecords = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/mold-abnormal/query', param || {}, {
    params: { pageNum, pageSize },
  })

// 异常记录 - 更新
export const updateAbnormalRecord = (data) =>
  request.put('/api/mold-abnormal/update', data)

// 异常记录 - 删除
export const deleteAbnormalRecord = (id) =>
  request.delete(`/api/mold-abnormal/delete/${id}`)

