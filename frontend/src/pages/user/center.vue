<template>
  <div class="page">
    <PageHero
      compact
      kicker="个人中心"
      :title="`你好，${form.userName || account}`"
      desc="这里是你注册时留下的资料，姓名、性别、联系方式与头像可以自行修改，改完立即生效。"
      scene="fourth"
    />

    <div class="container">
      <div v-if="loading" class="skeleton skel-block"></div>

      <ResultState
        v-else-if="!loaded"
        status="error"
        text="资料加载失败"
        hint="登录态可能已失效，或者网关与用户服务没有启动。"
        @retry="load"
      >
        <button class="btn btn-ghost" @click="$router.push('/index/home')">回到首页</button>
      </ResultState>

      <div v-else class="center-grid">
        <div class="center-main">
          <div class="card center-card">
            <header class="center-card__head">
              <button type="button" class="center-card__avatar" @click="pickAvatar">
                <SafeImage variant="avatar" :src="form.avatar" :label="form.userName || account" :seed="account" />
                <span class="center-card__avatar-mask">更换头像</span>
              </button>
              <div class="center-card__head-text">
                <h2 class="center-card__name">{{ form.userName || '未填写姓名' }}</h2>
                <p class="center-card__sub">游客 · {{ account }}</p>
                <p class="center-card__hint">
                  点头像从电脑里选一张，选完点「保存资料」生效
                  <button v-if="form.avatar" type="button" class="center-card__clear" @click="clearAvatar">
                    移除头像
                  </button>
                </p>
              </div>
              <input
                ref="avatarInput"
                class="center-card__file"
                type="file"
                accept="image/*"
                @change="onAvatarPick"
              />
            </header>

            <el-form
              ref="profileForm"
              class="center-form"
              :model="form"
              :rules="rules"
              label-position="top"
              @submit.prevent
            >
              <el-form-item label="姓名" prop="userName">
                <el-input v-model="form.userName" placeholder="请输入姓名" maxlength="50" clearable />
              </el-form-item>

              <el-form-item label="性别" prop="gender">
                <el-select v-model="form.gender" placeholder="请选择性别" clearable style="width: 100%">
                  <el-option v-for="item in GENDERS" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>

              <el-form-item label="联系方式" prop="contactPhone">
                <el-input v-model="form.contactPhone" v-bind="phoneGuard" placeholder="11 位手机号" clearable />
                <p class="form-tip">
                  下单时会默认带出这个号码，也是商家联系你的方式，换号了记得回来改。
                </p>
              </el-form-item>
            </el-form>

            <div class="center-card__actions">
              <button class="btn btn-primary" :disabled="saving" @click="save">
                {{ saving ? '保存中…' : '保存资料' }}
              </button>
              <button class="btn btn-ghost" :disabled="saving" @click="load">放弃修改</button>
            </div>
          </div>

          <!--
            实名认证。放在个人中心是为了让「预填」真的成立：购票页要的就是这三个字段
            （姓名 / 联系方式 / 实名），在这里填过一次，购票页那一整块就不用再填。
            和上面那张卡分开是因为它是另一个接口、另一套规则：资料可以随便改，
            实名一旦通过就锁死。
          -->
          <div class="card center-card">
            <header class="center-card__head">
              <div class="center-card__head-text">
                <h2 class="center-card__name">实名认证</h2>
                <p class="center-card__sub">景区门票按人出票，认证一次长期有效，买票时不再重复填写。</p>
              </div>
              <span class="tag" :class="identity.verified ? 'tag--teal' : 'tag--price'">
                {{ identityTag }}
              </span>
            </header>

            <div v-if="identityLoading" class="skeleton skel-verify"></div>

            <template v-else-if="identity.verified">
              <div class="kv">
                <div class="kv__row">
                  <span class="kv__k">真实姓名</span>
                  <span class="kv__v">{{ identity.realName }}</span>
                </div>
                <div class="kv__row">
                  <span class="kv__k">身份证号</span>
                  <span class="kv__v">{{ identity.idCardMasked }}</span>
                </div>
                <div class="kv__row">
                  <span class="kv__k">认证时间</span>
                  <span class="kv__v">{{ fmtDateTime(identity.verifyTime) }}</span>
                </div>
              </div>
              <p class="form-tip">
                证件号只回显脱敏后的号码，完整号码不下发到浏览器。如需变更请联系客服。
              </p>
            </template>

            <!-- 已提交、等审核：不再给表单（重填也只是重交同一份材料），只说明进度 -->
            <template v-else-if="pending">
              <div class="kv">
                <div class="kv__row">
                  <span class="kv__k">真实姓名</span>
                  <span class="kv__v">{{ identity.realName }}</span>
                </div>
                <div class="kv__row">
                  <span class="kv__k">身份证号</span>
                  <span class="kv__v">{{ identity.idCardMasked }}</span>
                </div>
              </div>
              <p class="form-tip">
                认证已提交，正在等待管理员审核。审核通过后即可购票、订酒店与报团；
                在审核完成前不需要重复提交。
              </p>
            </template>

            <template v-else>
              <!-- 被驳回：把管理员的原话放出来，用户才知道该改什么 -->
              <p v-if="rejected" class="form-tip form-tip--warn">
                上次认证未通过：{{ identity.auditReply || '请核对姓名与身份证号' }}。请修正后重新提交。
              </p>

              <el-form
                ref="identityFormRef"
                class="center-form"
                :model="identityForm"
                :rules="identityRules"
                label-position="top"
                @submit.prevent
              >
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="identityForm.realName" placeholder="与身份证一致" maxlength="30" clearable />
                </el-form-item>

                <el-form-item label="身份证号" prop="idCard">
                  <!--
                    用 idCardGuard 在输入期就剔掉非数字与多余的 X（末尾 X 保留、小写转大写），
                    所以不设 maxlength：粘贴来的证件号常带空格，浏览器先按 maxlength 截断
                    会把末位校验码吃掉且看不出来。@blur 那道归一化留着兜底。
                  -->
                  <el-input
                    v-model="identityForm.idCard"
                    v-bind="idCardGuard"
                    placeholder="18 位身份证号"
                    clearable
                    @blur="identityForm.idCard = normalizeIdCard(identityForm.idCard)"
                  />
                  <p class="form-tip">认证通过后不能自行修改，请核对后再提交。</p>
                </el-form-item>
              </el-form>

              <div class="center-card__actions">
                <button class="btn btn-primary" :disabled="identitySaving" @click="submitIdentity">
                  {{ identitySaving ? '提交中…' : (rejected ? '重新提交' : '提交实名认证') }}
                </button>
              </div>
            </template>
          </div>
        </div>

        <aside class="center-side">
          <div class="card side-block">
            <h3 class="side-block__title">账号信息</h3>
            <div class="kv">
              <div class="kv__row">
                <span class="kv__k">用户账号</span>
                <span class="kv__v">{{ account || '—' }}</span>
              </div>
              <div class="kv__row">
                <span class="kv__k">用户 ID</span>
                <span class="kv__v">{{ form.id || '—' }}</span>
              </div>
              <div class="kv__row">
                <span class="kv__k">注册时间</span>
                <span class="kv__v">{{ form.addTime || '—' }}</span>
              </div>
              <div class="kv__row">
                <span class="kv__k">账号状态</span>
                <span class="kv__v">
                  <span class="tag" :class="form.status === 1 ? 'tag--price' : 'tag--teal'">
                    {{ form.status === 1 ? '已锁定' : '正常' }}
                  </span>
                </span>
              </div>
            </div>
          </div>

          <div class="card side-block side-block--note">
            <h3 class="side-block__title">关于账号</h3>
            <ul class="side-block__list">
              <li>用户账号（手机号）是系统识别你的唯一凭证，不支持修改。</li>
              <li>密码不在本页修改，请到登录页使用「忘记密码」。</li>
              <li>头像直接从电脑里选图，压缩后存在账号里；读不出来时退回姓名首字。</li>
              <li>账号连续输错密码会被锁定，锁定后请联系管理员。</li>
            </ul>
          </div>
        </aside>
      </div>

      <!-- 消费记录：与资料卡同属「登录后才能看」的内容，所以跟着 loaded 一起出现 -->
      <section class="card orders">
        <header class="orders__head">
          <div>
            <h3 class="orders__title">最近消费记录</h3>
            <p class="orders__desc">
              门票、酒店、餐厅、报团都在这里。待支付可以直接付款，也可以自己取消；未支付的订单有时限，到点会自动取消。
              餐厅到店结算，不进支付流程。
            </p>
          </div>
          <button type="button" class="orders__refresh" :disabled="ordersLoading" @click="loadOrders">
            {{ ordersLoading ? '刷新中…' : '刷新' }}
          </button>
        </header>

        <div class="orders__tabs" role="tablist">
          <button
            v-for="item in TABS"
            :key="item.key"
            type="button"
            class="orders__tab"
            :class="{ 'orders__tab--on': tab === item.key }"
            role="tab"
            :aria-selected="tab === item.key"
            @click="tab = item.key"
          >
            {{ item.label }}
            <em v-if="bucketCount(item.key)">{{ bucketCount(item.key) }}</em>
          </button>
        </div>

        <div v-if="ordersLoading" class="skeleton orders__skel"></div>

        <ResultState
          v-else-if="!ordersLoaded"
          status="error"
          text="消费记录加载失败"
          hint="登录态可能已失效，或者网关与用户服务没有启动。"
          @retry="loadOrders"
        />

        <ResultState
          v-else-if="!visibleOrders.length"
          status="empty"
          :text="emptyText"
          hint="下单、预订或报团之后，记录会出现在这里。"
        />

        <ul v-else class="orders__list">
          <li v-for="row in visibleOrders" :key="row.source + '-' + row.id" class="order">
            <div class="order__media">
              <SafeImage :src="row.image" :seed="row.source + row.id" :alt="row.title" ratio="1 / 1" />
            </div>

            <div class="order__body">
              <div class="order__top">
                <span class="tag tag--brand">{{ row.sourceLabel }}</span>
                <span class="tag" :class="stateClass(row)">{{ stateText(row) }}</span>
              </div>
              <h4 class="order__title">{{ row.title || '未命名' }}</h4>
              <p class="order__meta">
                <span>{{ row.quantityDesc }}</span>
                <!--
                  餐厅的着眼点是「约的哪个时段」，所以这里显示到店时间而不是下单时间；
                  没填到店时间（老数据）就退回下单那一刻，别让这一格空着。
                -->
                <span v-if="isRestaurant(row) && row.reservationTime">
                  预约 {{ fmtDateTime(row.reservationTime) }}
                </span>
                <span v-else>{{ fmtDateTime(row.addTime) }}</span>
                <span class="order__no">单号 {{ row.orderNo || '—' }}</span>
              </p>
              <p v-if="isRestaurant(row) && row.remark" class="order__remark">
                备注：{{ row.remark }}
              </p>
            </div>

            <div class="order__foot">
              <!-- 餐厅到店结算，没有金额可显示，用文字代替数字 -->
              <div v-if="isRestaurant(row)" class="order__amount order__amount--note">到店结算</div>
              <div v-else class="order__amount"><small>¥</small>{{ fmtMoney(row.amount) }}</div>

              <!--
                餐厅不参与「去支付 / 去评价」，这里只回显商家的审核回复，
                没有回复就退回到一句到店提示，免得那一块空着。
              -->
              <span v-if="isRestaurant(row)" class="order__done">
                {{ row.auditReply || '到店出示预约信息即可' }}
              </span>

              <!--
                待支付：一行倒计时 + 「取消订单 / 去支付」两个动作。
                倒计时归零时后端那边的时限也到了，按钮先自己禁掉，
                等 tick 里的静默刷新把这一单换成「已取消」，不必用户手动刷。
              -->
              <div v-else-if="!row.paid && !row.cancelled" class="order__actions">
                <span v-if="expired(row)" class="order__countdown order__countdown--over">已超时</span>
                <span v-else-if="row.expireAt" class="order__countdown">
                  {{ remainText(row) }} 后关闭
                </span>
                <div class="order__btns">
                  <button
                    type="button"
                    class="btn btn-ghost btn-sm"
                    :disabled="acting"
                    @click="cancelOrder(row)"
                  >
                    取消订单
                  </button>
                  <button
                    type="button"
                    class="btn btn-primary btn-sm"
                    :disabled="acting || expired(row)"
                    @click="openPay(row)"
                  >
                    去支付
                  </button>
                </div>
              </div>

              <span v-else-if="row.cancelled" class="order__done">订单已取消</span>
              <button
                v-else-if="!row.commented"
                type="button"
                class="btn btn-ghost btn-sm"
                @click="openComment(row)"
              >
                去评价
              </button>
              <span v-else class="order__done">已完成</span>
            </div>
          </li>
        </ul>
      </section>
    </div>

    <!--
      评价弹窗。放在 .card 外面：弹窗是浮层，挂进任何带 overflow 或层叠上下文的卡片里
      都可能被裁掉，append-to-body 让它直接挂到 body 上。
    -->
    <el-dialog
      v-model="commentOpen"
      :title="commentTarget ? '评价：' + commentTarget.title : '发表评价'"
      width="480px"
      append-to-body
    >
      <div v-if="commentTarget" class="cmt">
        <p class="cmt__head">
          <span class="tag tag--brand">{{ commentTarget.sourceLabel }}</span>
          <span class="cmt__no">单号 {{ commentTarget.orderNo || '—' }}</span>
        </p>

        <el-form label-position="top" @submit.prevent>
          <el-form-item v-if="commentTarget.source !== 'group'" label="评分">
            <el-rate v-model="commentForm.score" :max="5" show-score score-template="{value} 分" />
          </el-form-item>

          <el-form-item label="评价内容">
            <el-input
              v-model="commentForm.content"
              type="textarea"
              :rows="4"
              maxlength="300"
              show-word-limit
              placeholder="说说这次体验怎么样，写下的内容会显示在对应的详情页评论区。"
            />
            <p v-if="commentTarget.source === 'group'" class="form-tip">
              线路评论区没有星级字段，报团的评价只保存文字。
            </p>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <button type="button" class="btn btn-ghost" :disabled="commentSaving" @click="commentOpen = false">
          取消
        </button>
        <button type="button" class="btn btn-primary" :disabled="commentSaving" @click="submitComment">
          {{ commentSaving ? '提交中…' : '提交评价' }}
        </button>
      </template>
    </el-dialog>

    <!-- 收银台弹窗。金额由待付款的那条记录给 -->
    <PayDialog
      v-model="payOpen"
      :amount="payTarget ? Number(payTarget.amount) : null"
      :loading="acting"
      @confirm="payOrder"
    />
  </div>
