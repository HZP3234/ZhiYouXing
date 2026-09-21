<template>
  <section class="reviews">
    <SectionHead kicker="真实点评" :title="heading" align="left" />

    <ResultState v-if="loading" status="loading" text="正在加载点评…" />

    <ResultState
      v-else-if="error"
      status="error"
      text="点评加载失败"
      hint="接口没通，或后端未启动。"
      @retry="load"
    />

    <ResultState v-else-if="!list.length" status="empty" text="还没有点评" hint="成为第一个分享体验的人。" />

    <template v-else>
      <div class="reviews__summary">
        <div class="reviews__score">
          <strong>{{ avg === null ? '—' : avg.toFixed(1) }}</strong>
          <RatingStars :score="avg" :count="total" />
        </div>

        <ul class="reviews__bars">
          <li v-for="row in distribution" :key="row.star" class="reviews__bar">
            <span class="reviews__bar-label">{{ row.star }} 星</span>
            <span class="reviews__bar-track"><i :style="{ width: row.pct + '%' }"></i></span>
            <span class="reviews__bar-count">{{ row.count }}</span>
          </li>
        </ul>
      </div>

      <ul class="reviews__list">
        <li v-for="item in list" :key="item.id" class="review">
          <div class="review__avatar">
            <SafeImage variant="avatar" :label="item.nickname" :alt="item.nickname" :seed="item.id" />
          </div>

          <div class="review__body">
            <div class="review__head">
              <span class="review__name">{{ item.nickname || '匿名用户' }}</span>
              <RatingStars :score="Number(item.score) || null" />
              <span class="review__time">{{ fmtFromNow(item.addTime) }}</span>
            </div>

            <p class="review__content">{{ item.content }}</p>

            <div v-if="item.reply" class="review__reply">
              <span class="review__reply-tag">商家回复</span>
              <span>{{ item.reply }}</span>
            </div>
          </div>
        </li>
      </ul>
    </template>
  </section>
</template>

<script>
import SectionHead from '@/components/SectionHead.vue'
import ResultState from '@/components/ResultState.vue'
import RatingStars from '@/components/RatingStars.vue'
import SafeImage from '@/components/SafeImage.vue'
import { listComments } from '@/api/comment'
import { avgScore, fmtFromNow } from '@/common/format'

/**
 * 评论列表。三张评论表结构完全一样，只有表名不同，所以三个详情页共用这一个组件，
 * 用 commentPrefix 决定查哪张表。
 *
 * 真实库里这三张表**都是空的**，所以真接口模式下这里会稳定显示「还没有点评」；
 * 演示用的评论全部来自 mock。
 *
 * 评分只能在这里算出来（三张主表都没有评分字段），
 * 详情页头部也要显示评分，所以加载完通过 @loaded 把结果抛上去。
 */
export default {
  name: 'ReviewList',
  components: { SectionHead, ResultState, RatingStars, SafeImage },
  props: {
    commentPrefix: { type: String, required: true },
    refId: { type: [String, Number], required: true },
    title: { type: String, default: '点评' },
  },
  emits: ['loaded'],
  data() {
    return { list: [], total: 0, loading: true, error: false }
  },
  computed: {
    heading() {
      return this.total ? `${this.title}（${this.total}）` : this.title
    },
    avg() {
      return avgScore(this.list.map((c) => c.score))
    },
    /** 5→1 星各多少条。score 是 3.5/4/4.5 这种，四舍五入到整数档 */
    distribution() {
      const buckets = [5, 4, 3, 2, 1].map((star) => ({ star, count: 0 }))
      for (const c of this.list) {
        const s = Math.round(Number(c.score) || 0)
        const bucket = buckets.find((b) => b.star === s) || buckets[buckets.length - 1]
        bucket.count += 1
      }
      const max = Math.max(1, ...buckets.map((b) => b.count))
      return buckets.map((b) => ({ ...b, pct: Math.round((b.count / max) * 100) }))
    },
  },
  watch: {
    refId() {
      this.load()
    },
  },
  created() {
    this.load()
  },
  methods: {
    fmtFromNow,
    async load() {
      this.loading = true
      this.error = false
      try {
        const res = await listComments(this.commentPrefix, this.refId)
        if (res.data.code === 0) {
          this.list = res.data.data.list || []
          this.total = res.data.data.totalCount ?? this.list.length
        } else {
          this.list = []
          this.total = 0
        }
      } catch (e) {
        this.error = true
        this.list = []
        this.total = 0
      } finally {
        this.loading = false
        this.$emit('loaded', { list: this.list, avg: this.avg, total: this.total })
      }
    },
  },
}
</script>

<style scoped>
.reviews__summary {
  display: flex;
  align-items: center;
  gap: 48px;
  margin-bottom: 28px;
  padding: 24px 28px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

.reviews__score {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 132px;
  text-align: center;
}

.reviews__score strong {
  color: #e6873b;
  font-size: 40px;
  font-weight: 800;
  line-height: 1;
}

.reviews__bars {
  flex: 1;
  margin: 0;
  padding: 0;
  list-style: none;
}

.reviews__bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 3px 0;
}

.reviews__bar-label,
.reviews__bar-count {
  color: var(--ink-3);
  font-size: 12px;
  white-space: nowrap;
}

.reviews__bar-count {
  min-width: 22px;
  text-align: right;
}

.reviews__bar-track {
  flex: 1;
  height: 7px;
  border-radius: 999px;
  background: var(--sand-deep);
  overflow: hidden;
}

.reviews__bar-track i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #f0b877, #e6873b);
}

.reviews__list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.review {
  display: flex;
  gap: 16px;
  padding: 22px 0;
  border-top: 1px solid var(--line);
}

.review__avatar {
  flex: 0 0 auto;
  width: 42px;
  height: 42px;
}

.review__body {
  flex: 1;
  min-width: 0;
}

.review__head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.review__name {
  color: var(--ink);
  font-size: 15px;
  font-weight: 600;
}

.review__time {
  margin-left: auto;
  color: var(--ink-3);
  font-size: 12px;
}

.review__content {
  margin-top: 8px;
  color: var(--ink-2);
  font-size: 14px;
  line-height: 1.8;
}

.review__reply {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: var(--brand-soft);
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.7;
}

.review__reply-tag {
  flex: 0 0 auto;
  color: var(--brand);
  font-weight: 600;
}

@media (max-width: 860px) {
  .reviews__summary {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
    padding: 20px;
  }

  .review__time {
    margin-left: 0;
  }
}
</style>
