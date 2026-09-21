<template>
  <div v-if="modelValue" class="signup-mask" @click.self="close">
    <div class="signup">
      <header class="signup__head">
        <div class="signup__head-text">
          <h3 class="signup__title">{{ receipt ? (paid ? '支付成功' : '报名成功') : '报名参团' }}</h3>
          <p class="signup__route">{{ route ? route.routeName : '' }}</p>
        </div>
        <button class="signup__close" type="button" aria-label="关闭" @click="close">×</button>
      </header>

      <div class="signup__body">
        <!-- ============ 提交后的回执 ============ -->
        <div v-if="receipt" class="receipt">
          <span
            class="receipt__icon"
            :class="paid ? 'receipt__icon--paid' : 'receipt__icon--unpaid'"
            v-html="paid ? icons.check : icons.clock"
          ></span>
          <p class="receipt__title">{{ paid ? '已支付，报名完成' : '报名已提交，等待支付' }}</p>
          <p class="receipt__hint">
            {{ paid
              ? '这条报团记录已进个人中心的「已支付」，出发前可在那里评价。'
              : '未支付的报团单同样占着名额，请在 10 分钟内到个人中心完成付款，超时会自动取消。' }}
          </p>

          <div class="kv">
            <div class="kv__row"><span class="kv__k">线路</span><span class="kv__v">{{ route.routeName }}</span></div>
            <div class="kv__row"><span class="kv__k">出发日期</span><span class="kv__v">{{ fmtDate(route.departureDate) }}</span></div>
            <div class="kv__row"><span class="kv__k">报名人数</span><span class="kv__v">{{ form.signupCount }} 人</span></div>
            <div class="kv__row">
              <span class="kv__k">报名人（实名本人）</span>
              <span class="kv__v">{{ identity.realName }} · {{ identity.idCardMasked }}</span>
            </div>
            <div class="kv__row"><span class="kv__k">联系电话</span><span class="kv__v">{{ form.contactPhone }}</span></div>
            <div class="kv__row">
              <span class="kv__k">报团金额</span>
              <span class="kv__v receipt__money">¥{{ fmtMoney(payAmount) }}</span>
            </div>
          </div>
        </div>

        <!-- ============ 表单 ============ -->
        <template v-else>
          <div class="signup__section">
            <h4 class="signup__section-title">实名认证</h4>
            <span class="tag" :class="verified ? 'tag--teal' : 'tag--price'">
              {{ identityTag }}
            </span>
          </div>

          <p v-if="identityLoading" class="signup__tip">正在读取认证状态…</p>
          <p v-else-if="verified" class="signup__done">
            已认证：{{ identity.realName }} · {{ identity.idCardMasked }}
          </p>
          <!-- 提交了但还没人审：不再给表单，否则用户会以为要重填一遍（重填也只是重交同一份材料） -->
          <p v-else-if="pending" class="signup__tip signup__tip--warn">
            实名认证已提交，正在等待管理员审核，通过后即可报名。
            审核结果会显示在个人中心，期间无需重复提交。
          </p>
          <!-- 被驳回：给出管理员的原话，并放行表单让他改了重交 -->
          <p v-else-if="rejected" class="signup__tip signup__tip--warn">
            上次实名认证未通过：{{ identity.auditReply || '请核对姓名与身份证号' }}。
            请修正下面的信息后重新提交。
          </p>
          <p v-else class="signup__tip signup__tip--warn">
            报团按实名信息登记，报名前必须完成实名认证。姓名与证件号将随本次报名一并提交，
            提交后不能自行修改（如需变更请联系客服），请核对后再填。
          </p>

          <!--
            实名认证与报名信息在同一个 el-form 里：提交前只需要一次 validate()，
            分成两个 form 要判两次、还得合并两份结果（与门票下单页同一套做法）。
          -->
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
            <div v-if="!identityLoading && !verified && !pending" class="signup__grid">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="与身份证一致" maxlength="30" clearable />
              </el-form-item>

              <el-form-item label="身份证号" prop="idCard">
                <!--
                  用 idCardGuard 在输入期就剔掉非数字与多余的 X（末尾 X 保留、小写转大写），
                  所以不设 maxlength：粘贴来的证件号常带空格，浏览器先按 maxlength 截断
                  会把末位校验码吃掉且看不出来。@blur 那道归一化留着兜底。
                -->
                <el-input
                  v-model="form.idCard"
                  v-bind="idCardGuard"
                  placeholder="18 位身份证号"
                  clearable
                  @blur="form.idCard = normalizeIdCard(form.idCard)"
                />
              </el-form-item>
            </div>

            <h4 class="signup__section-title signup__section-title--sub">报名信息</h4>

            <div class="signup__grid">
              <el-form-item label="出发日期">
                <el-input :model-value="fmtDate(route.departureDate)" disabled />
                <p class="signup__hint">出发日期由线路定，不能自行修改。</p>
              </el-form-item>

              <el-form-item label="报名人数" prop="signupCount">
                <el-input-number v-model="form.signupCount" :min="1" :max="MAX_SIGNUP" style="width: 100%" />
                <p class="signup__hint">
                  这条线路共 {{ route.groupQuota != null ? route.groupQuota + ' 个名额' : '名额待定' }}，
                  单次最多报 {{ MAX_SIGNUP }} 人。
                </p>
              </el-form-item>

              <!-- 报名人不是可填字段：报团记录上的人就是认证的那个人（后端按 user_identity 写入） -->
              <el-form-item label="报名人">
                <el-input :model-value="verified ? identity.realName : '完成实名认证后自动带出'" disabled />
                <p class="signup__hint">报团按实名信息登记，不支持改成他人。</p>
              </el-form-item>

              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" v-bind="phoneGuard" placeholder="11 位手机号" clearable />
                <p class="signup__hint">已从个人中心带出（没填过请到个人中心补上），导游联系你时用这个号码。</p>
              </el-form-item>
            </div>
          </el-form>

          <div class="signup__sum">
            <span>报团金额</span>
            <strong><small>¥</small>{{ fmtMoney(amount) }}</strong>
          </div>
          <p class="signup__sum-detail">
            ¥{{ fmtMoney(route.routeFee) }} / 人 × {{ form.signupCount }} 人。名额在提交时由服务端复核，
            不够会被退回来并提示还剩几个。
          </p>
        </template>
      </div>

      <footer class="signup__foot">
        <template v-if="receipt">
          <button v-if="!paid" class="btn btn-primary" @click="payOpen = true">
            立即支付 ¥{{ fmtMoney(payAmount) }}
          </button>
          <button v-else class="btn btn-primary" @click="goCenter">去个人中心查看</button>
          <button class="btn btn-ghost" @click="close">关闭</button>
        </template>
        <template v-else>
          <button class="btn btn-ghost" @click="close">再看看</button>
          <button class="btn btn-primary" :disabled="submitting || pending" @click="submit">
            {{ submitting ? '提交中…' : submitLabel }}
          </button>
        </template>
      </footer>
    </div>

    <!-- 收银台：与门票下单页、个人中心那两处共用一个组件 -->
    <PayDialog v-model="payOpen" :amount="payAmount" :loading="paying" @confirm="pay" />
  </div>
