<template>
  <section class="profile">
    <div class="profile__card">
      <header class="profile__head">
        <span class="profile__avatar">
          <safe-image variant="avatar" :label="displayName" :seed="form.staffAccount || form.guideNo" />
        </span>
        <div class="profile__head-text">
          <h2 class="profile__name">{{ displayName || '未填写姓名' }}</h2>
          <p class="profile__role">{{ roleName }} · {{ account }}</p>
        </div>
      </header>

      <el-form ref="profileForm" class="profile__form" :model="form" :rules="rules" label-position="top">
        <el-form-item
          v-for="field in fields"
          :key="field.prop"
          :label="field.label"
          :prop="field.prop"
          :class="{ 'is-wide': field.type === 'textarea' }"
        >
          <el-input
            v-if="field.type === 'textarea'"
            v-model="form[field.prop]"
            type="textarea"
            :rows="4"
            :disabled="field.readonly"
            :placeholder="field.readonly ? '' : `请输入${field.label}`"
          />
          <!--
            这里有一套**自己的**字段渲染器，不是 AdminCrud 那个通用渲染器。
            所以 PROFILE_FIELDS 里写了 type 还得在这里对应开分支，否则类型等于没写
            （手机号会退化成能打字母的普通文本框）。
          -->
          <el-input
            v-else-if="field.type === 'phone'"
            v-model="form[field.prop]"
            v-bind="phoneGuard"
            :disabled="field.readonly"
            :placeholder="field.readonly ? '' : `请输入${field.label}`"
          />
          <el-input
            v-else
            v-model="form[field.prop]"
            :disabled="field.readonly"
            :placeholder="field.readonly ? '' : `请输入${field.label}`"
          />
        </el-form-item>
      </el-form>

      <div class="profile__actions">
        <el-button type="primary" :loading="saving" @click="save">保存资料</el-button>
        <el-button @click="load">放弃修改</el-button>
      </div>
    </div>

    <!--
      资质认证：四个管理端角色要交，交完由管理员在 /admin/resource/qualification_* 审核。
      管理员与游客没有资质，这块不渲染（后端 /user/qualification/detail 也会拒掉他们）。
    -->
    <div v-if="isSubmitter" class="profile__card">
      <header class="qual__head">
        <h3 class="qual__title">资质认证</h3>
        <el-tag v-if="qualification" :type="qualTagType" disable-transitions>{{ qualification.auditStatus || '待审核' }}</el-tag>
        <el-tag v-else type="info" disable-transitions>未提交</el-tag>
      </header>

      <p class="qual__hint">{{ qualHint }}</p>

      <el-form ref="qualFormEl" class="profile__form" :model="qualForm" :rules="qualRules" label-position="top">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="qualForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactPhone">
          <el-input v-model="qualForm.contactPhone" v-bind="phoneGuard" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="所属单位" prop="orgName">
          <el-input v-model="qualForm.orgName" placeholder="如：所在酒店 / 景点 / 餐厅名称" />
        </el-form-item>
        <el-form-item label="资质证书编号" prop="certNo">
          <el-input v-model="qualForm.certNo" placeholder="请输入资质证书编号" />
        </el-form-item>
        <el-form-item class="is-wide" label="资质说明" prop="qualification">
          <el-input
            v-model="qualForm.qualification"
            type="textarea"
            :rows="3"
            placeholder="从业年限、主要经历等，供管理员审核"
          />
        </el-form-item>
        <el-form-item class="is-wide" label="证明材料图片路径" prop="image">
          <el-input v-model="qualForm.image" placeholder="多个路径用英文逗号分隔，例如 upload/a.jpg,upload/b.jpg" />
        </el-form-item>
      </el-form>

      <div class="profile__actions">
        <el-button type="primary" :loading="qualSaving" @click="submitQualification">{{ qualButtonLabel }}</el-button>
      </div>
    </div>

    <div class="profile__note">
      <h3>关于账号</h3>
      <ul>
        <li>登录账号（{{ accountLabel }}）是系统识别你的唯一凭证，不支持修改。</li>
        <li>密码修改请走登录页的「忘记密码」，本页只维护个人资料。</li>
        <li>姓名会显示在管理端顶栏；留空会退回显示账号。</li>
        <li v-if="isSubmitter">资质认证由管理员审核，材料变更后重新提交会回到「待审核」。</li>
      </ul>
    </div>
  </section>
