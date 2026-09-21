<template>
  <div class="page">
    <PageHero
      compact
      :kicker="isEdit ? '修改攻略' : '写攻略'"
      :title="isEdit ? '修改这篇攻略' : '把你的这一趟写下来'"
      :desc="
        isEdit
          ? '改完保存即刻生效，标签会被整体替换成你这次选的这批。'
          : '标题、正文、标签填好就能发布。正文当文章写，段落之间空一行。'
      "
      scene="fourth"
    />

    <div class="container">
      <div v-if="loading" class="skeleton skel-block"></div>

      <ResultState
        v-else-if="loadError"
        status="error"
        :text="loadError"
        hint="这条攻略不存在，或者接口没有响应。"
        @retry="loadGuide"
      >
        <button class="btn btn-ghost" @click="$router.push('/index/travel_guide')">返回攻略列表</button>
      </ResultState>

      <div v-else class="write-layout">
        <div class="card form-card">
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
            <el-form-item label="攻略标题" prop="guideTitle">
              <el-input
                v-model="form.guideTitle"
                maxlength="100"
                show-word-limit
                clearable
                placeholder="例如：青海湖到茶卡盐湖 3 天自驾，加油与住宿点位都在这里"
              />
            </el-form-item>

            <el-form-item label="标签">
              <el-select
                v-model="form.tagNames"
                multiple
                filterable
                allow-create
                default-first-option
                :multiple-limit="MAX_TAGS"
                placeholder="从下面挑，或直接输入一个新标签后按回车"
                style="width: 100%"
              >
                <el-option v-for="t in tags" :key="t.tagId" :label="t.tagName" :value="t.tagName" />
              </el-select>
              <p class="form-tip">
                最多 {{ MAX_TAGS }} 个。列表框里没有的词直接打上去回车就能新建，别人也能用它筛到你这篇。
              </p>
            </el-form-item>

            <el-form-item label="正文" prop="guideDetail">
              <el-input
                v-model="form.guideDetail"
                type="textarea"
                :rows="18"
                placeholder="像写游记那样写，一段一件事。段落之间空一行，页面上就会分段显示。&#10;&#10;例如：&#10;第一段写为什么去、什么时候去。&#10;&#10;第二段写路上怎么走、哪里加油、哪里住。&#10;&#10;第三段写花了多少钱、有什么坑。"
              />
            </el-form-item>

          </el-form>
        </div>

        <aside class="side-col">
          <div class="side-card">
            <h3 class="side-card__title">封面预览</h3>
            <div class="cover-preview">
              <SafeImage
                :seed="form.guideTitle || 'new-guide'"
                ratio="16 / 10"
                :alt="form.guideTitle"
              />
            </div>
            <p class="side-card__tip">攻略统一使用默认风景图，不再单独填封面地址。</p>
          </div>

          <div class="side-card">
            <h3 class="side-card__title">这篇攻略</h3>
            <div class="kv">
              <div class="kv__row"><span class="kv__k">标题</span><span class="kv__v">{{ form.guideTitle || '未填写' }}</span></div>
              <div class="kv__row"><span class="kv__k">标签</span><span class="kv__v">{{ form.tagNames.length }} 个</span></div>
              <div class="kv__row"><span class="kv__k">正文</span><span class="kv__v">{{ bodyChars }} 字 / {{ paragraphCount }} 段</span></div>
            </div>

            <button class="btn btn-primary submit" :disabled="submitting" @click="submit">
              {{ submitting ? '提交中…' : isEdit ? '保存修改' : '发布攻略' }}
            </button>
            <button class="btn btn-ghost submit submit--ghost" @click="cancel">取消</button>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
/*
 * 写攻略 / 改攻略。新建与编辑共用这一个组件，靠路由上有没有 :id 区分。
 *
 * 硬约定：**作者与发布时间不由前端传。** 请求体里根本没有这两个字段
 * （后端收的是 TravelGuideForm），作者取服务端会话里的账号、昵称现查 user 表。
 * 前端少传一个字段什么都不会坏，这是故意的。
 */
import PageHero from '@/components/PageHero.vue'
import SafeImage from '@/components/SafeImage.vue'
import ResultState from '@/components/ResultState.vue'
import { getGuide, saveGuide, updateGuide, listGuideTags } from '@/api/travel_guide'
import { currentUser, ensureLogin } from '@/common/user'

/* 与后端 TravelGuideTagService.MAX_TAGS_PER_GUIDE 一致，两处都卡 */
const MAX_TAGS = 8

