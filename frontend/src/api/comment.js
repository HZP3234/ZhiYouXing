import { get } from './client'

/*
 * 三张评论表（attraction_comment / hotel_comment / restaurant_comment）
 * 结构完全一样（refId / userId / avatarUrl / nickname / content / score / reply / addTime），
 * 只有表名不同。所以详情页共用一个 ReviewList 组件、一个请求函数，
 * 由 commentPrefix 决定查哪张表 —— 没必要复制三份。
 */

/**
 * @param {string} commentPrefix  'attraction_comment' | 'hotel_comment' | 'restaurant_comment'
 * @param {number|string} refId   关联的记录 id
 */
export const listComments = (commentPrefix, refId, params = {}) =>
  get(commentPrefix, 'list', { refId, sort: 'addTime', order: 'desc', limit: 20, ...params })
