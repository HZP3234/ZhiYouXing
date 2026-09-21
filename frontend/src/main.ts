import './assets/main.css'

// element-plus 组件样式 + 中文语言包
import 'element-plus/dist/index.css'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { ElMessage, ElMessageBox } from 'element-plus'

import App from './App.vue'
import router from './router'
import config from './config/config'
import http from './common/http'
import validate from './common/validate'
import FileUpload from './components/FileUpload.vue'
import Editor from './components/Editor.vue'
import SafeImage from './components/SafeImage.vue'
import PageHero from './components/PageHero.vue'
import SectionHead from './components/SectionHead.vue'
import ResultState from './components/ResultState.vue'
import RatingStars from './components/RatingStars.vue'
import ReviewList from './components/ReviewList.vue'

const app = createApp(App)

app.use(router)
app.use(ElementPlus, { locale: zhCn })

/*
 * 代码生成器的页面是按 Vue 2 的全局插件写的（this.$config / this.$http / this.$message…），
 * 这些 API 在 Vue 3 里没有对应物，统一挂到 globalProperties 上补回来。
 */
app.config.globalProperties.$config = config
app.config.globalProperties.$http = http
app.config.globalProperties.$validate = validate
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$msgbox = ElMessageBox
app.config.globalProperties.$alert = ElMessageBox.alert
app.config.globalProperties.$confirm = ElMessageBox.confirm

// 生成器页面里直接写了 <file-upload> 和 <editor>，需要全局注册（Vue 会自动匹配 kebab-case）
app.component('FileUpload', FileUpload)
app.component('Editor', Editor)

/*
 * 景点 / 酒店 / 餐厅九个页面共用的展示组件。
 * 和上面的 FileUpload / Editor 一样全局注册，页面里直接写 <safe-image>、<page-hero>，
 * 不必每个文件都 import 一遍。（页面里显式 import 也仍然有效，就近的优先。）
 */
app.component('SafeImage', SafeImage)
app.component('PageHero', PageHero)
app.component('SectionHead', SectionHead)
app.component('ResultState', ResultState)
app.component('RatingStars', RatingStars)
app.component('ReviewList', ReviewList)

app.mount('#app')
