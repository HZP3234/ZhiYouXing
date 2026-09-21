import { post } from './client'

const PREFIX = 'group_tour'

/*
 * 报团下单（游客侧）。
 *
 * 注意这个前缀有两个身份：管理端的「报团信息」那一页走的是 /tour_guide/group_tour/**
 * （导游的 scoped CRUD，见后端 TourGuideScopeController），游客报名走的是这里的
 * /group_tour/save（见后端 GroupTourController.createOrder）。
 * 两条路径前缀不同名，所以不会被互相顶掉。
 *
 * 请求体只有三个字段：{ routeId, signupCount, contactPhone }。
 * 线路名 / 图片 / 费用 / 出发日期 / 导游工号由服务端从线路表取，用户账号与姓名
 * 由服务端从登录态与 user_identity 取 —— 前端发了也会被覆盖，写了反而让人
 * 以为这些值说了算（尤其金额：让它由调用方决定等于可以一块钱报团）。
 */

/**
 * 报名参团。需要登录（接口没有 @IgnoreAuth，没带 Token 直接 401）。
 * 返回 { id, amount }：id 是落库主键，拿去调 payConsumption({source:'group', id}) 付款。
 *
 * 被拒绝的几种业务情况（都是 code!=0，msg 里带原因，直接展示给用户即可）：
 * 未实名认证 / 名额不足 / 线路不存在 / 报名人数不合法。
 */
export const saveGroupTour = (body) => post(PREFIX, 'save', body)
