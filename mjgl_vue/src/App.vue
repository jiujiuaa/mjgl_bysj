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
const canAccessBusinessConfig = computed(() => authStore.canAccessPath('/business-config'))

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

const goBusinessConfig = () => {
  if (!authStore.isAuthenticated || !canAccessBusinessConfig.value) {
    return
  }
  router.push('/business-config')
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
        <button
          v-if="canAccessBusinessConfig"
          type="button"
          class="global-header-settings"
          title="业务配置"
          @click="goBusinessConfig"
        >
          <img class="settings-icon" src="/设置.png" alt="业务配置" />
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
:root {
  --ui-bg: #f3f6fb;
  --ui-surface: #ffffff;
  --ui-border: #e2e8f0;
  --ui-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
  --ui-shadow-hover: 0 12px 28px rgba(15, 23, 42, 0.12);
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --control-h: 36px;
  --btn-primary-bg: linear-gradient(135deg, #1e40af 0%, #2563eb 100%);
  --btn-primary-text: #ffffff;
  --btn-secondary-bg: #ffffff;
  --btn-secondary-text: #334155;
  --btn-secondary-border: #cbd5e1;
  --btn-danger-bg: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
  --btn-danger-text: #ffffff;
  --status-success-bg: #dcfce7;
  --status-success-text: #166534;
  --status-info-bg: #dbeafe;
  --status-info-text: #1d4ed8;
  --status-warning-bg: #fef3c7;
  --status-warning-text: #92400e;
  --status-danger-bg: #fee2e2;
  --status-danger-text: #b91c1c;
  --status-muted-bg: #e5e7eb;
  --status-muted-text: #4b5563;
}

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
  background: radial-gradient(circle at 10% -10%, #dbeafe 0%, transparent 40%),
    radial-gradient(circle at 110% 10%, #e0e7ff 0%, transparent 35%), var(--ui-bg);
}

#app {
  min-height: 100vh;
}

.app-root {
  min-height: 100vh;
}

.app-root::before {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.35) 0%, rgba(255, 255, 255, 0) 32%);
  z-index: 0;
}

.app-root > * {
  position: relative;
  z-index: 1;
}

.global-header {
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08);
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

.global-header-settings {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  border-radius: 999px;
  transition: background 0.2s, transform 0.1s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.global-header-settings:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.global-header-settings:active {
  transform: translateY(0);
}

.settings-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  display: block;
  filter: contrast(1.15) saturate(1.05);
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

/* 全局轻量美化：覆盖大部分页面通用结构 */
.top-header,
.panel-card,
.menu-list-card,
.quick-entry-card,
.stat-card,
.card,
.table-card,
.list-card {
  box-shadow: var(--ui-shadow);
  border-color: var(--ui-border);
}

.panel-card,
.menu-list-card,
.quick-entry-card,
.stat-card,
.card,
.table-card,
.list-card {
  transition: box-shadow 0.22s ease, transform 0.22s ease;
}

.panel-card:hover,
.menu-list-card:hover,
.quick-entry-card:hover,
.stat-card:hover,
.card:hover,
.table-card:hover,
.list-card:hover {
  box-shadow: var(--ui-shadow-hover);
  transform: translateY(-1px);
}

button,
.btn,
.action-btn,
.menu-link {
  transition: all 0.18s ease;
}

button:hover,
.btn:hover,
.action-btn:hover {
  filter: saturate(1.04);
}

/* 按钮层级统一 */
button,
.btn,
.action-btn,
.submit-button,
.cancel-button,
.link-btn {
  border-radius: 8px;
  border: 1px solid transparent;
  font-weight: 500;
}

.btn.primary,
.submit-button,
.save-button,
.confirm-ok-btn.warning,
.action-btn.primary {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
  border-color: transparent;
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.28);
}

.btn.primary:hover,
.submit-button:hover,
.save-button:hover,
.confirm-ok-btn.warning:hover,
.action-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.32);
}

.btn.ghost,
.cancel-button,
.confirm-cancel-btn,
.action-btn.secondary,
.action-btn.cancel {
  background: var(--btn-secondary-bg);
  color: var(--btn-secondary-text);
  border-color: var(--btn-secondary-border);
}

.btn.ghost:hover,
.cancel-button:hover,
.confirm-cancel-btn:hover,
.action-btn.secondary:hover,
.action-btn.cancel:hover {
  background: #f8fafc;
  border-color: #94a3b8;
}

.danger-btn,
.delete-btn,
.confirm-ok-btn.danger,
.action-btn.delete,
.action-btn.danger {
  background: var(--btn-danger-bg);
  color: var(--btn-danger-text);
  border-color: transparent;
  box-shadow: 0 6px 14px rgba(239, 68, 68, 0.25);
}

.danger-btn:hover,
.delete-btn:hover,
.confirm-ok-btn.danger:hover,
.action-btn.delete:hover,
.action-btn.danger:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(239, 68, 68, 0.32);
}

button:disabled,
.btn:disabled,
.action-btn:disabled,
.submit-button:disabled,
.cancel-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none !important;
  transform: none !important;
}

.link-btn {
  background: transparent;
  color: #2563eb;
  border-color: transparent;
}

.link-btn:hover {
  background: rgba(37, 99, 235, 0.08);
  color: #1d4ed8;
}

/* 状态标签统一 */
.status-tag,
.status-badge,
.badge,
.tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 24px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  border: 1px solid transparent;
  white-space: nowrap;
}

