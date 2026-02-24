<template>
  <div class="user-management-container">
    <!-- 左侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">管理系统</div>
      </div>
      <nav class="sidebar-menu">
        <!-- 普通员工不显示“用户管理”菜单，其它业务模块照常显示 -->
        <div
          v-if="authStore.isAdmin"
          class="menu-item active"
        >
          <span class="menu-icon">👤</span>
          <span>用户管理</span>
        </div>
        <div
          class="menu-item"
          @click="router.push('/mold-management')"
        >
          <span class="menu-icon">🧱</span>
          <span>模具管理</span>
        </div>
        <div class="menu-item disabled">
          <span class="menu-icon">🛠</span>
          <span>运维管理</span>
        </div>
        <div class="menu-item disabled">
          <span class="menu-icon">📈</span>
          <span>监测与异常</span>
        </div>
        <div class="menu-item disabled">
          <span class="menu-icon">❤️</span>
          <span>健康评估</span>
        </div>
      </nav>
      <div class="sidebar-footer">
        <span class="sidebar-username">{{ authStore.username }}</span>
        <button class="sidebar-logout" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <!-- 右侧主区域 -->
    <div class="layout-main">
      <!-- 顶部条 -->
      <header class="top-header">
        <div class="top-title">用户管理</div>
        <div class="top-subtitle">添加和管理系统用户</div>
      </header>

      <!-- 主内容区域 -->
      <main class="main-content">
        <div class="content-wrapper">
          <!-- 只有管理员才显示用户管理模块 -->
          <section v-if="authStore.isAdmin">
            <!-- 用户列表 -->
            <div class="card user-list-card">
              <div class="card-header">
                <h3 class="card-title">用户列表</h3>
                <button @click="handleShowAddDialog" class="add-user-btn">
                  <span class="btn-icon">+</span>
                  添加用户
                </button>
              </div>
              <div class="card-body">
                <!-- 查询过滤器 -->
                <form @submit.prevent="handleQueryUsers" class="filter-form">
                  <div class="filter-row">
                    <input
                      v-model="queryForm.realName"
                      type="text"
                      placeholder="姓名"
                      class="filter-input"
                    />
                    <select v-model="queryForm.role" class="filter-input">
                      <option value="">全部角色</option>
                      <option value="USER">普通用户</option>
                      <option value="INSPECTOR">检查员</option>
                      <option value="OPERATOR">操作员</option>
                      <option value="ADMIN">管理员</option>
                    </select>
                    <select v-model.number="queryForm.enabled" class="filter-input">
                      <option value="">全部状态</option>
                      <option :value="1">启用</option>
                      <option :value="0">禁用</option>
                    </select>
                    <input
                      v-model="queryForm.startDate"
                      type="datetime-local"
                      step="3600"
                      placeholder="开始时间"
                      class="filter-input"
                    />
                    <input
                      v-model="queryForm.endDate"
                      type="datetime-local"
                      step="3600"
                      placeholder="结束时间"
                      class="filter-input"
                    />
                    <button type="submit" :disabled="queryLoading" class="filter-btn filter-btn-primary">
                      {{ queryLoading ? '查询中' : '查询' }}
                    </button>
                    <button type="button" @click="handleResetQuery" class="filter-btn filter-btn-secondary">
                      重置
                    </button>
                  </div>
                </form>
                <div v-if="successMessage" class="success-message">
                  <span class="message-icon">✓</span>
                  {{ successMessage }}
                </div>
                <div v-if="errorMessage" class="error-message">
                  <span class="message-icon">⚠</span>
                  {{ errorMessage }}
                </div>
                <div v-if="userListLoading" class="table-loading">用户列表加载中...</div>
                <div v-else>
                  <table class="user-table">
                    <thead>
                      <tr>
                        <th>用户名</th>
                        <th>真实姓名</th>
                        <th>角色</th>
                        <th>状态</th>
                        <th>手机号</th>
                        <th>邮箱</th>
                        <th>创建时间</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-if="!authStore.users || authStore.users.length === 0">
                        <td colspan="8" class="empty-cell">暂无用户数据</td>
                      </tr>
                      <tr v-for="user in authStore.users" :key="user.id || user.username">
                        <td>{{ user.username }}</td>
                        <td>{{ user.realName }}</td>
                        <td>{{ formatRole(user.role) }}</td>
                        <td>
                          <span :class="getStatusClass(user.status)">
                            {{ formatStatus(user.status) }}
                          </span>
                        </td>
                        <td>{{ user.phone || '-' }}</td>
                        <td>{{ user.email || '-' }}</td>
                        <td>{{ formatDate(user.createdAt) }}</td>
                        <td>
                          <div class="action-buttons">
                            <button @click="handleEditUser(user)" class="action-btn edit-btn">编辑</button>
                            <button 
                              @click="handleToggleStatus(user)" 
                              :class="['action-btn', user.status === 'ENABLED' || (user.status && user.status.code === 1) ? 'disable-btn' : 'enable-btn']"
                            >
                              {{ user.status === 'ENABLED' || (user.status && user.status.code === 1) ? '禁用' : '启用' }}
                            </button>
                            <button @click="handleDeleteUser(user)" class="action-btn delete-btn">删除</button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <!-- 确认对话框 -->
            <div v-if="showConfirmDialog" class="dialog-overlay" @click="showConfirmDialog = false">
              <div class="confirm-dialog" @click.stop>
                <div :class="['confirm-icon', confirmConfig.type]">
                  <span v-if="confirmConfig.type === 'warning'">⚠️</span>
                  <span v-else-if="confirmConfig.type === 'danger'">🗑️</span>
                </div>
                <h3 class="confirm-title">{{ confirmConfig.title }}</h3>
                <p class="confirm-message">{{ confirmConfig.message }}</p>
                <div class="confirm-actions">
                  <button 
                    @click="showConfirmDialog = false" 
                    class="confirm-cancel-btn"
                  >
                    {{ confirmConfig.cancelText }}
                  </button>
                  <button 
                    @click="() => { confirmConfig.onConfirm?.(); showConfirmDialog = false; }" 
                    :class="['confirm-ok-btn', confirmConfig.type]"
                  >
                    {{ confirmConfig.confirmText }}
                  </button>
                </div>
              </div>
            </div>

            <!-- 添加用户对话框 -->
            <div v-if="showAddDialog" class="dialog-overlay" @click="showAddDialog = false">
              <div class="dialog-content" @click.stop>
                <div class="dialog-header">
                  <h3>添加新用户</h3>
                  <button @click="showAddDialog = false" class="dialog-close">×</button>
                </div>
                <div class="dialog-body">
                  <form @submit.prevent="handleAddUser" class="add-user-form">
                    <div class="form-row">
                      <div class="form-group">
                        <label for="addUsername">用户名 *</label>
                        <input
                          id="addUsername"
                          v-model="userForm.username"
                          type="text"
                          placeholder="请输入用户名"
                          required
                          class="form-input"
                        />
                      </div>
                      <div class="form-group">
                        <label for="addPassword">密码 *</label>
                        <input
                          id="addPassword"
                          v-model="userForm.password"
                          type="password"
                          placeholder="请输入密码"
                          required
                          class="form-input"
                        />
                      </div>
                    </div>
                    <div class="form-row">
                      <div class="form-group">
                        <label for="addRealName">真实姓名 *</label>
                        <input
                          id="addRealName"
                          v-model="userForm.realName"
                          type="text"
                          placeholder="请输入真实姓名"
                          required
                          class="form-input"
                        />
                      </div>
                      <div class="form-group">
                        <label for="addRole">角色 *</label>
                        <select id="addRole" v-model="userForm.role" required class="form-input">
                          <option value="">请选择角色</option>
                          <option value="USER">普通用户</option>
                          <option value="INSPECTOR">检查员</option>
                          <option value="OPERATOR">操作员</option>
                          <option value="ADMIN">管理员</option>
                        </select>
                      </div>
                    </div>
                    <div class="form-row">
                      <div class="form-group">
                        <label for="addAge">年龄</label>
                        <input
                          id="addAge"
                          v-model.number="userForm.age"
                          type="number"
                          placeholder="请输入年龄"
                          min="0"
                          class="form-input"
                        />
                      </div>
                      <div class="form-group">
                        <label for="addPhone">手机号</label>
                        <input
                          id="addPhone"
                          v-model="userForm.phone"
                          type="tel"
                          placeholder="请输入手机号"
                          class="form-input"
                        />
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="addEmail">邮箱</label>
                      <input
                        id="addEmail"
                        v-model="userForm.email"
                        type="email"
                        placeholder="请输入邮箱"
                        class="form-input"
                      />
                    </div>
                    <div v-if="addErrorMessage" class="error-message">
                      <span class="message-icon">⚠</span>
                      {{ addErrorMessage }}
                    </div>
                    <div v-if="addSuccessMessage" class="success-message">
                      <span class="message-icon">✓</span>
                      {{ addSuccessMessage }}
                    </div>
                    <div class="dialog-actions">
                      <button type="submit" :disabled="loading" class="submit-button">
                        {{ loading ? '添加中...' : '添加用户' }}
                      </button>
                      <button type="button" @click="showAddDialog = false" class="cancel-button">
                        取消
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>

            <!-- 编辑用户对话框 -->
            <div v-if="showEditDialog" class="dialog-overlay" @click="showEditDialog = false">
              <div class="dialog-content" @click.stop>
                <div class="dialog-header">
                  <h3>编辑用户</h3>
                  <button @click="showEditDialog = false" class="dialog-close">×</button>
                </div>
                <div class="dialog-body">
                  <form @submit.prevent="handleUpdateUser" class="edit-user-form">
                    <div class="form-row">
                      <div class="form-group">
                        <label>用户名</label>
                        <input :value="editingUser.username" disabled class="form-input" />
                      </div>
                      <div class="form-group">
                        <label for="editRole">角色 *</label>
                        <select id="editRole" v-model="editForm.role" required class="form-input">
                          <option value="USER">普通用户</option>
                          <option value="INSPECTOR">检查员</option>
                          <option value="OPERATOR">操作员</option>
                          <option value="ADMIN">管理员</option>
                        </select>
                      </div>
                    </div>
                    <div class="form-row">
                      <div class="form-group">
                        <label for="editRealName">真实姓名 *</label>
                        <input
                          id="editRealName"
                          v-model="editForm.realName"
                          type="text"
                          required
                          class="form-input"
                        />
                      </div>
                      <div class="form-group">
                        <label for="editAge">年龄</label>
                        <input
                          id="editAge"
                          v-model.number="editForm.age"
                          type="number"
                          min="0"
                          class="form-input"
                        />
                      </div>
                    </div>
                    <div class="form-row">
                      <div class="form-group">
                        <label for="editPhone">手机号</label>
                        <input
                          id="editPhone"
                          v-model="editForm.phone"
                          type="tel"
                          class="form-input"
                        />
                      </div>
                      <div class="form-group">
                        <label for="editEmail">邮箱</label>
                        <input
                          id="editEmail"
                          v-model="editForm.email"
                          type="email"
                          class="form-input"
                        />
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="editPassword">新密码（留空则不修改）</label>
                      <input
                        id="editPassword"
                        v-model="editForm.password"
                        type="password"
                        placeholder="留空则不修改密码"
                        class="form-input"
                      />
                    </div>
                    <div v-if="editErrorMessage" class="error-message">
                      <span class="message-icon">⚠</span>
                      {{ editErrorMessage }}
                    </div>
                    <div v-if="editSuccessMessage" class="success-message">
                      <span class="message-icon">✓</span>
                      {{ editSuccessMessage }}
                    </div>
                    <div class="dialog-actions">
                      <button type="submit" :disabled="editLoading" class="submit-button">
                        {{ editLoading ? '保存中...' : '保存' }}
                      </button>
                      <button type="button" @click="showEditDialog = false" class="cancel-button">
                        取消
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          </section>

          <!-- 非管理员时，这里预留给其他模块或简单提示，目前先留空 --> 
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 表单数据，对应后端 RegisterDTO
const userForm = ref({
  username: '',
  password: '',
  realName: '',
  age: undefined,
  phone: '',
  email: '',
  role: '',
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const userListLoading = ref(false)

// 查询表单
const queryForm = ref({
  realName: '',
  role: '',
  enabled: '', // 0=禁用，1=启用
  startDate: '',
  endDate: '',
})
const queryLoading = ref(false)
const isQueryMode = ref(false) // 是否处于查询模式

// 添加用户相关
const showAddDialog = ref(false)
const addErrorMessage = ref('')
const addSuccessMessage = ref('')

// 编辑相关
const showEditDialog = ref(false)
const editingUser = ref(null)
const editForm = ref({
  realName: '',
  age: undefined,
  phone: '',
  email: '',
  role: '',
  password: '',
})
const editLoading = ref(false)
const editErrorMessage = ref('')
const editSuccessMessage = ref('')

// 确认对话框
const showConfirmDialog = ref(false)
const confirmConfig = ref({
  title: '',
  message: '',
  type: 'warning', // warning, danger
  confirmText: '确定',
  cancelText: '取消',
  onConfirm: null,
})

// 检查是否已登录；是否管理员只用于决定是否加载用户列表（UI 已经隐藏了模块）
onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  // 普通员工不加载用户列表，直接返回
  if (!authStore.isAdmin) {
    return
  }
  // 加载用户列表
  await handleLoadAllUsers()
})

