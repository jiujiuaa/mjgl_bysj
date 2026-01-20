import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { 
  login as loginApi, 
  register as registerApi, 
  fetchAllUsers, 
  logoutApi,
  updateUser as updateUserApi,
  deleteUser as deleteUserApi,
  updateUserStatus as updateUserStatusApi,
  queryUsers as queryUsersApi
} from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const users = ref([])

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  // 登录方法
  // 对应后端：POST /api/auth/login
  // 请求：LoginDTO {username, password}
  // 响应：Result<LoginVO> {code: 200, message: "success", data: {token, username}}
  const login = async (loginData) => {
    try {
      const response = await loginApi(loginData)
      // response 是 Result<LoginVO> 对象
      if (response.code === 200 && response.data) {
        token.value = response.data.token
        username.value = response.data.username
        role.value = response.data.role || ''
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('username', response.data.username)
        localStorage.setItem('role', response.data.role || '')
        return { success: true }
      }
      return { success: false, message: response.message || '登录失败' }
    } catch (error) {
      // 捕获后端返回的错误信息（如：用户名或密码错误）
      return { success: false, message: error.message || '登录失败' }
    }
  }

  // 登出方法（先调用后端，再清理本地状态）
  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      // 后端无状态登出，失败也不影响前端清理
      console.error('logout error:', error)
    }
    token.value = ''
    username.value = ''
    role.value = ''
    users.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }

  // 添加用户方法（管理员功能）
  // 对应后端：POST /api/auth/register
  // 请求：RegisterDTO {username, password, realName, age, phone, email, role}
  // 响应：Result<String> {code: 200, message: "success", data: null}
  // 权限：需要 ADMIN 角色（@PreAuthorize("hasRole('ADMIN')")）
  const addUser = async (userData) => {
    try {
      const response = await registerApi(userData)
      // response 是 Result<String> 对象
      if (response.code === 200) {
        return { success: true, message: '添加用户成功' }
      }
      return { success: false, message: response.message || '添加用户失败' }
    } catch (error) {
      // 捕获后端返回的错误信息（如：用户名不能为空、用户名已存在等）
      return { success: false, message: error.message || '添加用户失败' }
    }
  }

  // 查询所有用户（管理员功能）
  // 对应后端：GET /api/auth/alluser
  // 响应：Result<List<UserVO>>
  const loadUsers = async () => {
    try {
      const response = await fetchAllUsers()
      if (response.code === 200) {
        users.value = response.data || []
        return { success: true }
      }
      return { success: false, message: response.message || '查询用户失败' }
    } catch (error) {
      return { success: false, message: error.message || '查询用户失败' }
    }
  }

  // 更新用户信息（管理员功能）
  const updateUser = async (id, userData) => {
    try {
      const response = await updateUserApi(id, userData)
      if (response.code === 200) {
        return { success: true, message: response.message || '更新用户成功' }
      }
      return { success: false, message: response.message || '更新用户失败' }
    } catch (error) {
      return { success: false, message: error.message || '更新用户失败' }
    }
  }

  // 删除用户（管理员功能）
  const removeUser = async (id) => {
    try {
      const response = await deleteUserApi(id)
      if (response.code === 200) {
        return { success: true, message: response.message || '删除用户成功' }
      }
      return { success: false, message: response.message || '删除用户失败' }
    } catch (error) {
      return { success: false, message: error.message || '删除用户失败' }
    }
  }

  // 更新用户状态（管理员功能）
  const changeUserStatus = async (id, status) => {
    try {
      const response = await updateUserStatusApi(id, status)
      if (response.code === 200) {
        return { success: true, message: response.message || '更新用户状态成功' }
      }
      return { success: false, message: response.message || '更新用户状态失败' }
    } catch (error) {
      return { success: false, message: error.message || '更新用户状态失败' }
    }
  }

  // 条件查询用户（管理员功能）
  const searchUsers = async (params) => {
    try {
      const response = await queryUsersApi(params)
      if (response.code === 200) {
        users.value = response.data || []
        return { success: true }
      }
      return { success: false, message: response.message || '查询用户失败' }
    } catch (error) {
      return { success: false, message: error.message || '查询用户失败' }
    }
  }

  return {
    token,
    username,
    role,
    users,
    isAuthenticated,
    isAdmin,
    login,
    logout,
    addUser,
    loadUsers,
    updateUser,
    removeUser,
    changeUserStatus,
    searchUsers,
  }
})
