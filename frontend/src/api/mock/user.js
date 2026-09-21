import { ok, fail, mockSave } from './engine'
import { idCardError, realNameError, normalizeIdCard } from '@/common/validate'

/*
 * mock 下的 user 服务接口：实名认证 + 个人中心消费记录。
 *
 * 为什么这两个也做 mock：默认模式走的是 config.useMock = true（真实库是占位数据），
 * 而「点景点购票 → 实名认证 → 支付 → 个人中心看到已支付」是完整的一条链。
 * 前三步在 mock 里本来就能跑，只有最后两步属于 user 服务 —— 不补上的话，
 * 演示时订单会停在「支付成功」，个人中心却是空的。
 *
 * 数据落在 localStorage['mock:ticket_order']（由 engine.mockSave 写的门票订单），
 * 字段名与后端 ConsumptionDao.selectTicketOrders 对齐，页面不必分叉。
 *
 * 两个刻意的边界：
 *   1. 只映射门票订单。酒店预订（mock:hotel_reservation）与报团没有对应的 mock 列表 ——
 *      真接口下它们由 ConsumptionDao 一并返回，mock 下不假装支持，免得两边的分区数字对不上。
 *   2. 账号直接读 localStorage['username']，不走 common/user 的 currentUser()：
 *      那边 import 了 api/client，而 client 又 import 本模块所在的 mock 入口，
 *      绕成环虽然能跑，但没必要。取值口径与 currentUser 一致（登录时写的就是这个键）。
 */

const IDENTITY_KEY = 'mock:user_identity'
const TICKET_ORDER_KEY = 'mock:ticket_order'

/*
 * 未支付订单的时限，与后端 ConsumptionController.PAY_TIMEOUT_MINUTES 一致（10 分钟）。
 *
 * 这里刻意不 import 后端那份、也不抽公共常量：mock 是「另一套实现」，两份值
 * 各自独立才是对的 —— 抽到一起会让人以为改动其中一处就够，而 mock 和后端
 * 本就要分别改。唯一的要求是改一处时记得另一处，所以在两边都写了这句注释。
 */
const PAY_TIMEOUT_MS = 10 * 60 * 1000

function readRows(key) {
  try {
    const rows = JSON.parse(localStorage.getItem(key) || '[]')
    return Array.isArray(rows) ? rows : []
  } catch (e) {
    return []
  }
}

function writeRows(key, rows) {
  localStorage.setItem(key, JSON.stringify(rows))
}

/** 未登录时是空串。mock 不拦未登录，空串本身就是一个合法（匿名的）账号 */
function currentAccount() {
  return localStorage.getItem('username') || ''
}

/** 落库时间，格式与 engine.mockSave 一致（'yyyy-MM-dd HH:mm:ss'），列表按它倒序 */
function now() {
  return new Date().toLocaleString('sv-SE')
}

/**
 * 脱敏：前 6 后 4。完整的证件号只存在 localStorage 里（用户自己填的），
 * 接口返回值一律脱敏 —— 与后端 IdCardUtils.mask 同一口径，
 * 页面在两种模式下拿到的都是「110101********1234」这种。
 */
function maskIdCard(idCard) {
  const v = String(idCard || '')
  if (v.length < 11) return null
  return `${v.slice(0, 6)}${'*'.repeat(v.length - 10)}${v.slice(-4)}`
}

/* ---------------- 实名认证 ---------------- */

function findIdentity() {
  const account = currentAccount()
  return readRows(IDENTITY_KEY).find((r) => String(r.userAccount || '') === account) || null
}

/** 对外的形状与后端 UserIdentityVO 一致：只给脱敏号 */
function toVO(row) {
  if (!row) return { verified: false, realName: null, idCardMasked: null, verifyTime: null }
  return {
    verified: true,
    realName: row.realName,
    idCardMasked: maskIdCard(row.idCard),
    verifyTime: row.verifyTime,
  }
}

