import { get, detail, post } from './client'

const PREFIX = 'restaurant'

/*
 * 预约端点是预约表，不是餐厅表：/restaurant/save 收的是 RestaurantEntity（新增餐厅），
 * 预约要在 /restaurant_reservation/save（RestaurantReservationController）上新增。
 */
const ORDER_PREFIX = 'restaurant_reservation'

/*
 * 餐厅是三个模块里字段最少的一个：
 *   - 主表**没有分类字段**（restaurant_type 只在预约表上，且可为空），所以没有分类筛选；
 *   - avg_cost 是 varchar，真实库存的是「人均消费1」这种文案，不是数字，
 *     所以它不参与任何计算，后端对它排序也是字典序（'100' < '88'），
 *     列表页因此不提供「按人均排序」。
 * 可用参数实际上只有：restaurantName / sort / order / page / limit
 */

/** 列表。@IgnoreAuth 免登录 */
export const listRestaurants = (params) => get(PREFIX, 'list', params)

/** 详情。@IgnoreAuth 免登录 */
export const getRestaurant = (id) => detail(PREFIX, id)

/**
 * 预约。需要 header Token。
 * body 对齐 RestaurantReservationEntity：
 * restaurantName / restaurantImage / dinerCount / reservationTime / diningRemark /
 * userAccount / userName / contactPhone / auditStatus / auditReply
 *
 * 刻意不提交 restaurantType：主表没有这个字段，硬凑一个值只会污染数据。
 * 这一列在库里可为空，ValidatorUtils 的必填校验也已被注释掉，不填不会拦。
 */
export const saveRestaurantReservation = (body) => post(ORDER_PREFIX, 'save', body)
