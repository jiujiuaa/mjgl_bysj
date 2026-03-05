<template>
  <div class="dashboard-container">
    <AppSidebar />

    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">监测数据展示</div>
        <div class="top-subtitle">模具温度、润滑、运维与异常数据可视化</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <!-- 标签页 -->
          <div class="tabs">
            <button
              v-for="t in tabs"
              :key="t.key"
              class="tab-btn"
              :class="{ active: activeTab === t.key }"
              @click="activeTab = t.key"
            >
              {{ t.label }}
            </button>
          </div>

          <!-- 综合仪表盘 -->
          <section v-show="activeTab === 'overview'" class="chart-section">
            <div class="stats-cards">
              <div class="stat-card">
                <span class="stat-icon">🧱</span>
                <div class="stat-content">
                  <span class="stat-value">{{ stats.moldTotal }}</span>
                  <span class="stat-label">模具总数</span>
                </div>
              </div>
              <div class="stat-card">
                <span class="stat-icon">📊</span>
                <div class="stat-content">
                  <span class="stat-value">{{ stats.inUse }}</span>
                  <span class="stat-label">使用中</span>
                </div>
              </div>
              <div class="stat-card">
                <span class="stat-icon">🩺</span>
                <div class="stat-content">
                  <span class="stat-value">{{ stats.pendingRepair }}</span>
                  <span class="stat-label">待处理维修</span>
                </div>
              </div>
              <div class="stat-card">
                <span class="stat-icon">⏰</span>
                <div class="stat-content">
                  <span class="stat-value">{{ stats.pendingReminder }}</span>
                  <span class="stat-label">待处理保养提醒</span>
                </div>
              </div>
              <div class="stat-card">
                <span class="stat-icon">⚠</span>
                <div class="stat-content">
                  <span class="stat-value">{{ stats.abnormalCount }}</span>
                  <span class="stat-label">异常记录</span>
                </div>
              </div>
            </div>

            <div class="chart-row">
              <div class="chart-card half">
                <h3 class="chart-title">模具状态分布</h3>
                <div ref="moldStatusChartRef" class="chart-dom"></div>
              </div>
              <div class="chart-card half">
                <h3 class="chart-title">异常类型分布</h3>
                <div ref="abnormalTypeChartRef" class="chart-dom"></div>
              </div>
            </div>
          </section>

          <!-- 监测趋势：按单模具查看，无“全部” -->
          <section v-show="activeTab === 'monitoring'" class="chart-section">
            <div class="filter-bar">
              <div class="filter-item">
                <label>模具 <span class="required">*</span></label>
                <select v-model="monitoringFilter.moldId" class="form-input" required>
                  <option value="">请选择模具</option>
                  <option v-for="m in molds" :key="m.id" :value="m.id">
                    {{ m.moldCode || m.name || m.id }}
                  </option>
                </select>
              </div>
              <div class="filter-item">
                <label>开始日期</label>
                <input v-model="monitoringFilter.startDate" type="date" class="form-input" />
              </div>
              <div class="filter-item">
                <label>结束日期</label>
                <input v-model="monitoringFilter.endDate" type="date" class="form-input" />
              </div>
              <button class="primary-btn" :disabled="!monitoringFilter.moldId" @click="loadMonitoringCharts">查询</button>
            </div>

            <div class="chart-card">
              <h3 class="chart-title">温度巡检趋势</h3>
              <div ref="temperatureChartRef" class="chart-dom"></div>
            </div>
            <div class="chart-card">
              <h3 class="chart-title">润滑巡检趋势（液位 %）</h3>
              <div ref="lubricationChartRef" class="chart-dom"></div>
            </div>
            <div class="chart-card">
              <h3 class="chart-title">润滑巡检趋势（压力 kPa）</h3>
              <div ref="pressureChartRef" class="chart-dom"></div>
            </div>
            <div class="chart-card">
              <h3 class="chart-title">异常数量趋势</h3>
              <div ref="abnormalTrendChartRef" class="chart-dom"></div>
            </div>
          </section>

          <!-- 运维统计 -->
          <section v-show="activeTab === 'ops'" class="chart-section">
            <div class="filter-bar">
              <div class="filter-item">
                <label>开始日期</label>
                <input v-model="opsFilter.startDate" type="date" class="form-input" />
              </div>
              <div class="filter-item">
                <label>结束日期</label>
                <input v-model="opsFilter.endDate" type="date" class="form-input" />
              </div>
              <button class="primary-btn" @click="loadOpsCharts">查询</button>
            </div>

            <div class="chart-row">
              <div class="chart-card half">
                <h3 class="chart-title">维修状态分布</h3>
                <div ref="repairStatusChartRef" class="chart-dom"></div>
              </div>
              <div class="chart-card half">
                <h3 class="chart-title">保养提醒状态分布</h3>
                <div ref="reminderStatusChartRef" class="chart-dom"></div>
              </div>
            </div>
            <div class="chart-row">
              <div class="chart-card half">
                <h3 class="chart-title">维修费用统计（按模具）</h3>
                <div ref="repairCostChartRef" class="chart-dom"></div>
              </div>
              <div class="chart-card half">
                <h3 class="chart-title">保养类型分布</h3>
                <div ref="maintenanceTypeChartRef" class="chart-dom"></div>
              </div>
            </div>
          </section>

          <!-- 模具概览 -->
          <section v-show="activeTab === 'mold'" class="chart-section">
            <div class="chart-row">
              <div class="chart-card half">
                <h3 class="chart-title">模具状态分布</h3>
                <div ref="moldStatusChart2Ref" class="chart-dom"></div>
              </div>
              <div class="chart-card half">
                <h3 class="chart-title">模具累计使用次数 TOP10</h3>
                <div ref="moldUsageChartRef" class="chart-dom"></div>
              </div>
            </div>
            <div class="chart-card">
              <h3 class="chart-title">模具累计维修/保养成本对比</h3>
              <div ref="moldCostChartRef" class="chart-dom"></div>
            </div>
          </section>

          <div v-if="loading" class="loading-overlay">数据加载中...</div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import AppSidebar from '@/components/AppSidebar.vue'
