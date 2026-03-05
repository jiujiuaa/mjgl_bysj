import request from '@/utils/request'

/** 分页条件查询报警记录（与后端 AlertRecordQueryParam 对应） */
export const queryAlerts = (param, pageNum = 1, pageSize = 10) =>
  request.post('/api/alerts/query', param || {}, {
    params: { pageNum, pageSize },
  })

/** 根据 ID 查询单条报警 */
export const getAlertById = (id) =>
  request.get(`/api/alerts/${id}`)

/** 标记为已解决 */
export const resolveAlert = (id, remark) =>
  request.put(`/api/alerts/${id}/resolve`, null, {
    params: remark ? { remark } : {},
  })

/** 标记为已忽略 */
export const ignoreAlert = (id, remark) =>
  request.put(`/api/alerts/${id}/ignore`, null, {
    params: remark ? { remark } : {},
  })

/** 手动触发一次智能预警规则引擎 */
export const runAlertRules = () =>
  request.post('/api/alerts/run-rules')