export function mockIdentityDetail() {
  return ok(toVO(findIdentity()))
}

/**
 * 提交认证。
 *
 * 校验直接复用 common/validate 里前端那份（后端那一份在 common 的 IdCardUtils），
 * 口径完全一致 —— 否则会出现「页面说格式没问题、mock 却拒绝」这种一开关就复现不了的怪事。
 */
export function mockIdentitySave(payload) {
  const realName = String(payload.realName || '').trim()
  const idCard = normalizeIdCard(payload.idCard)

  const nameMsg = realNameError(realName)
  if (nameMsg) return fail(nameMsg)
  const cardMsg = idCardError(idCard)
  if (cardMsg) return fail(cardMsg)

  const rows = readRows(IDENTITY_KEY)
  const account = currentAccount()
  const mine = rows.find((r) => String(r.userAccount || '') === account)

  if (mine) {
    // 同号重复提交按成功返回（连点两次不该报错），换号拒绝 —— 与后端同一条规则
    if (mine.idCard !== idCard) {
      return fail('本账号已完成实名认证，证件号不能自行修改，如需变更请联系客服')
    }
    return ok(toVO(mine))
  }

  if (rows.some((r) => r.idCard === idCard)) {
    return fail('该证件号已被其他账号认证，一个证件号只能认证一个账号')
  }

  const row = {
    id: Date.now(),
    addTime: now(),
    userAccount: account,
    realName,
    idCard,
    verifyTime: now(),
  }
  rows.push(row)
  writeRows(IDENTITY_KEY, rows)
  return ok(toVO(row))
}

/* ---------------- 门票下单 ---------------- */

/**
 * 门票订单落库（mock 版的 TicketOrderController.createOrder）。
 *
 * 前端不再发 userAccount / userName / idCard / isPay 这四个字段 —— 真接口里它们
 * 由服务端从登录态与 user_identity 写死。mock 必须做同一件事：少了这一步，
 * 订单记录的 userAccount 就是 undefined，个人中心按账号过滤时一条都匹配不上，
 * 「支付成功但个人中心是空的」。顺带把「没实名不让下单」这条门也放到 mock 里，
 * 两种模式的行为才真正一致。
 */
export function mockTicketOrderSave(payload) {
  const identity = findIdentity()
  if (!identity) return fail('购票前请先完成实名认证')

  return ok(
    mockSave('ticket_order', {
      ...payload,
      userAccount: currentAccount(),
      userName: identity.realName,
      idCard: identity.idCard,
      isPay: '未支付',
    }),
  )
}

/* ---------------- 个人中心：消费记录 ---------------- */

/** add_time 是 'yyyy-MM-dd HH:mm:ss'。换成 ISO 的 T 分隔再交给 Date，各浏览器才当本地时间解析 */
function parseAddTime(value) {
  const t = Date.parse(String(value || '').replace(' ', 'T'))
  return Number.isNaN(t) ? null : t
}

/** 未支付、且已经过了时限的行，就地翻成「已取消」。返回有没有改动，供调用方决定要不要写回 */
function expireUnpaid(rows) {
  let changed = false
  const deadline = Date.now() - PAY_TIMEOUT_MS
  rows.forEach((r) => {
    const isPay = r.isPay || '未支付'
    if (isPay !== '未支付') return
    const at = parseAddTime(r.addTime)
    if (at !== null && at < deadline) {
      r.isPay = '已取消'
      changed = true
    }
  })
  return changed
}

/** 与 ConsumptionDao.selectTicketOrders 同一套列，另加后端 VO 算出来的 paid / commented / cancelled / expireAt */
function toConsumptionRow(r) {
  const isPay = r.isPay || '未支付'
  const isComment = r.isComment || '待评价'
  const at = parseAddTime(r.addTime)
  return {
    source: 'ticket',
    sourceLabel: '景点门票',
    id: r.id,
    orderNo: r.orderNo,
    title: r.attractionName,
    image: r.image,
    amount: r.totalAmount,
    quantityDesc: `×${r.quantity || 1} 张`,
    addTime: r.addTime,
    isPay,
    isComment,
    paid: isPay === '已支付',
    commented: isComment === '已评价',
    cancelled: isPay === '已取消',
    // 与后端 fillExpireAt 同口径：餐厅才没有 expireAt，门票一律有
    expireAt: at === null ? null : at + PAY_TIMEOUT_MS,
  }
}

