/**
 * localStorage 薄封装。
 * 代码生成器原本从 `@/common/storage` 引这个模块，作用就是统一读写本地缓存。
 */
export default {
  set(key, value) {
    localStorage.setItem(key, value)
  },
  get(key) {
    return localStorage.getItem(key)
  },
  remove(key) {
    localStorage.removeItem(key)
  },
  clear() {
    localStorage.clear()
  },
  /** 取出来当对象用，解析失败返回 null */
  getObj(key) {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    try {
      return JSON.parse(raw)
    } catch (e) {
      return null
    }
  },
  setObj(key, value) {
    localStorage.setItem(key, JSON.stringify(value))
  },
}
