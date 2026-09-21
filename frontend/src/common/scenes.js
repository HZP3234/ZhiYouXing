/*
 * 首页那套内联 SVG 图标与风景画，抽到这里给全站复用（home.vue 也从这里引）。
 *
 * 为什么风景画要转成 data URI 而不是直接 v-html：
 * 列表页一屏有 9~12 张卡片，每张都要一张兜底图。若各自 v-html 一份内联 SVG，
 * DOM 里会有十几份 <linearGradient id="ban-danxia-sky">，ID 重复（浏览器只认第一个），
 * 而且节点数白白翻十倍。转成 data URI 塞进 <img src> 后，每张图是独立的图片资源，
 * 没有 ID 冲突，也能直接用 object-fit: cover 裁切。
 */

/** 24×24 线性图标，stroke 用 currentColor，颜色交给外层 CSS */
const svg = (paths) =>
  `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${paths}</svg>`

export const icons = {
  attraction: svg(
    '<path d="M3 19.5l5.6-8.4 3.9 5.6 2.6-3.6L21 19.5H3z"/><circle cx="16.8" cy="6.4" r="2.3"/>',
  ),
  hotel: svg('<rect x="5" y="3" width="14" height="18" rx="2"/><path d="M9 7.5h2M13 7.5h2M9 12h2M13 12h2M9.5 17h5"/>'),
  restaurant: svg(
    '<path d="M4 11h16a8 8 0 01-8 8 8 8 0 01-8-8z"/><path d="M9.2 8.2c0-1.6 1-2.1 1-3.7M13.4 8.2c0-1.6 1-2.1 1-3.7"/>',
  ),
  route: svg(
    '<circle cx="6" cy="6" r="2.6"/><circle cx="18" cy="18" r="2.6"/><path d="M8.6 6H14a4 4 0 010 8h-4a4 4 0 000 4h5.4"/>',
  ),
  guide: svg('<path d="M4 5.5A2.5 2.5 0 016.5 3H19v18H6.5A2.5 2.5 0 014 18.5v-13z"/><path d="M8.5 7.5h6M8.5 11.5h4"/>'),
  agent: svg(
    '<path d="M11 3.2l1.7 4.4 4.4 1.7-4.4 1.7L11 15.4 9.3 11 4.9 9.3l4.4-1.7L11 3.2z"/><path d="M17.6 14.4l.9 2.3 2.3.9-2.3.9-.9 2.3-.9-2.3-2.3-.9 2.3-.9.9-2.3z"/>',
  ),
  search: svg('<circle cx="10.8" cy="10.8" r="6.2"/><path d="M15.4 15.4L20.5 20.5"/>'),
  users: svg(
    '<circle cx="9.2" cy="8" r="3.3"/><path d="M3.6 20a5.6 5.6 0 0111.2 0"/><path d="M16.2 5.3a3.3 3.3 0 010 5.5M18 20a5.6 5.6 0 00-2-4.3"/>',
  ),
  star: svg('<path d="M12 3.6l2.6 5.3 5.9.9-4.3 4.1 1 5.9-5.2-2.8-5.2 2.8 1-5.9-4.3-4.1 5.9-.9L12 3.6z"/>'),
  arrow: svg('<path d="M4.6 12h14.2M13 6.2l5.8 5.8-5.8 5.8"/>'),
  /* 下面几个是首页没有、新页面要用的 */
  clock: svg('<circle cx="12" cy="12" r="8.6"/><path d="M12 7.2V12l3.2 2"/>'),
  pin: svg('<path d="M12 21s6.4-5.6 6.4-10.4A6.4 6.4 0 105.6 10.6C5.6 15.4 12 21 12 21z"/><circle cx="12" cy="10.4" r="2.4"/>'),
  phone: svg('<path d="M6.4 3.6h3l1.5 3.8-2 1.4a12 12 0 005.9 5.9l1.4-2 3.8 1.5v3a2 2 0 01-2.2 2A16.4 16.4 0 014.4 5.8a2 2 0 012-2.2z"/>'),
  like: svg('<path d="M7.4 20.4V9.6l4.2-6.2a2 2 0 013 2.4l-1.2 4h4.3a2 2 0 011.9 2.5l-1.7 6.7a2.4 2.4 0 01-2.3 1.8H7.4z"/><path d="M7.4 20.4H4.2V9.6h3.2"/>'),
  comment: svg('<path d="M20.4 12.6a7.6 7.6 0 01-8.2 7.6 8.6 8.6 0 01-2.7-.5L4.2 21l1.3-4.4a7.6 7.6 0 01-.9-3.6 7.6 7.6 0 018.2-7.6 7.6 7.6 0 017.6 7.2z"/>'),
  calendar: svg('<rect x="3.6" y="5.2" width="16.8" height="15.2" rx="2.4"/><path d="M3.6 10h16.8M8.4 3.4v3.6M15.6 3.4v3.6"/>'),
  /* 床（侧视：左床头板 + 床垫线 + 右床尾 + 枕头）。酒店那两页用它标「N 个房型 · 剩 M 间」 */
  bed: svg('<path d="M3.2 19.6V5.4"/><path d="M3.2 13.2h17.6v6.4"/><path d="M20.8 13.2v-2a2 2 0 00-2-2h-6.6"/><circle cx="7.4" cy="10.6" r="2.2"/>'),
  check: svg('<path d="M4.8 12.6l4.8 4.8 9.6-10.8"/>'),
  ticket: svg('<path d="M3.6 9.2V6.8a1.6 1.6 0 011.6-1.6h13.6a1.6 1.6 0 011.6 1.6v2.4a2.8 2.8 0 000 5.6v2.4a1.6 1.6 0 01-1.6 1.6H5.2a1.6 1.6 0 01-1.6-1.6v-2.4a2.8 2.8 0 000-5.6z"/><path d="M13.6 5.2v13.6" stroke-dasharray="2.6 2.6"/>'),
}

