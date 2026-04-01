import request from '@/utils/request'

/**
 * 登录接口
 * @param {Object} data - 登录数据 {username, password}
 * @returns {Promise}
 */
export const login = (data) => {
  return request.post('/api/auth/login', data)
}

/**
 * 注册/添加用户接口（需要管理员权限）
 * @param {Object} data - 用户数据 {username, password, realName, age, phone, email, role}
 * @returns {Promise}
 */
export const register = (data) => {
  return request.post('/api/auth/register', data)
}

/**
 * 公开注册（默认普通用户）
 * @param {Object} data - 用户数据 {username, password, realName, phone, email}
 * @returns {Promise}
 */
export const signup = (data) => {
  return request.post('/api/auth/signup', data)
}

/**
 * 查询所有用户（需要管理员权限）
 * @returns {Promise}
 */
export const fetchAllUsers = () => {
  return request.get('/api/auth/alluser')
}

/**
 * 退出登录（可选：通知后端）
 * @returns {Promise}
 */
export const logoutApi = () => {
  return request.post('/api/auth/logout')
}

/**
 * 更新用户信息（需要管理员权限）
 * @param {String} id - 用户ID
 * @param {Object} data - 用户数据 {realName, age, phone, email, role, password(可选)}
 * @returns {Promise}
 */
export const updateUser = (id, data) => {
  return request.put(`/api/auth/user/${id}`, data)
}

/**
 * 删除用户（需要管理员权限）
 * @param {String} id - 用户ID
 * @returns {Promise}
 */
export const deleteUser = (id) => {
  return request.delete(`/api/auth/user/${id}`)
}

/** 批量删除用户（管理员） */
export const batchDeleteUsers = (ids) =>
  request.post('/api/auth/user/batch-delete', { ids: ids || [] })

/**
 * 更新用户状态（启用/禁用）（需要管理员权限）
 * @param {String} id - 用户ID
 * @param {String} status - 状态 "ENABLED" 或 "DISABLED"
 * @returns {Promise}
 */
export const updateUserStatus = (id, status) => {
  return request.put(`/api/auth/user/${id}/status`, { status })
}

/**
 * 条件查询用户（需要管理员权限）
 * @param {Object} params - 查询参数 {realName, role, status, startDate, endDate}
 * @returns {Promise}
 */
export const queryUsers = (params) => {
  return request.get('/api/auth/users/query', { params })
}

/**
 * 获取当前登录用户个人资料
 * @returns {Promise}
 */
export const fetchCurrentUserProfile = () => {
  return request.get('/api/auth/profile')
}

/**
 * 更新当前登录用户个人资料
 * @param {Object} data - 资料数据 {realName, age, phone, email, newPassword}
 * @returns {Promise}
 */
export const updateCurrentUserProfile = (data) => {
  return request.put('/api/auth/profile', data)
}
