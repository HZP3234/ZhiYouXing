<template>
  <!--
    根节点必须恰好一个：index.vue 的 $route 监听里读 document.getElementById('scrollView').offsetTop，
    那个 id 是靠属性透传落到本组件根元素上的。多根节点会让 attrs 落不下来。
  -->
  <div class="travel-guide">
    <!-- ============ 页头 ============ -->
    <section class="page-hero">
      <div class="container">
        <div class="page-hero__inner">
          <span class="page-hero__kicker">智游行 · 行前功课</span>
          <h1 class="page-hero__title">旅游攻略</h1>
          <p class="page-hero__desc">
            目的地玩法、交通住宿、避坑贴士与摄影机位，由走过这条路的人写成。也可以把你自己那一趟写下来。
          </p>
          <div class="page-hero__search">
            <input
              v-model="keywordInput"
              class="search-input"
              placeholder="搜索攻略，标题或正文里出现即命中，例如：莫高窟预约 / 青海湖穿衣"
              @keyup.enter="onSearch"
            />
            <button class="btn btn-primary" @click="onSearch">
              搜索
              <span class="btn-icon" v-html="icons.search"></span>
            </button>
          </div>
          <div class="page-hero__actions">
            <button class="btn btn-primary" @click="goWrite">
              <span class="btn-icon" v-html="icons.guide"></span>写攻略
            </button>
            <button class="btn btn-ghost" @click="toggleMine">
              {{ mine ? '看全部攻略' : '我的攻略' }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- ============ 标签筛选 ============ -->
    <!-- 我的攻略视图下没有这排 chips：那个列表是按作者过滤的，再叠标签筛选意义不大 -->
    <section v-if="!mine" class="section section--tabs">
      <div class="container">
        <div class="filter-bar">
          <div class="filter-row">
            <span class="filter-row__label">标签</span>
            <div class="chip-row">
              <button class="chip" :class="{ 'is-active': !activeTagId }" @click="setTag('')">全部</button>
              <!-- 字典来自 /travel_guide_tag/list，每个 chip 带该标签下的篇数。
                   tags 为空（后端没起或还没人打过标签）时这一排就只剩「全部」，不是错误态。 -->
              <button
                v-for="t in tags"
                :key="t.tagId"
                class="chip"
                :class="{ 'is-active': String(activeTagId) === String(t.tagId) }"
                @click="setTag(t.tagId)"
              >
                {{ t.tagName }}<em class="chip__num">{{ t.guideCount }}</em>
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ============ 主体：左列表 + 右侧栏 ============ -->
    <section class="section">
      <div class="container">
        <div class="layout">
          <!-- 攻略列表 -->
          <div class="main-col">
            <div class="list-head">
              <h2 class="list-title">{{ listTitle }}</h2>
              <span class="list-tip">按发布时间从新到旧</span>
            </div>

            <!-- 骨架卡：保持和真实卡片一样的高度，避免加载时页面跳一下 -->
            <div v-if="loading" class="skel-list">
              <div v-for="n in pageSize" :key="n" class="skeleton skel-guide"></div>
            </div>

            <ResultState
              v-else-if="error"
              status="error"
              text="攻略加载失败"
              hint="接口没有响应。若正在用真实数据，请确认网关与 travel 服务已启动。"
              @retry="reload"
            />

            <ResultState
              v-else-if="!items.length"
              status="empty"
              :text="mine ? '你还没有发布过攻略' : '没有找到相关攻略'"
              :hint="mine ? '把这一趟写下来，别人也能用得上。' : '换个标签或关键词再试试。'"
            >
              <button v-if="mine" class="btn btn-primary state-reset" @click="goWrite">去写第一篇</button>
              <button v-else-if="hasFilter" class="btn btn-ghost state-reset" @click="clearFilters">清空筛选条件</button>
            </ResultState>

            <template v-else>
              <article
                v-for="(item, i) in items"
                :key="item.id"
                class="guide-card reveal"
                :style="{ animationDelay: Math.min(i, 8) * 60 + 'ms' }"
                @click="goDetail(item)"
              >
                <!-- 攻略不再有封面字段，这一格固定走兜底风景图（seed 保证同一篇每次同一幅） -->
                <div class="guide-card__cover">
                  <SafeImage :seed="item.id" :alt="item.guideTitle" />
                </div>
                <div class="guide-card__body">
                  <h3 class="guide-card__title">{{ item.guideTitle }}</h3>
                  <p class="guide-card__desc">{{ excerpt(item.guideDetail) }}</p>
                  <ul v-if="tagNamesOf(item).length" class="guide-card__tags">
                    <li v-for="t in tagNamesOf(item)" :key="t">#{{ t }}</li>
                  </ul>
                  <div class="guide-card__meta">
                    <div class="guide-card__author">
                      <span class="guide-card__avatar">{{ authorInitial(item) }}</span>
                      <div>
                        <div class="guide-card__author-name">{{ authorOf(item) }}</div>
                        <div class="guide-card__author-date">{{ fmtDate(item.addTime) }}</div>
                      </div>
                    </div>
                    <!-- 自己的攻略多两个按钮，省得先进详情页才能改 -->
                    <div v-if="isMine(item)" class="guide-card__ops">
                      <button class="op-btn" @click.stop="goEdit(item)">编辑</button>
                      <button class="op-btn op-btn--danger" @click.stop="removeGuide(item)">删除</button>
                    </div>
                  </div>
                </div>
              </article>

              <div v-if="total > pageSize" class="pager">
                <el-pagination
                  background
                  layout="prev, pager, next, total"
                  :total="total"
                  :page-size="pageSize"
                  :current-page="page"
                  @current-change="onPageChange"
                />
              </div>
            </template>
          </div>

          <!-- 右侧栏 -->
          <aside class="side-col">
            <div class="side-block">
              <h4 class="side-title">热门标签</h4>
              <ul v-if="topTags.length" class="tag-cloud">
                <li v-for="t in topTags" :key="t.tagId">
                  <button class="tag-pill" @click="setTag(t.tagId)">
                    {{ t.tagName }}<em>{{ t.guideCount }}</em>
                  </button>
                </li>
              </ul>
              <p v-else class="side-empty">还没有人打标签，等你来开个头。</p>
            </div>

            <div class="side-block side-block--tip">
              <h4 class="side-title">写攻略小提示</h4>
              <ul class="check-list">
                <li v-for="c in writeTips" :key="c"><span class="check-dot">✓</span>{{ c }}</li>
              </ul>
            </div>
          </aside>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
/*
 * 这个页面本轮从「纯静态演示」改成真接口了：
 *   - 列表走 /travel_guide/search（**不是 /list**）—— tagId 与 keyword 都不是
 *     travel_guide 的实体列，落到 /list 上会被 MPUtil 静默忽略，筛了等于没筛。
 *   - 标签 chips 走 /travel_guide_tag/list，带每个标签下的篇数。
 *   - 我的攻略走 /travel_guide/page，那条按会话里的账号过滤。
 *
 * 保留的只是视觉：.page-hero / .guide-card / .btn 这些类名与排版沿用原来的静态页，
 * 所以观感是连续的；被删掉的是写死在 data() 里的 8 篇攻略、分类 tab、
 * 点赞收藏（那几个是 localStorage 假数据，真表里的计数器本轮不维护）。
 */
import ResultState from '@/components/ResultState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { searchGuides, listMyGuides, listGuideTags, deleteGuides } from '@/api/travel_guide'
import { icons } from '@/common/scenes'
import { fmtDate } from '@/common/format'
import { currentUser, isLogin, ensureLogin } from '@/common/user'

const PAGE_SIZE = 6

/* 正文首段的截断长度。摘要是渲染时从正文里抠的，不建新列 —— 这样它永远和正文同步 */
const EXCERPT_LEN = 90

export default {
  name: 'TravelGuidePage',
  components: { ResultState, SafeImage },
  data() {
    return {
      icons,
      fmtDate,

      items: [],
      tags: [],
      total: 0,
      page: 1,
      pageSize: PAGE_SIZE,
      loading: true,
      error: false,

      /* 已生效的筛选条件。注意 keyword（已提交）与 keywordInput（正在输入）是两个变量：
         输入框里的字没按搜索之前不该触发请求。 */
      keyword: '',
      keywordInput: '',
      activeTagId: '',
      mine: false,

      /* 竞态保护：快速连点标签时，先发的慢请求不能覆盖后发的快请求 */
      reqId: 0,

      writeTips: [
        '像写游记那样写，一段一件事',
        '段落之间空一行，页面上就会分段显示',
        '正文不能贴 HTML 标签，写了也只会当成文字显示',
        '标签随手打，没有的会自动建',
      ],
    }
  },
  computed: {
    listTitle() {
      if (this.mine) return `我的攻略 · 共 ${this.total} 篇`
      if (this.keyword) return `“${this.keyword}” · 共 ${this.total} 篇`
      return `全部攻略 · 共 ${this.total} 篇`
    },
    hasFilter() {
      return Boolean(this.keyword || this.activeTagId)
    },
    topTags() {
      return this.tags.slice(0, 8)
    },
  },
  watch: {
    /*
     * 路由的 query 是筛选条件的**唯一入口**：标签 chip、搜索、我的攻略
     * 都是先 push 一个新 query，再由这里统一落状态并请求一次。
     *
     * 这样做是因为攻略详情页上那排标签 chip 会跳回本页并带上 tagId ——
     * 如果本页已经在显示，只有 query 变了（组件被复用、created 不会再跑），
     * 不监听就什么都不会发生。
     */
    '$route.query': {
      handler(q) {
        this.applyRoute(q)
      },
    },
  },
  created() {
    this.fetchTags()
    this.applyRoute(this.$route.query)
  },
  methods: {
    /* ---------------- 路由 ↔ 状态 ---------------- */

    applyRoute(query) {
      this.mine = query.mine === '1'
      this.activeTagId = query.tagId ? String(query.tagId) : ''
      this.keyword = query.keyword ? String(query.keyword) : ''
      this.keywordInput = this.keyword
      this.page = 1
      this.load()
    },

    /**
     * 把筛选条件写进 URL 再让上面的 watcher 去请求，而不是直接改 data 再请求 ——
     * 状态只有一份（URL 里那串），不会出现「URL 说 tagId=3、页面上选的是 4」。
     * patch 里没提到的字段沿用当前值。
     */
    pushQuery(patch = {}) {
      const next = {
        mine: 'mine' in patch ? patch.mine : this.mine,
        tagId: 'tagId' in patch ? patch.tagId : this.activeTagId,
        keyword: 'keyword' in patch ? patch.keyword : this.keyword,
      }
      const query = {}
      if (next.mine) query.mine = '1'
      if (next.tagId) query.tagId = String(next.tagId)
      if (next.keyword) query.keyword = next.keyword
      this.$router.push({ path: '/index/travel_guide', query })
    },

    /* ---------------- 取数 ---------------- */

    async fetchTags() {
      try {
        const res = await listGuideTags()
        this.tags = res.data.code === 0 ? res.data.data || [] : []
      } catch (e) {
        // 字典拿不到不挡列表，只是筛不了标签，静默降级成「只有全部」
        this.tags = []
      }
    },

    async load() {
      const token = ++this.reqId
      this.loading = true
      this.error = false

      const params = { page: this.page, limit: this.pageSize }
      try {
        let res
        if (this.mine) {
          /* 我的攻略：不传 userAccount —— /page 是拿会话里的账号过滤的，
             前端传了也不作数（传错反而会筛出别人的）。 */
          res = await listMyGuides(params)
        } else {
          res = await searchGuides({
            ...params,
            tagId: this.activeTagId || undefined,
            keyword: this.keyword || undefined,
            sort: 'addTime',
            order: 'desc',
          })
        }
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

    /** 不改筛选条件地重拉（错误态的重试、翻页后的兜底） */
    reload() {
      this.load()
    },

    /* ---------------- 交互 ---------------- */

    setTag(tagId) {
      if (String(tagId) === String(this.activeTagId)) return
      this.pushQuery({ tagId: tagId || '' })
    },

    onSearch() {
      this.pushQuery({ keyword: this.keywordInput.trim() })
    },

    clearFilters() {
      this.pushQuery({ tagId: '', keyword: '' })
    },

    toggleMine() {
      // 进「我的攻略」要登录：那一页按作者过滤，没登录只会空着
      if (!this.mine && !isLogin()) {
        this.$message({ message: '登录后才能看「我的攻略」', type: 'info' })
        ensureLogin(this.$router, 'travel_guide')
        return
      }
      // 我的攻略视图没有标签 chips，顺手把标签清掉，两边状态不打架
      this.pushQuery({ mine: !this.mine, tagId: '' })
    },

    onPageChange(page) {
      this.page = page
      this.load() // 翻页只重拉，不重置页码
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    goDetail(item) {
      this.$router.push(`/index/travel_guide/detail/${item.id}`)
    },

    goWrite() {
      ensureLogin(this.$router, 'travel_guide').then((ok) => {
        if (ok) this.$router.push('/index/travel_guide/write')
      })
    },

    goEdit(item) {
      this.$router.push(`/index/travel_guide/write/${item.id}`)
    },

    async removeGuide(item) {
      try {
        await this.$confirm(`确定删除《${item.guideTitle}》吗？删除后不可恢复。`, '删除攻略', {
          confirmButtonText: '删除',
          cancelButtonText: '再想想',
          type: 'warning',
        })
      } catch (e) {
        return // 取消
      }
      try {
        const res = await deleteGuides([item.id])
        if (res.data.code === 0) {
          this.$message({ message: '已删除', type: 'success' })
          this.load()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      } catch (e) {
        this.$message.error('删除失败，请稍后再试')
      }
    },

    /* ---------------- 展示 ---------------- */

    /** 正文首段截断成摘要。空行分段，所以第一段就是到第一个空行为止 */
    excerpt(detail) {
      const text = String(detail || '')
        .split(/\n\s*\n/)[0]
        .replace(/\s+/g, ' ')
        .trim()
      if (!text) return '作者还没有写正文。'
      return text.length > EXCERPT_LEN ? text.slice(0, EXCERPT_LEN) + '…' : text
    },

    /* 老数据的 user_name 是空的（本轮之前的行没有这个值），回落到账号 */
    authorOf(item) {
      return item.userName || item.userAccount || '匿名游客'
    },
    authorInitial(item) {
      return this.authorOf(item).charAt(0)
    },
    tagNamesOf(item) {
      return item.tagNames || []
    },
    isMine(item) {
      const me = currentUser()
      return Boolean(me.account) && item.userAccount === me.account
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.travel-guide { background: var(--sand); min-height: 100vh; }

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
/* 页头里那两个按钮压在深色底上，白底的 ghost 按钮要自己带阴影才立得住 */
.page-hero__actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 18px; }

/* 标签筛选。.filter-bar / .chip 这些类是全局的（assets/main.css），这里只补篇数 */
.section { padding: 40px 0; }
.section--tabs { padding: 20px 0 0; }
.chip__num {
  margin-left: 6px; font-style: normal; font-size: 12px; opacity: .65;
}

/* 布局 */
.layout { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 24px; align-items: start; }
.list-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 20px; }
.list-title { color: var(--ink); font-size: 20px; font-weight: 700; }
.list-tip { color: var(--ink-3); font-size: 13px; }

/* 攻略卡片 */
.guide-card {
  display: flex; gap: 18px; padding: 18px; margin-bottom: 18px;
  background: var(--card); border: 1px solid var(--line);
  border-radius: var(--radius-lg); cursor: pointer;
  transition: transform .22s ease, box-shadow .22s ease, border-color .22s ease;
}
.guide-card:hover { transform: translateY(-4px); border-color: transparent; box-shadow: var(--shadow); }
.guide-card__cover {
  flex: none; width: 220px; height: 150px; border-radius: 12px;
  overflow: hidden; position: relative;
}
.guide-card__cover :deep(.safe-image) { width: 100%; height: 100%; object-fit: cover; }
.guide-card__body { flex: 1; display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.guide-card__title {
  margin: 0; color: var(--ink); font-size: 17px; font-weight: 700; line-height: 1.45;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.guide-card__desc {
  margin: 0; color: var(--ink-2); font-size: 13px; line-height: 1.7;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.guide-card__tags { list-style: none; margin: 0; padding: 0; display: flex; flex-wrap: wrap; gap: 6px; }
.guide-card__tags li { color: var(--brand-dark); font-size: 12px; }
.guide-card__meta {
  margin-top: auto; padding-top: 8px;
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
}
.guide-card__author { display: flex; align-items: center; gap: 8px; }
.guide-card__avatar {
  width: 30px; height: 30px; border-radius: 50%;
  background: var(--brand-soft); color: var(--brand);
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
}
.guide-card__author-name { color: var(--ink); font-size: 13px; font-weight: 600; }
.guide-card__author-date { color: var(--ink-3); font-size: 11px; }
.guide-card__ops { display: flex; gap: 8px; }
.op-btn {
  padding: 5px 12px; border: 1px solid var(--line); border-radius: 999px;
  background: #fff; color: var(--ink-2); font-size: 12px;
  font-family: inherit; cursor: pointer; transition: all .15s ease;
}
.op-btn:hover { border-color: var(--brand-light); color: var(--brand); }
.op-btn--danger:hover { border-color: #e5484d; color: #e5484d; }

/* 侧栏 */
.side-col { display: flex; flex-direction: column; gap: 18px; }
.side-block {
  padding: 20px; background: var(--card);
  border: 1px solid var(--line); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.side-title { margin: 0 0 14px; color: var(--ink); font-size: 15px; font-weight: 700; }
.side-empty { margin: 0; color: var(--ink-3); font-size: 13px; line-height: 1.7; }
.tag-cloud { list-style: none; margin: 0; padding: 0; display: flex; flex-wrap: wrap; gap: 8px; }
.tag-pill {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 5px 12px; border: 1px solid var(--line); border-radius: 999px;
  background: #fff; color: var(--ink-2); font-size: 13px;
  font-family: inherit; cursor: pointer; transition: all .15s ease;
}
.tag-pill:hover { border-color: var(--brand-light); color: var(--brand); }
.tag-pill em {
  font-style: normal; font-size: 11px;
  color: var(--brand); background: var(--brand-soft);
  padding: 1px 7px; border-radius: 999px;
}

.side-block--tip { background: var(--brand-soft); border-color: transparent; }
.check-list { list-style: none; margin: 0; padding: 0; }
.check-list li {
  display: flex; gap: 8px; padding: 6px 0;
  color: var(--ink-2); font-size: 13px; line-height: 1.6;
}
.check-dot { color: var(--brand); font-weight: 700; }

/* 骨架 / 分页 / 空态 */
.skel-list { display: flex; flex-direction: column; gap: 18px; }
.skel-guide { height: 186px; border-radius: var(--radius-lg); }
.pager { display: flex; justify-content: center; margin-top: 28px; }
.state-reset { margin-top: 6px; }

/* 入场 */
.reveal { animation: fadeUp .5s cubic-bezier(.22,.8,.3,1) both; }
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: none; } }

@media (max-width: 1080px) {
  .layout { grid-template-columns: 1fr; }
  .side-col { order: -1; }
  .page-hero__title { font-size: 34px; }
}
@media (max-width: 720px) {
  .guide-card { flex-direction: column; }
  .guide-card__cover { width: 100%; height: 180px; }
  .page-hero { padding: 48px 0 60px; }
  .page-hero__title { font-size: 28px; }
  .page-hero__search { flex-direction: column; border-radius: 20px; }
  .search-input { padding: 12px 16px; }
}
</style>