function myTicketOrders() {
  const account = currentAccount()
  return readRows(TICKET_ORDER_KEY).filter((r) => String(r.userAccount || '') === account)
}

export function mockConsumptionList() {
  // 先清超时的，再把结果读出来 —— 与后端 list 里「进来先 expire 一遍」同一顺序
  const all = readRows(TICKET_ORDER_KEY)
  if (expireUnpaid(all)) writeRows(TICKET_ORDER_KEY, all)

  const account = currentAccount()
  const rows = all.filter((r) => String(r.userAccount || '') === account).map(toConsumptionRow)
  // addTime 是 'yyyy-MM-dd HH:mm:ss'，定长格式直接按字符串倒序即可
  rows.sort((a, b) => String(b.addTime || '').localeCompare(String(a.addTime || '')))
  return ok(rows)
}

/**
 * 取出「全量行 + 当前账号那一行」。
 *
 * 先把已超时的单子取消掉再定位，是刻意的：付款和取消都要按同一个
 * 「未支付」状态位判，而超时正是把状态位从「未支付」改走的那一步。
 * 不先做的话，演示模式里一张超时的单还能被付掉 —— 超时形同虚设。
 */
function loadMyOrder(payload) {
  const all = readRows(TICKET_ORDER_KEY)
  if (expireUnpaid(all)) writeRows(TICKET_ORDER_KEY, all)

  const account = currentAccount()
  const row =
    all.find((r) => String(r.id) === String(payload.id) && String(r.userAccount || '') === account) || null
  return { all, row }
}

/** 请求体里 source 只能是 ticket；其余来源 mock 下没有对应的假订单表 */
function badSource(payload) {
  return payload.source && payload.source !== 'ticket'
    ? fail('mock 未实现的记录类型：' + payload.source, 404)
    : null
}

/**
 * 去支付。返回结构与后端一致：状态位不是「未支付」就报错。
 *
 * 请求体只说明「付哪一单」—— 与后端 ConsumptionController.pay 同一口径，
 * 不再核对手机号（那是原先的支付前表单，已去掉）。
 */
export function mockConsumptionPay(payload) {
  const wrongSource = badSource(payload)
  if (wrongSource) return wrongSource

  const { all, row } = loadMyOrder(payload)
  if (!row || (row.isPay || '未支付') !== '未支付') {
    return fail('订单不存在、已支付或已取消，无法支付')
  }

  row.isPay = '已支付'
  writeRows(TICKET_ORDER_KEY, all)
  return ok(null)
}

/** 取消订单。只认「未支付」，付过款或已取消的都拒绝 —— 与后端同一条判据 */
export function mockConsumptionCancel(payload) {
  const wrongSource = badSource(payload)
  if (wrongSource) return wrongSource

  const { all, row } = loadMyOrder(payload)
  if (!row || (row.isPay || '未支付') !== '未支付') {
    return fail('订单不存在、已支付或已取消，无法取消')
  }

  row.isPay = '已取消'
  writeRows(TICKET_ORDER_KEY, all)
  return ok(null)
}

/**
 * 去评价。真接口把评价写进 attraction_comment 这类评论表，而 mock 的评论是
 * buildComments() 现生成的（按 refId 造出来的），写进去也没地方显示 ——
 * 与其假装成功，不如把话说清楚，免得演示时以为评价功能坏了。
 */
export function mockConsumptionComment() {
  return fail('演示模式不写入评价：评论由本地数据生成，改 config.useMock = false 走真接口后可用')
}
