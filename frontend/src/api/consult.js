import { get, post } from './client'

const PREFIX = 'consult'

/*
 * 线路咨询：游客 ↔ 线路导游的对话。
 *
 * 三个接口都**要登录**（不同于 /travel_route/search 那种 @IgnoreAuth）——
 * 后端要靠会话里的 username 判断「谁在说话」，没有 token 直接 401。
 * 游客端传的 userAccount 会被后端忽略、一律取自己的账号；只有导游端需要它来指定回给谁。
 *
 * 消息不落库：后端只在内存里中转，服务重启即清空。前端负责把历史存 localStorage
 * （见 components/ConsultChat.vue），那是「留存」的实际落点。
 */

/** 发一条消息。body：{ routeId, content, userAccount? } */
export const sendConsult = (body) => post(PREFIX, 'send', body)

/**
 * 取会话消息并把本方标已读。params：{ routeId, userAccount? }
 * options 用来传 silent —— 轮询调用必传，否则断线时每 2 秒弹一条红字。
 */
export const fetchConsultHistory = (params, options) => get(PREFIX, 'history', params, options)

/** 导游端专用：自己名下所有线路的咨询会话列表（含未读数）。轮询同样传 silent */
export const fetchConsultConversations = (options) => get(PREFIX, 'conversations', {}, options)
