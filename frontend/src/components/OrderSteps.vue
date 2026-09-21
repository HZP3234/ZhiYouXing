<template>
  <ol class="steps">
    <li v-for="(s, i) in steps" :key="s" class="steps__item" :class="i <= current ? 'is-done' : ''">
      <span class="steps__dot">{{ i < current ? '✓' : i + 1 }}</span>
      <span class="steps__label">{{ s }}</span>
    </li>
  </ol>
</template>

<script>
/**
 * 下单页顶部的步骤条。三个模块的流程完全一样，所以抽出来共用。
 */
export default {
  name: 'OrderSteps',
  props: {
    /** 当前进行到第几步（从 0 开始） */
    current: { type: Number, default: 1 },
    steps: { type: Array, default: () => ['选择商品', '填写信息', '提交预约'] },
  },
}
</script>

<style scoped>
.steps {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 26px;
  padding: 0;
  list-style: none;
  flex-wrap: wrap;
}

.steps__item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink-3);
  font-size: 13.5px;
}

.steps__item + .steps__item::before {
  content: '';
  width: 34px;
  height: 1px;
  margin-right: 8px;
  background: var(--line);
}

.steps__dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--sand-deep);
  color: var(--ink-3);
  font-size: 12.5px;
  font-weight: 700;
}

.steps__item.is-done {
  color: var(--brand);
  font-weight: 600;
}

.steps__item.is-done .steps__dot {
  background: linear-gradient(135deg, var(--brand-light), var(--brand));
  color: #fff;
}
</style>
