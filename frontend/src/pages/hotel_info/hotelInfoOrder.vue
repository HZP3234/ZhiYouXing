<template>
  <div class="page">
    <PageHero
      compact
      kicker="确认预订"
      :title="submitted ? (paid ? '支付成功' : '预订成功') : `预订${item ? item.roomName : ''}`"
      :desc="submitted
        ? (paid ? '房费已付清，订单可在个人中心的「已支付」里查看。' : '订单已提交，付清房费后凭预约号到店办理入住。')
        : '酒店订单按实名入住：先完成实名认证，再核对房型、入住日期与入住人。'"
      scene="second"
    />

    <div class="container">
      <!-- 提交成功：整页换成回执，避免用户回头再点一次提交 -->
      <div v-if="submitted" class="done card">
        <span class="done__icon" :class="paid ? 'done__icon--paid' : 'done__icon--unpaid'" v-html="paid ? icons.check : icons.clock"></span>
        <h2 class="done__title">{{ paid ? '支付成功' : '预订提交成功' }}</h2>
        <p class="done__no">预约号 <strong>{{ reservationNo }}</strong></p>
        <p class="done__state tag" :class="paid ? 'tag--teal' : 'tag--price'">{{ paid ? '已支付' : '待支付' }}</p>

        <div class="done__kv kv">
          <div class="kv__row"><span class="kv__k">酒店</span><span class="kv__v">{{ item.hotelName }}</span></div>
          <div class="kv__row"><span class="kv__k">房型</span><span class="kv__v">{{ item.roomName }} × {{ form.roomCount }} 间</span></div>
          <div class="kv__row"><span class="kv__k">入住日期</span><span class="kv__v">{{ form.reservationTime }}</span></div>
          <div class="kv__row"><span class="kv__k">入住晚数</span><span class="kv__v">{{ form.stayDays }} 晚</span></div>
          <div class="kv__row"><span class="kv__k">入住人（实名本人）</span><span class="kv__v">{{ identity.realName }} · {{ identity.idCardMasked }}</span></div>
          <div class="kv__row"><span class="kv__k">联系手机号</span><span class="kv__v">{{ form.contactPhone }}</span></div>
          <div class="kv__row"><span class="kv__k">房费</span><span class="kv__v done__money">¥{{ fmtMoney(totalAmount) }}</span></div>
        </div>

        <div class="done__actions">
          <button v-if="!paid" class="btn btn-primary" @click="payOpen = true">
            立即支付 ¥{{ fmtMoney(totalAmount) }}
          </button>
          <button v-else class="btn btn-primary" @click="$router.push('/index/center')">去个人中心查看</button>
          <button class="btn btn-ghost" @click="$router.push('/index/hotel_info')">继续看其他房型</button>
        </div>

        <p class="done__hint">
          {{ paid
            ? '订单已进入个人中心的「已支付」，可以在那里继续评价。'
            : '未支付的订单会在 10 分钟后自动取消；也可以稍后在个人中心的「待支付」里完成付款。' }}
        </p>
      </div>

      <template v-else>
        <OrderSteps :current="1" :steps="['选择房型', '填写信息', '提交预订']" />

        <div v-if="loading" class="skeleton skel-block"></div>

        <ResultState
          v-else-if="!item"
          status="error"
          text="房型信息加载失败"
          hint="这条记录不存在，或者接口没有响应。"
          @retry="fetchDetail"
        >
          <button class="btn btn-ghost" @click="$router.push('/index/hotel_info')">返回酒店列表</button>
        </ResultState>

        <div v-else class="detail-main">
          <div class="order-col">
            <div class="card goods">
              <div class="goods__media">
                <SafeImage :src="item.roomImage" :seed="item.id" ratio="4 / 3" :alt="item.roomName" />
              </div>
              <div class="goods__body">
                <span class="goods__eyebrow">{{ item.hotelName }}</span>
                <h3 class="goods__title">{{ item.roomName }}</h3>
                <div class="goods__tags">
                  <span class="tag tag--brand">{{ item.roomType }}</span>
                  <span v-for="f in facilities.slice(0, 3)" :key="f" class="tag">{{ f }}</span>
                </div>
                <p v-if="hotel && hotel.hotelAddress" class="goods__meta">{{ hotel.hotelAddress }}</p>
                <div class="goods__price"><small>¥</small>{{ fmtMoney(item.roomPrice) }}<em>/ 间 / 晚</em></div>
              </div>
            </div>

            <div class="card form-card">
              <!--
                实名认证区块。已认证时只读展示，未认证时就是预订的前置表单 ——
                两者的字段都在同一个 el-form 里，因为提交前只需要一次 validate()，
                分成两个 form 反而要判两次（拿到的两份校验结果还得自己合并）。
              -->
              <div class="form-card__head">
                <h3 class="form-card__title">实名认证</h3>
                <span class="tag" :class="verified ? 'tag--teal' : 'tag--price'">
                  {{ identityTag }}
                </span>
              </div>

              <div v-if="identityLoading" class="verify-skel skeleton"></div>

              <p v-else-if="verified" class="verify-done">
                已认证：{{ identity.realName }} · {{ identity.idCardMasked }}
                <span v-if="identity.verifyTime" class="verify-done__time">（{{ fmtDateTime(identity.verifyTime) }}）</span>
              </p>

              <!-- 提交了但还没人审：这时不再给表单，否则用户会以为要重填一遍（重填也只是重交同一份材料） -->
              <p v-else-if="pending" class="form-tip form-tip--warn">
                实名认证已提交，正在等待管理员审核，通过后即可预订。
                审核结果会显示在个人中心，期间无需重复提交。
              </p>

              <!-- 被驳回：给出管理员的原话，并放行表单让他改了重交 -->
              <p v-else-if="rejected" class="form-tip form-tip--warn">
                上次实名认证未通过：{{ identity.auditReply || '请核对姓名与身份证号' }}。
                请修正下面的信息后重新提交。
              </p>

              <p v-else class="form-tip form-tip--warn">
                酒店按实名入住，预订前必须完成实名认证。姓名与证件号将随订单一并提交，
                提交后不能自行修改（如需变更请联系客服），请核对后再填。
              </p>

              <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
                <div v-if="!identityLoading && !verified && !pending" class="form-grid">
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="form.realName" placeholder="与身份证一致" maxlength="30" clearable />
                  </el-form-item>

                  <el-form-item label="身份证号" prop="idCard">
                    <!--
                      用 idCardGuard 在输入期就剔掉非数字与多余的 X（末尾 X 保留、小写转大写），
                      所以不要设 maxlength：浏览器是「先按 maxlength 截断、再清洗」，
                      粘一份带空格/连字符的 18 位证件号时会把末位校验码截掉，而用户看不出来。
                      @blur 那道归一化留着兜底（长度不对由规则给提示）。
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

                <h3 class="form-card__title form-card__title--sub">填写预订信息</h3>

                <div class="form-grid">
                  <el-form-item label="入住日期" prop="reservationTime">
                    <el-date-picker
                      v-model="checkInDate"
                      type="date"
                      value-format="YYYY-MM-DD"
                      placeholder="选择入住日期"
                      :disabled-date="disablePast"
                      style="width: 100%"
                    />
                    <p class="form-tip">入住当天 14:00 后可办理入住。</p>
                  </el-form-item>

                  <el-form-item label="入住晚数" prop="stayDays">
                    <el-input-number v-model="form.stayDays" :min="1" :max="30" style="width: 100%" />
                  </el-form-item>

                  <el-form-item label="房间数" prop="roomCount">
                    <!-- form.roomCount 是「订几间」，item.roomCount 是「剩几间」，同名不同义 -->
                    <el-input-number v-model="form.roomCount" :min="1" :max="maxRooms" style="width: 100%" />
                    <p v-if="soldOut" class="form-tip form-tip--warn">该房型当前已订满，暂时无法预订。</p>
                    <p v-else class="form-tip">该房型剩余 {{ maxRooms }} 间。</p>
                  </el-form-item>

                  <el-form-item label="离店日期">
                    <el-input :model-value="checkOutDate" readonly />
                    <p class="form-tip">由入住日期 + 晚数自动算出。</p>
                  </el-form-item>

                  <!-- 入住人不再是可填字段：酒店按实名登记，入住人就是认证的那个人（后端按 user_identity 写入） -->
                  <el-form-item label="入住人">
                    <el-input :model-value="verified ? identity.realName : '完成实名认证后自动带出'" disabled />
                    <p class="form-tip">订单按实名信息登记，入住人即认证本人，不支持改成他人。</p>
                  </el-form-item>

                  <el-form-item label="联系手机号" prop="contactPhone">
                    <el-input v-model="form.contactPhone" v-bind="phoneGuard" placeholder="11 位手机号" clearable />
                    <p class="form-tip">
                      已从个人中心带出（没填过请到个人中心补上），酒店联系入住客人用这个号码。
                    </p>
                  </el-form-item>
                </div>
              </el-form>
            </div>
          </div>

          <aside class="side-card">
            <h3 class="side-card__title">费用明细</h3>

            <div class="kv">
              <div class="kv__row">
                <span class="kv__k">房间单价</span>
                <span class="kv__v">¥{{ fmtMoney(item.roomPrice) }} / 间 / 晚</span>
              </div>
              <div class="kv__row">
                <span class="kv__k">房间数</span>
                <span class="kv__v">× {{ form.roomCount }} 间</span>
              </div>
              <div class="kv__row">
                <span class="kv__k">入住晚数</span>
                <span class="kv__v">× {{ form.stayDays }} 晚</span>
              </div>
              <div class="kv__row">
                <span class="kv__k">入住 / 离店</span>
                <span class="kv__v">{{ checkInDate || '待选择' }} → {{ checkOutDate }}</span>
              </div>
            </div>

            <div class="sum">
              <span>应付总额</span>
              <strong><small>¥</small>{{ fmtMoney(totalAmount) }}</strong>
            </div>

            <button class="btn btn-primary btn-block" :disabled="submitting || soldOut || pending" @click="submit">
              {{ submitting ? '提交中…' : submitLabel }}
            </button>
            <p class="side-card__hint">
              提交即表示同意酒店预订须知。未认证的账号会先用上面的信息提交实名认证，
              审核通过后才能预订；未通过不会生成订单。
              提交后可在本页当场支付，也可以稍后在个人中心的「待支付」里付款（未支付的订单 10 分钟后自动取消）。
            </p>
          </aside>
        </div>
      </template>
    </div>

    <!-- 收银台弹窗：与个人中心、景点下单页共用一个组件 -->
    <PayDialog
      v-model="payOpen"
      :amount="totalAmount"
      :loading="paying"
      @confirm="pay"
    />
  </div>
