<template>
  <!-- 根节点必须恰好一个：index.vue 的 $route 监听读 #scrollView.offsetTop，靠属性透传落到这里 -->
  <div class="page">
    <PageHero
      kicker="选择房型"
      :title="hotelName"
      :desc="heroDesc"
      :scene="hotelName"
    >
      <div v-if="!loading && !error && rooms.length" class="hero-stats">
        <span><strong>{{ rooms.length }}</strong> 个房型可选</span>
        <span v-if="minPrice !== null"><strong>¥{{ minPrice }}</strong> 起 / 晚</span>
        <span><strong>{{ totalStock }}</strong> 间剩余</span>
      </div>
    </PageHero>

    <div class="container">
      <nav class="crumb">
        <button class="crumb__link" @click="$router.push('/index/hotel_info')">酒店信息</button>
        <span class="crumb__sep">/</span>
        <span class="crumb__cur">{{ hotelName }}</span>
      </nav>

      <div v-if="loading" class="grid-cards">
        <div v-for="n in 3" :key="n" class="skeleton skel-card"></div>
      </div>

      <ResultState
        v-else-if="error"
        status="error"
        text="房型加载失败"
        hint="接口没有响应。若正在用真实数据，请确认网关或直连的酒店服务已启动。"
        @retry="fetchList"
      >
        <button class="btn btn-ghost" @click="$router.push('/index/hotel_info')">返回酒店列表</button>
      </ResultState>

      <ResultState
        v-else-if="!rooms.length"
        status="empty"
        text="这家酒店暂时没有在售房型"
        hint="房型可能已下架或已订满。换一家酒店看看，或者回列表重新筛选。"
      >
        <button class="btn btn-ghost" @click="$router.push('/index/hotel_info')">返回酒店列表</button>
      </ResultState>

      <template v-else>
        <div class="grid-cards">
          <article
            v-for="(r, i) in rooms"
            :key="r.id"
            class="card card--link reveal"
            :style="{ animationDelay: Math.min(i, 8) * 60 + 'ms' }"
            @click="goDetail(r)"
          >
            <div class="card__media">
              <SafeImage :src="imageOf(r)" :seed="r.id" ratio="16 / 10" :alt="r.roomName" />
              <span class="card__badge tag tag--overlay">{{ r.roomType }}</span>
              <span v-if="soldOut(r)" class="card__corner tag tag--price">已订满</span>
            </div>

            <div class="card__body">
              <h3 class="card__title">{{ r.roomName }}</h3>

              <div class="card__tags">
                <span v-for="f in facilitiesOf(r)" :key="f" class="tag">{{ f }}</span>
              </div>

              <p class="card__meta">
                <i class="meta-ico" v-html="icons.bed"></i>
                <span :class="{ 'is-tight': isTight(r) }">{{ stockText(r) }}</span>
              </p>
              <p v-if="r.hotelName" class="card__meta">
                <i class="meta-ico" v-html="icons.pin"></i>
                <span>{{ r.hotelName }}</span>
              </p>

              <div class="card__foot">
                <div class="card__price"><small>¥</small>{{ fmtMoney(r.roomPrice) }}<em>/ 晚</em></div>
                <!-- .stop：卡片整块也绑了 goDetail，不拦会先跳详情再跳下单 -->
                <button
                  class="btn btn-primary btn-sm"
                  type="button"
                  :disabled="soldOut(r)"
                  @click.stop="goOrder(r)"
                >
                  {{ soldOut(r) ? '已订满' : '预订这间' }}
                </button>
              </div>
            </div>
          </article>
        </div>

        <p class="foot-hint">
          价格为每晚单间价，入住当天 14:00 后办理入住、次日 12:00 前退房。点卡片看房型详情与住客点评；
          直接点「预订这间」进下单页，填写入住日期与住几晚。
        </p>
      </template>
    </div>
  </div>
</template>

<script>
import { listRooms } from '@/api/hotel'
import { icons } from '@/common/scenes'
import { imageList, splitTags } from '@/common/media'
import { fmtMoney } from '@/common/format'

