<template>
  <div class="page">
    <div v-if="loading" class="container"><div class="skeleton skel-block"></div></div>

    <div v-else-if="!item" class="container">
      <ResultState
        status="error"
        :text="error || '攻略不存在'"
        hint="这条攻略可能已经被作者删掉了，或者接口没有响应。"
        @retry="fetchDetail"
      >
        <button class="btn btn-ghost" @click="$router.push('/index/travel_guide')">返回攻略列表</button>
      </ResultState>
    </div>

    <template v-else>
      <!-- 封面：攻略不再有封面字段，统一走兜底风景图 -->
      <div class="cover">
        <SafeImage :seed="item.id" :alt="item.guideTitle" ratio="21 / 8" />
        <div class="cover__mask"></div>
      </div>

      <div class="container">
        <div class="detail-layout">
          <article class="card article">
            <button class="back" @click="$router.push('/index/travel_guide')">← 返回攻略列表</button>

            <h1 class="article__title">{{ item.guideTitle }}</h1>

            <div class="article__meta">
              <span class="article__avatar">{{ authorName.charAt(0) }}</span>
              <span class="article__author">{{ authorName }}</span>
              <span class="article__dot">·</span>
              <span>{{ fmtDate(item.addTime) }} 发布</span>
            </div>

            <!-- 标签：可以点，跳回列表页按这个标签筛。用 tagId 而不是名字 —— 名字可能重名或被改 -->
            <ul v-if="tagList.length" class="article__tags">
              <li v-for="t in tagList" :key="t.tagId">
                <button class="tag-chip" @click="searchByTag(t)">#{{ t.tagName }}</button>
              </li>
            </ul>

            <!--
              ★ 正文渲染的唯一入口 ★

              段落式纯文本：作者在写作页用空行分段，这里按空行切回来，
              每段一个 <p>，内容走 {{ }} **文本插值**。

              绝对不要换成 v-html —— 正文是陌生游客写的，那等于把 XSS 直接放进页面。
              插值转义是这条链路上唯一的安全边界，换掉它就等于没有边界。
              （白空格用 CSS 的 pre-line 保住段内的软换行，不需要任何 HTML。）
            -->
            <div class="article__body">
              <p v-for="(p, i) in paragraphs" :key="i" class="article__para">{{ p }}</p>
              <p v-if="!paragraphs.length" class="article__empty">这篇攻略还没有正文。</p>
            </div>

            <div v-if="isMine" class="article__ops">
              <button class="btn btn-primary" @click="$router.push(`/index/travel_guide/write/${item.id}`)">编辑这篇</button>
              <button class="btn btn-ghost btn-danger" :disabled="deleting" @click="remove">
                {{ deleting ? '删除中…' : '删除这篇' }}
              </button>
            </div>
          </article>

          <aside class="side-col">
            <div class="side-card">
              <h3 class="side-card__title">作者</h3>
              <div class="author">
                <span class="author__avatar">{{ authorName.charAt(0) }}</span>
                <div>
                  <div class="author__name">{{ authorName }}</div>
                  <div class="author__date">{{ fmtDate(item.addTime) }}</div>
                </div>
              </div>
              <p class="side-card__tip">攻略由游客自己发布，出行前请再核对景区最新公告。</p>
            </div>

            <div v-if="tagList.length" class="side-card">
              <h3 class="side-card__title">这篇的标签</h3>
              <ul class="side-tags">
                <li v-for="t in tagList" :key="t.tagId">
                  <button class="tag-pill" @click="searchByTag(t)">{{ t.tagName }}<em>{{ t.guideCount }}</em></button>
                </li>
              </ul>
            </div>

            <div class="side-card side-card--tip">
              <h3 class="side-card__title">你也写过这一趟？</h3>
              <p class="side-card__tip">把经历写下来，别人能按标签和关键词找到它。</p>
              <button class="btn btn-primary write-btn" @click="goWrite">写我的攻略</button>
            </div>
          </aside>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
/*
 * 攻略详情。@IgnoreAuth 的公开内容，所以路由上也没有 requiresAuth。
 *
 * 正文是本页最要紧的地方：**段落式纯文本 + 文本插值渲染**。
 * 旧的静态页这里是一句 v-html="currentGuide.content"，那在「用户自己写正文」
 * 的前提下是直接的 XSS 漏洞，本轮必须消失 —— 全页搜不到一个 v-html。
 *
 * 标签要显示成「标签名 + 篇数」，所以这里拉一次字典（chips 上那个数字），
 * 用本篇的 tagNames 去字典里对出 id。字典里没有的名字（理论上不该出现，
 * 除非字典被手工清过）就退化成不可点的纯文本，别硬造一个假 id 去筛。
 */
