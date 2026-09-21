<template>
  <!--
    根节点必须恰好一个：index.vue 的 $route 监听里读 document.getElementById('scrollView').offsetTop，
    那个 id 是靠属性透传落到本组件根元素上的。多根节点会让 attrs 落不下来，
    于是每次路由切换都在 watcher 里抛 TypeError。
  -->
  <div class="page">
    <PageHero kicker="热门景点" title="全国热门景区，一次看完" :desc="heroDesc" scene="first">
      <div class="hero-stats">
        <span><strong>{{ total || '—' }}</strong> 处景区</span>
        <span><strong>{{ types.length || '—' }}</strong> 种类型</span>
        <span><strong>24</strong> 小时可收藏</span>
      </div>
    </PageHero>

    <div class="container">
      <div class="filter-bar">
        <!-- 类型：接口驱动，不硬编码。切真实接口后字典会变成「景点类型1..8」，页面照样能筛 -->
        <div class="filter-row">
          <span class="filter-row__label">类型</span>
          <div class="chip-row">
            <button class="chip" :class="{ 'is-active': !filters.attractionType }" @click="setType('')">全部</button>
            <button
              v-for="t in types"
              :key="t.id"
              class="chip"
              :class="{ 'is-active': filters.attractionType === t.attractionType }"
              @click="setType(t.attractionType)"
            >
              {{ t.attractionType }}
            </button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-row__label">价格</span>
          <div class="chip-row">
            <button
              v-for="p in pricePresets"
              :key="p.label"
              class="chip"
              :class="{ 'is-active': priceKey === p.key }"
              @click="setPrice(p)"
            >
              {{ p.label }}
            </button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-row__label">排序</span>
          <div class="chip-row">
            <button
              v-for="s in sortOptions"
              :key="s.key"
              class="chip"
              :class="{ 'is-active': sortKey === s.key }"
              @click="setSort(s)"
            >
              {{ s.label }}
            </button>
          </div>
        </div>

        <div class="filter-row">
          <span class="filter-row__label">搜索</span>
          <el-input
            v-model="filters.attractionName"
            placeholder="输入景点名称，例如「莫高窟」"
            clearable
            class="filter-search"
            @input="onKeywordInput"
            @clear="applyFilters"
          >
            <template #prefix><i class="meta-ico" v-html="icons.search"></i></template>
          </el-input>
          <button v-if="hasFilter" class="chip chip--reset" @click="resetFilters">清空全部筛选</button>
        </div>
      </div>

      <!-- 加载中：9 张骨架卡，保持和真实网格一样的占位，避免高度跳动 -->
      <div v-if="loading" class="grid-cards list-grid">
        <div v-for="n in pageSize" :key="n" class="skeleton skel-card"></div>
      </div>

      <ResultState
        v-else-if="error"
        status="error"
        text="景点列表加载失败"
        hint="接口没有响应。若正在用真实数据，请确认网关或直连的三个服务已启动。"
        @retry="fetchList"
      />

      <ResultState
        v-else-if="!items.length"
        status="empty"
        text="没有匹配的景点"
        hint="换个类型、放宽价格区间，或者清空关键词试试。"
      >
        <button class="btn btn-ghost state-reset" @click="resetFilters">清空筛选条件</button>
      </ResultState>

      <template v-else>
        <div class="grid-cards list-grid">
          <article
            v-for="(item, i) in items"
            :key="item.id"
            class="card card--link reveal"
            :style="{ animationDelay: Math.min(i, 8) * 60 + 'ms' }"
            @click="goDetail(item)"
          >
            <div class="card__media">
              <SafeImage :src="item.image" :seed="item.id" ratio="16 / 10" :alt="item.attractionName" />
              <span class="card__badge tag tag--overlay">{{ item.attractionType }}</span>
              <span v-if="isFree(item)" class="card__corner tag tag--teal">免费</span>
            </div>

            <div class="card__body">
              <h3 class="card__title">{{ item.attractionName }}</h3>

              <p class="card__meta">
                <i class="meta-ico" v-html="icons.pin"></i>
                <span>{{ item.attractionLocation }}</span>
              </p>
              <p class="card__meta">
                <i class="meta-ico" v-html="icons.clock"></i>
                <span>{{ item.openingHours }}</span>
              </p>

              <div class="card__foot">
                <div v-if="isFree(item)" class="card__price card__price--free">免费开放</div>
                <div v-else class="card__price"><small>¥</small>{{ fmtMoney(item.ticketPrice) }}<em>起</em></div>

                <div class="card__heat">
                  <span><i class="meta-ico" v-html="icons.like"></i>{{ item.thumbsUpNum }}</span>
                  <span><i class="meta-ico" v-html="icons.comment"></i>{{ item.discussNum }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div class="pager">
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
  </div>
</template>

<script>
import { listAttractions, listAttractionTypes } from '@/api/attraction'
import { icons } from '@/common/scenes'
import { fmtMoney } from '@/common/format'

const PAGE_SIZE = 9

export default {
  name: 'AttractionList',
  data() {
    return {
      icons,
      items: [],
      types: [],
      total: 0,
      page: 1,
      pageSize: PAGE_SIZE,
      loading: true,
      error: false,

      // 真正发给后端的字段，和后端实体字段同名
      filters: { attractionType: '', attractionName: '', ticketPriceStart: '', ticketPriceEnd: '' },
      priceKey: 'all',
      sortKey: 'default',

      sortOptions: [
        { key: 'default', label: '综合', sort: 'clickTime', order: 'desc' },
        { key: 'hot', label: '热度', sort: 'thumbsUpNum', order: 'desc' },
        { key: 'priceAsc', label: '价格从低到高', sort: 'ticketPrice', order: 'asc' },
        { key: 'priceDesc', label: '价格从高到低', sort: 'ticketPrice', order: 'desc' },
      ],

      pricePresets: [
        { key: 'all', label: '不限', start: '', end: '' },
        { key: 'low', label: '¥100 以下', start: '', end: 100 },
        { key: 'mid', label: '¥100 - 200', start: 100, end: 200 },
        { key: 'high', label: '¥200 以上', start: 200, end: '' },
      ],

      // 竞态保护：快速连点筛选时，先发的慢请求不能覆盖后发的快请求
      reqId: 0,
      keywordTimer: null,
    }
  },
  computed: {
    heroDesc() {
      return '按类型、门票价格和热度自由组合筛选，点开任意一处可以看详细介绍、门票和真实点评。'
    },
    hasFilter() {
      return Boolean(
        this.filters.attractionType || this.filters.attractionName || this.filters.ticketPriceStart !== '' || this.filters.ticketPriceEnd !== '',
      )
    },
  },
  created() {
    /*
     * 首页和导航的搜索框带的是 indexQueryCondition，但后端 MPUtil 只认实体字段名，
     * 这个参数会被**静默丢弃**（不报错，只是筛不出来）。
     * 所以在这里翻译成 attractionName 再发出去。
     */
    const kw = this.$route.query.indexQueryCondition
    if (kw) this.filters.attractionName = String(kw)

    this.fetchTypes()
    this.fetchList()
  },
  methods: {
    fmtMoney,
    isFree(item) {
      return Number(item.ticketPrice) === 0
    },
    goDetail(item) {
      this.$router.push(`/index/attraction/detail/${item.id}`)
    },

    async fetchTypes() {
      try {
        const res = await listAttractionTypes()
        this.types = res.data.code === 0 ? res.data.data.list || [] : []
      } catch (e) {
        // 字典拿不到不影响列表，只是筛不了类型，静默降级
        this.types = []
      }
    },

    async fetchList() {
      const token = ++this.reqId
      this.loading = true
      this.error = false

      const sort = this.sortOptions.find((s) => s.key === this.sortKey) || this.sortOptions[0]
      const params = { page: this.page, limit: this.pageSize, sort: sort.sort, order: sort.order }
      for (const [k, v] of Object.entries(this.filters)) {
        if (v !== '' && v !== null && v !== undefined) params[k] = v
      }

      try {
        const res = await listAttractions(params)
        if (token !== this.reqId) return // 已经被更新的请求取代，丢弃
        if (res.data.code === 0) {
          this.items = res.data.data.list || []
          this.total = res.data.data.totalCount || 0
        } else {
          this.error = true
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

    /** 任何筛选条件变化都走这里：页码必须归 1，否则在第 3 页筛选会筛出空列表 */
    applyFilters() {
      this.page = 1
      this.fetchList()
    },

    setType(type) {
      this.filters.attractionType = type
      this.applyFilters()
    },

    setPrice(preset) {
      this.priceKey = preset.key
      this.filters.ticketPriceStart = preset.start
      this.filters.ticketPriceEnd = preset.end
      this.applyFilters()
    },

    setSort(option) {
      this.sortKey = option.key
      this.applyFilters()
    },

    onKeywordInput() {
      clearTimeout(this.keywordTimer)
      this.keywordTimer = setTimeout(() => this.applyFilters(), 300)
    },

    onPageChange(page) {
      this.page = page
      this.fetchList() // 翻页只重拉，不重置页码
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    resetFilters() {
      this.filters = { attractionType: '', attractionName: '', ticketPriceStart: '', ticketPriceEnd: '' }
      this.priceKey = 'all'
      this.sortKey = 'default'
      this.applyFilters()
    },
  },
}
</script>

<style scoped>
.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  color: var(--ink-2);
  font-size: 14px;
}

.hero-stats strong {
  margin-right: 4px;
  color: var(--brand);
  font-size: 22px;
  font-weight: 800;
}

.list-grid {
  margin-top: 32px;
}

.skel-card {
  aspect-ratio: 16 / 11;
}

.filter-search {
  flex: 1;
  min-width: 220px;
  max-width: 340px;
}

.filter-search :deep(.el-input__wrapper) {
  border-radius: 999px;
  box-shadow: inset 0 0 0 1px var(--line);
}

.filter-search :deep(.el-input__prefix) {
  color: var(--ink-3);
}

.chip--reset {
  border-style: dashed;
  color: var(--ink-3);
}

.card__price--free {
  color: var(--teal);
  font-size: 18px;
}

.state-reset {
  margin-top: 6px;
}
</style>
