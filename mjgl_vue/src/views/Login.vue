<template>
  <div class="login-container">
    <div class="login-wrapper">
      <section class="brand-panel">
        <div class="brand-badge">MJGL</div>
        <h2 class="brand-title">模具全生命周期管理平台</h2>
        <p class="brand-desc">
          覆盖模具台账、使用、维修、保养、报警与审批流程，统一入口，统一协同。
        </p>
        <div class="brand-highlights">
          <div class="highlight-item">
            <img src="/项目总览.png" alt="概览" class="highlight-icon" />
            <span>多维度数据总览</span>
          </div>
          <div class="highlight-item">
            <img src="/消息通知.png" alt="通知" class="highlight-icon" />
            <span>实时告警通知</span>
          </div>
          <div class="highlight-item">
            <img src="/提交.png" alt="审批" class="highlight-icon" />
            <span>审批流程联动</span>
          </div>
        </div>
        <div class="brand-stats">
          <div class="stat-card">
            <div class="stat-value">10+</div>
            <div class="stat-label">核心业务模块</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">24h</div>
            <div class="stat-label">告警实时可见</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">100%</div>
            <div class="stat-label">流程线上留痕</div>
          </div>
        </div>
        <div class="brand-tags">
          <span class="brand-tag">模具台账</span>
          <span class="brand-tag">使用记录</span>
          <span class="brand-tag">维修保养</span>
          <span class="brand-tag">报警中心</span>
          <span class="brand-tag">审批中心</span>
        </div>
      </section>

      <div class="login-box">
        <div class="login-header">
          <h1 class="system-title">管理系统</h1>
          <p class="system-subtitle">Mold Lifecycle Management</p>
          <div class="env-row">
            <span class="env-chip">生产管理场景</span>
            <span class="env-chip">统一认证入口</span>
          </div>
          <div class="auth-switch">
            <button
              type="button"
              class="switch-btn"
              :class="{ active: !isRegisterMode }"
              @click="switchMode(false)"
            >
              登录
            </button>
            <button
              type="button"
              class="switch-btn"
              :class="{ active: isRegisterMode }"
              @click="switchMode(true)"
            >
              注册
            </button>
          </div>
        </div>
        <form v-if="!isRegisterMode" @submit.prevent="handleLogin" class="login-form">
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
            <img class="error-icon" src="/消息通知.png" alt="错误" />
            {{ errorMessage }}
          </div>
          <button type="submit" :disabled="loading" class="login-button">
            {{ loading ? '登录中...' : '登录' }}
          </button>
          <div v-if="registerMessage" class="success-message">
            {{ registerMessage }}
          </div>
          <div class="login-tips">
            <div class="tip-item">建议使用企业账号登录以获取完整权限</div>
            <div class="tip-item">如密码遗忘，请联系系统管理员重置</div>
          </div>
          <div class="login-footer">
            <span>推荐浏览器：Chrome / Edge 最新版</span>
            <span>Version 1.0</span>
          </div>
        </form>

        <form v-else @submit.prevent="handleSignup" class="login-form">
          <div class="form-group">
            <label for="reg-username">用户名</label>
            <input
              id="reg-username"
              v-model="registerForm.username"
              type="text"
              placeholder="请输入用户名"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label for="reg-realname">真实姓名</label>
            <input
              id="reg-realname"
              v-model="registerForm.realName"
              type="text"
              placeholder="请输入真实姓名"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label for="reg-password">密码</label>
            <input
              id="reg-password"
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label for="reg-confirm-password">确认密码</label>
            <input
              id="reg-confirm-password"
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label for="reg-phone">手机号（可选）</label>
            <input
              id="reg-phone"
              v-model="registerForm.phone"
              type="text"
              placeholder="请输入手机号"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label for="reg-email">邮箱（可选）</label>
            <input
              id="reg-email"
              v-model="registerForm.email"
              type="email"
              placeholder="请输入邮箱"
              class="form-input"
            />
          </div>
          <div v-if="errorMessage" class="error-message">
            <img class="error-icon" src="/消息通知.png" alt="错误" />
            {{ errorMessage }}
          </div>
          <button type="submit" :disabled="loading" class="login-button">
            {{ loading ? '注册中...' : '注册并创建普通用户' }}
          </button>
          <div class="login-tips">
            <div class="tip-item">注册后默认角色为普通用户（USER）</div>
            <div class="tip-item">如需其他角色权限，请联系管理员分配</div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { signup } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = ref({
  username: '',
  password: '',
})

const loading = ref(false)
const errorMessage = ref('')
const registerMessage = ref('')
const isRegisterMode = ref(false)
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
})