</template>

<script>
import PageHero from '@/components/PageHero.vue'
import PayDialog from '@/components/PayDialog.vue'
import ResultState from '@/components/ResultState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { listConsumption, payConsumption, cancelConsumption, commentConsumption } from '@/api/consumption'
import { getIdentity, saveIdentity } from '@/api/identity'
import { isIdCard, isMobile, isRealName, normalizeIdCard } from '@/common/validate'
import { fmtDateTime, fmtMoney } from '@/common/format'
import { idCardGuard, phoneGuard } from '@/common/inputFilter'

/** user.gender 是 varchar，没有字典表，前端给三个常用值就够 */
const GENDERS = ['男', '女', '保密']

/** 选图上限 5MB，超了直接劝退，别让浏览器读一个几十 MB 的文件 */
const MAX_AVATAR_FILE = 5 * 1024 * 1024

/** 头像最长边。56px 的圆框用不着比这更大的图 */
const AVATAR_MAX_SIDE = 200

/*
 * 消费记录的分区。
 *
 * 「已支付」是所有付过款的单，也就是待评价 + 已评价的并集 —— 一条记录会同时
 * 出现在「已支付」和「待评价」里。这样点「已支付」看到的是完整流水，
 * 想看「还没评价的有哪些」再点「待评价」，两个问题各答一次。
 *
 * 「全部」在最前面，四类订单（门票 / 酒店 / 餐厅 / 报团）都能看到。
 *
 * 餐厅预约另开一页「预约」，不能只丢在「全部」里：它是四类里唯一一张
 * 「订座审核」表 —— 没有支付也没有评价，落不进后面任何一个分区，
 * 混在「全部」里等于没有归宿。用户找自己的预约时会先去找一个叫「预约」的
 * 页签，找不到就以为压根没约上（实测就是这么反馈的）。
 *
 * 「已取消」同样单开一页而不是并进「全部」了事：作废的单子是用户自己点掉、
 * 或者眼睁睁看它超时的，找起来最急的就是它 —— 它不在「待支付」里，
 * 要是也不单独给个位置，用户会以为这单凭空没了。
 */
