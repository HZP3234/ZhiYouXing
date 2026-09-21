import { createRouter, createWebHashHistory } from 'vue-router'
import { isLogin } from '@/common/user'
import { isAdminTable } from '@/config/config'

/*
 * 用 hash 模式：index.vue 的 $route 监听里直接读 window.location.href 再 split('#')，
 * 是按 hash 路由写的；换成 history 模式那段跳转逻辑会失灵。
 */
const routes = [
  { path: '/', redirect: '/index/home' },

  {
    path: '/index',
    component: () => import('@/pages/index.vue'),
    children: [
      { path: '', redirect: '/index/home' },
      { path: 'home', name: 'home', component: () => import('@/pages/home/home.vue') },
      { path: 'agent', name: 'agent', component: () => import('@/pages/agent/agent.vue') },
      { path: 'attraction', name: 'attraction', component: () => import('@/pages/attraction/attractionList.vue'), meta: { activeMenu: '/index/attraction' } },
      { path: 'attraction/detail/:id', name: 'attractionDetail', component: () => import('@/pages/attraction/attractionDetail.vue'), meta: { activeMenu: '/index/attraction', requiresAuth: true } },
      { path: 'attraction/order/:id', name: 'attractionOrder', component: () => import('@/pages/attraction/attractionOrder.vue'), meta: { activeMenu: '/index/attraction', requiresAuth: true } },
      { path: 'hotel_info', name: 'hotelInfo', component: () => import('@/pages/hotel_info/hotelInfoList.vue'), meta: { activeMenu: '/index/hotel_info' } },
      { path: 'hotel_info/detail/:id', name: 'hotelInfoDetail', component: () => import('@/pages/hotel_info/hotelInfoDetail.vue'), meta: { activeMenu: '/index/hotel_info', requiresAuth: true } },
      { path: 'hotel_info/order/:id', name: 'hotelInfoOrder', component: () => import('@/pages/hotel_info/hotelInfoOrder.vue'), meta: { activeMenu: '/index/hotel_info', requiresAuth: true } },
      /* 「先选酒店、再选床位」的中间那级：一层是酒店列表，点某家酒店进到这里挑它的房型。
         :name 是酒店名（可能带中文，列表页 push 时 encodeURIComponent 过）。
         酒店名是**内容**、不是订单，所以不加 requiresAuth —— 与列表页一致，免登录可看。 */
      { path: 'hotel_info/hotel/:name', name: 'hotelInfoRooms', component: () => import('@/pages/hotel_info/hotelInfoRooms.vue'), meta: { activeMenu: '/index/hotel_info' } },
      { path: 'restaurant', name: 'restaurant', component: () => import('@/pages/restaurant/restaurantList.vue'), meta: { activeMenu: '/index/restaurant' } },
      { path: 'restaurant/detail/:id', name: 'restaurantDetail', component: () => import('@/pages/restaurant/restaurantDetail.vue'), meta: { activeMenu: '/index/restaurant', requiresAuth: true } },
      { path: 'restaurant/order/:id', name: 'restaurantOrder', component: () => import('@/pages/restaurant/restaurantOrder.vue'), meta: { activeMenu: '/index/restaurant', requiresAuth: true } },
      { path: 'travel_route', name: 'travelRoute', component: () => import('@/pages/travel/travel_route.vue'), meta: { activeMenu: '/index/travel_route' } },
      { path: 'travel_guide', name: 'travelGuide', component: () => import('@/pages/travel/travel_guide.vue'), meta: { activeMenu: '/index/travel_guide' } },
      /* 攻略详情：公开内容，**不加 requiresAuth** —— 与后端 /travel_guide/detail/{id} 的 @IgnoreAuth 一个口径，
         也和列表页一致（列表页本身免登录，点进去却要求登录会说不通）。 */
      { path: 'travel_guide/detail/:id', name: 'travelGuideDetail', component: () => import('@/pages/travel/travelGuideDetail.vue'), meta: { activeMenu: '/index/travel_guide' } },
      /* 写作页：这两条**必须**要登录。后端 /save 与 /update 会把会话里的账号钉成作者，
         没有 Token 的请求会被 AuthorizationInterceptor 直接拦下，所以路由层先拦一道，
         避免用户填完一大篇正文才在提交时被退回登录页。
         新建与编辑共用同一个组件，编辑态的 id 在路径上。 */
      { path: 'travel_guide/write', name: 'travelGuideWrite', component: () => import('@/pages/travel/travelGuideWrite.vue'), meta: { activeMenu: '/index/travel_guide', requiresAuth: true } },
      { path: 'travel_guide/write/:id', name: 'travelGuideEdit', component: () => import('@/pages/travel/travelGuideWrite.vue'), meta: { activeMenu: '/index/travel_guide', requiresAuth: true } },
      /* 游客个人中心。顶栏与菜单里的「个人中心」早就指向这里，之前没有路由，一直落到兜底占位页 */
      { path: 'center', name: 'userCenter', component: () => import('@/pages/user/center.vue'), meta: { requiresAuth: true } },

      {
        path: ':pathMatch(.*)*',
        name: 'placeholder',
        component: () => import('@/pages/placeholder/placeholder.vue'),
      },
    ],
  },

  {
    path: '/admin',
    component: () => import('@/pages/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/home' },
      { path: 'home', name: 'adminHome', component: () => import('@/pages/admin/AdminHome.vue') },
      { path: 'resource/:resource', name: 'adminResource', component: () => import('@/pages/admin/AdminCrud.vue') },
      { path: 'profile', name: 'adminProfile', component: () => import('@/pages/admin/AdminProfile.vue') },
      /* 导游的咨询收件箱。不是 crud 页 —— 会话只在服务内存里，没有表也没有资源前缀 */
      { path: 'consult', name: 'adminConsult', component: () => import('@/pages/admin/AdminChat.vue') },
    ],
  },

  { path: '/login', name: 'login', component: () => import('@/pages/login/login.vue') },
  { path: '/register', name: 'register', component: () => import('@/pages/register/register.vue') },

  { path: '/:pathMatch(.*)*', redirect: '/index/home' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  /*
   * 谁是「管理端角色」由 localStorage 里登录时写的身份决定
   * （login.vue 写 frontSessionTable / UserTableName），与后端 token 里的 tableName 一致。
   * 这只是前端的门帘——真权限在后端：管理端接口都过 AuthorizationInterceptor，
   * 没有 token 一律 401。所以这里判错了最多是看到空列表，不会越权改数据。
   */
  if (to.meta.requiresAdmin) {
    const tableName = localStorage.getItem('frontSessionTable') || localStorage.getItem('UserTableName') || ''
    if (!isLogin()) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    if (!isAdminTable(tableName)) {
      return { path: '/index/home' }
    }
  }

  if (to.meta.requiresAuth && !isLogin()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
