<template>
  <section class="dash">
    <header class="dash__hero">
      <div class="dash__hero-text">
        <p class="dash__hello">{{ greeting }}，{{ displayName }}</p>
        <h2 class="dash__title">{{ roleName }} · 工作台</h2>
        <p class="dash__desc">{{ intro }}</p>
      </div>
      <span class="dash__hero-badge">{{ account }}</span>
    </header>

    <div v-if="cards.length" class="dash__grid">
      <router-link v-for="card in cards" :key="card.path" class="dash__card" :to="card.path">
        <span class="dash__card-label">{{ card.label }}</span>
        <strong class="dash__card-value">
          <template v-if="card.loading">—</template>
          <template v-else-if="card.error">无法统计</template>
          <template v-else>{{ card.total }}</template>
        </strong>
        <span class="dash__card-foot">{{ card.foot }}</span>
      </router-link>
    </div>

    <div class="dash__tips">
      <h3 class="dash__tips-title">使用提示</h3>
      <ul class="dash__tips-list">
        <li>左侧菜单是本角色可管理的全部数据，点进去即可查询、新增、修改、删除。</li>
        <li>{{ tipForKind }}</li>
        <li>图片路径填的是后端存储用的相对路径（如 upload/xxx.jpg），列表里加载不出来时会自动换成示意插画。</li>
        <li>个人资料里可以改姓名与联系方式，登录账号是唯一键，不可修改。</li>
      </ul>
    </div>
  </section>
</template>

<script>
import { menuOf } from '@/config/adminMenu'
import { RESOURCES } from '@/config/adminResources'
import { adminAccount, adminPrefix, adminRoleName, adminTable } from '@/common/admin'

const INTRO = {
  attraction_staff: '在这里维护景点资料、景点类型，处理门票订单与游客评论。',
  hotel_staff: '在这里维护酒店与客房信息，处理预订订单与住客评论。',
  restaurant_staff: '在这里维护餐厅信息，审核预约、回复评论。',
  daoyou: '在这里维护旅游线路与攻略，跟进报团信息。',
  users: '在这里审核游客的实名认证，以及导游、酒店/景点/餐厅前台的资质材料。',
}

export default {
  data() {
    return {
      cards: [],
      profile: {},
    }
  },
  computed: {
    tableName() {
      return adminTable()
    },
    prefix() {
      return adminPrefix()
    },
    roleName() {
      return adminRoleName()
    },
    account() {
      return adminAccount()
    },
    displayName() {
      // username 只有管理员（users 表）会返回 —— 那张表没有单独的姓名字段
      return this.profile.staffName || this.profile.guideName || this.profile.userName
        || this.profile.username || this.account
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 6) return '夜深了'
      if (hour < 12) return '早上好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    intro() {
      return INTRO[this.tableName] || '在这里管理你的业务数据。'
    },
    tipForKind() {
      const kinds = new Set(
        menuOf(this.tableName)
          .filter((item) => item.kind === 'crud')
          .map((item) => (RESOURCES[item.path.split('/').pop()] || {}).kind),
      )
      if (kinds.has('order')) return '订单列表里可以直接把订单标记为已支付 / 未支付，对应线下的收款确认。'
      if (kinds.has('audit')) return '预约列表里点「通过」或「驳回」即可完成审核，驳回需要填写原因。'
      return '评论列表里点「回复」可以补充回复内容，游客端会展示。'
    },
  },
  created() {
    this.loadProfile()
    this.buildCards()
  },
  methods: {
    loadProfile() {
      this.$http.get(`${this.prefix}/session`, { silent: true })
        .then((res) => {
          if (res.data.code === 0 && res.data.data) this.profile = res.data.data
        })
        .catch(() => {})
    },

    buildCards() {
      const items = menuOf(this.tableName)
        .filter((item) => item.kind === 'crud')
        .map((item) => {
          const key = item.path.split('/').pop()
          const resource = RESOURCES[key] || {}
          return {
            path: item.path,
            label: item.label,
            foot: resource.kind === 'order' ? '订单' : resource.kind === 'audit' ? '待审核与已审核' : resource.kind === 'comment' ? '游客评论' : '条记录',
            key,
            // 页面键与接口资源名可能不同（四类资质共用一个 qualification 接口），
            // 计数必须按资源名 + 固定条件走，否则四个卡片会数出同一个总数
            endpoint: resource.endpoint || key,
            fixedQuery: resource.fixedQuery || {},
            loading: true,
            error: false,
            total: 0,
          }
        })
      this.cards = items
      /*
       * 必须遍历 this.cards 而不是上面的 items —— items 是原始对象数组，
       * this.cards 取出来才是响应式代理。loadCount 里改的是 card.loading / card.total，
       * 改原始对象不会触发渲染，卡片就永远停在「—」（计数其实已经拿到了）。
       */
      this.cards.forEach((card) => this.loadCount(card))
    },

    loadCount(card) {
      this.$http.get(`${this.prefix}/${card.endpoint}/page`, { params: { ...card.fixedQuery, page: 1, limit: 1 }, silent: true })
        .then((res) => {
          if (res.data.code !== 0) {
            card.error = true
            return
          }
          card.total = Number((res.data.data && res.data.data.totalCount) || 0)
        })
        .catch(() => {
          card.error = true
        })
        .finally(() => {
          card.loading = false
        })
    },
  },
}
</script>

<style scoped>
.dash {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.dash__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 26px;
  border-radius: var(--radius-lg);
  background: linear-gradient(120deg, var(--brand-dark), var(--brand) 58%, var(--teal));
  color: #fff;
  box-shadow: var(--shadow);
}

.dash__hello {
  margin: 0 0 4px;
  font-size: 13px;
  opacity: 0.85;
}

.dash__title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
}

.dash__desc {
  margin: 0;
  max-width: 620px;
  font-size: 14px;
  line-height: 1.7;
  opacity: 0.9;
}

.dash__hero-badge {
  flex: none;
  padding: 7px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 13px;
  letter-spacing: 0.04em;
}

.dash__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
}

.dash__card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 18px 20px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--card);
  text-decoration: none;
  transition: border-color 0.16s, box-shadow 0.16s, transform 0.16s;
}

.dash__card:hover {
  border-color: var(--brand);
  box-shadow: var(--shadow);
  transform: translateY(-2px);
}

.dash__card-label {
  color: var(--ink-2);
  font-size: 14px;
}

.dash__card-value {
  color: var(--brand-dark);
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
}

.dash__card-foot {
  color: var(--ink-3);
  font-size: 12px;
}

.dash__tips {
  padding: 18px 22px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--sand-deep);
}

.dash__tips-title {
  margin: 0 0 10px;
  color: var(--ink);
  font-size: 15px;
  font-weight: 600;
}

.dash__tips-list {
  margin: 0;
  padding-left: 18px;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 2;
}

@media (max-width: 720px) {
  .dash__hero {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
