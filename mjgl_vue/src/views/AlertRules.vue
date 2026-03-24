<template>
  <div class="alert-rules-container">
    <AppSidebar />

    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">预警规则管理</div>
        <div class="top-subtitle">定制智能预警规则：统计维度、天数、阈值与启用状态</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section>
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">规则列表</h3>
                <div class="header-actions">
                  <button
                    type="button"
                    class="secondary-btn delete-btn"
                    :disabled="batchDeleting || selectedIds.length === 0"
                    @click="handleBatchDelete"
                  >
                    {{ batchDeleting ? '删除中...' : `批量删除 (${selectedIds.length})` }}
                  </button>
                  <button type="button" class="secondary-btn" :disabled="initLoading" @click="handleInitDefaults">
                    {{ initLoading ? '初始化中...' : '初始化默认规则' }}
                  </button>
                  <button type="button" class="primary-btn" @click="openEdit(null)">
                    新增规则
                  </button>
                </div>
              </div>
              <div class="card-body">
                <div v-if="successMessage" class="success-message">
                  <span class="message-icon">✓</span>
                  {{ successMessage }}
                </div>
                <div v-if="errorMessage" class="error-message">
                  <span class="message-icon">⚠</span>
                  {{ errorMessage }}
                </div>

                <div v-if="listLoading" class="table-loading">加载中...</div>
                <div v-else>
                  <div class="table-wrapper">
                    <table class="mold-table">
                      <thead>
                        <tr>
                          <th class="select-col">
                            <input
                              type="checkbox"
                              :checked="isAllPageSelected()"
                              @change="toggleSelectAllPage($event.target.checked)"
                              title="全选本页"
                            />
                          </th>
                          <th>规则编码</th>
                          <th>规则名称</th>
                          <th>数据来源</th>
                          <th>条件摘要</th>
                          <th>启用</th>
                          <th>排序</th>
                          <th>操作</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="!rules.length">
                          <td colspan="8" class="empty-cell">暂无规则，可点击「初始化默认规则」或「新增规则」</td>
                        </tr>
                        <tr v-for="row in rules" :key="row.id">
                          <td class="select-col">
                            <input
                              type="checkbox"
                              :checked="isSelected(row.id)"
                              @change="toggleRow(row.id)"
                            />
                          </td>
                          <td>{{ row.code }}</td>
                          <td>{{ row.name }}</td>
                          <td>{{ sourceLabel(row.source) }}</td>
                          <td>{{ conditionSummary(row) }}</td>
                          <td>
                            <span :class="row.enabled === 1 ? 'status-normal' : 'status-ignored'">
                              {{ row.enabled === 1 ? '启用' : '禁用' }}
                            </span>
                          </td>
                          <td>{{ row.sortOrder ?? 0 }}</td>
                          <td>
                            <div class="action-buttons">
                              <button
                                class="action-btn"
                                :disabled="rowLoadingId === row.id"
                                @click="openEdit(row)"
                              >
                                编辑
                              </button>
                              <button
                                v-if="row.enabled === 1"
                                class="action-btn"
                                :disabled="rowLoadingId === row.id"
                                @click="handleSetEnabled(row, 0)"
                              >
                                禁用
                              </button>
                              <button
                                v-else
                                class="action-btn status-btn"
                                :disabled="rowLoadingId === row.id"
                                @click="handleSetEnabled(row, 1)"
                              >
                                启用
                              </button>
                              <button
                                class="action-btn delete-btn"
                                :disabled="rowLoadingId === row.id"
                                @click="handleDelete(row)"
                              >
                                删除
                              </button>
                            </div>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>

    <!-- 新增/编辑 弹窗 -->
    <div v-if="showEditDialog" class="dialog-overlay" @click="closeEdit">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>{{ editId ? '编辑规则' : '新增规则' }}</h3>
          <button class="dialog-close" @click="closeEdit">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>规则编码 <span class="required">*</span></label>
            <input
              v-model="form.code"
              type="text"
              class="form-input"
              placeholder="如 RECENT_30D_FAULT_GE_3"
              :readonly="!!editId"
            />
            <span v-if="editId" class="form-hint">编码不可修改</span>
          </div>
          <div class="form-group">
            <label>规则名称 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              class="form-input"
              placeholder="如：近30天故障≥3次"
            />
          </div>
          <div class="form-group">
            <label>触发条件描述（可选，默认同名称）</label>
            <input
              v-model="form.description"
              type="text"
              class="form-input"
              placeholder="写入报警记录的触发条件说明"
            />
          </div>
          <div class="form-group">
            <label>数据来源 <span class="required">*</span></label>
            <select v-model="form.source" class="form-input">
              <option value="repair">维修记录</option>
              <option value="abnormal">异常记录</option>
              <option value="temperature">温度记录</option>
              <option value="lubrication">润滑记录</option>
            </select>
          </div>
          <!-- repair / abnormal：统计天数 + 次数阈值 -->
          <div v-if="form.source === 'repair' || form.source === 'abnormal'" class="form-row">
            <div class="form-group">
              <label>统计天数 <span class="required">*</span></label>
              <input
                v-model.number="form.days"
                type="number"
                min="1"
                max="365"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label>次数阈值 <span class="required">*</span></label>
              <input
                v-model.number="form.threshold"
                type="number"
                min="1"
                max="1000"
                class="form-input"
              />
            </div>
          </div>
          <!-- temperature：触发方式 + 统计天数 + 温度阈值 + 比较方式 + (聚合方式 或 次数阈值) -->
          <template v-if="form.source === 'temperature'">
            <div class="form-group">
              <label>触发方式 <span class="required">*</span></label>
              <select v-model="form.triggerMode" class="form-input">
                <option value="value">按聚合值（最大/平均温度满足阈值）</option>
                <option value="count">按次数（近N天内有K次超过/低于阈值）</option>
              </select>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>统计天数 <span class="required">*</span></label>
                <input
                  v-model.number="form.timeWindowMinutes"
                  type="number"
                  min="1"
                  max="365"
                  class="form-input"
                  placeholder="如 7"
                />
              </div>
              <div class="form-group">
                <label>温度阈值(℃) <span class="required">*</span></label>
                <input
                  v-model.number="form.valueThreshold"
                  type="number"
                  step="0.1"
                  class="form-input"
                  placeholder="如 80"
                />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>比较方式 <span class="required">*</span></label>
                <select v-model="form.compareOp" class="form-input">
                  <option value="gt">大于 (>)</option>
                  <option value="ge">大于等于 (≥)</option>
                  <option value="lt">小于 (&lt;)</option>
                  <option value="le">小于等于 (≤)</option>
                </select>
              </div>
              <div v-if="form.triggerMode === 'value'" class="form-group">
                <label>聚合方式 <span class="required">*</span></label>
                <select v-model="form.metricType" class="form-input">
                  <option value="max">最大值</option>
                  <option value="avg">平均值</option>
                </select>
              </div>
              <div v-if="form.triggerMode === 'count'" class="form-group">
                <label>次数阈值（至少几次） <span class="required">*</span></label>
                <input
                  v-model.number="form.threshold"
                  type="number"
                  min="1"
                  max="10000"
                  class="form-input"
                  placeholder="如 3"
                />
              </div>
            </div>
          </template>
          <!-- lubrication：触发方式 + 统计天数 + 指标 + 数值阈值 + 比较方式 + (聚合方式 或 次数阈值) -->
          <template v-if="form.source === 'lubrication'">
            <div class="form-group">
              <label>触发方式 <span class="required">*</span></label>
              <select v-model="form.triggerMode" class="form-input">
                <option value="value">按聚合值（最大/最小/平均满足阈值）</option>
                <option value="count">按次数（近N天内有K次超过/低于阈值）</option>
              </select>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>统计天数 <span class="required">*</span></label>
                <input
                  v-model.number="form.timeWindowMinutes"
                  type="number"
                  min="1"
                  max="365"
                  class="form-input"
                  placeholder="如 7"
                />
              </div>
              <div class="form-group">
                <label>润滑指标 <span class="required">*</span></label>
                <select v-model="form.metricField" class="form-input">
                  <option value="oil_level_percent">液位百分比(%)</option>
                  <option value="pressure_kpa">压力(kPa)</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>数值阈值 <span class="required">*</span></label>
                <input
                  v-model.number="form.valueThreshold"
                  type="number"
                  step="0.01"
                  class="form-input"
                  placeholder="液位0-100 或 压力值"
                />
              </div>
              <div class="form-group">
                <label>比较方式 <span class="required">*</span></label>
                <select v-model="form.compareOp" class="form-input">
                  <option value="gt">大于 (>)</option>
                  <option value="ge">大于等于 (≥)</option>
                  <option value="lt">小于 (&lt;)</option>
                  <option value="le">小于等于 (≤)</option>
                </select>
              </div>
            </div>
            <div v-if="form.triggerMode === 'value'" class="form-group">
              <label>聚合方式 <span class="required">*</span></label>
              <select v-model="form.metricType" class="form-input">
                <option value="max">最大值</option>
                <option value="min">最小值</option>
                <option value="avg">平均值</option>
              </select>
            </div>
            <div v-if="form.triggerMode === 'count'" class="form-group">
              <label>次数阈值（至少几次） <span class="required">*</span></label>
              <input
                v-model.number="form.threshold"
                type="number"
                min="1"
                max="10000"
                class="form-input"
                placeholder="如 3"
              />
            </div>
          </template>
          <div class="form-row">
            <div class="form-group">
              <label>启用</label>
              <select v-model.number="form.enabled" class="form-input">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </div>
            <div class="form-group">
              <label>排序（越小越先执行）</label>
              <input
                v-model.number="form.sortOrder"
                type="number"
                min="0"
                class="form-input"
              />
            </div>
          </div>
          <div class="dialog-footer">
            <button type="button" class="secondary-btn" @click="closeEdit">取消</button>
            <button type="button" class="primary-btn" :disabled="saveLoading" @click="submitSave">
              {{ saveLoading ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import AppSidebar from '@/components/AppSidebar.vue'
import { useTableMultiSelect } from '@/composables/useTableMultiSelect'
import {
  listAlertRules,
  saveAlertRule,
  deleteAlertRule,
  batchDeleteAlertRules,
  setAlertRuleEnabled,
  initDefaultAlertRules,
} from '@/api/alertRules'

const successMessage = ref('')
const errorMessage = ref('')
const listLoading = ref(false)
const initLoading = ref(false)
const saveLoading = ref(false)
const rowLoadingId = ref(null)
const batchDeleting = ref(false)
const rules = ref([])
const { selectedIds, isSelected, toggleRow, isAllPageSelected, toggleSelectAllPage, clearSelection } =
  useTableMultiSelect(() => rules.value)
const showEditDialog = ref(false)
const editId = ref(null)

const form = reactive({
  code: '',
  name: '',
  description: '',
  source: 'repair',
  days: 30,
  threshold: 3,
  timeWindowMinutes: 7,
  valueThreshold: null,
  compareOp: 'gt',
  metricType: 'max',
  metricField: 'oil_level_percent',
  triggerMode: 'value',
  enabled: 1,
  sortOrder: 0,
})

function sourceLabel(v) {
  const map = {
    repair: '维修记录',
    abnormal: '异常记录',
    temperature: '温度记录',
    lubrication: '润滑记录',
  }
  return map[v] ?? v
}

function conditionSummary(row) {
  if (row.source === 'repair' || row.source === 'abnormal') {
    return `近${row.days ?? '-'}天 ≥${row.threshold ?? '-'}次`
  }
  if (row.source === 'temperature') {
    const d = row.timeWindowMinutes ?? '-'
    const op = { gt: '>', ge: '≥', lt: '<', le: '≤' }[row.compareOp] ?? ''
    if (row.triggerMode === 'count') {
      return `近${d}天 ≥${row.threshold ?? '-'}次 温度${op}${row.valueThreshold ?? ''}℃`
    }
    const agg = { max: '最大', avg: '平均' }[row.metricType] ?? ''
    return `近${d}天 ${agg}温度${op}${row.valueThreshold ?? ''}℃`
  }
  if (row.source === 'lubrication') {
    const d = row.timeWindowMinutes ?? '-'
    const field = row.metricField === 'oil_level_percent' ? '液位' : '压力'
    const op = { gt: '>', ge: '≥', lt: '<', le: '≤' }[row.compareOp] ?? ''
    if (row.triggerMode === 'count') {
      return `近${d}天 ≥${row.threshold ?? '-'}次 ${field}${op}${row.valueThreshold ?? ''}`
    }
    const agg = { max: '最大', min: '最小', avg: '平均' }[row.metricType] ?? ''
    return `近${d}天 ${agg}${field}${op}${row.valueThreshold ?? ''}`
  }
  return '-'
}

function showMsg(success, msg) {
  if (success) {
    successMessage.value = msg
    errorMessage.value = ''
  } else {
    errorMessage.value = msg
    successMessage.value = ''
  }
  setTimeout(() => {
    successMessage.value = ''
    errorMessage.value = ''
  }, 3000)
}

async function loadList() {
  listLoading.value = true
  try {
    const res = await listAlertRules()
    rules.value = res?.data ?? []
  } catch (e) {
    showMsg(false, e?.message || '加载规则列表失败')
    rules.value = []
  } finally {
    listLoading.value = false
  }
}

function openEdit(row) {
  editId.value = row ? row.id : null
  if (row) {
    form.code = row.code
    form.name = row.name
    form.description = row.description ?? ''
    form.source = row.source ?? 'repair'
    form.days = row.days ?? 30
    form.threshold = row.threshold ?? 3
    form.timeWindowMinutes = row.timeWindowMinutes ?? 7
    form.valueThreshold = row.valueThreshold != null ? row.valueThreshold : null
    form.compareOp = row.compareOp ?? 'gt'
    form.metricType = row.metricType ?? 'max'
    form.metricField = row.metricField ?? 'oil_level_percent'
    form.triggerMode = row.triggerMode ?? 'value'
    form.enabled = row.enabled ?? 1
    form.sortOrder = row.sortOrder ?? 0
  } else {
    form.code = ''
    form.name = ''
    form.description = ''
    form.source = 'repair'
    form.days = 30
    form.threshold = 3
    form.timeWindowMinutes = 7
    form.valueThreshold = null
    form.compareOp = 'gt'
    form.metricType = 'max'
    form.metricField = 'oil_level_percent'
    form.triggerMode = 'value'
    form.enabled = 1
    form.sortOrder = 0
  }
  showEditDialog.value = true
}

function closeEdit() {
  showEditDialog.value = false
  editId.value = null
}

async function submitSave() {
  if (!form.code?.trim()) {
    showMsg(false, '请输入规则编码')
    return
  }
  if (!form.name?.trim()) {
    showMsg(false, '请输入规则名称')
    return
  }
  if (form.source === 'repair' || form.source === 'abnormal') {
    if (!form.days || form.days < 1 || form.days > 365) {
      showMsg(false, '统计天数需在 1～365 之间')
      return
    }
    if (!form.threshold || form.threshold < 1 || form.threshold > 1000) {
      showMsg(false, '次数阈值需在 1～1000 之间')
      return
    }
  }
  if (form.source === 'temperature') {
    if (!form.timeWindowMinutes || form.timeWindowMinutes < 1 || form.timeWindowMinutes > 365) {
      showMsg(false, '请填写统计天数(1～365)')
      return
    }
    if (form.valueThreshold == null || form.valueThreshold === '') {
      showMsg(false, '请填写温度阈值')
      return
    }
    if (!form.compareOp) {
      showMsg(false, '请选择比较方式')
      return
    }
    if (form.triggerMode === 'value' && !form.metricType) {
      showMsg(false, '请选择聚合方式')
      return
    }
    if (form.triggerMode === 'count' && (!form.threshold || form.threshold < 1)) {
      showMsg(false, '按次数触发时请填写次数阈值(至少1)')
      return
    }
  }
  if (form.source === 'lubrication') {
    if (!form.timeWindowMinutes || form.timeWindowMinutes < 1 || form.timeWindowMinutes > 365) {
      showMsg(false, '请填写统计天数(1～365)')
      return
    }
    if (form.valueThreshold == null || form.valueThreshold === '') {
      showMsg(false, '请填写数值阈值')
      return
    }
    if (!form.compareOp || !form.metricField) {
      showMsg(false, '请选择比较方式和润滑指标')
      return
    }
    if (form.triggerMode === 'value' && !form.metricType) {
      showMsg(false, '请选择聚合方式')
      return
    }
    if (form.triggerMode === 'count' && (!form.threshold || form.threshold < 1)) {
      showMsg(false, '按次数触发时请填写次数阈值(至少1)')
      return
    }
  }
  saveLoading.value = true
  try {
    const payload = {
      code: form.code.trim(),
      name: form.name.trim(),
      description: form.description?.trim() || undefined,
      source: form.source,
      days: form.source === 'repair' || form.source === 'abnormal' ? form.days : undefined,
      threshold: form.source === 'repair' || form.source === 'abnormal' ? form.threshold : undefined,
      timeWindowMinutes: form.source === 'temperature' || form.source === 'lubrication' ? form.timeWindowMinutes : undefined,
      valueThreshold: form.source === 'temperature' || form.source === 'lubrication' ? form.valueThreshold : undefined,
      compareOp: form.source === 'temperature' || form.source === 'lubrication' ? form.compareOp : undefined,
      metricType: (form.source === 'temperature' || form.source === 'lubrication') && form.triggerMode === 'value' ? form.metricType : undefined,
      metricField: form.source === 'lubrication' ? form.metricField : undefined,
      triggerMode: form.source === 'temperature' || form.source === 'lubrication' ? (form.triggerMode || 'value') : undefined,
      threshold: (form.source === 'temperature' || form.source === 'lubrication') && form.triggerMode === 'count' ? form.threshold : (form.source === 'repair' || form.source === 'abnormal' ? form.threshold : undefined),
      enabled: form.enabled,
      sortOrder: form.sortOrder ?? 0,
    }
    if (editId.value) payload.id = editId.value
    await saveAlertRule(payload)
    showMsg(true, editId.value ? '规则已更新' : '规则已新增')
    closeEdit()
    loadList()
  } catch (e) {
    showMsg(false, e?.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function handleSetEnabled(row, enabled) {
  rowLoadingId.value = row.id
  try {
    await setAlertRuleEnabled(row.id, enabled)
    showMsg(true, enabled === 1 ? '已启用' : '已禁用')
    loadList()
  } catch (e) {
    showMsg(false, e?.message || '操作失败')
  } finally {
    rowLoadingId.value = null
  }
}

async function handleDelete(row) {
  if (!confirm(`确定删除规则「${row.name}」吗？`)) return
  rowLoadingId.value = row.id
  try {
    await deleteAlertRule(row.id)
    showMsg(true, '已删除')
    clearSelection()
    loadList()
  } catch (e) {
    showMsg(false, e?.message || '删除失败')
  } finally {
    rowLoadingId.value = null
  }
}

async function handleBatchDelete() {
  const ids = [...selectedIds.value]
  if (!ids.length) return
  if (!confirm(`确定批量删除选中的 ${ids.length} 条规则吗？`)) return
  batchDeleting.value = true
  try {
    await batchDeleteAlertRules(ids)
    showMsg(true, '批量删除成功')
    clearSelection()
    loadList()
  } catch (e) {
    showMsg(false, e?.message || '批量删除失败')
  } finally {
    batchDeleting.value = false
  }
}

async function handleInitDefaults() {
  initLoading.value = true
  try {
    const res = await initDefaultAlertRules()
    const added = res?.data ?? 0
    showMsg(true, added > 0 ? `已初始化 ${added} 条默认规则` : '默认规则已存在，无需重复初始化')
    loadList()
  } catch (e) {
    showMsg(false, e?.message || '初始化失败')
  } finally {
    initLoading.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.select-col {
  width: 40px;
  text-align: center;
}

.alert-rules-container {
  min-height: 100vh;
  display: flex;
  background: #f3f4f6;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.top-header {
  padding: 20px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.top-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.top-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.main-content {
  flex: 1;
  padding: 20px 24px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.card-body {
  padding: 20px;
}

.success-message,
.error-message {
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.success-message {
  background: #d1fae5;
  color: #065f46;
}

.error-message {
  background: #fee2e2;
  color: #991b1b;
}

.table-loading {
  padding: 24px;
  text-align: center;
  color: #6b7280;
}

.table-wrapper {
  overflow-x: auto;
}

.mold-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.mold-table th,
.mold-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.mold-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.mold-table tbody tr:hover {
  background: #f9fafb;
}

.empty-cell {
  color: #9ca3af;
  text-align: center;
  padding: 24px;
}

.status-normal {
  color: #059669;
}

.status-ignored {
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 4px 10px;
  font-size: 13px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  background: #e5e7eb;
  color: #374151;
}

.action-btn.status-btn {
  background: #d1fae5;
  color: #065f46;
}

.action-btn.delete-btn {
  background: #fee2e2;
  color: #991b1b;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.primary-btn,
.secondary-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.primary-btn {
  background: #2563eb;
  color: #fff;
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.secondary-btn {
  background: #fff;
  color: #374151;
  border: 1px solid #d1d5db;
}

.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background: #fff;
  border-radius: 8px;
  min-width: 420px;
  max-width: 90vw;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  line-height: 1;
}

.dialog-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row .form-group {
  flex: 1;
}

.form-group label {
  display: block;
  font-size: 13px;
  color: #374151;
  margin-bottom: 6px;
}

.required {
  color: #dc2626;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.form-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
