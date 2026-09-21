import { ElMessage, ElMessageBox } from 'element-plus'
import storage from '@/common/storage'
import { useMockFor } from '@/api/client'

/*
 * 下单页需要的登录信息。
 *
 * 后端 /save 走 AuthorizationInterceptor，没有 header Token 会直接抛
 * 「请先登录」，所以提交前必须先确认登录态。
 *
 * 用户信息散在几处 localStorage（login.vue 写 frontToken / username，
 * index.vue 拉到 session 后写 sessionForm / frontUserid），
 * 这里统一收口。**任何一项取不到就留空让用户手填**，
 * 不要假设它一定存在 —— sessionForm 只有在进过 index.vue 之后才会有。
 */

export const USER_TABLE = () => localStorage.getItem('UserTableName') || 'users'

export function getToken() {
  return localStorage.getItem('frontToken') || ''
}

export function isLogin() {
  return Boolean(getToken())
}

/** @returns {{account:string, name:string, phone:string, id:string}} */
export function currentUser() {
  const session = storage.getObj('sessionForm') || {}
  const account = localStorage.getItem('username') || session.username || ''
  return {
    account,
    /*
     * session 里放的是 user 表的整条记录，列名就是 user_name / contact_phone。
     * 早先这里找的是 name / phone，两个都不存在，于是「姓名」永远退化成登录账号、
     * 「手机号」永远是空串 —— 下单页那次「默认带出个人中心的资料」一直是坏的。
     * 后一个候选值留给代码生成器的其它角色表（它们同样用 name / phone 这类列名）。
     */
    name: session.userName || session.name || session.username || account,
    // 支付时要核对的账号绑定号码，就是个人中心里填的「联系方式」
    phone: session.contactPhone || session.phone || session.mobile || session.tel || '',
    id: localStorage.getItem('frontUserid') || session.id || '',
  }
}

/**
 * 提交前的登录闸门。返回 true 才允许继续提交。
 * 未登录时弹确认框，同意就跳登录页并带上 redirect，登录完能回到当前页。
 *
 * @param {import('vue-router').Router} router
 * @param {string} prefix 下单打哪个前缀 —— 决定这次提交是不是真的会发出去。
 *                        景点（attraction）/ 餐厅（restaurant）/ 酒店（hotel_reservation）
 *                        都已切到真后端，这三个模块下单**必须登录**；
 *                        只有还没接后端的模块才会走下面那条放行分支。
 */
export async function ensureLogin(router, prefix) {
  if (isLogin()) return true

  /*
   * 还在 mock 的模块直接放行。
   *
   * 原因：登录接口本身也要后端（/users/login 走网关），后端没起就登不进去，
   * 于是「列表 → 详情 → 下单」的闭环会在最后一步被一个永远进不去的登录页卡死 ——
   * 而 mock 模式存在的意义恰恰是「后端没起也能完整演示」。
   *
   * 切到真后端的前缀走不到这里，登录闸门照常生效，所以不会出现越权下单。
   * 酒店（hotel_reservation）进 client.js 的 REAL_PREFIXES 之后就是从这个分支
   * 挪到了「真拦」那一侧 —— 未登录点「预订这间」会被要求先登录，这是想要的。
   */
  if (useMockFor(prefix)) {
    ElMessage.info('演示模式：未登录也可提交，数据只存在本地浏览器')
    return true
  }

  try {
    await ElMessageBox.confirm('下单需要先登录，是否现在去登录？', '还没登录', {
      confirmButtonText: '去登录',
      cancelButtonText: '再看看',
      type: 'warning',
    })
  } catch (e) {
    return false // 用户点了取消或右上角关闭
  }

  router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
  return false
}