/** 全部风景画共用的画布：1600×600，slice 保证填满任意比例的容器 */
const scene = (inner) =>
  `<svg viewBox="0 0 1600 600" preserveAspectRatio="xMidYMid slice" xmlns="http://www.w3.org/2000/svg" style="display:block;width:100%;height:100%">${inner}</svg>`

export const scenes = {
  first: scene(`
    <defs>
      <linearGradient id="ban-danxia-sky" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#eef7fe"/><stop offset="1" stop-color="#d4e7f8"/>
      </linearGradient>
    </defs>
    <rect width="1600" height="600" fill="url(#ban-danxia-sky)"/>
    <circle cx="1290" cy="146" r="88" fill="#bfdff5" opacity="0.9"/>
    <path d="M0 344C200 262 372 306 540 344s312 44 486-10 356-38 574 26v240H0z" fill="#a9d5e8"/>
    <path d="M0 396C214 322 396 366 560 400s320 38 496-14 342-32 544 30v184H0z" fill="#7fb6de"/>
    <path d="M0 448C230 388 404 424 576 452s318 32 494-16 340-26 530 34v130H0z" fill="#4f92cd"/>
    <path d="M0 500C252 456 428 484 604 506s316 22 486-18 336-22 510 34v78H0z" fill="#346fb0"/>
    <path d="M0 548C280 520 460 540 640 556s320 12 480-20 330-16 480 36v28H0z" fill="#1c3d68"/>
  `),

  second: scene(`
    <defs>
      <linearGradient id="ban-mogao-sky" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#eef7fe"/><stop offset="1" stop-color="#d9e9f7"/>
      </linearGradient>
      <path id="ban-mogao-niche" d="M0 76V24a22 22 0 0 1 44 0v52z"/>
    </defs>
    <rect width="1600" height="600" fill="url(#ban-mogao-sky)"/>
    <circle cx="320" cy="130" r="72" fill="#c9e2f7" opacity="0.85"/>
    <path d="M0 300C160 266 320 266 480 300s320 32 480 0 320-32 480 0h160v300H0z" fill="#a7c0d6"/>
    <path d="M0 300h1600v300H0z" fill="#93aec6"/>
    <g fill="#3e6183" opacity="0.5">
      <use href="#ban-mogao-niche" x="200" y="330"/>
      <use href="#ban-mogao-niche" x="330" y="330"/>
      <use href="#ban-mogao-niche" x="460" y="330"/>
      <use href="#ban-mogao-niche" x="590" y="330"/>
      <use href="#ban-mogao-niche" x="720" y="330"/>
      <use href="#ban-mogao-niche" x="850" y="330"/>
      <use href="#ban-mogao-niche" x="980" y="330"/>
      <use href="#ban-mogao-niche" x="1110" y="330"/>
    </g>
    <g fill="#2f4d6b" opacity="0.42">
      <use href="#ban-mogao-niche" x="265" y="456"/>
      <use href="#ban-mogao-niche" x="395" y="456"/>
      <use href="#ban-mogao-niche" x="525" y="456"/>
      <use href="#ban-mogao-niche" x="655" y="456"/>
      <use href="#ban-mogao-niche" x="785" y="456"/>
      <use href="#ban-mogao-niche" x="915" y="456"/>
      <use href="#ban-mogao-niche" x="1045" y="456"/>
    </g>
    <path d="M0 298h1600" stroke="#7d99b1" stroke-width="3" opacity="0.35"/>
  `),

  third: scene(`
    <defs>
      <linearGradient id="ban-mingsha-sky" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#eaf5fe"/><stop offset="1" stop-color="#d0e5f8"/>
      </linearGradient>
    </defs>
    <rect width="1600" height="600" fill="url(#ban-mingsha-sky)"/>
    <circle cx="1190" cy="168" r="76" fill="#cfe6f8" opacity="0.95"/>
    <path d="M0 296C260 206 520 258 760 302s420 52 840-16v314H0z" fill="#a3c9e8"/>
    <path d="M0 382C300 322 560 372 820 402s440 26 780-34v232H0z" fill="#7fadd6"/>
    <path d="M0 472C320 432 620 468 900 486s420-4 700-30v144H0z" fill="#5b8fbe"/>
    <path d="M690 468a170 128 0 0 1 340 0 250 190 0 0 0-340 0z" fill="#12808d" opacity="0.92"/>
    <path d="M840 448c26-13 56-13 82 0" stroke="#e2f2f4" stroke-width="4" fill="none" opacity="0.5" stroke-linecap="round"/>
  `),

  fourth: scene(`
    <defs>
      <linearGradient id="ban-qinghai-sky" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#f2f9fe"/><stop offset="1" stop-color="#dcecfa"/>
      </linearGradient>
      <linearGradient id="ban-qinghai-water" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#3f9ecb"/><stop offset="1" stop-color="#14648f"/>
      </linearGradient>
    </defs>
    <rect width="1600" height="600" fill="url(#ban-qinghai-sky)"/>
    <circle cx="1250" cy="146" r="78" fill="#cfe4f8" opacity="0.9"/>
    <path d="M0 300L240 176 430 292 620 190 820 300z" fill="#a9c8de" opacity="0.85"/>
    <path d="M600 300L880 158 1080 284 1280 196 1600 300z" fill="#8db4d2" opacity="0.78"/>
    <path d="M0 300h1600v300H0z" fill="url(#ban-qinghai-water)"/>
    <g stroke="#ffffff" stroke-width="3" stroke-linecap="round" fill="none" opacity="0.3">
      <path d="M200 382c40-16 80-16 120 0"/>
      <path d="M420 442c50-18 100-18 150 0"/>
      <path d="M1000 400c44-16 88-16 132 0"/>
      <path d="M1240 472c50-18 100-18 150 0"/>
    </g>
    <g stroke="#2f5f88" stroke-width="4" stroke-linecap="round" fill="none" opacity="0.5">
      <path d="M1180 132c14-14 28-14 42 0"/>
      <path d="M1240 104c12-12 24-12 36 0"/>
      <path d="M1252 158c10-10 20-10 30 0"/>
    </g>
  `),
}

