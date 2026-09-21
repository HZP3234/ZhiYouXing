<template>
  <div class="page">
    <div v-if="loading" class="container detail-loading">
      <div class="skeleton skel-hero"></div>
      <div class="skeleton skel-line"></div>
    </div>

    <ResultState
      v-else-if="error || !item"
      status="error"
      text="餐厅信息加载失败"
      hint="这条记录不存在，或者接口没有响应。"
      @retry="fetchDetail"
    >
      <button class="btn btn-ghost" @click="$router.push('/index/restaurant')">返回餐厅列表</button>
    </ResultState>

    <div v-else class="container">
      <nav class="crumb">
        <button class="crumb__link" @click="$router.push('/index/restaurant')">美食餐厅</button>
        <span class="crumb__sep">/</span>
        <span class="crumb__cur">{{ item.restaurantName }}</span>
      </nav>

      <div class="detail-top">
        <div class="gallery">
          <div class="gallery__main">
            <SafeImage :src="gallery[active]" :seed="`${item.id}-${active}`" ratio="16 / 10" :alt="item.restaurantName" />
            <span v-if="dishes.length" class="gallery__badge tag tag--overlay">招牌 · {{ dishes[0] }}</span>
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
              <SafeImage :src="img" :seed="`${item.id}-${i}`" ratio="4 / 3" :alt="`${item.restaurantName} 图 ${i + 1}`" />
            </button>
          </div>
        </div>

        <aside class="info-card">
          <h1 class="info-card__title">{{ item.restaurantName }}</h1>

          <div class="info-card__tags">
            <span v-for="d in dishes" :key="d" class="tag tag--brand">{{ d }}</span>
          </div>

          <RatingStars :score="score.avg" :count="score.total" />

          <div class="kv">
            <div class="kv__row">
              <span class="kv__k">营业时间</span>
              <span class="kv__v">{{ item.businessHours || '以门店公告为准' }}</span>
            </div>
            <div class="kv__row">
              <span class="kv__k">餐厅地址</span>
              <span class="kv__v">{{ item.restaurantAddress }}</span>
            </div>
            <div class="kv__row">
              <span class="kv__k">联系电话</span>
              <span class="kv__v">{{ item.contactPhone || '—' }}</span>
            </div>
            <div class="kv__row">
              <span class="kv__k">人气</span>
              <span class="kv__v">{{ item.thumbsUpNum }} 点赞 · {{ item.discussNum }} 条点评 · {{ item.clickNum }} 浏览</span>
            </div>
          </div>

          <!-- 餐厅没有金额字段可算（avgCost 是文案），所以这里给「人均」而不是总价 -->
          <div class="info-card__cost">
            <template v-if="cost.amount !== null">
              <small>人均</small><strong>¥{{ cost.amount }}</strong>
            </template>
            <template v-else>
              <small>人均</small><strong class="is-plain">{{ cost.text }}</strong>
            </template>
          </div>

          <button class="btn btn-primary btn-block" @click="goOrder">预约就餐</button>
          <p class="info-card__hint">
            预约提交后需商家审核，到店按实际点餐结算。
            <a class="info-card__link" @click="goMyReservations">查看我的预约 ›</a>
          </p>
        </aside>
      </div>

      <section class="block">
        <SectionHead kicker="餐厅介绍" :title="`关于${item.restaurantName}`" align="left" />
        <p v-for="(para, i) in paragraphs" :key="i" class="block__para">{{ para }}</p>

        <div class="info-strip">
          <div class="info-strip__item">
            <i class="meta-ico" v-html="icons.clock"></i>
            <span class="info-strip__label">营业时间</span>
            <span class="info-strip__value">{{ item.businessHours || '—' }}</span>
          </div>
          <div class="info-strip__item">
            <i class="meta-ico" v-html="icons.phone"></i>
            <span class="info-strip__label">订座电话</span>
            <span class="info-strip__value">{{ item.contactPhone || '—' }}</span>
          </div>
          <div class="info-strip__item">
            <i class="meta-ico" v-html="icons.pin"></i>
            <span class="info-strip__label">地址</span>
            <span class="info-strip__value">{{ item.restaurantAddress }}</span>
          </div>
        </div>
      </section>

      <ReviewList
        class="block"
        comment-prefix="restaurant_comment"
        :ref-id="id"
        title="食客点评"
        @loaded="onReviewsLoaded"
      />

      <section v-if="related.length" class="block">
        <SectionHead kicker="附近还值得吃" title="其他食客也在看" align="left" />
        <div class="grid-cards">
          <article
            v-for="r in related"
            :key="r.id"
            class="card card--link"
            @click="$router.push(`/index/restaurant/detail/${r.id}`)"
          >
            <div class="card__media">
              <SafeImage :src="r.restaurantImage" :seed="r.id" ratio="16 / 10" :alt="r.restaurantName" />
            </div>
            <div class="card__body">
              <h3 class="card__title">{{ r.restaurantName }}</h3>
              <p class="card__meta">
                <i class="meta-ico" v-html="icons.pin"></i>
                <span>{{ r.restaurantAddress }}</span>
              </p>
              <div class="card__foot">
                <div class="card__price" :class="{ 'card__price--plain': costOf(r).amount === null }">
                  <template v-if="costOf(r).amount !== null"><small>人均 ¥</small>{{ costOf(r).amount }}</template>
                  <template v-else>{{ costOf(r).text }}</template>
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { getRestaurant, listRestaurants } from '@/api/restaurant'
import { icons } from '@/common/scenes'
import { imageList, splitTags } from '@/common/media'
import { parseAmount } from '@/common/format'

