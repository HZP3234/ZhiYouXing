import config from '@/config/config'
import { mockPage, mockList, mockDetail, mockSave, ok, fail, delay } from './engine'
import { ATTRACTIONS, ATTRACTION_TYPE_ROWS } from './data/attraction'
import { HOTEL_ROOMS, ROOM_TYPE_ROWS } from './data/hotel'
import { RESTAURANTS } from './data/restaurant'
import { buildComments } from './data/comments'
import {
  mockIdentityDetail,
  mockIdentitySave,
  mockTicketOrderSave,
  mockConsumptionList,
  mockConsumptionPay,
  mockConsumptionCancel,
  mockConsumptionComment,
} from './user'

/*
 * ============ mock 的兜底开关 ============
 *
 * 它只管「没被 client.js 的 REAL_PREFIXES 收走」的前缀，现在只剩酒店
 * （hotel_info / hotel_reservation / room_type / hotel_comment）。
 * attraction、ticket_order、user、以及餐厅那一组（restaurant /
 * restaurant_reservation / restaurant_comment）都已经在名单里，两种模式下都直接打后端。
 *
 * 所以下面 user 那一段和 attraction 的下单分支**当前走不到**：它们是上一轮
 * 「后端没起也能演示完整链路」的实现，保留着是因为那几个前缀一旦从
 * REAL_PREFIXES 里拿掉，整条链就得重新写一遍。要真用不上，删掉更干净。
 *
 * 临时全切真库不用改代码：浏览器控制台执行 localStorage.setItem('mockOff','1') 再刷新。
 */
export const USE_MOCK = config.useMock && localStorage.getItem('mockOff') !== '1'

/*
 * 每个模块的列表查询选项，对齐后端 MPUtil 的语义：
 *   rangeMap    xxxStart / xxxEnd 映射到哪一列（景点是门票价，酒店是房价）
 *   nameField   首页搜索框带来的 indexQueryCondition 落到哪一列
 *   defaultSort 不传 sort 时的排序列
 * 餐厅既没有价格区间参数、也没有分类字段，所以这里只有 nameField。
 */
const LIST_OPTS = {
  attraction: { rangeMap: { ticketPrice: 'ticketPrice' }, nameField: 'attractionName', defaultSort: 'clickTime' },
  hotel_info: { rangeMap: { roomPrice: 'roomPrice' }, nameField: 'roomName', defaultSort: 'clickTime' },
  restaurant: { nameField: 'restaurantName', defaultSort: 'addTime' },
}

/** 三个模块各自的：数据源 + 下单落到哪张表（表名和后端一致，便于对照） */
const SOURCES = {
  attraction: { rows: ATTRACTIONS, table: 'ticket_order' },
  hotel_info: { rows: HOTEL_ROOMS, table: 'hotel_reservation' },
  restaurant: { rows: RESTAURANTS, table: 'restaurant_reservation' },
}

/*
 * 下单走的是订单表前缀（hotel_reservation），而数据源和假订单表都按内容前缀
 * （hotel_info）索引，两者不同名。从 SOURCES 的 table 列反查，避免同一组
 * 对照关系在调用方和这里各写一遍 —— 那边漏改一个就会掉到下面的 404 分支。
 */
const CONTENT_BY_TABLE = Object.fromEntries(
  Object.entries(SOURCES).map(([content, { table }]) => [table, content]),
)

/** 分类字典。真实接口返回的是「景点类型1..8」这类占位值，但页面是接口驱动的，不会坏 */
const DICTS = {
  attraction_type: ATTRACTION_TYPE_ROWS,
  room_type: ROOM_TYPE_ROWS,
}

/** 三张评论表结构完全一样，前缀不同而已。餐厅评论已切真接口，这里只剩酒店走得到 */
const COMMENT_TABLES = ['attraction_comment', 'hotel_comment', 'restaurant_comment']

/**
 * 按 (method, prefix, action) 分发。参数和返回值都照抄后端，
 * 所以 client.js 在两种模式下能给出完全一样的信封。
 */
export async function mockHandle(method, prefix, action, payload = {}) {
  await delay()

  // 下单前缀先归一到内容前缀，下面的数据源、假订单表、评论表都按内容前缀索引
  prefix = CONTENT_BY_TABLE[prefix] ?? prefix

  /*
   * user 服务：实名认证与个人中心的消费记录。
   *
   * 这两件事不属于「有本地数据的三个模块」，但同样走 client.js，所以在 mock 下
   * 也得有分支 —— 少了它，默认模式里「购票 → 实名 → 支付 → 个人中心看到已支付」
   * 这条链会在最后两步断掉（先 404，个人中心空白）。
   * action 带子路径是因为 client 拼的是 `{prefix}/{action}`。
   */
  if (prefix === 'user') {
    if (method === 'get' && action === 'identity/detail') return mockIdentityDetail()
    if (method === 'post' && action === 'identity/save') return mockIdentitySave(payload)
    if (method === 'get' && action === 'consumption/list') return mockConsumptionList()
    if (method === 'post' && action === 'consumption/pay') return mockConsumptionPay(payload)
    if (method === 'post' && action === 'consumption/cancel') return mockConsumptionCancel(payload)
    if (method === 'post' && action === 'consumption/comment') return mockConsumptionComment()
    return fail(`mock 未实现的接口：${method} ${prefix}/${action}`, 404)
  }

  // 分类字典：xxx_type/list 返回全量，不分页
  if (method === 'get' && action === 'list' && DICTS[prefix]) {
    return ok(mockList(DICTS[prefix]))
  }

  // 评论：xxx_comment/list?refId=<id>，按需生成后照常分页
  if (method === 'get' && action === 'list' && COMMENT_TABLES.includes(prefix)) {
    const rows = buildComments(payload.refId, prefix)
    return ok(
      mockPage(rows, { ...payload, sort: payload.sort || 'addTime', order: payload.order || 'desc' }, { defaultSort: 'addTime' }),
    )
  }

  const source = SOURCES[prefix]
  if (!source) return fail(`mock 未实现的接口：${method} ${prefix}/${action}`, 404)

  if (method === 'get' && action === 'list') {
    return ok(mockPage(source.rows, payload, LIST_OPTS[prefix] || {}))
  }

  if (method === 'get' && action === 'detail') {
    const row = mockDetail(source.rows, payload.id)
    return row ? ok(row) : fail('记录不存在', 404)
  }

  if (method === 'post' && action === 'save') {
    /*
     * 门票下单多一道实名认证的门，并且 userAccount / userName / idCard / isPay
     * 由 mock 按「服务端」的口径写死（前端已经不发这四个字段了）。
     */
    if (prefix === 'attraction') return mockTicketOrderSave(payload)
    // 落 localStorage：既让下单闭环有结果，也方便直接核对字段名有没有写错
    return ok(mockSave(source.table, payload))
  }

  return fail(`mock 未实现的接口：${method} ${prefix}/${action}`, 404)
}
