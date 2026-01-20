<template>
  <div class="welcome-container">
    <div class="welcome-content">
      <div class="welcome-icon">👋</div>
      <h1 class="welcome-title">欢迎使用管理系统</h1>
      <p class="welcome-message">您好，{{ authStore.username }}！</p>
      <p class="welcome-description">您当前角色没有用户管理权限，如有需要请联系管理员。</p>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>

<style scoped>
.welcome-container {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.welcome-content {
  text-align: center;
  background: white;
  padding: 60px 40px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.welcome-message {
  font-size: 18px;
  color: #1e3c72;
  font-weight: 500;
  margin: 0 0 12px 0;
}

.welcome-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
}

@media (max-width: 480px) {
  .welcome-content {
    padding: 40px 24px;
  }

  .welcome-icon {
    font-size: 48px;
  }

  .welcome-title {
    font-size: 24px;
  }
}
</style>
