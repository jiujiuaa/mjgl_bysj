import {
  queryTemperatureLogs,
  queryLubricationLogs,
} from '@/api/monitoringLogs'
import { queryAbnormalRecords } from '@/api/abnormalRecords'
import { fetchMolds } from '@/api/molds'
import { fetchAllRepairRecords, queryRepairRecords } from '@/api/repairRecords'
import { queryMaintenanceLogs } from '@/api/maintenanceLogs'
import { queryMaintenanceReminders } from '@/api/maintenanceReminders'

const CHART_PAGE_SIZE = 500

/** 温度巡检数据（用于折线图） */
export const fetchTemperatureChartData = (param) =>
  queryTemperatureLogs(param, 1, CHART_PAGE_SIZE)

/** 润滑巡检数据（用于折线图） */
export const fetchLubricationChartData = (param) =>
  queryLubricationLogs(param, 1, CHART_PAGE_SIZE)

/** 异常记录数据（用于趋势/饼图） */
export const fetchAbnormalChartData = (param) =>
  queryAbnormalRecords(param, 1, CHART_PAGE_SIZE)

/** 模具列表（用于统计） */
export const fetchMoldsForChart = (pageNum = 1, pageSize = 500) =>
  fetchMolds(pageNum, pageSize)

/** 维修记录（全部或按条件，用于统计） */
export const fetchRepairForChart = (param) =>
  param && (param.startTime || param.endTime)
    ? queryRepairRecords(param, 1, CHART_PAGE_SIZE)
    : fetchAllRepairRecords()

/** 保养记录（用于统计） */
export const fetchMaintenanceForChart = (param) =>
  queryMaintenanceLogs(param || {}, 1, CHART_PAGE_SIZE)

/** 保养提醒（用于统计） */
export const fetchRemindersForChart = (param) =>
  queryMaintenanceReminders(param || {}, 1, CHART_PAGE_SIZE)
