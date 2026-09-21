<template>
  <div class="auth">
    <div class="auth__glow auth__glow--a" aria-hidden="true"></div>
    <div class="auth__glow auth__glow--b" aria-hidden="true"></div>

    <el-form ref="registerForm" class="auth__card" :model="registerForm" :rules="rules">
      <header class="auth__head">
        <img class="auth__logo" src="/pictures/logo.png" alt="智游行" />
        <h1 class="auth__title">注册{{ roleName }}账号</h1>
      </header>

      <el-form-item class="auth-field is-required" prop="tableName">
        <label class="auth-field__label">用户类型</label>
        <el-select v-model="registerForm.tableName" size="large" style="width: 100%">
          <el-option v-for="role in roles" :key="role.tableName" :label="role.roleName" :value="role.tableName" />
        </el-select>
      </el-form-item>

      <el-form-item class="auth-field is-required" prop="phone">
        <label class="auth-field__label">手机号</label>
        <el-input v-model="registerForm.phone" v-bind="phoneGuard" placeholder="请输入手机号" size="large">
          <template #prefix>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
              <circle cx="12" cy="8" r="3.6" />
              <path d="M4.5 20a7.5 7.5 0 0 1 15 0" />
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item class="auth-field is-required" prop="password">
        <label class="auth-field__label">密码</label>
        <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入 3-13 位密码" size="large">
          <template #prefix>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
              <rect x="4.5" y="10.5" width="15" height="9.5" rx="2.2" />
              <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5" />
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <p v-if="isAdmin" class="auth__hint">
        管理端账号注册后即可登录「{{ roleName }}」管理后台；账号列即手机号，姓名可在个人资料里补填。
      </p>

      <el-button class="auth-submit" @click="submitForm('registerForm')">注册</el-button>

      <div class="auth__foot">
        <span>已有账号？</span>
        <router-link class="auth__link" :to="{ path: '/login', query: { role: registerForm.tableName } }">直接登录</router-link>
      </div>
    </el-form>
  </div>
</template>

<script>
import { ROLES, GUEST_TABLE, apiPrefix, isAdminTable, REGISTER_ACCOUNT_FIELD, roleNameOf, roleOf } from '@/config/config'
import { phoneGuard } from '@/common/inputFilter'

export default {
  //数据集合
  data() {
    return {
      // 守卫对象要在这里露一次面才进得了模板（import 绑定在模板里看不见）
      phoneGuard,
      /*
       * 管理员（users）不列在这里：那张表只有预置的 admin 一行，
       * 后端的注册接口也只会把新账号写成「用户」角色（见 UsersController.register），
       * 所以注册页给它一个选项只会让人白填一遍。
       */
      roles: ROLES.filter((role) => !role.noRegister),
      registerForm: {
        tableName: GUEST_TABLE,
        phone: '',
        password: '',
      },
      rules: {
        tableName: [
          { required: true, message: '请选择用户类型', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { validator: this.$validate.isMobile, trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, max: 13, message: '密码长度需在 3 到 13 位之间', trigger: 'blur' }
        ]
      },
    }
  },

  computed: {
    roleName() {
      return roleNameOf(this.registerForm.tableName)
    },
    isAdmin() {
      return isAdminTable(this.registerForm.tableName)
    },
  },

  created() {
    // 从登录页带过来的角色（?role=daoyou），让两个页面的选择保持一致；
    // 只在「本页确实列了这个角色」时采纳，免得 ?role=users 这种手敲的地址
    // 把下拉框设成一个不存在的选项
    const role = this.$route.query.role
    if (role && roleOf(role) && this.roles.some((item) => item.tableName === role)) {
      this.registerForm.tableName = role
    }
  },

  //方法集合
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (!valid) {
          return false
        }

        /*
         * 账号列各表不同名（user_account / guide_no / staff_account），
         * 由 REGISTER_ACCOUNT_FIELD 映射；手机号同时写进 contact_phone。
         * 姓名不采集，后端 register 会补一个占位名（见各 Controller.register）。
         */
        const tableName = this.registerForm.tableName
        const body = {
          [REGISTER_ACCOUNT_FIELD[tableName]]: this.registerForm.phone,
          password: this.registerForm.password,
          contactPhone: this.registerForm.phone,
        }
        const url = apiPrefix(tableName) + '/register'
        this.$http.post(url, body).then(res => {
          if (res.data.code === 0) {
            this.$message({
              message: '注册成功，请登录',
              type: 'success',
              duration: 1500,
              onClose: () => {
                // 把角色带回登录页，省得再选一次
                this.$router.push({ path: '/login', query: { role: tableName } })
              }
            });
          } else {
            this.$message.error(res.data.msg);
          }
        });
      });
    },
  }
}
</script>
