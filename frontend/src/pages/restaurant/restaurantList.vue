<template>
  <!-- 根节点必须恰好一个：index.vue 的 $route 监听读 #scrollView.offsetTop，靠属性透传落到这里 -->
  <div class="page">
    <PageHero kicker="美食餐厅" title="到一个地方，先吃本地那一口" :desc="heroDesc" scene="third">
      <div class="hero-bar">
        <div class="hero-stats">
          <span><strong>{{ total || '—' }}</strong> 家餐厅</span>
          <span v-if="minCost !== null"><strong>¥{{ minCost }}</strong> 当前页最低人均</span>
          <span><strong>18</strong> 小时可预约</span>
        </div>
        <!--
          预约记录本身落在个人中心，但用户是在餐厅这条线上下的单，
          回到这里找不到入口就只能靠记忆去翻个人中心。直接给一个。
        -->
        <button class="btn btn-ghost hero-bar__link" @click="goMyReservations">
          <i class="meta-ico" v-html="icons.clock"></i>我的预约
        </button>
      </div>
    </PageHero>

    <div class="container">
      <!--
        ⚠️ 餐厅这里只有「排序 + 关键词」，没有分类 chips、也没有价格区间。
        原因是后端 restaurant 主表**根本没有分类字段**（restaurant_type 只在预约表上，
        且可为空），而 avg_cost 是 varchar 不是数字 —— 真实库存的是「人均消费1」这种文案，
        传 xxxStart/xxxEnd 后端也没有对应参数。所以列表页刻意不提供「按人均排序」：
        后端对它是字典序，'100' 会排在 '88' 前面，做出来只会让人困惑。
      -->
      <div class="filter-bar">
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
            v-model="filters.restaurantName"
            placeholder="输入餐厅名或招牌菜，例如「泡馍」"
            clearable
            class="filter-search"
            @input="onKeywordInput"
            @clear="applyFilters"
          >
            <template #prefix><i class="meta-ico" v-html="icons.search"></i></template>
          </el-input>
          <button v-if="hasFilter" class="chip chip--reset" @click="resetFilters">清空筛选</button>
        </div>

        <p class="filter-note">餐厅没有分类字段，按人气、点赞或直接搜招牌菜名来找就行。</p>
      </div>

      <div v-if="loading" class="grid-cards list-grid">
        <div v-for="n in pageSize" :key="n" class="skeleton skel-card"></div>
      </div>

      <ResultState
        v-else-if="error"
        status="error"
        text="餐厅列表加载失败"
        hint="接口没有响应。若正在用真实数据，请确认网关或直连的三个服务已启动。"
        @retry="fetchList"
      />

      <ResultState
        v-else-if="!items.length"
        status="empty"
        text="没有匹配的餐厅"
        hint="换个关键词试试，或者清空筛选条件看全部。"
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
              <SafeImage :src="item.restaurantImage" :seed="item.id" ratio="16 / 10" :alt="item.restaurantName" />
              <span v-if="dishesOf(item).length" class="card__badge tag tag--overlay">招牌 · {{ dishesOf(item)[0] }}</span>
            </div>

            <div class="card__body">
              <h3 class="card__title">{{ item.restaurantName }}</h3>

              <div class="card__tags">
                <span v-for="d in dishesOf(item).slice(0, 4)" :key="d" class="tag">{{ d }}</span>
              </div>

              <p class="card__meta">
                <i class="meta-ico" v-html="icons.clock"></i>
                <span>{{ item.businessHours }}</span>
              </p>
              <p class="card__meta">
                <i class="meta-ico" v-html="icons.pin"></i>
                <span>{{ item.restaurantAddress }}</span>
              </p>

              <div class="card__foot">
                <!-- avgCost 是字符串：能抠出数字才加 ¥，抠不出（真库的「人均消费1」）就原样显示，绝不编造金额 -->
                <div class="card__price" :class="{ 'card__price--plain': costOf(item).amount === null }">
                  <template v-if="costOf(item).amount !== null"><small>人均 ¥</small>{{ costOf(item).amount }}</template>
                  <template v-else>{{ costOf(item).text }}</template>
                </div>

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
import { listRestaurants } from '@/api/restaurant'
import { icons } from '@/common/scenes'
import { splitTags } from '@/common/media'
import { parseAmount } from '@/common/format'

