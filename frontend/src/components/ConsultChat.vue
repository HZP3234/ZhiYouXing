<template>
  <div class="consult">
    <header class="consult__head">
      <span class="consult__avatar" v-html="icons.user"></span>
      <div class="consult__who">
        <strong>{{ peerLabel }}</strong>
        <small v-if="subLabel">{{ subLabel }}</small>
      </div>
      <span class="consult__live" :class="{ 'is-off': offline }">
        {{ offline ? '未连接' : '在线咨询' }}
      </span>
      <!-- 关闭按钮由使用方决定要不要（弹窗里要，嵌页面里不要），
           放在头部而不是遮罩角上，避免和「在线咨询」标签叠在一起 -->
      <button v-if="closable" class="consult__close" type="button" @click="$emit('close')">×</button>
    </header>

    <div ref="scroller" class="consult__body">
      <p v-if="!messages.length" class="consult__hint">
        还没有消息。可以直接问，比如「这条线路 10 月还有位置吗」「能帮忙安排接送吗」。
      </p>

      <div
        v-for="m in messages"
        :key="m.id"
        class="consult__row"
        :class="{ 'is-self': m.from === selfRole }"
      >
        <div class="consult__bubble">{{ m.content }}</div>
        <span class="consult__time">{{ stamp(m.time) }}</span>
      </div>
    </div>

    <footer class="consult__composer">
      <textarea
        ref="input"
        v-model="draft"
        class="consult__input"
        rows="1"
        :maxlength="MAX_LEN"
        placeholder="输入你想咨询的内容，Enter 发送，Shift + Enter 换行"
        @input="autoGrow"
        @keydown="onKeydown"
      ></textarea>
      <button class="consult__send" type="button" :disabled="sending || !canSend" @click="send">
        {{ sending ? '发送中' : '发送' }}
      </button>
    </footer>
  </div>
</template>

<script>
/*
 * 游客 ↔ 导游的聊天窗口。两端共用这一个组件，靠 selfRole 决定气泡靠哪边。
 *
 * 消息来自两处，合并后展示：
 *   1. 服务端内存（只活在这次服务运行期间，重启即空）—— 决定「对方发的我能不能收到」；
 *   2. localStorage（按会话存，见 storageKey）—— 决定「聊过的话刷新后还在不在」。
 * 需求要「留存但不落数据库」，这两条合起来正好：库不建表，但历史不丢。
 * 合并按消息 id（后端发的 UUID）去重 —— 不能用序号，服务重启后序号会从头开始，
 * 会和 localStorage 里的旧消息撞上。
 *
 * 轮询间隔 2 秒。轮询与首次加载都走 silent，失败只把顶部标成「未连接」，
 * 不弹 toast —— 否则后端一断就是每 2 秒一条红字。发消息是用户主动触发的，不 silent。
 */
import { fetchConsultHistory, sendConsult } from '@/api/consult'
import { fmtDateTime, parseDate } from '@/common/format'

const POLL_MS = 2000

/* 单条消息长度上限，与后端 ConsultController.MAX_CONTENT 对齐。
   前端先拦一道，省得白跑一趟网络再被后端拒绝。 */
const MAX_LEN = 500

/* localStorage 里每个会话最多留多少条。聊天记录不该无限涨，
   超出部分从最早的一端丢 —— 近期对话才是「留存」真正要保的。 */
const MAX_KEEP = 300

const svg = (paths) =>
  `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${paths}</svg>`
const icons = {
  user: svg('<circle cx="12" cy="8" r="3.6"/><path d="M4.5 20a7.5 7.5 0 0 1 15 0"/>'),
}

const pad = (n) => String(n).padStart(2, '0')

