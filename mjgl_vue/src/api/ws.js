import request from '@/utils/request'

// 广播告警（所有订阅 /topic/alerts 的客户端都能收到）
export const sendAlertApi = (data) => request.post('/api/ws/send', data)

// 按用户ID单播告警（订阅 /topic/alerts.user.{userId} 的客户端收到）
export const sendAlertToUserApi = (data) => request.post('/api/ws/sendToUser', data)