</template>

<script>
import PayDialog from '@/components/PayDialog.vue'
import { saveGroupTour } from '@/api/group_tour'
import { getIdentity, saveIdentity } from '@/api/identity'
import { payConsumption } from '@/api/consumption'
import { icons } from '@/common/scenes'
import { fmtDate, fmtMoney, roundMoney } from '@/common/format'
import { idCardGuard, phoneGuard } from '@/common/inputFilter'
import { isIdCard, isMobile, isRealName, normalizeIdCard } from '@/common/validate'
import { currentUser } from '@/common/user'
import { ElMessage } from 'element-plus'

/** 单次报名人数上限，与后端 GroupTourController.MAX_SIGNUP 同值（那边是权威，这里只是提前拦住） */
const MAX_SIGNUP = 20

/**
 * 报名参团弹窗（线路页的「立即报名」）。
 *
 * 为什么做成弹窗而不是像门票/酒店那样单开一个下单页：线路列表的数据是整条回来的
 * （含每日行程），另开一页要么重新拉一次、要么把整条塞进路由参数；而报团要填的东西
 * 只有「几个人、留什么电话」，一屏就够 —— 弹窗省掉一次往返，也不会让用户离开列表。
 *
 * 流程与那两个页面一致：实名认证是硬前置（未认证的账号先用本弹窗里的姓名/证件号
 * 去认证，认证不通过就不会产生订单），下单成功后接着弹收银台付款。判断标准全在
 * 服务端（GroupTourController.createOrder 自己查线路、算金额、核名额），
 * 前端这里算出来的金额只是给用户看的预览。
 */