const TABS = [
  { key: 'all', label: '全部' },
  { key: 'reservation', label: '预约' },
  { key: 'pendingPay', label: '待支付' },
  { key: 'paid', label: '已支付' },
  { key: 'pendingComment', label: '待评价' },
  { key: 'commented', label: '已评价' },
  { key: 'cancelled', label: '已取消' },
]

/*
 * 分区靠后端算好的 paid / commented / cancelled 三个布尔来分，不比对「已支付」
 * 这类中文字面量：订单表那两列是 varchar、没有约束，写法一变前端就会静默分错区。
 *
 * 餐厅的 paid 恒为 false（后端 isPay 回 null），所以它天然落进「待支付」——
 * 那是不对的，它并没有欠款。逐个分区里显式排掉餐厅，只让它出现在
 * 「全部」和「预约」两页。
 *
 * 已取消的单同样 paid 为 false，必须再排掉一次：不排的话它会同时挂在
 * 「待支付」（还带个「去支付」按钮）和「已取消」两页里。
 */
const isRestaurant = (row) => row.source === 'restaurant'

const BUCKETS = {
  all: () => true,
  reservation: (row) => isRestaurant(row),
  pendingPay: (row) => !isRestaurant(row) && !row.paid && !row.cancelled,
  paid: (row) => row.paid,
  pendingComment: (row) => row.paid && !row.commented,
  commented: (row) => row.paid && row.commented,
  cancelled: (row) => row.cancelled,
}

