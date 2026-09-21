<template>
  <div class="auth">
    <div class="auth__glow auth__glow--a" aria-hidden="true"></div>
    <div class="auth__glow auth__glow--b" aria-hidden="true"></div>

    <el-form ref="loginForm" class="auth__card" :model="loginForm" :rules="rules">
      <header class="auth__head">
        <img class="auth__logo" src="/pictures/logo.png" alt="智游行" />
        <h1 class="auth__title">「智游行」旅游服务平台</h1>
      </header>

      <el-form-item class="auth-field" prop="tableName">
        <label class="auth-field__label">用户类型</label>
        <el-select v-model="loginForm.tableName" size="large" style="width: 100%">
          <el-option v-for="role in roles" :key="role.tableName" :label="role.roleName" :value="role.tableName" />
        </el-select>
      </el-form-item>

      <el-form-item class="auth-field" prop="username">
        <label class="auth-field__label">{{ accountLabel }}</label>
        <!--
          按角色决定要不要过手机号守卫：管理员账号是 users 表里的 admin（字母），
          挂上过滤会连账号都打不出来；其余角色的账号列就是手机号，可以只留数字。
        -->
        <el-input
          v-model="loginForm.username"
          v-bind="isPhoneAccount ? phoneGuard : {}"
          :placeholder="`请输入${accountLabel}`"
          size="large"
        >
          <template #prefix>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
              <circle cx="12" cy="8" r="3.6" />
              <path d="M4.5 20a7.5 7.5 0 0 1 15 0" />
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item class="auth-field" prop="password">
        <label class="auth-field__label">密码</label>
        <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" size="large">
          <template #prefix>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
              <rect x="4.5" y="10.5" width="15" height="9.5" rx="2.2" />
              <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5" />
            </svg>
          </template>
        </el-input>
      </el-form-item>

      <p v-if="isAdmin" class="auth__hint">
        当前选中「{{ roleName }}」，登录后将直接进入管理后台。
      </p>

      <el-button class="auth-submit" @click="loginPost('loginForm')">登录</el-button>

      <div class="auth__foot">
        <span>还没有账号？</span>
        <router-link class="auth__link" :to="{ path: '/register', query: { role: loginForm.tableName } }">立即注册</router-link>
      </div>
    </el-form>
  </div>
</template>

<script>
import { ROLES, GUEST_TABLE, apiPrefix, isAdminTable, landingPath, roleNameOf, roleOf } from '@/config/config'
import { setAdminSession } from '@/common/admin'
import { phoneGuard } from '@/common/inputFilter'
import { isMobile } from '@/common/validate'

export default {
  //数据集合
  data() {
    return {
      baseUrl: this.$config.baseUrl,
      // 守卫对象要在这里露一次面才进得了模板（import 绑定在模板里看不见）
      phoneGuard,
      // 五个角色共用这一页，选中谁就打谁的 /<prefix>/login
      roles: ROLES,
      loginForm: {
        username: '',
        password: '',
        tableName: GUEST_TABLE,
        code: '',
      },
      codes: [{
        num: 1,
        color: '#000',
        rotate: '10deg',
        size: '16px'
      }, {
        num: 2,
        color: '#000',
        rotate: '10deg',
        size: '16px'
      }, {
        num: 3,
        color: '#000',
        rotate: '10deg',
        size: '16px'
      }, {
        num: 4,
        color: '#000',
        rotate: '10deg',
        size: '16px'
      }],
      flag: false,
      verifyCheck2: false,
    }
  },
  components: {
  },
  computed: {
    roleName() {
      return roleNameOf(this.loginForm.tableName)
    },
    /** 除游客外都是管理端角色，登录后落在 /admin/home */
    isAdmin() {
      return isAdminTable(this.loginForm.tableName)
    },
    /** 管理员用的是 users 表里的账号（admin），其余角色注册时填的都是手机号 */
    accountLabel() {
      return this.loginForm.tableName === 'users' ? '管理员账号' : '手机号'
    },
    /** 账号列是不是手机号。只有管理员不是 —— 注意不能拿 isAdmin 判，它是「除游客外都为真」 */
    isPhoneAccount() {
      return this.loginForm.tableName !== 'users'
    },
    /*
     * 规则写成 computed 而不是 data 里的常量：提示文案与手机号格式规则都要跟着
     * 当前选中的角色走（切一下用户类型，账号框的含义就从手机号变成管理员账号了）。
     */
    rules() {
      return {
        tableName: [
          { required: true, message: '请选择用户类型', trigger: 'change' }
        ],
        username: [
          { required: true, message: `请输入${this.accountLabel}`, trigger: 'blur' },
          {
            // 管理员账号是 admin 这种字母账号，只有手机号账号才做格式校验
            validator: (rule, value, callback) => {
              if (!this.isPhoneAccount) return callback()
              return isMobile(rule, value, callback)
            },
            trigger: 'blur',
          }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      }
    },
  },
  created() {
    // 注册页跳回来时带着 ?role=xxx，直接选中同一个角色
    const role = this.$route.query.role
    if (role && roleOf(role)) {
      this.loginForm.tableName = role
    }
  },
  //方法集合
  methods: {
    randomString() {
      var len = 4;
      var chars = [
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
        'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
        '3', '4', '5', '6', '7', '8', '9'
      ]
      var colors = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f']
      var sizes = ['14', '15', '16', '17', '18']

      var output = []
      for (var i = 0; i < len; i++) {
        // 随机验证码
        var key = Math.floor(Math.random() * chars.length)
        this.codes[i].num = chars[key]
        // 随机验证码颜色
        var code = '#'
        for (var j = 0; j < 6; j++) {
          var key = Math.floor(Math.random() * colors.length)
          code += colors[key]
        }
        this.codes[i].color = code
        // 随机验证码方向
        var rotate = Math.floor(Math.random() * 45)
        var plus = Math.floor(Math.random() * 2)
        if (plus == 1) rotate = '-' + rotate
        this.codes[i].rotate = 'rotate(' + rotate + 'deg)'
        // 随机验证码字体大小
        var size = Math.floor(Math.random() * sizes.length)
        this.codes[i].size = sizes[size] + 'px'
      }
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    loginPost(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const tableName = this.loginForm.tableName
          this.$http.get(`${apiPrefix(tableName)}/login`, {params: this.loginForm}).then(res => {
            if (res.data.code === 0) {
              setAdminSession({
                tableName,
                token: res.data.token,
                account: this.loginForm.username,
                roleName: roleNameOf(tableName),
              })
              localStorage.setItem('keyPath', 0)

              /*
               * 登录接口只认「用户类型 + 账号 + 密码」，不校验角色与账号是否匹配
               * （账号列各表名字不同：user_account / guide_no / staff_account，
               *  前缀已经决定查哪张表了）。所以落地页只能按选中的类型来定。
               */
              const redirect = this.$route.query.redirect
              const target = typeof redirect === 'string' && redirect ? redirect : landingPath(tableName)
              this.$router.push(target)
              this.$message({
                message: '登录成功',
                type: 'success',
                duration: 1500,
              });
            } else {
              this.$message.error(res.data.msg);
            }
          });
        } else {
          return false;
        }
      });
    },
  }
}
</script>