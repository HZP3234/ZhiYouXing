import { get, post } from './client'

/*
 * 个人中心的消费记录（user 服务）。
 *
 * 单独抽成模块而不是像以前那样在 center.vue 里直接 this.$http：
 * 直接打 axios 会绕过 mock 分支，默认模式（config.useMock = true）下
 * 「下单 → 支付 → 个人中心看到已支付」这条链的最后一步就没数据可显示。
 */

const PREFIX = 'user'

/** 消费记录列表。账号由后端从登录态取，不接受参数 */
export const listConsumption = () => get(PREFIX, 'consumption/list')

/** 去支付：{source: 'ticket'|'hotel'|'group', id} */
export const payConsumption = (body) => post(PREFIX, 'consumption/pay', body)

/**
 * 取消订单：{source, id}。只有「未支付」的单能取消，
 * 付过款或已经取消的会回一条业务错误（后端只认未支付那一态）。
 */
export const cancelConsumption = (body) => post(PREFIX, 'consumption/cancel', body)

/**
 * 去评价：{source, id, score, content}。
 * 评价会写进景点/酒店/线路的详情页评论区，mock 里没有对应的评论表可写，
 * 所以 mock 分支只回一条「演示模式不支持」的业务错误（见 mock/user.js）。
 */
export const commentConsumption = (body) => post(PREFIX, 'consumption/comment', body)
