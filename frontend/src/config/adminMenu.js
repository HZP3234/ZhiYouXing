/*
 * 管理端左侧菜单，按角色切分。
 *
 * 四个 staff 角色各管自己那一摊：
 *   景点前台 → 景点/景点类型/门票订单/景点评论
 *   酒店前台 → 酒店信息/客房/酒店预订/酒店评论
 *   餐厅前台 → 餐厅/餐厅预约/餐厅评论
 *   导游     → 旅游线路/报团信息；旅游攻略由用户在游客端看，管理端不出（旅游模块上游是 travel 服务，没有 staff 表）
 *   管理员   → 只做审核：实名认证 + 四类管理端资质 + 线路审核 + 酒店/景点/餐厅信息审核（见下面 ADMIN_MENUS.users）
 *
 * 每项 kind 决定跳到哪一类页面：
 *   home    工作台（AdminHome.vue）
 *   crud    资源列表（AdminCrud.vue，:resource 取 RESOURCES 的键）
 *   profile 个人资料（AdminProfile.vue）
 *
 * 菜单里只出现「本角色有权限的接口前缀」——不做的不是藏起来，而是后端路由都没有，
 * 前端也就不列（比如餐厅前台看不到酒店预订）。
 */

const PROFILE_ITEM = { kind: 'profile', path: '/admin/profile', label: '个人资料', icon: 'user' }

export const ADMIN_MENUS = {
  attraction_staff: [
    { kind: 'home', path: '/admin/home', label: '工作台', icon: 'home' },
    { kind: 'crud', path: '/admin/resource/attraction', label: '景点信息', icon: 'photo' },
    { kind: 'crud', path: '/admin/resource/attraction_type', label: '景点类型', icon: 'tag' },
    { kind: 'crud', path: '/admin/resource/ticket_order', label: '门票订单', icon: 'ticket' },
    { kind: 'crud', path: '/admin/resource/attraction_comment', label: '景点评论', icon: 'chat' },
    PROFILE_ITEM,
  ],

  hotel_staff: [
    { kind: 'home', path: '/admin/home', label: '工作台', icon: 'home' },
    { kind: 'crud', path: '/admin/resource/hotel_info', label: '酒店信息', icon: 'office' },
    { kind: 'crud', path: '/admin/resource/room_type', label: '客房', icon: 'tag' },
    { kind: 'crud', path: '/admin/resource/hotel_reservation', label: '酒店预订', icon: 'ticket' },
    { kind: 'crud', path: '/admin/resource/hotel_comment', label: '酒店评论', icon: 'chat' },
    PROFILE_ITEM,
  ],

  restaurant_staff: [
    { kind: 'home', path: '/admin/home', label: '工作台', icon: 'home' },
    { kind: 'crud', path: '/admin/resource/restaurant', label: '餐厅信息', icon: 'food' },
    { kind: 'crud', path: '/admin/resource/restaurant_reservation', label: '餐厅预约', icon: 'ticket' },
    { kind: 'crud', path: '/admin/resource/restaurant_comment', label: '餐厅评论', icon: 'chat' },
    PROFILE_ITEM,
  ],

  daoyou: [
    { kind: 'home', path: '/admin/home', label: '工作台', icon: 'home' },
    { kind: 'crud', path: '/admin/resource/travel_route', label: '旅游线路', icon: 'route' },
    { kind: 'crud', path: '/admin/resource/group_tour', label: '报团信息', icon: 'ticket' },
    // 不是 crud：没有表、也没有资源前缀（对话只活在内存+游客浏览器里），
    // 所以要单独一页，不能挂进 AdminCrud 的通用列表
    { kind: 'chat', path: '/admin/consult', label: '咨询消息', icon: 'chat' },
    PROFILE_ITEM,
  ],

  /*
   * 管理员：只审不改内容。
   *
   * 九个页面共用 AdminCrud.vue，但资源键各不相同（菜单项要能各自高亮、
   * 工作台的卡片也要按页面分别计数）。四类资质实际落在同一张 qualification 表上，
   * 靠 RESOURCES 里的 endpoint: 'qualification' + fixedQuery.tableName 分流
   * ——见 src/config/adminResources.js。线路 / 酒店 / 景点 / 餐厅四类内容审核各一张表
   * （travel_route、hotel_info、attraction、restaurant），接口分别挂在
   * /users/travel_route_audit、/users/hotel_info_audit、/users/attraction_audit、
   * /users/restaurant_audit ——数据分别由 travel / hotel / attraction / food 服务写入，
   * 五个服务连同一个库，所以 user 服务读得到（见 UsersScopeController）。
   *
   * 菜单 kind 仍是 crud（它就是 AdminCrud 页），列表里给不给「新增」按钮
   * 由资源的 noCreate 决定，不是靠菜单 kind。
   */
  users: [
    { kind: 'home', path: '/admin/home', label: '工作台', icon: 'home' },
    { kind: 'crud', path: '/admin/resource/user_identity', label: '实名认证审核', icon: 'user' },
    { kind: 'crud', path: '/admin/resource/qualification_daoyou', label: '导游资质审核', icon: 'route' },
    { kind: 'crud', path: '/admin/resource/qualification_hotel_staff', label: '酒店前台资质审核', icon: 'office' },
    { kind: 'crud', path: '/admin/resource/qualification_attraction_staff', label: '景点前台资质审核', icon: 'photo' },
    { kind: 'crud', path: '/admin/resource/qualification_restaurant_staff', label: '餐厅前台资质审核', icon: 'food' },
    { kind: 'crud', path: '/admin/resource/travel_route_audit', label: '线路审核', icon: 'route' },
    { kind: 'crud', path: '/admin/resource/hotel_info_audit', label: '酒店信息审核', icon: 'office' },
    { kind: 'crud', path: '/admin/resource/attraction_audit', label: '景点信息审核', icon: 'photo' },
    { kind: 'crud', path: '/admin/resource/restaurant_audit', label: '餐厅信息审核', icon: 'food' },
    PROFILE_ITEM,
  ],
}

/** 取该角色的菜单；游客或未登记角色返回空数组（守卫会把人赶回前台） */
export function menuOf(tableName) {
  return ADMIN_MENUS[tableName] || []
}

/** 该角色是否有这个菜单路径，用于校验手敲 URL / 书签 */
export function canAccess(tableName, path) {
  return menuOf(tableName).some((item) => item.path === path)
}
