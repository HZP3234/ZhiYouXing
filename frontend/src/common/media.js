import config from '@/config/config'
import { pickScene } from '@/common/scenes'

/*
 * 图片路径处理。后端的图片字段是「逗号分隔的相对路径串」，而且没有前导斜杠，
 * 例如：'upload/remenjingdian_tupian1.jpg,upload/remenjingdian_tupian2.jpg'
 * （和 FileUpload.vue 里 `baseUrl + url` 的约定一致）
 *
 * 这里所有函数都必须对 null 安全 —— 真实库里这些列可能是 NULL，
 * 而 `null.split(',')` 是在渲染期抛错，会直接白屏。
 */

/** 图片串 → 数组。空值一律返回 [] */
export function imageList(raw) {
  if (!raw) return []
  if (Array.isArray(raw)) return raw.filter(Boolean)
  const value = String(raw).trim()
  /*
   * base64 头像（个人中心选图后存在 user.avatar 里）本身就是个 data URL，
   * 而 data URL 的 "base64," 里那个逗号会把下面按逗号切开的逻辑拦腰截断，
   * 只剩下 "data:image/jpeg;base64" 这种取不到图的前半段。
   */
  if (value.startsWith('data:')) return [value]
  return value
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}

export function firstImage(raw) {
  return imageList(raw)[0] || ''
}

/**
 * 相对路径补成可请求的 URL。
 * 已经是 http(s):// 、data:/blob: 或 / 开头的原样返回
 * （mock 用的 /pictures/*.png 走 / 这一支，个人中心的 base64 头像走 data: 这一支）。
 */
export function resolveImage(raw) {
  const first = firstImage(raw)
  if (!first) return ''
  if (/^(https?:|data:|blob:)/i.test(first)) return first
  if (first.startsWith('/')) return first
  return config.baseUrl + first
}

/**
 * 「设施 / 特色菜品」这类字段也是逗号串，另外兼容中文逗号和顿号。
 * 真库里是 '客房设施1' 这样的单值，mock 里是多项，两种都能切。
 */
export function splitTags(raw) {
  if (!raw) return []
  return String(raw)
    .split(/[,，、]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

/** 图片加载失败 / 为空时用的风景画兜底，返回 { uri, position, key } */
export function fallbackScene(seed) {
  return pickScene(seed)
}