export default {
  name: 'GroupTourDialog',
  components: { PayDialog },
  props: {
    modelValue: { type: Boolean, default: false },
    /** 要报名的线路。必须是列表里那条（含 routeFee / groupQuota / departureDate） */
    route: { type: Object, default: null },
  },
  emits: ['update:modelValue'],
  data() {
    const user = currentUser()
    return {
      icons,
      fmtDate,
      fmtMoney,
      normalizeIdCard,
      // 守卫对象要在这里露一次面才进得了模板（import 绑定在模板里看不见）。
      // 不能放 methods：Vue 会对 methods 上的每项做 bind，普通对象没有 bind 会直接抛。
      phoneGuard,
      idCardGuard,

      /* 实名认证状态。identityLoading 期间不渲染那半个表单，免得未认证的用户填一半就提交 */
      identityLoading: true,
      identity: { verified: false, realName: '', idCardMasked: '', auditStatus: '', auditReply: '' },

      submitting: false,
      /* 回执。orderId 是下单接口回传的落库主键，付款要拿它去 /user/consumption/pay */
      receipt: false,
      orderId: null,
      /* 服务端算出来的金额。回执与收银台都用它，避免前端预览值与入库值不一致时对不上账 */
      orderAmount: null,
      paying: false,
      paid: false,
      payOpen: false,

      form: {
        realName: '',
        idCard: '',
        signupCount: 1,
        contactPhone: user.phone,
      },

      rules: {
        signupCount: [{ required: true, message: '请选择报名人数', trigger: 'change' }],
        // 实名认证的两条规则与后端 IdCardUtils 同源（见 common/validate.js 的注释）
        realName: [{ validator: isRealName, trigger: 'blur' }],
        idCard: [{ validator: isIdCard, trigger: 'blur' }],
        contactPhone: [
          { required: true, message: '请填写联系手机号', trigger: 'blur' },
          { validator: isMobile, trigger: 'blur' },
        ],
      },
    }
  },
  computed: {
    MAX_SIGNUP() {
      return MAX_SIGNUP
    },
    verified() {
      return this.identity.verified === true
    },
    /*
     * 认证状态有三档：没提交过（auditStatus 空串）、待审核、已驳回。
     * verified 只认「已通过」，所以报名弹窗要自己把另外两种分开 ——
     * 待审核时给表单等于让人重交一遍，已驳回时不给表单人又没法改。
     */
    pending() {
      return this.identity.auditStatus === '待审核'
    },
    rejected() {
      return this.identity.auditStatus === '已驳回'
    },
    identityTag() {
      if (this.verified) return '已认证'
      if (this.pending) return '审核中'
      if (this.rejected) return '已驳回'
      return '未认证'
    },
    submitLabel() {
      if (this.pending) return '实名认证审核中'
      return this.verified ? '提交报名' : '实名认证并报名'
    },
    /** 预览金额。用 roundMoney 与后端 amountOf 的两位小数口径对齐，免得预览 ¥3840.0000001 */
    amount() {
      if (!this.route) return 0
      return roundMoney(Number(this.route.routeFee || 0) * Number(this.form.signupCount || 0))
    },
    /** 收银台与回执上的金额：下单后以服务端回的为准 */
    payAmount() {
      return this.orderAmount == null ? this.amount : this.orderAmount
    },
  },
  watch: {
    /*
     * 每次打开都重置：上一次填到一半的人、上一个已提交的回执都不该留到下一次。
     * 认证状态每次重拉 —— 用户可能刚在个人中心认证完就回来报名。
     */
    modelValue(open) {
      if (open) this.reset()
    },
  },
  created() {
    // 组件是常驻的（靠 v-if 控制遮罩），若一挂载就是打开状态，watch 不会触发
    if (this.modelValue) this.reset()
  },
  methods: {
    reset() {
      const user = currentUser()
      this.identityLoading = true
      this.identity = { verified: false, realName: '', idCardMasked: '', auditStatus: '', auditReply: '' }
      this.submitting = false
      this.receipt = false
      this.orderId = null
      this.orderAmount = null
      this.paying = false
      this.paid = false
      this.payOpen = false
      this.form = { realName: '', idCard: '', signupCount: 1, contactPhone: user.phone }
      this.fetchIdentity()
    },

    close() {
      // 付款请求在路上时不让关：关掉了也拿不到结果，用户只会以为没付成功
      if (this.paying) return
      this.$emit('update:modelValue', false)
    },

    goCenter() {
      this.close()
      this.$router.push('/index/center')
    },

    /**
     * 拉认证状态。接口挂了按「未认证」处理而不是报错：
     * 用户照样能填认证表单，真正的判定在提交时（由服务端说了算）。
     */
    async fetchIdentity() {
      try {
        const res = await getIdentity()
        const data = res.data.code === 0 ? res.data.data : null
        this.identity = {
          verified: Boolean(data && data.verified),
          realName: (data && data.realName) || '',
          idCardMasked: (data && data.idCardMasked) || '',
          auditStatus: (data && data.auditStatus) || '',
          auditReply: (data && data.auditReply) || '',
        }
      } catch (e) {
        this.identity = { verified: false, realName: '', idCardMasked: '', auditStatus: '', auditReply: '' }
      } finally {
        this.identityLoading = false
      }
    },

    async submit() {
      if (!this.route) return
      // 按钮已经置灰，这里再挡一次：审核中的单进不去，也不该白跑一趟后端
      if (this.pending) {
        ElMessage.warning('实名认证正在审核中，通过后即可报名')
        return
      }

      try {
        await this.$refs.formRef.validate()
      } catch (e) {
        ElMessage.warning('请先补全必填信息（未认证的账号需要填真实姓名与身份证号）')
        return
      }

      this.submitting = true
      try {
        // 1) 未认证先认证。认证不通过就停在这里，不会留下半条报名记录
        if (!this.verified && !(await this.certify())) return

        /*
         * 2) 下单。只发「报几个人、留什么电话」——
         * routeId 让服务端自己查线路（费用、导游、出发日期），userAccount / userName /
         * 金额一律不发：那些后端会覆盖（见 GroupTourController.createOrder），
         * 写了反而让人以为这些值说了算。
         */
        const res = await saveGroupTour({
          routeId: this.route.id,
          signupCount: this.form.signupCount,
          contactPhone: this.form.contactPhone,
        })
        if (res.data.code !== 0) {
          // 名额不够 / 线路不存在 / 人数不合法：后端给的 msg 已经能直接读，原样显示
          ElMessage.error(res.data.msg || '报名失败，请稍后重试')
          return
        }

        const saved = res.data.data || {}
        this.orderId = saved.id || null
        this.orderAmount = saved.amount == null ? null : Number(saved.amount)
        this.receipt = true
      } catch (e) {
        // 传输层错误已由 http 拦截器提示过，这里不再重复弹
      } finally {
        this.submitting = false
      }
    },

    /** 提交实名认证。返回是否已通过（通过后 identity 里是脱敏信息，前端不再持有完整证件号） */
    async certify() {
      try {
        const res = await saveIdentity({
          realName: String(this.form.realName || '').trim(),
          idCard: normalizeIdCard(this.form.idCard),
        })
        if (res.data.code !== 0) {
          ElMessage.error(res.data.msg || '实名认证失败，请核对姓名与身份证号')
          return false
        }
        const data = res.data.data || {}
        this.identity = {
          verified: Boolean(data.verified),
          realName: data.realName || '',
          idCardMasked: data.idCardMasked || '',
          auditStatus: data.auditStatus || '',
          auditReply: data.auditReply || '',
        }
        if (this.identity.verified) {
          ElMessage.success('实名认证已完成')
          return true
        }
        /*
         * 提交成功但没通过 = 进了待审核队列（服务端要人工审）。
         * 这不是错误，别再报「认证未通过」——那会让用户以为是自己填错了。
         */
        if (this.pending) {
          ElMessage.success('实名认证已提交，管理员审核通过后即可报名')
          return false
        }
        ElMessage.error('实名认证未通过，请检查姓名与身份证号')
        return false
      } catch (e) {
        return false
      }
    },

    /**
     * 支付。请求体只有「哪张单」：归属由服务端按登录态核（payGroupTour 那条 UPDATE
     * 带 user_account 条件），别人的单改不动。
     */
    async pay() {
      if (!this.orderId) {
        this.payOpen = false
        ElMessage.warning('没有拿到订单号，请到个人中心的「待支付」里付款')
        return
      }
      this.paying = true
      try {
        const res = await payConsumption({ source: 'group', id: this.orderId })
        if (res.data.code !== 0) {
          ElMessage.error(res.data.msg || '支付失败，请稍后重试')
          return
        }
        this.paid = true
        this.payOpen = false
        ElMessage.success('支付成功，可在个人中心的「已支付」里查看')
      } catch (e) {
        // 传输层错误已由拦截器提示
      } finally {
        this.paying = false
      }
    },
  },
}
</script>

