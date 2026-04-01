<template>
  <div class="home-container">
    <div class="layout-main">
      <header class="top-header">
        <div class="hero-left">
          <div class="top-title">模具管理系统门户</div>
          <div class="top-subtitle">你好，{{ authStore.username || '用户' }}。统一查看系统状态，并从下方快捷进入各功能菜单。</div>
          <div class="hero-tags">
            <span class="hero-tag">角色：{{ authStore.role || '-' }}</span>
            <span class="hero-tag">当前时间：{{ nowText }}</span>
          </div>
        </div>
        <div class="header-actions">
          <span class="refresh-tips">自动刷新：30秒</span>
          <button class="header-icon-btn refresh-btn" type="button" :disabled="loading" @click="loadHomeData">
            <img class="btn-icon" src="/更新.png" alt="刷新" />
            <span>{{ loading ? '刷新中...' : '刷新' }}</span>
          </button>
        </div>
      </header>

      <main class="main-content">
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

        <section class="stats-grid">
          <div class="stat-card">
            <div class="stat-label">模具资产总数</div>
            <div class="stat-value">{{ moldStats.total }}</div>
            <div class="stat-desc">系统内已登记模具总量</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">使用中模具</div>
            <div class="stat-value">{{ moldStats.inUse }}</div>
            <div class="stat-desc">当前处于生产/借用中</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">待报废模具</div>
            <div class="stat-value">{{ moldStats.waitScrap }}</div>
            <div class="stat-desc">待执行报废流程</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">待处理告警</div>
            <div class="stat-value">{{ alertStats.pending }}</div>
            <div class="stat-desc">需尽快关注的异常项</div>
          </div>
          <div v-if="canViewApproval" class="stat-card">
            <div class="stat-label">待审批总数</div>
            <div class="stat-value">{{ approvalStats.total }}</div>
            <div class="stat-desc">集中在审批中心处理</div>
          </div>
        </section>

        <section class="panel-grid">
          <div v-if="canViewApproval" class="panel-card">
            <div class="panel-header">
              <h3>审批待办</h3>
              <button class="link-btn" type="button" @click="go('/approval-center')">进入审批中心</button>
            </div>
            <div class="todo-list">
              <div class="todo-item"><span>使用记录</span><b>{{ approvalStats.use }}</b></div>
              <div class="todo-item"><span>保养记录</span><b>{{ approvalStats.maintenance }}</b></div>
              <div class="todo-item"><span>维修记录</span><b>{{ approvalStats.repair }}</b></div>
              <div class="todo-item"><span>报废申请</span><b>{{ approvalStats.scrap }}</b></div>
            </div>
          </div>

          <div class="panel-card">
            <div class="panel-header">
              <h3>告警摘要</h3>
              <button class="link-btn" type="button" @click="go('/alert-records')">查看全部</button>
            </div>
            <div v-if="latestAlerts.length === 0" class="empty-text">暂无告警数据</div>
            <div v-else class="alert-list">
              <div v-for="a in latestAlerts" :key="a.id" class="alert-item">
                <div class="alert-title">{{ a.alertTypeDesc || a.alertType || '告警' }} - {{ a.title || '-' }}</div>
                <div class="alert-meta">{{ a.moldCode || '-' }} / {{ formatDate(a.createdAt) }}</div>
              </div>
            </div>
          </div>
        </section>

        <section class="quick-entry-card">
          <div class="panel-header">
            <h3>功能菜单快捷入口</h3>
          </div>
          <div class="quick-grid">
            <button v-for="entry in visibleQuickEntries" :key="entry.path" class="quick-item" type="button" @click="go(entry.path)">
              <img class="quick-icon" :src="entry.icon" :alt="entry.title" />
              <span class="quick-title">{{ entry.title }}</span>
              <span class="quick-desc">{{ entry.desc }}</span>
            </button>
          </div>
        </section>

        <section class="menu-list-card">
          <div class="panel-header">
            <h3>系统菜单总览</h3>
          </div>
          <div class="menu-list-grid">
            <div class="menu-group">
              <div class="menu-group-title">基础管理</div>
              <div class="menu-links">
                <button v-if="canAccess('/user-management')" type="button" class="menu-link" @click="go('/user-management')">用户管理</button>
                <button v-if="canAccess('/mold-management')" type="button" class="menu-link" @click="go('/mold-management')">模具管理</button>
              </div>
            </div>
            <div class="menu-group">
              <div class="menu-group-title">运维管理</div>
              <div class="menu-links">
                <button v-if="canAccess('/mold-use-records')" type="button" class="menu-link" @click="go('/mold-use-records')">使用记录</button>
                <button v-if="canAccess('/repair-records')" type="button" class="menu-link" @click="go('/repair-records')">维修记录</button>
                <button v-if="canAccess('/maintenance-plans')" type="button" class="menu-link" @click="go('/maintenance-plans')">保养计划</button>
                <button v-if="canAccess('/maintenance-logs')" type="button" class="menu-link" @click="go('/maintenance-logs')">保养记录</button>
                <button v-if="canAccess('/mold-scrap-records')" type="button" class="menu-link" @click="go('/mold-scrap-records')">报废申请</button>
                <button v-if="canViewApproval" type="button" class="menu-link" @click="go('/approval-center')">审批中心</button>
              </div>
            </div>
            <div class="menu-group">
              <div class="menu-group-title">监测与分析</div>
              <div class="menu-links">
                <button v-if="canAccess('/alert-records')" type="button" class="menu-link" @click="go('/alert-records')">报警管理</button>
                <button v-if="canAccess('/dashboard-charts')" type="button" class="menu-link" @click="go('/dashboard-charts')">监测数据展示</button>
                <button v-if="canAccess('/mold-statistics')" type="button" class="menu-link" @click="go('/mold-statistics')">模具统计看板</button>
                <button v-if="canAccess('/health-reports')" type="button" class="menu-link" @click="go('/health-reports')">健康评估</button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { fetchMolds } from '@/api/molds'
