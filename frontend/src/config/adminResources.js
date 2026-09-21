/*
 * 管理端「资源」定义表。
 *
 * 后端所有业务控制器都是代码生成器那一套，接口形状完全一致：
 *   GET  /<prefix>/page?page=&limit=&<字段>=   分页（信封 code 0，data = { list, totalCount, currPage, pageSize }）
 *   GET  /<prefix>/info/{id}                   详情
 *   POST /<prefix>/save                        新增（id 由后端按时间戳生成）
 *   POST /<prefix>/update                      修改（null 字段 MyBatis-Plus 会跳过）
 *   POST /<prefix>/delete  body=[id,...]       批量删除
 * 所以管理端只需要一份「字段表」+ 一个通用页面（src/pages/admin/AdminCrud.vue），
 * 不必给每张表手写一个列表页。
 *
 * kind 决定列表上方额外的操作按钮：
 *   crud    内容类 —— 新增 / 编辑 / 删除
 *   order   订单类 —— 内容类全部 + 「标记已支付 / 标记未支付」改 is_pay
 *   audit   预约类 —— 内容类全部 + 「审核通过 / 驳回」改 audit_status / audit_reply
 *   comment 评论类 —— 编辑（回复）/ 删除，不给新增
 *
 * 还有三个可选项（管理员那组审核页在用，见文件末尾）：
 *   endpoint    接口路径里的资源名。默认等于 RESOURCES 的键，也就是页面路由的 :resource；
 *               四类资质同表不同页，页面键必须唯一，就用它把请求指回 /users/qualification。
 *   fixedQuery  每次 page 请求都带上的固定查询条件（如 { tableName: 'tour_guide' }），
 *               用来在共享的一张表里只看自己那一类。值必须是后端的真表名，
 *               不是 config.js 里那套前端角色键（导游是 tour_guide 而非 daoyou）。
 *   noCreate    不给「新增」按钮。审核页面的数据都由提交方写入，管理员只改结论。
 *
 * 字段 type 决定表单控件与列表渲染 —— 注意 columns 和 fields 共用一部分名字，但词表并不相同：
 *
 *   fields（录入控件，AdminCrud.vue 的字段渲染器）：
 *     text(默认) | textarea | select | date | datetime | days
 *     number    整数/小数，可选 min / max / precision / step
 *     money     金额，同 number，只是 precision 缺省 2（以前只写在 columns 里能显示金额，
 *               写进 fields 会整个控件不渲染 —— 渲染器当时没有这个分支，现已补上）
 *     phone     手机号，只能打数字、最多 11 位（见 src/common/inputFilter.js）
 *     idcard    身份证，数字 + 末位 X、最多 18 位。只给「用户自己录入」的框用：
 *               实名认证审核页读的是后端脱敏过的 id_card_masked（前 6 后 4 带 *），
 *               挂上这个类型会把 * 全吃掉，那个字段必须留在 text
 *     tel       座机/含区号的联系电话，允许数字、连字符与区号前的 +，最多 20 位。
 *               为什么不全用 phone：餐厅表 contact_phone 有 124 行全是
 *               「020-81391234」这种座机，按 11 位纯数字口径会被截掉一位
 *     images    逗号分隔的图片路径；库里这些路径指向不存在的文件，
 *               列表里一律用全局的 <safe-image> 兜底成风景画（见 src/common/media.js）
 *
 *   columns（列表渲染，AdminCrud.vue 的列表渲染器）：
 *     text(默认) | image | money | tag | 其余交给默认文本
 *
 * 数字字段的 min / max / precision 缺省值（写字段时按这两条原则取）：
 *   1. 金额一类只写 min: 0 与 precision: 2，**不写 max**。线路费上万是正常业务，
 *      硬加上限会造成「合法金额存不进去」，而且金额列是 double，库里本来没有上限。
 *   2. 数量类那些 99999 / 9999 / 999 / 365 不是业务硬上限，是「够用、又能挡住手滑多打几个 0」
 *      的量级。真正有语义的只有两处：score 的 max: 5，以及订单类字段的 min: 1。
 *   ⚠️ el-input-number 的 modelValue 侦听器是 immediate 的，一挂上越界的 min/max 就会把值
 *      夹到边界并 emit。所以只读镜像字段（*_audit）一律不写 min/max，免得打开详情就改数据。
 *      （渲染器也对 readonly 字段直接忽略 bounds，两边各挡一道。）
 *
 * 日期一律用 date / datetime（el-date-picker），不要开成文本框：
 *   只到天用 'YYYY-MM-DD'，有时刻意义的才用 'YYYY-MM-DD HH:mm:ss'。
 *   后端 LocalDateTime 都挂了 @JsonFormat(yyyy-MM-dd HH:mm:ss)，只发日期会 400（见 departureDate）。
 */

/** 是否支付 / 是否审核两个枚举值的标签色 */
const PAY_TAG = { 已支付: 'success', 未支付: 'info' }
const AUDIT_TAG = { 已通过: 'success', 已驳回: 'danger', 待审核: 'warning' }

export const PAY_OPTIONS = ['未支付', '已支付']
export const AUDIT_OPTIONS = ['待审核', '已通过', '已驳回']

