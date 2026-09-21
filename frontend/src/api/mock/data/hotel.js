import { hashSeed } from '@/common/scenes'

/*
 * 酒店 mock 数据。
 *
 * ⚠️ 这份数据已经过期了，而且**走不到**：hotel_info / room_type / hotel_reservation /
 * hotel_comment 四个前缀都在 client.js 的 REAL_PREFIXES 里，酒店这块永远打真接口。
 * 真实模型是「酒店 / 客房」两张表（hotel_info 一家酒店一行 + 审核三列，
 * room_type 是客房、带 hotel_name 归属列），下面还停在旧的「hotel_info 一行 = 一个房型」
 * 上。留着只是因为 client.js 说「本轮先不删」；真要恢复 mock 兜底，先把这里
 * 改成两张表，并同步 mock/index.js 的 LIST_OPTS / SOURCES。
 *
 * 真表 room_type.id 从 71 起，这里对齐。
 */

/** 房型字典。真实库里是「客房类型1..8」这种占位值 */
export const ROOM_TYPES = [
  '大床房',
  '双床房',
  '亲子套房',
  '商务套房',
  '观景房',
  '温泉房',
  '民宿木屋',
  '青年旅舍',
]

const HOTELS = [
  {
    name: '西宁青海湖大酒店',
    address: '青海省西宁市城中区七一路 188 号',
    photo: 4,
    blurb: '位于市中心七一路，步行可达莫家街夜市，是环湖出发的常见落脚点。',
  },
  {
    name: '敦煌沙洲驿精品酒店',
    address: '甘肃省敦煌市沙州镇阳关中路 56 号',
    photo: 1,
    blurb: '紧邻沙洲夜市，出门就是小吃一条街，到鸣沙山车程十五分钟。',
  },
  {
    name: '张掖丹霞国际饭店',
    address: '甘肃省张掖市甘州区县府街 21 号',
    photo: 3,
    blurb: '高铁站与丹霞景区之间的主干道上，去七彩丹霞约四十分钟车程。',
  },
  {
    name: '西安钟楼云栖酒店',
    address: '陕西省西安市碑林区南大街 12 号',
    photo: 3,
    blurb: '距钟楼两百米，地铁二号线出口就在楼下，去回民街步行十分钟。',
  },
  {
    name: '乌鲁木齐天山雪莲酒店',
    address: '新疆乌鲁木齐市天山区人民路 302 号',
    photo: 1,
    blurb: '邻大巴扎，去天山天池的一日游车辆多在此集合。',
  },
  {
    name: '兰州黄河之滨宾馆',
    address: '甘肃省兰州市城关区南滨河东路 509 号',
    photo: 4,
    blurb: '临黄河而建，中山桥与白塔山都在步行范围内，晚上沿河散步很舒服。',
  },
]

const ROOMS = [
  {
    name: '高级大床房',
    type: '大床房',
    price: 328,
    desc: '一张一米八的大床，房间约二十八平方米，配工作台与落地窗。适合单人或情侣出行，窗外多为城市街景。',
    facility: '免费Wi-Fi,空调,24小时热水,独立卫浴,免费停车',
  },
  {
    name: '豪华双床房',
    type: '双床房',
    price: 468,
    desc: '两张一米三五的单人床，房间约三十五平方米，带独立会客区。适合朋友同行或带稍大的孩子入住，加床需提前告知。',
    facility: '免费Wi-Fi,空调,24小时热水,独立卫浴,免费停车,含双早',
  },
  {
    name: '家庭亲子套房',
    type: '亲子套房',
    price: 888,
    desc: '一室一厅，主卧大床加儿童房上下铺，配儿童洗漱用品和小帐篷。客厅有餐桌，可以在房间里解决早饭，适合三口之家。',
    facility: '免费Wi-Fi,空调,24小时热水,独立卫浴,免费停车,含双早,儿童用品',
  },
]

/** 房型图按酒店配图偏移，保证同一家酒店的三个房型图不重样 */
function roomImages(photo, i) {
  const n = 4
  const start = (photo + i) % n
  return [0, 1, 2].map((k) => `/pictures/lun_bo_tu_${((start + k) % n) + 1}.png`).join(',')
}

export const HOTEL_ROOMS = HOTELS.flatMap((hotel, hi) =>
  ROOMS.map((room, ri) => {
    const id = 71 + hi * ROOMS.length + ri
    const h = hashSeed(`hotel:${id}`)
    const p = (n) => String(n).padStart(2, '0')
    const d = new Date(Date.now() - (h % 300) * 86400000)
    const addTime = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(h % 24)}:${p((h >> 3) % 60)}:00`
    // 同一家酒店的三个房型价格拉开档次，且各酒店同房型价格不同
    const price = room.price + hi * 26 + ri * 8

    return {
      id,
      roomName: room.name,
      roomImage: roomImages(hotel.photo, ri),
      roomType: room.type,
      roomFacility: room.facility,
      roomPrice: price,
      roomCount: 3 + (h % 26),
      roomDescription: `${hotel.blurb}${room.desc}`,
      hotelName: hotel.name,
      hotelAddress: hotel.address,
      clickTime: addTime,
      clickNum: 40 + (h % 380),
      discussNum: 2 + ((h >> 6) % 38),
      storeUpNum: 20 + ((h >> 10) % 220),
      addTime,
    }
  }),
)

export const ROOM_TYPE_ROWS = ROOM_TYPES.map((t, i) => ({
  id: 61 + i,
  roomType: t,
  image: `/pictures/lun_bo_tu_${(i % 4) + 1}.png`,
  addTime: `2026-0${(i % 9) + 1}-1${i % 9} 10:00:00`,
}))

/** 评论由 buildComments(refId) 按需生成，见 mock/index.js */
export const HOTEL_COMMENT_TABLE = 'hotel_comment'