import { queryAlerts } from '@/api/alerts'
import { useApprovalDashboard } from '@/composables/useApprovalDashboard'

const router = useRouter()
const authStore = useAuthStore()
const canAccess = (path) => authStore.canAccessPath(path)

const loading = ref(false)
const errorMessage = ref('')
const latestAlerts = ref([])
const timerId = ref(null)
const nowText = ref('')

const moldStats = reactive({
  total: 0,
  inUse: 0,
  waitScrap: 0,
})

const alertStats = reactive({
  pending: 0,
})

const { pendingUseRecords, pendingMaintenanceLogs, pendingRepairRecords, pendingScrapApplications, loadApprovalDashboard } =
  useApprovalDashboard()

const canViewApproval = computed(() => canAccess('/approval-center'))
const quickEntries = computed(() => {
  const base = [
    { path: '/mold-management', icon: '/项目总览.png', title: '模具管理', desc: '维护模具台账与状态' },
    { path: '/mold-use-records', icon: '/列表.png', title: '使用记录', desc: '查看/维护领用归还记录' },
    { path: '/repair-records', icon: '/检修.png', title: '维修记录', desc: '跟踪维修过程与验收' },
    { path: '/maintenance-logs', icon: '/工作台.png', title: '保养记录', desc: '查看保养执行情况' },
    { path: '/alert-records', icon: '/消息通知.png', title: '报警管理', desc: '处理实时告警与异常' },
    { path: '/mold-statistics', icon: '/项目总览.png', title: '统计看板', desc: '查看趋势与统计分析' },
  ]
  return canViewApproval.value
    ? [{ path: '/approval-center', icon: '/提交.png', title: '审批中心', desc: '统一审批待办事项' }, ...base]
    : base
})
const visibleQuickEntries = computed(() => quickEntries.value.filter((entry) => canAccess(entry.path)))

const approvalStats = computed(() => {
  const use = pendingUseRecords.value.length
  const maintenance = pendingMaintenanceLogs.value.length
  const repair = pendingRepairRecords.value.length
  const scrap = pendingScrapApplications.value.length
  return { use, maintenance, repair, scrap, total: use + maintenance + repair + scrap }
})