.status-success,
.status-enabled,
.status-normal,
.badge-success,
.tag-success {
  background: var(--status-success-bg);
  color: var(--status-success-text);
  border-color: #bbf7d0;
}

.status-info,
.status-processing,
.status-running,
.status-active,
.badge-info,
.tag-info {
  background: var(--status-info-bg);
  color: var(--status-info-text);
  border-color: #bfdbfe;
}

.status-warning,
.status-pending,
.status-waiting,
.badge-warning,
.tag-warning {
  background: var(--status-warning-bg);
  color: var(--status-warning-text);
  border-color: #fde68a;
}

.status-danger,
.status-error,
.status-rejected,
.status-overdue,
.badge-danger,
.tag-danger {
  background: var(--status-danger-bg);
  color: var(--status-danger-text);
  border-color: #fecaca;
}

.status-muted,
.status-disabled,
.status-closed,
.badge-muted,
.tag-muted {
  background: var(--status-muted-bg);
  color: var(--status-muted-text);
  border-color: #d1d5db;
}

/* 全局加载态与骨架屏 */
.loading-state {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-size: 14px;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #dbeafe;
  border-top-color: #2563eb;
  animation: spin 0.7s linear infinite;
}

.skeleton {
  position: relative;
  overflow: hidden;
  background: #e2e8f0;
  border-radius: 8px;
}

.skeleton::after {
  content: '';
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.65), transparent);
  animation: skeleton-shimmer 1.25s ease-in-out infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes skeleton-shimmer {
  100% {
    transform: translateX(100%);
  }
}

/* 统一焦点可见性（键盘导航友好） */
:focus-visible {
  outline: 2px solid #60a5fa;
  outline-offset: 2px;
}

/* 滚动条统一 */
*::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

*::-webkit-scrollbar-track {
  background: #f1f5f9;
}

*::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
  border: 2px solid #f1f5f9;
}

*::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 统一对话框进场动画 */
.dialog-content,
.confirm-dialog {
  animation: dialog-fade-in 0.2s ease-out;
}

@keyframes dialog-fade-in {
  from {
    opacity: 0;
    transform: translateY(6px) scale(0.99);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 低动态偏好 */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}

/* 第二层统一优化：标题区、表格、弹窗 */
.top-header {
  border: 1px solid var(--ui-border);
  border-radius: 14px;
  margin-bottom: var(--space-4);
}

.top-title {
  letter-spacing: 0.3px;
}

.top-subtitle {
  color: #cbd5e1;
}

.card-header,
.panel-header,
.dialog-header {
  border-bottom-color: #e2e8f0;
  padding: var(--space-3) var(--space-4);
}

.table-wrapper,
.table-container {
  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: var(--radius-md);
  box-shadow: var(--ui-shadow);
}

table th {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  color: #334155;
}

table tbody tr {
  transition: background-color 0.18s ease;
}

table tbody tr:hover {
  background: rgba(59, 130, 246, 0.05);
}

.dialog-overlay {
  backdrop-filter: blur(2px);
}

.dialog-content,
.confirm-dialog {
  border: 1px solid var(--ui-border);
  box-shadow: var(--ui-shadow-hover);
}

/* 间距节奏：统一常见块之间留白 */
.main-content > * + *,
.card + .card,
.panel-card + .panel-card,
.table-card + .table-card,
.list-card + .list-card {
  margin-top: var(--space-4);
}

.form-row {
  margin-bottom: var(--space-3);
}

.form-group {
  gap: var(--space-2);
}

/* 空状态统一 */
.empty-state,
.empty-text,
.table-empty,
.no-data {
  padding: var(--space-5) var(--space-4);
  border: 1px dashed #cbd5e1;
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  color: #64748b;
  text-align: center;
  font-size: 14px;
}

/* 成功/错误提示统一 */
.success-message,
.error-message,
.warning-message {
  border-radius: var(--radius-sm);
  border-width: 1px;
  border-style: solid;
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-3);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.success-message {
  border-color: #bbf7d0;
  background: #f0fdf4;
  color: #166534;
}

.error-message {
  border-color: #fecaca;
  background: #fef2f2;
  color: #991b1b;
}

.warning-message {
  border-color: #fde68a;
  background: #fffbeb;
  color: #92400e;
}

/* 交互控件统一：输入框、下拉、文本域、分页 */
input[type='text'],
input[type='password'],
input[type='number'],
input[type='date'],
input[type='datetime-local'],
select,
textarea,
.form-input,
.filter-input,
.search-input {
  height: var(--control-h);
  border: 1px solid #cbd5e1;
  border-radius: var(--radius-sm);
  background: #fff;
  color: #0f172a;
  padding: 0 10px;
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
}

textarea {
  min-height: 88px;
  height: auto;
  padding: 8px 10px;
  line-height: 1.5;
}

input::placeholder,
textarea::placeholder {
  color: #94a3b8;
}

input:focus,
select:focus,
textarea:focus,
.form-input:focus,
.filter-input:focus,
.search-input:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.14);
}

input:disabled,
select:disabled,
textarea:disabled,
.form-input:disabled {
  background: #f8fafc;
  color: #94a3b8;
  cursor: not-allowed;
}

select {
  padding-right: 10px;
}

.pagination,
.pager,
.page-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn,
.pager button,
.pagination button {
  min-width: 34px;
  height: 34px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  cursor: pointer;
  transition: all 0.18s ease;
}

.page-btn:hover,
.pager button:hover,
.pagination button:hover {
  border-color: #93c5fd;
  background: #eff6ff;
}

.page-btn:disabled,
.pager button:disabled,
.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
