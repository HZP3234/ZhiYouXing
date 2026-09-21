<template>
  <section class="chat">
    <div class="chat-panel">
      <header class="chat-head">
        <span class="chat-head__mark" v-html="icons.sparkle"></span>
        <div class="chat-head__text">
          <h1 class="chat-head__title">AI 行程助手</h1>
          <p class="chat-head__sub">说清目的地、天数与预算，帮你排一份带三档预算的行程</p>
        </div>
        <button v-if="messages.length" class="chat-head__clear" type="button" @click="clearChat">
          清空对话
        </button>
      </header>

      <div ref="scroller" class="chat-body">
        <div v-if="!messages.length && !loading" class="welcome">
          <span class="welcome__mark" v-html="icons.sparkle"></span>
          <h2 class="welcome__title">想去哪儿？</h2>
          <p class="welcome__desc">
            把出发地、天数、同行人数和预算写进去，助手会给出行程概要，以及经济 / 舒适 / 品质三档方案。
          </p>
        </div>

        <div
          v-for="(message, index) in messages"
          :key="index"
          class="msg"
          :class="`msg--${message.role}`"
        >
          <span
            v-if="message.role === 'assistant'"
            class="msg__avatar"
            v-html="icons.sparkle"
          ></span>
          <div class="msg__main">
            <p v-if="message.role === 'user'" class="msg__bubble">{{ message.text }}</p>

            <template v-else>
              <div v-if="message.error" class="msg__error">
                <p>{{ message.text }}</p>
                <button class="msg__retry" type="button" :disabled="loading" @click="retry(index)">
                  {{ loading ? '生成中' : '重试' }}
                </button>
              </div>
              <template v-else>
                <div class="msg__doc" v-html="message.html"></div>
                <div class="msg__tools">
                  <button class="msg__tool" type="button" @click="copy(message, index)">
                    {{ copiedIndex === index ? '已复制' : '复制' }}
                  </button>
                </div>
              </template>
            </template>
          </div>
        </div>

        <div v-if="loading" class="msg msg--assistant">
          <span class="msg__avatar" v-html="icons.sparkle"></span>
          <div class="msg__main">
            <div class="thinking">
              <span class="thinking__dots"><i></i><i></i><i></i></span>
              <span class="thinking__text">正在规划行程，通常需要 30~60 秒，先别走开</span>
            </div>
          </div>
        </div>
      </div>

      <div class="composer">
        <div class="composer__box">
          <textarea
            ref="input"
            v-model="draft"
            class="composer__input"
            rows="1"
            :disabled="loading"
            placeholder="例如：从天津出发，青海湖 5 天，两个人，预算 6000 左右"
            @input="autoGrow"
            @keydown="onKeydown"
          ></textarea>
          <button
            class="composer__send"
            type="button"
            :disabled="loading || !canSend"
            :title="loading ? '正在生成' : '发送'"
            @click="send()"+
          >
            <span v-if="loading" class="composer__spinner"></span>
            <span v-else v-html="icons.send"></span>
          </button>
        </div>
        <div class="composer__foot">
          <span>助手会记住本对话的最近几轮，可以直接追问「那改成 7 天」</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { renderMarkdown } from '@/common/markdown'

const svg = (paths) =>
  `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${paths}</svg>`

const icons = {
  sparkle: svg(
    '<path d="M11 3.2l1.7 4.4 4.4 1.7-4.4 1.7L11 15.4 9.3 11 4.9 9.3l4.4-1.7L11 3.2z"/><path d="M17.6 14.4l.9 2.3 2.3.9-2.3.9-.9 2.3-.9-2.3-2.3-.9 2.3-.9.9-2.3z"/>',
  ),
  send: svg('<path d="M12 19.5V4.8M6 10.8L12 4.8l6 6"/>'),
}

const REQUEST_TIMEOUT = 3 * 60 * 1000

const STORAGE_KEY = 'frontAgentChat'
const MAX_KEEP = 30

/**
 * 每次请求带回的上下文条数。一条提问约 1000 字、一份方案约 1200 字，
 * 带 6 条（约 3 轮）足够支撑「那改成 7 天」这类追问，再多只是徒增 token。
 */
