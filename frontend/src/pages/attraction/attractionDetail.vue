<template>
  <div class="page">
    <div v-if="loading" class="container detail-loading">
      <div class="skeleton skel-hero"></div>
      <div class="skeleton skel-line"></div>
    </div>

    <ResultState
      v-else-if="error || !item"
      status="error"
      text="景点信息加载失败"
      hint="这条记录不存在，或者接口没有响应。"
      @retry="fetchDetail"
    >
      <button class="btn btn-ghost" @click="$router.push('/index/attraction')">返回景点列表</button>
    </ResultState>

    <div v-else class="container">
      <nav class="crumb">
        <button class="crumb__link" @click="$router.push('/index/attraction')">热门景点</button>
        <span class="crumb__sep">/</span>
        <span class="crumb__cur">{{ item.attractionName }}</span>
      </nav>

      <div class="detail-top">
        <!-- 画廊：主图 + 缩略图。三张图来自 image 字段的逗号串 -->
        <div class="gallery">
          <div class="gallery__main">
            <SafeImage :src="gallery[active]" :seed="`${item.id}-${active}`" ratio="16 / 10" :alt="item.attractionName" />
            <span class="gallery__badge tag tag--overlay">{{ item.attractionType }}</span>
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
              <SafeImage :src="img" :seed="`${item.id}-${i}`" ratio="4 / 3" :alt="`${item.attractionName} 图 ${i + 1}`" />
            </button>
          </div>
        </div>

        <aside class="info-card">
          <h1 class="info-card__title">{{ item.attractionName }}</h1>

          <div class="info-card__tags">
            <span class="tag tag--brand">{{ item.attractionType }}</span>
            <span v-if="isFree" class="tag tag--teal">免费开放</span>
          </div>

          <RatingStars :score="score.avg" :count="score.total" />

          <div class="kv">
            <div class="kv__row">
              <span class="kv__k">开放时间</span>
              <span class="kv__v">{{ item.openingHours || '以景区公告为准' }}</span>
            </div>
            <div class="kv__row">
              <span class="kv__k">所在地</span>
              <span class="kv__v">{{ item.attractionLocation }}</span>
            </div>
            <div class="kv__row">
              <span class="kv__k">剩余票量</span>
              <span class="kv__v">{{ item.quantity }} 张</span>
            </div>
            <div class="kv__row">
              <span class="kv__k">热度</span>
              <span class="kv__v">{{ item.thumbsUpNum }} 点赞 · {{ item.discussNum }} 条点评 · {{ item.storeUpNum }} 收藏</span>
            </div>
          </div>

          <div class="info-card__price">
            <template v-if="isFree">
              <strong class="is-free">免费</strong>
            </template>
            <template v-else>
              <small>¥</small><strong>{{ fmtMoney(item.ticketPrice) }}</strong><em>/ 人</em>
            </template>
          </div>

          <button class="btn btn-primary btn-block" @click="goOrder">立即预订</button>
          <p class="info-card__hint">下单后可在个人中心查看订单，景区现场凭订单号入园。</p>
        </aside>
      </div>

      <section class="block">
        <SectionHead kicker="景点介绍" :title="`关于${item.attractionName}`" align="left" />
        <p v-for="(para, i) in paragraphs" :key="i" class="block__para">{{ para }}</p>
      </section>

      <ReviewList
        ref="reviews"
        class="block"
        comment-prefix="attraction_comment"
        :ref-id="id"
        title="游客点评"
        @loaded="onReviewsLoaded"
      />

      <section v-if="related.length" class="block">
        <SectionHead kicker="猜你也会喜欢" :title="`同类型的其他${item.attractionType}`" align="left" />
        <div class="grid-cards">
          <article
            v-for="r in related"
            :key="r.id"
            class="card card--link"
            @click="$router.push(`/index/attraction/detail/${r.id}`)"
          >
            <div class="card__media">
              <SafeImage :src="r.image" :seed="r.id" ratio="16 / 10" :alt="r.attractionName" />
            </div>
            <div class="card__body">
              <h3 class="card__title">{{ r.attractionName }}</h3>
              <div class="card__foot">
                <div v-if="Number(r.ticketPrice) === 0" class="card__price card__price--free">免费</div>
                <div v-else class="card__price"><small>¥</small>{{ fmtMoney(r.ticketPrice) }}<em>起</em></div>
                <span class="card__heat">{{ r.attractionLocation }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { getAttraction, listAttractions } from '@/api/attraction'
import { imageList } from '@/common/media'
import { fmtMoney } from '@/common/format'

export default {
  name: 'AttractionDetail',
  data() {
    return {
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
    /** image 是逗号串，可能为 null —— 走 imageList 才不会在渲染期 split 抛错 */
    gallery() {
      return imageList(this.item && this.item.image)
    },
    isFree() {
      return this.item ? Number(this.item.ticketPrice) === 0 : false
    },
    paragraphs() {
      const text = (this.item && this.item.attractionDescription) || ''
      return text.split(/\n+/).filter((s) => s.trim())
    },
  },
  watch: {
    // 从「同类推荐」点进来时是同路由不同参数，组件不会重建，必须自己重拉
    id() {
      this.active = 0
      this.fetchDetail()
      this.fetchRelated()
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
      try {
        const res = await getAttraction(this.id)
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
    /** 同类型推荐。attractionType 是后端真实支持的查询参数，切真接口也照跑 */
    async fetchRelated() {
      if (!this.item) return
      try {
        const res = await listAttractions({ page: 1, limit: 4, attractionType: this.item.attractionType })
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
      this.$router.push(`/index/attraction/order/${this.id}`)
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

.info-card__price .is-free {
  color: var(--teal);
  font-size: 30px;
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

.card__price--free {
  color: var(--teal);
  font-size: 18px;
}

.card__heat {
  max-width: 55%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
