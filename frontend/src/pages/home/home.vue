<template>
  <div class="home">
    <section class="banner" aria-label="平台推荐位">
      <div class="banner-viewport" @mouseenter="pause" @mouseleave="resume">
        <div class="banner-track" :style="{ transform: `translateX(-${current * 100}%)` }">
          <div
            v-for="(b, i) in banners"
            :key="b.title"
            class="banner-slide"
            :aria-hidden="i === current ? 'false' : 'true'"
          >
            <img
              v-if="b.image"
              class="banner-slide__img"
              :src="b.image"
              :alt="b.title"
              @error="b.image = ''"
            />
            <div v-else class="banner-slide__art" v-html="scenes[b.scene]"></div>
            <div class="banner-slide__mask"></div>

            <div class="container banner-slide__copy">
              <div class="banner-slide__kicker">{{ b.kicker }}</div>
              <h2 class="banner-slide__title">{{ b.title }}</h2>
              <p class="banner-slide__desc">{{ b.desc }}</p>
              <button class="btn btn-onbrand" @click="bannerClick(b)">
                {{ b.action }}
                <span class="btn-icon" v-html="icons.arrow"></span>
              </button>
            </div>
          </div>
        </div>

        <button class="banner-nav banner-nav--prev" aria-label="上一张" @click="prev">‹</button>
        <button class="banner-nav banner-nav--next" aria-label="下一张" @click="next">›</button>

        <div class="banner-dots">
          <button
            v-for="(b, i) in banners"
            :key="`dot-${b.title}`"
            class="banner-dot"
            :class="{ 'is-active': i === current }"
            :aria-label="`第 ${i + 1} 张`"
            @click="goTo(i)"
          ></button>
        </div>
      </div>
    </section>

    <!-- ============ 系统板块 ============ -->
    <section class="section">
      <div class="container">
        <header class="section-head">
          <h2 class="section-title">这个平台能帮你做什么</h2>
          <p class="section-desc">
            围绕「去哪玩、住哪儿、吃什么、怎么走」四件事组织内容，另有导游与 AI 助手两条专用通道。
          </p>
        </header>

        <div class="module-grid">
          <button
            v-for="(m, i) in modules"
            :key="m.key"
            class="module-card reveal"
            :style="{ animationDelay: `${i * 70}ms` }"
            @click="go(m.path)"
          >
            <span class="module-card__icon" v-html="icons[m.key]"></span>
            <span class="module-card__name">{{ m.name }}</span>
            <span class="module-card__desc">{{ m.desc }}</span>
            <span class="module-card__go">
              进入
              <span class="module-card__arrow">→</span>
            </span>
          </button>
        </div>
      </div>
    </section>

    <section class="section section--band">
      <div class="container">
        <header class="section-head">
          <h2 class="section-title">点一个省份，看它的城市与热门景点</h2>
          <p class="section-desc">
            看中国地图，点热门城市，走热门景点！
          </p>
        </header>

        <div class="kg">
          <div class="kg__canvas">
            <div ref="chinaMap" class="china-map"></div>
            <div class="kg__hint">滚轮缩放 · 拖拽平移 · 点击省份</div>
          </div>

          <aside class="kg__panel">
            <template v-if="activeInfo">
              <span class="kg__badge">{{ activeProvince }}</span>
              <p class="kg__intro">{{ activeInfo.intro }}</p>

              <h4 class="kg__sub">热门城市 · {{ activeInfo.cities.length }}</h4>
              <ul class="kg__cities">
                <li v-for="c in activeInfo.cities" :key="c.name">
                  <div class="kg__city">
                    <span class="kg__city-name">{{ c.name }}</span>
                    <span class="kg__city-tag">{{ c.tag }}</span>
                  </div>
                  <ul class="kg__spots">
                    <li v-for="s in c.spots" :key="s">{{ s }}</li>
                  </ul>
                </li>
              </ul>

              <button class="btn btn-primary kg__go" @click="go('/index/attraction')">
                去看该省景点列表
              </button>
            </template>

            <div v-else class="kg__empty">
              <strong>点一个省份试试</strong>
              <p>
                地图上每个省份都能点。点开后会在这里列出它的热门城市，
                以及每个城市里值得去的景点——顺着省份一路看下去就行。
              </p>
              <ul class="kg__legend">
                <li><b>{{ provinceCount }}</b> 个省级行政区</li>
                <li><b>{{ cityCount }}</b> 个热门城市</li>
                <li><b>{{ spotCount }}</b> 个热门景点</li>
              </ul>
              <div class="kg__quick">
                <button
                  v-for="p in quickProvinces"
                  :key="p"
                  type="button"
                  @click="selectProvince(p)"
                >
                  {{ p }}
                </button>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </section>

    <footer class="about">
      <div class="container">
        <div class="about-grid">
          <div class="about-brand">
            <div class="about-logo">
              <img src="/pictures/logo.png" alt="智游行" />
              <span>智游行</span>
            </div>
            <p class="about-intro">
              智游行是一个面向全国旅行的信息平台：把景点、酒店、餐厅、线路与攻略收在一处，
              配上导游入驻通道与 AI 行程助手，让行前功课一次做完。
            </p>
          </div>

          <div class="about-col">
            <h3 class="about-col__title">快速入口</h3>
            <ul class="about-links">
              <li v-for="m in modules" :key="m.key">
                <a href="javascript:;" @click="go(m.path)">{{ m.name }}</a>
              </li>
            </ul>
          </div>

          <div class="about-col">
            <h3 class="about-col__title">联系我们</h3>
            <ul class="about-links about-links--plain">
              <li>邮箱：zhiyouxing@zhiyouxing.com</li>
              <li>电话：400-000-0000</li>
              <li>地址：天津市滨海新区</li>
            </ul>
          </div>
        </div>

        <div class="about-foot">
          <span>© {{ year }} 智游行 ZhiYouXing</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import * as echarts from 'echarts/core'
import { GeoComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { PROVINCE_NAMES, PROVINCE_SPOTS } from '@/data/provinceSpots'
// 图标与风景画已抽到公共模块，景点/酒店/餐厅页面共用同一套（含 SVG→data URI 的兜底逻辑）
import { icons, scenes } from '@/common/scenes'

echarts.use([GeoComponent, TooltipComponent, CanvasRenderer])

let chart = null

const resizeMap = () => chart && chart.resize()

export default {
  data() {
    return {
      icons,
      scenes,
      Token: localStorage.getItem('frontToken'),
      keyword: '',
      year: new Date().getFullYear(),
      activeProvince: '',

      current: 0,
      timer: null,
      banners: [
        {
          scene: 'first',
          image: '/pictures/lun_bo_tu_1.png',
          kicker: '「智游行」· 旅游服务平台',
          title: '把一趟旅行的准备，收进一个平台',
          desc: '景点、酒店、餐厅、线路、攻略五个板块集中在一处，不用在十几个网站之间来回翻。',
          action: '从热门景点开始',
          link: '/index/attraction',
        },
        {
          scene: 'second',
          image: '/pictures/lun_bo_tu_2.png',
          kicker: '向导发布 · 游客收藏',
          title: '让走过这条路的人替你排行程',
          desc: '导游在平台上发布成熟线路，起点终点、每日安排都写得清楚，挑一条跟着走就行。',
          action: '浏览旅游线路',
          link: '/index/travel_route',
        },
        {
          scene: 'third',
          image: '/pictures/lun_bo_tu_3.png',
          kicker: '住哪儿 · 吃什么',
          title: '住宿和吃饭，出发前就定下来',
          desc: '酒店房型、价格、实拍图，加上当地餐厅汇总，落地之后不用临时抓瞎。',
          action: '查看酒店信息',
          link: '/index/hotel_info',
        },
        {
          scene: 'fourth',
          image: '/pictures/lun_bo_tu_4.png',
          kicker: '行前功课 · 智能规划',
          title: '功课做在前面，行程交给 AI',
          desc: '目的地玩法与注意事项一次看全；定不下怎么走，让 AI 助手按天数和预算出方案。',
          action: '查阅旅游攻略',
          link: '/index/travel_guide',
        },
      ],

      tags: ['景点', '酒店', '美食', '线路', '攻略', '导游'],
      modules: [
        {
          key: 'attraction',
          name: '热门景点',
          desc: '按热度浏览全国各地景区，支持按名称直接搜索。',
          path: '/index/attraction',
        },
        {
          key: 'hotel',
          name: '酒店信息',
          desc: '查看酒店房型图片与价格，按推荐优先级排序。',
          path: '/index/hotel_info',
        },
        {
          key: 'restaurant',
          name: '美食餐厅',
          desc: '当地餐厅信息汇总，出发前先把吃什么定下来。',
          path: '/index/restaurant',
        },
        {
          key: 'route',
          name: '旅游线路',
          desc: '导游发布的成熟线路，含起点、终点与行程安排。',
          path: '/index/travel_route',
        },
        {
          key: 'guide',
          name: '旅游攻略',
          desc: '目的地玩法与注意事项，把行前功课做扎实。',
          path: '/index/travel_guide',
        },
        {
          key: 'agent',
          name: 'AI 助手',
          desc: '说出目的地、天数与预算，让 AI 帮你把行程排出来。',
          path: '/index/agent',
        },
      ],
      features: [
        {
          icon: 'search',
          title: '搜索直达',
          desc: '在首页按景点名称搜索，直接跳到对应列表，不用一层层点进去。',
        },
        {
          icon: 'users',
          title: '用户与导游双角色',
          desc: '游客注册后收藏行程，导游另有入驻通道，用于发布自己的线路。',
        },
        {
          icon: 'agent',
          title: 'AI 行程助手',
          desc: '助手入口在所有用户端页面常驻，不用翻菜单找入口。',
        },
        {
          icon: 'star',
          title: '收藏与点赞',
          desc: '看到中意的地点先收藏起来，回头还能在个人中心找回。',
        },
      ],
      steps: [
        { title: '注册或登录', desc: '游客注册账号，导游可走注册页的角色通道入驻。' },
        { title: '浏览与搜索', desc: '景点、酒店、餐厅、线路、攻略五个板块逐个看过去。' },
        { title: '收藏后出发', desc: '把心仪内容收藏下来，定不下行程时点右下角找 AI 助手。' },
      ],
    }
  },
  mounted() {
    this.startAutoplay()
    this.initMap()
  },
  computed: {
    activeInfo() {
      return PROVINCE_SPOTS[this.activeProvince] || null
    },
    quickProvinces() {
      return PROVINCE_NAMES
    },
    provinceCount() {
      return Object.keys(PROVINCE_SPOTS).length
    },
    cityCount() {
      return Object.values(PROVINCE_SPOTS).reduce((n, p) => n + p.cities.length, 0)
    },
    spotCount() {
      return Object.values(PROVINCE_SPOTS).reduce(
        (n, p) => n + p.cities.reduce((m, c) => m + c.spots.length, 0),
        0,
      )
    },
  },
  beforeUnmount() {
    this.stopAutoplay()
    this.disposeMap()
  },
  methods: {
    selectProvince(name) {
      if (!PROVINCE_SPOTS[name]) return
      this.activeProvince = name
      if (chart) chart.dispatchAction({ type: 'geoSelect', geoIndex: 0, name })
    },
    async initMap() {
      const el = this.$refs.chinaMap
      if (!el) return
      try {
        const res = await fetch('/maps/china.json')
        const geo = await res.json()
        echarts.registerMap('china', geo)
        chart = echarts.init(el)
        chart.setOption({
          tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(13, 44, 77, 0.94)',
            borderWidth: 0,
            padding: [10, 14],
            textStyle: { color: '#fff', fontSize: 13, lineHeight: 20 },
            formatter: (p) => {
              const info = PROVINCE_SPOTS[p.name]
              if (!info) return p.name || ''
              const spots = info.cities.reduce((n, c) => n + c.spots.length, 0)
              return `<b>${p.name}</b><br/><span style="opacity:.7">${info.cities.length} 个热门城市 · ${spots} 个热门景点</span><br/>${info.intro}`
            },
          },
          geo: {
            map: 'china',
            roam: true,
            zoom: 1.3,
            scaleLimit: { min: 0.9, max: 10 },
            selectedMode: 'single',
            label: { show: false },
            itemStyle: {
              areaColor: '#e9f1fa',
              borderColor: '#ffffff',
              borderWidth: 0.9,
              shadowBlur: 14,
              shadowColor: 'rgba(22, 103, 196, 0.14)',
            },
            emphasis: {
              itemStyle: { areaColor: '#bcd8f3' },
            },
            select: {
              itemStyle: { areaColor: '#1667c4' },
            },
          },
        })
        chart.on('click', (p) => {
          if (p.componentType !== 'geo' || !PROVINCE_SPOTS[p.name]) return
          if (p.name === this.activeProvince) {
            this.activeProvince = ''
            chart.dispatchAction({ type: 'geoUnSelect', geoIndex: 0, name: p.name })
          } else {
            this.activeProvince = p.name
          }
        })
        window.addEventListener('resize', resizeMap)
      } catch {
        this.$message({ message: '地图数据加载失败', type: 'warning' })
      }
    },
    disposeMap() {
      window.removeEventListener('resize', resizeMap)
      if (chart) {
        chart.dispose()
        chart = null
      }
    },

    startAutoplay() {
      this.stopAutoplay()
      if (this.banners.length < 2) return
      const noMotion =
        window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches
      if (noMotion) return
      this.timer = window.setInterval(this.next, 5200)
    },
    stopAutoplay() {
      if (this.timer) {
        window.clearInterval(this.timer)
        this.timer = null
      }
    },
    pause() {
      this.stopAutoplay()
    },
    resume() {
      this.startAutoplay()
    },
    goTo(i) {
      const total = this.banners.length
      this.current = ((i % total) + total) % total
    },
    next() {
      this.goTo(this.current + 1)
    },
    prev() {
      this.goTo(this.current - 1)
    },
    bannerClick(b) {
      if (!b.link) return
      if (/^https?:\/\//.test(b.link)) {
        window.open(b.link, '_blank', 'noopener')
        return
      }
      this.$router.push(b.link)
    },
    go(path) {
      this.$router.push(path)
    },
    search() {
      const name = (this.keyword || '').trim()
      if (!name) {
        this.$message({ message: '请输入景点名称', type: 'warning' })
        return
      }
      // 原路径 /index/remenjingdian 是代码生成器的旧表名，前端没有这个路由，
      // 点下去会落到兜底占位页。改成真实的景点列表路由。
      this.$router.push({ path: '/index/attraction', query: { indexQueryCondition: name } })
    },
  },
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.home {
  background: var(--sand);
}

/* ---------------- 通用小件 ---------------- */

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 46px;
  padding: 0 26px;
  border: 0;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    background 0.18s ease;
}

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, var(--brand-light), var(--brand));
  box-shadow: 0 8px 20px rgba(22, 103, 196, 0.28);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(22, 103, 196, 0.36);
}

