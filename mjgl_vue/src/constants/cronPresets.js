/**
 * 业务配置中心 — 定时任务常用预设（Spring 6 域：秒 分 时 日 月 周）
 */
export const CRON_PRESET_CUSTOM = '__custom__'

/** @type {{ id: string, label: string, cron: string }[]} */
export const CRON_PRESETS = [
  { id: 'daily_00', label: '每天 00:00（午夜）', cron: '0 0 0 * * ?' },
  { id: 'daily_01', label: '每天 01:00', cron: '0 0 1 * * ?' },
  { id: 'daily_02', label: '每天 02:00', cron: '0 0 2 * * ?' },
  { id: 'daily_03', label: '每天 03:00（凌晨 3 点）', cron: '0 0 3 * * ?' },
  { id: 'daily_04', label: '每天 04:00', cron: '0 0 4 * * ?' },
  { id: 'daily_05', label: '每天 05:00', cron: '0 0 5 * * ?' },
  { id: 'daily_06', label: '每天 06:00', cron: '0 0 6 * * ?' },
  { id: 'daily_08', label: '每天 08:00', cron: '0 0 8 * * ?' },
  { id: 'daily_09', label: '每天 09:00', cron: '0 0 9 * * ?' },
  { id: 'daily_12', label: '每天 12:00（中午）', cron: '0 0 12 * * ?' },
  { id: 'daily_15', label: '每天 15:00', cron: '0 0 15 * * ?' },
  { id: 'daily_18', label: '每天 18:00', cron: '0 0 18 * * ?' },
  { id: 'daily_21', label: '每天 21:00', cron: '0 0 21 * * ?' },
  { id: 'daily_23', label: '每天 23:00', cron: '0 0 23 * * ?' },
  { id: 'daily_0010', label: '每天 00:10（0 点 10 分）', cron: '0 10 0 * * ?' },
  { id: 'daily_0130', label: '每天 01:30', cron: '0 30 1 * * ?' },
  { id: 'every_1h', label: '每小时（整点）', cron: '0 0 0/1 * * ?' },
  { id: 'every_6h', label: '每 6 小时（从 0 点起整点）', cron: '0 0 */6 * * ?' },
  { id: 'every_12h', label: '每 12 小时', cron: '0 0 */12 * * ?' },
]

export const normalizeCronExpression = (s) => (s || '').trim().split(/\s+/).filter(Boolean).join(' ')

/**
 * @param {string} cron
 * @returns {{ id: string, label: string, cron: string } | undefined}
 */
export const findPresetByCron = (cron) => {
  const n = normalizeCronExpression(cron)
  return CRON_PRESETS.find((p) => normalizeCronExpression(p.cron) === n)
}
