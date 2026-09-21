<template>
  <div class="main-containers">
    <div class="body-containers" :style='{"minHeight":"100vh","padding":"200px 0 0","margin":"0","position":"relative","background":"#fff"}'>
      <div class="top-container" :style='{"boxShadow":"none","padding":"0 20px","alignItems":"center","display":"flex","justifyContent":"flex-end","top":"0","left":"0","background":"linear-gradient(180deg, #fbfdff 0%, #eaf3fc 55%, #dbeaf8 100%)","width":"100%","position":"absolute","height":"200px","zIndex":"1002"}'>
        <div :style='{"display":"flex","alignItems":"center","gap":"14px","padding":"0 0 0 40px","top":"0","left":"0","position":"absolute","height":"140px"}'>
          <img :style='{"width":"92px","height":"92px","objectFit":"contain","display":"block"}' src="/pictures/logo.png" alt="智游行">
          <span :style='{"color":"var(--navy)","lineHeight":"1","fontSize":"34px","fontWeight":"bold"}'>「智游行」旅游服务平台</span>
        </div>
        <!-- weather -->
        <div v-if="weather.city" class="weather" :style='{"padding":"0 20px 0 0","alignItems":"center","justifyContent":"center","display":"flex"}'>
          <div :style='{"padding":"0 4px","fontSize":"18px","lineHeight":"1","color":"var(--brand)","fontWeight":"500"}'>{{weather.city}}</div>
          <div :style='{"padding":"0 4px","fontSize":"18px","lineHeight":"1","color":"var(--brand)","fontWeight":"500"}'>{{weather.tem}}°</div>
          <div :style='{"padding":"0 4px","fontSize":"18px","lineHeight":"1","color":"var(--brand)","fontWeight":"500"}'>{{weather.wea}}</div>
          <div :style='{"padding":"0 4px","fontSize":"18px","lineHeight":"1","color":"var(--brand)","fontWeight":"500"}'>{{weather.win}}</div>
          <div :style='{"padding":"0 4px","fontSize":"18px","lineHeight":"1","color":"var(--brand)","fontWeight":"500"}'>{{weather.win_speed}}</div>
        </div>
        <!-- time -->
        <div :style='{"padding":"0 20px 0 0","fontSize":"18px","lineHeight":"1","color":"var(--brand)","fontWeight":"500"}'>{{times}}</div>

        <div v-if="false" :style='{"color":"#666","margin":"0 10px","fontSize":"14px"}'>0753-1234567</div>

        <div id="search" class="search" :style='{"margin":"0 10px","flexWrap":"wrap","background":"#fff","display":"none","height":"auto"}'>
          <div :style='{"margin":"0 10px 0 0"}' class="select">
            <el-select v-model="queryIndex">
              <el-option v-for="(item,index) in queryList" :key="index" :label="item.queryName" :value="index"></el-option>
            </el-select>
          </div>
          <div :style='{"margin":"0 10px 0 0"}' class="input" v-if="queryIndex==0">
            <el-input v-model="remenjingdianjingdianmingcheng" placeholder="景点名称"></el-input>
          </div>
          <div :style='{"margin":"0"}' class="btn" v-if="queryIndex==0">
            <el-button :style='{"border":"0","cursor":"pointer","padding":"0 10px","margin":"0","outline":"none","color":"rgba(255, 255, 255, 1)","borderRadius":"4px","background":"rgba(64, 158, 255, 1)","width":"auto","lineHeight":"44px","fontSize":"14px","height":"44px"}' type="primary" @click="search('attraction')">
              <span class="icon iconfont icon-fangdajing07" :style='{"color":"rgba(255, 255, 255, 1)","margin":"0 4px 0 0","fontSize":"14px"}'></span>
              搜索
            </el-button>
          </div>
        </div>
        <!-- 登录态入口：未登录＝占位头像 +「未登录」，整块可点，点了去登录页；登录后＝首字头像 + 用户名称 -->
        <div class="user-entry" :class="{ 'user-entry--guest': !Token }" :style='{"padding":"0 20px 0 0","display":"flex","alignItems":"center","gap":"10px","order":"5"}' @click="onUserEntry">
          <span class="user-entry__avatar">
            <svg v-if="!Token" class="user-entry__glyph" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" aria-hidden="true">
              <circle cx="12" cy="8" r="3.6" />
              <path d="M4.5 20a7.5 7.5 0 0 1 15 0" />
            </svg>
            <safe-image v-else variant="avatar" :label="displayName || username" :seed="username || 'user'" />
          </span>
          <span class="user-entry__name" :style='{"color":"var(--brand)","fontSize":"18px","lineHeight":"32px","fontWeight":"500"}'>{{ userLabel }}</span>
        </div>
        <div v-if="Token && notAdmin" :style='{"padding":"0 20px 0 0","color":"var(--brand)","fontSize":"18px","lineHeight":"32px","fontWeight":"500","height":"32px","order":"6"}' @click="goMenu('/index/center')">个人中心</div>
        <el-button v-if="Token" @click="logout" :style='{"border":"none","padding":"0 20px 0 0","margin":"0 0px","color":"var(--brand)","borderRadius":"0","background":"none","display":"inline-block","fontSize":"18px","lineHeight":"32px","fontWeight":"500","height":"32px","order":"7"}'>退出</el-button>
      </div>

      <div class="menu-preview" :style='{"padding":"0 0","borderColor":"var(--line)","top":"138px","left":"40px","right":"40px","background":"#fff","borderWidth":"1px","borderRadius":"14px","width":"auto","position":"absolute","borderStyle":"solid","height":"auto","zIndex":"1005","boxShadow":"0 8px 20px rgba(22, 103, 196, 0.1)"}'>
        <el-scrollbar wrap-class="scrollbar-wrapper-horizontal">
          <el-menu class="el-menu-horizontal-demo" :style='{"border":0,"padding":"0","listStyle":"none","margin":"0","background":"none","display":"flex","position":"relative"}' :default-active="activeMenu" :unique-opened="true" mode="horizontal" :router="true" @select="handleSelect">
            <el-menu-item class="home" index="/index/home">
              <span :style='{"padding":"0 6px","margin":"0","color":"inherit","width":"14px","lineHeight":"56px","fontSize":"inherit","height":"56px"}' class="icon iconfont icon-shouye-zhihui"></span>
              <span :style='{"padding":"0 6px","lineHeight":"56px","fontSize":"inherit","color":"inherit","height":"56px"}'>系统首页</span>
            </el-menu-item>
            <el-menu-item class="item" v-for="(menu, index) in menuList" :index="menu.url" :key="index">
              <i :style='{"padding":"0 6px","margin":"0","color":"inherit","width":"14px","lineHeight":"56px","fontSize":"inherit","height":"56px"}' :class="iconArr[index]"></i>
              <span :style='{"padding":"0 6px","lineHeight":"56px","fontSize":"inherit","color":"inherit","height":"56px"}'>{{menu.name}}</span>
            </el-menu-item>
            <el-menu-item class="user" index="/index/center" v-if="Token && notAdmin">
              <span :style='{"padding":"0 10px","margin":"0","color":"inherit","width":"14px","lineHeight":"56px","fontSize":"14px","height":"56px"}' class="icon iconfont icon-shouye-zhihui"></span>
              <span :style='{"padding":"0 10px","lineHeight":"56px","fontSize":"14px","color":"inherit","height":"56px"}'>个人中心</span>
            </el-menu-item>
          </el-menu>
        </el-scrollbar>
      </div>

      <router-view id="scrollView"></router-view>

      <div v-if="bottomContent" class="bottom-preview" :style='{"width":"100%","height":"auto"}'>
        <div :style='{"minHeight":"120px","width":"100%","padding":"20px","overflow":"hidden","background":"#123","height":"auto"}'><div v-html="bottomContent"></div></div>
      </div>

      <div class="float-agent-btn" @click="goAgent" title="AI 助手">
        <span class="float-agent-btn__title">AI</span>
        <span class="float-agent-btn__label">助手</span>
      </div>
    </div>

  </div>