import {
  fetchTemperatureChartData,
  fetchLubricationChartData,
  fetchAbnormalChartData,
  fetchMoldsForChart,
  fetchRepairForChart,
  fetchMaintenanceForChart,
  fetchRemindersForChart,
} from '@/api/charts'

const activeTab = ref('overview')
const loading = ref(false)
const molds = ref([])

const tabs = [
  { key: 'overview', label: '综合仪表盘' },
  { key: 'monitoring', label: '监测趋势' },
  { key: 'ops', label: '运维统计' },
  { key: 'mold', label: '模具概览' },
]

const stats = reactive({
  moldTotal: 0,
  inUse: 0,
  pendingRepair: 0,
  pendingReminder: 0,
  abnormalCount: 0,
})

const monitoringFilter = reactive({
  moldId: '',
  startDate: '',
  endDate: '',
})

const opsFilter = reactive({
  startDate: '',
  endDate: '',
})

// chart refs
const moldStatusChartRef = ref(null)
const moldStatusChart2Ref = ref(null)
const abnormalTypeChartRef = ref(null)
const temperatureChartRef = ref(null)
const lubricationChartRef = ref(null)
const pressureChartRef = ref(null)
const abnormalTrendChartRef = ref(null)
const repairStatusChartRef = ref(null)
const reminderStatusChartRef = ref(null)
const repairCostChartRef = ref(null)
const maintenanceTypeChartRef = ref(null)
const moldUsageChartRef = ref(null)
const moldCostChartRef = ref(null)

const MOLD_STATUS_MAP = {
  1: '在库',
  2: '使用中',
  3: '维修中',
  4: '外借',
  5: '待报废',
}

const REPAIR_STATUS_MAP = {
  1: '待处理',
  2: '维修中',
  3: '已修复',
  4: '已验收',
}

const REMINDER_STATUS_MAP = {
  1: '待处理',
  2: '已提醒',
  3: '已完成',
  4: '已忽略',
}

const ABNORMAL_TYPE_MAP = {
  1: '温度异常',
  2: '润滑异常',
  3: '其它',
}

const formatDate = (val) => {
  if (!val) return ''
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const getDefaultDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  return {
    startDate: start.toISOString().slice(0, 10),
    endDate: end.toISOString().slice(0, 10),
  }
}

const initDateRange = () => {
  const { startDate, endDate } = getDefaultDateRange()
  monitoringFilter.startDate = startDate
  monitoringFilter.endDate = endDate
  opsFilter.startDate = startDate
  opsFilter.endDate = endDate
}

const loadMolds = async () => {
  try {
    const res = await fetchMoldsForChart(1, 500)
    molds.value = res.data?.list ?? []
    if (molds.value.length && !monitoringFilter.moldId) {
      monitoringFilter.moldId = molds.value[0].id
    }
  } catch (e) {
    console.error('加载模具列表失败', e)
  }
}