export default {
  name: 'ConsultChat',
  props: {
    /** 线路id（travel_route.id） */
    routeId: { type: [Number, String], required: true },
    /** 线路名称，仅用于窗口副标题 */
    routeName: { type: String, default: '' },
    /** 我是谁："user"（游客）或 "guide"（导游） */
    selfRole: { type: String, default: 'user' },
    /** 对方账号。导游端必传（要知道回给哪个游客）；游客端不需要 */
    peerAccount: { type: String, default: '' },
    /** 对方显示名。导游端传游客账号；游客端留空则用线路导游名 */
    peerName: { type: String, default: '' },
    /** 是否在头部显示关闭按钮（弹窗场景用）。点了只是 $emit('close')，由使用方决定做什么 */
    closable: { type: Boolean, default: false },
  },
  emits: ['close'],
  data() {
    return {
      icons,
      MAX_LEN,
      messages: [],
      draft: '',
      sending: false,
      offline: false,
      guideName: '',
      timer: null,
    }
  },
  computed: {
    /*
     * localStorage 的键。游客端一条线路一个会话（线路的导游唯一，不会有第二个对话）；
     * 导游端要按游客分开，否则同一个导游跟两个游客的聊天会串成一条。
     */
    storageKey() {
      return this.selfRole === 'guide'
        ? `consult:${this.routeId}:${this.peerAccount}`
        : `consult:${this.routeId}`
    },
    peerLabel() {
      if (this.selfRole === 'guide') return this.peerName || '游客'
      return this.guideName || '线路导游'
    },
    subLabel() {
      const parts = []
      if (this.routeName) parts.push(`关于《${this.routeName}》`)
      if (this.selfRole === 'guide' && this.peerAccount) parts.push(this.peerAccount)
      return parts.join(' · ')
    },
    canSend() {
      return this.draft.trim().length > 0
    },
  },
  watch: {
    // 新消息进来就滚到底，和微信一样
    'messages.length'() {
      this.$nextTick(this.scrollToBottom)
    },
    // 同一个组件被复用到另一条会话（导游端切换游客）时，换键重载
    storageKey() {
      this.messages = []
      this.loadLocal()
      this.refresh()
      this.$nextTick(this.scrollToBottom)
    },
  },
  mounted() {
    this.loadLocal()
    this.refresh()
    this.timer = window.setInterval(this.refresh, POLL_MS)
  },
  beforeUnmount() {
    if (this.timer) window.clearInterval(this.timer)
  },
  methods: {
    /* ---------------- 取数与合并 ---------------- */

    loadLocal() {
      try {
        const raw = JSON.parse(localStorage.getItem(this.storageKey) || '[]')
        this.messages = Array.isArray(raw) ? raw.filter((m) => m && m.id) : []
      } catch {
        // 存的东西坏了就当没有，别让一条脏数据把整个窗口卡住
        this.messages = []
      }
      this.$nextTick(this.scrollToBottom)
    },

    persist() {
      try {
        const keep = this.messages.slice(-MAX_KEEP)
        localStorage.setItem(this.storageKey, JSON.stringify(keep))
      } catch {
        // 隐私模式下 localStorage 会直接抛，聊天本身不该因此用不了
      }
    },

    /** 把服务端返回的消息并进来：按 id 去重，按时间排序 */
    merge(incoming) {
      if (!Array.isArray(incoming) || !incoming.length) return
      const byId = new Map()
      for (const m of this.messages) byId.set(m.id, m)
      let added = false
      for (const m of incoming) {
        if (!m || !m.id || byId.has(m.id)) continue
        byId.set(m.id, m)
        added = true
      }
      if (!added) return
      this.messages = Array.from(byId.values()).sort((a, b) => a.time - b.time)
      this.persist()
    },

    async refresh() {
      const params = { routeId: this.routeId }
      if (this.selfRole === 'guide') params.userAccount = this.peerAccount
      try {
        const res = await fetchConsultHistory(params, { silent: true })
        if (res.data.code === 0) {
          this.offline = false
          const data = res.data.data || {}
          // 导游名由服务端给（游客端自己不知道线路的导游叫什么）
          if (data.guideName) this.guideName = data.guideName
          this.merge(data.messages)
        } else {
          // 业务码不对（如 token 过期、无权查看）不重试，直接标离线，避免刷屏
          this.offline = true
        }
      } catch {
        this.offline = true
      }
    },

    /* ---------------- 发送 ---------------- */

    async send() {
      const content = this.draft.trim()
      if (!content || this.sending) return
      this.sending = true
      const body = { routeId: this.routeId, content }
      if (this.selfRole === 'guide') body.userAccount = this.peerAccount
      try {
        const res = await sendConsult(body)
        if (res.data.code === 0) {
          this.draft = ''
          this.$nextTick(this.autoGrow)
          await this.refresh()
        } else {
          this.$message({ message: res.data.msg || '发送失败', type: 'error' })
        }
      } catch {
        // 传输层失败已由拦截器提示过，这里不再重复
      } finally {
        this.sending = false
      }
    },

    /* ---------------- 小工具 ---------------- */

    onKeydown(event) {
      // 输入法选词的回车不算发送（isComposing 在部分浏览器上晚一步置位，补一个 keyCode）
      if (event.key !== 'Enter' || event.shiftKey || event.isComposing || event.keyCode === 229) {
        return
      }
      event.preventDefault()
      this.send()
    },

    autoGrow() {
      const el = this.$refs.input
      if (!el) return
      el.style.height = 'auto'
      el.style.height = `${Math.min(el.scrollHeight, 96)}px`
    },

    scrollToBottom() {
      const el = this.$refs.scroller
      if (el) el.scrollTop = el.scrollHeight
    },

    /** 当天只显示 HH:mm，跨天补上日期 —— 聊到第二天时能看出分界 */
    stamp(t) {
      const d = parseDate(t)
      if (!d) return ''
      const now = new Date()
      if (d.toDateString() === now.toDateString()) {
        return `${pad(d.getHours())}:${pad(d.getMinutes())}`
      }
      return fmtDateTime(d)
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.consult {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: var(--card);
}

/* ---------------- 头部 ---------------- */
.consult__head {
  display: flex;
  flex: none;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--line);
  background: linear-gradient(180deg, #fbfdff, #f4f8fd);
}
.consult__avatar {
  flex: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--brand-soft);
  color: var(--brand);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.consult__avatar :deep(svg) {
  width: 20px;
  height: 20px;
}
.consult__who {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
  min-width: 0;
}
.consult__who strong {
  color: var(--ink);
  font-size: 15px;
}
.consult__who small {
  color: var(--ink-3);
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.consult__live {
  margin-left: auto;
  flex: none;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 12px;
}
.consult__live.is-off {
  background: var(--sand-deep);
  color: var(--ink-3);
}
.consult__close {
  flex: none;
  margin-left: 8px;
  border: 0;
  background: transparent;
  color: var(--ink-3);
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}
.consult__close:hover {
  color: var(--ink);
}

/* ---------------- 消息区 ---------------- */
.consult__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 18px;
  background: var(--sand);
}
.consult__hint {
  margin: 12px auto;
  max-width: 320px;
  color: var(--ink-3);
  font-size: 13px;
  line-height: 1.8;
  text-align: center;
}
.consult__row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 14px;
}
.consult__row.is-self {
  align-items: flex-end;
}
.consult__bubble {
  max-width: 78%;
  padding: 10px 14px;
  border-radius: 14px 14px 14px 4px;
  background: #fff;
  border: 1px solid var(--line);
  color: var(--ink);
  font-size: 14px;
  line-height: 1.7;
  /* 用户手打的换行要留住，同时长串英文/链接能断行不撑破气泡 */
  white-space: pre-wrap;
  word-break: break-word;
}
.consult__row.is-self .consult__bubble {
  border-color: transparent;
  border-radius: 14px 14px 4px 14px;
  background: linear-gradient(135deg, var(--brand-light), var(--brand));
  color: #fff;
}
.consult__time {
  margin-top: 4px;
  color: var(--ink-3);
  font-size: 11px;
}

/* ---------------- 输入区 ---------------- */
.consult__composer {
  display: flex;
  flex: none;
  gap: 10px;
  padding: 12px 14px;
  border-top: 1px solid var(--line);
  background: var(--card);
}
.consult__input {
  flex: 1;
  min-height: 40px;
  max-height: 96px;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--sand);
  color: var(--ink);
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: 0;
  transition: border-color 0.16s ease, background 0.16s ease;
}
.consult__input:focus {
  border-color: var(--brand-light);
  background: #fff;
}
.consult__send {
  flex: none;
  align-self: flex-end;
  height: 40px;
  padding: 0 20px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--brand-light), var(--brand));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: opacity 0.16s ease, transform 0.16s ease;
}
.consult__send:hover:not(:disabled) {
  transform: translateY(-1px);
}
.consult__send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