// 加载所有用户
const handleLoadAllUsers = async () => {
  userListLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  isQueryMode.value = false
  try {
    const res = await authStore.loadUsers()
    if (!res.success) {
      errorMessage.value = res.message || '加载用户列表失败'
    }
  } catch (error) {
    errorMessage.value = error.message || '加载用户列表失败'
    console.error('加载用户列表失败:', error)
  } finally {
    userListLoading.value = false
  }
}

// 条件查询用户
const handleQueryUsers = async () => {
  queryLoading.value = true
  userListLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  
  try {
    const params = {}
    if (queryForm.value.realName) {
      params.realName = queryForm.value.realName
    }
    if (queryForm.value.role) {
      params.role = queryForm.value.role
    }
    if (queryForm.value.enabled !== '') {
      params.enabled = queryForm.value.enabled
    }
    if (queryForm.value.startDate) {
      // 将 datetime-local 格式转换为 yyyy-MM-dd HH:mm:ss
      params.startDate = queryForm.value.startDate.replace('T', ' ') + ':00'
    }
    if (queryForm.value.endDate) {
      params.endDate = queryForm.value.endDate.replace('T', ' ') + ':00'
    }
    
    const res = await authStore.searchUsers(params)
    if (res.success) {
      isQueryMode.value = true
      successMessage.value = `查询成功，找到 ${authStore.users.length} 条记录`
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } else {
      errorMessage.value = res.message || '查询用户失败'
    }
  } catch (error) {
    errorMessage.value = error.message || '查询用户失败'
    console.error('查询用户失败:', error)
  } finally {
    queryLoading.value = false
    userListLoading.value = false
  }
}