.btn-ghost {
  color: var(--ink);
  background: #fff;
  box-shadow: inset 0 0 0 1.5px var(--line);
}

.btn-ghost:hover {
  color: var(--brand);
  box-shadow: inset 0 0 0 1.5px var(--brand-light);
  transform: translateY(-2px);
}

.btn-onbrand {
  color: var(--brand);
  background: #fff;
}

.btn-onbrand-ghost {
  color: #fff;
  background: rgba(255, 255, 255, 0.14);
  box-shadow: inset 0 0 0 1.5px rgba(255, 255, 255, 0.5);
}

.btn-onbrand:hover,
.btn-onbrand-ghost:hover {
  transform: translateY(-2px);
}

.btn-icon {
  display: inline-flex;
  width: 18px;
  height: 18px;
}

.btn-icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.section {
  padding: 88px 0;
}

.section--band {
  background: linear-gradient(180deg, var(--sand-deep), var(--sand));
}

.section-head {
  max-width: 720px;
  margin: 0 auto 48px;
  text-align: center;
}

.section-kicker {
  display: inline-block;
  margin-bottom: 14px;
  padding: 5px 14px;
  border-radius: 999px;
  color: var(--brand);
  background: var(--brand-soft);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.section-title {
  color: var(--ink);
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.section-desc {
  margin-top: 14px;
  color: var(--ink-2);
  font-size: 16px;
  line-height: 1.75;
}

/* ---------------- 轮播 ---------------- */

.banner {
  background: var(--ink);
}

.banner-viewport {
  position: relative;
  overflow: hidden;
  height: 460px;
}

.banner-track {
  display: flex;
  height: 100%;
  transition: transform 0.65s cubic-bezier(0.4, 0.05, 0.2, 1);
}

.banner-slide {
  position: relative;
  flex: 0 0 100%;
  height: 100%;
}

.banner-slide__img,
.banner-slide__art {
  position: absolute;
  inset: 0;
  display: block;
  width: 100%;
  height: 100%;
}

.banner-slide__img {
  object-fit: cover;
}

.banner-slide__mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgba(13, 44, 77, 0.8) 0%,
    rgba(13, 44, 77, 0.52) 40%,
    rgba(13, 44, 77, 0.05) 78%
  );
}