const HISTORY_KEEP = 6

export default {
  data() {
    return {
      icons,
      draft: '',
      loading: false,
      messages: [],
      copiedIndex: -1,
    }
  },
  computed: {
    canSend() {
      return this.draft.trim().length > 0
    },
  },
  watch: {
    messages: {
      deep: true,
      handler(list) {
        try {
          localStorage.setItem(
            STORAGE_KEY,
            JSON.stringify(
              list.slice(-MAX_KEEP).map(({ role, text, error }) => ({
                role,
                text,
                error: Boolean(error),
              })),
            ),
          )
        } catch {
          // 存不下就算了，不影响当前这次对话
        }
      },
    },
  },
  created() {
    /* 在途请求的取消器，非响应式，故意不放进 data */
    this.controller = null
  },
  mounted() {
    this.restore()
    this.autoGrow()
  },
  beforeUnmount() {
    /* 离开本页就掐掉还没返回的请求，别让答案落到已经关掉的会话里 */
    this.abort()
  },
  methods: {
    restore() {
      let saved
      try {
        saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
      } catch {
        localStorage.removeItem(STORAGE_KEY)
        return
      }
      if (!Array.isArray(saved)) return
      this.messages = saved.slice(-MAX_KEEP).map((item) => {
        const error = Boolean(item.error)
        const text = item.text || ''
        const role = item.role === 'user' ? 'user' : 'assistant'
        return {
          role,
          text,
          error,
          html: role === 'assistant' && !error ? renderMarkdown(text) : '',
        }
      })
      this.$nextTick(this.scrollToBottom)
    },

    async send(preset) {
      const requirement = (typeof preset === 'string' ? preset : this.draft).trim()
      if (!requirement || this.loading) return

      this.messages.push({ role: 'user', text: requirement })
      this.draft = ''
      this.$nextTick(() => {
        this.autoGrow()
        this.scrollToBottom()
      })
      await this.request(requirement)
    },

    /**
     * 取此前几轮对话作为上下文。出错的那几条不算，最后一条若就是本次要发的需求也不重复带。
     */
    buildHistory(requirement) {
      const list = this.messages.filter((message) => !message.error)
      const last = list[list.length - 1]
      if (last && last.role === 'user' && last.text === requirement) list.pop()
      return list
        .slice(-HISTORY_KEEP)
        .map(({ role, text }) => ({ role, content: text }))
    },

    async request(requirement) {
      if (this.loading) return

      this.loading = true
      this.copiedIndex = -1
      this.$nextTick(this.scrollToBottom)

      const controller = new AbortController()
      this.controller = controller
      try {
        const res = await this.$http.post(
          'agent/travel/plan',
          { requirement, history: this.buildHistory(requirement) },
          { timeout: REQUEST_TIMEOUT, signal: controller.signal },
        )
        const body = res.data || {}
        const content = body.data && body.data.content
        if (body.code === 200 && content) {
          this.messages.push({ role: 'assistant', text: content, html: renderMarkdown(content) })
        } else {
          this.pushError(body.message || '助手没有返回内容，请重试')
        }
      } catch (error) {
        /* 自己取消的不算失败，页面已经离开了，不用再补一条错误气泡 */
        if (!controller.signal.aborted) {
          this.pushError(`请求失败：${error.friendlyMessage || error.message || '网络异常'}`)
        }
      } finally {
        this.controller = null
        this.loading = false
        this.$nextTick(this.scrollToBottom)
      }
    },

    abort() {
      if (this.controller) {
        this.controller.abort()
        this.controller = null
      }
    },

    pushError(text) {
      const last = this.messages[this.messages.length - 1]
      if (last && last.error && last.text === text) return
      this.messages.push({ role: 'assistant', text, error: true, html: '' })
    },

    retry(index) {
      if (this.loading) return
      const asked = this.messages[index - 1]
      if (!asked || asked.role !== 'user') return
      this.messages.splice(index, 1)
      this.request(asked.text)
    },

    clearChat() {
      this.$confirm('清空当前对话记录？', '清空对话', {
        confirmButtonText: '清空',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          this.messages = []
          this.copiedIndex = -1
          localStorage.removeItem(STORAGE_KEY)
        })
        .catch(() => {})
    },

    async copy(message, index) {
      try {
        await navigator.clipboard.writeText(message.text)
        this.copiedIndex = index
      } catch {
        this.$message({ message: '复制失败，请手动选中', type: 'warning' })
      }
    },

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
      el.style.height = `${Math.min(el.scrollHeight, 168)}px`
    },

    scrollToBottom() {
      const el = this.$refs.scroller
      if (el) el.scrollTop = el.scrollHeight
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>

.chat {
  padding: 0 24px;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: var(--maxw);
  /*
   * 200px 是 index.vue 里 .body-containers 的上内边距，再减 40px 留出下边距——
   * 否则面板底部正好压在视口底边上，输入框贴着屏幕最下面。
   */
  height: calc(100vh - 240px);
  min-height: 600px;
  margin: 0 auto;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

/* ---------------- 信息栏 ---------------- */

.chat-head {
  display: flex;
  flex: none;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--line);
  background: linear-gradient(180deg, #fbfdff, #f4f8fd);
}

.chat-head__mark {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-light), var(--brand));
  box-shadow: 0 6px 16px rgba(22, 103, 196, 0.28);
}

.chat-head__mark :deep(svg) {
  width: 20px;
  height: 20px;
}

.chat-head__text {
  flex: 1;
  min-width: 0;
}

.chat-head__title {
  color: var(--ink);
  font-size: 17px;
  font-weight: 700;
  line-height: 1.35;
}

.chat-head__sub {
  color: var(--ink-3);
  font-size: 12.5px;
  line-height: 1.5;
}

.chat-head__clear {
  flex: none;
  padding: 7px 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--ink-2);
  background: var(--card);
  font-family: inherit;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.chat-head__clear:hover {
  border-color: var(--brand-light);
  color: var(--brand);
  background: var(--brand-soft);
}

/* ---------------- 消息流 ---------------- */

.chat-body {
  flex: 1;
  min-height: 0;
  padding: 24px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* ---------------- 空态 ---------------- */

.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 680px;
  margin: 0 auto;
  padding: 28px 0 8px;
  text-align: center;
  animation: agentFadeUp 0.5s cubic-bezier(0.22, 0.8, 0.3, 1) both;
}

.welcome__mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 22px;
  color: var(--brand);
  background: var(--brand-soft);
}

