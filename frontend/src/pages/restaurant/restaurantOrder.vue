<template>
  <div class="page">
    <PageHero
      compact
      kicker="预约就餐"
      :title="submitted ? '预约已提交' : `预约${item ? item.restaurantName : ''}`"
      :desc="
        submitted
          ? '商家审核后会与您电话确认，请留意来电。'
          : '选择用餐人数和到店时间，有忌口或包间需求可以写在备注里。'
      "
      scene="third"
    />

    <div class="container">
      <div v-if="submitted" class="done card">
        <span class="done__icon" v-html="icons.check"></span>
        <h2 class="done__title">预约已提交，等待商家审核</h2>
        <p class="done__no">预约单号 <strong>{{ reservationNo }}</strong></p>

        <div class="done__kv kv">
          <div class="kv__row"><span class="kv__k">餐厅</span><span class="kv__v">{{ item.restaurantName }}</span></div>
          <div class="kv__row"><span class="kv__k">用餐人数</span><span class="kv__v">{{ form.dinerCount }} 人</span></div>
          <div class="kv__row"><span class="kv__k">到店时间</span><span class="kv__v">{{ fmtDateTime(form.reservationTime) }}</span></div>
          <div class="kv__row" v-if="form.diningRemark"><span class="kv__k">用餐备注</span><span class="kv__v">{{ form.diningRemark }}</span></div>
          <div class="kv__row"><span class="kv__k">联系人</span><span class="kv__v">{{ form.userName }} · {{ form.contactPhone }}</span></div>
          <div class="kv__row"><span class="kv__k">审核状态</span><span class="kv__v"><span class="tag tag--price">待审核</span></span></div>
        </div>

        <div class="done__actions">
          <!--
            预约记录落在个人中心的「最近消费记录」里（餐厅预约那一类），
            给一个入口直达 —— 否则用户提交完就找不到自己约的是哪一单了。
          -->
          <button class="btn btn-primary" @click="$router.push({ path: '/index/center', query: { tab: 'reservation' } })">
            查看我的预约
          </button>
          <button class="btn btn-ghost" @click="$router.push('/index/restaurant')">继续找吃的</button>
          <button class="btn btn-ghost" @click="$router.push('/index/home')">回到首页</button>
        </div>
      </div>

      <template v-else>
        <OrderSteps :current="1" :steps="['选择餐厅', '填写信息', '提交预约']" />

        <div v-if="loading" class="skeleton skel-block"></div>

        <ResultState
          v-else-if="!item"
          status="error"
          text="餐厅信息加载失败"
          hint="这条记录不存在，或者接口没有响应。"
          @retry="fetchDetail"
        >
          <button class="btn btn-ghost" @click="$router.push('/index/restaurant')">返回餐厅列表</button>
        </ResultState>

        <div v-else class="detail-main">
          <div class="order-col">
            <div class="card goods">
              <div class="goods__media">
                <SafeImage :src="item.restaurantImage" :seed="item.id" ratio="4 / 3" :alt="item.restaurantName" />
              </div>
              <div class="goods__body">
                <h3 class="goods__title">{{ item.restaurantName }}</h3>
                <div class="goods__tags">
                  <span v-for="d in dishes.slice(0, 4)" :key="d" class="tag tag--brand">{{ d }}</span>
                </div>
                <p class="goods__meta">
                  <i class="meta-ico" v-html="icons.clock"></i>
                  <span>{{ item.businessHours }}</span>
                </p>
                <p class="goods__meta">
                  <i class="meta-ico" v-html="icons.pin"></i>
                  <span>{{ item.restaurantAddress }}</span>
                </p>
                <p class="goods__meta">
                  <i class="meta-ico" v-html="icons.phone"></i>
                  <span>{{ item.contactPhone || '—' }}</span>
                </p>
              </div>
            </div>

            <div class="card form-card">
              <h3 class="form-card__title">填写预约信息</h3>

              <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
                <div class="form-grid">
                  <el-form-item label="用餐人数" prop="dinerCount">
                    <el-input-number v-model="form.dinerCount" :min="1" :max="50" style="width: 100%" />
                    <p class="form-tip">超过 10 人建议在备注里说明，方便商家预留大桌或包间。</p>
                  </el-form-item>

                  <el-form-item label="到店时间" prop="reservationTime">
                    <el-date-picker
                      v-model="form.reservationTime"
                      type="datetime"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      placeholder="选择到店日期与时间"
                      :disabled-date="disablePast"
                      style="width: 100%"
                    />
                  </el-form-item>

                  <el-form-item label="联系人姓名" prop="userName">
                    <el-input v-model="form.userName" placeholder="到店联系人姓名" clearable />
                  </el-form-item>

                  <el-form-item label="联系手机号" prop="contactPhone">
                    <el-input v-model="form.contactPhone" v-bind="phoneGuard" placeholder="11 位手机号" clearable />
                  </el-form-item>
                </div>

                <el-form-item label="用餐备注">
                  <el-input
                    v-model="form.diningRemark"
                    type="textarea"
                    :rows="3"
                    maxlength="200"
                    show-word-limit
                    placeholder="例如：需要靠窗位、有老人小孩、忌口辣、需要宝宝椅"
                  />
                </el-form-item>
              </el-form>
            </div>
          </div>

          <aside class="side-card">
            <h3 class="side-card__title">预约信息</h3>

            <div class="kv">
              <div class="kv__row">
                <span class="kv__k">用餐人数</span>
                <span class="kv__v">{{ form.dinerCount }} 人</span>
              </div>
              <div class="kv__row">
                <span class="kv__k">到店时间</span>
                <span class="kv__v">{{ form.reservationTime ? fmtDateTime(form.reservationTime) : '待选择' }}</span>
              </div>
              <div class="kv__row">
                <span class="kv__k">人均参考</span>
                <span class="kv__v">{{ cost.amount !== null ? '¥' + cost.amount : cost.text }}</span>
              </div>
            </div>

            <!-- 餐厅预约没有金额可算，所以这里不摆「应付总额」，只说明结算方式 -->
            <div class="sum sum--note">
              <span>预付金额</span>
              <strong class="sum__none">无</strong>
            </div>

            <button class="btn btn-primary btn-block" :disabled="submitting" @click="submit">
              {{ submitting ? '提交中…' : '提交预约' }}
            </button>
            <p class="side-card__hint">
              餐厅预约仅保留座位，不支持在线支付，到店后按实际点餐结算。提交后需商家审核。
            </p>
          </aside>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import PageHero from '@/components/PageHero.vue'