// 重置查询表单
const handleResetQuery = () => {
  queryForm.value = {
    realName: '',
    role: '',
    enabled: '',
    startDate: '',
    endDate: '',
  }
  isQueryMode.value = false
  handleLoadAllUsers()
}

// 编辑用户
const handleEditUser = (user) => {
  editingUser.value = user
  editForm.value = {
    realName: user.realName || '',
    age: user.age,
    phone: user.phone || '',
    email: user.email || '',
    role: user.role || '',
    password: '',
  }
  editErrorMessage.value = ''
  editSuccessMessage.value = ''
  showEditDialog.value = true
}

// 更新用户
const handleUpdateUser = async () => {
  if (!editingUser.value || !editingUser.value.id) {
    editErrorMessage.value = '用户信息不完整'
    return
  }
  
  editLoading.value = true
  editErrorMessage.value = ''
  editSuccessMessage.value = ''
  
  try {
    const updateData = {
      realName: editForm.value.realName,
      age: editForm.value.age,
      phone: editForm.value.phone,
      email: editForm.value.email,
      role: editForm.value.role,
    }
    // 只有提供了新密码才更新
    if (editForm.value.password) {
      updateData.password = editForm.value.password
    }
    
    const result = await authStore.updateUser(editingUser.value.id, updateData)
    if (result.success) {
      editSuccessMessage.value = result.message || '更新用户成功'
      // 重新加载用户列表
      await handleLoadAllUsers()
      // 3秒后关闭对话框
      setTimeout(() => {
        showEditDialog.value = false
        editSuccessMessage.value = ''
      }, 2000)
    } else {
      editErrorMessage.value = result.message || '更新用户失败'
    }
  } catch (error) {
    editErrorMessage.value = error.message || '更新用户失败'
  } finally {
    editLoading.value = false
  }
}