/*
 * 图片 → 200px 的 JPEG data URL。
 *
 * user.avatar 是 longtext，但 /user/session 每次进 /index 页面都会把整条用户记录
 * 带回来，原图直存等于让每个页面都背上几百 KB。压到 200px 后通常只剩二三十 KB，
 * 顺带避开网关 WebFlux 默认 256KB 的请求体上限。
 */
function toAvatarDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onerror = () => reject(new Error('读取失败'))
    reader.onload = () => {
      const img = new Image()
      img.onerror = () => reject(new Error('解码失败'))
      img.onload = () => {
        const scale = Math.min(1, AVATAR_MAX_SIDE / Math.max(img.width, img.height))
        const width = Math.max(1, Math.round(img.width * scale))
        const height = Math.max(1, Math.round(img.height * scale))
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        // 先铺白底：转 JPEG 时透明区域会变成黑块
        ctx.fillStyle = '#fff'
        ctx.fillRect(0, 0, width, height)
        ctx.drawImage(img, 0, 0, width, height)
        resolve(canvas.toDataURL('image/jpeg', 0.9))
      }
      img.src = reader.result
    }
    reader.readAsDataURL(file)
  })
}

export default {
  name: 'UserCenter',
  components: { PageHero, PayDialog, ResultState, SafeImage },
  data() {
    return {
      GENDERS,
      TABS,
      // 守卫对象要在这里露一次面才进得了模板（import 绑定在模板里看不见）
      phoneGuard,
      idCardGuard,
      loading: false,
      saving: false,
      loaded: false,
      /* 消费记录。和资料分开加载：资料挂了整页换成错误态，消费记录挂了只坏自己那一块 */
      orders: [],
      ordersLoading: false,
      ordersLoaded: false,
      /* 默认落在「全部」：四类订单一眼看全，不用先猜自己在哪个分区 */
      tab: 'all',
      /* 支付中，防止连点把同一条记录提交两次 */
      acting: false,
      /*
       * 倒计时用的「现在」。每秒更新一次，模板拿它去减 row.expireAt。
       * 放在 data 里而不是每行各存一个剩余秒数：一份时钟算所有行，
       * 列表里十条待支付也只跑一个定时器。
       */
      now: Date.now(),
      timer: null,
      /* 超时后自动刷新列表的一次性闸门，防止某一单没被后端取消时每秒重刷 */
      expireRefreshed: false,
      /* 收银台弹窗：payTarget 是待付款的那条记录，金额与单据都由它来 */
      payOpen: false,
      payTarget: null,
      commentOpen: false,
      commentTarget: null,
      commentForm: { score: 5, content: '' },
      commentSaving: false,
      /* 实名认证。已认证或待审核时只读展示，未提交/被驳回时 identityForm 是待提交的表单 */
      identityLoading: true,
      identity: { verified: false, realName: '', idCardMasked: '', verifyTime: '', auditStatus: '', auditReply: '' },
      identityForm: { realName: '', idCard: '' },
      identitySaving: false,
      /* 证件号与后端 IdCardUtils / 下单页同一份规则（见 common/validate.js 的注释） */
      identityRules: {
        realName: [{ validator: isRealName, trigger: 'blur' }],
        idCard: [{ validator: isIdCard, trigger: 'blur' }],
      },
      /*
       * 只放「可改的四列 + 只读展示用的几列」。
       * 接口返回的整车对象里有密码，直接铺进 form 会一路带回请求体，
       * 所以这里是白名单式赋值，不是整体展开。
       */
      form: {
        id: '',
        addTime: '',
        userAccount: '',
        userName: '',
        gender: '',
        contactPhone: '',
        avatar: '',
        status: 0,
      },
      rules: {
        userName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        contactPhone: [{ validator: isMobile, trigger: 'blur' }],
      },
    }
  },
  computed: {
    account() {
      return localStorage.getItem('username') || this.form.userAccount || ''
    },
    visibleOrders() {
      const match = BUCKETS[this.tab]
      return match ? this.orders.filter(match) : []
    },
    emptyText() {
      if (this.tab === 'all') return '还没有任何订单记录'
      if (this.tab === 'reservation') return '还没有餐厅预约记录'
      const item = TABS.find((t) => t.key === this.tab)
      return item ? `暂无${item.label}的记录` : '这里还没有内容'
    },
    /*
     * 认证状态有三档：没提交过（auditStatus 空串）、待审核、已驳回。
     * verified 只认「已通过」，所以这一页要自己把另外两种分开 ——
     * 待审核时给表单等于让人重交一遍，已驳回时不给表单人又没法改。
     */
    pending() {
      return this.identity.auditStatus === '待审核'
    },
    rejected() {
      return this.identity.auditStatus === '已驳回'
    },
    identityTag() {
      if (this.identity.verified) return '已认证'
      if (this.pending) return '审核中'
      if (this.rejected) return '已驳回'
      return '未认证'
    },
  },
  created() {
    /*
     * 餐厅列表页与预约成功页会带 ?tab=reservation 过来，落地就直接停在
     * 「预约」那一页。未知的 key 一律忽略，免得 URL 被手改后页面空着。
     *
     * 这里只「读一次」，之后切页签不再回写 URL —— 别为了地址栏好看去调
     * router.replace：index.vue 的 $route 监听每次路由变化都会把页面滚到
     * #scrollView 顶部，回写会让每点一个页签就跳一下滚动，比地址栏不同步更烦人。
     */
    const wanted = this.$route.query.tab
    if (TABS.some((t) => t.key === wanted)) this.tab = wanted

    this.load()
    this.loadOrders()
    this.loadIdentity()
  },
  mounted() {
    /*
     * 待支付的倒计时每秒走一格。挂 mounted 而不是 created：
     * 定时器要 setTimeout 的句柄，created 阶段实例还没挂载，
     * 而且这时候也没有任何行需要显示倒计时。
     */
    this.timer = setInterval(this.tick, 1000)
  },
  beforeUnmount() {
    // 离开页面必须清掉，否则这个组件已经销毁了，定时器还在改一个不存在的实例
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },
  methods: {
    /*
     * 模板里用到的格式化函数必须挂到实例上。
     * 模块顶层 import 的绑定在 Options API 的模板里取不到（模板走的是
     * 组件代理，只认 data/computed/methods），漏掉这两个会让订单列表那支
     * 渲染时抛 TypeError，Vue 放弃这次 patch，页面就永远停在骨架屏上。
     */
    fmtDateTime,
    fmtMoney,
    normalizeIdCard,
    isRestaurant,

    /* 原生 input[type=file] 的点击必须由用户手势触发，所以这里只负责转发 .click() */
    pickAvatar() {
      this.$refs.avatarInput.click()
    },

    onAvatarPick(e) {
      const input = e.target
      const file = input.files && input.files[0]
      // 先清空 value：连着选同一个文件时，change 不会再触发
      input.value = ''
      if (!file) return

      if (!file.type.startsWith('image/')) {
        this.$message.error('只能选图片文件')
        return
      }
      if (file.size > MAX_AVATAR_FILE) {
        this.$message.error('图片不要超过 5MB，换一张小一点的')
        return
      }

      toAvatarDataUrl(file)
        .then((url) => {
          // 只是填进 form，点「保存资料」才写库 —— 免得选错了没法反悔
          this.form.avatar = url
        })
        .catch(() => {
          this.$message.error('这张图片读不出来，换一张试试')
        })
    },

    clearAvatar() {
      this.form.avatar = ''
    },

    /*
     * 不带 id：后端是从 Token 里的登录态取人的。
     * 也别在这里等 frontUserid —— 它由父级 index.vue 的 getSession 异步写入，
     * 子组件的 created 跑在它前面，硬刷新时会稳定地判成「没登录」。
     */
    load() {
      this.loading = true
      this.$http
        .get('user/profile')
        .then((res) => {
          if (res.data.code !== 0) {
            this.loaded = false
            this.$message.error(res.data.msg || '获取资料失败')
            return
          }
          const data = res.data.data || {}
          this.form = {
            id: data.id || '',
            addTime: data.addTime || '',
            userAccount: data.userAccount || '',
            userName: data.userName || '',
            gender: data.gender || '',
            contactPhone: data.contactPhone || '',
            avatar: data.avatar || '',
            status: data.status || 0,
          }
          this.loaded = true
        })
        .catch(() => {
          this.loaded = false
        })
        .finally(() => {
          this.loading = false
        })
    },

    save() {
      this.$refs.profileForm.validate((valid) => {
        if (!valid) return
        this.saving = true
        /*
         * 三个可空字段统一成空串再发：MyBatis-Plus 的 updateById 会跳过 null 列，
         * 而 el-select 清空后给的是 undefined，直接发出去就是「清不掉」——
         * 页面看着清空了，重新加载又回来了。
         */
        const body = {
          userName: this.form.userName,
          gender: this.form.gender || '',
          contactPhone: this.form.contactPhone || '',
          avatar: this.form.avatar || '',
        }
        this.$http
          .post('user/profile/update', body)
          .then((res) => {
            if (res.data.code !== 0) {
              this.$message.error(res.data.msg || '保存失败')
              return
            }
            /*
             * 顶栏的姓名、下单页预填的姓名与手机号读的都是 localStorage.sessionForm，
             * 不刷新这里的话，改完得再刷一次页面才生效，看起来像没保存成功。
             */
            const cached = JSON.parse(localStorage.getItem('sessionForm') || '{}')
            localStorage.setItem('sessionForm', JSON.stringify({ ...cached, ...body }))
            localStorage.setItem('frontDisplayName', this.form.userName)
            this.$message.success('资料已保存')
          })
          .finally(() => {
            this.saving = false
          })
      })
    },

    /* ---------------- 消费记录 ---------------- */

    /*
     * 走 api/consumption 而不是 this.$http：这份列表要能被 mock 接管，
     * 否则默认模式（config.useMock = true）下「下单 → 支付 → 个人中心」的
     * 最后一步会显示空列表 —— 订单其实已经写进 localStorage 了。
     *
     * 不带账号参数：后端从 Token 里取（和 user/profile 一样）。
     * 失败不弹 $message —— 进这一页时资料接口已经弹过一次了，
     * 再弹一条重复的红字没用；这一块自己有错误态和重试按钮。
     */
    loadOrders() {
      this.ordersLoading = true
      listConsumption()
        .then((res) => {
          if (res.data.code !== 0) {
            this.ordersLoaded = false
            return
          }
          this.orders = res.data.data || []
          this.ordersLoaded = true
          // 手动刷过一轮，自动超时刷新的闸门重新放开（见 tick）
          this.expireRefreshed = false
        })
        .catch(() => {
          this.ordersLoaded = false
        })
        .finally(() => {
          this.ordersLoading = false
        })
    },

    /*
     * 卡片右上角的状态。
     * 餐厅走的是「审核」而不是「付没付 / 评没评」，所以单独分支到 auditStatus。
     * 已取消要排在「未支付」前面判：它同样没付过款，顺序反了会被显示成「待支付」。
     */
    stateText(row) {
      if (isRestaurant(row)) return row.auditStatus || '待审核'
      if (row.cancelled) return '已取消'
      if (!row.paid) return '待支付'
      return row.commented ? '已评价' : '待评价'
    },

    stateClass(row) {
      if (isRestaurant(row)) {
        if (row.auditStatus === '已通过') return 'tag--teal'
        if (row.auditStatus === '已驳回') return 'tag--price'
        return ''
      }
      // 已取消用 tag 的默认灰底，不再引入一个只此一处的修饰类
      if (row.cancelled) return ''
      if (!row.paid) return 'tag--price'
      return row.commented ? 'tag--teal' : ''
    },

    /* ---------------- 待支付倒计时 ---------------- */

    /* 到点没到点，以秒级时钟 this.now 为准；没有 expireAt 的行（餐厅）永远不算超时 */
    expired(row) {
      return Boolean(row.expireAt) && row.expireAt <= this.now
    },

    /* 剩余时间文案，形如「09:58」。不足一分钟也补成「00:xx」，宽度不跳 */
    remainText(row) {
      const total = Math.max(0, Math.ceil((row.expireAt - this.now) / 1000))
      const mm = String(Math.floor(total / 60)).padStart(2, '0')
      const ss = String(total % 60).padStart(2, '0')
      return `${mm}:${ss}`
    },

    tick() {
      this.now = Date.now()
      /*
       * 有单子走到点了，就去后端取一次新状态 —— 后端在这一刻也把它判成超时，
       * 刷新后它会作为「已取消」离开待支付分区。
       *
       * 真到点的那一次之后，expired() 对这条记录恒为 true，若后端因为时钟或
       * 别的原因没把它划走，这个判断会每秒钟成立一次。expireRefreshed 保证
       * 一轮里只自动刷新一次，剩下的留给用户点「刷新」。
       */
      const stale = this.orders.some(
        (row) => !isRestaurant(row) && !row.paid && !row.cancelled && this.expired(row),
      )
      if (stale) this.refreshAfterExpire()
    },

    /*
     * 静默重取列表：不走 loadOrders()，那个会把整块换成骨架屏，
     * 倒计时到点就闪一下白，比不刷新还难受。
     */
    refreshAfterExpire() {
      if (this.expireRefreshed) return
      this.expireRefreshed = true
      listConsumption()
        .then((res) => {
          if (res.data.code === 0) {
            this.orders = res.data.data || []
            this.ordersLoaded = true
          }
        })
        .catch(() => {
          /*
           * 静默失败：网络抖一下不值得弹红字，这一单会停在「已超时」，
           * 用户点「刷新」或「取消订单」都能走到同一条路。
           */
        })
    },

    bucketCount(key) {
      const match = BUCKETS[key]
      return match ? this.orders.filter(match).length : 0
    },

    openPay(row) {
      this.payTarget = row
      this.payOpen = true
    },

    /*
     * 付款。金额与单据都在 payTarget 上，收银台只负责确认一声 ——
     * 这单是不是本人的由服务端按登录态判，请求体里不带账号。
     */
    payOrder() {
      const row = this.payTarget
      if (!row) return

      this.acting = true
      payConsumption({ source: row.source, id: row.id })
        .then((res) => {
          if (res.data.code !== 0) {
            this.$message.error(res.data.msg || '支付失败')
            return
          }
          this.payOpen = false
          // 付完这条就从「待支付」掉出去，跟着跳到「待评价」，否则看起来像凭空少了一条
          this.$message.success('支付成功，已移到「待评价」')
          this.tab = 'pendingComment'
          this.loadOrders()
        })
        .finally(() => {
          this.acting = false
        })
    },

    /*
     * 取消订单。不可逆，所以先确认一次 —— 同一张单要重新买，得回景点/酒店/线路
     * 再走一遍下单。
     */
    cancelOrder(row) {
      this.$confirm('取消后这张订单就作废了，需要重新下单。确定取消吗？', '取消订单', {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning',
      })
        .then(() => {
          this.acting = true
          cancelConsumption({ source: row.source, id: row.id })
            .then((res) => {
              if (res.data.code !== 0) {
                this.$message.error(res.data.msg || '取消失败')
                // 超时被后端先取消了之类：本地那份已经过时，取一次新的
                this.loadOrders()
                return
              }
              // 与付款后跳「待评价」对称：让用户看见这单挪去了哪里
              this.$message.success('订单已取消')
              this.tab = 'cancelled'
              this.loadOrders()
            })
            .finally(() => {
              this.acting = false
            })
        })
        .catch(() => {
          // 点了「再想想」或按 Esc，什么都不做
        })
    },

    /* ---------------- 实名认证 ---------------- */

    /*
     * 拉认证状态。失败按「未认证」处理：让用户看到并填写认证表单，
     * 真正的判定在提交时（和下单一样由服务端说了算）。
     */
    loadIdentity() {
      this.identityLoading = true
      getIdentity()
        .then((res) => {
          const data = res.data.code === 0 ? res.data.data : null
          this.identity = {
            verified: Boolean(data && data.verified),
            realName: (data && data.realName) || '',
            idCardMasked: (data && data.idCardMasked) || '',
            verifyTime: (data && data.verifyTime) || '',
            auditStatus: (data && data.auditStatus) || '',
            auditReply: (data && data.auditReply) || '',
          }
        })
        .catch(() => {
          this.identity = { verified: false, realName: '', idCardMasked: '', verifyTime: '', auditStatus: '', auditReply: '' }
        })
        .finally(() => {
          this.identityLoading = false
        })
    },

    submitIdentity() {
      this.$refs.identityFormRef.validate((valid) => {
        if (!valid) return
        this.identitySaving = true
        saveIdentity({
          realName: String(this.identityForm.realName || '').trim(),
          idCard: normalizeIdCard(this.identityForm.idCard),
        })
          .then((res) => {
            if (res.data.code !== 0) {
              this.$message.error(res.data.msg || '实名认证失败，请核对姓名与身份证号')
              return
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
            this.identityForm = { realName: '', idCard: '' }
            /*
             * 提交成功但没通过 = 进了待审核队列（服务端要人工审），不是失败。
             * 这里要分开说：报「认证未通过」会让用户以为是自己填错了。
             */
            if (this.identity.verified) {
              this.$message.success('实名认证已完成，购票时不用再填')
            } else if (this.pending) {
              this.$message.success('实名认证已提交，管理员审核通过后即可购票')
            } else {
              this.$message.warning('认证已提交，等待审核结果')
            }
          })
          .finally(() => {
            this.identitySaving = false
          })
      })
    },

    openComment(row) {
      this.commentTarget = row
      this.commentForm = { score: 5, content: '' }
      this.commentOpen = true
    },

    submitComment() {
      const row = this.commentTarget
      if (!row) return
      if (!this.commentForm.content.trim()) {
        this.$message.error('写两句再提交吧')
        return
      }

      this.commentSaving = true
      commentConsumption({
        source: row.source,
        id: row.id,
        // 线路评论表没有评分列，报团不带 score，后端也会忽略
        score: row.source === 'group' ? null : this.commentForm.score,
        content: this.commentForm.content,
      })
        .then((res) => {
          if (res.data.code !== 0) {
            this.$message.error(res.data.msg || '评价提交失败')
            return
          }
          this.commentOpen = false
          this.$message.success('评价已提交，在对应详情页的评论区能看到')
          this.tab = 'commented'
          this.loadOrders()
        })
        .finally(() => {
          this.commentSaving = false
        })
    },
  },
}
</script>

<style scoped>
.skel-block {
  height: 320px;
  border-radius: var(--radius-lg);
  background: var(--sand-deep);
}

.center-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

/* 资料卡与实名认证卡在左栏竖着排；右栏留给账号信息那两张说明卡 */
.center-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.center-card {
  padding: 24px 26px 22px;
}

.skel-verify {
  height: 96px;
  border-radius: var(--radius-sm);
}

.center-card__head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 18px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

/* 占满头像到右边缘之间的空间：实名卡靠它把「已认证/未认证」那个 tag 顶到最右 */
.center-card__head-text {
  flex: 1;
  min-width: 0;
}

.center-card__avatar {
  position: relative;
  display: block;
  flex: none;
  width: 56px;
  height: 56px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  overflow: hidden;
  background: var(--brand-soft);
  cursor: pointer;
}

/* 悬停/键盘聚焦时才浮出提示，平时不干扰 */
.center-card__avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #fff;
  background: rgba(13, 44, 77, 0.62);
  font-size: 11px;
  line-height: 1.3;
  text-align: center;
  opacity: 0;
  transition: opacity 0.18s ease;
}

