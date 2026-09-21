<template>
  <el-dialog
    :model-value="modelValue"
    title="收银台"
    width="440px"
    append-to-body
    :close-on-click-modal="!loading"
    :close-on-press-escape="!loading"
    @update:model-value="close"
    @open="onOpen"
  >
    <div class="pay">
      <p class="pay__label">应付金额</p>
      <p class="pay__amount"><small>¥</small>{{ fmtMoney(amount) }}</p>

      <p class="pay__label">支付方式</p>
      <ul class="pay__methods">
        <li v-for="item in METHODS" :key="item.key">
          <button
            type="button"
            class="pay__method"
            :class="{ 'pay__method--on': method === item.key }"
            :aria-pressed="method === item.key"
            @click="method = item.key"
          >
            <span class="pay__method-mark">{{ item.mark }}</span>
            <span class="pay__method-text">
              <strong>{{ item.label }}</strong>
              <em>{{ item.desc }}</em>
            </span>
            <span class="pay__method-dot"></span>
          </button>
        </li>
      </ul>

      <p class="pay__tip">本项目未接入真实支付渠道，点「确认支付」即把这一单标记为已付。</p>
    </div>

    <template #footer>
      <button type="button" class="btn btn-ghost" :disabled="loading" @click="close">取消</button>
      <button type="button" class="btn btn-primary" :disabled="loading" @click="submit">
        {{ loading ? '支付中…' : '确认支付' }}
      </button>
    </template>
  </el-dialog>
</template>

<script>
import { fmtMoney } from '@/common/format'

/**
 * 收银台弹窗。
 *
 * 个人中心与门票下单页两处都要付款，抽成组件是为了让收银台在两边长得一样 ——
 * 分散在两个页面里很容易改了一处漏一处。
 *
 * 付款不带任何核对动作：金额由调用方给，点「确认支付」就发请求。支付方式是一组
 * 本地状态，只影响界面 —— 后端订单表没有这一列，选了也不会传（本项目没有接支付
 * 渠道，付款就是把这单的 is_pay 翻成「已支付」，见 ConsumptionController.pay）。
 * 之所以还留着支付方式，是因为这是演示里「收银台」该有的样子；下面那行提示把
 * 没接渠道这件事说清楚，免得看的人以为真扣了钱。
 */
export default {
  name: 'PayDialog',
  props: {
    modelValue: { type: Boolean, default: false },
    /** 应付金额。传 null 时不显示数字，列表里的单条记录由调用方决定金额 */
    amount: { type: Number, default: null },
    /** 请求进行中：按钮进 loading 并挡住关闭，避免连点重复提交 */
    loading: { type: Boolean, default: false },
  },
  emits: ['update:modelValue', 'confirm'],
  data() {
    return {
      METHODS: [
        { key: 'alipay', mark: '支', label: '支付宝', desc: '扫码或免密支付' },
        { key: 'wechat', mark: '微', label: '微信支付', desc: '需在手机上确认' },
        { key: 'bank', mark: '卡', label: '银行卡', desc: '储蓄卡 / 信用卡' },
      ],
      method: 'alipay',
    }
  },
  methods: {
    fmtMoney,

    /* 每次打开都退回默认方式：上一次选的留在下一次，容易被当成「这次也是这么付的」 */
    onOpen() {
      this.method = 'alipay'
    },

    close() {
      if (this.loading) return
      this.$emit('update:modelValue', false)
    },

    submit() {
      if (this.loading) return
      this.$emit('confirm')
    },
  },
}
</script>

<style scoped>
.pay__label {
  color: var(--ink-3);
  font-size: 13px;
}

.pay__amount {
  margin: 4px 0 18px;
  color: var(--price);
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
}

.pay__amount small {
  margin-right: 2px;
  font-size: 15px;
  font-weight: 700;
}

.pay__methods {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 8px 0 0;
  padding: 0;
  list-style: none;
}

.pay__method {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: #fff;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease;
}

.pay__method:hover {
  border-color: var(--brand);
}

.pay__method--on {
  border-color: var(--brand);
  background: var(--brand-soft);
}

.pay__method-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--sand-deep);
  color: var(--ink-2);
  font-size: 14px;
  font-weight: 700;
}

/* 选中态的色块：光靠边框深浅，两种方式挨着看不太出来 */
.pay__method--on .pay__method-mark {
  background: var(--brand);
  color: #fff;
}

.pay__method-text {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.pay__method-text strong {
  color: var(--ink);
  font-size: 14px;
  font-weight: 600;
}

.pay__method-text em {
  margin-top: 2px;
  color: var(--ink-3);
  font-size: 12px;
  font-style: normal;
}

.pay__method-dot {
  flex: none;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 1px solid var(--line);
  background: transparent;
}

.pay__method--on .pay__method-dot {
  border-color: var(--brand);
  background: var(--brand);
}

.pay__tip {
  margin-top: 12px;
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.7;
}
</style>
