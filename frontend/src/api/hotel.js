import { get, detail, post } from './client'

const HOTEL_PREFIX = 'hotel_info'
const ROOM_PREFIX = 'room_type'

/*
 * 预订端点是预订表，不是酒店表：/hotel_reservation/save 收的是 HotelReservationEntity，
 * 由用户在酒店下单页发起（酒店前台没有新增预订这个口子）。
 */
const ORDER_PREFIX = 'hotel_reservation'

/*
 * 酒店这一块是「酒店 / 客房」两张表：
 *
 *   hotel_info —— 一家酒店一行（名称/地址/图片/介绍 + 审核三列）。游客端
 *                 /hotel_info/list、/detail/{id} 只放行 audit_status = 已通过。
 *   room_type  —— 客房（room_name / room_type / 价格 / 数量 ...），归属列 hotel_name。
 *                 /room_type/list、/detail/{id} 同样只放行已过审酒店的客房。
 *
 * 但列表页要的是「酒店卡片 + 每家的房型」，得两个接口合起来看：拉全量酒店 + 全量客房，
 * 在客户端按 hotel_name 把客房挂到酒店上，再由房型/价格/关键词筛客房、反推该出现哪些酒店
 * （见 pages/hotel_info/hotelInfoList.vue）。
 *
 * limit 必须一次取完：分页会让同一家酒店的客房被拆到两页，聚出两个各残缺一半的「酒店」。
 * 真库现在 44 家酒店 / 132 个客房，500 绰绰有余；哪天客房总数超过这个上限，正确的做法是
 * 让后端提供一个按 hotel_name 分组的接口，而不是继续往上调 limit。
 *
 * params 收的是筛选项与排序，page 与 limit 由这里锁定，调用方不要传 —— 传了会被覆盖。
 */
const FETCH_LIMIT = 500

const fullFetch = (prefix, params) => get(prefix, 'list', { ...params, page: 1, limit: FETCH_LIMIT })

/** 酒店列表（只含已过审的）。@IgnoreAuth 免登录 */
export const listHotels = (params = {}) => fullFetch(HOTEL_PREFIX, params)

/** 客房列表（只含已过审酒店的客房）。@IgnoreAuth 免登录 */
export const listRooms = (params = {}) => fullFetch(ROOM_PREFIX, params)

/** 客房详情。@IgnoreAuth 免登录 */
export const getRoom = (id) => detail(ROOM_PREFIX, id)

/**
 * 按酒店名取那一家酒店（客房实体上只有 hotel_name，地址/介绍/热度都在酒店表里）。
 * 取不到返回 null：酒店没过审、或名字对不上。
 *
 * hotelName 在后端是 **LIKE** 匹配（「青海湖大酒店」会连「青海湖大酒店式公寓」一起捞出来），
 * 所以这里必须再精确比对一次，否则「同店其他房型」那类位置会混进别的酒店。
 */
export async function getHotelByName(name) {
  if (!name) return null
  const res = await listHotels({ hotelName: name })
  const list = res.data.code === 0 ? res.data.data.list || [] : []
  return list.find((h) => h.hotelName === name) || null
}

/**
 * 预订。需要 header Token。
 *
 * body 只发「这单订了什么」，逐字对齐 HotelReservationEntity 的前半截：
 * reservationNo / roomName / roomImage / roomPrice / roomCount / stayDays /
 * totalAmount / hotelName / reservationTime / contactPhone
 *
 * userAccount / userName / idCard / isPay **不发**：那四个字段决定「房是谁订的、谁入住、
 * 付没付钱」，后端一律以登录态与 user_identity 为准并覆盖请求体
 * （见 HotelReservationController.createOrder）。发了也会被忽略，写了反而让人以为说了算。
 *
 * 返回体只有 { id, reservationNo }：id 是付款要用的落库主键
 * （POST /user/consumption/pay，source 用 'hotel'），单号给回执页显示。
 *
 * 注意 roomCount 在这里是「订几间」，而客房详情里的 roomCount 是「剩余几间」，同名不同义。
 */
export const saveHotelReservation = (body) => post(ORDER_PREFIX, 'save', body)