import OrderSteps from '@/components/OrderSteps.vue'
import ResultState from '@/components/ResultState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { getRestaurant, saveRestaurantReservation } from '@/api/restaurant'
import { icons } from '@/common/scenes'
import { splitTags } from '@/common/media'
import { fmtDateTime, genOrderNo, parseAmount } from '@/common/format'
import { disablePast } from '@/common/date'
import { phoneGuard } from '@/common/inputFilter'
import { isMobile } from '@/common/validate'
import { ensureLogin, currentUser } from '@/common/user'
import { ElMessage } from 'element-plus'

const pad = (n) => String(n).padStart(2, '0')

/**
 * 默认到店时间给下一个饭点，省得用户从零点开始翻日历。
 * 11 点前 → 今天 12:00；17 点前 → 今天 18:00；再往后 → 明天 12:00。
 */
function nextMealTime() {
  const now = new Date()
  const target = new Date(now)
  target.setMinutes(0, 0, 0)
  if (now.getHours() < 11) {
    target.setHours(12)
  } else if (now.getHours() < 17) {
    target.setHours(18)
  } else {
    target.setDate(target.getDate() + 1)
    target.setHours(12)
  }
  return `${target.getFullYear()}-${pad(target.getMonth() + 1)}-${pad(target.getDate())} ${pad(target.getHours())}:00:00`
}