.banner-slide__copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.banner-slide__kicker {
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 2px;
}

.banner-slide__title {
  margin: 12px 0 0;
  color: #fff;
  font-size: 44px;
  font-weight: 800;
  letter-spacing: -1px;
  text-shadow: 0 4px 22px rgba(0, 0, 0, 0.3);
}

.banner-slide__desc {
  margin-top: 14px;
  max-width: 470px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 16px;
  line-height: 1.8;
}

.banner-slide__copy .btn {
  align-self: flex-start;
  margin-top: 28px;
}

.banner-nav {
  position: absolute;
  top: 50%;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  margin-top: -22px;
  padding: 0 0 4px;
  border: 0;
  border-radius: 50%;
  color: #fff;
  background: rgba(13, 44, 77, 0.36);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.3);
  font-family: inherit;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
  transition:
    background 0.2s ease,
    transform 0.2s ease;
}

.banner-nav:hover {
  background: rgba(13, 44, 77, 0.66);
  transform: scale(1.06);
}

.banner-nav--prev {
  left: 18px;
}

.banner-nav--next {
  right: 18px;
}

.banner-dots {
  position: absolute;
  left: 50%;
  bottom: 22px;
  z-index: 2;
  display: flex;
  gap: 10px;
  transform: translateX(-50%);
}

