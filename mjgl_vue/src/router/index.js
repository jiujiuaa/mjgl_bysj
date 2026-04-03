import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { canAccessPathByRole } from '@/constants/permissions'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/dashboard-charts',
      name: 'DashboardCharts',
      component: () => import('@/views/DashboardCharts.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/user-management',
      name: 'UserManagement',
      component: () => import('@/views/UserManagement.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/business-config',
      name: 'BusinessConfigCenter',
      component: () => import('@/views/BusinessConfigCenter.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'UserProfile',
      component: () => import('@/views/UserProfile.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/mold-management',
      name: 'MoldManagement',
      component: () => import('@/views/MoldManagement.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/mold-use-records',
      name: 'MoldUseRecords',
      component: () => import('@/views/MoldUseRecords.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/mold-scrap-records',
      name: 'MoldScrapApplications',
      component: () => import('@/views/MoldScrapApplications.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/repair-records',
      name: 'RepairRecords',
      component: () => import('@/views/RepairRecords.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/maintenance-plans',
      name: 'MaintenancePlans',
      component: () => import('@/views/MaintenancePlans.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/maintenance-logs',
      name: 'MaintenanceLogs',
      component: () => import('@/views/MaintenanceLogs.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/maintenance-reminders',
      name: 'MaintenanceReminders',
      component: () => import('@/views/MaintenanceReminders.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/monitoring-temperature',
      name: 'MonitoringTemperature',
      component: () => import('@/views/MonitoringTemperature.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/monitoring-lubrication',
      name: 'MonitoringLubrication',
      component: () => import('@/views/MonitoringLubrication.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/monitoring-abnormal',
      name: 'MonitoringAbnormal',
      component: () => import('@/views/MonitoringAbnormal.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/alert-records',
      name: 'AlertRecords',
      component: () => import('@/views/AlertRecords.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/alert-rules',
      name: 'AlertRules',
      component: () => import('@/views/AlertRules.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/health-reports',
      name: 'HealthReports',
      component: () => import('@/views/HealthReports.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/mold-statistics',
      name: 'MoldStatistics',
      component: () => import('@/views/MoldStatistics.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/approval-center',
      name: 'ApprovalCenter',
      component: () => import('@/views/ApprovalCenter.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/monitoring-manual',
      redirect: '/monitoring-temperature',
    },
    {
      path: '/alert-test',
      name: 'AlertTest',
      component: () => import('@/views/AlertTest.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

// 路由守卫 - 检查登录状态与基础角色权限
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 需要登录但未登录，跳转到登录页
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }

  // 已登录访问登录页，统一跳到首页
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/home')
    return
  }

  if (to.meta.requiresAuth && authStore.isAuthenticated) {
    const canAccess = canAccessPathByRole(to.path, authStore.role)
    if (!canAccess) {
      next('/home')
      return
    }
  }

  next()
})

export default router