import ResultState from '@/components/ResultState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { getGuide, listGuideTags, deleteGuides } from '@/api/travel_guide'
import { currentUser, ensureLogin } from '@/common/user'
import { fmtDate } from '@/common/format'

export default {
  name: 'TravelGuideDetail',
  components: { ResultState, SafeImage },
  data() {
    return {
      item: null,
      tags: [],
      loading: true,
      error: '',
      deleting: false,
    }
  },
  computed: {
    authorName() {
      if (!this.item) return ''
      // 老数据没有 user_name（本轮之前的行不带这个值），回落显示账号
      return this.item.userName || this.item.userAccount || '匿名游客'
    },
    isMine() {
      if (!this.item) return false
      const me = currentUser()
      return Boolean(me.account) && this.item.userAccount === me.account
    },
    /** 按空行切段。规则与写作页的段数统计逐字一致，作者看到的段数就是最终的段数 */
    paragraphs() {
      if (!this.item) return []
      return String(this.item.guideDetail || '')
        .split(/\n\s*\n/)
        .map((p) => p.trim())
        .filter(Boolean)
    },
    /** 本篇的标签，带上字典里的 id 与篇数 */
    tagList() {
      if (!this.item) return []
      const names = this.item.tagNames || []
      return names.map((name) => {
        const hit = this.tags.find((t) => t.tagName === name)
        return { tagId: hit ? hit.tagId : '', tagName: name, guideCount: hit ? hit.guideCount : '' }
      })
    },
  },
  created() {
    this.fetchDetail()
    this.fetchTags()
  },
  methods: {
    fmtDate,
    async fetchDetail() {
      this.loading = true
      this.error = ''
      try {
        const res = await getGuide(this.$route.params.id)
        if (res.data.code === 0 && res.data.data) {
          this.item = res.data.data
        } else {
          this.item = null
          this.error = res.data.msg || '攻略不存在'
        }
      } catch (e) {
        this.item = null
        this.error = '攻略加载失败'
      } finally {
        this.loading = false
      }
    },
    async fetchTags() {
      try {
        const res = await listGuideTags()
        this.tags = res.data.code === 0 ? res.data.data || [] : []
      } catch (e) {
        // 拿不到字典只是标签不可点，正文照常看
        this.tags = []
      }
    },

    searchByTag(t) {
      if (t.tagId === '' || t.tagId == null) {
        this.$message({ message: '这个标签已经不在标签库里了，没法用它筛选', type: 'info' })
        return
      }
      this.$router.push({ path: '/index/travel_guide', query: { tagId: String(t.tagId) } })
    },

    async remove() {
      try {
        await this.$confirm(`确定删除《${this.item.guideTitle}》吗？删除后不可恢复。`, '删除攻略', {
          confirmButtonText: '删除',
          cancelButtonText: '再想想',
          type: 'warning',
        })
      } catch (e) {
        return // 取消
      }
      this.deleting = true
      try {
        const res = await deleteGuides([this.item.id])
        if (res.data.code === 0) {
          this.$message({ message: '已删除', type: 'success' })
          this.$router.replace('/index/travel_guide')
        } else if (res.data.code === 401) {
          this.$message.error(res.data.msg || '请先登录')
          ensureLogin(this.$router, 'travel_guide')
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      } catch (e) {
        this.$message.error('删除失败，请稍后再试')
      } finally {
        this.deleting = false
      }
    },

    goWrite() {
      ensureLogin(this.$router, 'travel_guide').then((ok) => {
        if (ok) this.$router.push('/index/travel_guide/write')
      })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.page { background: var(--sand); min-height: 100vh; padding-bottom: 60px; }

.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  height: 44px; padding: 0 24px; border: 0; border-radius: 999px;
  font-size: 15px; font-weight: 600; font-family: inherit; cursor: pointer;
  transition: transform .18s ease, box-shadow .18s ease;
}
.btn:disabled { opacity: .6; cursor: not-allowed; transform: none; }
.btn-primary {
  color: #fff; background: linear-gradient(135deg, var(--brand-light), var(--brand));
  box-shadow: 0 8px 20px rgba(22,103,196,.28);
}
.btn-primary:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 12px 26px rgba(22,103,196,.36); }
.btn-ghost { color: var(--ink); background: #fff; box-shadow: inset 0 0 0 1.5px var(--line); }
.btn-ghost:hover:not(:disabled) { color: var(--brand); box-shadow: inset 0 0 0 1.5px var(--brand-light); }
.btn-danger:hover:not(:disabled) { color: #e5484d; box-shadow: inset 0 0 0 1.5px #e5484d; }

/* 封面 */
.cover { position: relative; height: 320px; overflow: hidden; background: var(--navy); }
.cover :deep(.safe-image) { width: 100%; height: 100%; object-fit: cover; }
.cover__mask {
  position: absolute; inset: 0;
  background: linear-gradient(180deg, rgba(13,44,77,.05), rgba(13,44,77,.35));
}

.detail-layout {
  display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 24px;
  align-items: start; margin-top: -48px;
}

/* 正文卡片 */
.article {
  position: relative; z-index: 1;
  padding: 30px 34px 34px;
}
.back {
  border: 0; background: transparent; padding: 0; cursor: pointer;
  color: var(--ink-3); font-size: 13px; font-family: inherit;
  transition: color .15s ease;
}
.back:hover { color: var(--brand); }
.article__title { margin: 14px 0 0; color: var(--ink); font-size: 30px; line-height: 1.4; font-weight: 800; }
.article__meta {
  display: flex; align-items: center; gap: 8px; margin-top: 16px;
  color: var(--ink-3); font-size: 13px;
}
.article__avatar {
  width: 30px; height: 30px; border-radius: 50%;
  background: var(--brand-soft); color: var(--brand);
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
}
.article__author { color: var(--ink); font-weight: 600; }
.article__dot { color: var(--line); }

.article__tags { list-style: none; margin: 18px 0 0; padding: 0; display: flex; flex-wrap: wrap; gap: 8px; }
.tag-chip {
  padding: 5px 14px; border: 1px solid var(--line); border-radius: 999px;
  background: #fff; color: var(--brand-dark); font-size: 13px;
  font-family: inherit; cursor: pointer; transition: all .15s ease;
}
.tag-chip:hover { border-color: var(--brand-light); background: var(--brand-soft); color: var(--brand); }

.article__body { margin-top: 26px; }
.article__para {
  margin: 0 0 18px; color: var(--ink-2);
  font-size: 15.5px; line-height: 2;
  /* 段内保留单个换行（作者一段里分行的清单不会被挤成一行），段间靠 <p> 的 margin */
  white-space: pre-line;
}
.article__empty { color: var(--ink-3); font-size: 14px; }

.article__ops {
  display: flex; gap: 12px; margin-top: 30px; padding-top: 22px;
  border-top: 1px dashed var(--line);
}

/* 侧栏 */
.side-col { display: flex; flex-direction: column; gap: 18px; margin-top: 72px; }
.side-card {
  padding: 20px; background: var(--card);
  border: 1px solid var(--line); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.side-card__title { margin: 0 0 14px; color: var(--ink); font-size: 15px; font-weight: 700; }
.side-card__tip { margin: 10px 0 0; color: var(--ink-3); font-size: 12.5px; line-height: 1.7; }
.side-card--tip { background: var(--brand-soft); border-color: transparent; }
.side-card--tip .side-card__tip { margin-top: 0; }
.author { display: flex; align-items: center; gap: 10px; }
.author__avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: var(--brand-soft); color: var(--brand);
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700;
}
.author__name { color: var(--ink); font-size: 14px; font-weight: 600; }
.author__date { color: var(--ink-3); font-size: 12px; }
.side-tags { list-style: none; margin: 0; padding: 0; display: flex; flex-wrap: wrap; gap: 8px; }
.tag-pill {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 5px 12px; border: 1px solid var(--line); border-radius: 999px;
  background: #fff; color: var(--ink-2); font-size: 13px;
  font-family: inherit; cursor: pointer; transition: all .15s ease;
}
.tag-pill:hover { border-color: var(--brand-light); color: var(--brand); }
.tag-pill em {
  font-style: normal; font-size: 11px;
  color: var(--brand); background: var(--brand-soft);
  padding: 1px 7px; border-radius: 999px;
}
.write-btn { width: 100%; margin-top: 14px; }
.skel-block { height: 320px; margin-top: 32px; border-radius: var(--radius-lg); }

@media (max-width: 1080px) {
  .detail-layout { grid-template-columns: 1fr; }
  .side-col { margin-top: 0; order: 2; }
  .cover { height: 220px; }
  .article { padding: 24px 20px 28px; }
  .article__title { font-size: 24px; }
}
</style>