.center-card__avatar:hover .center-card__avatar-mask,
.center-card__avatar:focus-visible .center-card__avatar-mask {
  opacity: 1;
}

.center-card__file {
  display: none;
}

.center-card__hint {
  margin-top: 6px;
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.7;
}

.center-card__clear {
  margin-left: 8px;
  padding: 0;
  border: 0;
  color: var(--brand);
  background: none;
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
}

.center-card__clear:hover {
  text-decoration: underline;
}

.center-card__name {
  color: var(--ink);
  font-size: 18px;
  font-weight: 700;
}

.center-card__sub {
  margin-top: 4px;
  color: var(--ink-3);
  font-size: 13px;
}

.center-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.form-tip {
  margin-top: 6px;
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.7;
}

.form-tip--warn {
  color: var(--price);
}

.center-card__actions {
  display: flex;
  gap: 12px;
  padding-top: 6px;
}

.center-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-block {
  padding: 20px 22px;
}

.side-block--note {
  background: var(--sand-deep);
}

.side-block__title {
  margin-bottom: 12px;
  color: var(--ink);
  font-size: 15px;
  font-weight: 600;
}

.side-block__list {
  margin: 0;
  padding-left: 18px;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 2;
}

/* ---------------- 最近消费记录 ---------------- */

.orders {
  margin-top: 20px;
  padding: 22px 26px 24px;
}

