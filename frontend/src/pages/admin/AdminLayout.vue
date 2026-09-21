<template>
  <div class="admin">
    <aside class="admin__side">
      <div class="admin__brand">
        <img class="admin__logo" src="/pictures/logo.png" alt="智游行" />
        <div class="admin__brand-text">
          <strong>智游行</strong>
          <span>{{ roleName }}</span>
        </div>
      </div>

      <nav class="admin__nav">
        <router-link
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          class="admin__nav-item"
          :class="{ 'is-active': activePath === item.path }"
        >
          <span class="admin__nav-icon" v-html="iconSvg(item.icon)"></span>
          <span class="admin__nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="admin__side-foot">
        <router-link class="admin__back" to="/index/home">返回前台</router-link>
      </div>
    </aside>

    <div class="admin__main">
      <header class="admin__top">
        <div class="admin__crumb">
          <span class="admin__crumb-role">{{ roleName }}</span>
          <h1 class="admin__crumb-title">{{ pageTitle }}</h1>
        </div>

        <div class="admin__user">
          <span class="admin__avatar">
            <safe-image variant="avatar" :label="displayName" :seed="account" />
          </span>
          <span class="admin__who">
            <strong>{{ displayName }}</strong>
            <small>{{ account }}</small>
          </span>
          <button class="admin__logout" type="button" @click="logout">退出登录</button>
        </div>
      </header>

      <main class="admin__body">
        <router-view :key="$route.fullPath" />
      </main>
    </div>
  </div>
</template>

<script>
import { menuOf } from '@/config/adminMenu'
import { RESOURCES } from '@/config/adminResources'
import { adminAccount, adminPrefix, adminRoleName, adminTable, clearAdminSession } from '@/common/admin'

const ICONS = {
  home: '<path d="M3.5 10.5 12 4l8.5 6.5"/><path d="M5.5 9.5V20h13V9.5"/><path d="M10 20v-5.5h4V20"/>',
  photo: '<rect x="3.5" y="5" width="17" height="14" rx="2.2"/><circle cx="9" cy="10" r="1.6"/><path d="m4.5 17.5 4.5-4.3 3.3 3 2.7-2.4 4.5 4.2"/>',
  tag: '<path d="M4 11V5.5A1.5 1.5 0 0 1 5.5 4H11l8.5 8.5-7 7z"/><circle cx="8" cy="8" r="1.4"/>',
  office: '<rect x="4.5" y="3.5" width="15" height="17" rx="2"/><path d="M9 8h2M13 8h2M9 12h2M13 12h2M10 20.5v-4h4v4"/>',
  food: '<path d="M7 3.5v7a2.5 2.5 0 0 0 5 0v-7"/><path d="M9.5 10.5v10"/><path d="M16.5 3.5c1.6 0 2.5 1.4 2.5 4s-.9 3.6-2.5 3.6z"/><path d="M16.5 11.1v9.4"/>',
  ticket: '<path d="M4 8.5A2.5 2.5 0 0 1 6.5 6h11A2.5 2.5 0 0 1 20 8.5v1a2 2 0 0 0 0 4v1a2.5 2.5 0 0 1-2.5 2.5h-11A2.5 2.5 0 0 1 4 14.5v-1a2 2 0 0 0 0-4z"/><path d="M12 9v6"/>',
  chat: '<path d="M20 12.5c0 3.6-3.6 6.5-8 6.5a9.6 9.6 0 0 1-2.6-.35L5 20.5l1-3.1A6.4 6.4 0 0 1 4 12.5C4 8.9 7.6 6 12 6s8 2.9 8 6.5z"/>',
  route: '<circle cx="6" cy="6" r="2.5"/><circle cx="18" cy="18" r="2.5"/><path d="M8.5 6h5A3.5 3.5 0 0 1 17 9.5v0A3.5 3.5 0 0 1 13.5 13h-3A3.5 3.5 0 0 0 7 16.5v0A3.5 3.5 0 0 0 10.5 20h5"/>',
  book: '<path d="M4.5 5.5A2 2 0 0 1 6.5 3.5H19v14H6.5a2 2 0 0 0-2 2z"/><path d="M4.5 19.5a2 2 0 0 1 2-2H19v3H6.5a2 2 0 0 1-2-1z"/>',
  user: '<circle cx="12" cy="8" r="3.6"/><path d="M4.5 20a7.5 7.5 0 0 1 15 0"/>',
}