export default {
  name: 'TravelGuideWrite',
  components: { PageHero, SafeImage, ResultState },
  data() {
    return {
      MAX_TAGS,
      tags: [],
      submitting: false,
      loading: false,
      loadError: '',

      form: {
        guideTitle: '',
        guideDetail: '',
        tagNames: [],
      },

      rules: {
        guideTitle: [
          { required: true, message: '请填写攻略标题', trigger: 'blur' },
          { min: 2, max: 100, message: '标题 2 - 100 个字', trigger: 'blur' },
        ],
        guideDetail: [
          { required: true, message: '请填写攻略正文', trigger: 'blur' },
          { min: 10, message: '正文至少写 10 个字，让别人能看懂', trigger: 'blur' },
        ],
      },
    }
  },
  computed: {
    /** 路由上带 id 就是编辑态 */
    editId() {
      const id = this.$route.params.id
      return id ? String(id) : ''
    },
    isEdit() {
      return Boolean(this.editId)
    },
    bodyChars() {
      return String(this.form.guideDetail || '').replace(/\s/g, '').length
    },
    /** 空行分段 —— 与详情页的渲染规则必须完全一致，否则作者看到的段数会和发布后不一样 */
    paragraphCount() {
      return String(this.form.guideDetail || '')
        .split(/\n\s*\n/)
        .filter((p) => p.trim()).length
    },
  },
  created() {
    this.fetchTags()
    if (this.isEdit) this.loadGuide()
  },
  methods: {
    async fetchTags() {
      try {
        const res = await listGuideTags()
        this.tags = res.data.code === 0 ? res.data.data || [] : []
      } catch (e) {
        // 字典拿不到也能写：标签框 allow-create，作者照样能新建
        this.tags = []
      }
    },

    async loadGuide() {
      this.loading = true
      this.loadError = ''
      try {
        const res = await getGuide(this.editId)
        if (res.data.code !== 0 || !res.data.data) {
          this.loadError = res.data.msg || '攻略不存在'
          return
        }
        const data = res.data.data
        /* 只能改自己的。后端 /update 也会拿库里那行的 user_account 再拦一次，
           这里先拦是为了别让作者填完一整篇才被拒。 */
        const me = currentUser()
        if (me.account && data.userAccount !== me.account) {
          this.$message.error('只能修改自己发布的攻略')
          this.$router.replace(`/index/travel_guide/detail/${this.editId}`)
          return
        }
        for (const key of Object.keys(this.form)) {
          if (key === 'tagNames') continue
          // 库里的 NULL 要落成空串，否则 el-input 会显示成 "null"
          this.form[key] = data[key] == null ? '' : String(data[key])
        }
        this.form.tagNames = Array.isArray(data.tagNames) ? data.tagNames.slice(0, MAX_TAGS) : []
      } catch (e) {
        this.loadError = '攻略加载失败'
      } finally {
        this.loading = false
      }
    },

    /**
     * 组装请求体。
     *
     * 标签原样发数组 —— [] 表示「一篇标签都不挂」，后端会整体替换。
     */
    buildBody() {
      const body = {
        guideTitle: this.form.guideTitle.trim(),
        guideDetail: this.form.guideDetail.trim(),
        tagNames: this.form.tagNames,
      }
      if (this.isEdit) body.id = Number(this.editId)
      return body
    },

    async submit() {
      if (this.submitting) return
      try {
        await this.$refs.formRef.validate()
      } catch (e) {
        this.$message({ message: '标题和正文都要填', type: 'warning' })
        return
      }
      // 路由层已经拦过一道（requiresAuth），这里再确认一次是因为会话可能刚好过期
      const ok = await ensureLogin(this.$router, 'travel_guide')
      if (!ok) return

      this.submitting = true
      try {
        const body = this.buildBody()
        const res = this.isEdit ? await updateGuide(body) : await saveGuide(body)
        if (res.data.code === 0) {
          this.$message({ message: this.isEdit ? '已保存' : '发布成功', type: 'success' })
          /* 新建时后端会回新行的 id，直接进详情页 —— 比回列表更容易看到成果，
             也顺带验证了这篇确实入库了。 */
          const id = this.isEdit ? this.editId : res.data.data && res.data.data.id
          this.$router.replace(id ? `/index/travel_guide/detail/${id}` : '/index/travel_guide')
        } else if (res.data.code === 401) {
          // 后端会话过期时回的是 code 401 + HTTP 200，http.js 不认，只能在这里处理
          this.$message.error(res.data.msg || '请先登录')
          ensureLogin(this.$router, 'travel_guide')
        } else {
          this.$message.error(res.data.msg || '提交失败')
        }
      } catch (e) {
        this.$message.error('提交失败，请稍后再试')
      } finally {
        this.submitting = false
      }
    },

    cancel() {
      if (this.isEdit) this.$router.push(`/index/travel_guide/detail/${this.editId}`)
      else this.$router.push('/index/travel_guide')
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
.btn-ghost:hover { color: var(--brand); box-shadow: inset 0 0 0 1.5px var(--brand-light); }

.write-layout {
  display: grid; grid-template-columns: minmax(0, 1fr) 320px; gap: 24px; align-items: start;
  margin-top: 32px;
}

.form-card { padding: 26px 28px; }
.form-card :deep(.el-form-item__label) { color: var(--ink); font-weight: 600; }
.form-tip { margin: 6px 0 0; color: var(--ink-3); font-size: 12.5px; line-height: 1.7; }

/* 正文那个大框：行高放宽一点，写起来才像写文章 */
.form-card :deep(textarea) { line-height: 1.9; font-family: inherit; }

.side-col { display: flex; flex-direction: column; gap: 18px; position: sticky; top: 16px; }
.side-card {
  padding: 20px; background: var(--card);
  border: 1px solid var(--line); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.side-card__title { margin: 0 0 14px; color: var(--ink); font-size: 15px; font-weight: 700; }
.side-card__tip { margin: 10px 0 0; color: var(--ink-3); font-size: 12.5px; line-height: 1.7; }
.cover-preview { border-radius: 12px; overflow: hidden; background: var(--sand-deep); }

.kv { display: flex; flex-direction: column; gap: 10px; margin-bottom: 16px; }
.kv__row { display: flex; justify-content: space-between; gap: 12px; font-size: 13px; }
.kv__k { flex: none; color: var(--ink-3); }
.kv__v {
  color: var(--ink); font-weight: 600; text-align: right; min-width: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.submit { width: 100%; }
.submit--ghost { margin-top: 10px; }

.skel-block { height: 320px; border-radius: var(--radius-lg); }

@media (max-width: 1080px) {
  .write-layout { grid-template-columns: 1fr; }
  .side-col { position: static; order: -1; }
}
@media (max-width: 720px) {
  .form-card { padding: 20px 18px; }
}
</style>
