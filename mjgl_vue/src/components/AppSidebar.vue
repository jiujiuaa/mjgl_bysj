<template>
  <div class="sidebar-wrapper">
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">管理系统</div>
    </div>
    <nav class="sidebar-menu">
      <!-- 顶级：用户管理 -->
      <div
        class="menu-item"
        :class="{ active: route.path === '/user-management' }"
        @click="go('/user-management')"
      >
        <span class="menu-icon">👤</span>
        <span>用户管理</span>
      </div>

      <!-- 顶级：模具管理 -->
      <div
        class="menu-item"
        :class="{ active: route.path === '/mold-management' }"
        @click="go('/mold-management')"
      >
        <span class="menu-icon">🧱</span>
        <span>模具管理</span>
      </div>

      <!-- 运维管理分组 -->
      <div
        class="menu-item parent-item"
        @click="sidebarStore.toggleOps()"
      >
        <span class="menu-icon">🛠</span>
        <span>运维管理</span>
        <span class="submenu-arrow">{{ sidebarStore.showOpsChildren ? '▾' : '▸' }}</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/mold-use-records' }"
        @click="go('/mold-use-records')"
      >
        <span class="menu-icon">📒</span>
        <span>使用记录</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/repair-records' }"
        @click="go('/repair-records')"
      >
        <span class="menu-icon">🩺</span>
        <span>维修记录</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/maintenance-plans' }"
        @click="go('/maintenance-plans')"
      >
        <span class="menu-icon">🧽</span>
        <span>保养计划</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/maintenance-logs' }"
        @click="go('/maintenance-logs')"
      >
        <span class="menu-icon">🧴</span>
        <span>保养记录</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/maintenance-reminders' }"
        @click="go('/maintenance-reminders')"
      >
        <span class="menu-icon">⏰</span>
        <span>保养提醒</span>
      </div>

      <!-- 监测与异常分组 -->
      <div
        class="menu-item parent-item"
        @click="sidebarStore.toggleMonitoring()"
      >
        <span class="menu-icon">📈</span>
        <span>监测与异常</span>
        <span class="submenu-arrow">{{ sidebarStore.showMonitoringChildren ? '▾' : '▸' }}</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/monitoring-temperature' }"
        @click="go('/monitoring-temperature')"
      >
        <span class="menu-icon">🌡</span>
        <span>温度巡检录入</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/monitoring-lubrication' }"
        @click="go('/monitoring-lubrication')"
      >
        <span class="menu-icon">🛢</span>
        <span>润滑巡检录入</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/monitoring-abnormal' }"
        @click="go('/monitoring-abnormal')"
      >
        <span class="menu-icon">⚠</span>
        <span>异常记录</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/alert-records' }"
        @click="go('/alert-records')"
      >
        <span class="menu-icon">🔔</span>
        <span>报警管理</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/alert-rules' }"
        @click="go('/alert-rules')"
      >
        <span class="menu-icon">📋</span>
        <span>预警规则</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren"
        class="menu-item child-item"
        :class="{ active: route.path === '/dashboard-charts' }"
        @click="go('/dashboard-charts')"
      >
        <span class="menu-icon">📊</span>
        <span>监测数据展示</span>
      </div>

      <!-- 健康评估 -->
      <div
        class="menu-item"
        :class="{ active: route.path === '/health-reports' }"
        @click="go('/health-reports')"
      >
        <span class="menu-icon">❤️</span>
        <span>健康评估</span>
      </div>
    </nav>

    <div class="sidebar-footer">
      <span class="sidebar-username">{{ authStore.username }}</span>
      <button class="sidebar-logout" @click="handleLogout">
        退出登录
      </button>
    </div>
  </aside>
  <div class="sidebar-spacer" aria-hidden="true"></div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSidebarStore } from '@/stores/sidebar'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const sidebarStore = useSidebarStore()

const go = (path) => {
  if (route.path === path) return
  router.push(path)
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar-wrapper {
  display: flex;
  flex-shrink: 0;
}

.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  width: 220px;
  z-index: 100;
  background: #1e3c72;
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
}

.sidebar-spacer {
  width: 220px;
  flex-shrink: 0;
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

.parent-item {
  font-weight: 600;
  justify-content: flex-start;
}

.child-item {
  padding-left: 28px;
  font-size: 13px;
}

.submenu-arrow {
  font-size: 12px;
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

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .sidebar-spacer {
    width: 0;
  }

  .sidebar-menu {
    flex-direction: row;
  }
}
</style>

