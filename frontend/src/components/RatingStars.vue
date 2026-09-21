<template>
  <span class="rating" :class="{ 'rating--empty': score === null }">
    <el-rate :model-value="score || 0" disabled allow-half :size="size" />
    <span v-if="score === null" class="rating__text">暂无评分</span>
    <span v-else class="rating__score">{{ score.toFixed(1) }}</span>
    <span v-if="count" class="rating__count">{{ count }} 条点评</span>
  </span>
</template>

<script>
/**
 * 星级显示。
 *
 * 三张主表**都没有评分字段**（attraction / hotel_info / restaurant 都没有 score 列），
 * 评分只能从评论表的 score 自己算。没评论时传 null，这里显示「暂无评分」，
 * 而不是显示 0.0 —— 0 分和没有评分是两回事。
 */
export default {
  name: 'RatingStars',
  props: {
    score: { type: Number, default: null },
    count: { type: Number, default: 0 },
    size: { type: String, default: 'small' },
  },
}
</script>

<style scoped>
.rating {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.rating :deep(.el-rate) {
  height: auto;
}

.rating :deep(.el-rate__icon) {
  margin-right: 1px;
  font-size: 15px;
}

.rating__score {
  color: #e6873b;
  font-size: 15px;
  font-weight: 700;
}

.rating__text,
.rating__count {
  color: var(--ink-3);
  font-size: 13px;
}

.rating--empty :deep(.el-rate__icon) {
  opacity: 0.45;
}
</style>
