<template>
  <!-- 头像：有真图（个人中心选的那张）就贴图，没设或加载失败才退回首字圆底 -->
  <template v-if="variant === 'avatar'">
    <img
      v-if="showingAvatarImage"
      class="safe-avatar__img"
      :src="resolved"
      :alt="alt"
      @error="onError"
    />
    <span v-else class="safe-avatar" :style="avatarStyle">{{ initial }}</span>
  </template>

  <img
    v-else
    class="safe-image"
    :src="current"
    :alt="alt"
    :loading="loading"
    :style="imgStyle"
    @error="onError"
  />
</template>

<script>
import { resolveImage, fallbackScene } from '@/common/media'

/**
 * 全站唯一的图片出口。存在的原因是：**图片必然加载失败**。
 *
 * 真实库里图片路径指向 upload/*.jpg，而网关根本没有 /api/upload/** 这个路由，
 * 文件也不存在。如果直接用 <img :src>，九个页面会变成满屏破图图标。
 *
 * 所以这里做两级兜底：
 *   1. 路径为空或加载失败 → 换成首页那套内联 SVG 风景画（转成 data URI，不走网络）；
 *   2. 同一张图每次落到同一幅画（按 seed 取模），刷新页面不会变。
 * 最终效果：即使全站图片全挂，页面仍是「有图有景」的完整观感。
 */
export default {
  name: 'SafeImage',
  props: {
    src: { type: String, default: '' },
    alt: { type: String, default: '' },
    /** 兜底图的选择依据。不传就退回用 src / alt，保证同一条数据每次同一幅画 */
    seed: { type: [String, Number], default: '' },
    /** 'media' 普通图片 | 'avatar' 首字头像 */
    variant: { type: String, default: 'media' },
    /** CSS aspect-ratio，例如 '16 / 10'。传了图片就能自己把高度撑开 */
    ratio: { type: String, default: '' },
    fit: { type: String, default: 'cover' },
    /** 手动指定兜底图的裁切位置，覆盖自动算出来的那个 */
    position: { type: String, default: '' },
    /** 头像取首字用；不传就用 alt */
    label: { type: String, default: '' },
    lazy: { type: Boolean, default: true },
  },
  data() {
    return { failed: false }
  },
  computed: {
    resolved() {
      return resolveImage(this.src)
    },
    scene() {
      return fallbackScene(this.seed || this.src || this.alt || 'zhiyouxing')
    },
    usingScene() {
      return !this.resolved || this.failed
    },
    current() {
      return this.usingScene ? this.scene.uri : this.resolved
    },
    showingAvatarImage() {
      return Boolean(this.resolved) && !this.failed
    },
    loading() {
      return this.lazy ? 'lazy' : 'eager'
    },
    initial() {
      const s = String(this.label || this.alt || '').trim()
      return s ? s.charAt(0) : '游'
    },
    imgStyle() {
      const style = { objectFit: this.fit }
      if (this.ratio) style.aspectRatio = this.ratio
      // 只有用兜底画的时候才需要挪裁切窗口；真图老老实实居中
      if (this.usingScene) style.objectPosition = this.position || this.scene.position
      return style
    },
    avatarStyle() {
      const palette = {
        first: 'var(--brand-soft)',
        second: 'var(--teal-soft)',
        third: 'var(--sand-deep)',
        fourth: '#fdeee0',
      }
      return { background: palette[this.scene.key] || 'var(--brand-soft)' }
    },
  },
  watch: {
    // 换了数据源要把失败标记清掉，否则翻页后所有图都停在兜底画上
    src() {
      this.failed = false
    },
  },
  methods: {
    onError() {
      this.failed = true
    },
  },
}
</script>

<style scoped>
.safe-image {
  display: block;
  width: 100%;
  background: var(--sand-deep);
}

.safe-avatar__img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.safe-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  color: var(--brand-dark);
  font-size: 0.92em;
  font-weight: 700;
  user-select: none;
}
</style>
