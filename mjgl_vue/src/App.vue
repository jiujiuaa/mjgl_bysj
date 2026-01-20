<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 登录页不显示右上角用户信息，其它页面且已登录时显示
const showGlobalHeader = computed(() => authStore.isAuthenticated && route.path !== '/login')

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-root">
    <header v-if="showGlobalHeader" class="global-header">
      <div class="global-header-right">
        <span class="global-header-username">{{ authStore.username }}</span>
        <button class="global-header-logout" @click="handleLogout">退出</button>
      </div>
    </header>
    <RouterView />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Microsoft YaHei', '微软雅黑', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: #f3f4f6;
}

#app {
  min-height: 100vh;
}

.app-root {
  min-height: 100vh;
}

.global-header {
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 200;
}

.global-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.global-header-username {
  font-size: 14px;
  color: #374151;
}

.global-header-logout {
  padding: 6px 14px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  font-size: 13px;
  cursor: pointer;
  color: #374151;
  transition: all 0.2s;
}

.global-header-logout:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}
</style>
