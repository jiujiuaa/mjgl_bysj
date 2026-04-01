<template>
  <div class="mold-statistics-container">
    <AppSidebar />

    <div class="layout-main" v-back-to-top>
      <header class="top-header">
        <div class="top-title">模具统计看板</div>
        <div class="top-subtitle">模具维度统计、时间趋势与 Excel 报表导出</div>
      </header>

      <main class="main-content">
        <div class="content-wrapper">
          <section class="card" style="margin-bottom: 16px;">
            <div class="card-header">
              <h3 class="card-title">筛选条件</h3>
              <div class="header-actions">
                <button class="primary-btn" :disabled="loading" @click="handleQuery">
                  {{ loading ? '加载中...' : '查询' }}
                </button>
                <button class="secondary-btn" :disabled="loading" @click="handleExportXlsx">
                  导出Excel
                </button>
              </div>
            </div>

            <div class="card-body">
              <div class="query-row">
                <div class="query-item">
                  <label>模具</label>
                  <select v-model="query.moldId" class="form-input query-input">
                    <option value="">全部</option>
                    <option v-for="m in molds" :key="m.id" :value="m.id">
                      {{ (m.moldCode || '') + (m.name ? ` - ${m.name}` : '') || m.id }}
                    </option>
                  </select>
                </div>

                <div class="query-item">
                  <label>开始日期</label>
                  <input v-model="query.startDate" type="date" class="form-input query-input" />
                </div>

                <div class="query-item">
                  <label>结束日期</label>
                  <input v-model="query.endDate" type="date" class="form-input query-input" />
                </div>

                <div class="query-item">
                  <label>趋势粒度</label>
                  <select v-model="query.bucketType" class="form-input query-input">
                    <option value="DAY">DAY(日)</option>
                    <option value="WEEK">WEEK(周)</option>
                    <option value="MONTH">MONTH(月)</option>
                  </select>
                </div>
              </div>
            </div>
          </section>

          <section class="card" style="margin-bottom: 16px;">
            <div class="card-header">
              <h3 class="card-title">时间趋势</h3>
            </div>
            <div class="card-body">
              <div class="chart-row">
                <div class="chart-card half">
                  <h3 class="chart-title">使用/维修/保养次数趋势</h3>
                  <div ref="trendCountChartRef" class="chart-dom" />
                </div>
                <div class="chart-card half">
                  <h3 class="chart-title">生产时长 & 平均维修时长</h3>
                  <div ref="trendTimeChartRef" class="chart-dom" />
                </div>
              </div>
            </div>
          </section>

          <section class="card">
            <div class="card-header">
              <h3 class="card-title">模具维度统计</h3>
            </div>
            <div class="card-body">
              <div v-if="loading" class="table-loading">统计加载中...</div>
              <div v-else class="table-wrapper">
                <table class="mold-table">
                  <thead>
                    <tr>
                      <th>模具编号</th>
                      <th>名称</th>
                      <th>累计使用次数(次)</th>
                      <th>累计生产时长(小时)</th>
                      <th>维修次数(次)</th>
                      <th>平均维修时长(小时)</th>
                      <th>保养计划数(推导MP)</th>
                      <th>保养完成数(次)</th>
                      <th>保养周期达标率(%)</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="!stats.length">
                      <td colspan="9" class="empty-cell">暂无统计数据</td>
                    </tr>
                    <tr v-for="s in stats" :key="s.moldId">
                      <td>{{ s.moldCode }}</td>
                      <td>{{ s.moldName }}</td>
                      <td>{{ s.totalUsageCount ?? '-' }}</td>
                      <td>{{ formatBig(s.totalProductionTimeHours) }}</td>
                      <td>{{ s.repairFrequency ?? '-' }}</td>
                      <td>{{ formatBig(s.avgRepairDurationHours) }}</td>
                      <td>{{ s.maintenancePlannedCount ?? '-' }}</td>
                      <td>{{ s.maintenanceCompletedCount ?? '-' }}</td>
                      <td>{{ formatBig(s.maintenanceRatePercent) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import AppSidebar from '@/components/AppSidebar.vue'
import { fetchMolds } from '@/api/molds'
import { queryMoldStats, queryMoldTrends, exportMoldStatsXlsx } from '@/api/moldStatistics'

const molds = ref([])
const loading = ref(false)

const query = reactive({
  moldId: '',
  startDate: '',
  endDate: '',
  bucketType: 'WEEK',
})

const stats = ref([])
const trends = ref(null)

const trendCountChartRef = ref(null)
const trendTimeChartRef = ref(null)
let trendCountChart = null
let trendTimeChart = null

const pad2 = (n) => String(n).padStart(2, '0')
const formatDateInput = (d) => `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`

const initDefaultRange = () => {
  const end = new Date()
  const start = new Date(end.getTime() - 29 * 24 * 60 * 60 * 1000)
  query.startDate = formatDateInput(start)
  query.endDate = formatDateInput(end)
}

const formatBig = (v) => {
  if (v === null || v === undefined) return '-'
  const num = typeof v === 'string' ? Number(v) : v
  if (Number.isNaN(num)) return String(v)
  return num.toFixed(2).replace(/\\.00$/, '')
}

const buildStatsParam = () => {
  return {
    moldId: query.moldId || undefined,
    startDate: query.startDate || undefined,
    endDate: query.endDate || undefined,
  }
}

const buildTrendsParam = () => {
  return {
    moldId: query.moldId || undefined,
    startDate: query.startDate || undefined,
    endDate: query.endDate || undefined,
    bucketType: query.bucketType || undefined,
  }
}

const ensureChart = (chartRef, chartVarSetter) => {
  const el = chartRef?.value
  if (!el) return
  let ch = echarts.getInstanceByDom(el)
  if (!ch) ch = echarts.init(el)
  chartVarSetter(ch)
}

const updateCharts = () => {
  if (!trends.value) return

  ensureChart(trendCountChartRef, (ch) => {
    trendCountChart = ch
  })
  ensureChart(trendTimeChartRef, (ch) => {
    trendTimeChart = ch
  })

  const labels = trends.value.labels || []
  const usageCounts = trends.value.usageCounts || []
  const repairCounts = trends.value.repairCounts || []
  const maintenanceCounts = trends.value.maintenanceCounts || []

  const usageProductionHours = trends.value.usageProductionHours || []
  const avgRepairDurationHours = trends.value.avgRepairDurationHours || []

  if (trendCountChart) {
    trendCountChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['使用次数', '维修次数', '保养完成数'] },
      grid: { left: 40, right: 20, bottom: 30, top: 40, containLabel: true },
      xAxis: { type: 'category', data: labels },
      yAxis: { type: 'value' },
      series: [
        { name: '使用次数', type: 'line', smooth: true, data: usageCounts },
        { name: '维修次数', type: 'line', smooth: true, data: repairCounts },
        { name: '保养完成数', type: 'line', smooth: true, data: maintenanceCounts },
      ],
    })
  }

  if (trendTimeChart) {
    trendTimeChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['生产时长(h)', '平均维修时长(h)'] },
      grid: { left: 40, right: 20, bottom: 30, top: 40, containLabel: true },
      xAxis: { type: 'category', data: labels },
      yAxis: [
        { type: 'value', name: '生产时长(h)' },
        { type: 'value', name: '维修时长(h)', axisLabel: { formatter: '{value}' } },
      ],
      series: [
        { name: '生产时长(h)', type: 'bar', data: usageProductionHours, yAxisIndex: 0 },
        { name: '平均维修时长(h)', type: 'line', data: avgRepairDurationHours, yAxisIndex: 1, smooth: true },
      ],
    })
  }
}