const loadOverviewData = async () => {
  loading.value = true
  try {
    const [moldsRes, repairRes, reminderRes, abnormalRes] = await Promise.all([
      fetchMoldsForChart(1, 500),
      fetchRepairForChart(),
      fetchRemindersForChart(),
      fetchAbnormalChartData({}),
    ])

    const moldList = moldsRes.data?.list ?? []
    const repairList = repairRes.data ?? []
    const reminderList = (reminderRes.data?.list ?? reminderRes.data) ?? []
    const abnormalList = abnormalRes.data?.list ?? abnormalRes.data ?? []

    stats.moldTotal = moldList.length
    stats.inUse = moldList.filter((m) => m.currentStatus === 2).length
    stats.pendingRepair = repairList.filter((r) => r.status === 1).length
    stats.pendingReminder = reminderList.filter((r) => r.status === 1).length
    stats.abnormalCount = Array.isArray(abnormalList) ? abnormalList.length : 0

    // 模具状态饼图
    const statusCount = {}
    moldList.forEach((m) => {
      const s = m.currentStatus ?? 0
      statusCount[s] = (statusCount[s] || 0) + 1
    })
    renderPie(moldStatusChartRef, Object.entries(statusCount).map(([k, v]) => ({
      name: MOLD_STATUS_MAP[k] || `状态${k}`,
      value: v,
    })), ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'])

    // 异常类型饼图
    const typeCount = {}
    ;(Array.isArray(abnormalList) ? abnormalList : []).forEach((a) => {
      const t = a.abnormalType ?? 3
      typeCount[t] = (typeCount[t] || 0) + 1
    })
    const abnormalData = Object.entries(typeCount).map(([k, v]) => ({
      name: ABNORMAL_TYPE_MAP[k] || `类型${k}`,
      value: v,
    }))
    if (abnormalData.length === 0) abnormalData.push({ name: '暂无数据', value: 1 })
    renderPie(abnormalTypeChartRef, abnormalData, ['#ee6666', '#fac858', '#73c0de'])
  } catch (e) {
    console.error('加载概览数据失败', e)
  } finally {
    loading.value = false
  }
}

const loadMonitoringCharts = async () => {
  if (!monitoringFilter.moldId) return
  loading.value = true
  try {
    const param = {
      moldId: monitoringFilter.moldId,
      startTime: monitoringFilter.startDate ? new Date(monitoringFilter.startDate) : null,
      endTime: monitoringFilter.endDate ? new Date(monitoringFilter.endDate + 'T23:59:59') : null,
    }

    const [tempRes, lubRes, abnormalRes] = await Promise.all([
      fetchTemperatureChartData(param),
      fetchLubricationChartData(param),
      fetchAbnormalChartData(param),
    ])

    const tempList = tempRes.data?.list ?? []
    const lubList = lubRes.data?.list ?? lubRes.data ?? []
    const abnormalList = abnormalRes.data?.list ?? abnormalRes.data ?? []

    // 温度折线
    const tempSorted = [...tempList].sort((a, b) => new Date(a.operationTime) - new Date(b.operationTime))
    renderLine(
      temperatureChartRef,
      tempSorted.map((x) => formatDate(x.operationTime)),
      [{ name: '温度(℃)', data: tempSorted.map((x) => x.temperature) }],
      '#ee6666'
    )

    // 润滑液位折线
    const lubSorted = [...lubList].sort((a, b) => new Date(a.operationTime) - new Date(b.operationTime))
    renderLine(
      lubricationChartRef,
      lubSorted.map((x) => formatDate(x.operationTime)),
      [{ name: '液位(%)', data: lubSorted.map((x) => x.oilLevelPercent) }],
      '#5470c6'
    )

    // 润滑压力折线
    renderLine(
      pressureChartRef,
      lubSorted.map((x) => formatDate(x.operationTime)),
      [{ name: '压力(kPa)', data: lubSorted.map((x) => x.pressureKpa ?? 0) }],
      '#91cc75'
    )

    // 异常数量趋势（按日聚合）
    const abnormalByDate = {}
    ;(Array.isArray(abnormalList) ? abnormalList : []).forEach((a) => {
      const d = a.occurredAt ? new Date(a.occurredAt).toISOString().slice(0, 10) : ''
      if (d) abnormalByDate[d] = (abnormalByDate[d] || 0) + 1
    })
    const dates = Object.keys(abnormalByDate).sort()
    renderLine(
      abnormalTrendChartRef,
      dates,
      [{ name: '异常数', data: dates.map((d) => abnormalByDate[d]) }],
      '#fac858'
    )
  } catch (e) {
    console.error('加载监测图表失败', e)
  } finally {
    loading.value = false
  }
}

const loadOpsCharts = async () => {
  loading.value = true
  try {
    const repairParam = {}
    if (opsFilter.startDate) repairParam.startTime = opsFilter.startDate + 'T00:00:00'
    if (opsFilter.endDate) repairParam.endTime = opsFilter.endDate + 'T23:59:59'

    const maintParam = {}
    if (opsFilter.startDate) maintParam.startActualTime = opsFilter.startDate + 'T00:00:00'
    if (opsFilter.endDate) maintParam.endActualTime = opsFilter.endDate + 'T23:59:59'

    const [repairRes, reminderRes, maintenanceRes] = await Promise.all([
      fetchRepairForChart(repairParam),
      fetchRemindersForChart({}),
      fetchMaintenanceForChart(maintParam),
    ])

    const repairList = repairRes.data?.list ?? repairRes.data ?? []
    const reminderList = (reminderRes.data?.list ?? reminderRes.data) ?? []
    const maintenanceList = (maintenanceRes.data?.list ?? maintenanceRes.data) ?? []

    // 维修状态饼图
    const repairStatusCount = {}
    repairList.forEach((r) => {
      const s = r.status ?? 0
      repairStatusCount[s] = (repairStatusCount[s] || 0) + 1
    })
    const repairData = Object.entries(repairStatusCount).map(([k, v]) => ({
      name: REPAIR_STATUS_MAP[k] || `状态${k}`,
      value: v,
    }))
    if (repairData.length === 0) repairData.push({ name: '暂无数据', value: 1 })
    renderPie(repairStatusChartRef, repairData, ['#5470c6', '#91cc75', '#fac858', '#ee6666'])

    // 保养提醒状态饼图
    const reminderStatusCount = {}
    reminderList.forEach((r) => {
      const s = r.status ?? 0
      reminderStatusCount[s] = (reminderStatusCount[s] || 0) + 1
    })
    const reminderData = Object.entries(reminderStatusCount).map(([k, v]) => ({
      name: REMINDER_STATUS_MAP[k] || `状态${k}`,
      value: v,
    }))
    if (reminderData.length === 0) reminderData.push({ name: '暂无数据', value: 1 })
    renderPie(reminderStatusChartRef, reminderData, ['#ee6666', '#fac858', '#91cc75', '#73c0de'])

    // 维修费用按模具
    const repairByMold = {}
    repairList.forEach((r) => {
      const mid = r.moldId || 'unknown'
      const name = r.moldName || r.moldCode || mid
      repairByMold[name] = (repairByMold[name] || 0) + Number(r.cost || 0)
    })
    const moldNames = Object.keys(repairByMold).slice(0, 10)
    renderBar(repairCostChartRef, moldNames, moldNames.map((n) => repairByMold[n]), '#5470c6')

    // 保养类型分布
    const maintTypeCount = {}
    maintenanceList.forEach((m) => {
      const t = m.maintenanceType || '未分类'
      maintTypeCount[t] = (maintTypeCount[t] || 0) + 1
    })
    const maintData = Object.entries(maintTypeCount).map(([k, v]) => ({ name: k, value: v }))
    if (maintData.length === 0) maintData.push({ name: '暂无数据', value: 1 })
    renderPie(maintenanceTypeChartRef, maintData, ['#91cc75', '#fac858', '#73c0de', '#ee6666'])
  } catch (e) {
    console.error('加载运维图表失败', e)
  } finally {
    loading.value = false
  }
}

const loadMoldCharts = async () => {
  loading.value = true
  try {
    const [moldsRes, repairRes, maintenanceRes] = await Promise.all([
      fetchMoldsForChart(1, 500),
      fetchRepairForChart(),
      fetchMaintenanceForChart(),
    ])
    const moldList = moldsRes.data?.list ?? []
    const repairList = repairRes.data ?? []
    const maintenanceList = (maintenanceRes.data?.list ?? maintenanceRes.data) ?? []

    const statusCount = {}
    moldList.forEach((m) => {
      const s = m.currentStatus ?? 0
      statusCount[s] = (statusCount[s] || 0) + 1
    })
    renderPie(moldStatusChart2Ref, Object.entries(statusCount).map(([k, v]) => ({
      name: MOLD_STATUS_MAP[k] || `状态${k}`,
      value: v,
    })), ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'])

    const usageTop = [...moldList]
      .filter((m) => (m.totalUsageCount ?? 0) > 0)
      .sort((a, b) => (b.totalUsageCount ?? 0) - (a.totalUsageCount ?? 0))
      .slice(0, 10)
    renderBar(
      moldUsageChartRef,
      usageTop.map((m) => m.moldCode || m.name || m.id),
      usageTop.map((m) => m.totalUsageCount ?? 0),
      '#91cc75'
    )

    const repairByMold = {}
    repairList.forEach((r) => {
      const name = r.moldName || r.moldCode || r.moldId || '未知'
      repairByMold[name] = repairByMold[name] || { repair: 0, maintenance: 0 }
      repairByMold[name].repair += Number(r.cost || 0)
    })
    maintenanceList.forEach((m) => {
      const name = m.moldName || m.moldCode || m.moldId || '未知'
      repairByMold[name] = repairByMold[name] || { repair: 0, maintenance: 0 }
      repairByMold[name].maintenance += Number(m.cost || 0)
    })
    const costTop = Object.entries(repairByMold)
      .filter(([, v]) => v.repair > 0 || v.maintenance > 0)
      .sort(([, a], [, b]) => (b.repair + b.maintenance) - (a.repair + a.maintenance))
      .slice(0, 10)
    const names = costTop.map(([n]) => n)
    renderBarStack(
      moldCostChartRef,
      names,
      costTop.map(([, v]) => v.repair),
      costTop.map(([, v]) => v.maintenance),
      '维修成本',
      '保养成本'
    )
  } catch (e) {
    console.error('加载模具图表失败', e)
  } finally {
    loading.value = false
  }
}

const renderPie = (chartRef, data, colors = []) => {
  if (!chartRef?.value || !data?.length) return
  let ch = echarts.getInstanceByDom(chartRef.value)
  if (!ch) ch = echarts.init(chartRef.value)
  ch.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 条 ({d}%)' },
    legend: { bottom: 0 },
    color: colors.length ? colors : undefined,
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data,
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        fontSize: 12,
      },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' } },
    }],
  })
}

