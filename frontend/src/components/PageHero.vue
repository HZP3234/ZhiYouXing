<template>
  <header class="page-hero" :class="{ 'page-hero--compact': compact }">
    <!-- 风景画打底 + 白色渐变压住，保证文字对比度，也延续首页的「高原蓝」观感 -->
    <div class="page-hero__bg" :style="bgStyle" aria-hidden="true"></div>

    <div class="container page-hero__inner">
      <span v-if="kicker" class="section-kicker">{{ kicker }}</span>
      <h1 class="page-hero__title">{{ title }}</h1>
      <p v-if="desc" class="page-hero__desc">{{ desc }}</p>
      <div v-if="$slots.default" class="page-hero__extra"><slot /></div>
    </div>
  </header>
</template>

<script>
import { pickScene } from '@/common/scenes'

/**
 * 列表页 / 下单页的页头。
 * index.vue 给内容区留了 200px 的顶部内边距（让开固定导航），
 * 页头从那里接着往下画，所以这里不再额外加顶部留白。
 */
export default {
  name: 'PageHero',
  props: {
    kicker: { type: String, default: '' },
    title: { type: String, default: '' },
    desc: { type: String, default: '' },
    /** 用哪幅风景画打底。不传就按标题取，保证同类页面每次一致 */
    scene: { type: [String, Number], default: '' },
    compact: { type: Boolean, default: false },
  },
  computed: {
    bgStyle() {
      const scene = pickScene(this.scene || this.title || 'zhiyouxing')
      return {
        backgroundImage: `linear-gradient(180deg, rgba(246, 248, 251, 0.72) 0%, rgba(246, 248, 251, 0.94) 62%, var(--sand) 100%), url("${scene.uri}")`,
        backgroundPosition: `center, ${scene.position}`,
      }
    },
  },
}
</script>

<style scoped>
.page-hero {
  position: relative;
  overflow: hidden;
  padding: 56px 0 40px;
}

.page-hero--compact {
  padding: 34px 0 26px;
}

.page-hero__bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-repeat: no-repeat;
}

.page-hero__inner {
  position: relative;
  z-index: 1;
}

.page-hero__title {
  color: var(--ink);
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -0.6px;
  line-height: 1.22;
}

.page-hero--compact .page-hero__title {
  font-size: 28px;
}

.page-hero__desc {
  max-width: 680px;
  margin-top: 14px;
  color: var(--ink-2);
  font-size: 16px;
  line-height: 1.75;
}

.page-hero__extra {
  margin-top: 24px;
}

@media (max-width: 860px) {
  .page-hero {
    padding: 34px 0 26px;
  }

  .page-hero__title {
    font-size: 28px;
  }

  .page-hero--compact .page-hero__title {
    font-size: 23px;
  }

  .page-hero__desc {
    font-size: 14px;
  }
}
</style>