</template>

<script>
import axios from 'axios'
import { apiPrefix } from '@/config/config'

export default {
  data() {
    return {
      queryList:[
        {
          queryName:"景点名称",
        },
      ],
      queryIndex: 0,
      remenjingdianjingdianmingcheng: '',
      activeIndex: '0',
      menuList: [],
      form: {
        ask: '',
        userid: localStorage.getItem('frontUserid')
      },
      headportrait: localStorage.getItem('frontHeadportrait')?localStorage.getItem('frontHeadportrait'):'',
      Token: localStorage.getItem('frontToken'),
      username: localStorage.getItem('username'),
      /*
       * 真实姓名（各表列名不同：user_name / guide_name / staff_name），登录后由 getSession 填上。
       * 缓存在 localStorage 里，刷新页面时先显示上次的值，不用等接口回来。
       */
      displayName: localStorage.getItem('frontDisplayName') || '',
      notAdmin: localStorage.getItem('frontSessionTable')!='"users"',
      timer: '',
      // 时间
      times: '',
      // 天气
      weather: {},
      iconArr: [
        'el-icon-star-off',
        'el-icon-goods',
        'el-icon-warning',
        'el-icon-question',
        'el-icon-info',
        'el-icon-help',
        'el-icon-picture-outline-round',
        'el-icon-camera-solid',
        'el-icon-video-camera-solid',
        'el-icon-video-camera',
        'el-icon-bell',
        'el-icon-s-cooperation',
        'el-icon-s-order',
        'el-icon-s-platform',
        'el-icon-s-operation',
        'el-icon-s-promotion',
        'el-icon-s-release',
        'el-icon-s-ticket',
        'el-icon-s-management',
        'el-icon-s-open',
        'el-icon-s-shop',
        'el-icon-s-marketing',
        'el-icon-s-flag',
        'el-icon-s-comment',
        'el-icon-s-finance',
        'el-icon-s-claim',
        'el-icon-s-opportunity',
        'el-icon-s-data',
        'el-icon-s-check'
      ],
      bottomContent: '',
    }
  },
  created() {
    this.$nextTick(() => {
      // 获取时间
      this.setTimes()
    })
    // 获取天气
    this.getWeather()
    this.menuList = this.$config.indexNav;
    if(localStorage.getItem('frontToken') && localStorage.getItem('frontToken')!=null) {
      this.getSession()
    }
  },
  mounted() {
    this.activeIndex = localStorage.getItem('keyPath') || '0';
  },
  computed: {
    activeMenu() {
      const route = this.$route
      const {
        meta,
        path
      } = route
      // if st path, the sidebar will highlight the path you sete
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    /* 未登录显示「未登录」；登录后显示姓名，session 还没回来就先退回登录账号（手机号） */
    userLabel() {
      return this.Token ? (this.displayName || this.username || '已登录') : '未登录'
    },
  },
  watch: {
    $route(newValue) {
      let that = this
      let url = window.location.href
      let arr = url.split('#')
      for (let x in this.menuList) {
        if (newValue.path == this.menuList[x].url) {
          this.activeIndex = x
        }
      }
      this.Token = localStorage.getItem('frontToken')
      /* 个人中心改完姓名会回写 frontDisplayName，这里重读一次，顶栏不用刷新页面就跟着变 */
      this.displayName = localStorage.getItem('frontDisplayName') || ''
      if(arr[1]!='/index/home'){
        var element = document.getElementById('scrollView');
        var distance = element.offsetTop;
        window.scrollTo( 0, distance )
      }else{
        window.scrollTo( 0, 0 )
      }
    },
    headportrait(){
      this.$forceUpdate()
    },
  },
  methods: {
    search(tablename) {
      if (this.queryIndex == 0 && this.remenjingdianjingdianmingcheng) {
        this.$router.push({path: '/index/' + tablename, query: {indexQueryCondition: this.remenjingdianjingdianmingcheng}});
      }
    },
    // 获取当前时间
    setTimes() {
      setInterval(()=>{
        let d = new Date()
        this.times = d.getFullYear() + '-' + (d.getMonth() + 1) + '-' + d.getDate() + ' ' + d.getHours() + ':' + d.getMinutes() + ':' + d.getSeconds()
      },1000)
    },

    getWeather(){
      axios({
        method: 'get',
        url: 'http://v0.yiketianqi.com/free/day?appid=69475998&appsecret=rldbX1Zl'
      }).then(res => {
        this.weather = res.data || {}
      }).catch(() => {
        this.weather = {}
      })
    },

    async getSession() {
      /*
       * 这个请求只是「刷新一下本地缓存的用户信息」，失败不影响任何功能
       * （下单页会把取不到的手机号留空让用户手填），所以 silent 掉。
       * 网关没起时它必然失败，不静默就是每次进页面弹一条红字。
       *
       * 路径要用接口前缀而不是表名：游客的表名是 yonghu，接口却是 /user/session，
       * 网关只按 /api/user/** 建了路由，直接拼表名会打到不存在的 /api/yonghu/session。
       */
      const tableName = localStorage.getItem('UserTableName')
      if (!tableName) return
      await this.$http
        .get(`${apiPrefix(tableName)}/session`, { emulateJSON: true, silent: true })
        .then(async res => {
          if (res.data.code == 0) {
            localStorage.setItem('sessionForm',JSON.stringify(res.data.data))
            localStorage.setItem('frontUserid', res.data.data.id);
            // 顶栏要显示姓名：五个角色的列名不同，取第一个有值的
            const name = res.data.data.userName || res.data.data.guideName || res.data.data.staffName || ''
            if (name) {
              this.displayName = name
              localStorage.setItem('frontDisplayName', name)
            }
            if(res.data.data.vip) {
              localStorage.setItem('vip', res.data.data.vip);
            }
            if(res.data.data.touxiang) {
              this.headportrait = res.data.data.touxiang
              localStorage.setItem('frontHeadportrait', res.data.data.touxiang);
            } else if(res.data.data.headportrait) {
              this.headportrait = res.data.data.headportrait
              localStorage.setItem('frontHeadportrait', res.data.data.headportrait);
            }
          }
        })
        .catch(() => {});
    },
    handleSelect(keyPath) {
      if (keyPath) {
        localStorage.setItem('keyPath', keyPath)
      }
    },
    toLogin() {
      this.$router.push('/login');
    },
    /* 未登录时点顶栏那头像/「未登录」去登录页；登录后这块不响应点击 */
    onUserEntry() {
      if (!this.Token) {
        this.toLogin()
      }
    },
    logout() {
      localStorage.clear();
      this.$router.push('/index/home');
      this.activeIndex = '0'
      localStorage.setItem('keyPath', this.activeIndex)
      this.Token = ''
      this.$forceUpdate()
      this.$message({
        message: '登出成功',
        type: 'success',
        duration: 1000,
      });
    },
    goBackend() {
      localStorage.setItem('Token', localStorage.getItem('frontToken'));
      localStorage.setItem('role', localStorage.getItem('frontRole'));
      localStorage.setItem('sessionTable', localStorage.getItem('frontSessionTable'));
      localStorage.setItem('headportrait', localStorage.getItem('frontHeadportrait'));
      localStorage.setItem('userid', localStorage.getItem('frontUserid'));
      window.open(`${this.$config.baseUrl}admin/dist/index.html`, "_blank");
    },
    goMenu(path) {
      this.$router.push(path);
    },
    goAgent() {
      this.$router.push('/index/agent');
    },
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.menu-preview {
  .el-scrollbar {
    height: 100%;

    & :deep(.scrollbar-wrapper-vertical) {
      overflow-x: hidden;
    }

    & :deep(.scrollbar-wrapper-horizontal) {
      overflow-y: hidden;

      .el-scrollbar__view {
        white-space: nowrap;
      }
    }
  }
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.home {
  cursor: pointer;
  border: 0;
  padding: 0 12px;
  margin: 0 12px;
  color: #000;
  white-space: nowrap;
  display: flex;
  font-size: 16px;
  line-height: 60px;
  background: none;
  align-items: center;
  position: relative;
  list-style: none;
  height: 60px;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.home:hover {
  color: #fff;
  background: var(--brand);
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.home.is-active {
  color: #fff;
  background: var(--brand);
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.user {
  cursor: pointer;
  border: 0;
  padding: 0 20px;
  color: #333;
  white-space: nowrap;
  display: none;
  font-size: 14px;
  line-height: 56px;
  background: #fff;
  align-items: center;
  position: relative;
  list-style: none;
  height: 56px;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.user:hover {
  color: #fff;
  background: red;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.user.is-active {
  color: #fff;
  background: blue;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.service {
  cursor: pointer;
  border: 0;
  padding: 0 20px;
  color: #333;
  white-space: nowrap;
  display: none;
  font-size: 14px;
  line-height: 56px;
  background: #fff;
  align-items: center;
  position: relative;
  list-style: none;
  height: 56px;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.service:hover {
  color: #fff;
  background: red;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.service.is-active {
  color: #fff;
  background: blue;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.shop {
  cursor: pointer;
  border: 0;
  padding: 0 20px;
  color: #333;
  white-space: nowrap;
  display: none;
  font-size: 14px;
  line-height: 56px;
  background: #fff;
  align-items: center;
  position: relative;
  list-style: none;
  height: 56px;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.shop:hover {
  color: #fff;
  background: red;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.shop.is-active {
  color: #fff;
  background: blue;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.back {
  cursor: pointer;
  border: 0;
  padding: 0 20px;
  color: #333;
  white-space: nowrap;
  display: none;
  font-size: 14px;
  line-height: 56px;
  background: #fff;
  align-items: center;
  position: relative;
  list-style: none;
  height: 56px;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.back:hover {
  color: #fff;
  background: red;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.back.is-active {
  color: #fff;
  background: blue;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.item {
  cursor: pointer;
  border: 0;
  padding: 0 12px;
  margin: 0 12px;
  color: #000;
  white-space: nowrap;
  display: flex;
  font-size: 16px;
  line-height: 60px;
  background: none;
  align-items: center;
  position: relative;
  list-style: none;
  height: 60px;
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.item:hover {
  color: #fff;
  background: var(--brand);
}

.menu-preview .el-menu-horizontal-demo .el-menu-item.item.is-active {
  color: #fff;
  background: var(--brand);
}

.banner-preview {
  .el-carousel :deep(.el-carousel__indicator button) {
    width: 0;
    height: 0;
    display: none;
  }
}

.banner-preview .el-carousel :deep(.el-carousel__container .el-carousel__arrow--left) {
  width: 36px;
  font-size: 12px;
  height: 36px;
}

.banner-preview .el-carousel :deep(.el-carousel__container .el-carousel__arrow--left:hover) {
  background: red;
}

.banner-preview .el-carousel :deep(.el-carousel__container .el-carousel__arrow--right) {
  width: 36px;
  font-size: 12px;
  height: 36px;
}

.banner-preview .el-carousel :deep(.el-carousel__container .el-carousel__arrow--right:hover) {
  background: red;
}

.banner-preview .el-carousel :deep(.el-carousel__indicators) {
  padding: 0;
  margin: 0;
  z-index: 2;
  position: absolute;
  list-style: none;
}

.banner-preview .el-carousel :deep(.el-carousel__indicators li) {
  padding: 0;
  margin: 0 4px;
  background: #fff;
  display: inline-block;
  width: 12px;
  opacity: 0.4;
  transition: 0.3s;
  height: 12px;
}

.banner-preview .el-carousel :deep(.el-carousel__indicators li:hover) {
  padding: 0;
  margin: 0 4px;
  background: #fff;
  display: inline-block;
  width: 24px;
  opacity: 0.7;
  height: 12px;
}

.banner-preview .el-carousel :deep(.el-carousel__indicators li.is-active) {
  padding: 0;
  margin: 0 4px;
  background: #fff;
  display: inline-block;
  width: 24px;
  opacity: 1;
  height: 12px;
}

.chat-content {
  padding-bottom: 20px;
  width: 100%;
  margin-bottom: 10px;
  max-height: 300px;
  height: 300px;
  overflow-y: scroll;
  border: 1px solid #eeeeee;
  background: #fff;

  .left-content {
    float: left;
    margin-bottom: 10px;
    padding: 10px;
    max-width: 80%;
  }

  .right-content {
    float: right;
    margin-bottom: 10px;
    padding: 10px;
    max-width: 80%;
  }
}

.clear-float {
  clear: both;
}

// -------- search --------
.main-containers .search .select :deep(.el-input__inner) {
  border: 0;
  border-radius: 4px;
  padding: 0 30px 0 10px;
  box-shadow: 0 0 6px rgba(64, 158, 255, .3);
  outline: none;
  color: rgba(64, 158, 255, 1);
  width: 180px;
  font-size: 14px;
  height: 44px;
}

.main-containers .search .input :deep(.el-input__inner) {
  border: 0;
  border-radius: 4px;
  padding: 0 10px;
  box-shadow: 0 0 6px rgba(64, 158, 255, .3);
  outline: none;
  color: rgba(64, 158, 255, 1);
  width: 180px;
  font-size: 14px;
  height: 44px;
}
// -------- search --------

.main-containers .btn-service {
  border: 0;
  padding: 0 20px 0 0;
  margin: 0 0;
  color: var(--brand);
  background: none;
  font-weight: 500;
  width: auto;
  font-size: 18px;
  line-height: 32px;
  height: 32px;
  order: 4;
}

.main-containers .btn-service:hover {
  opacity: 0.7;
}

.main-containers .btn-shop {
  border: 0;
  padding: 0 20px 0 0;
  margin: 0 0px;
  color: var(--brand);
  background: none;
  font-weight: 500;
  width: auto;
  font-size: 18px;
  line-height: 32px;
  height: 32px;
  order: 3;
}

.main-containers .btn-shop:hover {
  opacity: 0.7;
}

// -------- 顶栏登录态入口 --------
.main-containers .user-entry {
  cursor: pointer;
}

.main-containers .user-entry--guest:hover {
  opacity: 0.75;
}

.main-containers .user-entry__avatar {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #fff;
  color: var(--brand);
  font-size: 22px;
  line-height: 1;
  overflow: hidden;
  box-shadow: 0 0 0 2px #fff, 0 2px 8px rgba(22, 103, 196, 0.22);
}

.main-containers .user-entry__glyph {
  width: 26px;
  height: 26px;
}

.main-containers .user-entry__name {
  white-space: nowrap;
}

.float-agent-btn {
  position: fixed;
  bottom: 100px;
  right: 30px;
  width: 64px;
  height: 64px;
  background: var(--brand);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1010;
  color: #fff;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(22, 103, 196, 0.45);
  transition: opacity 0.2s, transform 0.2s;
  user-select: none;
}

.float-agent-btn:hover {
  opacity: 0.85;
  transform: scale(1.08);
}

.float-agent-btn__title {
  font-size: 18px;
  line-height: 1.2;
}

.float-agent-btn__label {
  font-size: 12px;
  line-height: 1.2;
}
</style>
