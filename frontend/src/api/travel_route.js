import { get } from './client'

const PREFIX = 'travel_route'

/*
 * 线路页。
 *
 * 为什么**列表不走 /list 而走 /search**：/list 是代码生成器产物，参数经 MPUtil 裸反射
 * 映射到实体字段上，而 keyword 要同时命中线路名称 / 景点名称 / 起点 / 终点 / 途经路段，
 * 这不是实体字段，落到 /list 上会被静默忽略 —— 传了等于没传，页面看着正常但筛选没生效。
 *（攻略页踩过同一个坑，见 api/travel_guide.js。）
 *
 * 返回信封与别处完全一致：res.data.data.list / .totalCount（PageUtils）。
 * 每条线路还多一个 daily 数组（每日行程，按 day 升序，没录就是空数组）——
 * 详情抽屉直接拿它渲染，不为「每日安排」单独发一次请求。
 */

/**
 * 列表页唯一入口。@IgnoreAuth 免登录。
 *
 * params 认这几种（null / 空串一律别发，后端按「没传」处理）：
 *   keyword=青海湖                            线路名/景点名/起点/终点/途经路段里出现即命中，不用加 %
 *   days_start=4&days_end=6                   行程天数区间，只传一端就是单边限制
 *   routeFee_start=2000&routeFee_end=3500     线路费用区间，同上
 *   page=1&limit=6
 *   sort=routeFee&order=asc                   排序（sort 传 camelCase，后端自己转下划线）
 *
 * 注意区间参数用的是 _start / _end 后缀，那是后端 MPUtil.between 认的写法；
 * 写成 daysFrom / daysMin 之类一律无效，而且同样不会报错。
 */
export const searchRoutes = (params) => get(PREFIX, 'search', params)
