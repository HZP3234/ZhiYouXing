import { hashSeed } from '@/common/scenes'

/*
 * 评论数据生成器。三张评论表（attraction_comment / hotel_comment / restaurant_comment）
 * 结构完全一样，只有表名不同，所以共用这一个生成器。
 *
 * 关键点：用 refId 当随机种子，且用「线性同余」而不是 Math.random ——
 * 同一条数据每次生成的评论、评分、昵称必须完全一致，
 * 否则每次刷新详情页评分都在变，看起来像 bug。
 *
 * 三张评论表现在库里都有真数据（景点 393 条、酒店 72 条、餐厅 76 条）。
 * 景点详情页的评论已经切到真接口（见 client.js 的 REAL_READ_PREFIXES），
 * 酒店与餐厅这一轮没切，所以还在用这里生成的 —— 两边共用一个文案池，
 * 所以看起来是同一套口径。
 *
 * 每条的条数由 commentCountOf 决定（3~5 条），它和 zhiyouxing.sql 里那批种子
 * 是同一个公式算出来的。景点表的 discuss_num 也取这个值，所以卡片上写「N 条讨论」，
 * 点进去就是 N 条 —— 改这里的公式，SQL 那边要一起重新生成。
 */

const NICKNAMES = [
  '旅行的猫',
  '西北偏北',
  '一路向晴',
  '老张看世界',
  '小满未满',
  '大漠孤烟',
  '背包客阿May',
  '山与海之间',
  '慢半拍先生',
  '云上散步',
  '摄影师老王',
  '爱吃辣的兔子',
  '走走停停',
  '半个当地人',
]

/** 按评分分档。混着写才像真的 —— 全是五星反而假 */
const PHRASES = {
  5: [
    '讲解很有意思，拍了一下午照片，出片率极高。',
    '比想象中震撼，值得专门跑一趟。',
    '早上来的，人不多，光线也好，体验非常好。',
    '带爸妈来的，他们比我还兴奋，全程走了三个多小时。',
    '现场比照片好看，照片拍不出那种层次感。',
    '安排得很舒服，不用赶，慢慢逛刚好一天。',
    '第二次来了，还是觉得值。',
    '工作人员很热心，问路都耐心指。',
  ],
  4.5: [
    '整体很满意，就是停车稍微绕了一圈。',
    '风景没得说，建议穿舒服的鞋，要走不少路。',
    '很出片，就是风有点大，注意带件外套。',
    '内容很丰富，一天时间有点紧，建议留两天。',
    '值得来，性价比不错。',
  ],
  4: [
    '景色值得，就是旺季人有点多，建议早上八点前到。',
    '挺好的，交通再方便一点就完美了。',
    '总体满意，卫生间和休息区可以再多一些。',
    '淡季来体验会好很多，旺季排队时间偏长。',
    '还不错，符合预期。',
  ],
  3.5: [
    '门票偏贵，体验还可以。',
    '期待值拉太高了，实际一般，但还是值得看看。',
    '景色可以，配套一般，餐饮选择比较少。',
    '人多的时候体验会打折扣，尽量错峰。',
  ],
}

const REPLIES = [
  '感谢您的认可，欢迎下次再来！',
  '给您带来的不便我们深表歉意，已反馈给现场管理部门。',
  '感谢您的建议，我们会在旺季增加引导人员。',
  '谢谢支持，期待与您再次相遇。',
  '已记录您的意见，祝您旅途愉快。',
  '感谢反馈，相关问题正在改进中。',
]

/** 线性同余，给一个种子就得到可复现的伪随机序列 */
function makeRng(seed) {
  let s = seed >>> 0 || 1
  return () => {
    s = (Math.imul(s, 1664525) + 1013904223) >>> 0
    return s / 4294967296
  }
}

const pick = (rng, arr) => arr[Math.floor(rng() * arr.length) % arr.length]

const SCORES = [5, 5, 4.5, 4.5, 4, 4, 3.5]

/**
 * 某个 refId 会生成几条评论，3~5 条。
 *
 * 单独抽出来是给 attraction 的 discussNum 用 —— 它需要「条数」而不想真的
 * 把评论造出来。注意它内部另起了一个 rng，和 buildComments 里那个互不影响，
 * 两边算出的数字相同只是因为公式一致。
 */
export function commentCountOf(refId, table = 'comment') {
  return 3 + Math.floor(makeRng(hashSeed(`${table}:${refId}`))() * 3)
}

/**
 * 生成某条数据的评论。
 * @param {number|string} refId  关联的记录 id
 * @param {string} table         评论表名，用于拼 id 前缀（只是为了让 id 看起来不像假的）
 */
export function buildComments(refId, table = 'comment') {
  const rng = makeRng(hashSeed(`${table}:${refId}`))
  // 第一抽留给条数：后面每一步都踩着这个序列，写成 commentCountOf(...) 会少消耗一个数，
  // 生成出来的评论就全错位了 —— 所以这里必须自己 rng() 一次。
  const count = 3 + Math.floor(rng() * 3) // 3~5 条

  const now = Date.now()
  const list = []

  for (let i = 0; i < count; i++) {
    const score = pick(rng, SCORES)
    // 只有约 1/3 的评论有商家回复 —— 真实感来自不整齐
    const hasReply = rng() < 0.34
    // 越靠前的评论越新，最大 120 天前
    const daysAgo = Math.floor(rng() * 120) + i * 3
    const at = new Date(now - daysAgo * 86400000 - Math.floor(rng() * 86400000))

    list.push({
      id: hashSeed(`${table}:${refId}:${i}`) % 900000 + 100000,
      refId: Number(refId),
      userId: hashSeed(`u:${pick(rng, NICKNAMES)}`) % 900 + 100,
      avatarUrl: `upload/avatar${(i % 8) + 1}.jpg`, // 必然 404，由 SafeImage 退化成首字头像
      nickname: pick(rng, NICKNAMES),
      content: pick(rng, PHRASES[score]),
      score,
      reply: hasReply ? pick(rng, REPLIES) : '',
      addTime: `${at.getFullYear()}-${String(at.getMonth() + 1).padStart(2, '0')}-${String(at.getDate()).padStart(2, '0')} ${String(at.getHours()).padStart(2, '0')}:${String(at.getMinutes()).padStart(2, '0')}:00`,
    })
  }

  // 新的在前，和 sort=addTime&order=desc 的结果一致
  list.sort((a, b) => b.addTime.localeCompare(a.addTime))
  return list
}
