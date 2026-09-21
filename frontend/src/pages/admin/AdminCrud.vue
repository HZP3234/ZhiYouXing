<template>
  <section class="crud">
    <!-- 搜索 -->
    <el-form class="crud__search" :inline="true" @submit.prevent>
      <el-form-item v-for="item in config.search" :key="item.prop" :label="item.label">
        <!--
          搜索条件按 item.type 挑控件，不写 type 就是普通文本框 —— 现有资源全都是
          这一种，所以那条分支与改前逐字一致，行为不能变。

          数字控件的 min 缺省是「不限」而不是表单里那个 0：搜索填 0 是一个真实的
          边界值（比如查免费景点），不该被当成无意义的条件挡掉。
        -->
        <el-input-number
          v-if="item.type === 'number' || item.type === 'money'"
          v-model="query[item.prop]"
          :controls="false"
          :min="item.min"
          :max="item.max"
          :precision="item.precision !== undefined ? item.precision : (item.type === 'money' ? 2 : 0)"
          :placeholder="`请输入${item.label}`"
          style="width: 190px"
          @keyup.enter="search"
          @change="search"
        />

        <el-date-picker
          v-else-if="item.type === 'date'"
          v-model="query[item.prop]"
          type="date"
          value-format="YYYY-MM-DD"
          :placeholder="`请选择${item.label}`"
          clearable
          style="width: 190px"
          @change="search"
        />

        <el-date-picker
          v-else-if="item.type === 'datetime'"
          v-model="query[item.prop]"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          :placeholder="`请选择${item.label}`"
          clearable
          style="width: 190px"
          @change="search"
        />

        <el-input
          v-else
          v-model="query[item.prop]"
          :placeholder="`请输入${item.label}`"
          clearable
          style="width: 190px"
          @keyup.enter="search"
          @clear="search"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="crud__panel">
      <!-- 工具栏 -->
      <div class="crud__bar">
        <div class="crud__bar-left">
          <el-button v-if="canCreate" type="primary" @click="openCreate">新增{{ config.title }}</el-button>
          <el-button :disabled="selection.length === 0" @click="removeRows(selection)">批量删除</el-button>
        </div>
        <div class="crud__bar-right">
          <span class="crud__count">共 {{ total }} 条</span>
          <el-button text @click="loadList">刷新</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="rows"
        row-key="id"
        border
        stripe
        show-overflow-tooltip
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="46" />

        <el-table-column
          v-for="col in config.columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :min-width="colWidth(col)"
        >
          <template #default="{ row }">
            <div v-if="col.type === 'image'" class="crud__thumb">
              <safe-image :src="row[col.prop]" :seed="row.id || row[col.prop]" />
            </div>

            <el-tag v-else-if="col.type === 'tag'" :type="tagType(col, row[col.prop])" disable-transitions>
              {{ row[col.prop] || '—' }}
            </el-tag>

            <span v-else-if="col.type === 'money'" class="crud__money">{{ money(row[col.prop]) }}</span>

            <span v-else>{{ display(row[col.prop]) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <div class="crud__ops">
              <!-- 订单：一键改支付状态 -->
              <template v-if="config.kind === 'order'">
                <el-button
                  v-if="row[config.payField] !== '已支付'"
                  size="small"
                  type="success"
                  @click="setPay(row, '已支付')"
                >标记已付</el-button>
                <el-button v-else size="small" @click="setPay(row, '未支付')">标记未付</el-button>
              </template>

              <!-- 预约：审核通过 / 驳回（驳回要填原因） -->
              <template v-if="config.kind === 'audit'">
                <el-button
                  v-if="row[config.auditField] !== '已通过'"
                  size="small"
                  type="success"
                  @click="audit(row, '已通过')"
                >通过</el-button>
                <el-button
                  v-if="row[config.auditField] !== '已驳回'"
                  size="small"
                  type="danger"
                  plain
                  @click="reject(row)"
                >驳回</el-button>
              </template>

              <el-button size="small" @click="openEdit(row)">
                {{ config.kind === 'comment' ? '回复' : '编辑' }}
              </el-button>
              <el-button size="small" type="danger" plain @click="removeRows([row])">删除</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <result-state
            status="empty"
            :text="`暂无${config.title}`"
            :hint="config.kind === 'comment' ? '还没有用户发表评论' : '换一个查询条件，或点右上角刷新'"
          />
        </template>
      </el-table>

      <div class="crud__pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.limit"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadList"
          @size-change="onSizeChange"
        />
      </div>
    </div>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      top="6vh"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="editForm" class="crud__form" :model="form" :rules="rules" label-position="top">
        <el-form-item
          v-for="field in config.fields"
          :key="field.prop"
          :label="field.label"
          :prop="field.prop"
          :class="{ 'is-wide': field.type === 'textarea' || field.type === 'images' || field.type === 'days' }"
        >
          <el-input
            v-if="!field.type || field.type === 'text'"
            v-model="form[field.prop]"
            :disabled="field.readonly"
            :placeholder="`请输入${field.label}`"
          />

          <el-input
            v-else-if="field.type === 'textarea'"
            v-model="form[field.prop]"
            type="textarea"
            :rows="3"
            :disabled="field.readonly"
            :placeholder="`请输入${field.label}`"
          />

          <!--
            数字与金额合并成一条分支，差别只在缺省小数位。
            金额原先**没有**分支 —— type:'money' 只写在 columns 里时靠列表那条渲染路径显示，
            一旦有资源把它写进 fields，这个控件就整个不渲染（表单里空一块）。

            min 的缺省必须显式写 0，不能省：element-plus 自己的默认是 Number.MIN_SAFE_INTEGER
            （等于不限），省掉就等于给所有没声明 min 的字段新放开了负数。
          -->
          <el-input-number
            v-else-if="field.type === 'number' || field.type === 'money'"
            v-model="form[field.prop]"
            :controls="false"
            :min="numericMin(field)"
            :max="field.readonly ? undefined : field.max"
            :step="field.step"
            :disabled="field.readonly"
            :precision="numericPrecision(field)"
            style="width: 100%"
          />

          <el-select
            v-else-if="field.type === 'select'"
            v-model="form[field.prop]"
            clearable
            :disabled="field.readonly"
            :placeholder="`请选择${field.label}`"
            style="width: 100%"
          >
            <el-option v-for="opt in optionsOf(field)" :key="opt" :label="opt" :value="opt" />
          </el-select>

          <!-- 手机号 / 身份证只让输数字（身份证末位可 X），守卫见 common/inputFilter.js -->
          <el-input
            v-else-if="field.type === 'phone'"
            v-model="form[field.prop]"
            :disabled="field.readonly"
            v-bind="phoneGuard"
            :placeholder="`请输入${field.label}`"
          />

          <el-input
            v-else-if="field.type === 'idcard'"
            v-model="form[field.prop]"
            :disabled="field.readonly"
            v-bind="idCardGuard"
            :placeholder="`请输入${field.label}（末位可为 X）`"
          />

          <!--
            座机 / 联系电话：允许数字和连字符。餐厅表的 contact_phone 存的是
            「020-81391234」这类座机，用 phone 那个 11 位纯数字口径会把号码截坏。
          -->
          <el-input
            v-else-if="field.type === 'tel'"
            v-model="form[field.prop]"
            :disabled="field.readonly"
            v-bind="telGuard"
            :placeholder="`请输入${field.label}`"
          />

          <el-date-picker
            v-else-if="field.type === 'date'"
            v-model="form[field.prop]"
            type="date"
            :disabled="field.readonly"
            value-format="YYYY-MM-DD"
            :placeholder="`请选择${field.label}`"
            style="width: 100%"
          />

          <el-date-picker
            v-else-if="field.type === 'datetime'"
            v-model="form[field.prop]"
            type="datetime"
            :disabled="field.readonly"
            value-format="YYYY-MM-DD HH:mm:ss"
            :placeholder="`请选择${field.label}`"
            style="width: 100%"
          />

          <el-input
            v-else-if="field.type === 'images'"
            v-model="form[field.prop]"
            :disabled="field.readonly"
            placeholder="多个路径用英文逗号分隔，例如 upload/a.jpg,upload/b.jpg"
          />

          <!--
            逐日行程：一个可增删的数组，每项就是 travel_route_day 的一行。
            「Day N」由序号推出来，不回写 item.day —— 服务端按数组顺序重编号，
            前端跟着改反而会和「行程天数」的推导打架。
          -->
          <div v-else-if="field.type === 'days'" class="crud__days">
            <div v-for="(item, idx) in form[field.prop] || []" :key="idx" class="crud__day">
              <div class="crud__day-head">
                <span class="crud__day-no">Day {{ idx + 1 }}</span>
                <el-button link type="danger" :disabled="field.readonly" @click="removeDay(field.prop, idx)">
                  删除
                </el-button>
              </div>
              <el-input
                v-model="item.title"
                :disabled="field.readonly"
                placeholder="当天标题，例如：抵达西宁 · 宿西宁"
              />
              <el-input
                v-model="item.plan"
                type="textarea"
                :rows="2"
                :disabled="field.readonly"
                placeholder="当天行程安排"
              />
            </div>

            <div v-if="!(form[field.prop] || []).length" class="crud__day-empty">
              还没有安排，点下面「加一天」开始录入。
            </div>

            <el-button :disabled="field.readonly" @click="addDay(field.prop)">+ 加一天</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script>
import { RESOURCES } from '@/config/adminResources'
import { adminPrefix } from '@/common/admin'
import { phoneGuard, idCardGuard, telGuard } from '@/common/inputFilter'

export default {
  data() {
    return {
      rows: [],
      total: 0,
      loading: false,
      saving: false,
      selection: [],
      query: { page: 1, limit: 10 },

      dialogVisible: false,
      form: {},
      isCreate: false,

      optionMap: {},

      /*
       * 手机号 / 身份证输入框的字符守卫（见 common/inputFilter.js）。
       * 必须挂在 data 上模板才拿得到 —— import 进来的绑定不出现在模板作用域里；
       * 而放进 methods 会被 Vue 逐个 .bind()，对象没有 .bind，会直接抛错。
       */
      phoneGuard,
      idCardGuard,
      telGuard,
    }
  },
  computed: {
    key() {
      return this.$route.params.resource
    },
    config() {
      return RESOURCES[this.key] || { title: '未知资源', prefix: '', kind: 'crud', search: [], columns: [], fields: [] }
    },
    /*
     * 请求路径里的资源名。默认就是页面的 :resource，只有「一张表拆成好几页」时才不同
     * ——管理员的四类资质各自一个页面（键唯一），却都打 /users/qualification。
     */
    endpoint() {
      return this.config.endpoint || this.key
    },
    prefix() {
      return adminPrefix()
    },
    /** 审核页面的数据由提交方写入，管理员只改结论，列表上不给「新增」 */
    canCreate() {
      return this.config.kind !== 'comment' && !this.config.noCreate
    },
    dialogTitle() {
      if (this.config.kind === 'comment') return '回复评论'
      return (this.isCreate ? '新增' : '编辑') + this.config.title
    },
    /*
     * 驳回弹窗的文案。资源标题以「审核」结尾的都是内容/资质审核
     * （酒店/景点/餐厅信息审核、线路审核、实名认证审核、四类资质审核），
     * 说「驳回预约」不对 —— 按标题推导成「驳回景点信息」这种。
     * 只有餐厅预约那类 reservation 才真是预约，沿用原来的说法。
     * 想改文案就在资源里写 rejectTitle / rejectPlaceholder。
     */
    rejectTitle() {
      if (this.config.rejectTitle) return this.config.rejectTitle
      const title = this.config.title || ''
      return title.endsWith('审核') ? `驳回${title.slice(0, -2)}` : '驳回预约'
    },
    rejectPlaceholder() {
      if (this.config.rejectPlaceholder) return this.config.rejectPlaceholder
      return this.rejectTitle === '驳回预约'
        ? '例如：该时段已约满，请改约其他时间'
        : '例如：图片与实景不符，请重新上传'
    },
    rules() {
      const out = {}
      this.config.fields.forEach((field) => {
        if (!field.required) return
        /*
         * 触发时机要跟控件走：文本框/文本域是 blur（输完离开才提示），
         * 日期、下拉、数字这些「选完就有值」的控件提交的是 change。
         * 一律写 blur 会出现「日期已经选好了，点保存仍报『请选择出发日期』」——
         * blur 发生在选择之前，选完那一刻根本没重量校验。
         */
        const picked = ['select', 'date', 'datetime', 'number', 'money'].includes(field.type)
        out[field.prop] = [{
          required: true,
          message: `${picked ? '请选择' : '请输入'}${field.label}`,
          trigger: picked ? 'change' : 'blur',
        }]
      })
      return out
    },
  },
  created() {
    this.config.search.forEach((item) => {
      this.query[item.prop] = this.blankSearch(item)
    })
    this.loadOptions()
    this.loadList()
  },
  methods: {
    // ==================== 列表 ====================

    loadList() {
      this.loading = true
      // 固定条件（管理员看某类资质时传的 tableName）不参与「空值不传」的清理，
      // 它一旦丢掉，四个页面就会互相看到别人的数据
      const params = { ...this.config.fixedQuery, ...this.query }
      // 空搜索项不要传，后端 likeOrEq 会把空串拼成 LIKE '%%'
      Object.keys(params).forEach((k) => {
        if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k]
      })

      this.$http.get(`${this.prefix}/${this.endpoint}/page`, { params })
        .then((res) => {
          if (res.data.code !== 0) {
            this.$message.error(res.data.msg || '查询失败')
            return
          }
          const page = res.data.data || {}
          this.rows = page.list || []
          this.total = Number(page.totalCount || 0)
        })
        .finally(() => {
          this.loading = false
        })
    },

    search() {
      this.query.page = 1
      this.loadList()
    },

    resetSearch() {
      this.config.search.forEach((item) => {
        this.query[item.prop] = this.blankSearch(item)
      })
      this.search()
    },

    onSizeChange() {
      this.query.page = 1
      this.loadList()
    },

    onSelectionChange(rows) {
      this.selection = rows
    },

    // ==================== 选项 ====================

    /** 动态下拉（景点类型 / 客房类型）来自对应类型的 /list，只取一次 */
    loadOptions() {
      this.config.fields
        .filter((field) => field.optionsFrom)
        .forEach((field) => {
          const { prefix, key } = field.optionsFrom
          this.$http.get(`/${prefix}/list`, { params: { page: 1, limit: 200 }, silent: true })
            .then((res) => {
              if (res.data.code !== 0) return
              const list = (res.data.data && res.data.data.list) || []
              this.optionMap[field.prop] = [...new Set(list.map((it) => it[key]).filter(Boolean))]
            })
            .catch(() => {})
        })
    },

    optionsOf(field) {
      if (field.options) return field.options
      return this.optionMap[field.prop] || []
    },

    // ==================== 新增 / 编辑 ====================

    openCreate() {
      this.isCreate = true
      const blank = {}
      this.config.fields.forEach((field) => {
        blank[field.prop] = this.blankOf(field)
      })
      this.form = blank
      this.dialogVisible = true
    },

    openEdit(row) {
      this.isCreate = false
      /*
       * 列表可能没返回表单要用的全部字段（列和字段是两份配置），
       * 所以编辑时重新拉一次详情，保证表单是满的。
       */
      this.$http.get(`${this.prefix}/${this.endpoint}/info/${row.id}`)
        .then((res) => {
          if (res.data.code !== 0) {
            this.$message.error(res.data.msg || '获取详情失败')
            return
          }
          const data = { ...(res.data.data || row) }
          this.config.fields.forEach((field) => {
            const missing = data[field.prop] === undefined || data[field.prop] === null
            if (field.type === 'days') {
              // 老线路没有 daily（后端返回 null），编辑器要拿到数组才能 v-for
              data[field.prop] = Array.isArray(data[field.prop]) ? data[field.prop] : []
            } else if (missing) {
              data[field.prop] = this.isNumericField(field) ? undefined : ''
            }
          })
          this.form = data
          this.dialogVisible = true
        })
    },

    /** 新增时各类型的空值：数字/金额留 undefined（el-input-number 的空态），数组给 []，其余空串 */
    blankOf(field) {
      if (field.type === 'days') return []
      return this.isNumericField(field) ? undefined : ''
    },

    /**
     * 搜索项的初始空值。数字控件给 null 而不是空串 —— el-input-number 的 modelValue
     * 只接受 Number/null，给 '' 会报 prop 类型告警。loadList 剔除空条件时同时认 '' 与 null，
     * 所以两者都表示「没填」。
     */
    blankSearch(item) {
      return this.isNumericField(item) ? null : ''
    },

    addDay(prop) {
      if (!Array.isArray(this.form[prop])) this.form[prop] = []
      this.form[prop].push({ title: '', plan: '' })
    },

    removeDay(prop, idx) {
      if (Array.isArray(this.form[prop])) this.form[prop].splice(idx, 1)
    },

    submit() {
      this.$refs.editForm.validate((valid) => {
        if (!valid) return
        this.saving = true
        const url = this.isCreate ? 'save' : 'update'
        this.$http.post(`${this.prefix}/${this.endpoint}/${url}`, this.form)
          .then((res) => {
            if (res.data.code !== 0) {
              this.$message.error(res.data.msg || '保存失败')
              return
            }
            this.$message.success(this.isCreate ? '新增成功' : '保存成功')
            this.dialogVisible = false
            this.loadList()
          })
          .finally(() => {
            this.saving = false
          })
      })
    },

    // ==================== 删除 ====================

    removeRows(rows) {
      const ids = rows.map((row) => row.id).filter((id) => id !== undefined && id !== null)
      if (ids.length === 0) return
      this.$confirm(`确定删除选中的 ${ids.length} 条${this.config.title}吗？删除后无法恢复。`, '删除确认', {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
      }).then(() => {
        this.$http.post(`${this.prefix}/${this.endpoint}/delete`, ids).then((res) => {
          if (res.data.code !== 0) {
            this.$message.error(res.data.msg || '删除失败')
            return
          }
          this.$message.success('删除成功')
          this.selection = []
          // 删掉当前页最后几条时要回退一页，否则会停在空页
          const maxPage = Math.max(1, Math.ceil((this.total - ids.length) / this.query.limit))
          if (this.query.page > maxPage) this.query.page = maxPage
          this.loadList()
        })
      }).catch(() => {})
    },

    // ==================== 订单 / 预约操作 ====================

    setPay(row, value) {
      this.patch(row, { [this.config.payField]: value }, value === '已支付' ? '已标记为已支付' : '已标记为未支付')
    },

    audit(row, value) {
      this.patch(row, { [this.config.auditField]: value, [this.config.replyField]: row[this.config.replyField] || '审核通过' }, '审核结果已保存')
    },

    reject(row) {
      this.$prompt('请填写驳回原因，提交方会看到这条回复', this.rejectTitle, {
        confirmButtonText: '确定驳回',
        cancelButtonText: '取消',
        inputPlaceholder: this.rejectPlaceholder,
        inputValidator: (value) => (value && value.trim() ? true : '驳回原因不能为空'),
      }).then(({ value }) => {
        this.patch(row, { [this.config.auditField]: '已驳回', [this.config.replyField]: value.trim() }, '已驳回')
      }).catch(() => {})
    },

    /**
     * 只改一两个字段。MyBatis-Plus 的 updateById 会跳过 null 字段，
     * 所以这里把 id 和要改的字段一起发过去就够了，不会覆盖其它列。
     *
     * 传输层失败（服务没起来、502、连接被拒）不用在这里再 catch 弹一条：
     * common/http.js 的响应拦截器已经统一 ElMessage.error 过了，重复 catch
     * 只会叠出两条红字，而且把「请求被主动取消」也当成保存失败误报。
     */
    patch(row, changes, successMsg) {
      this.$http.post(`${this.prefix}/${this.endpoint}/update`, { id: row.id, ...changes })
        .then((res) => {
          if (res.data.code !== 0) {
            this.$message.error(res.data.msg || '操作失败')
            return
          }
          Object.assign(row, changes)
          this.$message.success(successMsg)
        })
    },

    // ==================== 展示辅助 ====================

    display(value) {
      if (value === null || value === undefined || value === '') return '—'
      return String(value)
    },

    money(value) {
      if (value === null || value === undefined || value === '') return '—'
      const num = Number(value)
      return Number.isNaN(num) ? String(value) : `¥${num.toFixed(2)}`
    },

    isMoneyField(prop) {
      return /price|amount|fee|cost/i.test(prop)
    },

    /** number / money 都走 el-input-number —— 空态、取值、提交格式三者一致 */
    isNumericField(field) {
      return field.type === 'number' || field.type === 'money'
    },

    /**
     * 小数位：字段声明优先；没写就沿用老的启发式（money 与「名字像金额的」一律两位）。
     * 保留老启发式是为了不动存量里没声明 precision 的字段。
     */
    numericPrecision(field) {
      if (field.precision !== undefined) return field.precision
      return field.type === 'money' || this.isMoneyField(field.prop) ? 2 : 0
    },

    /**
     * 下限：字段声明优先，否则给 0。
     *
     * 为什么不能省这个 0（元素自己写 `:min` 缺省时 element-plus 用的是 Number.MIN_SAFE_INTEGER）：
     * 省掉等于给所有没声明 min 的字段放开了负数。
     *
     * 为什么只读字段反过来要「不给下限」：el-input-number 的 modelValue 侦听器是 immediate 的，
     * 一挂上 min 就会在弹窗打开的一瞬间把越界值夹到边界并 emit，而只读的那些是 *_audit 审核镜像，
     * 值来自提交方 —— 打开详情看一眼就把别人的数据改了。只读镜像按原值显示就好。
     */
    numericMin(field) {
      if (field.min !== undefined) return field.min
      return field.readonly ? undefined : 0
    },

    tagType(col, value) {
      if (!value) return 'info'
      return (col.tagMap && col.tagMap[value]) || 'info'
    },

    colWidth(col) {
      if (col.type === 'image') return 92
      if (col.type === 'tag') return 100
      if (col.type === 'money') return 110
      const text = String(col.label || '')
      // 按表头字数给个下限，中文多一个字大约 16px；min-width 只是下限，宽表仍会平分剩余
      return Math.max(110, text.length * 16 + 40)
    },
  },
}
</script>

<style scoped>
.crud {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.crud__search {
  padding: 14px 16px 0;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.crud__panel {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}

.crud__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--line);
}

.crud__bar-left {
  display: flex;
  gap: 8px;
}

.crud__bar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.crud__count {
  color: var(--ink-3);
  font-size: 13px;
}

.crud__thumb {
  width: 62px;
  height: 40px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--sand-deep);
}

.crud__money {
  color: var(--price);
  font-weight: 600;
}

.crud__ops {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.crud__ops :deep(.el-button + .el-button) {
  margin-left: 0;
}

.crud__pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
  border-top: 1px solid var(--line);
}

.crud__form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.crud__form :deep(.el-form-item.is-wide) {
  grid-column: 1 / -1;
}

.crud__form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.crud__days {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.crud__day {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 12px;
  background: var(--sand, #f7f4ee);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
}

.crud__day-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.crud__day-no {
  font-weight: 600;
  font-size: 13px;
  color: var(--ink-2, #555);
}

.crud__day-empty {
  color: var(--ink-3);
  font-size: 13px;
}

@media (max-width: 720px) {
  .crud__form {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
