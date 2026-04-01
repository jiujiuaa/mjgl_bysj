import request from '@/utils/request'

export const fetchUnreadNotifications = () => request.get('/api/notifications/unread')

export const fetchAllNotifications = () => request.get('/api/notifications/all')

export const markNotificationRead = (id) =>
  request.post(`/api/notifications/${id}/read`)

export const markAllNotificationsRead = () =>
  request.post('/api/notifications/read-all')

