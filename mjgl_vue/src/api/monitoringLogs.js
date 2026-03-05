import request from '@/utils/request'

// 温度巡检记录
export const createTemperatureLog = (data) =>
  request.post('/api/temperature-log/create', data)

export const queryTemperatureLogs = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/temperature-log/query', param || {}, {
    params: { pageNum, pageSize },
  })

// 润滑巡检记录
export const createLubricationLog = (data) =>
  request.post('/api/lubrication-log/create', data)

export const queryLubricationLogs = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/lubrication-log/query', param || {}, {
    params: { pageNum, pageSize },
  })

// 人工异常上报
export const createManualAbnormal = (data) =>
  request.post('/api/mold-abnormal/manual-create', data)