export default {
  name: 'RestaurantDetail',
  data() {
    return {
      icons,
      item: null,
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
      return imageList(this.item && this.item.restaurantImage)
    },
    dishes() {
      return splitTags(this.item && this.item.signatureDish)
    },
    cost() {
      return this.costOf(this.item || {})
    },
    paragraphs() {
      const text = (this.item && this.item.restaurantIntro) || ''
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
    /** 能抠出数字才当金额，否则原样显示文案（真库的「人均消费1」不能变成 ¥1） */
    costOf(item) {
      const amount = parseAmount(item.avgCost)
      if (amount !== null) return { amount, text: '' }
      return { amount: null, text: item.avgCost ? String(item.avgCost) : '以到店为准' }
    },
    async fetchDetail() {
      this.loading = true
      this.error = false
      try {
        const res = await getRestaurant(this.id)
        if (res.data.code === 0 && res.data.data) {
          this.item = res.data.data
          this.fetchRelated()
        } else {
          this.error = true
        }
      } catch (e) {
        this.error = true
      } finally {
        this.loading = false
      }
    },
    /** 餐厅主表没有分类字段，做不了「同类推荐」，退而取当前热门里的其他几家 */
    async fetchRelated() {
      if (!this.item) return
      try {
        const res = await listRestaurants({ page: 1, limit: 5, sort: 'clickNum', order: 'desc' })
        const list = res.data.code === 0 ? res.data.data.list || [] : []
        this.related = list.filter((r) => String(r.id) !== String(this.item.id)).slice(0, 3)
      } catch (e) {
        this.related = []
      }
    },
    onReviewsLoaded({ avg, total }) {
      this.score = { avg, total }
    },
    goOrder() {
      this.$router.push(`/index/restaurant/order/${this.id}`)
    },
    /** 带 tab 参数直达个人中心的「预约」页 */
    goMyReservations() {
      this.$router.push({ path: '/index/center', query: { tab: 'reservation' } })
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

.info-card__cost {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin: 18px 0 16px;
  color: var(--price);
}

.info-card__cost small {
  color: var(--ink-3);
  font-size: 13px;
}

.info-card__cost strong {
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
}

.info-card__cost .is-plain {
  color: var(--ink-3);
  font-size: 16px;
  font-weight: 600;
}

.info-card__hint {
  margin-top: 12px;
  color: var(--ink-3);
  font-size: 12.5px;
  line-height: 1.7;
}

.info-card__link {
  color: var(--brand);
  cursor: pointer;
  white-space: nowrap;
}

.info-card__link:hover {
  text-decoration: underline;
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

.info-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  margin-top: 26px;
  padding: 22px 24px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--card);
}

.info-strip__item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 14px;
}

.info-strip__item .meta-ico {
  display: inline-flex;
  flex: 0 0 auto;
  width: 17px;
  height: 17px;
  color: var(--brand);
}

.info-strip__item svg {
  width: 17px;
  height: 17px;
}

.info-strip__label {
  color: var(--ink-3);
  font-size: 13px;
}

.info-strip__value {
  flex: 1;
  min-width: 0;
  color: var(--ink);
  word-break: break-word;
}

.card__price--plain {
  color: var(--ink-3);
  font-size: 14px;
  font-weight: 600;
}

@media (max-width: 1080px) {
  .info-strip {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 860px) {
  .info-card__title {
    font-size: 22px;
  }

  .gallery__thumbs {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
