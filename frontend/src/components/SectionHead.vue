<template>
  <div class="section-head" :class="align === 'left' ? 'section-head--left' : ''">
    <span v-if="kicker" class="section-kicker">{{ kicker }}</span>
    <h2 class="section-title">{{ title }}</h2>
    <p v-if="desc" class="section-desc">{{ desc }}</p>
  </div>
</template>

<script>
/**
 * 区块标题。样式和首页的 section-head 完全一致
 * （那套规则已从 home.vue 提升到 main.css，见「共享版式」一节）。
 */
export default {
  name: 'SectionHead',
  props: {
    kicker: { type: String, default: '' },
    title: { type: String, default: '' },
    desc: { type: String, default: '' },
    /** 'center' | 'left' */
    align: { type: String, default: 'center' },
  },
}
</script>

<style scoped>
.section-head--left {
  max-width: none;
  margin-left: 0;
  margin-right: 0;
  text-align: left;
}
</style>
