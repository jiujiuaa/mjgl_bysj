<template>
  <div class="sidebar-wrapper">
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">管理系统</div>
    </div>
    <nav class="sidebar-menu">
      <!-- 顶级：用户管理 -->
      <div
        v-if="canAccess('/user-management')"
        class="menu-item"
        :class="{ active: route.path === '/user-management' }"
        @click="go('/user-management')"
      >
        <img class="menu-icon" src="/查看.png" alt="用户管理" />
        <span>用户管理</span>
      </div>
      <!-- 顶级：模具管理 -->
      <div
        v-if="canAccess('/mold-management')"
        class="menu-item"
        :class="{ active: route.path === '/mold-management' }"
        @click="go('/mold-management')"
      >
        <img class="menu-icon" src="/项目总览.png" alt="模具管理" />
        <span>模具管理</span>
      </div>

      <!-- 运维管理分组 -->
      <div
        v-if="showOpsGroup"
        class="menu-item parent-item"
        @click="sidebarStore.toggleOps()"
      >
        <img class="menu-icon" src="/工作台.png" alt="运维管理" />
        <span>运维管理</span>
        <span class="submenu-arrow">{{ sidebarStore.showOpsChildren ? '▾' : '▸' }}</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren && canAccess('/mold-use-records')"
        class="menu-item child-item"
        :class="{ active: route.path === '/mold-use-records' }"
        @click="go('/mold-use-records')"
      >
        <img class="menu-icon" src="/列表.png" alt="使用记录" />
        <span>使用记录</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren && canAccess('/mold-scrap-records')"
        class="menu-item child-item"
        :class="{ active: route.path === '/mold-scrap-records' }"
        @click="go('/mold-scrap-records')"
      >
        <img class="menu-icon" src="/提交.png" alt="报废申请" />
        <span>报废申请</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren && canAccess('/repair-records')"
        class="menu-item child-item"
        :class="{ active: route.path === '/repair-records' }"
        @click="go('/repair-records')"
      >
        <img class="menu-icon" src="/检修.png" alt="维修记录" />
        <span>维修记录</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren && canAccess('/maintenance-plans')"
        class="menu-item child-item"
        :class="{ active: route.path === '/maintenance-plans' }"
        @click="go('/maintenance-plans')"
      >
        <img class="menu-icon" src="/工作台.png" alt="保养计划" />
        <span>保养计划</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren && canAccess('/maintenance-logs')"
        class="menu-item child-item"
        :class="{ active: route.path === '/maintenance-logs' }"
        @click="go('/maintenance-logs')"
      >
        <img class="menu-icon" src="/工作台.png" alt="保养记录" />
        <span>保养记录</span>
      </div>
      <div
        v-if="sidebarStore.showOpsChildren && canAccess('/maintenance-reminders')"
        class="menu-item child-item"
        :class="{ active: route.path === '/maintenance-reminders' }"
        @click="go('/maintenance-reminders')"
      >
        <img class="menu-icon" src="/消息通知.png" alt="保养提醒" />
        <span>保养提醒</span>
      </div>

      <!-- 监测与异常分组 -->
      <div
        v-if="showMonitoringGroup"
        class="menu-item parent-item"
        @click="sidebarStore.toggleMonitoring()"
      >
        <img class="menu-icon" src="/项目总览.png" alt="监测与异常" />
        <span>监测与异常</span>
        <span class="submenu-arrow">{{ sidebarStore.showMonitoringChildren ? '▾' : '▸' }}</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren && canAccess('/monitoring-temperature')"
        class="menu-item child-item"
        :class="{ active: route.path === '/monitoring-temperature' }"
        @click="go('/monitoring-temperature')"
      >
        <img class="menu-icon" src="/检修.png" alt="温度巡检录入" />
        <span>温度巡检录入</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren && canAccess('/monitoring-lubrication')"
        class="menu-item child-item"
        :class="{ active: route.path === '/monitoring-lubrication' }"
        @click="go('/monitoring-lubrication')"
      >
        <img class="menu-icon" src="/检修.png" alt="润滑巡检录入" />
        <span>润滑巡检录入</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren && canAccess('/monitoring-abnormal')"
        class="menu-item child-item"
        :class="{ active: route.path === '/monitoring-abnormal' }"
        @click="go('/monitoring-abnormal')"
      >
        <img class="menu-icon" src="/消息通知.png" alt="异常记录" />
        <span>异常记录</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren && canAccess('/alert-records')"
        class="menu-item child-item"
        :class="{ active: route.path === '/alert-records' }"
        @click="go('/alert-records')"
      >
        <img class="menu-icon" src="/消息通知.png" alt="报警管理" />
        <span>报警管理</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren && canAccess('/alert-rules')"
        class="menu-item child-item"
        :class="{ active: route.path === '/alert-rules' }"
        @click="go('/alert-rules')"
      >
        <img class="menu-icon" src="/列表.png" alt="预警规则" />
        <span>预警规则</span>
      </div>
      <div
        v-if="sidebarStore.showMonitoringChildren && canAccess('/dashboard-charts')"
        class="menu-item child-item"
        :class="{ active: route.path === '/dashboard-charts' }"
        @click="go('/dashboard-charts')"
      >
        <img class="menu-icon" src="/项目总览.png" alt="监测数据展示" />
        <span>监测数据展示</span>
      </div>

      <!-- 健康评估 -->
      <div
        v-if="canAccess('/health-reports')"
        class="menu-item"
        :class="{ active: route.path === '/health-reports' }"
        @click="go('/health-reports')"
      >
        <img class="menu-icon" src="/查看.png" alt="健康评估" />
        <span>健康评估</span>
      </div>

      <!-- 统计看板与报表 -->
      <div
        v-if="canAccess('/mold-statistics')"
        class="menu-item"
        :class="{ active: route.path === '/mold-statistics' }"
        @click="go('/mold-statistics')"
      >
        <img class="menu-icon" src="/项目总览.png" alt="模具统计看板" />
        <span>模具统计看板</span>
      </div>
      <div
        v-if="canAccess('/approval-center')"
        class="menu-item"
        :class="{ active: route.path === '/approval-center' }"
        @click="go('/approval-center')"
      >
        <img class="menu-icon" src="/提交.png" alt="审批中心" />
        <span>审批中心</span>
      </div>
    </nav>

    <div class="sidebar-footer">
      <button
        class="home-shortcut-row"
        type="button"
        title="返回首页"
        @click="go('/home')"
      >
        <span class="home-shortcut-btn">
          <img class="home-shortcut-icon" src="/037_主页37.png" alt="返回首页" />
        </span>
        <span class="home-shortcut-text">返回首页</span>
      </button>
      <button class="sidebar-logout" @click="handleLogout">
        退出登录
      </button>
    </div>
  </aside>
  <div class="sidebar-spacer" aria-hidden="true"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSidebarStore } from '@/stores/sidebar'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const sidebarStore = useSidebarStore()
