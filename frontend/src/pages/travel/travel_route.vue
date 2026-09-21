<template>
  <div class="travel-route">
    <!-- ============ 页头 ============ -->
    <section class="page-hero">
      <div class="container">
        <div class="page-hero__inner">
          <span class="page-hero__kicker">智游行 · 成熟线路</span>
          <h1 class="page-hero__title">旅游线路</h1>
          <p class="page-hero__desc">
            由持证导游与资深玩家发布，覆盖全国主要目的地的跟团线路。起点、终点、每日安排一目了然，挑一条跟着走。
          </p>
          <div class="page-hero__search">
            <input
              v-model="keywordInput"
              class="search-input"
              placeholder="搜索线路名称 / 目的地，例如：青海湖、西安、张家界"
              @keyup.enter="onSearch"
            />
            <button class="btn btn-primary" @click="onSearch">
              搜索
              <span class="btn-icon" v-html="icons.search"></span>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- ============ 筛选与排序 ============ -->
    <!-- 目的地不在这里做 chips：上面那个搜索框同时搜线路名和起终点，再摆一排省份只会重复。
         主题（深度游/摄影/亲子…）也一样拿掉 —— travel_route 表里根本没有这一列，
         留着就是「点了没反应」的假筛选。 -->
    <section class="section section--filter">
      <div class="container">
        <div class="filter-bar">
          <div class="filter-group">
            <span class="filter-label">天数</span>
            <button
              v-for="(d, i) in dayOptions"
              :key="d.label"
              class="filter-chip"
              :class="{ 'is-active': filterDay === i }"
              @click="filterDay = i"
            >{{ d.label }}</button>
          </div>
          <div class="filter-group">
            <span class="filter-label">预算</span>
            <button
              v-for="(f, i) in feeOptions"
              :key="f.label"
              class="filter-chip"
              :class="{ 'is-active': filterFee === i }"
              @click="filterFee = i"
            >{{ f.label }}</button>
          </div>
          <div class="filter-sort">
            <span class="filter-label">排序</span>
            <select v-model="sortBy" class="sort-select">
              <option value="latest">最新上架</option>
              <option value="hot">最热</option>
              <option value="priceAsc">价格从低到高</option>
              <option value="priceDesc">价格从高到低</option>
              <option value="days">天数最短</option>
            </select>
          </div>
        </div>
      </div>
    </section>

    <!-- ============ 线路列表 ============ -->
    <section class="section">
      <div class="container">
        <div class="list-head">
          <h2 class="list-title">共 {{ total }} 条线路</h2>
          <span class="list-tip">点击卡片查看每日行程安排</span>
        </div>

        <!-- 骨架卡：高度对齐真实卡片，避免加载时页面跳一下 -->
        <div v-if="loading" class="route-grid">
          <div v-for="n in pageSize" :key="n" class="skeleton skel-route"></div>
        </div>

        <ResultState
          v-else-if="error"
          status="error"
          text="线路加载失败"
          hint="接口没有响应。若正在用真实数据，请确认网关与 travel 服务已启动。"
          @retry="load"
        />

        <ResultState
          v-else-if="!items.length"
          status="empty"
          text="没有符合条件的线路"
          hint="换个关键词，或者把天数和预算放宽一点再试试。"
        >
          <button v-if="hasFilter" class="btn btn-ghost state-reset" @click="resetFilter">
            清空筛选条件
          </button>
        </ResultState>

        <div v-else class="route-grid">
          <article
            v-for="(r, i) in items"
            :key="r.id"
            class="route-card reveal"
            :style="{ animationDelay: `${Math.min(i, 5) * 60}ms` }"
            @click="openDetail(r)"
          >
            <div class="route-card__cover">
              <!-- 线路图片是逗号分隔的多张路径，SafeImage 取第一张；
                   路径不存在或加载失败时自己退回首页那套风景画，不会出现破图 -->
              <SafeImage :src="r.routeImage" :seed="r.id" :alt="r.routeName" />
              <span v-if="r.attractionType" class="route-card__badge">{{ r.attractionType }}</span>
              <div class="route-card__overlay">
                <div class="route-card__overlay-row">
                  <span>{{ r.startPoint || '待定' }}</span>
                  <span class="route-card__dash">——</span>
                  <span>{{ r.endPoint || '待定' }}</span>
                </div>
                <div class="route-card__overlay-days">{{ daysText(r) }}</div>
              </div>
            </div>

            <div class="route-card__body">
              <h3 class="route-card__title">{{ r.routeName }}</h3>
              <p class="route-card__desc">{{ excerpt(r.routeDetail) }}</p>

              <ul v-if="cardTags(r).length" class="route-card__tags">
                <li v-for="t in cardTags(r)" :key="t">{{ t }}</li>
              </ul>

              <div class="route-card__meta">
                <div class="route-card__guide">
                  <span class="route-card__guide-avatar" v-html="icons.user"></span>
                  <div>
                    <div class="route-card__guide-name">{{ r.guideName || '导游待定' }}</div>
                    <div class="route-card__guide-sub">{{ metaSub(r) }}</div>
                  </div>
                </div>
                <div class="route-card__price">
                  <span class="route-card__price-num">¥{{ fmtMoney(r.routeFee) }}</span>
                  <span class="route-card__price-unit">/人起</span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <!-- ============ 分页 ============ -->
        <div v-if="!loading && !error && total > pageSize" class="pagination">
          <button class="page-btn" :disabled="page === 1" @click="onPageChange(page - 1)">上一页</button>
          <button
            v-for="p in totalPages"
            :key="p"
            class="page-num"
            :class="{ 'is-active': p === page }"
            @click="onPageChange(p)"
          >{{ p }}</button>
          <button class="page-btn" :disabled="page === totalPages" @click="onPageChange(page + 1)">下一页</button>
        </div>
      </div>
    </section>

    <!-- ============ 详情抽屉 ============ -->
    <!-- 抽屉里的内容全部来自 /travel_route/search 那一份数据（daily 是跟着列表一起回来的），
         所以点开是瞬时的，没有 loading 态。 -->
    <div v-if="currentRoute" class="drawer-mask" @click.self="currentRoute = null">
      <aside class="drawer">
        <header class="drawer__head">
          <div>
            <span v-if="currentRoute.attractionType" class="drawer__badge">{{ currentRoute.attractionType }}</span>
            <h3 class="drawer__title">{{ currentRoute.routeName }}</h3>
          </div>
          <button class="drawer__close" @click="currentRoute = null">×</button>
        </header>
        <div class="drawer__body">
          <p class="drawer__desc">{{ currentRoute.routeDetail || '这条线路还没有补充详细介绍。' }}</p>

          <div class="drawer__info">
            <div><label>出发地</label>{{ currentRoute.startPoint || '待定' }}</div>
            <div><label>目的地</label>{{ currentRoute.endPoint || '待定' }}</div>
            <div><label>行程</label>{{ daysText(currentRoute) }}</div>
            <div><label>价格</label>¥{{ fmtMoney(currentRoute.routeFee) }} /人起</div>
            <div><label>出发日期</label>{{ fmtDate(currentRoute.departureDate) }}</div>
            <!-- 这一格给的是线路的报团名额（总容量），不是「还剩几个」——
                 剩余数只有服务端算得出来（要减掉已报名的人数），列表接口里没有这个字段 -->
            <div><label>报团名额</label>{{ quotaText(currentRoute) }}</div>
            <div><label>交通方式</label>{{ currentRoute.transportMode || '待定' }}</div>
            <div><label>联系方式</label>{{ currentRoute.contactPhone || '未留' }}</div>
          </div>

          <h4 class="drawer__sub">每日安排</h4>
          <ol v-if="dailyOf(currentRoute).length" class="drawer__days">
            <li v-for="d in dailyOf(currentRoute)" :key="d.id || d.day">
              <div class="drawer__day-no">Day {{ d.day }}</div>
              <div class="drawer__day-body">
                <strong>{{ d.title }}</strong>
                <p>{{ d.plan }}</p>
              </div>
            </li>
          </ol>
          <p v-else class="drawer__text drawer__text--muted">这条线路还没有录入每日行程。</p>

          <h4 class="drawer__sub">导游</h4>
          <p class="drawer__text">
            {{ currentRoute.guideName || '待定' }}
            <span v-if="currentRoute.guideResume">：{{ currentRoute.guideResume }}</span>
          </p>
        </div>
        <footer class="drawer__foot">
          <button class="btn btn-ghost" @click="bookNow">立即咨询</button>
          <button class="btn btn-primary" @click="joinNow">立即报名</button>
        </footer>
      </aside>
    </div>

    <!-- ============ 咨询聊天窗 ============ -->
    <!-- 和详情抽屉互斥：点「立即咨询」时会先关掉抽屉，不然两层遮罩叠在一起，
         点空白处只会关掉上面那层，看着像没反应。 -->
    <div v-if="chatRoute" class="chat-mask" @click.self="chatRoute = null">
      <div class="chat-modal">
        <ConsultChat
          :route-id="chatRoute.id"
          :route-name="chatRoute.routeName"
          self-role="user"
          closable
          @close="chatRoute = null"
        />
      </div>
    </div>

    <!-- ============ 报名参团弹窗 ============ -->
    <!-- 同样与抽屉互斥（点「立即报名」时会关掉抽屉）。靠 v-if 收在组件内部，
         所以这里常驻挂载没问题，关闭状态下一个节点都不渲染。 -->
    <GroupTourDialog v-model="signupOpen" :route="signupRoute" />
  </div>