export const RESOURCES = {
  /* ==================== 景点前台 ==================== */

  /*
   * 景点信息（景点级：一个景点一行）。
   *
   * 这个表单**没有直接上架这回事**：一提交后端就把 audit_status 盖成「待审核」，
   * 而游客端的 /attraction/list、/detail/{id} 只放行「已通过」，所以在管理员点通过之前，
   * 游客的「热门景点」页看不到这个景点、也下不了单。
   * 审核结论只读地列在 columns 里让前台看进度，不在 fields 中 —— 改不了。
   */
  attraction: {
    title: '景点信息',
    prefix: 'attraction',
    kind: 'crud',
    search: [{ prop: 'attractionName', label: '景点名称' }],
    columns: [
      { prop: 'image', label: '图片', type: 'image' },
      { prop: 'attractionName', label: '景点名称' },
      { prop: 'attractionType', label: '景点类型' },
      { prop: 'ticketPrice', label: '门票价格', type: 'money' },
      { prop: 'quantity', label: '数量' },
      { prop: 'openingHours', label: '开放时间' },
      { prop: 'attractionLocation', label: '景点位置' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'attractionName', label: '景点名称', required: true },
      { prop: 'attractionType', label: '景点类型', type: 'select', optionsFrom: { prefix: 'attraction_type', key: 'attractionType' } },
      { prop: 'image', label: '图片路径', type: 'images' },
      { prop: 'openingHours', label: '开放时间' },
      { prop: 'ticketPrice', label: '门票价格', type: 'number', required: true, min: 0, precision: 2 },
      { prop: 'quantity', label: '数量', type: 'number', min: 0, max: 999999, precision: 0 },
      { prop: 'attractionLocation', label: '景点位置' },
      { prop: 'attractionDescription', label: '景点介绍', type: 'textarea' },
    ],
  },

  attraction_type: {
    title: '景点类型',
    prefix: 'attraction_type',
    kind: 'crud',
    search: [{ prop: 'attractionType', label: '景点类型' }],
    columns: [
      { prop: 'image', label: '图片', type: 'image' },
      { prop: 'attractionType', label: '景点类型' },
    ],
    fields: [
      { prop: 'attractionType', label: '景点类型', required: true },
      { prop: 'image', label: '图片路径', type: 'images' },
    ],
  },

  ticket_order: {
    title: '门票订单',
    prefix: 'ticket_order',
    kind: 'order',
    payField: 'isPay',
    search: [
      { prop: 'orderNo', label: '订单编号' },
      { prop: 'userName', label: '用户姓名' },
    ],
    columns: [
      { prop: 'orderNo', label: '订单编号' },
      { prop: 'attractionName', label: '景点名称' },
      { prop: 'ticketPrice', label: '门票价格', type: 'money' },
      { prop: 'quantity', label: '购买数量' },
      { prop: 'totalAmount', label: '总金额', type: 'money' },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'userName', label: '用户姓名' },
      { prop: 'contactPhone', label: '联系方式' },
      { prop: 'purchaseTime', label: '购买时间' },
      { prop: 'isPay', label: '是否支付', type: 'tag', tagMap: PAY_TAG },
    ],
    fields: [
      { prop: 'orderNo', label: '订单编号', required: true },
      { prop: 'attractionName', label: '景点名称' },
      { prop: 'ticketPrice', label: '门票价格', type: 'number', min: 0, precision: 2 },
      { prop: 'quantity', label: '购买数量', type: 'number', min: 1, max: 9999, precision: 0 },
      { prop: 'totalAmount', label: '总金额', type: 'number', min: 0, precision: 2 },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'userName', label: '用户姓名' },
      { prop: 'contactPhone', label: '联系方式', type: 'phone' },
      { prop: 'purchaseTime', label: '购买时间', type: 'date' },
      { prop: 'isPay', label: '是否支付', type: 'select', options: PAY_OPTIONS },
    ],
  },

  attraction_comment: {
    title: '景点评论',
    prefix: 'attraction_comment',
    kind: 'comment',
    search: [{ prop: 'nickname', label: '用户名' }],
    columns: [
      { prop: 'nickname', label: '用户名' },
      { prop: 'content', label: '评论内容' },
      { prop: 'score', label: '评分' },
      { prop: 'reply', label: '回复内容' },
      { prop: 'addTime', label: '评论时间' },
    ],
    fields: [
      { prop: 'nickname', label: '用户名' },
      { prop: 'content', label: '评论内容', type: 'textarea' },
      { prop: 'score', label: '评分', type: 'number', min: 0, max: 5, precision: 0 },
      { prop: 'reply', label: '回复内容', type: 'textarea' },
    ],
  },

  /* ==================== 酒店前台 ==================== */

  /*
   * 酒店信息（酒店级：一家酒店一行，客房在 room_type 里单独维护）。
   *
   * 这个表单**没有直接营业这回事**：一提交后端就把 audit_status 盖成「待审核」，
   * 而游客端的 /hotel_info/list、/detail/{id}（以及它下面客房的 /room_type/list、
   * /detail/{id}）只放行「已通过」，所以在管理员点通过之前，游客的酒店页看不到这家店。
   * 审核结论只读地列在 columns 里让前台看进度，不在 fields 中 —— 改不了。
   */
  hotel_info: {
    title: '酒店信息',
    prefix: 'hotel_info',
    kind: 'crud',
    search: [
      { prop: 'hotelName', label: '酒店名称' },
      { prop: 'hotelAddress', label: '酒店地址' },
    ],
    columns: [
      { prop: 'hotelImage', label: '图片', type: 'image' },
      { prop: 'hotelName', label: '酒店名称' },
      { prop: 'hotelAddress', label: '酒店地址' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'hotelName', label: '酒店名称', required: true },
      { prop: 'hotelAddress', label: '酒店地址' },
      { prop: 'hotelImage', label: '图片路径', type: 'images' },
      { prop: 'hotelIntro', label: '酒店介绍', type: 'textarea' },
    ],
  },

  /*
   * 客房（客房级：一家酒店的每个在售房型一行，归属列 hotel_name 由后端按登录员工带出）。
   * 客房不审核，前台添加后立即生效 —— 但游客能不能摸到，还要看它所属的酒店过没过审。
   */
  room_type: {
    title: '客房',
    prefix: 'room_type',
    kind: 'crud',
    search: [
      { prop: 'roomName', label: '客房名称' },
      { prop: 'roomType', label: '客房类型' },
    ],
    columns: [
      { prop: 'roomImage', label: '图片', type: 'image' },
      { prop: 'roomName', label: '客房名称' },
      { prop: 'roomType', label: '客房类型' },
      { prop: 'roomPrice', label: '客房价格', type: 'money' },
      { prop: 'roomCount', label: '客房数量' },
    ],
    fields: [
      { prop: 'roomName', label: '客房名称', required: true },
      { prop: 'roomType', label: '客房类型', required: true },
      { prop: 'roomImage', label: '图片路径', type: 'images' },
      // room_type 的 room_price / room_count 两列在库里都是 int，所以价格也是整数、没有小数位
      { prop: 'roomPrice', label: '客房价格', type: 'number', min: 0, precision: 0 },
      { prop: 'roomCount', label: '客房数量', type: 'number', min: 0, max: 9999, precision: 0 },
      { prop: 'roomFacility', label: '客房设施', type: 'textarea' },
      { prop: 'roomDescription', label: '客房介绍', type: 'textarea' },
    ],
  },

  /*
   * 酒店预订（只读 + 标记已支付）。预订只能由用户在游客端发起（酒店下单页），
   * 前台没有「新增预订」这个口子 —— 后端 /hotel_staff/hotel_reservation 也只剩
   * page/info/update/delete，没有 save。
   */
  hotel_reservation: {
    title: '酒店预订',
    prefix: 'hotel_reservation',
    kind: 'order',
    payField: 'isPay',
    noCreate: true,
    search: [
      { prop: 'reservationNo', label: '预订单号' },
      { prop: 'userName', label: '用户姓名' },
    ],
    columns: [
      { prop: 'reservationNo', label: '预订单号' },
      { prop: 'hotelName', label: '酒店名称' },
      { prop: 'roomName', label: '客房名称' },
      { prop: 'roomPrice', label: '客房价格', type: 'money' },
      { prop: 'roomCount', label: '客房数量' },
      { prop: 'stayDays', label: '入住天数' },
      { prop: 'totalAmount', label: '总金额', type: 'money' },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'userName', label: '用户姓名' },
      { prop: 'contactPhone', label: '联系方式' },
      { prop: 'reservationTime', label: '预约时间' },
      { prop: 'isPay', label: '是否支付', type: 'tag', tagMap: PAY_TAG },
    ],
    fields: [
      { prop: 'reservationNo', label: '预订单号', required: true },
      { prop: 'hotelName', label: '酒店名称' },
      { prop: 'roomName', label: '客房名称' },
      // hotel_reservation 的 room_price 也是 int 列；房间数/天数都是「已经发生」的量，最小 1
      { prop: 'roomPrice', label: '客房价格', type: 'number', min: 0, precision: 0 },
      { prop: 'roomCount', label: '客房数量', type: 'number', min: 1, max: 999, precision: 0 },
      { prop: 'stayDays', label: '入住天数', type: 'number', min: 1, max: 365, precision: 0 },
      { prop: 'totalAmount', label: '总金额', type: 'number', min: 0, precision: 2 },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'userName', label: '用户姓名' },
      { prop: 'contactPhone', label: '联系方式', type: 'phone' },
      { prop: 'reservationTime', label: '预约时间', type: 'datetime' },
      { prop: 'isPay', label: '是否支付', type: 'select', options: PAY_OPTIONS },
    ],
  },

  hotel_comment: {
    title: '酒店评论',
    prefix: 'hotel_comment',
    kind: 'comment',
    search: [{ prop: 'nickname', label: '用户名' }],
    columns: [
      { prop: 'nickname', label: '用户名' },
      { prop: 'content', label: '评论内容' },
      { prop: 'score', label: '评分' },
      { prop: 'reply', label: '回复内容' },
      { prop: 'addTime', label: '评论时间' },
    ],
    fields: [
      { prop: 'nickname', label: '用户名' },
      { prop: 'content', label: '评论内容', type: 'textarea' },
      { prop: 'score', label: '评分', type: 'number', min: 0, max: 5, precision: 0 },
      { prop: 'reply', label: '回复内容', type: 'textarea' },
    ],
  },

  /* ==================== 餐厅前台 ==================== */

  /*
   * 餐厅信息（餐厅级：一家餐厅一行）。
   *
   * 与景点信息同一个套路：一提交后端就把 audit_status 盖成「待审核」，游客端的
   * /restaurant/list、/detail/{id} 只放行「已通过」。
   *
   * 注意别与下面 restaurant_reservation 里那对 auditStatus/auditReply 混了 ——
   * 那个是餐厅前台审「订座」用的，审的是预约单；这里是餐厅**主表内容**，审的人是管理员。
   */
  restaurant: {
    title: '餐厅信息',
    prefix: 'restaurant',
    kind: 'crud',
    search: [{ prop: 'restaurantName', label: '餐厅名称' }],
    columns: [
      { prop: 'restaurantImage', label: '图片', type: 'image' },
      { prop: 'restaurantName', label: '餐厅名称' },
      { prop: 'signatureDish', label: '特色菜品' },
      { prop: 'businessHours', label: '营业时间' },
      { prop: 'avgCost', label: '人均消费', type: 'money' },
      { prop: 'restaurantAddress', label: '餐厅地址' },
      { prop: 'contactPhone', label: '联系电话' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'restaurantName', label: '餐厅名称', required: true },
      { prop: 'restaurantImage', label: '图片路径', type: 'images' },
      { prop: 'signatureDish', label: '特色菜品' },
      { prop: 'businessHours', label: '营业时间' },
      { prop: 'avgCost', label: '人均消费', type: 'number', min: 0, precision: 2 },
      { prop: 'restaurantAddress', label: '餐厅地址' },
      // 用 tel 而不是 phone：这家表的 contact_phone 有 124 行全是座机（020-81391234、
      // 00853-28827123 这种），手机号「11 位纯数字」的口径会把号码截掉一位还写回去
      { prop: 'contactPhone', label: '联系电话', type: 'tel' },
      { prop: 'restaurantIntro', label: '餐厅简介', type: 'textarea' },
    ],
  },

  restaurant_reservation: {
    title: '餐厅预约',
    prefix: 'restaurant_reservation',
    kind: 'audit',
    auditField: 'auditStatus',
    replyField: 'auditReply',
    search: [
      { prop: 'restaurantName', label: '餐厅名称' },
      { prop: 'userName', label: '用户姓名' },
    ],
    columns: [
      { prop: 'restaurantImage', label: '图片', type: 'image' },
      { prop: 'restaurantName', label: '餐厅名称' },
      { prop: 'restaurantType', label: '餐厅类型' },
      { prop: 'dinerCount', label: '用餐人数' },
      { prop: 'reservationTime', label: '预约时间' },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'userName', label: '用户姓名' },
      { prop: 'contactPhone', label: '联系方式' },
      { prop: 'auditStatus', label: '是否审核', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'restaurantName', label: '餐厅名称', required: true },
      { prop: 'restaurantType', label: '餐厅类型' },
      { prop: 'restaurantImage', label: '图片路径', type: 'images' },
      { prop: 'dinerCount', label: '用餐人数', type: 'number', min: 1, max: 999, precision: 0 },
      { prop: 'reservationTime', label: '预约时间', type: 'datetime' },
      { prop: 'diningRemark', label: '用餐备注', type: 'textarea' },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'userName', label: '用户姓名' },
      { prop: 'contactPhone', label: '联系方式', type: 'phone' },
      { prop: 'auditStatus', label: '是否审核', type: 'select', options: AUDIT_OPTIONS },
      { prop: 'auditReply', label: '审核回复', type: 'textarea' },
    ],
  },

  restaurant_comment: {
    title: '餐厅评论',
    prefix: 'restaurant_comment',
    kind: 'comment',
    search: [{ prop: 'nickname', label: '用户名' }],
    columns: [
      { prop: 'nickname', label: '用户名' },
      { prop: 'content', label: '评论内容' },
      { prop: 'score', label: '评分' },
      { prop: 'reply', label: '回复内容' },
      { prop: 'addTime', label: '评论时间' },
    ],
    fields: [
      { prop: 'nickname', label: '用户名' },
      { prop: 'content', label: '评论内容', type: 'textarea' },
      { prop: 'score', label: '评分', type: 'number', min: 0, max: 5, precision: 0 },
      { prop: 'reply', label: '回复内容', type: 'textarea' },
    ],
  },

  /* ==================== 导游 ==================== */

  /*
   * 导游的线路维护页。
   *
   * 字段按「游客『旅游线路』页真正会渲染的东西」来定（卡片 + 详情抽屉两处）：
   *   routeImage/routeName/routeFee        → 卡片封面、标题、价格
   *   attractionType                       → 封面角标 + 抽屉 badge
   *   startPoint/endPoint                  → 卡片 overlay + 抽屉出发地/目的地
   *   transportMode                        → 卡片标签 + 抽屉
   *   departureDate/groupQuota             → 卡片 meta + 抽屉
   *   routeDetail                          → 卡片简介 + 抽屉详情
   *   daily                                → 抽屉「每日安排」（Day 1 / Day 2…）
   *
   * 不进表单的三类：
   *   attractionName/attractionLocation/viaRoad   这三个导游不录。
   *               游客页的卡片标签、「主要景点」「途经路段」三处因此对新线路只显示「待定」——
   *               是有意留白，不是漏了字段。
   *   days        行程天数由后端按 daily 行数推导（口径见 TourGuideScopeController），
   *               手填会与「每日安排」的条数打架
   *   guideNo/guideName/contactPhone/guideResume  由后端从登录态带出（stampGuide），
   *               真让前端传等于谁都能把联系方式改成别人的
   *   thumbsUpNum/crazilyNum/discussNum/storeUpNum 运营计数，不该手填
   *
   * 这个表单**没有直接落库这回事**：一提交后端就把 audit_status 盖成「待审核」，
   * 而游客端的 /travel_route/search 与 /detail 只放行「已通过」，
   * 所以在管理员点通过之前，游客的旅游线路页看不到这条线路。
   * 审核结论只读地列在 columns 里让导游看进度，不在 fields 中 —— 改不了。
   */
  travel_route: {
    title: '旅游线路',
    prefix: 'travel_route',
    kind: 'crud',
    search: [
      { prop: 'routeName', label: '线路名称' },
      { prop: 'guideNo', label: '导游工号' },
    ],
    columns: [
      { prop: 'routeImage', label: '图片', type: 'image' },
      { prop: 'routeName', label: '线路名称' },
      { prop: 'startPoint', label: '起点' },
      { prop: 'endPoint', label: '终点' },
      { prop: 'days', label: '行程天数' },
      { prop: 'transportMode', label: '交通方式' },
      { prop: 'routeFee', label: '线路费用', type: 'money' },
      { prop: 'groupQuota', label: '报团名额' },
      { prop: 'departureDate', label: '出发日期' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'routeName', label: '线路名称', required: true },
      { prop: 'routeImage', label: '图片路径', type: 'images' },
      // 下拉选项来自景点类型的真表（见 AdminCrud.loadOptions），与景点模块共用一套取值
      { prop: 'attractionType', label: '景点类型', type: 'select', optionsFrom: { prefix: 'attraction_type', key: 'attractionType' } },
      { prop: 'startPoint', label: '起点' },
      { prop: 'endPoint', label: '终点' },
      { prop: 'routeFee', label: '线路费用', type: 'number', min: 0, precision: 2 },
      // 名额为 0 等于这条线路永远约不上，不是合法状态，所以最小 1（库存那种 0=售罄不适用这里）
      { prop: 'groupQuota', label: '报团名额', type: 'number', min: 1, max: 9999, precision: 0 },
      // 必须是 datetime 而不是 date：实体上的 @JsonFormat 是 yyyy-MM-dd HH:mm:ss，
      // 只发 YYYY-MM-DD 会被 Jackson 判为 400，前端只看到「操作失败」
      { prop: 'departureDate', label: '出发日期', type: 'datetime' },
      { prop: 'transportMode', label: '交通方式' },
      { prop: 'routeDetail', label: '线路详情', type: 'textarea' },
      { prop: 'daily', label: '每日行程', type: 'days' },
    ],
  },

  group_tour: {
    title: '报团信息',
    prefix: 'group_tour',
    kind: 'order',
    payField: 'isPay',
    search: [
      { prop: 'routeName', label: '线路名称' },
      { prop: 'userName', label: '用户姓名' },
    ],
    columns: [
      { prop: 'routeImage', label: '图片', type: 'image' },
      { prop: 'routeName', label: '线路名称' },
      { prop: 'departureDate', label: '出发日期' },
      { prop: 'routeFee', label: '线路费用', type: 'money' },
      { prop: 'signupCount', label: '报名人数' },
      { prop: 'groupTourAmount', label: '报团金额', type: 'money' },
      { prop: 'guideNo', label: '导游工号' },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'userName', label: '用户姓名' },
      { prop: 'contactPhone', label: '联系方式' },
      { prop: 'groupTourTime', label: '报团时间' },
      { prop: 'isPay', label: '是否支付', type: 'tag', tagMap: PAY_TAG },
    ],
    fields: [
      { prop: 'routeName', label: '线路名称', required: true },
      { prop: 'routeImage', label: '图片路径', type: 'images' },
      { prop: 'departureDate', label: '出发日期', type: 'datetime' },
      { prop: 'routeFee', label: '线路费用', type: 'number', min: 0, precision: 2 },
      { prop: 'signupCount', label: '报名人数', type: 'number', min: 1, max: 999, precision: 0 },
      { prop: 'groupTourAmount', label: '报团金额', type: 'number', min: 0, precision: 2 },
      { prop: 'guideNo', label: '导游工号' },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'userName', label: '用户姓名' },
      { prop: 'contactPhone', label: '联系方式', type: 'phone' },
      { prop: 'groupTourTime', label: '报团时间', type: 'datetime' },
      { prop: 'isPay', label: '是否支付', type: 'select', options: PAY_OPTIONS },
    ],
  },

  /* ==================== 管理员（审核台） ====================
   *
   * 五个页面，都只审不改：noCreate 去掉「新增」，操作列只留「通过 / 驳回」。
   * 列只读展示，管理员改不动提交人的姓名与证件号 —— 后端也只认
   * audit_status / audit_reply 两个字段（见 UsersScopeController）。
   */

  /*
   * 实名认证。列表里的证件号是后端脱敏过的（前 6 后 4），
   * 完整号码不出后端，与 /user/identity/detail 的口径一致，所以这里读的是
   * idCardMasked 而不是 idCard。
   */
  user_identity: {
    title: '实名认证审核',
    prefix: 'users',
    kind: 'audit',
    auditField: 'auditStatus',
    replyField: 'auditReply',
    noCreate: true,
    search: [
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'realName', label: '真实姓名' },
    ],
    columns: [
      { prop: 'realName', label: '真实姓名' },
      { prop: 'idCardMasked', label: '身份证号' },
      { prop: 'userAccount', label: '用户账号' },
      { prop: 'addTime', label: '提交时间' },
      { prop: 'verifyTime', label: '认证通过时间' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'realName', label: '真实姓名', readonly: true },
      { prop: 'idCardMasked', label: '身份证号', readonly: true },
      { prop: 'userAccount', label: '用户账号', readonly: true },
      { prop: 'auditStatus', label: '审核状态', type: 'select', options: AUDIT_OPTIONS },
      { prop: 'auditReply', label: '审核回复', type: 'textarea' },
    ],
  },

  /*
   * 四类管理端资质。同一张 qualification 表、同一个 /users/qualification 接口，
   * 靠 endpoint + fixedQuery 分成四个页面：菜单项各自高亮、工作台各自计数。
   */
  // 页面键沿用前端的角色键 daoyou，fixedQuery 里的却是后端真表名 tour_guide
  qualification_daoyou: qualificationAudit('导游资质审核', 'tour_guide'),
  qualification_hotel_staff: qualificationAudit('酒店前台资质审核', 'hotel_staff'),
  qualification_attraction_staff: qualificationAudit('景点前台资质审核', 'attraction_staff'),
  qualification_restaurant_staff: qualificationAudit('餐厅前台资质审核', 'restaurant_staff'),

  /*
   * 线路审核。线路由导游在自己的管理端提交（提交时后端盖成「待审核」），
   * 管理员在这里通过 / 驳回。通过之后这条线路才会出现在游客「旅游线路」页 ——
   * /travel_route/search 只放行 audit_status = 已通过。
   *
   * 数据在 travel_route 表里，但接口挂在 /users/travel_route_audit 下：管理端页面
   * 的请求前缀恒定是管理员自己的表名（AdminCrud.adminPrefix() → users），
   * 与四类资质同一个理由。线路内容全部只读，管理员只改审核结论。
   */
  travel_route_audit: {
    title: '线路审核',
    prefix: 'users',
    endpoint: 'travel_route_audit',
    kind: 'audit',
    auditField: 'auditStatus',
    replyField: 'auditReply',
    noCreate: true,
    search: [
      { prop: 'routeName', label: '线路名称' },
      { prop: 'guideNo', label: '导游工号' },
      { prop: 'guideName', label: '导游姓名' },
    ],
    columns: [
      { prop: 'routeImage', label: '图片', type: 'image' },
      { prop: 'routeName', label: '线路名称' },
      { prop: 'startPoint', label: '起点' },
      { prop: 'endPoint', label: '终点' },
      { prop: 'days', label: '行程天数' },
      { prop: 'transportMode', label: '交通方式' },
      { prop: 'routeFee', label: '线路费用', type: 'money' },
      { prop: 'groupQuota', label: '报团名额' },
      { prop: 'departureDate', label: '出发日期' },
      { prop: 'guideNo', label: '导游工号' },
      { prop: 'guideName', label: '导游姓名' },
      { prop: 'addTime', label: '提交时间' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'routeName', label: '线路名称', readonly: true },
      { prop: 'startPoint', label: '起点', readonly: true },
      { prop: 'endPoint', label: '终点', readonly: true },
      { prop: 'days', label: '行程天数', readonly: true },
      { prop: 'routeFee', label: '线路费用', readonly: true },
      { prop: 'groupQuota', label: '报团名额', readonly: true },
      { prop: 'departureDate', label: '出发日期', readonly: true },
      { prop: 'transportMode', label: '交通方式', readonly: true },
      { prop: 'guideNo', label: '导游工号', readonly: true },
      { prop: 'guideName', label: '导游姓名', readonly: true },
      { prop: 'contactPhone', label: '联系方式', readonly: true },
      { prop: 'routeDetail', label: '线路详情', type: 'textarea', readonly: true },
      { prop: 'auditStatus', label: '审核状态', type: 'select', options: AUDIT_OPTIONS },
      { prop: 'auditReply', label: '审核回复', type: 'textarea' },
    ],
  },

  /*
   * 酒店信息审核。酒店由酒店前台在自己的管理端提交（提交时后端盖成「待审核」），
   * 管理员在这里通过 / 驳回。通过之后这家酒店才算营业 ——
   * /hotel_info/list、/detail/{id} 与它下面客房的 /room_type/list、/detail/{id}
   * 都只放行 audit_status = 已通过。
   *
   * 数据在 hotel_info 表里，但接口挂在 /users/hotel_info_audit 下：管理端页面的请求
   * 前缀恒定是管理员自己的表名（AdminCrud.adminPrefix() → users），与线路审核同一个理由。
   * 酒店内容全部只读，管理员只改审核结论。
   */
  hotel_info_audit: {
    title: '酒店信息审核',
    prefix: 'users',
    endpoint: 'hotel_info_audit',
    kind: 'audit',
    auditField: 'auditStatus',
    replyField: 'auditReply',
    noCreate: true,
    search: [
      { prop: 'hotelName', label: '酒店名称' },
      { prop: 'hotelAddress', label: '酒店地址' },
    ],
    columns: [
      { prop: 'hotelImage', label: '图片', type: 'image' },
      { prop: 'hotelName', label: '酒店名称' },
      { prop: 'hotelAddress', label: '酒店地址' },
      { prop: 'addTime', label: '提交时间' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'hotelName', label: '酒店名称', readonly: true },
      { prop: 'hotelAddress', label: '酒店地址', readonly: true },
      { prop: 'hotelIntro', label: '酒店介绍', type: 'textarea', readonly: true },
      { prop: 'auditStatus', label: '审核状态', type: 'select', options: AUDIT_OPTIONS },
      { prop: 'auditReply', label: '审核回复', type: 'textarea' },
    ],
  },

  /*
   * 景点信息审核。与酒店信息审核同一个形状（数据在 attraction 表里，接口挂在
   * /users/attraction_audit 下，理由见 UsersScopeController），管理员只改审核结论。
   *
   * 不进 fields 的两处是照 hotel_info_audit 的取舍：
   *   image        列表里已有缩略图，表单里再放一次没有意义；
   *   attractionType  前台那份表单用 optionsFrom 拉真表（见上面 attraction），
   *                 审核页全是只读字段，再拉一次 /attraction_type/list 纯属浪费。
   */
  attraction_audit: {
    title: '景点信息审核',
    prefix: 'users',
    endpoint: 'attraction_audit',
    kind: 'audit',
    auditField: 'auditStatus',
    replyField: 'auditReply',
    noCreate: true,
    search: [
      { prop: 'attractionName', label: '景点名称' },
      { prop: 'attractionType', label: '景点类型' },
    ],
    columns: [
      { prop: 'image', label: '图片', type: 'image' },
      { prop: 'attractionName', label: '景点名称' },
      { prop: 'attractionType', label: '景点类型' },
      { prop: 'ticketPrice', label: '门票价格', type: 'money' },
      { prop: 'openingHours', label: '开放时间' },
      { prop: 'attractionLocation', label: '景点位置' },
      { prop: 'addTime', label: '提交时间' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'attractionName', label: '景点名称', readonly: true },
      { prop: 'attractionType', label: '景点类型', readonly: true },
      { prop: 'openingHours', label: '开放时间', readonly: true },
      // 只读镜像只声明 precision，不声明 min/max：el-input-number 对 modelValue 的监听是
      // immediate 的，一旦声明了会越界的边界，弹窗一打开就会把越界的老值夹住并回写 ——
      // 只读字段本该一个字都不改，不能让它有这个副作用。
      { prop: 'ticketPrice', label: '门票价格', type: 'number', readonly: true, precision: 2 },
      { prop: 'quantity', label: '数量', type: 'number', readonly: true, precision: 0 },
      { prop: 'attractionLocation', label: '景点位置', readonly: true },
      { prop: 'attractionDescription', label: '景点介绍', type: 'textarea', readonly: true },
      { prop: 'auditStatus', label: '审核状态', type: 'select', options: AUDIT_OPTIONS },
      { prop: 'auditReply', label: '审核回复', type: 'textarea' },
    ],
  },

  /*
   * 餐厅信息审核。同上，数据在 restaurant 表里、接口挂在 /users/restaurant_audit 下。
   *
   * **别与 restaurant_reservation 混**：那个 resource（kind 也是 audit）审的是
   * 预约单上的 audit_status/audit_reply，审核人是餐厅前台；这里审的是餐厅主表内容，
   * 审核人是管理员。同名不同表，两套完全独立。
   *
   * restaurantImage 只进 columns 不进 fields，理由同 attraction_audit 的 image。
   */
  restaurant_audit: {
    title: '餐厅信息审核',
    prefix: 'users',
    endpoint: 'restaurant_audit',
    kind: 'audit',
    auditField: 'auditStatus',
    replyField: 'auditReply',
    noCreate: true,
    search: [{ prop: 'restaurantName', label: '餐厅名称' }],
    columns: [
      { prop: 'restaurantImage', label: '图片', type: 'image' },
      { prop: 'restaurantName', label: '餐厅名称' },
      { prop: 'signatureDish', label: '特色菜品' },
      { prop: 'businessHours', label: '营业时间' },
      { prop: 'avgCost', label: '人均消费', type: 'money' },
      { prop: 'restaurantAddress', label: '餐厅地址' },
      { prop: 'contactPhone', label: '联系电话' },
      { prop: 'addTime', label: '提交时间' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'restaurantName', label: '餐厅名称', readonly: true },
      { prop: 'signatureDish', label: '特色菜品', readonly: true },
      { prop: 'businessHours', label: '营业时间', readonly: true },
      // avgCost 库里是 varchar（存的是文案不是数字），这里只是照前台那份表单的 type 走
      { prop: 'avgCost', label: '人均消费', type: 'number', readonly: true, precision: 2 },
      { prop: 'restaurantAddress', label: '餐厅地址', readonly: true },
      { prop: 'contactPhone', label: '联系电话', readonly: true },
      { prop: 'restaurantIntro', label: '餐厅简介', type: 'textarea', readonly: true },
      { prop: 'auditStatus', label: '审核状态', type: 'select', options: AUDIT_OPTIONS },
      { prop: 'auditReply', label: '审核回复', type: 'textarea' },
    ],
  },
}

