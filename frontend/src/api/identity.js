import { get, post } from './client'

/*
 * 实名认证（user 服务，网关 /api/user/**）。
 *
 * client 的 get/post 拼的是 `{prefix}/{action}`，所以 prefix 传 'user'、
 * action 带子路径。这样 mock 与真接口走同一个出口，切开关页面不用改。
 *
 * 接口返回的证件号永远是脱敏的（前 6 后 4），完整号码不出后端：
 * 认证之后前端不再持有它，订单表里的证件号由服务端从 user_identity 抄。
 */

const PREFIX = 'user'

/** 当前登录用户的认证状态：{verified, realName, idCardMasked, verifyTime} */
export const getIdentity = () => get(PREFIX, 'identity/detail')

/**
 * 提交认证。只需 {realName, idCard}，归属由后端从登录态取。
 * 未通过时返回 code!=0 与具体原因（姓名不合法 / 身份证号不正确 / 证件号已被占用）。
 */
export const saveIdentity = (body) => post(PREFIX, 'identity/save', body)