</template>

<script>
/*
 * 这个页面本轮从「纯静态演示」改成真接口了：
 *   - 列表走 /travel_route/search（**不是 /list**）—— keyword 要同时命中线路名、
 *     景点名、起点、终点、途经路段，不是实体字段，落到 /list 上会被 MPUtil 静默忽略，
 *     筛了等于没筛。
 *   - 天数与预算是区间筛选，走后端 MPUtil.between 认的 days_start/days_end、
 *     routeFee_start/routeFee_end；排序也是服务端排（sort + order）。
 *   - 每条线路带着 daily（每日行程，按天升序）一起回来，详情抽屉直接渲染。
 *
 * 被删掉的是写死在 data() 里的 6 条线路，以及「主题」「目的地（省份）」两组 chips：
 * 前者 travel_route 表里没有对应的列，后者与页头那个搜索框重复。
 *
 * 「收藏」也已整块删除：它原先只是往 localStorage 写一个 id 数组（routeFav），
 * 真表里虽有 store_up_num 计数，却没有收藏接口 —— 点了除了弹个 toast 什么也不会发生，
 * 个人中心也查不到。取而代之的是抽屉底部的两个动作：「立即咨询」开在线聊天窗
 * （ConsultChat.vue），「立即报名」开报团弹窗（GroupTourDialog.vue，会真正写
 * group_tour 表并接着走收银台）。
 */
