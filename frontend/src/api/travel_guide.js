import { get, detail, post } from './client'

const PREFIX = 'travel_guide'
const TAG_PREFIX = 'travel_guide_tag'

/*
 * 攻略是「用户写的内容」，所以这套接口的形状跟景点/酒店那套不一样：
 *
 *   - 列表**不走 /list 而走 /search**。/list 是代码生成器产物，参数经 MPUtil 裸反射
 *     映射到实体字段上，tagId / keyword 都不是 travel_guide 的列，会被静默忽略 ——
 *     传了等于没传，页面看着正常但筛选根本没生效。这两个条件只有在 /search 里才认。
 *   - 写走 /save 与 /update，请求体是 TravelGuideForm，**不带 userAccount / userName /
 *     addTime**：作者与时间由服务端从登录态取（框架那套「前端传作者、后端存下来」的
 *     老写法在这里是伪造漏洞，所以从一开始就没照抄）。
 *   - /detail/{id} 返回的对象比实体多一个 tagNames 数组（可能为空数组）。
 *
 * 返回信封与别处完全一致：res.data.data.list / .totalCount（PageUtils）。
 */

/**
 * 列表页唯一入口。@IgnoreAuth 免登录。
 *
 * params 认这几种（null / 空串一律别发，后端按「没传」处理）：
 *   tagId=3                        按标签筛（标签 id 来自 listGuideTags）
 *   keyword=茶卡盐湖                标题**或正文**里出现即命中，不用加 %
 *   page=1&limit=6
 *   sort=addTime&order=desc        按发布时间倒序（sort 传 camelCase，后端自己转）
 */
export const searchGuides = (params) => get(PREFIX, 'search', params)

/** 详情。@IgnoreAuth 免登录 */
export const getGuide = (id) => detail(PREFIX, id)

/**
 * 我的攻略。走后端 /page —— 那条会拿登录态里的账号去过滤，
 * 所以**不要**自己传 userAccount（后端的过滤条件是以会话为准拼的）。
 * 需要 header Token。
 */
export const listMyGuides = (params) => get(PREFIX, 'page', params)

/** 发布。body 字段对齐 TravelGuideForm，见上方注释。需要 Token */
export const saveGuide = (body) => post(PREFIX, 'save', body)

/**
 * 修改。body 里必须带 id，否则后端回「参数不完整」。
 */
export const updateGuide = (body) => post(PREFIX, 'update', body)

/**
 * 删除。body 直接是 id 数组（不是 { ids: [...] }）。
 * 后端整批校验：只要有一篇不是自己的，这一批一篇都不删。
 */
export const deleteGuides = (ids) => post(PREFIX, 'delete', ids)

/** 标签字典，给筛选 chips 与写作页的下拉用。带每篇标签下的攻略数。@IgnoreAuth */
export const listGuideTags = () => get(TAG_PREFIX, 'list')
