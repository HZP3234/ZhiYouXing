<template>
  <div class="page">
    <div v-if="loading" class="container detail-loading">
      <div class="skeleton skel-hero"></div>
      <div class="skeleton skel-line"></div>
    </div>

    <ResultState
      v-else-if="error || !item"
      status="error"
      text="房型信息加载失败"
      hint="这条记录不存在，或者接口没有响应。"
      @retry="fetchDetail"
    >
      <button class="btn btn-ghost" @click="$router.push('/index/hotel_info')">返回酒店列表</button>
    </ResultState>

    <div v-else class="container">
      <nav class="crumb">
        <button class="crumb__link" @click="$router.push('/index/hotel_info')">酒店信息</button>
        <span class="crumb__sep">/</span>
        <span class="crumb__cur">{{ item.hotelName }}</span>
      </nav>

      <div class="detail-top">
        <div class="gallery">
          <div class="gallery__main">
            <SafeImage :src="gallery[active]" :seed="`${item.id}-${active}`" ratio="16 / 10" :alt="item.roomName" />
            <span class="gallery__badge tag tag--overlay">{{ item.roomType }}</span>
          </div>
          <div v-if="gallery.length > 1" class="gallery__thumbs">
            <button
              v-for="(img, i) in gallery"
              :key="i"
              class="gallery__thumb"
              :class="{ 'is-active': i === active }"
              type="button"
              @click="active = i"
            >
              <SafeImage :src="img" :seed="`${item.id}-${i}`" ratio="4 / 3" :alt="`${item.roomName} 图 ${i + 1}`" />
            </button>
          </div>
        </div>

        <aside class="info-card">
          <span class="info-card__eyebrow">{{ item.hotelName }}</span>
          <h1 class="info-card__title">{{ item.roomName }}</h1>

          <div class="info-card__tags">
            <span class="tag tag--brand">{{ item.roomType }}</span>
            <span v-for="f in facilities" :key="f" class="tag">{{ f }}</span>
          </div>

          <RatingStars :score="score.avg" :count="score.total" />

          <div class="kv">
            <div class="kv__row">
              <span class="kv__k">所属酒店</span>
              <span class="kv__v">{{ item.hotelName }}</span>
            </div>
            <div v-if="hotel && hotel.hotelAddress" class="kv__row">
              <span class="kv__k">酒店地址</span>
              <span class="kv__v">{{ hotel.hotelAddress }}</span>
            </div>
            <div class="kv__row">
              <span class="kv__k">剩余房量</span>
              <span class="kv__v">{{ item.roomCount }} 间</span>
            </div>
            <!-- 浏览/点评/收藏是**酒店**级的计数（酒店表上的列），客房行上没有 -->
            <div v-if="hotel" class="kv__row">
              <span class="kv__k">酒店热度</span>
              <span class="kv__v">{{ hotel.clickNum }} 浏览 · {{ hotel.discussNum }} 条点评 · {{ hotel.storeUpNum }} 收藏</span>
            </div>
          </div>

          <div class="info-card__price">
            <small>¥</small><strong>{{ fmtMoney(item.roomPrice) }}</strong><em>/ 晚</em>
          </div>

          <button class="btn btn-primary btn-block" :disabled="soldOut" @click="goOrder">
            {{ soldOut ? '该房型已订满' : '立即预订' }}
          </button>
          <p class="info-card__hint">入住当天 14:00 后办理入住，次日 12:00 前退房。提交后商家会与您电话确认。</p>
        </aside>
      </div>

      <section class="block">
        <SectionHead kicker="房型介绍" :title="`关于${item.hotelName}的${item.roomName}`" align="left" />
        <p v-for="(para, i) in paragraphs" :key="i" class="block__para">{{ para }}</p>

        <div v-if="facilities.length" class="fac">
          <h3 class="fac__title">房间设施</h3>
          <ul class="fac__list">
            <li v-for="f in facilities" :key="f" class="fac__item">
              <i class="meta-ico" v-html="icons.check"></i>{{ f }}
            </li>
          </ul>
        </div>
      </section>

      <ReviewList
        class="block"
        comment-prefix="hotel_comment"
        :ref-id="id"
        title="住客点评"
        @loaded="onReviewsLoaded"
      />

      <section v-if="related.length" class="block">
        <SectionHead kicker="同店其他房型" :title="`${item.hotelName} 的其他选择`" align="left" />
        <div class="grid-cards">
          <article
            v-for="r in related"
            :key="r.id"
            class="card card--link"
            @click="$router.push(`/index/hotel_info/detail/${r.id}`)"
          >
            <div class="card__media">
              <SafeImage :src="r.roomImage" :seed="r.id" ratio="16 / 10" :alt="r.roomName" />
              <span class="card__badge tag tag--overlay">{{ r.roomType }}</span>
            </div>
            <div class="card__body">
              <h3 class="card__title">{{ r.roomName }}</h3>
              <p class="card__meta">
                <i class="meta-ico" v-html="icons.pin"></i>
                <span>{{ r.roomCount }} 间可订</span>
              </p>
              <div class="card__foot">
                <div class="card__price"><small>¥</small>{{ fmtMoney(r.roomPrice) }}<em>/ 晚</em></div>
              </div>
            </div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { getRoom, listRooms, getHotelByName } from '@/api/hotel'
