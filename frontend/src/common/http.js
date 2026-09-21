import axios from 'axios'
import { ElMessage } from 'element-plus'
import config from '@/config/config'

/**
 * 全局 axios 实例，挂在 app.config.globalProperties.$http 上。
 * 代码生成器的页面里所有请求都走 this.$http。
 */
const http = axios.create({
  baseURL: config.baseUrl,
  timeout: 15000,
})

// 请求拦截：带上登录后拿到的 token（后端 AuthorizationInterceptor 认这个头）
http.interceptors.request.use(
  (cfg) => {
    const token = localStorage.getItem('frontToken')
    if (token) {
      cfg.headers.set('Token', token)
    }
    return cfg
  },
  (error) => Promise.reject(error),
)

/*
 * 响应拦截只处理「传输层」失败（断网、超时、5xx）。
 * 业务码一律交给调用方自己判断——页面里普遍写了
 * `if (res.data.code == 0) ... else this.$message.error(res.data.msg)`，
 * 这里再弹一次会重复提示。
 *
 * 注意后端有两套信封，成功码不一样：
 *   - 代码生成器那套 R        → code 0，错误信息在 msg
 *   - common 的 Result        → code 200，错误信息在 message（agent 接口用的这套）
 * 判成功前先看清接口返回的是哪套。
 */
http.interceptors.response.use(
  (res) => res,
  (error) => {
    /*
     * 调用方传了 silent 就只 reject、不弹提示。
     * 给「失败也无所谓」的请求用 —— 比如 index.vue 的 config/list（轮播图），
     * 它自己写了 .catch() 兜底，是想静默降级的；拦截器在这里抢先弹一条红字，
     * 等于把它的意图废掉了。网关没起的时候每个 /index 页面都会挨一次。
     */
    if (error.config?.silent) return Promise.reject(error)

    /*
     * 主动取消（页面离开时 AbortController.abort）不是故障：它同样没有 response，
     * 落到下面会被当成「无法连接后端」弹一条红字，把页面自己的静默取消变成惊吓。
     */
    if (axios.isCancel(error)) return Promise.reject(error)

    const status = error.response?.status
    let msg = error.message
    if (status === 401) {
      msg = '登录已失效，请重新登录'
      localStorage.removeItem('frontToken')
    } else if (status === 404) {
      msg = '接口不存在：' + (error.config?.url || '')
    } else if (error.code === 'ECONNABORTED') {
      msg = '请求超时，请检查网关是否已启动'
    } else if (!error.response) {
      msg = '无法连接后端（网关 :8080 是否已启动？）'
    } else if (status >= 500) {
      /*
       * 网关没起时 Vite 代理会回 500（ECONNREFUSED），只显示 axios 的
       * "Request failed with status code 500" 根本看不出是后端没起。
       */
      msg = `${status}：后端没有正常响应，网关 :8080 与对应微服务是否都已启动？`
    }
    /* 页面想在正文里复述这句话时（agent 的错误气泡）直接取，别再去猜 axios 文案 */
    error.friendlyMessage = msg
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

export default http
