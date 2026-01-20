<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-box">
        <div class="login-header">
          <h1 class="system-title">管理系统</h1>
          <p class="system-subtitle">Management System</p>
        </div>
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              id="username"
              v-model="loginForm.username"
              type="text"
              placeholder="请输入用户名"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label for="password">密码</label>
            <input
              id="password"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              required
              class="form-input"
            />
          </div>
          <div v-if="errorMessage" class="error-message">
            <span class="error-icon">⚠</span>
            {{ errorMessage }}
          </div>
          <button type="submit" :disabled="loading" class="login-button">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = ref({
  username: '',
  password: '',
})

const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  errorMessage.value = ''
  loading.value = true

  try {
    const result = await authStore.login(loginForm.value)
    if (result.success) {
      // 登录成功，统一跳转到用户管理页（是否有权限由后端和页面自身控制）
      router.push('/user-management')
    } else {
      errorMessage.value = result.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #1e3c72 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-wrapper {
  width: 100%;
  max-width: 420px;
}

.login-box {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 48px 40px;
  width: 100%;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 2px solid #e8f0fe;
}

.system-title {
  font-size: 32px;
  font-weight: 600;
  color: #1e3c72;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.system-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  font-weight: 400;
  letter-spacing: 2px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
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
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 15px;
  transition: all 0.3s;
  background: #ffffff;
}

.form-input:hover {
  border-color: #2a5298;
}

.form-input:focus {
  outline: none;
  border-color: #1e3c72;
  box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.1);
}

.error-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #dc2626;
  font-size: 14px;
  text-align: center;
  padding: 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
}

.error-icon {
  font-size: 16px;
}

.login-button {
  padding: 14px;
  background: #1e3c72;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 8px;
}

.login-button:hover:not(:disabled) {
  background: #2a5298;
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.3);
}

.login-button:active:not(:disabled) {
  transform: translateY(1px);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #9ca3af;
}

@media (max-width: 480px) {
  .login-box {
    padding: 36px 28px;
  }

  .system-title {
    font-size: 28px;
  }
}
</style>