export default {
  data() {
    return {
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
    menus() {
      return menuOf(this.tableName)
    },
    activePath() {
      const path = this.$route.path
      // 资源的详情/编辑走同一页，只换 query，所以按前缀匹配菜单项
      const hit = this.menus.find((item) => path === item.path || path.startsWith(item.path + '/'))
      return hit ? hit.path : path
    },
    pageTitle() {
      if (this.$route.path === '/admin/home') return '工作台'
      if (this.$route.path === '/admin/profile') return '个人资料'
      if (this.$route.path === '/admin/consult') return '咨询消息'
      const key = this.$route.params.resource
      return (key && RESOURCES[key] && RESOURCES[key].title) || '管理端'
    },
    displayName() {
      // username 只有管理员（users 表）会返回 —— 那张表没有单独的姓名字段
      return this.profile.staffName || this.profile.guideName || this.profile.userName
        || this.profile.username || this.account
    },
  },
  created() {
    this.loadProfile()
  },
  methods: {
    iconSvg(name) {
      const body = ICONS[name] || ICONS.tag
      return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${body}</svg>`
    },
    loadProfile() {
      /*
       * 顶栏只在有 session 的时候才显示真名。后端没起 / token 过期都无所谓，
       * 退化成显示账号即可，不要在这里弹错误打扰人。
       */
      this.$http.get(`${this.prefix}/session`, { silent: true })
        .then((res) => {
          if (res.data.code === 0 && res.data.data) {
            this.profile = res.data.data
          }
        })
        .catch(() => {})
    },
    logout() {
      const done = () => {
        clearAdminSession()
        this.$message({ message: '已退出登录', type: 'success', duration: 1200 })
        this.$router.push('/login')
      }
      this.$http.post(`${this.prefix}/logout`, null, { silent: true })
        .then(done)
        .catch(done)
    },
  },
}
</script>

<style scoped>
.admin {
  display: flex;
  min-height: 100vh;
  background: var(--sand);
}

/* ============ 左侧菜单 ============ */
.admin__side {
  display: flex;
  flex-direction: column;
  flex: 0 0 232px;
  background: var(--navy);
  color: #cfe0f2;
}

.admin__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 18px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.09);
}

.admin__logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fff;
  object-fit: contain;
}

.admin__brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.admin__brand-text strong {
  color: #fff;
  font-size: 15px;
  letter-spacing: 0.04em;
}

.admin__brand-text span {
  color: #8fb2d4;
  font-size: 12px;
}

.admin__nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.admin__nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  color: #cfe0f2;
  font-size: 14px;
  text-decoration: none;
  transition: background 0.16s, color 0.16s;
}

.admin__nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.admin__nav-item.is-active {
  background: var(--brand);
  color: #fff;
  font-weight: 600;
}

.admin__nav-icon {
  display: inline-flex;
  flex: none;
}

.admin__nav-label {
  white-space: nowrap;
}

.admin__side-foot {
  padding: 12px 14px 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.09);
}

.admin__back {
  display: block;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #cfe0f2;
  font-size: 13px;
  text-align: center;
  text-decoration: none;
}

.admin__back:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

/* ============ 右侧主区 ============ */
.admin__main {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.admin__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 24px;
  background: var(--card);
  border-bottom: 1px solid var(--line);
}

.admin__crumb-role {
  display: block;
  color: var(--ink-3);
  font-size: 12px;
  letter-spacing: 0.04em;
}

.admin__crumb-title {
  margin: 0;
  color: var(--ink);
  font-size: 19px;
  font-weight: 700;
}

.admin__user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin__avatar {
  display: block;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--brand-soft);
}

.admin__who {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.admin__who strong {
  color: var(--ink);
  font-size: 13px;
}

.admin__who small {
  color: var(--ink-3);
  font-size: 12px;
}

.admin__logout {
  padding: 7px 14px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: #fff;
  color: var(--ink-2);
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.16s, color 0.16s;
}

.admin__logout:hover {
  border-color: var(--brand);
  color: var(--brand);
}

.admin__body {
  flex: 1;
  min-width: 0;
  padding: 20px 24px 32px;
}

@media (max-width: 900px) {
  .admin__side {
    flex-basis: 72px;
  }

  .admin__brand-text,
  .admin__nav-label,
  .admin__back {
    display: none;
  }

  .admin__nav-item {
    justify-content: center;
  }
}
</style>