/**
 * 四类资质页面长的一样，只有标题与 tableName 不同，用一个工厂生成。
 *
 * fixedQuery 里放 tableName，是因为它同时是列表的过滤条件、也是「这类资质归谁」的标识；
 * 后端 qualification/page 的 likeOrEq 会把它拼成等值查询。
 */
function qualificationAudit(title, tableName) {
  return {
    title,
    prefix: 'users',
    endpoint: 'qualification',
    fixedQuery: { tableName },
    kind: 'audit',
    auditField: 'auditStatus',
    replyField: 'auditReply',
    noCreate: true,
    search: [
      { prop: 'realName', label: '真实姓名' },
      { prop: 'orgName', label: '所属单位' },
    ],
    columns: [
      { prop: 'realName', label: '真实姓名' },
      { prop: 'userAccount', label: '登录账号' },
      { prop: 'contactPhone', label: '联系方式' },
      { prop: 'orgName', label: '所属单位' },
      { prop: 'certNo', label: '资质编号' },
      { prop: 'qualification', label: '资质说明' },
      { prop: 'image', label: '证明材料', type: 'image' },
      { prop: 'addTime', label: '提交时间' },
      { prop: 'auditStatus', label: '审核状态', type: 'tag', tagMap: AUDIT_TAG },
      { prop: 'auditReply', label: '审核回复' },
    ],
    fields: [
      { prop: 'realName', label: '真实姓名', readonly: true },
      { prop: 'userAccount', label: '登录账号', readonly: true },
      { prop: 'contactPhone', label: '联系方式', readonly: true },
      { prop: 'orgName', label: '所属单位', readonly: true },
      { prop: 'certNo', label: '资质编号', readonly: true },
      { prop: 'qualification', label: '资质说明', type: 'textarea', readonly: true },
      { prop: 'auditStatus', label: '审核状态', type: 'select', options: AUDIT_OPTIONS },
      { prop: 'auditReply', label: '审核回复', type: 'textarea' },
    ],
  }
}

