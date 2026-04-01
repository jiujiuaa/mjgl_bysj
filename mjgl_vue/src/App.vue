<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  fetchUnreadNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '@/api/notifications'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const canSendAlert = computed(() => authStore.canAccessPath('/alert-test'))

// 登录页不显示右上角用户信息，其它页面且已登录时显示
const showGlobalHeader = computed(() => authStore.isAuthenticated && route.path !== '/login')

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}

const goUserProfile = () => {
  if (!authStore.isAuthenticated) {
    return
  }
  router.push('/profile')
}

// 顶部右上角小铃铛相关状态（仅展示通知，不发送）
const notifications = ref([])
const showBellDropdown = ref(false)
const loadingNotifications = ref(false)
const unreadCount = computed(() => notifications.value.length)

const goAlertTest = () => {
  // 只允许已登录用户跳转，通常也只给管理员用
  if (!authStore.isAuthenticated || !canSendAlert.value) {
    return
  }
  router.push('/alert-test')
}

const loadNotifications = async () => {
  if (!authStore.isAuthenticated) {
    notifications.value = []
    return
  }
  loadingNotifications.value = true
  try {
    const res = await fetchUnreadNotifications()
    notifications.value = res.data || []
  } catch (e) {
    // 静默失败
  } finally {
    loadingNotifications.value = false
  }
}

const toggleBellDropdown = async () => {
  if (!showBellDropdown.value && authStore.isAuthenticated) {
    await loadNotifications()
  }
  showBellDropdown.value = !showBellDropdown.value
}

const handleNotificationClick = async (n, idx) => {
  // 兼容老行为：点击整行也视为已读
  await handleNotificationMarkRead(n, idx)
}

const handleNotificationMarkRead = async (n, idx) => {
  if (!n?.id) return
  try {
    await markNotificationRead(n.id)
  } catch (e) {
    // 忽略错误
  } finally {
    notifications.value.splice(idx, 1)
  }
}

const handleMarkAllNotificationsRead = async () => {
  if (!notifications.value.length) return
  try {
    await markAllNotificationsRead()
  } catch (e) {
    // 忽略错误
  } finally {
    notifications.value = []
  }
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    loadNotifications()
  }
})

watch(
  () => authStore.isAuthenticated,
  (val) => {
    if (val) {
      loadNotifications()
    } else {
      notifications.value = []
      showBellDropdown.value = false
    }
  },
)

watch(
  () => route.path,
  () => {
    innerScrollTarget.value = null
    innerScrollTop.value = 0
    updateShowBackToTop()
  },
)

// 返回顶部按钮：窗口或内部滚动超过 300px 时显示
const showBackToTop = ref(false)
const innerScrollTarget = ref(null)
const innerScrollTop = ref(0)

const updateShowBackToTop = () => {
  showBackToTop.value = window.scrollY > 300 || innerScrollTop.value > 300
}

const onWindowScroll = () => {
  updateShowBackToTop()
}

const onBackToTopScroll = (e) => {
  innerScrollTarget.value = e.target
  innerScrollTop.value = e.detail?.scrollTop ?? 0
  updateShowBackToTop()
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  if (innerScrollTarget.value) {
    innerScrollTarget.value.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

onMounted(() => {
  window.addEventListener('scroll', onWindowScroll, { passive: true })
  document.addEventListener('back-to-top-scroll', onBackToTopScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onWindowScroll)
  document.removeEventListener('back-to-top-scroll', onBackToTopScroll)
})
</script>

<template>
  <div class="app-root">
    <header v-if="showGlobalHeader" class="global-header">
      <div class="global-header-right">
        <button
          v-if="authStore.isAuthenticated && canSendAlert"
          type="button"
          class="global-header-send"
          title="发送告警 / 消息"
          @click="goAlertTest"
        >
          <img class="send-icon" src="/通知.png" alt="发送告警" />
        </button>
        <div class="global-header-bell" @click.stop="toggleBellDropdown">
          <img class="bell-icon" src="/消息通知.png" alt="通知" />
          <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount }}</span>
          <div v-if="showBellDropdown" class="bell-dropdown">
            <div class="bell-dropdown-header">
              <span>未读通知 ({{ unreadCount }})</span>
              <div class="bell-dropdown-actions">
                <button class="bell-read-all" type="button" @click.stop="handleMarkAllNotificationsRead">
                  全部已读
                </button>
                <button class="bell-refresh" type="button" @click.stop="loadNotifications">
                  刷新
                </button>
              </div>
            </div>
            <div v-if="loadingNotifications" class="bell-dropdown-empty">加载中...</div>
            <div v-else-if="unreadCount === 0" class="bell-dropdown-empty">暂无未读通知</div>
            <ul v-else class="bell-list">
              <li
                v-for="(n, idx) in notifications"
                :key="n.id || idx"
                class="bell-item"
              >
                <div class="bell-item-main" @click.stop="handleNotificationClick(n, idx)">
                  <div class="bell-item-title">{{ n.title }}</div>
                  <div class="bell-item-content">{{ n.content }}</div>
                  <div class="bell-item-meta">
                    推送人: {{ n.senderName || n.senderId || '系统' }} | 时间: {{ n.createdAt }}
                  </div>
                </div>
                <button
                  type="button"
                  class="bell-item-read"
                  @click.stop="handleNotificationMarkRead(n, idx)"
                >
                  已读
                </button>
              </li>
            </ul>
          </div>
        </div>
        <button class="global-header-profile" type="button" title="编辑个人资料" @click="goUserProfile">
          <span class="global-header-username">{{ authStore.username }}</span>
        </button>
        <button class="global-header-logout" @click="handleLogout">退出</button>
      </div>
    </header>
    <RouterView />
    <Transition name="fade">
      <button
        v-show="showBackToTop"
        type="button"
        class="back-to-top"
        title="返回顶部"
        aria-label="返回顶部"
        @click="scrollToTop"
      >
        ↑
      </button>
    </Transition>
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