.welcome__mark :deep(svg) {
  width: 32px;
  height: 32px;
}

.welcome__title {
  margin-top: 18px;
  color: var(--ink);
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.welcome__desc {
  max-width: 480px;
  margin-top: 10px;
  color: var(--ink-2);
  font-size: 14px;
  line-height: 1.8;
}

/* ---------------- 气泡 ---------------- */

.msg {
  display: flex;
  gap: 12px;
  max-width: 860px;
  margin: 0 auto 22px;
}

.msg:last-child {
  margin-bottom: 2px;
}

.msg--user {
  justify-content: flex-end;
}

.msg__avatar {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  margin-top: 2px;
  border-radius: 11px;
  color: var(--brand);
  background: var(--brand-soft);
}

.msg__avatar :deep(svg) {
  width: 18px;
  height: 18px;
}

.msg__main {
  min-width: 0;
  max-width: 100%;
}

.msg--assistant .msg__main {
  flex: 1;
}

.msg__bubble {
  padding: 11px 16px;
  border-radius: 16px 16px 4px 16px;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-light), var(--brand));
  box-shadow: 0 6px 16px rgba(22, 103, 196, 0.2);
  font-size: 14.5px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg__doc {
  padding: 18px 20px;
  border: 1px solid var(--line);
  border-radius: 4px 16px 16px 16px;
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

.msg__tools {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding-left: 4px;
}

.msg__tool {
  padding: 4px 12px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--ink-3);
  background: var(--card);
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.msg__tool:hover {
  border-color: var(--brand-light);
  color: var(--brand);
  background: var(--brand-soft);
}

.msg__error {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border: 1px solid #f0cccc;
  border-radius: 4px 16px 16px 16px;
  background: #fdf5f5;
  color: #a53b3b;
  font-size: 14px;
  line-height: 1.7;
}

.msg__error p {
  flex: 1;
  word-break: break-word;
}

.msg__retry {
  flex: none;
  padding: 5px 14px;
  border: 0;
  border-radius: 999px;
  color: #fff;
  background: var(--brand);
  font-family: inherit;
  font-size: 13px;
  cursor: pointer;
}

.msg__retry:hover:not(:disabled) {
  background: var(--brand-dark);
}

.msg__retry:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ---------------- 生成中 ---------------- */

.thinking {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 18px;
  border: 1px solid var(--line);
  border-radius: 4px 16px 16px 16px;
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

.thinking__dots {
  display: inline-flex;
  flex: none;
  gap: 4px;
}

.thinking__dots i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--brand-light);
  animation: agentThink 1.2s ease-in-out infinite;
}