/*
 * 个人资料字段。账号列只读（它是登录用的唯一键，改了就登不进来了）；
 * 密码不在这里 —— 各角色的 Controller 都没有改密接口，要改密码走 /resetPass。
 */
export const PROFILE_FIELDS = {
  /*
   * 管理员（users 表）。这张表只有账号与角色两列，没有姓名/联系方式，
   * 所以全是只读 —— 页面照常能打开，但没什么可改的。
   * 管理员的职责是审核别人的资料，自己不需要提交资质（见 AdminProfile 的资质区）。
   */
  users: [
    { prop: 'username', label: '登录账号', readonly: true },
    { prop: 'role', label: '角色', readonly: true },
  ],
  daoyou: [
    { prop: 'guideNo', label: '导游工号（登录账号）', readonly: true },
    { prop: 'guideName', label: '导游姓名', required: true },
    { prop: 'avatar', label: '头像路径' },
    { prop: 'specialty', label: '专业领域' },
    { prop: 'languageSkill', label: '语言能力' },
    { prop: 'contactPhone', label: '联系方式', type: 'phone' },
    { prop: 'guideResume', label: '个人履历', type: 'textarea' },
  ],
  hotel_staff: [
    { prop: 'staffAccount', label: '登录账号', readonly: true },
    { prop: 'staffName', label: '姓名', required: true },
    { prop: 'avatar', label: '头像路径' },
    { prop: 'contactPhone', label: '联系方式', type: 'phone' },
    { prop: 'hotelName', label: '所属酒店名称' },
    { prop: 'hotelAddress', label: '所属酒店地址' },
  ],
  attraction_staff: [
    { prop: 'staffAccount', label: '登录账号', readonly: true },
    { prop: 'staffName', label: '姓名', required: true },
    { prop: 'avatar', label: '头像路径' },
    { prop: 'contactPhone', label: '联系方式', type: 'phone' },
    { prop: 'attractionName', label: '所属景点名称' },
    { prop: 'attractionLocation', label: '所属景点位置' },
  ],
  restaurant_staff: [
    { prop: 'staffAccount', label: '登录账号', readonly: true },
    { prop: 'staffName', label: '姓名', required: true },
    { prop: 'avatar', label: '头像路径' },
    { prop: 'contactPhone', label: '联系方式', type: 'phone' },
    { prop: 'restaurantName', label: '所属餐厅名称' },
    { prop: 'restaurantAddress', label: '所属餐厅地址' },
  ],
}
