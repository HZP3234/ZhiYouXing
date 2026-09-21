<template>
  <!-- 根节点必须恰好一个：index.vue 的 $route 监听读 #scrollView.offsetTop，靠属性透传落到这里 -->
  <div class="page">
    <PageHero kicker="酒店信息" title="住得舒服，行程才走得远" :desc="heroDesc" scene="second">
      <div class="hero-stats">
        <span><strong>{{ hotels.length || '—' }}</strong> 家酒店</span>
        <span><strong>{{ typeCount || '—' }}</strong> 种房型可选</span>
        <span v-if="minPrice !== null"><strong>¥{{ minPrice }}</strong> 最低起价 / 晚</span>
      </div>
    </PageHero>

    <div class="container">
      <!--
        酒店 / 客房是两张表：hotel_info 一家酒店一行（只放行已过审的），room_type 是客房
        （归属列 hotel_name）。这里是两次请求各取全量（api 的 listHotels / listRooms），
        在客户端按 hotel_name 把客房挂到酒店上，再由房型/价格筛客房、反推该出现哪些酒店
        （见 computed.hotels）。

        为什么要全量而不是分页：分页会把同一家酒店的客房拆到两页，聚出两个各残缺一半的
        「酒店」；而且要按「筛完客房之后还有没有剩」来决定酒店去留，必须拿到完整的客房集。
        分页本身交给 pagedHotels 在**聚合后的酒店数组**上做，el-pagination 的 total 是
        酒店数（hotels.length），不会飘。上限见 api/hotel.js 的 FETCH_LIMIT。
      -->
      <div class="filter-bar">
        <div class="filter-row">
          <span class="filter-row__label">房型</span>
          <div class="chip-row">
            <button class="chip" :class="{ 'is-active': !filters.roomType }" @click="setType('')">全部</button>
            <button
              v-for="t in types"
              :key="t"
              class="chip"
              :class="{ 'is-active': filters.roomType === t }"
              @click="setType(t)"
            >
              {{ t }}
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
          <!-- 关键词落在哪一列由后端 LIKE 决定，只能二选一，不能同时传（同时传是 AND，必然查不到） -->
          <div class="scope-switch">
            <button class="chip chip--scope" :class="{ 'is-active': scope === 'hotel' }" @click="setScope('hotel')">按酒店</button>
            <button class="chip chip--scope" :class="{ 'is-active': scope === 'room' }" @click="setScope('room')">按房型</button>
          </div>
          <el-input
            v-model="keyword"
            :placeholder="scope === 'hotel' ? '输入酒店名，例如「青海湖大酒店」' : '输入房型名，例如「大床房」'"
            clearable
            class="filter-search"
            @input="onKeywordInput"
            @clear="applyKeyword"
          >
            <template #prefix><i class="meta-ico" v-html="icons.search"></i></template>
          </el-input>
          <button v-if="hasFilter" class="chip chip--reset" @click="resetFilters">清空全部筛选</button>
        </div>
      </div>

      <div v-if="loading" class="grid-cards list-grid">
        <div v-for="n in pageSize" :key="n" class="skeleton skel-card"></div>
      </div>

      <ResultState
        v-else-if="error"
        status="error"
        text="酒店列表加载失败"
        hint="接口没有响应。若正在用真实数据，请确认网关或直连的三个服务已启动。"
        @retry="fetchList"
      />

      <ResultState
        v-else-if="!hotels.length"
        status="empty"
        text="没有匹配的酒店"
        hint="换个房型分类、放宽价格区间，或者清空关键词试试。"
      >
        <button class="btn btn-ghost state-reset" @click="resetFilters">清空筛选条件</button>
      </ResultState>

      <template v-else>
        <div class="grid-cards list-grid">
          <article
            v-for="(h, i) in pagedHotels"
            :key="h.hotelName"
            class="card card--link reveal"
            :style="{ animationDelay: Math.min(i, 8) * 60 + 'ms' }"
            @click="goHotel(h)"
          >
            <div class="card__media">
              <SafeImage :src="h.image" :seed="h.id" ratio="16 / 10" :alt="h.hotelName" />
              <span v-if="h.cheapest && h.cheapest.roomType" class="card__badge tag tag--overlay">{{ h.cheapest.roomType }}</span>
              <span v-if="h.stock === 0" class="card__corner tag tag--price">已订满</span>
            </div>

            <div class="card__body">
              <h3 class="card__title">{{ h.hotelName }}</h3>

              <div class="card__tags">
                <span v-for="t in h.types" :key="t" class="tag">{{ t }}</span>
              </div>

              <p class="card__meta">
                <i class="meta-ico" v-html="icons.pin"></i>
                <span>{{ h.address }}</span>
              </p>
              <p class="card__meta">
                <i class="meta-ico" v-html="icons.bed"></i>
                <span>{{ h.rooms.length }} 个房型 · 剩 {{ h.stock }} 间</span>
              </p>

              <div class="card__foot">
                <div v-if="h.minPrice !== null" class="card__price"><small>¥</small>{{ fmtMoney(h.minPrice) }}<em>起 / 晚</em></div>
                <div v-else class="card__price"><em>暂无在售房型</em></div>
                <div class="card__heat">
                  <span><i class="meta-ico" v-html="icons.like"></i>{{ h.clickNum }}</span>
                  <span><i class="meta-ico" v-html="icons.comment"></i>{{ h.discussNum }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div class="pager">
          <el-pagination
            background
            layout="prev, pager, next, total"
            :total="hotels.length"
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
import { listHotels, listRooms } from '@/api/hotel'
import { icons } from '@/common/scenes'
import { imageList } from '@/common/media'
import { fmtMoney } from '@/common/format'

const PAGE_SIZE = 9
const num = (v) => Number(v) || 0

export default {
  name: 'HotelInfoList',
  data() {
    return {
      icons,
      // 已过审的酒店（hotel_info 一行 = 一家）
      hotelsRaw: [],
      // 这些酒店的客房（room_type 一行 = 一个房型）。酒店卡片由它聚合而来，见 computed.hotels
      rooms: [],
      page: 1,
      pageSize: PAGE_SIZE,
      loading: true,
      error: false,

      // 房型/价格筛的是**客房**；关键词按 scope 落在酒店名或客房名上
      filters: { roomType: '', roomPriceStart: '', roomPriceEnd: '' },
      keyword: '',
      scope: 'hotel',
      priceKey: 'all',
      sortKey: 'default',

      sortOptions: [
        { key: 'default', label: '综合' },
        { key: 'hot', label: '热度' },
        { key: 'priceAsc', label: '价格从低到高' },
        { key: 'priceDesc', label: '价格从高到低' },
      ],

      pricePresets: [
        { key: 'all', label: '不限', start: '', end: '' },
        { key: 'low', label: '¥300 以下', start: '', end: 300 },
        { key: 'mid', label: '¥300 - 600', start: 300, end: 600 },
        { key: 'high', label: '¥600 以上', start: 600, end: '' },
      ],

      reqId: 0,
      keywordTimer: null,
    }
  },
  computed: {
    heroDesc() {
      return '先挑酒店，再挑这家店的床型：点开卡片就能看到房型设施、剩余房数和真实住客点评。'
    },

    /**
     * 房型 chips 的取值：当前在售客房出现过的 roomType。
     *
     * 以前是从 room_type 字典表拉一份固定清单，现在 room_type 就是客房表本身，
     * 没有「房型字典」这个接口了 —— 直接看客房数据里有哪些房型更准
     * （某家酒店没上架的房型不该出现在筛选里）。
     */
    types() {
      return [...new Set(this.rooms.map((r) => r.roomType).filter(Boolean))]
    },

    /** 客房先按房型 / 价格 / 关键词（按房型搜索时）筛一遍，筛剩下的才轮到酒店 */
    filteredRooms() {
      const kw = this.scope === 'room' ? this.keyword.trim() : ''
      return this.rooms.filter((r) => {
        if (this.filters.roomType && r.roomType !== this.filters.roomType) return false
        if (kw && !String(r.roomName || '').includes(kw)) return false
        const price = num(r.roomPrice)
        if (this.filters.roomPriceStart !== '' && price < Number(this.filters.roomPriceStart)) return false
        if (this.filters.roomPriceEnd !== '' && price > Number(this.filters.roomPriceEnd)) return false
        return true
      })
    },

    /** 有没有在筛**客房**。有的话，一间都不剩的酒店整体不出现（而不是显示成 0 间） */
    hasRoomFilter() {
      return Boolean(
        this.filters.roomType ||
          (this.scope === 'room' && this.keyword.trim()) ||
          this.filters.roomPriceStart !== '' ||
          this.filters.roomPriceEnd !== '',
      )
    },

    /**
     * 酒店卡片列表：以**酒店表**为准（一家一张卡），再把筛剩下的客房按 hotel_name 挂上去。
     *
     * 展示值一律取聚合结果：起价 = 该店最低房价（minPrice），图取最便宜房型的首图
     * （没有客房时退回酒店自己的图），房型标签取该店在售的 roomType。
     * 排序在客户端做（两次请求各自取全量，后端那份排序传不进来），见 sortHotels。
     */
    hotels() {
      const byHotel = new Map()
      for (const r of this.filteredRooms) {
        const name = r.hotelName || '未标注酒店'
        if (!byHotel.has(name)) byHotel.set(name, [])
        byHotel.get(name).push(r)
      }

      const kw = this.scope === 'hotel' ? this.keyword.trim() : ''
      const list = []
      for (const h of this.hotelsRaw) {
        const name = h.hotelName || '未标注酒店'
        if (kw && !name.includes(kw)) continue

        const rooms = (byHotel.get(name) || []).sort((a, b) => num(a.roomPrice) - num(b.roomPrice))
        if (this.hasRoomFilter && !rooms.length) continue

        const cheapest = rooms[0] || null
        list.push({
          id: h.id,
          hotelName: name,
          address: h.hotelAddress || '',
          clickTime: h.clickTime || '',
          clickNum: num(h.clickNum),
          discussNum: num(h.discussNum),
          rooms,
          cheapest,
          minPrice: cheapest ? num(cheapest.roomPrice) : null,
          image:
            (cheapest && (imageList(cheapest.roomImage)[0] || cheapest.roomImage)) ||
            imageList(h.hotelImage)[0] ||
            h.hotelImage ||
            '',
          stock: rooms.reduce((sum, r) => sum + num(r.roomCount), 0),
          types: [...new Set(rooms.map((r) => r.roomType).filter(Boolean))],
        })
      }
      return this.sortHotels(list)
    },

    /** 本地分页：分的是**酒店**，不是客房行 */
    pagedHotels() {
      const start = (this.page - 1) * this.pageSize
      return this.hotels.slice(start, start + this.pageSize)
    },

    /** 当前结果里出现过的房型种类数（不是某张字典表的总数） */
    typeCount() {
      return this.types.length
    },

    /** 全量最低起价。数据本来就一次取全，这里不是「当前页最低」 */
    minPrice() {
      const nums = this.hotels.map((h) => h.minPrice).filter((n) => n !== null && Number.isFinite(n))
      return nums.length ? Math.min(...nums) : null
    },

    hasFilter() {
      return Boolean(
        this.filters.roomType ||
          this.keyword ||
          this.filters.roomPriceStart !== '' ||
          this.filters.roomPriceEnd !== '',
      )
    },
  },
  created() {
    // 首页搜索框带来的关键词，默认按酒店名找 —— 这是住客的直觉
    const kw = this.$route.query.indexQueryCondition
    if (kw) this.keyword = String(kw)

    this.fetchList()
  },
  methods: {
    fmtMoney,

    /** 进二级页挑这家酒店的床型。名字可能带中文，交给 router 编码，页面侧读回的是解码后的原文 */
    goHotel(hotel) {
      this.$router.push(`/index/hotel_info/hotel/${encodeURIComponent(hotel.hotelName)}`)
    },

    /**
     * 排序。以前是把 sort/order 透传给后端（客房排好序、酒店顺序跟着走），
     * 现在数据是本地的两张全量表，排序只能自己写。
     * 没客房的酒店排最后（起价视作 +∞ / -1），别让「已订满」的店占着头位。
     */
    sortHotels(list) {
      const priceOf = (h) => (h.minPrice === null ? null : Number(h.minPrice))
      const cmp = {
        // 综合：最近有动静的排前面（clickTime 与 addTime 同源，相等时比浏览数）
        default: (a, b) => String(b.clickTime).localeCompare(String(a.clickTime)) || b.clickNum - a.clickNum,
        hot: (a, b) => b.clickNum - a.clickNum,
        priceAsc: (a, b) => (priceOf(a) ?? Infinity) - (priceOf(b) ?? Infinity),
        priceDesc: (a, b) => (priceOf(b) ?? -1) - (priceOf(a) ?? -1),
      }
      return list.sort(cmp[this.sortKey] || cmp.default)
    },

    /** 两次请求各取全量：酒店 + 客房。筛选/排序都在本地，所以只在进页面和重试时才拉 */
    async fetchList() {
      const token = ++this.reqId
      this.loading = true
      this.error = false

      try {
        const [hotelRes, roomRes] = await Promise.all([listHotels(), listRooms()])
        if (token !== this.reqId) return
        if (hotelRes.data.code === 0 && roomRes.data.code === 0) {
          this.hotelsRaw = hotelRes.data.data.list || []
          this.rooms = roomRes.data.data.list || []
        } else {
          this.error = true
        }
      } catch (e) {
        if (token !== this.reqId) return
        this.error = true
        this.hotelsRaw = []
        this.rooms = []
      } finally {
        if (token === this.reqId) this.loading = false
      }
    },

    /** 筛选条件变了只回第一页，不用重新请求（数据已经是全量） */
    applyFilters() {
      this.page = 1
    },

    /**
     * 关键词只作用于当前 scope 那一侧：按酒店时筛酒店名，按房型时筛客房名。
     * 两边同时筛是 AND 语义（「这家店里叫这个名字的房型」），与页面上二选一的开关意图不符。
     */
    applyKeyword() {
      this.applyFilters()
    },

    setScope(scope) {
      this.scope = scope
    },

    setType(type) {
      this.filters.roomType = type
      this.applyFilters()
    },

    setPrice(preset) {
      this.priceKey = preset.key
      this.filters.roomPriceStart = preset.start
      this.filters.roomPriceEnd = preset.end
      this.applyFilters()
    },

    setSort(option) {
      this.sortKey = option.key
      this.applyFilters()
    },

    onKeywordInput() {
      clearTimeout(this.keywordTimer)
      this.keywordTimer = setTimeout(() => this.applyKeyword(), 300)
    },

    /** 翻页只切本地页码，**不重新请求**（数据已经是全量） */
    onPageChange(page) {
      this.page = page
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    resetFilters() {
      this.filters = { roomType: '', roomPriceStart: '', roomPriceEnd: '' }
      this.keyword = ''
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

.scope-switch {
  display: flex;
  gap: 8px;
}

.chip--scope {
  padding: 7px 14px;
  font-size: 13px;
}

.chip--reset {
  border-style: dashed;
  color: var(--ink-3);
}

.card__tags {
  margin-top: -2px;
}

.state-reset {
  margin-top: 6px;
}
</style>