.banner-dot {
  width: 26px;
  height: 5px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition:
    width 0.25s ease,
    background 0.25s ease;
}

.banner-dot.is-active {
  width: 44px;
  background: #fff;
}

/* ---------------- 首屏 ---------------- */

.hero {
  position: relative;
  overflow: hidden;
  padding-top: 56px;
  background: linear-gradient(165deg, #fbfdff 0%, #e9f2fb 48%, #dbe9f7 100%);
}

.hero-inner {
  position: relative;
  z-index: 1;
  padding-bottom: 72px;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 999px;
  color: var(--brand-dark);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: inset 0 0 0 1px rgba(22, 103, 196, 0.16);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
}

.eyebrow-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--brand);
}

.hero-title {
  margin: 22px 0 0;
  color: var(--ink);
  font-size: 54px;
  font-weight: 800;
  line-height: 1.22;
  letter-spacing: -1px;
}

.hero-sub {
  margin-top: 20px;
  max-width: 520px;
  color: var(--ink-2);
  font-size: 17px;
  line-height: 1.85;
}

.hero-search {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 30px;
  max-width: 560px;
  padding: 8px 8px 8px 20px;
  border-radius: 999px;
  background: #fff;
  box-shadow: var(--shadow);
}

.hero-search :deep(.el-input__wrapper) {
  padding: 0;
  box-shadow: none;
  background: transparent;
}