</template>

<script>
import { PROFILE_FIELDS } from '@/config/adminResources'
import { adminAccount, adminPrefix, adminRoleName, adminTable } from '@/common/admin'
import { apiPrefix, GUEST_TABLE } from '@/config/config'
import { phoneGuard } from '@/common/inputFilter'

/** 有资质要提交的角色。管理员是审的人，游客没有资质 */
const SUBMITTERS = ['daoyou', 'hotel_staff', 'attraction_staff', 'restaurant_staff']

export default {
  data() {
    return {
      form: {},
      saving: false,

      // 模板里只能看到 data / computed / methods 上的东西，import 进来的绑定看不见，
      // 所以守卫对象要在这里露一次面再 v-bind（放 methods 会被 Vue 当成函数 bind 掉）。
      phoneGuard,

      // 资质：null 表示还没提交过（后端 /user/qualification/detail 就是返 data=null）
      qualification: null,
      qualForm: {
        realName: '',
        contactPhone: '',
        orgName: '',
        certNo: '',
        qualification: '',
        image: '',
      },
      qualSaving: false,
      // 与后端 QualificationController.save 的三条必填一致：realName / orgName / certNo
      qualRules: {
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        orgName: [{ required: true, message: '请输入所属单位', trigger: 'blur' }],
        certNo: [{ required: true, message: '请输入资质证书编号', trigger: 'blur' }],
      },
    }
  },
  computed: {
    tableName() {
      return adminTable()
    },
    prefix() {
      return adminPrefix()
    },
    /*
     * 资质接口在 user 服务上（QualificationController 挂在 /user/qualification），
     * 与当前角色自己的前缀（hotel_staff / daoyou …）不是一个 —— 这里要的是游客那个前缀。
     */
    guestPrefix() {
      return apiPrefix(GUEST_TABLE)
    },
    isSubmitter() {
      return SUBMITTERS.includes(this.tableName)
    },
    qualTagType() {
      const status = (this.qualification && this.qualification.auditStatus) || ''
      if (status === '已通过') return 'success'
      if (status === '已驳回') return 'danger'
      return 'warning'
    },
    qualHint() {
      if (!this.qualification) {
        return '请填写并提交资质材料，管理员审核通过后即完成认证。'
      }
      const status = this.qualification.auditStatus || '待审核'
      if (status === '已通过') {
        return '审核已通过。如需变更材料，可直接改下面的内容后重新提交，重新提交会回到待审核。'
      }
      if (status === '已驳回') {
        const reason = this.qualification.auditReply || '未填写原因'
        return `审核未通过：${reason}。请按上述意见修改后重新提交。`
      }
      return '已提交，正在等待管理员审核。审核期间无需重复提交。'
    },
    qualButtonLabel() {
      if (!this.qualification) return '提交资质'
      return this.qualification.auditStatus === '已驳回' ? '重新提交' : '更新并重新提交'
    },
    roleName() {
      return adminRoleName()
    },
    account() {
      return adminAccount()
    },
    fields() {
      return PROFILE_FIELDS[this.tableName] || []
    },
    displayName() {
      // username 只有管理员（users 表）会返回 —— 那张表没有单独的姓名字段
      return this.form.staffName || this.form.guideName || this.form.userName
        || this.form.username || ''
    },
    accountLabel() {
      if (this.tableName === 'daoyou') return '导游工号'
      if (this.tableName === 'users') return '登录账号'
      return '手机号'
    },
    rules() {
      const out = {}
      this.fields.forEach((field) => {
        if (field.required) {
          out[field.prop] = [{ required: true, message: `请输入${field.label}`, trigger: 'blur' }]
        }
      })
      // 联系方式不是必填，所以上面那圈不会给它挂规则 —— 补一条格式校验，与下单页口径一致。
      // isMobile 对空值放过，所以「不填联系方式」照样能保存。
      // 注意：这四个 staff 表的联系方式当前全是手机号（建规则前查过，0 条例外），才用严格 11 位口径；
      // 若将来有员工要留座机，这条会连带挡住整份资料的保存，届时换成更宽的座机口径（telOnly）。
      const phone = this.fields.find((field) => field.prop === 'contactPhone')
      if (phone && !phone.readonly) {
        out.contactPhone = [
          ...(out.contactPhone || []),
          { validator: this.$validate.isMobile, trigger: 'blur' },
        ]
      }
      return out
    },
  },
  created() {
    // 资质表单默认带上资料里的姓名与电话，所以等资料回来了再拉资质
    this.load().then(() => this.loadQualification())
  },
  methods: {
    load() {
      return this.$http.get(`${this.prefix}/session`)
        .then((res) => {
          if (res.data.code !== 0) {
            this.$message.error(res.data.msg || '获取资料失败')
            return
          }
          const data = { ...(res.data.data || {}) }
          this.fields.forEach((field) => {
            if (data[field.prop] === null || data[field.prop] === undefined) data[field.prop] = ''
          })
          this.form = data
        })
    },

    loadQualification() {
      if (!this.isSubmitter) return
      this.$http.get(`${this.guestPrefix}/qualification/detail`, { silent: true })
        .then((res) => {
          if (res.data.code !== 0) return
          const row = res.data.data
          this.qualification = row || null
          if (row) {
            this.qualForm = {
              realName: row.realName || '',
              contactPhone: row.contactPhone || '',
              orgName: row.orgName || '',
              certNo: row.certNo || '',
              qualification: row.qualification || '',
              image: row.image || '',
            }
          } else {
            // 第一次提交：把资料里的姓名/电话带过来，省得再填一遍（都还能改）
            this.qualForm.realName = this.qualForm.realName || this.displayName
            this.qualForm.contactPhone = this.qualForm.contactPhone || this.form.contactPhone || ''
          }
        })
        .catch(() => {})
    },

    submitQualification() {
      this.$refs.qualFormEl.validate((valid) => {
        if (!valid) return
        this.qualSaving = true
        this.$http.post(`${this.guestPrefix}/qualification/save`, this.qualForm)
          .then((res) => {
            if (res.data.code !== 0) {
              this.$message.error(res.data.msg || '提交失败')
              return
            }
            this.$message.success('已提交，等待管理员审核')
            this.loadQualification()
          })
          .finally(() => {
            this.qualSaving = false
          })
      })
    },

    save() {
      this.$refs.profileForm.validate((valid) => {
        if (!valid) return
        this.saving = true
        this.$http.post(`${this.prefix}/update`, this.form)
          .then((res) => {
            if (res.data.code !== 0) {
              this.$message.error(res.data.msg || '保存失败')
              return
            }
            this.$message.success('资料已保存')
          })
          .finally(() => {
            this.saving = false
          })
      })
    },
  },
}
</script>

<style scoped>
.profile {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 860px;
}

.profile__card {
  padding: 24px 26px 20px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--card);
}

.profile__head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 18px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

.profile__avatar {
  display: block;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--brand-soft);
}

.profile__name {
  margin: 0 0 4px;
  color: var(--ink);
  font-size: 18px;
  font-weight: 700;
}

.profile__role {
  margin: 0;
  color: var(--ink-3);
  font-size: 13px;
}

.profile__form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.profile__form :deep(.el-form-item.is-wide) {
  grid-column: 1 / -1;
}

.profile__actions {
  display: flex;
  gap: 10px;
  padding-top: 4px;
}

/* ============ 资质认证 ============ */
.qual__head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 14px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.qual__title {
  margin: 0;
  color: var(--ink);
  font-size: 16px;
  font-weight: 700;
}

.qual__hint {
  margin: 0 0 18px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: var(--sand-deep);
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.7;
}

.profile__note {
  padding: 18px 22px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--sand-deep);
}

.profile__note h3 {
  margin: 0 0 10px;
  color: var(--ink);
  font-size: 15px;
  font-weight: 600;
}

.profile__note ul {
  margin: 0;
  padding-left: 18px;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 2;
}

@media (max-width: 720px) {
  .profile__form {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
