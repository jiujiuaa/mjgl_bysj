import request from '@/utils/request'

export const fetchBusinessConfigList = () => request.get('/api/admin/business-config')

export const saveBusinessConfigBatch = (items) =>
  request.put('/api/admin/business-config', { items })