import ResultState from '@/components/ResultState.vue'
import SafeImage from '@/components/SafeImage.vue'
import ConsultChat from '@/components/ConsultChat.vue'
import GroupTourDialog from '@/components/GroupTourDialog.vue'
import { searchRoutes } from '@/api/travel_route'
import { fmtDate, fmtMoney } from '@/common/format'
import { isLogin } from '@/common/user'

const svg = (paths) =>
  `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${paths}</svg>`
const icons = {
  search: svg('<circle cx="10.8" cy="10.8" r="6.2"/><path d="M15.4 15.4L20.5 20.5"/>'),
  user: svg('<circle cx="12" cy="8" r="3.6"/><path d="M4.5 20a7.5 7.5 0 0115 0"/>'),
}

const PAGE_SIZE = 6

/* 卡片摘要的截断长度。摘要是渲染时从 route_detail 里抠的，不建新列 —— 这样它永远和正文同步 */
const EXCERPT_LEN = 88

/* 排序：sort 传 camelCase 字段名，后端 MPUtil 自己转下划线；order 就是 asc / desc。
   「最热」用收藏数（store_up_num）—— 真表里没有点击量，thumbs_up_num 是导游点「赞」，
   语义上收藏更接近热度。

   「最新」是默认项，不能省：新过审的线路四个计数全是 0，按「最热」排永远垫底，
   一共 6 条一页时直接被挤到最后一页 —— 导游刚建完、管理员刚点通过，
   游客却「什么都没看到」，看着像审核没生效。按提交时间倒序才会落到第一张。 */
