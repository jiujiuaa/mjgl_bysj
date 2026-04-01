import request from '@/utils/request'

export const queryMoldStats = (param) =>
  request.post('/api/mold-statistics/mold-stats', param || {})

export const queryMoldTrends = (param) =>
  request.post('/api/mold-statistics/trends', param || {})

export const exportMoldStatsCsv = (param) =>
  request.post('/api/mold-statistics/export/mold-stats', param || {}, {
    responseType: 'blob',
  })

export const exportMoldStatsXlsx = (param) =>
  request.post('/api/mold-statistics/export/mold-stats-xlsx', param || {}, {
    responseType: 'blob',
  })