// 切换用户状态（启用/禁用）
const handleToggleStatus = async (user) => {
  if (!user.id) {
    errorMessage.value = '用户ID不存在'
    return
  }
  
  const currentStatus = typeof user.status === 'string' 
    ? user.status 
    : (user.status && user.status.code === 1 ? 'ENABLED' : 'DISABLED')
  const newStatus = currentStatus === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const actionText = newStatus === 'ENABLED' ? '启用' : '禁用'
  
  // 显示确认对话框
  confirmConfig.value = {
    title: `${actionText}用户`,
    message: `确定要${actionText}用户 "${user.username}" 吗？`,
    type: 'warning',
    confirmText: actionText,
    cancelText: '取消',
    onConfirm: async () => {
      errorMessage.value = ''
      successMessage.value = ''
      
      try {
        const result = await authStore.changeUserStatus(user.id, newStatus)
        if (result.success) {
          successMessage.value = result.message || '更新用户状态成功'
          // 如果处于查询模式，重新执行查询；否则加载全部用户
          if (isQueryMode.value) {
            await handleQueryUsers()
          } else {
            await handleLoadAllUsers()
          }
          setTimeout(() => {
            successMessage.value = ''
          }, 3000)
        } else {
          errorMessage.value = result.message || '更新用户状态失败'
        }
      } catch (error) {
        errorMessage.value = error.message || '更新用户状态失败'
      }
    }
  }
  showConfirmDialog.value = true
}

