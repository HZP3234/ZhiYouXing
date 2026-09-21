import { get, detail, post } from './client'

const PREFIX = 'attraction'

/*
 * 下单端点是订单表，不是景点表！
 * 后端 /attraction/save 收的是 AttractionEntity（新增一个景点），
 * 订单要在 /ticket_order/save（TicketOrderController）上新增。
 * 两者前缀不同，网关路由（/api/ticket_order/**）与直连代理表里都单独列了它，
 * client 的 REAL_PREFIXES 里两个前缀也都要在 —— 少列 ticket_order 会被判成
 * 还要走 mock，下单就落进本地假订单表，真库一条都不写。
 */
const ORDER_PREFIX = 'ticket_order'

/*
 * 参数直接透传给后端 MPUtil，它认这几种：
 *   attractionType=自然风光          实体字段，字符串走 LIKE、数字走等值
 *   ticketPriceStart=100&ticketPriceEnd=200   闭区间
 *   sort=ticketPrice&order=desc      sort 传 camelCase 列名，MPUtil 自己转 snake_case
 *   page=1&limit=9
 * 注意 indexQueryCondition 后端**不认**（MPUtil 只认实体字段名），
 * 首页搜索带过来的关键词必须由列表页翻译成 attractionName= 再发。
 */

/** 列表。@IgnoreAuth 免登录 */
export const listAttractions = (params) => get(PREFIX, 'list', params)

/** 详情。@IgnoreAuth 免登录 */
export const getAttraction = (id) => detail(PREFIX, id)

/** 景点分类字典，给筛选 chips 用。必须接口驱动，不能硬编码 */
export const listAttractionTypes = () => get('attraction_type', 'list')

/**
 * 下单。需要 header Token。
 * body 字段必须逐字对齐 TicketOrderEntity：
 * orderNo / attractionName / image / attractionType / ticketPrice / quantity /
 * totalAmount / userAccount / userName / contactPhone / purchaseTime / isPay
 */
export const saveTicketOrder = (body) => post(ORDER_PREFIX, 'save', body)
