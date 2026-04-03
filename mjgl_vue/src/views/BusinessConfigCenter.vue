<template>
  <div class="bcc-page">
    <AppSidebar />
    <div class="bcc-main">
      <header class="bcc-header">
        <h1>业务配置中心</h1>
        <p class="bcc-sub">
          保存后立即生效；修改「定时」类会重调度对应任务。定时项请用<strong>下拉选择常用时刻</strong>，无需理解 Cron；仅在「高级」中查看或粘贴原始表达式。
        </p>
      </header>

      <div v-if="errorMessage" class="bcc-error">{{ errorMessage }}</div>
      <div v-if="successMessage" class="bcc-success">{{ successMessage }}</div>

      <div v-if="loading" class="bcc-loading">
        <div class="loading-state">
          <span class="loading-spinner" />
          <span>正在加载配置项...</span>
        </div>
        <div class="bcc-skeleton-wrap">
          <div class="skeleton bcc-skeleton-card" />
          <div class="skeleton bcc-skeleton-card" />
          <div class="skeleton bcc-skeleton-row" />
          <div class="skeleton bcc-skeleton-row" />
          <div class="skeleton bcc-skeleton-row" />
          <div class="skeleton bcc-skeleton-row" />
        </div>
      </div>

      <div v-else>
        <section class="bcc-summary-grid">
          <div class="summary-card">
            <div class="summary-title">配置总数</div>
            <div class="summary-value">{{ totalCount }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-title">已修改项</div>
            <div class="summary-value summary-value-warn">{{ modifiedCount }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-title">定时项</div>
            <div class="summary-value">{{ cronCount }}</div>
          </div>
        </section>

        <section
          v-for="group in groupedRows"
          :key="group.key"
          class="group-card"
        >
          <div class="group-header">
            <div>
              <h3>{{ group.title }}</h3>
              <p>{{ group.desc }}</p>
            </div>
            <div class="group-header-right">
              <span class="group-count">{{ group.rows.length }} 项</span>
              <button type="button" class="group-toggle-btn" @click="toggleGroup(group.key)">
                {{ isGroupExpanded(group.key) ? '收起' : '展开' }}
              </button>
            </div>
          </div>

          <div v-show="isGroupExpanded(group.key)" class="group-body">
            <div
              v-for="row in group.rows"
              :key="row.configKey"
              :class="['config-item', { dirty: isRowDirty(row) }]"
            >
              <div class="config-meta">
                <div class="meta-line">
                  <span class="td-label">{{ row.label }}</span>
                  <span class="type-tag">{{ typeLabelForRow(row) }}</span>
                  <span v-if="isRowDirty(row)" class="dirty-tag">已修改</span>
                </div>
                <p class="td-desc">{{ row.description }}</p>
                <p class="td-human">{{ humanHint(row) }}</p>
              </div>
              <div class="td-tech">
                <template v-if="isCronType(row)">
                  <div class="cron-ui">
                    <label class="cron-field-label">执行时间</label>
                    <select
                      v-model="row._cronPresetId"
                      class="bcc-select"
                      @change="onCronPresetSelect(row)"
                    >
                      <option v-for="p in cronPresets" :key="p.id" :value="p.id">
                        {{ p.label }}
                      </option>
                      <option :value="cronPresetCustom">自定义（高级）…</option>
                    </select>
                    <button
                      type="button"
                      class="cron-advanced-toggle"
                      @click="row._cronAdvancedExpanded = !row._cronAdvancedExpanded"
                    >
                      {{ row._cronAdvancedExpanded ? '收起' : '展开' }}高级（原始 Cron）
                    </button>
                    <div v-show="row._cronAdvancedExpanded" class="cron-advanced-panel">
                      <label class="cron-advanced-label">原始表达式（Spring 6 段：秒 分 时 日 月 周）</label>
                      <input
                        v-model="row.draftValue"
                        class="bcc-input bcc-input-mono"
                        type="text"
                        :spellcheck="false"
                        @input="syncCronPresetFromRaw(row)"
                      />
                    </div>
                  </div>
                </template>
                <template v-else-if="isJwtExpirationRow(row)">
                  <div class="jwt-min-ui">
                    <label class="cron-field-label">有效时长</label>
                    <div class="jwt-input-row">
                      <input
                        v-model.number="row._jwtMinutes"
                        type="number"
                        class="bcc-input jwt-min-input"
                        min="1"
                        max="525600"
                        step="1"
                        placeholder="分钟"
                        @input="syncJwtMsFromMinutes(row)"
                      />
                      <span class="jwt-unit">分钟</span>
                    </div>
                    <p class="jwt-hint-line">保存时自动换算为毫秒写入后台（1～525600 分钟，约 1 分钟～365 天）</p>
                    <button
                      type="button"
                      class="cron-advanced-toggle"
                      @click="row._jwtShowAdvanced = !row._jwtShowAdvanced"
                    >
                      {{ row._jwtShowAdvanced ? '收起' : '展开' }}高级（毫秒，技术人员）
                    </button>
                    <div v-show="row._jwtShowAdvanced" class="cron-advanced-panel">
                      <label class="cron-advanced-label">存储值（毫秒，与后端一致）</label>
                      <input
                        v-model="row.draftValue"
                        class="bcc-input bcc-input-mono"
                        type="text"
                        inputmode="numeric"
                        :spellcheck="false"
                        @input="syncJwtMinutesFromMs(row)"
                      />
                    </div>
                  </div>
                </template>
                <template v-else>
                  <input v-model="row.draftValue" class="bcc-input" type="text" :spellcheck="false" />
                </template>
              </div>
            </div>
          </div>
        </section>

        <div class="bcc-sticky-actions">
          <div class="sticky-tip">
            <span>已修改 {{ modifiedCount }} 项</span>
            <span v-if="modifiedCount > 0">，离开前请先保存</span>
          </div>
          <div class="sticky-buttons">
            <button type="button" class="btn primary" :disabled="saving" @click="onSave">
              {{ saving ? '保存中...' : '保存全部' }}
            </button>
            <button type="button" class="btn ghost" :disabled="loading" @click="loadList">重新加载</button>
          </div>
        </div>
      </div>

      <div v-if="showUnsavedConfirmDialog" class="dialog-overlay" @click="handleUnsavedConfirmCancel">
        <div class="confirm-dialog" @click.stop>
          <div class="confirm-icon warning">⚠️</div>
          <h3 class="confirm-title">未保存修改</h3>
          <p class="confirm-message">{{ unsavedConfirmMessage }}</p>
          <div class="confirm-actions">
            <button type="button" class="confirm-cancel-btn" @click="handleUnsavedConfirmCancel">取消</button>
            <button type="button" class="confirm-ok-btn warning" @click="handleUnsavedConfirmOk">
              {{ unsavedConfirmOkText }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import AppSidebar from '@/components/AppSidebar.vue'
import { fetchBusinessConfigList, saveBusinessConfigBatch } from '@/api/businessConfig'
import { describeSpringCron } from '@/utils/cronHumanize'
import { CRON_PRESETS, CRON_PRESET_CUSTOM, findPresetByCron } from '@/constants/cronPresets'

const cronPresets = CRON_PRESETS
const cronPresetCustom = CRON_PRESET_CUSTOM

const decorateEditableRow = (r) => {
  const draftValue = r.configValue ?? ''
  const base = { ...r, draftValue }
  const vt = String(r.valueType || '').toUpperCase()
  if (vt === 'CRON') {
    const matched = findPresetByCron(draftValue)
    return {
      ...base,
      _cronPresetId: matched ? matched.id : CRON_PRESET_CUSTOM,
      _cronAdvancedExpanded: !matched,
    }
  }
  if (vt === 'LONG' && r.configKey === 'jwt.expiration_ms') {
    const ms = parseInt(String(draftValue).trim(), 10)
    const minutes =
      Number.isFinite(ms) && ms > 0 ? Math.max(1, Math.round(ms / 60000)) : ''
    return {
      ...base,
      _jwtMinutes: minutes,
      _jwtShowAdvanced: false,
    }
  }
  return base
}

const isJwtExpirationRow = (row) =>
  String(row.valueType || '').toUpperCase() === 'LONG' && row.configKey === 'jwt.expiration_ms'

const syncJwtMsFromMinutes = (row) => {
  const m = Number(row._jwtMinutes)
  if (!Number.isFinite(m) || m < 1) {
    row.draftValue = ''
    return
  }
  const capped = Math.min(525600, Math.round(m))
  row._jwtMinutes = capped
  row.draftValue = String(capped * 60000)
}

const syncJwtMinutesFromMs = (row) => {
  const raw = String(row.draftValue ?? '').trim()
  if (!raw) {
    row._jwtMinutes = ''
    return
  }
  const ms = parseInt(raw, 10)
  if (!Number.isFinite(ms) || ms <= 0) {
    row._jwtMinutes = ''
    return
  }
  row._jwtMinutes = Math.max(1, Math.min(525600, Math.round(ms / 60000)))
  row.draftValue = String(row._jwtMinutes * 60000)
}

const onCronPresetSelect = (row) => {
  if (row._cronPresetId === CRON_PRESET_CUSTOM) {
    row._cronAdvancedExpanded = true
    return
  }
  const preset = CRON_PRESETS.find((x) => x.id === row._cronPresetId)
  if (preset) {
    row.draftValue = preset.cron
    row._cronAdvancedExpanded = false
  }
}

const syncCronPresetFromRaw = (row) => {
  const matched = findPresetByCron(row.draftValue)
  row._cronPresetId = matched ? matched.id : CRON_PRESET_CUSTOM
}

const typeLabel = (vt) => {
  const u = String(vt || '').toUpperCase()
  const map = { CRON: '定时', INT: '整数', LONG: '整数(大)', STRING: '文本' }
  return map[u] || vt || '—'
}

const typeLabelForRow = (row) => (isJwtExpirationRow(row) ? '分钟' : typeLabel(row.valueType))

const isCronType = (row) => String(row.valueType || '').toUpperCase() === 'CRON'
const normalizeDraft = (v) => (v == null ? '' : String(v))

const GROUP_DEFS = [
  { key: 'auth', title: '认证与登录', desc: '令牌有效时长等登录相关策略' },
  { key: 'alert', title: '预警默认策略', desc: '预警统计窗口和触发阈值默认值' },
  { key: 'maintenance', title: '保养与提醒', desc: '保养提醒触发节奏和阈值配置' },
  { key: 'system', title: '系统与接口', desc: '分页、WebSocket、文件访问等系统级设置' },
]

const groupKeyByConfig = (configKey) => {
  if (!configKey) return 'system'
  if (configKey.startsWith('jwt.')) return 'auth'
  if (configKey.startsWith('alert.')) return 'alert'
  if (configKey.startsWith('maintenance.')) return 'maintenance'
  return 'system'
}

const DEFAULT_EXPANDED_GROUPS = {
  auth: true,
  alert: true,
  maintenance: true,
  system: false,
}

const intKeyHint = (key, v) => {
  switch (key) {
    case 'pagination.default_page_size':
      return `列表接口未传「每页条数」时，默认每次加载 ${v} 条`
    case 'alert.rule_default_days':
      return `预警规则里没写统计天数时，按「最近 ${v} 天」来算`
    case 'alert.rule_default_threshold':
      return `预警规则里没写次数门槛时，默认至少 ${v} 次才触发`
    case 'alert.metric_default_window_days':
      return `温度/润滑类预警在提示文字里，默认用「${v} 天」作为统计窗口描述`
    case 'maintenance.reminder_calendar_interval_days':
      return `按月固定日保养时，在提醒上展示的「间隔天数」约为 ${v} 天`
    case 'maintenance.reminder_days_before_due':
      return `按「下次保养日期」提醒：距离到期日不超过 ${v} 天时会触发（填 0 表示仅到期当天及已过期时触发）`
    case 'maintenance.reminder_remaining_usage_threshold':
      return `按「使用模次」保养时，剩余模次 ≤ ${v} 时开始提醒`
    case 'minio.presign_expire_days':
      return `附件预览/下载链接大约在 ${v} 天内有效，过期需重新打开页面获取新链接`
    default:
      return `当前数值：${v}`
  }
}

const humanHint = (row) => {
  const v = row.draftValue
  const key = row.configKey
  const t = String(row.valueType || '').toUpperCase()
  if (t === 'CRON') {
    return describeSpringCron(v)
  }
  if (t === 'LONG' && key === 'jwt.expiration_ms') {
    const min = row._jwtMinutes
    if (Number.isFinite(min) && min > 0) {
      const ms = min * 60000
      return `登录后令牌在 ${min} 分钟内有效；约合 ${(ms / 3600000).toFixed(2)} 小时、${(ms / 86400000).toFixed(2)} 天`
    }
    const n = Number(v)
    if (!Number.isFinite(n) || n <= 0) {
      return '请填写有效时长（分钟），或展开高级填写毫秒'
    }
    const m2 = Math.max(1, Math.round(n / 60000))
    return `登录后令牌约 ${m2} 分钟内有效（按当前毫秒值换算）`
  }
  if (t === 'INT') {
    return intKeyHint(key, v)
  }
  if (t === 'STRING' && key === 'websocket.allowed_origin_patterns') {
    const s = (v || '').trim()
    if (!s || s === '*') {
      return '允许任意来源连接实时消息（开发方便；生产建议改为具体前端域名，英文逗号分隔）'
    }
    return `仅允许这些来源连接实时消息：${s}`
  }
  if (t === 'STRING') {
    return v ? `当前内容：${v}` : '（空）'
  }
  return '—'
}

const loading = ref(true)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const editableRows = ref([])

const snapshotFromRows = (rows) =>
  JSON.stringify(
    [...rows]
      .map((r) => ({ k: r.configKey, v: r.draftValue == null ? '' : String(r.draftValue) }))
      .sort((a, b) => a.k.localeCompare(b.k)),
  )

const baselineSnapshot = ref('')
const baselineMap = ref({})
const expandedGroups = ref({})

const isDirty = computed(() => {
  if (loading.value) return false
  const rows = editableRows.value
  if (!rows.length) return false
  return snapshotFromRows(rows) !== baselineSnapshot.value
})

const isRowDirty = (row) => normalizeDraft(row.draftValue) !== normalizeDraft(baselineMap.value[row.configKey])

const groupedRows = computed(() =>
  GROUP_DEFS.map((g) => ({
    ...g,
    rows: editableRows.value.filter((row) => groupKeyByConfig(row.configKey) === g.key),
  })).filter((g) => g.rows.length > 0),
)

const totalCount = computed(() => editableRows.value.length)
const cronCount = computed(() => editableRows.value.filter((x) => isCronType(x)).length)
const modifiedCount = computed(() => editableRows.value.filter((x) => isRowDirty(x)).length)

const initExpandedGroups = () => {
  const current = expandedGroups.value
  expandedGroups.value = groupedRows.value.reduce((acc, g) => {
    acc[g.key] = Object.prototype.hasOwnProperty.call(current, g.key)
      ? current[g.key]
      : DEFAULT_EXPANDED_GROUPS[g.key] !== false
    return acc
  }, {})
}

const isGroupExpanded = (key) => expandedGroups.value[key] !== false

const toggleGroup = (key) => {
  expandedGroups.value = {
    ...expandedGroups.value,
    [key]: !isGroupExpanded(key),
  }
}

const LEAVE_UNSAVED_MSG = '有未保存的修改，确定要离开吗？'
const RELOAD_DISCARD_MSG = '有未保存的修改，重新加载将丢弃更改，是否继续？'

const showUnsavedConfirmDialog = ref(false)
const unsavedConfirmMessage = ref('')
const unsavedConfirmOkText = ref('确认')
const pendingUnsavedResolve = ref(null)

const askUnsavedConfirm = (message, okText = '确认') =>
  new Promise((resolve) => {
    if (pendingUnsavedResolve.value) {
      pendingUnsavedResolve.value(false)
      pendingUnsavedResolve.value = null
    }
    unsavedConfirmMessage.value = message
    unsavedConfirmOkText.value = okText
    showUnsavedConfirmDialog.value = true
    pendingUnsavedResolve.value = resolve
  })

const closeUnsavedConfirmDialog = (result) => {
  if (pendingUnsavedResolve.value) {
    pendingUnsavedResolve.value(result)
    pendingUnsavedResolve.value = null
  }
  showUnsavedConfirmDialog.value = false
}

const handleUnsavedConfirmCancel = () => closeUnsavedConfirmDialog(false)
const handleUnsavedConfirmOk = () => closeUnsavedConfirmDialog(true)

const handleBeforeUnload = (e) => {
  if (!isDirty.value) return
  e.preventDefault()
  e.returnValue = ''
}

const loadList = async (opts = {}) => {
  const skipReloadConfirm = opts.skipReloadConfirm === true
  if (!skipReloadConfirm && isDirty.value) {
    const ok = await askUnsavedConfirm(RELOAD_DISCARD_MSG, '继续重载')
    if (!ok) return
  }
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const res = await fetchBusinessConfigList()
    const list = res.data || []
    editableRows.value = list.map((r) => decorateEditableRow(r))
    baselineMap.value = Object.fromEntries(
      editableRows.value.map((r) => [r.configKey, normalizeDraft(r.draftValue)]),
    )
    baselineSnapshot.value = snapshotFromRows(editableRows.value)
    initExpandedGroups()
  } catch (e) {
    errorMessage.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const onSave = async () => {
  saving.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const items = editableRows.value.map((r) => ({
      configKey: r.configKey,
      configValue: r.draftValue == null ? '' : String(r.draftValue),
    }))
    const res = await saveBusinessConfigBatch(items)
    if (res.code === 200) {
      successMessage.value = '已保存'
      await loadList({ skipReloadConfirm: true })
    } else {
      errorMessage.value = res.message || '保存失败'
    }
  } catch (e) {
    errorMessage.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

onBeforeRouteLeave(async () => {
  if (!isDirty.value) return
  const ok = await askUnsavedConfirm(LEAVE_UNSAVED_MSG, '离开页面')
  if (!ok) return false
})

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  loadList()
})

onUnmounted(() => {
  closeUnsavedConfirmDialog(false)
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
.bcc-page {
  display: flex;
  min-height: 100vh;
  background: #f1f5f9;
}
.bcc-main {
  flex: 1;
  padding: 24px 28px;
  margin-left: 220px;
  max-width: 1100px;
}
.bcc-header h1 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #0f172a;
}
.bcc-sub {
  margin: 0 0 20px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}
.bcc-error {
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #fee2e2;
  border: 1px solid #fecaca;
  color: #991b1b;
  border-radius: 8px;
}
.bcc-success {
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #dcfce7;
  border: 1px solid #bbf7d0;
  color: #166534;
  border-radius: 8px;
}
.bcc-loading {
  color: #64748b;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bcc-skeleton-wrap {
  display: grid;
  grid-template-columns: repeat(2, minmax(180px, 1fr));
  gap: 12px;
}
.bcc-skeleton-card {
  height: 72px;
}
.bcc-skeleton-row {
  height: 86px;
  grid-column: 1 / -1;
}

.bcc-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.summary-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #dbeafe;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.05);
}

.summary-title {
  color: #64748b;
  font-size: 12px;
  margin-bottom: 4px;
}

.summary-value {
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.1;
}

.summary-value-warn {
  color: #c2410c;
}

.group-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
  margin-bottom: 14px;
  overflow: hidden;
}

.group-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
}

.group-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}