// 删除用户
const handleDeleteUser = async (user) => {
  if (!user.id) {
    errorMessage.value = '用户ID不存在'
    return
  }
  
  // 显示确认对话框
  confirmConfig.value = {
    title: '删除用户',
    message: `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
    type: 'danger',
    confirmText: '删除',
    cancelText: '取消',
    onConfirm: async () => {
      errorMessage.value = ''
      successMessage.value = ''
      
      try {
        const result = await authStore.removeUser(user.id)
        if (result.success) {
          successMessage.value = result.message || '删除用户成功'
          // 如果处于查询模式，重新执行查询；否则加载全部用户
          if (isQueryMode.value) {
            await handleQueryUsers()
          } else {
            await handleLoadAllUsers()
          }
          setTimeout(() => {
            successMessage.value = ''
          }, 3000)
        } else {
          errorMessage.value = result.message || '删除用户失败'
        }
      } catch (error) {
        errorMessage.value = error.message || '删除用户失败'
      }
    }
  }
  showConfirmDialog.value = true
}

// 添加用户，对应后端 POST /api/auth/register
const handleAddUser = async () => {
  addErrorMessage.value = ''
  addSuccessMessage.value = ''
  loading.value = true

  try {
    const result = await authStore.addUser(userForm.value)
    if (result.success) {
      addSuccessMessage.value = result.message || '添加用户成功'
      // 清空表单
      userForm.value = {
        username: '',
        password: '',
        realName: '',
        age: undefined,
        phone: '',
        email: '',
        role: '',
      }
      // 重新加载用户列表
      if (isQueryMode.value) {
        await handleQueryUsers()
      } else {
        await handleLoadAllUsers()
      }
      // 2秒后关闭对话框
      setTimeout(() => {
        showAddDialog.value = false
        addSuccessMessage.value = ''
      }, 2000)
    } else {
      // 显示后端返回的错误信息（如：用户名不能为空、用户名已存在等）
      addErrorMessage.value = result.message || '添加用户失败'
    }
  } catch (error) {
    addErrorMessage.value = error.message || '添加用户失败'
  } finally {
    loading.value = false
  }
}

// 显示添加用户对话框
const handleShowAddDialog = () => {
  // 重置表单
  userForm.value = {
    username: '',
    password: '',
    realName: '',
    age: undefined,
    phone: '',
    email: '',
    role: '',
  }
  // 清空消息
  addErrorMessage.value = ''
  addSuccessMessage.value = ''
  // 显示对话框
  showAddDialog.value = true
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

// 格式化用户角色
const formatRole = (role) => {
  if (!role) return '-'
  const roleMap = {
    USER: '普通用户',
    INSPECTOR: '检查员',
    OPERATOR: '操作员',
    ADMIN: '管理员',
  }
  return roleMap[role] || role
}

// 格式化用户状态（后端是枚举 UserStatusEnum）
const formatStatus = (status) => {
  if (!status) return '-'
  // 如果后端返回的是枚举名
  if (typeof status === 'string') {
    return status === 'ENABLED' ? '启用' : '禁用'
  }
  // 如果后端返回的是对象 {code, description}
  if (typeof status === 'object' && status.description) {
    return status.description
  }
  return String(status)
}

// 获取状态样式类
const getStatusClass = (status) => {
  const isEnabled = typeof status === 'string' 
    ? status === 'ENABLED' 
    : (status && status.code === 1)
  return isEnabled ? 'status-enabled' : 'status-disabled'
}

// 格式化日期时间
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  // 如果已经是格式化好的字符串，直接返回
  if (typeof dateStr === 'string' && dateStr.includes(' ')) {
    return dateStr
  }
  // 如果是日期对象，格式化
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>

<style scoped>
.user-management-container {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  flex-direction: row;
}

/* 左侧边栏 */
.sidebar {
  width: 220px;
  background: #1e3c72;
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
}

.sidebar-header {
  padding: 12px 8px 20px;
}

.sidebar-logo {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.sidebar-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: #e5e7eb;
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.menu-item .menu-icon {
  width: 18px;
  text-align: center;
}

.menu-item.active {
  background: #2563eb;
}

.menu-item:not(.active):hover {
  background: rgba(148, 163, 184, 0.25);
}

.menu-item.disabled {
  opacity: 0.6;
  cursor: default;
}

.sidebar-footer {
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.4);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-username {
  font-size: 13px;
}

.sidebar-logout {
  padding: 6px 10px;
  background: transparent;
  color: #e5e7eb;
  border-radius: 4px;
  border: 1px solid rgba(148, 163, 184, 0.8);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.sidebar-logout:hover {
  background: rgba(148, 163, 184, 0.3);
}

/* 右侧主区域 */
.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 顶部条 */
.top-header {
  padding: 16px 24px 8px;
}

.top-title {
  font-size: 22px;
  font-weight: 600;
  color: #111827;
}

.top-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 24px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.content-wrapper {
  width: 100%;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 卡片样式 */
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.add-user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(30, 60, 114, 0.2);
}

.add-user-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.3);
}

.add-user-btn:active {
  transform: translateY(0);
}

.add-user-btn .btn-icon {
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
}

.card-body {
  padding: 24px;
}

.add-user-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.form-input {
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 15px;
  transition: all 0.3s;
  background: white;
}

.form-input:hover {
  border-color: #1e3c72;
}

.form-input:focus {
  outline: none;
  border-color: #1e3c72;
  box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.1);
}

.form-input::placeholder {
  color: #9ca3af;
}

/* 消息提示 */
.success-message,
.error-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  padding: 12px 16px;
  border-radius: 6px;
}

.success-message {
  color: #065f46;
  background: #d1fae5;
  border: 1px solid #a7f3d0;
}

.error-message {
  color: #991b1b;
  background: #fee2e2;
  border: 1px solid #fecaca;
}

.message-icon {
  font-size: 16px;
  font-weight: bold;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

  .submit-button,
  .reset-button,
  .query-button,
  .reset-query-button,
  .load-all-button {
    padding: 12px 24px;
    border: none;
    border-radius: 6px;
    font-size: 15px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
  }

.submit-button {
  background: #1e3c72;
  color: white;
  flex: 1;
}

.submit-button:hover:not(:disabled) {
  background: #2a5298;
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.3);
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #9ca3af;
}

.reset-button {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 12px 20px;
}

  .reset-button:hover {
    background: #f9fafb;
    border-color: #9ca3af;
  }

  /* 过滤器样式 */
  .filter-form {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e5e7eb;
  }

  .filter-row {
    display: flex;
    gap: 10px;
    align-items: center;
    flex-wrap: wrap;
  }

  .filter-input {
    padding: 8px 12px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 14px;
    background: white;
    transition: all 0.2s;
    flex: 1;
    min-width: 120px;
    max-width: 180px;
  }

  .filter-input:hover {
    border-color: #1e3c72;
  }

  .filter-input:focus {
    outline: none;
    border-color: #1e3c72;
    box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.1);
  }

  .filter-input::placeholder {
    color: #9ca3af;
  }

  .filter-btn {
    padding: 8px 16px;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
  }

  .filter-btn-primary {
    background: #1e3c72;
    color: white;
  }

  .filter-btn-primary:hover:not(:disabled) {
    background: #2a5298;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(30, 60, 114, 0.3);
  }

  .filter-btn-primary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .filter-btn-secondary {
    background: white;
    color: #374151;
    border: 1px solid #d1d5db;
  }

  .filter-btn-secondary:hover {
    background: #f9fafb;
    border-color: #1e3c72;
    color: #1e3c72;
  }

  .filter-btn:active {
    transform: translateY(0);
  }

  /* 操作按钮样式 */
  .action-buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .action-btn {
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .edit-btn {
    background: #3b82f6;
    color: white;
  }

  .edit-btn:hover {
    background: #2563eb;
  }

  .enable-btn {
    background: #10b981;
    color: white;
  }

  .enable-btn:hover {
    background: #059669;
  }

  .disable-btn {
    background: #f59e0b;
    color: white;
  }

  .disable-btn:hover {
    background: #d97706;
  }

  .delete-btn {
    background: #ef4444;
    color: white;
  }

  .delete-btn:hover {
    background: #dc2626;
  }

  /* 状态样式 */
  .status-enabled {
    color: #10b981;
    font-weight: 500;
  }

  .status-disabled {
    color: #ef4444;
    font-weight: 500;
  }

  /* 对话框样式 */
  .dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    animation: fadeIn 0.25s ease-out;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }

  @keyframes slideUp {
    from {
      opacity: 0;
      transform: translateY(20px) scale(0.95);
    }
    to {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }

  .dialog-content {
    background: white;
    border-radius: 12px;
    width: 90%;
    max-width: 650px;
    max-height: 90vh;
    overflow: hidden;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    animation: slideUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    display: flex;
    flex-direction: column;
  }

  .dialog-header {
    padding: 24px 28px;
    border-bottom: 1px solid #e5e7eb;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
    flex-shrink: 0;
  }

  .dialog-header h3 {
    margin: 0;
    font-size: 19px;
    font-weight: 600;
    color: white;
    letter-spacing: 0.3px;
  }

  .dialog-close {
    background: rgba(255, 255, 255, 0.15);
    border: none;
    font-size: 22px;
    color: white;
    cursor: pointer;
    padding: 0;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    transition: all 0.2s;
    line-height: 1;
  }

  .dialog-close:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: rotate(90deg);
  }

  .dialog-close:active {
    transform: rotate(90deg) scale(0.95);
  }

  .dialog-body {
    padding: 28px;
    overflow-y: auto;
    flex: 1;
  }

  /* 自定义滚动条样式 */
  .dialog-body::-webkit-scrollbar {
    width: 8px;
  }

  .dialog-body::-webkit-scrollbar-track {
    background: #f3f4f6;
    border-radius: 4px;
  }

  .dialog-body::-webkit-scrollbar-thumb {
    background: #d1d5db;
    border-radius: 4px;
    transition: background 0.2s;
  }

  .dialog-body::-webkit-scrollbar-thumb:hover {
    background: #9ca3af;
  }

  .edit-user-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .dialog-actions {
    display: flex;
    gap: 12px;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #e5e7eb;
  }

  .cancel-button {
    flex: 1;
    padding: 12px 24px;
    background: white;
    color: #374151;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 15px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
  }

  .cancel-button:hover {
    background: #f9fafb;
    border-color: #1e3c72;
    color: #1e3c72;
    transform: translateY(-1px);
  }

  .cancel-button:active {
    transform: translateY(0);
  }

  .dialog-actions .submit-button {
    flex: 1;
  }

  /* 确认对话框样式 */
  .confirm-dialog {
    background: white;
    border-radius: 16px;
    width: 90%;
    max-width: 420px;
    padding: 32px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    animation: slideUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    text-align: center;
  }

  .confirm-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    animation: scaleIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  }

  @keyframes scaleIn {
    from {
      transform: scale(0);
      opacity: 0;
    }
    to {
      transform: scale(1);
      opacity: 1;
    }
  }

  .confirm-icon.warning {
    background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  }

  .confirm-icon.danger {
    background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  }

  .confirm-title {
    margin: 0 0 12px;
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
  }

  .confirm-message {
    margin: 0 0 28px;
    font-size: 15px;
    color: #6b7280;
    line-height: 1.6;
  }

  .confirm-actions {
    display: flex;
    gap: 12px;
  }

  .confirm-cancel-btn,
  .confirm-ok-btn {
    flex: 1;
    padding: 12px 24px;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
  }

  .confirm-cancel-btn {
    background: #f3f4f6;
    color: #374151;
  }

  .confirm-cancel-btn:hover {
    background: #e5e7eb;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .confirm-ok-btn {
    color: white;
  }

  .confirm-ok-btn.warning {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  }

  .confirm-ok-btn.warning:hover {
    background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
  }

  .confirm-ok-btn.danger {
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  }

  .confirm-ok-btn.danger:hover {
    background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
  }

  .confirm-cancel-btn:active,
  .confirm-ok-btn:active {
    transform: translateY(0);
  }

/* 用户列表表格 */
.user-list-card {
  margin-top: 20px;
}

.user-list-card .card-body {
  padding-top: 20px;
}

.table-loading {
  text-align: center;
  padding: 20px 0;
  color: #6b7280;
  font-size: 14px;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.user-table th,
.user-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  white-space: nowrap;
}

.user-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.user-table tbody tr:hover {
  background: #f3f4f6;
}

.empty-cell {
  text-align: center;
  color: #9ca3af;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .sidebar-menu {
    flex-direction: row;
  }

  .layout-main {
    width: 100%;
  }

  .main-content {
    padding: 16px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .card-body {
    padding: 20px 16px;
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-button,
  .reset-button {
    width: 100%;
  }

  .filter-row {
    flex-direction: column;
    gap: 8px;
  }

  .filter-input {
    max-width: none;
    width: 100%;
  }

  .filter-btn {
    width: 100%;
  }

  .dialog-content {
    width: 95%;
    max-width: none;
    margin: 0 10px;
  }

  .dialog-header {
    padding: 20px 20px;
  }

  .dialog-header h3 {
    font-size: 17px;
  }

  .dialog-body {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }

  .card-header {
    padding: 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .add-user-btn {
    width: 100%;
    justify-content: center;
  }

  .card-body {
    padding: 16px;
  }

  .dialog-header {
    padding: 16px;
  }

  .dialog-header h3 {
    font-size: 16px;
  }

  .dialog-body {
    padding: 16px;
  }

  .dialog-actions {
    flex-direction: column;
  }

  .dialog-actions .submit-button,
  .dialog-actions .cancel-button {
    width: 100%;
  }

  .confirm-dialog {
    max-width: 95%;
    padding: 24px;
  }

  .confirm-icon {
    width: 56px;
    height: 56px;
    font-size: 28px;
  }

  .confirm-title {
    font-size: 18px;
  }

  .confirm-message {
    font-size: 14px;
  }

  .confirm-actions {
    flex-direction: column;
  }

  .confirm-cancel-btn,
  .confirm-ok-btn {
    width: 100%;
  }
}
</style>