<style scoped>
/*
 * 遮罩用 1200：要压过首页顶栏行内写死的 1002 / 1005，以及线路页详情抽屉的 100。
 * 详见 travel_route.vue 里 .chat-mask 那段注释（同一批层级）。
 */
.signup-mask {
  position: fixed;
  inset: 0;
  z-index: 1200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(13, 44, 77, 0.45);
}

.signup {
  display: flex;
  flex-direction: column;
  width: min(620px, 100%);
  max-height: min(760px, 90vh);
  border-radius: var(--radius-lg);
  background: var(--card);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  animation: signupIn 0.24s cubic-bezier(0.22, 0.8, 0.3, 1);
}

@keyframes signupIn {
  from { transform: translateY(16px) scale(0.98); opacity: 0; }
  to { transform: none; opacity: 1; }
}

.signup__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex: none;
  padding: 20px 24px;
  border-bottom: 1px solid var(--line);
}

.signup__head-text {
  min-width: 0;
}

.signup__title {
  margin: 0;
  color: var(--ink);
  font-size: 19px;
  font-weight: 700;
}

.signup__route {
  margin: 4px 0 0;
  color: var(--ink-3);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.signup__close {
  flex: none;
  border: 0;
  background: transparent;
  color: var(--ink-3);
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
}

.signup__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 22px 24px;
}

