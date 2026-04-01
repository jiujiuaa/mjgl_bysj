<template>
  <div class="user-profile-container">
    <AppSidebar />
    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">个人资料</div>
        <div class="top-subtitle">查看并更新当前登录账号的基本信息</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section class="card">
            <div class="card-header">
              <h3 class="card-title">账号信息</h3>
            </div>
            <div class="card-body">
              <form class="profile-form" @submit.prevent="handleSubmit">
                <div class="form-row">
                  <div class="form-group">
                    <label>用户名</label>
                    <input class="form-input" type="text" :value="profile.username" disabled />
                  </div>
                  <div class="form-group">
                    <label>角色</label>
                    <input class="form-input" type="text" :value="formatRole(profile.role)" disabled />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label for="realName">真实姓名</label>
                    <input
                      id="realName"
                      v-model="profileForm.realName"
                      class="form-input"
                      type="text"
                      placeholder="请输入真实姓名"
                    />
                  </div>
                  <div class="form-group">
                    <label for="age">年龄</label>
                    <input
                      id="age"
                      v-model.number="profileForm.age"
                      class="form-input"
                      type="number"
                      min="0"
                      placeholder="请输入年龄"
                    />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label for="phone">手机号</label>
                    <input
                      id="phone"
                      v-model="profileForm.phone"
                      class="form-input"
                      type="tel"
                      placeholder="请输入手机号"
                    />
                  </div>
                  <div class="form-group">
                    <label for="email">邮箱</label>
                    <input
                      id="email"
                      v-model="profileForm.email"
                      class="form-input"
                      type="email"
                      placeholder="请输入邮箱"
                    />
                  </div>
                </div>

                <div class="divider" />

                <div class="form-row">
                  <div class="form-group">
                    <label for="newPassword">新密码（可选）</label>
                    <input
                      id="newPassword"
                      v-model="profileForm.newPassword"
                      class="form-input"
                      type="password"
                      placeholder="不修改请留空"
                    />
                  </div>
                  <div class="form-group">
                    <label for="confirmPassword">确认新密码</label>
                    <input
                      id="confirmPassword"
                      v-model="confirmPassword"
                      class="form-input"
                      type="password"
                      placeholder="再次输入新密码"
                    />
                  </div>
                </div>

                <div v-if="successMessage" class="success-message">
                  <span class="message-icon">✓</span>
                  {{ successMessage }}
                </div>
                <div v-if="errorMessage" class="error-message">
                  <span class="message-icon">⚠</span>
                  {{ errorMessage }}
                </div>

                <div class="form-actions">
                  <button class="submit-button" type="submit" :disabled="saving">
                    {{ saving ? '保存中...' : '保存修改' }}
                  </button>
                  <button class="reset-button" type="button" @click="resetForm" :disabled="saving">
                    还原
                  </button>
                </div>
              </form>
            </div>
          </section>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/AppSidebar.vue'
import { fetchCurrentUserProfile, updateCurrentUserProfile } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const profile = reactive({
  username: '',
  role: '',
  realName: '',
  age: undefined,
  phone: '',
  email: '',
})

const profileForm = reactive({
  realName: '',
  age: undefined,
  phone: '',
  email: '',
  newPassword: '',
})

const confirmPassword = ref('')
const loading = ref(false)
const saving = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const formatRole = (role) => {
  if (!role) return '-'
  const map = {
    USER: '普通用户',
    INSPECTOR: '检查员',
    OPERATOR: '操作员',
    ADMIN: '管理员',
  }
  return map[role] || role
}

const applyProfileToForm = (data) => {
  profile.username = data.username || ''
  profile.role = data.role || ''
  profile.realName = data.realName || ''
  profile.age = data.age
  profile.phone = data.phone || ''
  profile.email = data.email || ''

  profileForm.realName = profile.realName
  profileForm.age = profile.age
  profileForm.phone = profile.phone
  profileForm.email = profile.email
  profileForm.newPassword = ''
  confirmPassword.value = ''
}

const loadProfile = async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const res = await fetchCurrentUserProfile()
    if (res.code === 200 && res.data) {
      applyProfileToForm(res.data)
    } else {
      errorMessage.value = res.message || '加载个人资料失败'
    }
  } catch (e) {
    errorMessage.value = e.message || '加载个人资料失败'
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (profileForm.newPassword && profileForm.newPassword !== confirmPassword.value) {
    errorMessage.value = '两次输入的新密码不一致'
    return
  }

  const payload = {
    realName: profileForm.realName,
    age: profileForm.age,
    phone: profileForm.phone,
    email: profileForm.email,
    newPassword: profileForm.newPassword || undefined,
  }

  saving.value = true
  try {
    const res = await updateCurrentUserProfile(payload)
    if (res.code === 200) {
      successMessage.value = res.message || '更新成功'
      await loadProfile()
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } else {
      errorMessage.value = res.message || '更新失败'
    }
  } catch (e) {
    errorMessage.value = e.message || '更新失败'
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  applyProfileToForm(profile)
  successMessage.value = ''
  errorMessage.value = ''
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.user-profile-container {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  flex-direction: row;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

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

.main-content {
  flex: 1;
  padding: 24px;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
}

.content-wrapper {
  width: 100%;
}

.card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.card-body {
  padding: 24px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.form-input {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
  background: #ffffff;
}

.form-input:hover {
  border-color: #1e3c72;
}

.form-input:focus {
  outline: none;
  border-color: #1e3c72;
  box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.12);
}

.divider {
  height: 1px;
  background: #e5e7eb;
  margin: 4px 0 8px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.submit-button,
.reset-button {
  padding: 10px 22px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-button {
  background: #1e3c72;
  color: #ffffff;
  flex: 1;
}

.submit-button:hover:not(:disabled) {
  background: #2a5298;
  box-shadow: 0 4px 10px rgba(30, 64, 175, 0.35);
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.reset-button {
  background: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
  padding-left: 18px;
  padding-right: 18px;
}

.reset-button:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.success-message,
.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 14px;
}

.success-message {
  background: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.error-message {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.message-icon {
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .user-profile-container {
    flex-direction: column;
  }

  .main-content {
    padding: 16px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-button,
  .reset-button {
    width: 100%;
  }
}
</style>

