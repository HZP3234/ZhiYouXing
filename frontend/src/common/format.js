/*
 * 展示层的格式化工具。全部是纯函数，不碰 DOM 也不碰网络。
 */

/**
 * 价格：整数不加小数点，小数保留两位。
 * 注意不能无条件 toFixed(2) —— 门票 238 显示成「238.00」很难看。
 */
export function fmtMoney(n) {
  const v = Number(n)
  if (!Number.isFinite(v)) return '—'
  return Number.isInteger(v) ? String(v) : v.toFixed(2)
}

/**
 * 把「人均消费」这类字段抠成数字。餐厅表的 avg_cost 是 varchar，
 * 真实库里存的是「人均消费1」这种文案，不是数字。
 *
 * 只认「基本就是个数」的串（'38' / '¥38' / '38元'），
 * '人均消费1' 里的 1 是序号不是金额，宁可不显示也不能显示成 ¥1。
 */
export function parseAmount(raw) {
  if (raw == null || raw === '') return null
  const m = String(raw).trim().match(/^\D{0,3}(\d+(?:\.\d+)?)\s*(?:元|块)?$/)
  if (!m) return null
  const n = Number(m[1])
  return Number.isFinite(n) ? n : null
}

/** 容忍 'yyyy-MM-dd'、'yyyy-MM-dd HH:mm:ss'、ISO 串、时间戳 */
export function parseDate(v) {
  if (!v) return null
  if (v instanceof Date) return Number.isNaN(v.getTime()) ? null : v
  if (typeof v === 'number') return new Date(v)
  const s = String(v).trim().replace('T', ' ').replace(/\.\d+/, '')
  const m = s.match(/^(\d{4})-(\d{1,2})-(\d{1,2})(?: (\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?/)
  if (!m) {
    const d = new Date(s)
    return Number.isNaN(d.getTime()) ? null : d
  }
  return new Date(+m[1], +m[2] - 1, +m[3], +(m[4] || 0), +(m[5] || 0), +(m[6] || 0))
}

const pad = (n) => String(n).padStart(2, '0')

/** 'yyyy-MM-dd' */
export function fmtDate(v) {
  const d = parseDate(v)
  if (!d) return '—'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 'yyyy-MM-dd HH:mm' */
export function fmtDateTime(v) {
  const d = parseDate(v)
  if (!d) return '—'
  return `${fmtDate(d)} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 评论区的「3天前」。超过 30 天直接给日期 */
export function fmtFromNow(v) {
  const d = parseDate(v)
  if (!d) return '—'
  const diff = Date.now() - d.getTime()
  if (diff < 0) return fmtDate(d)
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min}分钟前`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour}小时前`
  const day = Math.floor(hour / 24)
  if (day < 30) return `${day}天前`
  return fmtDate(d)
}

/**
 * 订单号 / 预约号。
 * order_no 和 reservation_no 在库里都是 UNIQUE，撞了会直接 500 唯一键冲突，
 * 所以用「秒级时间 + 4 位随机」，并且提交按钮要进 loading 防连点。
 */
export function genOrderNo(prefix = 'NO') {
  const d = new Date()
  const stamp =
    `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}` +
    `${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}`
  const rand = String(Math.floor(Math.random() * 10000)).padStart(4, '0')
  return `${prefix}${stamp}${rand}`
}

/** 一组评分的平均值，保留一位小数。没有评分返回 null（页面显示「暂无评分」而不是「0.0」） */
export function avgScore(scores) {
  const list = (scores || []).map(Number).filter((n) => Number.isFinite(n) && n > 0)
  if (!list.length) return null
  const avg = list.reduce((a, b) => a + b, 0) / list.length
  return Math.round(avg * 10) / 10
}

/** 金额：避免 0.1+0.2 那种浮点尾巴 */
export function roundMoney(n) {
  const v = Number(n)
  return Number.isFinite(v) ? Math.round(v * 100) / 100 : 0
}