.group-header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.group-count {
  color: #334155;
  font-size: 12px;
  background: #e2e8f0;
  border-radius: 999px;
  padding: 4px 8px;
}

.group-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-toggle-btn {
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  background: #fff;
  color: #334155;
  font-size: 12px;
  padding: 4px 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.group-toggle-btn:hover {
  background: #f8fafc;
  border-color: #94a3b8;
}

.group-body {
  padding: 12px 14px;
}

.config-item {
  display: grid;
  grid-template-columns: minmax(280px, 1.3fr) minmax(340px, 1fr);
  gap: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
}

.config-item + .config-item {
  margin-top: 10px;
}

.config-item.dirty {
  border-color: #f59e0b;
  box-shadow: 0 0 0 1px rgba(245, 158, 11, 0.25);
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.dirty-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  background: #ffedd5;
  color: #c2410c;
  font-size: 11px;
  font-weight: 600;
}
.td-label {
  font-weight: 600;
  color: #1e293b;
}
.td-desc {
  margin: 0 0 6px;
  color: #64748b;
  line-height: 1.45;
}
.type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  background: #e0e7ff;
  color: #3730a3;
  font-size: 11px;
  font-weight: 600;
}
.td-human {
  margin: 0;
  color: #0f172a;
  font-size: 13px;
  line-height: 1.55;
}
.td-tech {
  min-width: 0;
}
.jwt-min-ui {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: stretch;
}
.jwt-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.jwt-min-input {
  width: 140px;
  max-width: 100%;
  min-width: 0;
}
.jwt-unit {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
}
.jwt-hint-line {
  margin: 0;
  font-size: 11px;
  color: #94a3b8;
  line-height: 1.45;
}
.field-hint {
  margin: 6px 0 0;
  font-size: 11px;
  color: #94a3b8;
  line-height: 1.4;
}
.bcc-input {
  width: 100%;
  min-width: 240px;
  padding: 8px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 13px;
}
.bcc-input-mono {
  font-family: ui-monospace, 'Cascadia Code', 'Consolas', monospace;
  font-size: 12px;
}
.cron-ui {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: stretch;
}
.cron-field-label {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}
.bcc-select {
  width: 100%;
  max-width: 360px;
  padding: 9px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
  cursor: pointer;
}
.cron-advanced-toggle {
  align-self: flex-start;
  padding: 0;
  border: none;
  background: none;
  color: #2563eb;
  font-size: 13px;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}
