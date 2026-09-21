/*
 * mock 查询引擎：复刻后端 MPUtil 的筛选/排序/分页语义。
 *
 * 目的是让「关掉 useMock」这个动作不改变任何页面行为 —— 同样的参数，
 * mock 和真接口必须给出同样的结果形状。所以这里刻意照着后端的实现来写，
 * 包括它的一些怪癖（见下面 avgCost 的注释），而不是「顺手做对一点」。
 *
 * 返回的信封也和 axios 一模一样：{ data: { code, msg, data } }。
 * 页面因此两种模式下都写 res.data.code === 0，不用分叉。
 */

/** 这些键是分页/排序参数，不是查询条件 */
const SKIP_KEYS = new Set(['page', 'limit', 'sort', 'order', 'currPage', 'pageSize'])

export const ok = (data, msg = 'success') => ({ data: { code: 0, msg, data } })

export const fail = (msg, code = 500) => ({ data: { code, msg, data: null } })

/** 模拟网络往返。故意留 200ms+：mock 秒回会掩盖加载态和竞态的 bug */
export const delay = (ms = 240) => new Promise((resolve) => setTimeout(resolve, ms))

const like = (haystack, needle) => String(haystack ?? '').toLowerCase().includes(String(needle).toLowerCase())

/**
 * 排序：数字列按数值比，字符串列按字典序。
 *
 * 字典序是有意为之 —— 后端 MPUtil 对 avgCost（varchar）就是这么排的，
 * '100' 会被排在 '88' 前面。餐厅列表因此不提供「按人均排序」这一项，
 * 但万一传了，mock 的表现要和后端一致，否则切过去才发现结果对不上。
 */
function compare(a, b, field) {
  const va = a[field]
  const vb = b[field]
  const na = Number(va)
  const nb = Number(vb)
  if (Number.isFinite(na) && Number.isFinite(nb) && va !== '' && vb !== '') {
    return na - nb
  }
  return String(va ?? '').localeCompare(String(vb ?? ''))
}

/**
 * @param {Array}  rows    全量数据
 * @param {Object} params  查询参数（就是页面上传给接口的那个对象）
 * @param {Object} opts
 *   - likeFields  强制走模糊匹配的字段（后端对实体里的字符串列一律 likeOrEq）
 *   - rangeMap    'ticketPriceStart' 这类参数映射到的列名，如 { ticketPrice: 'ticketPrice' }
 *   - nameField   关键词参数 indexQueryCondition 落到哪个列
 *   - defaultSort 不传 sort 时的排序列
 */
export function mockPage(rows, params = {}, opts = {}) {
  const { likeFields = [], rangeMap = {}, nameField = '', defaultSort = 'addTime', defaultOrder = 'desc' } = opts
  let list = rows.slice()

  for (const [key, raw] of Object.entries(params)) {
    if (SKIP_KEYS.has(key)) continue
    if (raw === '' || raw === null || raw === undefined) continue

    // 首页搜索框带来的关键词。后端完全不认这个参数（MPUtil 只认实体字段名），
    // 真接口下由列表页翻译成 attractionName= / roomName= / restaurantName=。
    if (key === 'indexQueryCondition') {
      if (nameField) list = list.filter((r) => like(r[nameField], raw))
      continue
    }

    // xxxStart / xxxEnd → 闭区间，对应 MPUtil 的 between
    const range = key.match(/^(.*?)(Start|End)$/)
    if (range) {
      const base = range[1]
      const field = rangeMap[base] || base
      if (!(field in (rows[0] || {}))) continue
      const bound = Number(raw)
      if (!Number.isFinite(bound)) continue
      list =
        range[2] === 'Start'
          ? list.filter((r) => Number(r[field]) >= bound)
          : list.filter((r) => Number(r[field]) <= bound)
      continue
    }

    // 其余键当作实体字段查询：字符串模糊、数字等值
    if (!(key in (rows[0] || {}))) continue
    const sample = rows[0][key]
    if (typeof sample === 'number') {
      const bound = Number(raw)
      if (Number.isFinite(bound)) list = list.filter((r) => Number(r[key]) === bound)
    } else if (likeFields.includes(key) || typeof sample === 'string') {
      list = list.filter((r) => like(r[key], raw))
    }
  }

  // 排序
  const sortField = params.sort || defaultSort
  const order = (params.order || defaultOrder).toLowerCase()
  if (sortField && sortField in (rows[0] || {})) {
    list.sort((a, b) => (order === 'desc' ? compare(b, a, sortField) : compare(a, b, sortField)))
  }

  // 分页
  const page = Math.max(1, parseInt(params.page ?? params.currPage ?? 1, 10) || 1)
  const limit = Math.max(1, parseInt(params.limit ?? params.pageSize ?? 10, 10) || 10)
  const totalCount = list.length
  const start = (page - 1) * limit

  return {
    list: list.slice(start, start + limit),
    totalCount,
    pageSize: limit,
    totalPage: Math.ceil(totalCount / limit),
    currPage: page,
  }
}

/** 不带分页的列表（分类字典这类接口返回的就是全量 list） */
export function mockList(rows) {
  return { list: rows.slice(), totalCount: rows.length }
}

export function mockDetail(rows, id) {
  const target = rows.find((r) => String(r.id) === String(id))
  return target || null
}

/**
 * 下单落到 localStorage，键名就是后端的表名。
 * 这同时是字段名的自检工具：提交完看一眼 localStorage['mock:ticket_order']，
 * 就能发现 orderNo 有没有被误写成 reservationNo。
 */
export function mockSave(table, entity) {
  const key = `mock:${table}`
  let saved = []
  try {
    saved = JSON.parse(localStorage.getItem(key) || '[]')
  } catch (e) {
    saved = []
  }
  const record = { ...entity, id: Date.now(), addTime: new Date().toLocaleString('sv-SE').replace('T', ' ') }
  saved.push(record)
  localStorage.setItem(key, JSON.stringify(saved))
  return record
}
