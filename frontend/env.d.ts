/// <reference types="vite/client" />

// swiper 只发布了 CSS 文件本身、没有配套的 .d.ts，
// 侧-effect 导入时 vue-tsc 会报 TS2882，这里显式声明一下。
declare module 'swiper/css'
declare module 'swiper/css/navigation'
declare module 'swiper/css/pagination'
