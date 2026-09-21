/**
 * 表单校验器，供 element-plus 的 `rules` 里 `validator` 字段使用。
 * 对应代码生成器里的 `this.$validate`。
 */

/** 手机号（严格：1 开头 11 位） */
export function isMobile(rule, value, callback) {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

/** 手机号（宽松：空值放过，和 register 页的判空逻辑一致） */
export function isMobile2(value) {
  if (!value) return true
  return /^1[3-9]\d{9}$/.test(value)
}

/** 整数 */
export function isIntNumer(rule, value, callback) {
  if (value !== undefined && value !== null && value !== '' && !/^-?\d+$/.test(value)) {
    callback(new Error('请输入整数'))
  } else {
    callback()
  }
}

/* ---------------- 实名认证 ---------------- */

/*
 * 身份证校验与后端 zhiyouxing-common 的 IdCardUtils 一一对应（18 位 + 出生日期 + 校验位）。
 * 两边都要有一份：这里是即时提示，那边是不能绕过的兜底。改其中一处记得改另一处。
 */

/** 加权因子与余数对应的校验位（ISO 7064:1983 MOD 11-2） */
const ID_WEIGHTS = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
const ID_CHECK_CODES = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']

/** 去掉空格与连字符、小写 x 转大写。抄证件号时带空格很常见，不能当成格式错误 */
export function normalizeIdCard(value) {
  return String(value == null ? '' : value)
    .replace(/[\s-]/g, '')
    .toUpperCase()
}

/** 身份证号不通过的原因；通过返回空串 */
export function idCardError(value) {
  const v = normalizeIdCard(value)
  if (!v) return '请填写身份证号'
  if (!/^\d{17}[\dX]$/.test(v)) return '身份证号应为 18 位，末位可以是 X'

  const year = Number(v.slice(6, 10))
  const month = Number(v.slice(10, 12))
  const day = Number(v.slice(12, 14))
  const birthday = new Date(year, month - 1, day)
  const real =
    birthday.getFullYear() === year && birthday.getMonth() === month - 1 && birthday.getDate() === day
  // 1 月和 2 月会被 Date 顺手进位成 3 月，所以逐段回读比对，不能只看能不能构造
  if (!real || year < 1900 || birthday.getTime() > Date.now()) return '身份证号里的出生日期不合法'

  let sum = 0
  for (let i = 0; i < 17; i++) sum += (v.charCodeAt(i) - 48) * ID_WEIGHTS[i]
  if (ID_CHECK_CODES[sum % 11] !== v[17]) return '身份证号校验位不正确，请核对后重填'
  return ''
}

/** element-plus 规则：身份证号 */
export function isIdCard(rule, value, callback) {
  const msg = idCardError(value)
  if (msg) {
    callback(new Error(msg))
  } else {
    callback()
  }
}

/**
 * 姓名：2~15 位汉字，或「汉字·汉字」的少数民族姓名，或 2~30 位字母。
 * 带间隔号的名字（如 买买提·艾力）不限制每段的字数 —— 段太短时整体仍是姓名的一部分，
 * 「2 字起」只对不含间隔号的普通姓名成立。
 */
const NAME_CN = /^[一-龥]{2,15}$|^[一-龥]{1,15}·[一-龥]{1,15}$/
const NAME_EN = /^[A-Za-z][A-Za-z\s.]{1,29}$/

/** 姓名不通过的原因；通过返回空串 */
export function realNameError(value) {
  const v = String(value == null ? '' : value).trim()
  if (!v) return '请填写真实姓名'
  if (!NAME_CN.test(v) && !NAME_EN.test(v)) return '姓名需为 2~15 位汉字，或 2~30 位字母'
  return ''
}

/** element-plus 规则：真实姓名 */
export function isRealName(rule, value, callback) {
  const msg = realNameError(value)
  if (msg) {
    callback(new Error(msg))
  } else {
    callback()
  }
}

export default { isMobile, isMobile2, isIntNumer, isIdCard, isRealName, idCardError, realNameError, normalizeIdCard }