import { icons } from '@/common/scenes'
import { imageList, splitTags } from '@/common/media'
import { fmtMoney } from '@/common/format'

export default {
  name: 'HotelInfoDetail',
  data() {
    return {
      icons,
      // 客房（room_type 一行）。页面主体说的是这一间房
      item: null,
      // 它所属的酒店（hotel_info 一行）。地址/热度在酒店表上，客房行里没有
      hotel: null,
      active: 0,
      loading: true,
      error: false,
      score: { avg: null, total: 0 },
      related: [],
    }
  },
  computed: {
    id() {
      return this.$route.params.id
    },
    gallery() {
      return imageList(this.item && this.item.roomImage)
    },
    facilities() {
      return splitTags(this.item && this.item.roomFacility)
    },
    soldOut() {
      return this.item ? Number(this.item.roomCount) <= 0 : false
    },
    paragraphs() {
      const text = (this.item && this.item.roomDescription) || ''
      return text.split(/\n+/).filter((s) => s.trim())
    },
  },
  watch: {
    id() {
      this.active = 0
      this.fetchDetail()
    },
  },
  created() {
    this.fetchDetail()
  },
  methods: {
    fmtMoney,
    async fetchDetail() {
      this.loading = true
      this.error = false
      this.hotel = null
      try {
        const res = await getRoom(this.id)
        if (res.data.code === 0 && res.data.data) {
          this.item = res.data.data
          this.fetchRelated()
          this.fetchHotel()
        } else {
          this.error = true
        }
      } catch (e) {
        this.error = true
      } finally {
        this.loading = false
      }
    },

    /**
     * 同店其他客房（room_type 的归属列是 hotel_name）。
     *
     * 后端对 hotelName 是 LIKE，所以「西宁青海湖大酒店」会把「西宁青海湖大酒店式公寓」
     * 这类也捞进来 —— 必须在前端再做一次精确比对，否则推荐位会混进别的酒店。
     */
    async fetchRelated() {
      if (!this.item) return
      try {
        const res = await listRooms({ hotelName: this.item.hotelName })
        const list = res.data.code === 0 ? res.data.data.list || [] : []
        this.related = list
          .filter((r) => r.hotelName === this.item.hotelName && String(r.id) !== String(this.item.id))
          .slice(0, 3)
      } catch (e) {
        this.related = []
      }
    },

    /** 客房行里没有酒店地址/热度，回头按 hotel_name 取一下那家酒店；取不到就不显示这几行 */
    async fetchHotel() {
      if (!this.item) return
      try {
        this.hotel = await getHotelByName(this.item.hotelName)
      } catch (e) {
        this.hotel = null
      }
    },
    onReviewsLoaded({ avg, total }) {
      this.score = { avg, total }
    },
    goOrder() {
      this.$router.push(`/index/hotel_info/order/${this.id}`)
    },
  },
}
</script>

<style scoped>
.detail-loading {
  padding-top: 30px;
}

.skel-hero {
  height: 420px;
}

.skel-line {
  height: 26px;
  margin-top: 20px;
  border-radius: 8px;
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

.gallery__main {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.gallery__badge {
  position: absolute;
  top: 16px;
  left: 16px;
}

.gallery__thumbs {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.gallery__thumb {
  padding: 0;
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: none;
  cursor: pointer;
  opacity: 0.7;
  transition:
    opacity 0.18s ease,
    border-color 0.18s ease;
}

.gallery__thumb:hover {
  opacity: 1;
}

.gallery__thumb.is-active {
  border-color: var(--brand);
  opacity: 1;
}

.info-card {
  padding: 28px;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

.info-card__eyebrow {
  display: block;
  margin-bottom: 6px;
  color: var(--brand);
  font-size: 14px;
  font-weight: 600;
}

.info-card__title {
  color: var(--ink);
  font-size: 28px;
  font-weight: 800;
  line-height: 1.3;
}

.info-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 14px 0 10px;
}

.info-card .kv {
  margin: 18px 0;
}

.info-card__price {
  display: flex;
  align-items: baseline;
  gap: 2px;
  margin: 18px 0 16px;
  color: var(--price);
}

.info-card__price small {
  font-size: 16px;
  font-weight: 700;
}

.info-card__price strong {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
}

.info-card__price em {
  margin-left: 4px;
  color: var(--ink-3);
  font-size: 13px;
  font-style: normal;
}

.info-card__hint {
  margin-top: 12px;
  color: var(--ink-3);
  font-size: 12.5px;
  line-height: 1.7;
}

.block {
  padding-top: 64px;
}

.block__para {
  color: var(--ink-2);
  font-size: 15.5px;
  line-height: 1.95;
}

.block__para + .block__para {
  margin-top: 16px;
}

.fac {
  margin-top: 26px;
  padding: 22px 24px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--card);
}

.fac__title {
  margin-bottom: 14px;
  color: var(--ink);
  font-size: 16px;
  font-weight: 700;
}

.fac__list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 20px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.fac__item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink-2);
  font-size: 14px;
}

.fac__item .meta-ico {
  display: inline-flex;
  flex: 0 0 auto;
  width: 16px;
  height: 16px;
  color: var(--teal);
}

.fac__item svg {
  width: 16px;
  height: 16px;
}

@media (max-width: 860px) {
  .info-card__title {
    font-size: 22px;
  }

  .gallery__thumbs {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .fac__list {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