.signup__section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.signup__section-title {
  margin: 0;
  color: var(--ink);
  font-size: 15px;
  font-weight: 700;
}

/* 实名认证与报名信息在同一张表单里，靠这条虚线把两段分开 */
.signup__section-title--sub {
  margin: 22px 0 12px;
  padding-top: 20px;
  border-top: 1px dashed var(--line);
}

.signup__tip {
  margin: 0 0 14px;
  color: var(--ink-3);
  font-size: 13px;
  line-height: 1.8;
}

.signup__tip--warn {
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: var(--brand-soft);
  color: var(--price);
}

.signup__done {
  margin: 0 0 14px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: var(--teal-soft);
  color: var(--teal);
  font-size: 13.5px;
  line-height: 1.7;
}

.signup__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 22px;
}

.signup__hint {
  margin-top: 2px;
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.6;
}

.signup__body :deep(.el-form-item) {
  margin-bottom: 16px;
}

.signup__sum {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
  color: var(--ink-2);
  font-size: 14px;
}

.signup__sum strong {
  color: var(--price);
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.signup__sum strong small {
  margin-right: 2px;
  font-size: 15px;
  font-weight: 700;
}

.signup__sum-detail {
  margin-top: 8px;
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.7;
}

.signup__foot {
  display: flex;
  flex: none;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--line);
}

.signup__foot .btn {
  flex: 1;
}

/* 通用按钮（与 travel_route.vue / Home.vue 保持一致，本组件独立所以再写一份） */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 44px;
  padding: 0 24px;
  border: 0;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, var(--brand-light), var(--brand));
  box-shadow: 0 8px 20px rgba(22, 103, 196, 0.28);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(22, 103, 196, 0.36);
}

.btn-ghost {
  color: var(--ink);
  background: #fff;
  box-shadow: inset 0 0 0 1.5px var(--line);
}

.btn-ghost:hover:not(:disabled) {
  color: var(--brand);
  box-shadow: inset 0 0 0 1.5px var(--brand-light);
  transform: translateY(-2px);
}

/* ---------------- 回执 ---------------- */
.receipt {
  text-align: center;
}

.receipt__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border-radius: 50%;
}

.receipt__icon :deep(svg) {
  width: 28px;
  height: 28px;
}

/* 未支付用暖色、已支付用青色：这两个态在回执里就是「还要不要动手」的分界线 */
.receipt__icon--unpaid {
  background: var(--brand-soft);
  color: var(--price);
}

.receipt__icon--paid {
  background: var(--teal-soft);
  color: var(--teal);
}

.receipt__title {
  margin: 16px 0 0;
  color: var(--ink);
  font-size: 20px;
  font-weight: 800;
}

.receipt__hint {
  margin: 8px 0 0;
  color: var(--ink-3);
  font-size: 12.5px;
  line-height: 1.7;
}

/* .kv__row / .kv__k / .kv__v 在 main.css 里是全局的，但 .kv 这个容器类没有 ——
   （用户端只有 travelGuideWrite.vue 自己定义过一份），所以这里要自己撑起来 */
.receipt .kv {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 20px 0 0;
  text-align: left;
}

.receipt__money {
  color: var(--price);
  font-size: 16px;
  font-weight: 700;
}

@media (max-width: 720px) {
  .signup__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
