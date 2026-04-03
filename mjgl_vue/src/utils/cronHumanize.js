/**
 * 将 Spring 6 域 Cron（秒 分 时 日 月 周）转为简短中文说明。
 * 无法识别时返回通用提示，避免只展示冷冰冰的表达式。
 */
const pad2 = (n) => String(n).padStart(2, '0')

/**
 * @param {string} expression
 * @returns {string}
 */
export function describeSpringCron(expression) {
  const t = (expression || '').trim()
  if (!t) return '（未填写）'

  const parts = t.split(/\s+/).filter(Boolean)
  if (parts.length !== 6) {
    return `非标准 6 段表达式（当前 ${parts.length} 段），建议由运维/技术人员修改`
  }

  const [sec, min, hour, dom, mon, dow] = parts

  // 每天在固定时刻：0 m h * * ?
  if (dom === '*' && mon === '*' && dow === '?') {
    if (/^\d+$/.test(sec) && /^\d+$/.test(min) && /^\d+$/.test(hour)) {
      const hh = pad2(parseInt(hour, 10))
      const mm = pad2(parseInt(min, 10))
      const ss = pad2(parseInt(sec, 10))
      const tail = sec === '0' ? '' : `:${ss}`
      return `每天在 ${hh}:${mm}${tail} 执行一次`
    }
  }

  // 每 N 小时整点：0 0 */N * * ?
  if (sec === '0' && min === '0' && /^\*\/\d+$/.test(hour) && dom === '*' && mon === '*' && dow === '?') {
    const n = parseInt(hour.slice(2), 10)
    if (n > 0) {
      return `每 ${n} 小时执行一次（从 0 点起的整点）`
    }
  }

  // 从 H 时起每 N 小时：0 0 H/N * * ?（如 0/1 表示每小时整点）
  if (sec === '0' && min === '0' && /^\d+\/\d+$/.test(hour) && dom === '*' && mon === '*' && dow === '?') {
    const [h0, step] = hour.split('/')
    const start = parseInt(h0, 10)
    const n = parseInt(step, 10)
    if (n > 0 && !Number.isNaN(start)) {
      if (n === 1 && start === 0) {
        return '每小时整点执行一次'
      }
      return `每 ${n} 小时执行一次（从每天 ${pad2(start)}:00 起按步长）`
    }
  }

  // 每 N 分钟：0 */N * * * ?
  if (/^\d+$/.test(sec) && /^\*\/\d+$/.test(min) && hour === '*' && dom === '*' && mon === '*' && dow === '?') {
    const n = parseInt(min.slice(2), 10)
    if (n > 0) {
      return `每 ${n} 分钟执行一次`
    }
  }

  // 每周固定星期、固定时刻：0 m h ? * DOW（DOW 可为 MON 或 1）
  if (sec === '0' && /^\d+$/.test(min) && /^\d+$/.test(hour) && dom === '?' && mon === '*' && dow !== '?' && dow !== '*') {
    const w = weekdayToCn(dow)
    const hh = pad2(parseInt(hour, 10))
    const mm = pad2(parseInt(min, 10))
    return `每${w} ${hh}:${mm} 执行一次`
  }

  // 每月固定日：0 m h D * ?（日不为 *）
  if (sec === '0' && /^\d+$/.test(min) && /^\d+$/.test(hour) && /^\d+$/.test(dom) && mon === '*' && dow === '?') {
    const day = parseInt(dom, 10)
    const hh = pad2(parseInt(hour, 10))
    const mm = pad2(parseInt(min, 10))
    return `每月 ${day} 日 ${hh}:${mm} 执行一次`
  }

  return `定时规则较复杂，表达式为：${t}。如需调整请联系技术人员或在文档核对 Spring Cron 语法`
}

const WEEK_MAP = {
  SUN: '周日',
  MON: '周一',
  TUE: '周二',
  WED: '周三',
  THU: '周四',
  FRI: '周五',
  SAT: '周六',
  1: '周日',
  2: '周一',
  3: '周二',
  4: '周三',
  5: '周四',
  6: '周五',
  7: '周六',
}

function weekdayToCn(dow) {
  const u = String(dow).toUpperCase()
  return WEEK_MAP[u] || WEEK_MAP[dow] || `周(${dow})`
}
