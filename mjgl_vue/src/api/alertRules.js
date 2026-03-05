import request from '@/utils/request'

/** 获取所有预警规则 */
export const listAlertRules = () =>
  request.get('/api/alert-rules/list')

/** 根据 ID 获取单条规则 */
export const getAlertRuleById = (id) =>
  request.get(`/api/alert-rules/${id}`)

/** 新增/编辑规则 */
export const saveAlertRule = (data) =>
  request.post('/api/alert-rules/save', data)

/** 删除规则 */
export const deleteAlertRule = (id) =>
  request.delete(`/api/alert-rules/${id}`)

/** 设置规则启用状态：enabled 0-禁用 1-启用 */
export const setAlertRuleEnabled = (id, enabled) =>
  request.put(`/api/alert-rules/${id}/enabled`, null, {
    params: { enabled },
  })

/** 初始化默认规则（若不存在则插入） */
export const initDefaultAlertRules = () =>
  request.post('/api/alert-rules/init-defaults')