.global-header-send {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  border-radius: 999px;
  transition: background 0.2s, transform 0.1s;
}

.global-header-send:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.global-header-send:active {
  transform: translateY(0);
}

.global-header-bell {
  position: relative;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 32px;
}

.bell-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
  filter: contrast(1.25) saturate(1.1);
}

.send-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
  display: block;
  filter: contrast(1.25) saturate(1.1);
}

.bell-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: #ef4444;
  color: #ffffff;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bell-dropdown {
  position: absolute;
  top: 28px;
  right: 0;
  width: 320px;
  max-height: 360px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.25);
  padding: 8px 0;
  z-index: 300;
}

.bell-dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px 8px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
  color: #374151;
}

.bell-dropdown-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bell-read-all {
  border: none;
  background: transparent;
  font-size: 12px;
  color: #2563eb;
  cursor: pointer;
}

.bell-refresh {
  border: none;
  background: transparent;
  font-size: 12px;
  color: #2563eb;
  cursor: pointer;
}

.bell-dropdown-empty {
  padding: 12px;
  font-size: 13px;
  color: #6b7280;
  text-align: center;
}

.bell-list {
  list-style: none;
  margin: 0;
  padding: 4px 0;
  max-height: 300px;
  overflow-y: auto;
}

.bell-item {
  padding: 8px 12px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.bell-item:last-child {
  border-bottom: none;
}

.bell-item-main {
  flex: 1;
  cursor: pointer;
}

.bell-item-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.bell-item-content {
  color: #4b5563;
  margin-bottom: 2px;
}

.bell-item-meta {
  font-size: 12px;
  color: #6b7280;
}

.bell-item-read {
  border: 1px solid #d1d5db;
  background: #ffffff;
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}

.bell-item-read:hover {
  background: #f3f4f6;
}

.global-header-username {
  font-size: 14px;
  color: #374151;
}

.global-header-profile {
  border: 1px solid transparent;
  background: transparent;
  border-radius: 6px;
  padding: 4px 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.global-header-profile:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
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

.back-to-top {
  position: fixed;
  right: 24px;
  top: 72px; /* 在全局顶栏下方 */
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(30, 60, 114, 0.4);
  z-index: 99;
  transition: transform 0.2s, box-shadow 0.2s;
}

.back-to-top:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(30, 60, 114, 0.5);
}

.back-to-top:active {
  transform: translateY(0);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 统一替换旧版 emoji 图标 */
span.btn-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  font-size: 0 !important;
  color: transparent !important;
  background: url('/新建.png') center/contain no-repeat;
  vertical-align: middle;
}

span.message-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  font-size: 0 !important;
  color: transparent !important;
  background: url('/消息通知.png') center/contain no-repeat;
  vertical-align: middle;
}

.success-message span.message-icon {
  background-image: url('/查看.png');
}

.error-message span.message-icon {
  background-image: url('/消息通知.png');
}

.confirm-icon.danger {
  font-size: 0 !important;
  color: transparent !important;
  background-color: #fee2e2 !important;
  background-image: url('/清除筛选.png') !important;
  background-repeat: no-repeat !important;
  background-position: center !important;
  background-size: 28px 28px !important;
}

span.stat-icon,
.welcome-icon {
  display: inline-block;
  width: 20px;
  height: 20px;
  font-size: 0 !important;
  color: transparent !important;
  background: url('/项目总览.png') center/contain no-repeat;
  vertical-align: middle;
}
</style>
