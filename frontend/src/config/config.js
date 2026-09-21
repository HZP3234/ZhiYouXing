export default {
  baseUrl: '/api/',

  /*
   * ============ mock 总开关 ============
   * 以前是「true → 景点/酒店/餐厅走 src/api/mock 的本地数据，false → 走真后端」。
   * **现在这一行对应用里用到的每一个前缀都已经不起作用了** —— 组长在
   * src/api/client.js 里加了 REAL_PREFIXES 白名单，把景点、餐厅、酒店、用户四组
   * 前缀（含各自的字典/评价/订单前缀）全部钉成真接口，判断统一走 useMockFor(prefix)。
   * 也就是说无论这里填 true 还是 false，页面拿到的都是真库数据。
   *
   * 值保留 true 是组长的取舍（他明确回退过一次改成 false 的提交），这里不再动它：
   * 留着给将来「还没接后端的模块」兜底演示 —— 那种模块的前缀不在白名单里，
   * 仍会落到 mock 分支，此时这个开关是有效的。
   *
   * 临时切换不用改代码：浏览器控制台执行 localStorage.setItem('mockOff','1') 后刷新。
   * 注意它只能**关闭** mock（USE_MOCK = useMock && mockOff !== '1'），没法把 mock 打开。
   *
   * 注意：只影响游客端走 src/api/client.js 的那些页面。
   * 管理端不走 mock —— 见 src/pages/admin/AdminCrud.vue，它直接打真实接口。
   */
  useMock: true,

  indexNav: [
    { name: '热门景点', url: '/index/attraction' },
    { name: '酒店信息', url: '/index/hotel_info' },
    { name: '美食餐厅', url: '/index/restaurant' },
    { name: '旅游线路', url: '/index/travel_route' },
    { name: '旅游攻略', url: '/index/travel_guide' },
    { name: 'AI 行程助手', url: '/index/agent' },
  ],
}

/*
 * ============ 角色 ============
 * 系统里共 6 个角色，共用同一个首页与同一套登录/注册页：
 *   游客留在前台（/index/*），另外五个角色登录后进管理端（/admin/*）。
 *
 * tableName 一处三用：
 *   1. 接口前缀（见下面的 API_PREFIX，登录就是打 /<prefix>/login）
 *   2. 注册时账号写进哪一列（见 REGISTER_ACCOUNT_FIELD）
 *   3. 后端 token 里记的身份，前端存 localStorage 的 frontSessionTable
 *
 * 这四个 staff 角色在后端各有一个只含登录态与个人资料的控制器
 * （attraction_staff 在 attraction 服务、hotel_staff 在 hotel、restaurant_staff 在 food）。
 *
 * 管理员（users）是账号表自身的一行，不走注册：
 *   noRegister 让注册页不列它，因为 users 表里只有预置的 admin 这一条，
 *   注册接口（UsersController.register）还会把 role 强制改成「用户」——点了也白点。
 *   它的登录/个人资料复用的是 UsersController 现成的 /users/login、/users/session、
 *   /users/update，审核功能则挂在 UsersScopeController 的 /users/user_identity/**、
 *   /users/qualification/** 上。
 */
export const ROLES = [
  { tableName: 'yonghu', roleName: '游客', admin: false },
  { tableName: 'daoyou', roleName: '导游', admin: true },
  { tableName: 'hotel_staff', roleName: '酒店前台', admin: true },
  { tableName: 'attraction_staff', roleName: '景点前台', admin: true },
  { tableName: 'restaurant_staff', roleName: '餐厅前台', admin: true },
  { tableName: 'users', roleName: '管理员', admin: true, noRegister: true },
]

export const GUEST_TABLE = 'yonghu'

export function roleOf(tableName) {
  return ROLES.find((item) => item.tableName === tableName) || null
}

/** 取中文角色名；未登记的角色返回空串 */
export function roleNameOf(tableName) {
  const role = roleOf(tableName)
  return role ? role.roleName : ''
}

/** 该角色是否进管理端（除游客外都是） */
export function isAdminTable(tableName) {
  const role = roleOf(tableName)
  return Boolean(role && role.admin)
}

/** 登录/注册成功后的落地页 */
export function landingPath(tableName) {
  return isAdminTable(tableName) ? '/admin/home' : '/index/home'
}

/*
 * ============ 接口前缀 ============
 * 网关按「资源前缀」路由（见 zhiyouxing-gateway 的 routes 与 vite.config.ts 的代理）。
 * 三个 staff 的前缀与它们的表名同名，是因为控制器就挂在 /<表名> 上。
 */
export const API_PREFIX = {
  yonghu: 'user',
  daoyou: 'tour_guide',
  users: 'users',
  hotel_staff: 'hotel_staff',
  attraction_staff: 'attraction_staff',
  restaurant_staff: 'restaurant_staff',
}

/** 取接口前缀；未登记的按原样返回 */
export function apiPrefix(tableName) {
  return API_PREFIX[tableName] || tableName
}

/*
 * 注册/登录只收「用户类型 + 手机号 + 密码」，手机号直接写进该角色的账号列
 * （后端登录接口就是按这一列查的：user.user_account / tour_guide.guide_no /
 *  *_staff.staff_account），同时再写一份 contact_phone。
 *
 * 代价：导游的「工号」实际存的是手机号；姓名字段注册时不再采集，
 * 后端在 register 里补一个占位名（见各 Controller.register）。
 */
export const REGISTER_ACCOUNT_FIELD = {
  yonghu: 'userAccount',
  daoyou: 'guideNo',
  hotel_staff: 'staffAccount',
  attraction_staff: 'staffAccount',
  restaurant_staff: 'staffAccount',
}