</template>

<script>
import PageHero from '@/components/PageHero.vue'
import OrderSteps from '@/components/OrderSteps.vue'
import PayDialog from '@/components/PayDialog.vue'
import ResultState from '@/components/ResultState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { getRoom, getHotelByName, saveHotelReservation } from '@/api/hotel'
import { getIdentity, saveIdentity } from '@/api/identity'
import { payConsumption } from '@/api/consumption'
import { icons } from '@/common/scenes'
import { splitTags } from '@/common/media'
import { fmtDateTime, fmtMoney, genOrderNo, roundMoney } from '@/common/format'
import { addDays, disablePast, withTime } from '@/common/date'
import { idCardGuard, phoneGuard } from '@/common/inputFilter'
import { isIdCard, isMobile, isRealName, normalizeIdCard } from '@/common/validate'
import { ensureLogin, currentUser } from '@/common/user'
import { ElMessage } from 'element-plus'

export default {
  name: 'HotelInfoOrder',
  components: { PageHero, OrderSteps, PayDialog, ResultState, SafeImage },
  data() {
    const user = currentUser()
    return {
      icons,
      // 守卫对象要在这里露一次面才进得了模板（import 绑定在模板里看不见）。
      // 不能放 methods：Vue 会对 methods 上的每项做 bind，普通对象没有 bind 会直接抛。
      phoneGuard,
      idCardGuard,
      // 客房（room_type 一行）。下单订的是这一间
      item: null,
      // 它所属的酒店（hotel_info 一行），只用来显示地址 —— 客房行里没有这一列
      hotel: null,
      loading: true,
      submitting: false,
      submitted: false,
      reservationNo: '',

      /* 实名认证状态。identityLoading 期间认证表单不渲染，免得未认证的用户先看到一半表单就提交 */
      identityLoading: true,
      identity: { verified: false, realName: '', idCardMasked: '', verifyTime: '', auditStatus: '', auditReply: '' },
      /* 回执页的支付状态。orderId 是下单接口回传的落库主键，付款要拿它去 /consumption/pay */
      orderId: null,
      paying: false,
      paid: false,
      /* 收银台弹窗。金额由 totalAmount 给，确认后走 pay() */
      payOpen: false,

      /**
       * 入住日期用单独的 date picker 绑一个 'yyyy-MM-dd' 串，
       * 提交时再拼上时间补成 'yyyy-MM-dd HH:mm:ss' —— 实体里 reservationTime 是这个格式，
       * 直接塞 'yyyy-MM-dd' 后端 LocalDateTime 反序列化会失败。
       */
      checkInDate: '',

      // 字段名逐字对齐 HotelReservationEntity（userName / idCard / userAccount / isPay 由后端补，见 submit）
      form: {
        roomCount: 1,
        stayDays: 1,
        reservationTime: '',
        contactPhone: user.phone,
        // 未认证时才填，认证之后这两个字段不再发给后端
        realName: '',
        idCard: '',
      },

      rules: {
        reservationTime: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
        stayDays: [{ required: true, message: '请选择入住晚数', trigger: 'change' }],
        roomCount: [{ required: true, message: '请选择房间数', trigger: 'change' }],
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
    id() {
      return this.$route.params.id
    },
    facilities() {
      return splitTags(this.item && this.item.roomFacility)
    },
    /** item.roomCount 是剩余间数 */
    maxRooms() {
      const n = Number(this.item && this.item.roomCount)
      return Number.isFinite(n) && n > 0 ? Math.floor(n) : 0
    },
    soldOut() {
      return this.item ? this.maxRooms === 0 : false
    },
    // 入住日期 + 晚数。空值/算不出来由 addDays 给 '—'，不用在这里判（行为与原来逐字一致）
    checkOutDate() {
      return this.addDays(this.checkInDate, this.form.stayDays)
    },
    totalAmount() {
      if (!this.item) return 0
      return roundMoney(
        Number(this.item.roomPrice) * Number(this.form.roomCount || 0) * Number(this.form.stayDays || 0),
      )
    },
    verified() {
      return this.identity.verified === true
    },
    /*
     * 认证状态有三档：没提交过（auditStatus 空串）、待审核、已驳回。
     * verified 只认「已通过」，所以预订页要自己把另外两种分开 ——
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
      return this.verified ? '提交预订' : '实名认证并提交预订'
    },
  },
  watch: {
    // 日期和表单对象分开存，变化时同步进 form，校验规则才拿得到值。
    // 那个 14:00:00 不是随便定的：reservationTime 是 LocalDateTime，只发 'yyyy-MM-dd'
    // 会被 Jackson 判 400（见 src/common/date.js 的 withTime）
    checkInDate(v) {
      this.form.reservationTime = withTime(v)
    },
  },
  created() {
    this.fetchDetail()
    this.fetchIdentity()
  },
  methods: {
    fmtMoney,
    fmtDateTime,
    normalizeIdCard,
    // import 进来的函数同样进不了模板，:disabled-date / 离店日期都要能取到名字。
    // 它们不读 this，挂进 methods 后被 bind 一次没有副作用。
    disablePast,
    addDays,
    /**
     * 拉认证状态。接口挂了按「未认证」处理而不是报错：
     * 用户照样能看到并填写认证表单，真正的判定在提交时（和下单一样由服务端说了算）。
     */
    async fetchIdentity() {
      this.identityLoading = true
      try {
        const res = await getIdentity()
        const data = res.data.code === 0 ? res.data.data : null
        this.identity = {
          verified: Boolean(data && data.verified),
          realName: (data && data.realName) || '',
          idCardMasked: (data && data.idCardMasked) || '',
          verifyTime: (data && data.verifyTime) || '',
          auditStatus: (data && data.auditStatus) || '',
          auditReply: (data && data.auditReply) || '',
        }
      } catch (e) {
        this.identity = { verified: false, realName: '', idCardMasked: '', verifyTime: '', auditStatus: '', auditReply: '' }
      } finally {
        this.identityLoading = false
      }
    },
    async fetchDetail() {
      this.loading = true
      try {
        const res = await getRoom(this.id)
        this.item = res.data.code === 0 ? res.data.data : null
        if (this.item && this.maxRooms > 0 && this.form.roomCount > this.maxRooms) {
          this.form.roomCount = this.maxRooms
        }
        this.fetchHotel()
      } catch (e) {
        this.item = null
      } finally {
        this.loading = false
      }
    },

    /** 客房行里没有酒店地址（那在 hotel_info 表上），按 hotel_name 回头取一下；取不到就不显示 */
    async fetchHotel() {
      if (!this.item) return
      try {
        this.hotel = await getHotelByName(this.item.hotelName)
      } catch (e) {
        this.hotel = null
      }
    },

    async submit() {
      if (!(await ensureLogin(this.$router, 'hotel_info'))) return
      // 按钮已经置灰，这里再挡一次：审核中的单进不去，也不该白跑一趟后端
      if (this.pending) {
        ElMessage.warning('实名认证正在审核中，通过后即可预订')
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
        // 1) 未认证先认证。认证不通过直接停在这里，不会留下半条订单
        if (!this.verified && !(await this.certify())) return

        /*
         * 2) 下单。
         * reservation_no 在库里是 UNIQUE，撞了会 500，所以带随机后缀；按钮也在 loading 中防连点。
         *
         * 只发「这单订了什么」：userAccount / userName / idCard / isPay 都不发 ——
         * 后端一律以登录态和 user_identity 为准（见 HotelReservationController.createOrder），
         * 前端发了也会被覆盖，写了反而让人以为这些值说了算。
         */
        const reservationNo = genOrderNo('HT')
        const body = {
          reservationNo,
          roomName: this.item.roomName,
          roomImage: this.item.roomImage,
          roomPrice: this.item.roomPrice,
          roomCount: this.form.roomCount,
          stayDays: this.form.stayDays,
          totalAmount: this.totalAmount,
          hotelName: this.item.hotelName,
          reservationTime: this.form.reservationTime,
          contactPhone: this.form.contactPhone,
        }

        const res = await saveHotelReservation(body)
        if (res.data.code !== 0) {
          ElMessage.error(res.data.msg || '预订失败，请稍后重试')
          return
        }

        this.reservationNo = reservationNo
        // 回执页要用 id 付款，后端只回 {id, reservationNo}（不回整条实体，里面有证件号）
        const saved = res.data.data || {}
        this.orderId = saved.id || null
        this.submitted = true
        window.scrollTo({ top: 0, behavior: 'smooth' })
      } catch (e) {
        // 传输层错误已经由 http 拦截器弹过提示了，这里不再重复弹
      } finally {
        this.submitting = false
      }
    },

    /** 提交实名认证。返回是否已通过（通过后 identity 里就是脱敏信息，前端不再持有完整证件号） */
    async certify() {
      const body = {
        realName: String(this.form.realName || '').trim(),
        idCard: normalizeIdCard(this.form.idCard),
      }
      try {
        const res = await saveIdentity(body)
        if (res.data.code !== 0) {
          ElMessage.error(res.data.msg || '实名认证失败，请核对姓名与身份证号')
          return false
        }
        const data = res.data.data || {}
        this.identity = {
          verified: Boolean(data.verified),
          realName: data.realName || '',
          idCardMasked: data.idCardMasked || '',
          verifyTime: data.verifyTime || '',
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
          ElMessage.success('实名认证已提交，管理员审核通过后即可预订')
          return false
        }
        ElMessage.error('实名认证未通过，请检查姓名与身份证号')
        return false
      } catch (e) {
        return false
      }
    },

    /**
     * 支付。
     *
     * 和实名认证一样走服务端：这一步只把订单的 is_pay 翻成「已支付」，
     * 所以个人中心的「已支付」分区在付款成功的下一次请求里就能看到这张单。
     *
     * source 用 'hotel'：ConsumptionController 按它分流到 payHotelReservation，
     * 那条 SQL 会按登录态核 user_account，别人的单改不动。
     */
    async pay() {
      if (!this.orderId) {
        this.payOpen = false
        ElMessage.warning('没有拿到预约号，请到个人中心的「待支付」里付款')
        return
      }
      this.paying = true
      try {
        const res = await payConsumption({ source: 'hotel', id: this.orderId })
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

.goods__eyebrow {
  color: var(--brand);
  font-size: 13px;
  font-weight: 600;
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
  color: var(--ink-3);
  font-size: 13px;
}

.goods__price {
  display: flex;
  align-items: baseline;
  gap: 2px;
  margin-top: auto;
  color: var(--price);
  font-size: 26px;
  font-weight: 800;
}

.goods__price small {
  font-size: 14px;
  font-weight: 700;
}

.goods__price em {
  margin-left: 4px;
  color: var(--ink-3);
  font-size: 12.5px;
  font-style: normal;
  font-weight: 500;
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

.form-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.form-card__head .form-card__title {
  margin-bottom: 0;
}

/* 实名认证与预订信息在同一个 el-form 里，靠这条虚线把两段分开 */
.form-card__title--sub {
  margin-top: 22px;
  padding-top: 20px;
  border-top: 1px dashed var(--line);
}

.verify-done {
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: var(--teal-soft);
  color: var(--teal);
  font-size: 13.5px;
  line-height: 1.7;
}

.verify-done__time {
  color: var(--ink-3);
}

.verify-skel {
  height: 42px;
  border-radius: var(--radius-sm);
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

.form-tip--warn {
  color: var(--price);
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

.sum strong {
  color: var(--price);
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
}

.sum strong small {
  margin-right: 2px;
  font-size: 15px;
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
  font-size: 24px;
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

.done__state {
  margin-top: 14px;
}

/* 未支付用暖色、已支付用青色：这两个状态在回执页是「还要不要动手」的分界线 */
.done__icon--unpaid {
  background: var(--brand-soft);
  color: var(--price);
}

.done__icon--paid {
  background: var(--teal-soft);
  color: var(--teal);
}

.done__hint {
  margin-top: 18px;
  color: var(--ink-3);
  font-size: 12.5px;
  line-height: 1.7;
}

.done__kv {
  width: 100%;
  margin: 26px 0 8px;
  text-align: left;
}

.done__money {
  color: var(--price);
  font-size: 16px;
  font-weight: 700;
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