const handleLogin = async () => {
  errorMessage.value = ''
  registerMessage.value = ''
  loading.value = true

  try {
    const result = await authStore.login(loginForm.value)
    if (result.success) {
      // 登录成功后进入首页门户
      router.push('/home')
    } else {
      errorMessage.value = result.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}

const resetRegisterForm = () => {
  registerForm.value = {
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    phone: '',
    email: '',
  }
}

const switchMode = (registerMode) => {
  isRegisterMode.value = registerMode
  errorMessage.value = ''
  registerMessage.value = ''
}

const handleSignup = async () => {
  errorMessage.value = ''
  registerMessage.value = ''
  if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.realName) {
    errorMessage.value = '请填写用户名、密码和真实姓名'
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  try {
    const res = await signup({
      username: registerForm.value.username,
      password: registerForm.value.password,
      realName: registerForm.value.realName,
      phone: registerForm.value.phone || undefined,
      email: registerForm.value.email || undefined,
    })
    if (res.code === 200) {
      registerMessage.value = '注册成功，请返回登录'
      resetRegisterForm()
      isRegisterMode.value = false
      loginForm.value.username = ''
      loginForm.value.password = ''
    } else {
      errorMessage.value = res.message || '注册失败'
    }
  } catch (error) {
    errorMessage.value = error.message || '注册失败'
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
  max-width: 980px;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 18px;
  align-items: stretch;
}

.brand-panel {
  color: #e2e8f0;
  border: 1px solid rgba(191, 219, 254, 0.28);
  border-radius: 12px;
  padding: 24px;
  background: linear-gradient(145deg, rgba(30, 64, 175, 0.42), rgba(30, 41, 59, 0.2));
  backdrop-filter: blur(2px);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-badge {
  display: inline-flex;
  width: fit-content;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(148, 197, 255, 0.2);
  border: 1px solid rgba(191, 219, 254, 0.45);
  font-size: 12px;
  letter-spacing: 1px;
  margin-bottom: 14px;
}

.brand-title {
  font-size: 28px;
  line-height: 1.3;
  margin: 0 0 10px;
}

.brand-desc {
  margin: 0;
  color: #cbd5e1;
  line-height: 1.7;
  font-size: 14px;
}

.brand-highlights {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.highlight-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #f8fafc;
  font-size: 14px;
}

.highlight-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
  filter: brightness(0) invert(1);
}

.brand-stats {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.stat-card {
  background: rgba(15, 23, 42, 0.25);
  border: 1px solid rgba(191, 219, 254, 0.28);
  border-radius: 10px;
  padding: 10px 8px;
}

.stat-value {
  font-size: 18px;
  color: #eff6ff;
  font-weight: 700;
}

.stat-label {
  margin-top: 4px;
  font-size: 12px;
  color: #cbd5e1;
}

.brand-tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.brand-tag {
  font-size: 12px;
  color: #e2e8f0;
  border: 1px solid rgba(191, 219, 254, 0.4);
  border-radius: 999px;
  padding: 3px 10px;
  background: rgba(30, 64, 175, 0.25);
}

.login-box {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 34px 32px;
  width: 100%;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 18px;
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

.env-row {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.env-chip {
  font-size: 12px;
  color: #1d4ed8;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  padding: 3px 10px;
}

.auth-switch {
  margin-top: 12px;
  display: inline-flex;
  background: #f1f5f9;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  padding: 3px;
}

.switch-btn {
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 12px;
  padding: 5px 14px;
  border-radius: 999px;
  cursor: pointer;
}

.switch-btn.active {
  background: #1d4ed8;
  color: #ffffff;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.success-message {
  color: #065f46;
  font-size: 13px;
  text-align: center;
  padding: 10px;
  background: #d1fae5;
  border: 1px solid #a7f3d0;
  border-radius: 6px;
}

.error-icon {
  width: 16px;
  height: 16px;
  object-fit: contain;
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

.login-tips {
  margin-top: 12px;
  border-top: 1px dashed #dbeafe;
  padding-top: 12px;
  display: grid;
  gap: 6px;
}

.login-footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-top: 8px;
  padding-top: 10px;
  border-top: 1px dashed #e5e7eb;
  font-size: 12px;
  color: #94a3b8;
}

.tip-item {
  font-size: 12px;
  color: #64748b;
}

@media (max-width: 480px) {
  .login-wrapper {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .brand-panel {
    padding: 18px;
  }

  .brand-stats {
    grid-template-columns: 1fr;
  }

  .brand-title {
    font-size: 22px;
  }

  .login-box {
    padding: 26px 20px;
  }

  .system-title {
    font-size: 28px;
  }

  .login-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