const SORTS = {
  latest: { sort: 'addTime', order: 'desc' },
  hot: { sort: 'storeUpNum', order: 'desc' },
  priceAsc: { sort: 'routeFee', order: 'asc' },
  priceDesc: { sort: 'routeFee', order: 'desc' },
  days: { sort: 'days', order: 'asc' },
}

export default {
  name: 'TravelRoutePage',
  components: { ResultState, SafeImage, ConsultChat, GroupTourDialog },
  data() {
    return {
      icons,
      fmtDate,
      fmtMoney,

      items: [],
      total: 0,
      page: 1,
      pageSize: PAGE_SIZE,
      loading: true,
      error: false,

      /* 已生效的条件 vs 正在输入的字。输入框里的内容没按搜索之前不该触发请求，
         所以 keyword（已提交）与 keywordInput（正在输入）必须是两个变量。 */
      keyword: '',
      keywordInput: '',

      /* 天数 / 预算都是「区间选项的下标」，0 = 全部。存下标而不是区间值，
         是为了让模板里的 v-for 能直接按 i 比高亮，不用再解析字符串。 */
      filterDay: 0,
      filterFee: 0,
      sortBy: 'latest',

      /* 竞态保护：快速连点筛选时，先发的慢请求不能覆盖后发的快请求 */
      reqId: 0,

      /* 详情抽屉里的线路 */
      currentRoute: null,
      /* 正在咨询的线路；非空时聊天弹窗打开。与 currentRoute 互斥 */
      chatRoute: null,
      /* 正在报名的线路与报名弹窗开关。同样与 currentRoute 互斥 */
      signupRoute: null,
      signupOpen: false,

      dayOptions: [
        { label: '全部', min: null, max: null },
        { label: '3 天以内', min: null, max: 3 },
        { label: '4 - 6 天', min: 4, max: 6 },
        { label: '7 天以上', min: 7, max: null },
      ],
      feeOptions: [
        { label: '全部', min: null, max: null },
        { label: '2000 元以下', min: null, max: 2000 },
        { label: '2000 - 3500 元', min: 2000, max: 3500 },
        { label: '3500 元以上', min: 3500, max: null },
      ],
    }
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.total / this.pageSize))
    },
    hasFilter() {
      return Boolean(this.keyword) || this.filterDay !== 0 || this.filterFee !== 0
    },
  },
  watch: {
    // 三个筛选条件一改就回第一页重拉。排序也是服务端排的，所以它同样要重拉
    filterDay() {
      this.reloadFromFirstPage()
    },
    filterFee() {
      this.reloadFromFirstPage()
    },
    sortBy() {
      this.reloadFromFirstPage()
    },
  },
  created() {
    this.load()
  },
  methods: {
    /* ---------------- 取数 ---------------- */

    /** 把当前条件拼成请求参数。空条件一律不发，后端按「没传」处理 */
    buildParams() {
      const params = { page: this.page, limit: this.pageSize }
      if (this.keyword) params.keyword = this.keyword

      // 区间只传有值的那一端：'7 天以上' 只发 days_start，'2000 以下' 只发 routeFee_end
      const day = this.dayOptions[this.filterDay]
      if (day) {
        if (day.min != null) params.days_start = day.min
        if (day.max != null) params.days_end = day.max
      }
      const fee = this.feeOptions[this.filterFee]
      if (fee) {
        if (fee.min != null) params.routeFee_start = fee.min
        if (fee.max != null) params.routeFee_end = fee.max
      }

      const sort = SORTS[this.sortBy] || SORTS.hot
      params.sort = sort.sort
      params.order = sort.order
      return params
    },

    async load() {
      const token = ++this.reqId
      this.loading = true
      this.error = false
      try {
        const res = await searchRoutes(this.buildParams())
        if (token !== this.reqId) return // 已被更新的请求取代，丢弃
        if (res.data.code === 0) {
          this.items = res.data.data.list || []
          this.total = res.data.data.totalCount || 0
        } else {
          this.error = true
          this.items = []
          this.total = 0
        }
      } catch (e) {
        if (token !== this.reqId) return
        this.error = true
        this.items = []
        this.total = 0
      } finally {
        if (token === this.reqId) this.loading = false
      }
    },

    /** 条件变了：回第一页再拉 */
    reloadFromFirstPage() {
      this.page = 1
      this.load()
    },

    /* ---------------- 交互 ---------------- */

    onSearch() {
      this.keyword = this.keywordInput.trim()
      this.reloadFromFirstPage()
    },

    resetFilter() {
      this.keyword = ''
      this.keywordInput = ''
      this.filterDay = 0
      this.filterFee = 0
      this.reloadFromFirstPage()
    },

    onPageChange(page) {
      this.page = page
      this.load() // 翻页只重拉，不重置页码
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    openDetail(r) {
      this.currentRoute = r
    },

    /*
     * 「立即咨询」打开在线聊天窗，不再只丢一个电话号码。
     *
     * 必须先登录：后端要靠会话里的账号判断「谁在跟谁聊」，没登录发的消息无处归属
     * （接口也会直接 401）。
     */
    bookNow() {
      if (!this.askLogin('在线咨询')) return
      this.chatRoute = this.currentRoute
      // 关掉详情抽屉，避免两层遮罩叠加
      this.currentRoute = null
    },

    /*
     * 「立即报名」开报团弹窗。
     *
     * 同样要先登录：报团单挂在账号上，报名人与证件号也得从该账号的实名记录里取
     * （接口没登录直接 401，前端先拦一道省得用户填完才被拒）。
     */
    joinNow() {
      if (!this.askLogin('报名参团')) return
      this.signupRoute = this.currentRoute
      this.signupOpen = true
      this.currentRoute = null
    },

    /**
     * 登录闸门，返回是否已登录。
     *
     * 用确认框而不是静默跳转 —— 用户刚点的是「咨询」或「报名」，
     * 突然被弹到登录页会莫名其妙，先问一句再走。
     */
    askLogin(what) {
      if (isLogin()) return true
      this.$msgbox
        .confirm(`${what}需要先登录，是否现在去登录？`, '请先登录', {
          confirmButtonText: '去登录',
          cancelButtonText: '再逛逛',
          type: 'info',
        })
        .then(() => {
          this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
        })
        .catch(() => {})
      return false
    },

    /* ---------------- 展示 ---------------- */

    excerpt(detail) {
      const text = String(detail || '').replace(/\s+/g, ' ').trim()
      if (!text) return '这条线路还没有补充详细介绍。'
      return text.length > EXCERPT_LEN ? text.slice(0, EXCERPT_LEN) + '…' : text
    },

    /** 卡片上那排小标签。景点类型已经做了封面角标，这里不重复 */
    cardTags(r) {
      return [r.transportMode].filter(Boolean)
    },

    daysText(r) {
      return r && r.days ? `${r.days} 天` : '天数待定'
    },

    quotaText(r) {
      return r && r.groupQuota != null ? `${r.groupQuota} 个名额` : '名额待定'
    },

    /** 导游下面那行小字。两处都是真表字段，没有就说明没有 */
    metaSub(r) {
      const parts = [r.departureDate ? `出发 ${fmtDate(r.departureDate)}` : '出发日期待定']
      // 是「报团名额」（总容量）而不是「还剩几个」：剩余数要减掉已报名的人数，
      // 列表接口里没有这个字段，只有服务端在提交报名时算得出来
      if (r.groupQuota != null) parts.push(`名额 ${r.groupQuota} 位`)
      return parts.join(' · ')
    },

    /** daily 只在 /search 与 /detail 上回填，别的路径是 null，所以这里必须兜底 */
    dailyOf(r) {
      return r && Array.isArray(r.daily) ? r.daily : []
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.travel-route { background: var(--sand); min-height: 100vh; }

/* 通用按钮（与 Home.vue 保持一致） */
.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  height: 44px; padding: 0 24px; border: 0; border-radius: 999px;
  font-size: 15px; font-weight: 600; font-family: inherit; cursor: pointer;
  transition: transform .18s ease, box-shadow .18s ease, background .18s ease;
}
.btn-primary {
  color: #fff; background: linear-gradient(135deg, var(--brand-light), var(--brand));
  box-shadow: 0 8px 20px rgba(22,103,196,.28);
}
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 12px 26px rgba(22,103,196,.36); }
.btn-ghost {
  color: var(--ink); background: #fff;
  box-shadow: inset 0 0 0 1.5px var(--line);
}
.btn-ghost:hover { color: var(--brand); box-shadow: inset 0 0 0 1.5px var(--brand-light); transform: translateY(-2px); }
.btn-icon { display: inline-flex; width: 18px; height: 18px; }
.btn-icon :deep(svg) { width: 18px; height: 18px; }

/* 页头 */
.page-hero {
  background: linear-gradient(135deg, var(--navy), #1d4e89);
  color: #fff; padding: 72px 0 88px;
}
.page-hero__kicker {
  display: inline-block; padding: 5px 14px; border-radius: 999px;
  background: rgba(255,255,255,.16); font-size: 13px; letter-spacing: 2px;
}
.page-hero__title { margin: 16px 0 0; font-size: 44px; font-weight: 800; letter-spacing: -1px; }
.page-hero__desc { margin-top: 14px; max-width: 620px; color: rgba(255,255,255,.85); font-size: 16px; line-height: 1.8; }
.page-hero__search {
  display: flex; gap: 10px; margin-top: 28px; max-width: 640px;
  padding: 6px; background: #fff; border-radius: 999px;
}
.search-input {
  flex: 1; border: 0; outline: 0; padding: 0 18px;
  font-size: 15px; color: var(--ink); background: transparent;
}

/* 筛选 */
.section { padding: 56px 0; }
.section--filter { padding: 28px 0 0; }
.filter-bar {
  display: flex; flex-wrap: wrap; gap: 18px 28px;
  padding: 22px 26px; background: var(--card);
  border: 1px solid var(--line); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.filter-group { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.filter-label { color: var(--ink-3); font-size: 13px; margin-right: 4px; }
.filter-chip {
  padding: 5px 14px; border: 1px solid var(--line); border-radius: 999px;
  background: var(--card); color: var(--ink-2); font-size: 13px; cursor: pointer;
  transition: all .18s ease;
}
.filter-chip:hover { color: var(--brand); border-color: var(--brand-light); }
.filter-chip.is-active {
  color: #fff; background: var(--brand); border-color: var(--brand);
}
.filter-sort { margin-left: auto; display: flex; align-items: center; gap: 8px; }
.sort-select {
  height: 34px; padding: 0 10px; border: 1px solid var(--line);
  border-radius: 8px; background: #fff; color: var(--ink); font-size: 13px;
}

/* 列表 */
.list-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 22px; }
.list-title { color: var(--ink); font-size: 22px; font-weight: 700; }
.list-tip { color: var(--ink-3); font-size: 13px; }
.route-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 22px;
}
.route-card {
  display: flex; flex-direction: column; background: var(--card);
  border: 1px solid var(--line); border-radius: var(--radius-lg);
  overflow: hidden; cursor: pointer;
  transition: transform .22s ease, box-shadow .22s ease, border-color .22s ease;
}
.route-card:hover { transform: translateY(-6px); border-color: transparent; box-shadow: var(--shadow); }
.route-card__cover { position: relative; height: 200px; overflow: hidden; }
/* SafeImage 的根节点在我们这一层拿不到 scoped 属性，得用 :deep 才选得中 */
.route-card__cover :deep(.safe-image) {
  position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover;
}
.route-card__badge {
  position: absolute; top: 12px; left: 12px; z-index: 2;
  padding: 4px 12px; border-radius: 999px;
  color: #fff; background: rgba(13,44,77,.78); font-size: 12px;
}
.route-card__overlay {
  position: absolute; left: 0; right: 0; bottom: 0;
  padding: 28px 16px 12px; z-index: 1;
  background: linear-gradient(180deg, transparent, rgba(0,0,0,.55));
  color: #fff;
}
.route-card__overlay-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.route-card__dash { opacity: .6; }
.route-card__overlay-days { font-size: 12px; opacity: .85; margin-top: 2px; }

.route-card__body { padding: 18px 20px 20px; display: flex; flex-direction: column; gap: 10px; flex: 1; }
.route-card__title { margin: 0; color: var(--ink); font-size: 17px; font-weight: 700; line-height: 1.4; }
.route-card__desc { margin: 0; color: var(--ink-2); font-size: 13px; line-height: 1.7;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.route-card__tags { list-style: none; margin: 0; padding: 0; display: flex; flex-wrap: wrap; gap: 6px; }
.route-card__tags li {
  padding: 3px 10px; border-radius: 999px;
  color: var(--brand-dark); background: var(--brand-soft); font-size: 12px;
}
.route-card__meta {
  margin-top: auto; padding-top: 12px; border-top: 1px dashed var(--line);
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
}
.route-card__guide { display: flex; align-items: center; gap: 8px; min-width: 0; }
.route-card__guide-avatar {
  flex: none; width: 32px; height: 32px; border-radius: 50%;
  background: var(--brand-soft); color: var(--brand);
  display: inline-flex; align-items: center; justify-content: center;
}
.route-card__guide-avatar :deep(svg) { width: 18px; height: 18px; }
.route-card__guide-name { color: var(--ink); font-size: 13px; font-weight: 600; }
.route-card__guide-sub { color: var(--ink-3); font-size: 11px; }
.route-card__price { flex: none; text-align: right; }
.route-card__price-num { color: #e5484d; font-size: 20px; font-weight: 800; }
.route-card__price-unit { color: var(--ink-3); font-size: 11px; margin-left: 2px; }

/* 骨架 / 分页 / 空态 */
.skel-route { height: 380px; border-radius: var(--radius-lg); }
.state-reset { margin-top: 6px; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 8px; margin-top: 36px; }
.page-btn {
  height: 34px; padding: 0 14px; border: 1px solid var(--line);
  border-radius: 8px; background: #fff; color: var(--ink-2); cursor: pointer;
}
.page-btn:disabled { opacity: .4; cursor: not-allowed; }
.page-num {
  min-width: 34px; height: 34px; padding: 0 8px;
  border: 1px solid var(--line); border-radius: 8px;
  background: #fff; color: var(--ink-2); cursor: pointer;
}
.page-num.is-active {
  background: var(--brand); color: #fff; border-color: var(--brand);
}

/* 抽屉 */
.drawer-mask {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(13,44,77,.45);
  display: flex; justify-content: flex-end;
}
.drawer {
  width: min(520px, 100%); height: 100%;
  background: #fff; display: flex; flex-direction: column;
  animation: slideIn .28s ease;
}
@keyframes slideIn { from { transform: translateX(40px); opacity: 0; } to { transform: none; opacity: 1; } }
.drawer__head {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 22px 26px; border-bottom: 1px solid var(--line);
}
.drawer__badge {
  display: inline-block; padding: 3px 10px; border-radius: 999px;
  color: var(--brand); background: var(--brand-soft); font-size: 12px;
}
.drawer__title { margin: 8px 0 0; color: var(--ink); font-size: 20px; }
.drawer__close {
  border: 0; background: transparent; font-size: 26px;
  color: var(--ink-3); cursor: pointer; line-height: 1;
}
.drawer__body { flex: 1; overflow-y: auto; padding: 22px 26px; }
.drawer__desc { margin: 0; color: var(--ink-2); line-height: 1.8; }
.drawer__info {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
  margin: 18px 0; padding: 16px;
  background: var(--sand); border-radius: 12px;
}
.drawer__info label { display: block; color: var(--ink-3); font-size: 12px; margin-bottom: 4px; }
.drawer__sub { color: var(--ink); font-size: 16px; margin: 22px 0 12px; }
.drawer__text { margin: 0; color: var(--ink-2); font-size: 13.5px; line-height: 1.8; }
.drawer__text--muted { color: var(--ink-3); }
.drawer__days { list-style: none; margin: 0; padding: 0; }
.drawer__days > li { display: flex; gap: 14px; padding: 12px 0; border-bottom: 1px dashed var(--line); }
.drawer__day-no {
  flex: none; width: 48px; height: 28px; border-radius: 8px;
  background: var(--brand); color: #fff; font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.drawer__day-body strong { color: var(--ink); font-size: 14px; }
.drawer__day-body p { margin: 4px 0 0; color: var(--ink-2); font-size: 13px; line-height: 1.7; }
.drawer__foot {
  display: flex; gap: 12px; padding: 16px 26px; border-top: 1px solid var(--line);
}
.drawer__foot .btn { flex: 1; }

/* 咨询聊天窗 */
.chat-mask {
  /*
   * z-index 要压过 index.vue 的顶栏：那一层是行内样式写死的 zIndex 1002（.top-container）
   * 与 1005（.menu-preview），而它们所在的容器高 200px、下面还挂着导航条。
   * 窗口矮一点（比如 1000px 高）时居中的弹窗会顶到导航条下面，z-index 低于 1005 就被盖住 ——
   * 表现为「弹窗打开着，但顶部几条消息看不见」。抽屉那层是 100，也有同样的问题。
   */
  position: fixed; inset: 0; z-index: 1100;
  background: rgba(13,44,77,.45);
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}
.chat-modal {
  width: min(520px, 100%);
  height: min(660px, 88vh);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  animation: popIn .24s cubic-bezier(.22,.8,.3,1);
}
@keyframes popIn { from { transform: translateY(16px) scale(.98); opacity: 0; } to { transform: none; opacity: 1; } }

/* 入场动画 */
.reveal { animation: fadeUp .5s cubic-bezier(.22,.8,.3,1) both; }
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: none; } }

@media (max-width: 1080px) {
  .route-grid { grid-template-columns: repeat(2, 1fr); }
  .page-hero__title { font-size: 34px; }
  .filter-sort { margin-left: 0; }
}
@media (max-width: 720px) {
  .route-grid { grid-template-columns: 1fr; }
  .page-hero { padding: 48px 0 60px; }
  .page-hero__title { font-size: 28px; }
  .page-hero__search { flex-direction: column; border-radius: 20px; }
  .search-input { padding: 12px 16px; }
}
</style>