const canAccess = (path) => authStore.canAccessPath(path)

const showOpsGroup = computed(() =>
  [
    '/mold-use-records',
    '/mold-scrap-records',
    '/repair-records',
    '/maintenance-plans',
    '/maintenance-logs',
    '/maintenance-reminders',
  ].some((path) => canAccess(path)),
)

const showMonitoringGroup = computed(() =>
  [
    '/monitoring-temperature',
    '/monitoring-lubrication',
    '/monitoring-abnormal',
    '/alert-records',
    '/alert-rules',
    '/dashboard-charts',
  ].some((path) => canAccess(path)),
)

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
  background: linear-gradient(180deg, #1e3a8a 0%, #1e3c72 45%, #1d4f91 100%);
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

.home-shortcut-btn {
  width: 34px;
  height: 34px;
  border: 1px solid rgba(191, 219, 254, 0.55);
  border-radius: 999px;
  background: linear-gradient(135deg, #38bdf8 0%, #2563eb 100%);
  color: #ffffff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.35);
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}

.home-shortcut-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.45);
}

.home-shortcut-btn:active {
  transform: translateY(0);
  opacity: 0.95;
}

.home-shortcut-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
  filter: brightness(0) invert(1);
  opacity: 0.95;
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
  border-radius: 10px;
  cursor: pointer;
  color: #e5e7eb;
  font-size: 14px;
  transition: background 0.2s, color 0.2s, transform 0.15s;
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
  height: 18px;
  object-fit: contain;
  flex-shrink: 0;
  filter: brightness(0) invert(1);
  opacity: 0.92;
  transition: transform 0.2s ease, opacity 0.2s ease, filter 0.2s ease;
}

.menu-item.active {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.35);
}

.menu-item.active .menu-icon,
.menu-item:hover .menu-icon {
  opacity: 1;
  filter: brightness(0) invert(1) drop-shadow(0 0 2px rgba(255, 255, 255, 0.35));
  transform: scale(1.05);
}

.menu-item:not(.active):hover {
  background: rgba(191, 219, 254, 0.2);
  transform: translateX(2px);
}

.menu-item.disabled {
  opacity: 0.6;
  cursor: default;
}

.sidebar-footer {
  padding-top: 12px;
  border-top: 1px solid rgba(191, 219, 254, 0.35);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.home-shortcut-row {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(148, 163, 184, 0.8);
  border-radius: 6px;
  padding: 6px 8px;
  background: transparent;
  color: #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
}

.home-shortcut-row:hover {
  background: rgba(148, 163, 184, 0.3);
}

.home-shortcut-text {
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

