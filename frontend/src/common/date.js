/*
 * 日期选择器与下单页共用的日期工具。
 *
 * 为什么不并进 format.js：那个文件的头注释把职责钉在「展示层的格式化」上，
 * 而这里放的是日期选择器的判定（:disabled-date）和下单要用的补时刻/日期推进，
 * 都不是格式化。混进去会让「格式化工具」这个词失真。
 * 依赖是单向的：本文件用 format.js 的 parseDate / fmtDate，反过来不行。
 */

import { parseDate, fmtDate } from '@/common/format'

/**
 * el-date-picker 的 :disabled-date —— 今天之前（先把今天的时分秒清零再比）不可选。
 * 原先 attractionOrder / hotelInfoOrder / restaurantOrder 各逐字抄了一份，统一放这里。
 */
export function disablePast(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

/**
 * 给「只选到天」的日期补上时刻，拼成后端要的 'yyyy-MM-dd HH:mm:ss'。
 *
 * 必须补：实体上的 LocalDateTime 都挂了 @JsonFormat(yyyy-MM-dd HH:mm:ss)，
 * 只发 'yyyy-MM-dd' 会被 Jackson 判 400（同一件事在 adminResources.js 的 departureDate
 * 和 hotelInfoOrder 的 reservationTime 各写过一遍警告）。酒店入住按惯例给 14:00。
 */
export function withTime(day, time = '14:00:00') {
  return day ? `${String(day).slice(0, 10)} ${time}` : ''
}

/**
 * 'yyyy-MM-dd' 加 n 天，仍返回 'yyyy-MM-dd'；空值/算不出来返回 '—'
 * （离店日期那种只读回显，算不出来就显示破折号，不显示 Invalid Date）。
 */
export function addDays(dateStr, days) {
  const d = parseDate(dateStr)
  if (!d) return '—'
  d.setDate(d.getDate() + (Number(days) || 0))
  return fmtDate(d)
}

export default { disablePast, withTime, addDays }