const formatDate = (val) => {
  if (!val) return '-'
  const date = new Date(typeof val === 'string' ? val.replace(' ', 'T') : val)
  if (Number.isNaN(date.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}/${pad(date.getMonth() + 1)}/${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const refreshNowText = () => {
  nowText.value = new Date().toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const go = (path) => {
  if (router.currentRoute.value.path === path) return
  router.push(path)
}

const loadHomeData = async () => {
  loading.value = true
  errorMessage.value = ''
  refreshNowText()
  try {
    const tasks = [
      fetchMolds(1, 1000),
      queryAlerts({ status: 1 }, 1, 5),
    ]
    if (canViewApproval.value) {
      tasks.push(loadApprovalDashboard())
    }
    const [moldsRes, alertsRes] = await Promise.all(tasks)
    const moldList = moldsRes.data?.list || []
    moldStats.total = moldList.length
    moldStats.inUse = moldList.filter((item) => item.currentStatus === 2).length
    moldStats.waitScrap = moldList.filter((item) => item.currentStatus === 5).length

    const alertList = alertsRes.data?.list || []
    latestAlerts.value = alertList
    alertStats.pending = alertsRes.data?.total ?? alertList.length
  } catch (e) {
    errorMessage.value = e.message || '加载首页数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await loadHomeData()
  timerId.value = setInterval(() => {
    loadHomeData()
  }, 30000)
})

onBeforeUnmount(() => {
  if (timerId.value) {
    clearInterval(timerId.value)
    timerId.value = null
  }
})
</script>

<style scoped>
.home-container { min-height: 100vh; background: #f1f5f9; }
.layout-main { display: flex; flex-direction: column; min-height: 100vh; }
.top-header { padding: 20px 24px; display: flex; justify-content: space-between; align-items: center; background: linear-gradient(120deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); color: #fff; }
.top-title { font-size: 24px; font-weight: 700; }
.top-subtitle { font-size: 13px; opacity: .9; margin-top: 4px; }
.hero-left { display: flex; flex-direction: column; gap: 6px; }
.hero-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.hero-tag { font-size: 12px; background: rgba(255, 255, 255, .2); border: 1px solid rgba(255, 255, 255, .3); padding: 3px 8px; border-radius: 999px; }
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 32px;
}
.refresh-tips {
  font-size: 12px;
  opacity: .9;
  line-height: 32px;
}
.header-icon-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
}
.btn-icon {
  width: 14px;
  height: 14px;
  object-fit: contain;
  flex-shrink: 0;
  filter: contrast(1.25) saturate(1.1);
}
.refresh-btn { color: #1e3c72; background: #fff; }
.logout-btn { color: #ffffff; background: rgba(239, 68, 68, 0.92); border: 1px solid rgba(255, 255, 255, 0.25); }
.main-content { padding: 20px 24px; display: flex; flex-direction: column; gap: 16px; width: 100%; max-width: 1400px; margin: 0 auto; }
.error-message { color: #991b1b; background: #fee2e2; border: 1px solid #fecaca; padding: 10px 12px; border-radius: 6px; }
.stats-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; }
.stat-card { background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); border: 1px solid #dbeafe; border-radius: 12px; padding: 14px; box-shadow: 0 4px 12px rgba(30, 64, 175, .08); }
.stat-label { font-size: 12px; color: #6b7280; }
.stat-value { margin-top: 6px; font-size: 24px; font-weight: 700; color: #1e3a8a; }
.stat-desc { margin-top: 6px; font-size: 12px; color: #64748b; }
.panel-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.panel-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; box-shadow: 0 1px 2px rgba(15, 23, 42, .06); }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.panel-header h3 { margin: 0; font-size: 16px; color: #1f2937; }
.link-btn { border: none; background: transparent; color: #2563eb; cursor: pointer; font-size: 13px; }
.todo-list { display: grid; gap: 8px; }
.todo-item { display: flex; justify-content: space-between; padding: 8px 10px; border-radius: 8px; background: #f8fafc; color: #334155; }
.alert-list { display: grid; gap: 8px; }
.alert-item { padding: 8px 10px; border-radius: 8px; border: 1px solid #e5e7eb; background: #fff; }
.alert-title { font-size: 13px; color: #1f2937; }
.alert-meta { font-size: 12px; color: #6b7280; margin-top: 4px; }
.quick-entry-card,
.menu-list-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; box-shadow: 0 1px 2px rgba(15, 23, 42, .06); }
.quick-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; }
.quick-item { padding: 12px; border: 1px solid #dbeafe; background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%); color: #1d4ed8; border-radius: 10px; cursor: pointer; font-weight: 600; display: flex; flex-direction: column; align-items: flex-start; gap: 4px; text-align: left; }
.quick-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
  filter: contrast(1.25) saturate(1.1);
}
.quick-title { font-size: 14px; }
.quick-desc { font-size: 12px; color: #475569; font-weight: 500; }
.menu-list-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
.menu-group { border: 1px solid #e5e7eb; border-radius: 10px; padding: 10px; background: #f8fafc; }
.menu-group-title { font-size: 13px; font-weight: 700; color: #334155; margin-bottom: 8px; }
.menu-links { display: flex; flex-wrap: wrap; gap: 8px; }
.menu-link { border: 1px solid #cbd5e1; border-radius: 999px; background: #fff; color: #334155; padding: 4px 10px; font-size: 12px; cursor: pointer; }
.empty-text { color: #9ca3af; font-size: 13px; }

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .quick-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .menu-list-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .top-header { padding: 14px 16px; flex-direction: column; align-items: flex-start; gap: 10px; }
  .main-content { padding: 14px 16px; }
  .stats-grid { grid-template-columns: 1fr; }
  .panel-grid { grid-template-columns: 1fr; }
  .quick-grid { grid-template-columns: 1fr; }
  .menu-list-grid { grid-template-columns: 1fr; }
}
</style>