.cron-advanced-toggle:hover {
  color: #1d4ed8;
}
.cron-advanced-panel {
  margin-top: 4px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
}
.cron-advanced-label {
  display: block;
  font-size: 11px;
  color: #64748b;
  margin-bottom: 6px;
  line-height: 1.4;
}
.bcc-actions {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
}

.bcc-sticky-actions {
  position: sticky;
  bottom: 12px;
  margin-top: 12px;
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: rgba(248, 250, 252, 0.95);
  backdrop-filter: blur(6px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.sticky-tip {
  color: #334155;
  font-size: 13px;
}

.sticky-buttons {
  display: flex;
  gap: 10px;
}
.btn {
  padding: 8px 18px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn.primary {
  background: linear-gradient(135deg, #1e40af 0%, #2563eb 100%);
  color: #fff;
}
.btn.ghost {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #334155;
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
  padding: 16px;
}

.confirm-dialog {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 430px;
  padding: 28px 24px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.24);
  text-align: center;
  border: 1px solid #e2e8f0;
}

.confirm-icon {
  width: 62px;
  height: 62px;
  margin: 0 auto 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.confirm-icon.warning {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.confirm-title {
  margin: 0 0 8px;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
}

.confirm-message {
  margin: 0 0 22px;
  color: #475569;
  line-height: 1.6;
  font-size: 14px;
}

.confirm-actions {
  display: flex;
  gap: 12px;
}

.confirm-cancel-btn,
.confirm-ok-btn {
  flex: 1;
  border: none;
  border-radius: 10px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.confirm-cancel-btn {
  background: #fff;
  color: #334155;
  border: 1px solid #cbd5e1;
}

.confirm-cancel-btn:hover {
  background: #f8fafc;
}

.confirm-ok-btn.warning {
  color: #fff;
  background: linear-gradient(135deg, #1e40af 0%, #2563eb 100%);
}

.confirm-ok-btn.warning:hover {
  filter: brightness(1.05);
}
@media (max-width: 1100px) {
  .bcc-main {
    margin-left: 0;
    max-width: none;
  }
  .config-item {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .bcc-summary-grid {
    grid-template-columns: 1fr;
  }
  .bcc-sticky-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .sticky-buttons {
    width: 100%;
  }
  .sticky-buttons .btn {
    flex: 1;
  }
  .bcc-skeleton-wrap {
    grid-template-columns: 1fr;
  }
}
</style>