.hero-search :deep(.el-input__inner) {
  height: 44px;
  color: var(--ink);
  font-size: 15px;
}

.hero-search__input {
  flex: 1;
}

.hero-search__btn {
  height: 44px;
  padding: 0 22px;
  flex: none;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 26px;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 28px 0 0;
  padding: 0;
  list-style: none;
}

.hero-tags li {
  padding: 5px 14px;
  border-radius: 999px;
  color: var(--ink-2);
  background: rgba(255, 255, 255, 0.66);
  font-size: 13px;
}

/* ---------------- 系统板块 ---------------- */

.module-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}

.module-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  padding: 30px 26px 26px;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--card);
  box-shadow: var(--shadow-sm);
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease;
}

.module-card:hover {
  transform: translateY(-6px);
  border-color: transparent;
  box-shadow: var(--shadow);
}

.module-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  margin-bottom: 4px;
  border-radius: 16px;
  color: var(--brand);
  background: var(--brand-soft);
}

.module-card__icon :deep(svg) {
  width: 26px;
  height: 26px;
}

.module-card__name {
  color: var(--ink);
  font-size: 19px;
  font-weight: 700;
}

.module-card__desc {
  color: var(--ink-2);
  font-size: 14px;
  line-height: 1.75;
}

.module-card__go {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  color: var(--brand);
  font-size: 13px;
  font-weight: 600;
}

.module-card__arrow {
  transition: transform 0.22s ease;
}

.module-card:hover .module-card__arrow {
  transform: translateX(4px);
}

/* ---------------- 知识图谱 ---------------- */

.kg {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 20px;
  align-items: stretch;
}