export default {
  name: 'HotelInfoRooms',
  data() {
    return {
      icons,
      rooms: [],
      loading: true,
      error: false,
      reqId: 0,
    }
  },
  computed: {
    /**
     * 路由参数里带的是酒店名（列表页点卡片时 encodeURIComponent 过）。
     * vue-router 一般已经解码，但这里再解一次是幂等的（中文里不含 % 转义），
     * 万一拿到的是未解码的原文也能兜住 —— 防御性解码，不用赌框架版本的行为。
     */
    hotelName() {
      const raw = this.$route.params.name
      if (!raw) return ''
      try {
        return decodeURIComponent(String(raw))
      } catch (e) {
        return String(raw)
      }
    },
    heroDesc() {
      return '同一家酒店的几种床位/房型摆在一起比：房型名、设施、剩余房数、价格一目了然，选好直接下单。'
    },
    minPrice() {
      const nums = this.rooms.map((r) => Number(r.roomPrice)).filter((n) => Number.isFinite(n))
      return nums.length ? Math.min(...nums) : null
    },
    totalStock() {
      return this.rooms.reduce((sum, r) => sum + (Number(r.roomCount) || 0), 0)
    },
  },
  watch: {
    // 同一组件换酒店（列表页 → 另一家）时路由参数变了但实例复用，必须重新拉
    hotelName() {
      this.fetchList()
    },
  },
  created() {
    this.fetchList()
  },
  methods: {
    fmtMoney,
    imageOf(room) {
      return imageList(room.roomImage)[0] || room.roomImage
    },
    facilitiesOf(room) {
      return splitTags(room.roomFacility).slice(0, 4)
    },
    soldOut(room) {
      return Number(room.roomCount) <= 0
    },
    isTight(room) {
      const n = Number(room.roomCount) || 0
      return n > 0 && n <= 5
    },
    stockText(room) {
      const n = Number(room.roomCount) || 0
      if (n <= 0) return '已订满'
      return n <= 5 ? `仅剩 ${n} 间` : `${n} 间可订`
    },

    /**
     * 拿这家酒店的客房（room_type 的归属列就是 hotel_name）。
     *
     * 后端对 hotelName 是 LIKE 匹配（「青海湖大酒店」会连「青海湖大酒店式公寓」一起
     * 捞出来），所以这里照 hotelInfoDetail.vue 的 fetchRelated 再精确比对一次 ——
     * 否则这家酒店的页面会混进别人家的客房。
     *
     * 排序不传给后端：同一家店就三五个房型，按价格从低到高排最符合「先挑便宜床位」的直觉。
     */
    async fetchList() {
      const token = ++this.reqId
      const name = this.hotelName

      this.loading = true
      this.error = false
      this.rooms = []
      if (!name) {
        this.loading = false
        return
      }

      try {
        const res = await listRooms({ hotelName: name })
        if (token !== this.reqId) return
        if (res.data.code === 0) {
          const list = res.data.data.list || []
          this.rooms = list
            .filter((r) => r.hotelName === name)
            .sort((a, b) => Number(a.roomPrice) - Number(b.roomPrice))
        } else {
          this.error = true
        }
      } catch (e) {
        if (token !== this.reqId) return
        this.error = true
      } finally {
        if (token === this.reqId) this.loading = false
      }
    },

    goDetail(room) {
      this.$router.push(`/index/hotel_info/detail/${room.id}`)
    },
    goOrder(room) {
      if (this.soldOut(room)) return
      this.$router.push(`/index/hotel_info/order/${room.id}`)
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

.crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 22px 0 18px;
  font-size: 13.5px;
}

.crumb__link {
  border: 0;
  padding: 0;
  background: none;
  color: var(--brand);
  font-family: inherit;
  font-size: inherit;
  cursor: pointer;
}

.crumb__link:hover {
  text-decoration: underline;
}

.crumb__sep,
.crumb__cur {
  color: var(--ink-3);
}

.skel-card {
  aspect-ratio: 16 / 11;
}

.card__meta .is-tight {
  color: var(--price);
  font-weight: 600;
}

.foot-hint {
  margin-top: 26px;
  color: var(--ink-3);
  font-size: 13px;
  line-height: 1.8;
}
</style>