const renderLine = (chartRef, xData, series, color) => {
  if (!chartRef?.value) return
  let ch = echarts.getInstanceByDom(chartRef.value)
  if (!ch) ch = echarts.init(chartRef.value)
  const s = series.map((sr) => ({
    name: sr.name,
    type: 'line',
    smooth: true,
    data: sr.data,
    itemStyle: { color },
  }))
  ch.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: s,
  })
}

const renderBar = (chartRef, xData, yData, color) => {
  if (!chartRef?.value) return
  let ch = echarts.getInstanceByDom(chartRef.value)
  if (!ch) ch = echarts.init(chartRef.value)
  ch.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: yData,
      itemStyle: { color },
      label: { show: true, position: 'top', formatter: '{c}' },
    }],
  })
}

const renderBarStack = (chartRef, xData, repairData, maintData, name1, name2) => {
  if (!chartRef?.value) return
  let ch = echarts.getInstanceByDom(chartRef.value)
  if (!ch) ch = echarts.init(chartRef.value)
  ch.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: [
      { name: name1, type: 'bar', stack: 'total', data: repairData, itemStyle: { color: '#5470c6' }, label: { show: true, position: 'top', formatter: '{c}' } },
      { name: name2, type: 'bar', stack: 'total', data: maintData, itemStyle: { color: '#91cc75' }, label: { show: true, position: 'top', formatter: '{c}' } },
    ],
  })
}

watch(activeTab, (tab) => {
  if (tab === 'monitoring') loadMonitoringCharts()
  else if (tab === 'ops') loadOpsCharts()
  else if (tab === 'mold') loadMoldCharts()
})

onMounted(async () => {
  initDateRange()
  await loadMolds()
  await loadOverviewData()
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  min-height: 100vh;
  background: #f3f4f6;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.top-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
}

.top-title {
  font-size: 22px;
  font-weight: 600;
}

.top-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

.main-content {
  flex: 1;
  padding: 24px;
  position: relative;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 10px 20px;
  border: 1px solid #d1d5db;
  background: #fff;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.tab-btn.active {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
  border-color: transparent;
}

.chart-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  font-size: 32px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e3c72;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-item label {
  font-size: 12px;
  color: #374151;
  font-weight: 500;
}

.filter-item label .required {
  color: #dc2626;
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  min-width: 140px;
}

.chart-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

@media (max-width: 900px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.chart-card.half {
  min-height: 320px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 16px 0;
}

.chart-dom {
  width: 100%;
  height: 320px;
}

.primary-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.primary-btn:hover {
  opacity: 0.95;
}

.loading-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #374151;
  z-index: 100;
}
</style>