.thinking__dots i:nth-child(2) {
  animation-delay: 0.16s;
}

.thinking__dots i:nth-child(3) {
  animation-delay: 0.32s;
}

.thinking__text {
  color: var(--ink-2);
  font-size: 13px;
}

/* ---------------- 消息里的 Markdown ---------------- */

.msg__doc :deep(.md-h) {
  color: var(--ink);
  font-weight: 700;
  line-height: 1.45;
}

.msg__doc :deep(.md-h1) {
  margin: 4px 0 14px;
  font-size: 22px;
}

.msg__doc :deep(.md-h2) {
  margin: 22px 0 12px;
  padding-left: 11px;
  border-left: 3px solid var(--brand);
  font-size: 18px;
}

.msg__doc :deep(.md-h3) {
  margin: 18px 0 10px;
  font-size: 16px;
}

.msg__doc :deep(.md-h4),
.msg__doc :deep(.md-h5),
.msg__doc :deep(.md-h6) {
  margin: 14px 0 8px;
  font-size: 15px;
}

.msg__doc :deep(.md-h2:first-child),
.msg__doc :deep(.md-h3:first-child) {
  margin-top: 2px;
}

.msg__doc :deep(.md-p) {
  margin: 0 0 12px;
  color: var(--ink);
  font-size: 14.5px;
  line-height: 1.85;
}

.msg__doc :deep(.md-p:last-child),
.msg__doc :deep(.md-list:last-child),
.msg__doc :deep(.md-table-wrap:last-child),
.msg__doc :deep(.md-quote:last-child),
.msg__doc :deep(.md-pre:last-child) {
  margin-bottom: 0;
}

.msg__doc :deep(.md-list) {
  margin: 0 0 12px;
  padding-left: 22px;
}

.msg__doc :deep(.md-li) {
  margin-bottom: 7px;
  color: var(--ink);
  font-size: 14.5px;
  line-height: 1.8;
}

.msg__doc :deep(.md-li:last-child) {
  margin-bottom: 0;
}

.msg__doc :deep(.md-li--sub) {
  margin-left: 6px;
  color: var(--ink-2);
  font-size: 14px;
}

.msg__doc :deep(strong) {
  color: var(--brand-dark);
  font-weight: 700;
}

.msg__doc :deep(em) {
  font-style: normal;
  color: var(--teal);
}

.msg__doc :deep(.md-code) {
  padding: 2px 6px;
  border-radius: 5px;
  color: var(--brand-dark);
  background: var(--brand-soft);
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 13px;
}

.msg__doc :deep(.md-quote) {
  margin: 0 0 12px;
  padding: 10px 16px;
  border-left: 3px solid var(--teal);
  border-radius: 0 10px 10px 0;
  background: var(--teal-soft);
  color: var(--ink-2);
  font-size: 14px;
  line-height: 1.8;
}

.msg__doc :deep(.md-pre) {
  margin: 0 0 12px;
  padding: 14px 16px;
  overflow-x: auto;
  border-radius: var(--radius-sm);
  background: var(--navy);
}

