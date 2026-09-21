import http from '@/common/http'
import { USE_MOCK, mockHandle } from './mock'

/*
 * 页面唯一的请求出口。业务页面只 import '@/api/attraction' 这类模块，
 * 不要直接碰 $http —— 那样就把 mock 分支漏掉了。
 *
 * 两种模式返回的信封**完全一致**：{ data: { code, msg, data } }
 * （mock 是照 axios 的形状手搓的）。所以页面里永远写：
 *     const res = await listAttractions({ page: 1 })
 *     if (res.data.code === 0) { res.data.data.list ... }
 * 切开关时页面一行都不用改。这是最容易埋雷的地方 ——
 * 如果 mock 返回裸载荷，切到真接口后所有 res.data.data.xxx 会集体变成 undefined，
 * 表现为「页面空白但控制台只有一条 TypeError」，而 mock 期完全看不出来。
 */

/*
 * 这几个前缀背后已经有真后端了：mock 里留着分支也走不到，所以不受 USE_MOCK 影响，
 * 永远打真接口。**这份名单现在把应用里用到的每个前缀都收全了**（景点 / 餐厅 / 酒店 /
 * 用户），也就是说 USE_MOCK 对页面行为已经没有影响了 —— mock 那一层成了走不到的死代码。
 * 本轮先不删（要动 mock/index.js 与 mock/data/* 好几处，还会一起干掉 mockOff 这个逃生口），
 * 留着给将来「还没接后端的模块」兜底演示。
 *
 * 内容前缀与下单前缀必须成对列上，缺一个的后果见下：
 *   - attraction 少了 ticket_order：门票下单会被判成「这个模块还在 mock」，
 *     票写进本地假订单表而不是 ticket_order 表 —— 页面一切正常，
 *     个人中心却连一条待支付都查不到，很难往这上面想。
 *   - restaurant 少了 restaurant_reservation：餐厅预约同样只落 localStorage，
 *     用户预约完在个人中心一条都看不到。两处都是「内容对了、订单没对」。
 *
 * restaurant 一行连 restaurant_reservation 一起收进来，是为了让预约真正写库：
 * 只把内容前缀切真接口的话，餐厅列表是真的、预约却是假的，比整块都假更难查。
 * restaurant_comment 也必须跟上：卡片上的「N 条点评」取自餐厅表的 discuss_num，
 * 那是真库里的数字（如 33），评论列表要是还走 mock 就只出四五条假评论 ——
 * 数字和内容对不上，比整块都假更容易被当成 bug。
 *
 * hotel 这一组四个前缀必须一起收：hotel_info（酒店）+ room_type（客房）
 * + hotel_reservation（下单）+ hotel_comment（住客点评）。理由和 restaurant 那组一样 ——
 * 少一个就是「半真半假」。酒店这里尤其不能漏 hotel_reservation：列表页要同时拉
 * 酒店与客房两张表、在客户端按 hotel_name 把客房挂到酒店上，数据源一切真、
 * 预订却只落浏览器 localStorage 的话，「列表是真的、下单是假的」会在同一个模块里
 * 同时出现，比整块都假更难查。
 *
 * 判断请统一问 useMockFor()，不要再用 USE_MOCK —— 一旦某个模块从 mock 切到真接口，
 * 只有这个名单要改，页面和下单闸门都不用动。
 */
const REAL_PREFIXES = [
  'attraction',
  'attraction_type',
  'attraction_comment',
  'ticket_order',
  'restaurant',
  'restaurant_reservation',
  'restaurant_comment',
  // 酒店这一组（见上）
  'hotel_info',
  'hotel_reservation',
  'hotel_comment',
  'room_type',
  'user',
  // 攻略这一组：内容前缀 + 标签字典。攻略不再是静态页了（写攻略要真落库），
  // 标签的 chips 也来自真库 —— mock 里没有 travel_guide 这个数据源，
  // 漏一个就是整页 404（mock 找不到源会直接 fail，永远到不了后端）。
  'travel_guide',
  'travel_guide_tag',
  // 线路这一组：内容前缀 + 它下面的每日行程（travel_route_day 没有自己的接口，
  // 是跟着 /travel_route/search 与 /detail 一起回来的，所以这里只需要线路本身）。
  // mock 里同样没有 travel_route 这个数据源，漏了就是整页 404。
  'travel_route',
  // 线路咨询（游客↔导游对话）。mock 里没有这个数据源，漏了 mockHandle 会直接 fail，
  // 表现为「聊天窗一发消息就报错」—— 而列表页看着一切正常，很容易只往网络/跨域上想。
  'consult',
  // 报团下单。内容前缀是 travel_route（已在上面），但下单打的是订单表前缀
  // group_tour —— 两个必须成对：漏了它，报名会走 mock 的分支，报名信息只落
  // localStorage，个人中心的「待支付」一条都查不到，而页面看起来一切正常。
  'group_tour',
]

/** 这个前缀这次请求走 mock 吗。prefix 用「内容前缀」，不是订单表前缀 */
export function useMockFor(prefix) {
  return USE_MOCK && !REAL_PREFIXES.includes(prefix)
}

/*
 * 列表：GET {prefix}/{action}?<params>
 *
 * options 是原样透传给 axios 的配置，目前只用到 silent —— 轮询类请求（咨询窗口每 2 秒
 * 拉一次）失败时不该弹 toast，否则后端一挂就是满屏红字，把「连接断了」变成刷屏。
 * 它只管传输层提示，业务码（res.data.code）仍由调用方判断。
 */
export function get(prefix, action, params = {}, options = {}) {
  if (useMockFor(prefix)) return mockHandle('get', prefix, action, params)
  return http.get(`${prefix}/${action}`, { params, ...options })
}

/*
 * 详情：GET {prefix}/detail/{id}
 * 用 /detail/{id} 而不是 /info/{id} —— 后者需要登录，
 * 而且 /page 那条会在 getSession().getAttribute("table_name").toString() 上直接 NPE。
 */
export function detail(prefix, id) {
  if (useMockFor(prefix)) return mockHandle('get', prefix, 'detail', { id })
  return http.get(`${prefix}/detail/${id}`)
}

/*
 * 新增/下单：POST {prefix}/{action}，需要 header Token
 *
 * prefix 就是接口路径上那个前缀，而它随动作而变：读列表 / 详情用内容前缀
 * （attraction / hotel_info / restaurant），下单用的是订单表前缀（ticket_order /
 * hotel_reservation / restaurant_reservation）—— 后端是两张表、两个控制器，
 * 混用不会报错，只会安安静静写错表（把订单写进景点表就是这个下场）。
 * mock 侧由 mockHandle 把订单表前缀折回内容前缀去找数据源，两边对得上。
 */
export function post(prefix, action, body = {}, options = {}) {
  if (useMockFor(prefix)) return mockHandle('post', prefix, action, body)
  return http.post(`${prefix}/${action}`, body, options)
}

export { USE_MOCK }