export const SCENE_KEYS = ['first', 'second', 'third', 'fourth']

/* 同一幅画横向挪一挪裁切窗口，观感就换了一种；4 幅 × 3 位移 = 12 种，网格里看不出重复 */
const SCENE_POSITIONS = ['18% 50%', '50% 50%', '82% 50%']

/** 字符串 → 稳定的非负整数。同一 seed 每次得到同一个值，刷新页面评分/配图不会变 */
export function hashSeed(seed) {
  const s = String(seed ?? '')
  let h = 2166136261
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return Math.abs(h)
}

/** 按 seed 挑定一幅风景画 */
export function sceneKey(seed) {
  return SCENE_KEYS[hashSeed(seed) % SCENE_KEYS.length]
}

const uriCache = new Map()

/** 风景画的 data URI，可直接塞进 <img src> */
export function sceneUri(seed) {
  const key = sceneKey(seed)
  if (!uriCache.has(key)) {
    uriCache.set(key, 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(scenes[key]))
  }
  return uriCache.get(key)
}

/** 风景画 + 裁切位置，图片兜底时用 */
export function pickScene(seed) {
  const h = hashSeed(seed)
  return {
    key: sceneKey(seed),
    uri: sceneUri(seed),
    position: SCENE_POSITIONS[(h >> 3) % SCENE_POSITIONS.length],
  }
}