.msg__doc :deep(.md-pre__code) {
  color: #dbe9f7;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre;
}

.msg__doc :deep(.md-hr) {
  margin: 18px 0;
  border: 0;
  border-top: 1px dashed var(--line);
}

.msg__doc :deep(.md-table-wrap) {
  margin: 0 0 14px;
  overflow-x: auto;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
}

.msg__doc :deep(.md-table) {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.msg__doc :deep(.md-table th) {
  padding: 10px 14px;
  border-bottom: 1px solid var(--line);
  color: var(--ink);
  background: var(--sand-deep);
  font-weight: 700;
  text-align: left;
  white-space: nowrap;
}

.msg__doc :deep(.md-table td) {
  padding: 10px 14px;
  border-bottom: 1px solid var(--line);
  color: var(--ink-2);
  line-height: 1.7;
}

.msg__doc :deep(.md-table tr:last-child td) {
  border-bottom: 0;
}

.msg__doc :deep(.md-table td:first-child) {
  color: var(--ink);
  font-weight: 600;
}

.msg__doc :deep(a) {
  color: var(--brand);
  border-bottom: 1px solid var(--brand-soft);
}

/* ---------------- 输入区 ---------------- */

.composer {
  flex: none;
  padding: 14px 24px 16px;
  border-top: 1px solid var(--line);
  background: var(--card);
}

.composer__box {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 860px;
  margin: 0 auto;
  padding: 6px 6px 6px 18px;
  border: 1.5px solid var(--line);
  border-radius: 22px;
  background: var(--sand);
  transition:
    border-color 0.2s ease,
    background 0.2s ease,
    box-shadow 0.2s ease;
}

.composer__box:focus-within {
  border-color: var(--brand-light);
  background: var(--card);
  box-shadow: 0 0 0 4px rgba(22, 103, 196, 0.1);
}

.composer__input {
  flex: 1;
  min-width: 0;
  max-height: 168px;
  padding: 10px 0;
  border: 0;
  outline: none;
  background: none;
  color: var(--ink);
  font-family: inherit;
  font-size: 14.5px;
  line-height: 1.7;
  resize: none;
  overflow-y: auto;
}

.composer__input::placeholder {
  color: var(--ink-3);
}

.composer__send {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, var(--brand-light), var(--brand));
  box-shadow: 0 6px 16px rgba(22, 103, 196, 0.3);
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    opacity 0.18s ease;
}

.composer__send :deep(svg) {
  width: 20px;
  height: 20px;
}

.composer__send:hover:not(:disabled) {
  transform: scale(1.06);
}

.composer__send:disabled {
  opacity: 0.4;
  box-shadow: none;
  cursor: not-allowed;
}

.composer__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: agentSpin 0.7s linear infinite;
}

.composer__foot {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  max-width: 860px;
  margin: 8px auto 0;
  color: var(--ink-3);
  font-size: 12px;
}

@keyframes agentFadeUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes agentThink {
  0%,
  60%,
  100% {
    opacity: 0.45;
    transform: translateY(0);
  }
  30% {
    opacity: 1;
    transform: translateY(-5px);
  }
}

@keyframes agentSpin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .chat-body {
    scroll-behavior: auto;
  }

  .welcome,
  .thinking__dots i,
  .composer__spinner {
    animation: none;
  }
}

@media (max-width: 900px) {
  .chat {
    padding: 0;
  }

  .chat-panel {
    /* 窄屏是通栏贴边的，留下边距会露出白底，所以这里把 40px 收回去 */
    height: calc(100vh - 200px);
    min-height: 520px;
    border: 0;
    border-radius: 0;
  }

  .chat-body {
    padding: 18px 14px;
  }

  .welcome {
    padding-top: 12px;
  }

  .welcome__title {
    font-size: 22px;
  }

  .msg__doc {
    padding: 14px 15px;
  }

  .composer {
    padding: 12px 14px 14px;
  }

  .composer__foot {
    justify-content: flex-end;
  }

  .composer__foot span:first-child {
    display: none;
  }
}
</style>