export default {
  name: 'RestaurantOrder',
  components: { PageHero, OrderSteps, ResultState, SafeImage },
  data() {
    const user = currentUser()
    return {
      icons,
      // 守卫对象要在这里露一次面才进得了模板（import 绑定在模板里看不见）。
      // 不能放 methods：Vue 会对 methods 上的每项做 bind，普通对象没有 bind 会直接抛。
      phoneGuard,
      item: null,
      loading: true,
      submitting: false,
      submitted: false,
      reservationNo: '',

      // 字段名逐字对齐 RestaurantReservationEntity
      form: {
        dinerCount: 2,
        reservationTime: nextMealTime(),
        diningRemark: '',
        userName: user.name,
        contactPhone: user.phone,
      },

      rules: {
        dinerCount: [{ required: true, message: '请选择用餐人数', trigger: 'change' }],
        reservationTime: [{ required: true, message: '请选择到店时间', trigger: 'change' }],
        userName: [{ required: true, message: '请填写联系人姓名', trigger: 'blur' }],
        contactPhone: [
          { required: true, message: '请填写联系手机号', trigger: 'blur' },
          { validator: isMobile, trigger: 'blur' },
        ],
      },
    }
  },
  computed: {
    id() {
      return this.$route.params.id
    },
    dishes() {
      return splitTags(this.item && this.item.signatureDish)
    },
    cost() {
      const raw = this.item && this.item.avgCost
      const amount = parseAmount(raw)
      if (amount !== null) return { amount, text: '' }
      return { amount: null, text: raw ? String(raw) : '以到店为准' }
    },
  },
  created() {
    this.fetchDetail()
  },
  methods: {
    fmtDateTime,
    // import 进来的函数同样进不了模板，:disabled-date 要的是能取到的名字。
    // 它不读 this，挂进 methods 后被 bind 一次没有副作用。
    disablePast,
    async fetchDetail() {
      this.loading = true
      try {
        const res = await getRestaurant(this.id)
        this.item = res.data.code === 0 ? res.data.data : null
      } catch (e) {
        this.item = null
      } finally {
        this.loading = false
      }
    },
    async submit() {
      if (!(await ensureLogin(this.$router, 'restaurant'))) return

      try {
        await this.$refs.formRef.validate()
      } catch (e) {
        ElMessage.warning('请先补全必填信息')
        return
      }

      this.submitting = true
      const user = currentUser()
      // 单号在这里生成，随请求体一起落库 —— 个人中心的消费记录会回显它
      const reservationNo = genOrderNo('CY')

      const body = {
        reservationNo,
        restaurantName: this.item.restaurantName,
        restaurantImage: this.item.restaurantImage,
        dinerCount: this.form.dinerCount,
        reservationTime: this.form.reservationTime,
        diningRemark: this.form.diningRemark,
        userAccount: user.account,
        userName: this.form.userName,
        contactPhone: this.form.contactPhone,
        // 和 create_db.sql 里 audit_status 的默认值一致；后台审核时由管理员改写
        auditStatus: '待审核',
        auditReply: '',
      }

      try {
        const res = await saveRestaurantReservation(body)
        if (res.data.code === 0) {
          this.reservationNo = reservationNo
          this.submitted = true
          window.scrollTo({ top: 0, behavior: 'smooth' })
        } else {
          ElMessage.error(res.data.msg || '预约失败，请稍后重试')
        }
      } catch (e) {
        // 传输层错误 http 拦截器已经弹过提示
      } finally {
        this.submitting = false
      }
    },
  },
}
</script>

<style scoped>
.skel-block {
  height: 520px;
}

.order-col {
  display: flex;
  flex-direction: column;
  gap: 26px;
}

.goods {
  flex-direction: row;
  overflow: hidden;
}

.goods__media {
  flex: 0 0 220px;
}

.goods__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 10px;
  padding: 22px 24px;
}

.goods__title {
  color: var(--ink);
  font-size: 20px;
  font-weight: 700;
}

.goods__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.goods__meta {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  color: var(--ink-3);
  font-size: 13px;
}

.goods__meta .meta-ico {
  display: inline-flex;
  flex: 0 0 auto;
  width: 14px;
  height: 14px;
}

.goods__meta svg {
  width: 14px;
  height: 14px;
}

.goods__meta span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.form-card {
  padding: 26px 28px 10px;
}

.form-card__title {
  margin-bottom: 18px;
  color: var(--ink);
  font-size: 17px;
  font-weight: 700;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 24px;
}

.form-tip {
  margin-top: 2px;
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.6;
}

.side-card__title {
  margin-bottom: 8px;
  color: var(--ink);
  font-size: 17px;
  font-weight: 700;
}

.sum {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 18px 0;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
  color: var(--ink-2);
  font-size: 14px;
}

.sum__none {
  color: var(--ink-3);
  font-size: 20px;
  font-weight: 700;
}

.side-card__hint {
  margin-top: 12px;
  color: var(--ink-3);
  font-size: 12.5px;
  line-height: 1.7;
}

.done {
  align-items: center;
  max-width: 620px;
  margin: 8px auto 0;
  padding: 44px 40px;
  text-align: center;
}

.done__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  border-radius: 50%;
  background: var(--teal-soft);
  color: var(--teal);
}

.done__icon :deep(svg) {
  width: 30px;
  height: 30px;
}

.done__title {
  margin-top: 18px;
  color: var(--ink);
  font-size: 22px;
  font-weight: 800;
}

.done__no {
  margin-top: 8px;
  color: var(--ink-3);
  font-size: 13.5px;
}

.done__no strong {
  color: var(--ink);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  letter-spacing: 0.5px;
}

.done__kv {
  width: 100%;
  margin: 26px 0 8px;
  text-align: left;
}

.done__actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
}

@media (max-width: 860px) {
  .form-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .goods {
    flex-direction: column;
  }

  .goods__media {
    flex: 0 0 auto;
  }

  .done {
    padding: 30px 22px;
  }

  .done__actions {
    flex-direction: column;
    width: 100%;
  }
}
</style>