const loadAll = async () => {
  loading.value = true
  try {
    const statsParam = buildStatsParam()
    const trendsParam = buildTrendsParam()
    const [sRes, tRes] = await Promise.all([
      queryMoldStats(statsParam),
      queryMoldTrends(trendsParam),
    ])

    stats.value = sRes.data || []
    trends.value = tRes.data || null

    updateCharts()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  loadAll()
}

const handleExportXlsx = async () => {
  try {
    const statsParam = buildStatsParam()
    const res = await exportMoldStatsXlsx(statsParam)
    const blob =
      res.data instanceof Blob
        ? res.data
        : new Blob([res.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })

    const start = query.startDate || ''
    const end = query.endDate || ''
    const fileName = `模具统计报表_使用-维修-保养_统计窗口_${start}_${end}.xlsx`

    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
    alert(e.message || '导出失败')
  }
}

onMounted(async () => {
  initDefaultRange()
  try {
    const res = await fetchMolds(1, 1000)
    molds.value = res.data?.list ?? []
  } catch (e) {
    console.error(e)
  }
  await loadAll()

  window.addEventListener('resize', () => {
    trendCountChart?.resize()
    trendTimeChart?.resize()
  })
})

watch(
  () => trends.value,
  () => updateCharts(),
)
</script>

<style scoped>
/* 布局/卡片样式：尽量复用你项目里其它“列表+卡片+表格”页面的视觉结构 */
.mold-statistics-container {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  flex-direction: row;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.top-header {
  padding: 16px 24px 8px;
}

.top-title {
  font-size: 22px;
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
  padding: 24px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.card-body {
  padding: 24px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.primary-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(30, 60, 114, 0.2);
}

.secondary-btn {
  padding: 10px 20px;
  background: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.query-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 20px;
  align-items: flex-end;
}

.query-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 140px;
  flex: 1;
  max-width: 220px;
}

.query-item label {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: #ffffff;
  width: 100%;
}

.table-loading {
  text-align: center;
  padding: 20px 0;
  color: #6b7280;
  font-size: 14px;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.mold-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.mold-table th,
.mold-table td {
  padding: 8px 10px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  vertical-align: top;
}

.mold-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.empty-cell {
  text-align: center;
  color: #9ca3af;
}

/* 趋势图容器布局：使用 grid 保证两列尺寸稳定 */
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
  min-height: 320px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.chart-dom {
  height: 320px;
  width: 100%;
}
</style>

