<template>
  <div class="chat-admin">
    <aside class="chat-admin__side">
      <header class="chat-admin__side-head">
        <strong>咨询会话</strong>
        <span>{{ sessions.length }} 条</span>
      </header>

      <div class="chat-admin__side-body">
        <p v-if="loading && !sessions.length" class="chat-admin__tip">正在加载…</p>
        <p v-else-if="!sessions.length" class="chat-admin__tip">
          还没有游客咨询。游客在「旅游线路」页点「立即咨询」发来的消息会出现在这里。
        </p>

        <button
          v-for="s in sessions"
          :key="s.routeId + ':' + s.userAccount"
          type="button"
          class="chat-admin__item"
          :class="{ 'is-active': isActive(s) }"
          @click="pick(s)"
        >
          <div class="chat-admin__item-top">
            <span class="chat-admin__name">{{ s.userAccount }}</span>
            <span class="chat-admin__time">{{ stamp(s.lastTime) }}</span>
          </div>
          <div class="chat-admin__route">{{ s.routeName }}</div>
          <div class="chat-admin__preview">
            <span v-if="s.lastFrom === 'guide'" class="chat-admin__me">我：</span>{{ s.lastContent }}
          </div>
          <span v-if="s.unread" class="chat-admin__badge">{{ s.unread > 99 ? '99+' : s.unread }}</span>
        </button>
      </div>
    </aside>

    <section class="chat-admin__panel">
      <!--
        :key 让切换游客时整个聊天窗重建。不重建的话上一位游客的消息会留在屏幕上，
        直到第一次轮询回来才被换掉 —— 那几百毫秒里对话是错的，很容易看岔。
      -->
      <ConsultChat
        v-if="active"
        :key="active.routeId + ':' + active.userAccount"
        :route-id="active.routeId"
        :route-name="active.routeName"
        self-role="guide"
        :peer-account="active.userAccount"
        :peer-name="active.userAccount"
      />
      <div v-else class="chat-admin__placeholder">
        <p>从左侧选一位游客开始回复</p>
        <small>游客发来的咨询会实时出现在左侧列表，带红点的是未读</small>
      </div>
    </section>
  </div>
</template>

<script>
/*
 * 导游端的「咨询消息」。左侧是咨询过我的游客，右侧是聊天窗（与游客端共用 ConsultChat）。
 *
 * 会话来自 /consult/conversations，按登录导游的工号过滤 —— 后端保证只看得到
 * 自己线路的咨询，前端不做也不该做这层筛选。
 *
 * 列表每 3 秒轮询一次（聊天窗内部另有 2 秒的消息轮询）。轮询一律 silent：
 * 后端没起的时候，这里弹 toast 会变成每 3 秒一条。
 */
import ConsultChat from '@/components/ConsultChat.vue'
import { fetchConsultConversations } from '@/api/consult'
import { fmtDateTime, parseDate } from '@/common/format'

const POLL_MS = 3000
const pad = (n) => String(n).padStart(2, '0')

export default {
  name: 'AdminChat',
  components: { ConsultChat },
  data() {
    return {
      sessions: [],
      loading: true,
      active: null,
      timer: null,
    }
  },
  mounted() {
    this.load()
    this.timer = window.setInterval(this.load, POLL_MS)
  },
  beforeUnmount() {
    if (this.timer) window.clearInterval(this.timer)
  },
  methods: {
    async load() {
      try {
        const res = await fetchConsultConversations({ silent: true })
        if (res.data.code === 0) {
          this.sessions = res.data.data || []
          // 当前打开的会话可能已经不在列表里了（理论上不会，防御一下）
          if (this.active) {
            const still = this.sessions.find((s) => this.isActive(s))
            if (still) this.active = still
          }
        }
      } catch {
        // 轮询失败静默：列表保持上一次的样子，聊天窗那边会显示「未连接」
      } finally {
        this.loading = false
      }
    },

    isActive(s) {
      return (
        !!this.active && this.active.routeId === s.routeId && this.active.userAccount === s.userAccount
      )
    },

    pick(s) {
      this.active = s
    },

    /** 当天只显示 HH:mm，跨天补日期 —— 和聊天窗里的时间口径一致 */
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
.chat-admin {
  display: flex;
  gap: 16px;
  /* 减去 AdminLayout 顶栏与内边距，让两栏撑满可视区、各自内部滚动 */
  height: calc(100vh - 150px);
  min-height: 460px;
}

/* ---------------- 左侧会话列表 ---------------- */
.chat-admin__side {
  display: flex;
  flex: 0 0 268px;
  flex-direction: column;
  min-height: 0;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--card);
  overflow: hidden;
}

.chat-admin__side-head {
  display: flex;
  flex: none;
  align-items: baseline;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--line);
}

.chat-admin__side-head strong {
  color: var(--ink);
  font-size: 15px;
}

.chat-admin__side-head span {
  color: var(--ink-3);
  font-size: 12px;
}

.chat-admin__side-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 8px;
}

.chat-admin__tip {
  margin: 16px 10px;
  color: var(--ink-3);
  font-size: 13px;
  line-height: 1.8;
}

.chat-admin__item {
  position: relative;
  display: block;
  width: 100%;
  padding: 10px 12px;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  text-align: left;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.16s ease;
}

.chat-admin__item:hover {
  background: var(--sand);
}

.chat-admin__item.is-active {
  background: var(--brand-soft);
}

.chat-admin__item-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.chat-admin__name {
  color: var(--ink);
  font-size: 14px;
  font-weight: 600;
}

.chat-admin__time {
  flex: none;
  color: var(--ink-3);
  font-size: 11px;
}

.chat-admin__route {
  margin-top: 2px;
  color: var(--brand);
  font-size: 12px;
}

.chat-admin__preview {
  margin-top: 4px;
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.6;
  /* 预览只留一行，超出省略 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-admin__me {
  color: var(--ink-2);
}

.chat-admin__badge {
  position: absolute;
  top: 32px;
  right: 10px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #e5484d;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}

/* ---------------- 右侧聊天区 ---------------- */
.chat-admin__panel {
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--card);
  overflow: hidden;
}

/* ConsultChat 的根节点自己不撑宽，在 flex 父容器里会缩成内容宽度（气泡的宽度）。
   这里补上 grow，让它填满右栏；placeholder 的 margin:auto 仍靠 flex 居中。 */
.chat-admin__panel :deep(.consult) {
  flex: 1;
  min-width: 0;
}

.chat-admin__placeholder {
  margin: auto;
  text-align: center;
}

.chat-admin__placeholder p {
  margin: 0;
  color: var(--ink-2);
  font-size: 15px;
}

.chat-admin__placeholder small {
  display: block;
  margin-top: 8px;
  color: var(--ink-3);
  font-size: 13px;
}
</style>
