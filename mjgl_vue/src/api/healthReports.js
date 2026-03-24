import request from '@/utils/request'

// 生成周期性健康评估报告（可选同步导出 PDF）
export const generateHealthReports = (data) =>
  request.post('/api/health-reports/generate', data || {})

// 分页条件查询健康报告列表
export const queryHealthReports = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/health-reports/query', param || {}, {
    params: { pageNum, pageSize },
  })

// 导出指定健康报告 PDF（并更新归档状态）
export const exportHealthReportPdf = (id) =>
  request.post(`/api/health-reports/${id}/export-pdf`)

// 删除指定健康报告（同时删除 PDF 归档文件）
export const deleteHealthReport = (id) =>
  request.post(`/api/health-reports/${id}/delete`)

// 批量删除健康报告
export const batchDeleteHealthReports = (ids) =>
  request.post('/api/health-reports/batch-delete', { ids: ids || [] })

// 获取单条报告
export const getHealthReportById = (id) => request.get(`/api/health-reports/${id}`)