.kg__canvas {
  position: relative;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

.china-map {
  width: 100%;
  height: 580px;
}

.kg__hint {
  position: absolute;
  right: 24px;
  bottom: 22px;
  padding: 5px 12px;
  border-radius: 999px;
  color: var(--ink-3);
  background: rgba(255, 255, 255, 0.86);
  font-size: 12px;
  pointer-events: none;
}

.kg__panel {
  display: flex;
  flex-direction: column;
  max-height: 606px;
  padding: 26px 20px 26px 24px;
  overflow-y: auto;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

.kg__badge {
  align-self: flex-start;
  padding: 4px 12px;
  border-radius: 999px;
  color: #fff;
  background: var(--brand);
  font-size: 12px;
  font-weight: 600;
}

.kg__intro {
  margin-top: 10px;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.7;
}

.kg__sub {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
  color: var(--ink);
  font-size: 14px;
  font-weight: 700;
}

.kg__cities {
  margin: 12px 0 0;
  padding: 0;
  list-style: none;
}

.kg__cities > li + li {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
}

.kg__city {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.kg__city-name {
  color: var(--ink);
  font-size: 15px;
  font-weight: 700;
}

.kg__city-tag {
  color: var(--ink-3);
  font-size: 12px;
}

.kg__spots {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 9px 0 0;
  padding: 0;
  list-style: none;
}

.kg__spots li {
  padding: 4px 10px;
  border-radius: 999px;
  color: var(--brand-dark);
  background: var(--brand-soft);
  font-size: 12px;
}

.kg__go {
  align-self: flex-start;
  margin-top: 22px;
}

.kg__empty {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: center;
  color: var(--ink-2);
  font-size: 14px;
  line-height: 1.85;
}

.kg__empty strong {
  color: var(--ink);
  font-size: 17px;
}

.kg__empty p {
  margin-top: 10px;
}

.kg__legend {
  margin: 20px 0 0;
  padding: 0;
  list-style: none;
}

.kg__legend li {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 9px;
  color: var(--ink-2);
  font-size: 13px;
}

.kg__legend b {
  color: var(--brand);
  font-size: 17px;
  font-weight: 800;
}

.kg__quick {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 20px;
}

.kg__quick button {
  padding: 5px 12px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--ink-2);
  background: var(--card);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.kg__quick button:hover {
  border-color: var(--brand-light);
  color: var(--brand);
  background: var(--brand-soft);
}

/* ---------------- 关于我们 ---------------- */

.about {
  padding: 64px 0 28px;
  color: rgba(255, 255, 255, 0.74);
  background: var(--navy);
}

.about-grid {
  display: grid;
  grid-template-columns: 1.7fr 1fr 1fr;
  gap: 48px;
}

.about-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.about-logo img {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  object-fit: cover;
}

.about-logo span {
  color: #fff;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 1px;
}

.about-intro {
  margin-top: 16px;
  max-width: 470px;
  font-size: 14px;
  line-height: 1.9;
}

.about-col__title {
  margin-bottom: 16px;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
}

.about-links {
  margin: 0;
  padding: 0;
  list-style: none;
}

.about-links li {
  margin-bottom: 10px;
  font-size: 14px;
}

.about-links a {
  color: rgba(255, 255, 255, 0.74);
  transition: color 0.2s ease;
}

.about-links a:hover {
  color: #fff;
}

.about-links--plain li {
  color: rgba(255, 255, 255, 0.6);
}

.about-foot {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 10px;
  margin-top: 48px;
  padding-top: 22px;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

/* ---------------- 入场动画 ---------------- */
.reveal {
  animation: fadeUp 0.6s cubic-bezier(0.22, 0.8, 0.3, 1) both;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .reveal {
    animation: none;
  }

  .banner-track {
    transition: none;
  }
}

/* ---------------- 窄屏 ---------------- */

@media (max-width: 1080px) {
  .hero-title {
    font-size: 44px;
  }

  .banner-viewport {
    height: 400px;
  }

  .banner-slide__title {
    font-size: 38px;
  }

  .module-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .about-grid {
    grid-template-columns: 1fr 1fr;
  }

  .about-brand {
    grid-column: 1 / -1;
  }

  .kg {
    grid-template-columns: minmax(0, 1fr);
  }

  .kg__panel {
    max-height: none;
  }
}

@media (max-width: 860px) {
  .hero {
    padding-top: 32px;
  }

  .hero-inner {
    padding-bottom: 48px;
  }

  .hero-title {
    font-size: 34px;
  }

  .banner-viewport {
    height: 330px;
  }

  .banner-slide__title {
    font-size: 28px;
  }

  .banner-slide__desc {
    font-size: 14px;
    line-height: 1.7;
  }

  .banner-slide__copy .btn {
    margin-top: 20px;
  }

  .banner-nav {
    display: none;
  }

  .section {
    padding: 60px 0;
  }

  .section-title {
    font-size: 26px;
  }

  .module-grid,
  .feature-grid,
  .steps {
    grid-template-columns: 1fr;
  }

  .cta__inner {
    padding: 34px 28px;
  }

  .cta__title {
    font-size: 24px;
  }

  .china-map {
    height: 380px;
  }

  .kg__hint {
    display: none;
  }

  .kg__panel {
    padding: 22px 18px;
  }

  .about {
    padding: 48px 0 24px;
  }

  .about-grid {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .about-foot {
    margin-top: 32px;
  }
}
</style>