const PAGE_SIZE = 9

export default {
  name: 'RestaurantList',
  data() {
    return {
      icons,
      items: [],
      total: 0,
      page: 1,
      pageSize: PAGE_SIZE,
      loading: true,
      error: false,

      // 字段名逐字对齐 RestaurantEntity：只有 restaurantName 一个可用筛选字段
      filters: { restaurantName: '' },
      sortKey: 'default',

      sortOptions: [
        { key: 'default', label: '综合', sort: 'addTime', order: 'desc' },
        { key: 'hot', label: '人气', sort: 'clickNum', order: 'desc' },
        { key: 'like', label: '点赞最多', sort: 'thumbsUpNum', order: 'desc' },
        { key: 'star', label: '收藏最多', sort: 'storeUpNum', order: 'desc' },
      ],

      reqId: 0,
      keywordTimer: null,
    }
  },
  computed: {
    heroDesc() {
      return '按人气、点赞和收藏排序，点开可以看招牌菜、营业时间、联系方式和其他食客的真实点评。'
    },
    /** 当前页最低人均。只统计能抠出数字的那些，抠不出的不参与 */
    minCost() {
      const nums = this.items.map((r) => parseAmount(r.avgCost)).filter((n) => n !== null)
      return nums.length ? Math.min(...nums) : null
    },
    hasFilter() {
      return Boolean(this.filters.restaurantName)
    },
  },
  created() {
    const kw = this.$route.query.indexQueryCondition
    if (kw) this.filters.restaurantName = String(kw)

    this.fetchList()
  },
  methods: {
    dishesOf(item) {
      return splitTags(item.signatureDish)
    },
    /**
     * 人均消费的展示形态。
     * mock 里是 '26' 这类数字串 → { amount: 26 }；真实库里是「人均消费1」→ { amount: null, text: '人均消费1' }。
     * 后者绝不能显示成「¥1」—— 那个 1 是序号不是金额。
     */
    costOf(item) {
      const amount = parseAmount(item.avgCost)
      if (amount !== null) return { amount, text: '' }
      return { amount: null, text: item.avgCost ? String(item.avgCost) : '人均以到店为准' }
    },
    goDetail(item) {
      this.$router.push(`/index/restaurant/detail/${item.id}`)
    },
    /** 带 tab 参数直达个人中心的「预约」页，别让用户自己再去里面找 */
    goMyReservations() {
      this.$router.push({ path: '/index/center', query: { tab: 'reservation' } })
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
        const res = await listRestaurants(params)
        if (token !== this.reqId) return
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

    applyFilters() {
      this.page = 1
      this.fetchList()
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
      this.fetchList()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    resetFilters() {
      this.filters = { restaurantName: '' }
      this.sortKey = 'default'
      this.applyFilters()
    },
  },
}
</script>

<style scoped>
.hero-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

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

/* 跟 hero 里的数字同一行，所以比标准按钮矮一档，不然会把这行撑得很空 */
.hero-bar__link {
  height: 38px;
  padding: 0 18px;
  font-size: 14px;
}

.hero-bar__link .meta-ico {
  display: inline-flex;
  width: 15px;
  height: 15px;
}

/*
 * 图标是 v-html 塞进来的，拿不到 scoped 的 data-v 属性，`svg` 那一层必须用 :deep()，
 * 否则选择器编成 `.meta-ico svg[data-v-x]` 永远匹配不上，图标会撑成 svg 默认尺寸。
 */
.hero-bar__link :deep(svg) {
  width: 15px;
  height: 15px;
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

.filter-note {
  padding: 0 0 14px;
  color: var(--ink-3);
  font-size: 12.5px;
}

.chip--reset {
  border-style: dashed;
  color: var(--ink-3);
}

/* 抠不出金额时显示的是原文案，不该用价格的橙色和大字号 */
.card__price--plain {
  color: var(--ink-3);
  font-size: 14px;
  font-weight: 600;
}

.state-reset {
  margin-top: 6px;
}
</style>