.orders__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.orders__title {
  color: var(--ink);
  font-size: 17px;
  font-weight: 700;
}

.orders__desc {
  margin-top: 6px;
  color: var(--ink-3);
  font-size: 13px;
  line-height: 1.7;
}

.orders__refresh {
  flex: none;
  padding: 0;
  border: 0;
  color: var(--brand);
  background: none;
  font-family: inherit;
  font-size: 13px;
  cursor: pointer;
}

.orders__refresh:hover:not(:disabled) {
  text-decoration: underline;
}

.orders__refresh:disabled {
  color: var(--ink-3);
  cursor: default;
}

.orders__tabs {
  display: flex;
  gap: 6px;
  margin: 18px 0 6px;
  border-bottom: 1px solid var(--line);
}

.orders__tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 14px;
  border: 0;
  background: none;
  color: var(--ink-2);
  font-family: inherit;
  font-size: 14px;
  cursor: pointer;
}

.orders__tab:hover {
  color: var(--brand);
}

.orders__tab em {
  padding: 0 6px;
  border-radius: 999px;
  background: var(--sand-deep);
  color: var(--ink-3);
  font-size: 11px;
  font-style: normal;
  font-weight: 600;
}

/* 选中态用下划线而不是实心块：四个分区是同一份列表的四种筛选，不是四个主按钮 */
.orders__tab--on {
  color: var(--brand);
  font-weight: 600;
}

