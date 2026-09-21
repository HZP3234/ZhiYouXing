<template>
  <div class="state" :class="'state--' + status">
    <span v-if="status === 'loading'" class="state__spin" aria-hidden="true"></span>
    <span v-else class="state__icon" v-html="icon"></span>

    <p class="state__text">{{ text || defaultText }}</p>
    <p v-if="hint" class="state__hint">{{ hint }}</p>

    <button v-if="status === 'error'" type="button" class="btn btn-ghost state__btn" @click="$emit('retry')">
      {{ retryText }}
    </button>
    <slot />
  </div>
</template>

<script>
import { icons } from '@/common/scenes'

/**
 * 加载中 / 空 / 出错 三态的统一展示。
 * 抽出来是为了让九个页面的空态和错误态长得一样 —— 尤其是错误态，
 * 网关没起的时候必须给出「能看懂、能重试」的提示，而不是一片空白。
 */
export default {
  name: 'ResultState',
  props: {
    /** 'loading' | 'empty' | 'error' */
    status: { type: String, default: 'loading' },
    text: { type: String, default: '' },
    hint: { type: String, default: '' },
    retryText: { type: String, default: '重新加载' },
  },
  emits: ['retry'],
  computed: {
    defaultText() {
      return { loading: '正在加载…', empty: '这里还没有内容', error: '加载失败' }[this.status] || ''
    },
    icon() {
      return this.status === 'empty' ? icons.search : icons.comment
    },
  },
}
</script>

<style scoped>
.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 72px 24px;
  text-align: center;
}

.state__icon {
  display: inline-flex;
  width: 44px;
  height: 44px;
  color: var(--ink-3);
}

.state__icon :deep(svg) {
  width: 44px;
  height: 44px;
}

.state__spin {
  width: 34px;
  height: 34px;
  border: 3px solid var(--brand-soft);
  border-top-color: var(--brand);
  border-radius: 50%;
  animation: state-spin 0.8s linear infinite;
}

@keyframes state-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .state__spin {
    animation-duration: 2s;
  }
}

.state__text {
  color: var(--ink-2);
  font-size: 15px;
}

.state__hint {
  max-width: 460px;
  color: var(--ink-3);
  font-size: 13px;
  line-height: 1.7;
}

.state--error .state__text {
  color: var(--ink);
  font-weight: 600;
}

.state__btn {
  height: 40px;
  margin-top: 6px;
  padding: 0 22px;
  font-size: 14px;
}
</style>