.orders__tab--on::after {
  content: '';
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: -1px;
  height: 2px;
  border-radius: 2px;
  background: var(--brand);
}

.orders__tab--on em {
  background: var(--brand-soft);
  color: var(--brand);
}

.orders__skel {
  height: 150px;
  margin-top: 16px;
}

.orders__list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.order {
  display: grid;
  grid-template-columns: 68px minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px dashed var(--line);
}

.order:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.order__media {
  width: 68px;
  height: 68px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--sand-deep);
}

.order__body {
  min-width: 0;
}

.order__top {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.order__title {
  margin-top: 7px;
  color: var(--ink);
  font-size: 15px;
  font-weight: 600;
}

.order__meta {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 6px;
  color: var(--ink-3);
  font-size: 12.5px;
}

.order__no {
  word-break: break-all;
}

.order__foot {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.order__amount {
  color: var(--price);
  font-size: 18px;
  font-weight: 700;
}

.order__amount small {
  margin-right: 2px;
  font-size: 12px;
  font-weight: 600;
}

/* 餐厅没有金额，用一行灰字代替那个价格数字，高度与价格行对齐 */
.order__amount--note {
  color: var(--ink-3);
  font-size: 14px;
  font-weight: 600;
}

.order__done {
  color: var(--ink-3);
  font-size: 12.5px;
}

/* 预约备注。单独一行、换行不撑破卡片 —— 用户可能写一整句「靠窗、忌口」 */
.order__remark {
  margin-top: 6px;
  color: var(--ink-3);
  font-size: 12.5px;
  line-height: 1.6;
  word-break: break-word;
}

/*
 * 待支付那两个动作 + 倒计时。竖着排是为了和「金额 / 按钮」这一列
 * 其它几种状态（一行文字、或单个按钮）的右对齐基线保持一致。
 */
.order__actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.order__btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 等宽数字：否则 1 和 8 宽度不同，倒计时每秒都在小幅左右抖 */
.order__countdown {
  color: var(--price);
  font-size: 12.5px;
  font-variant-numeric: tabular-nums;
}

.order__countdown--over {
  color: var(--ink-3);
}

.cmt__head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.cmt__no {
  color: var(--ink-3);
  font-size: 12.5px;
  word-break: break-all;
}

@media (max-width: 960px) {
  .center-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 640px) {
  .center-form {
    grid-template-columns: minmax(0, 1fr);
  }

  /* 窄屏下缩略图缩小，金额和按钮移到正文下面占满一行 */
  .order {
    grid-template-columns: 56px minmax(0, 1fr);
  }

  .order__media {
    width: 56px;
    height: 56px;
  }

  .order__foot {
    grid-column: 2;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }
}
</style>
