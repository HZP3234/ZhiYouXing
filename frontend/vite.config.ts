import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

/*
 * 四个业务模块的 HTTP 端口。默认直连它们，而不是走网关 ——
 * 网关的 uri 是 lb://zhiyouxing-xxx，寻址依赖 Nacos，而 Nacos 跑在 VM 里。
 * VM 没开机时网关自己能起来，但每一次转发都是 503。
 * 直连模式下用不到服务发现，各服务监听什么端口就打什么端口。
 */
const DIRECT_PORT = {
  user: 8081,
  attraction: 8082,
  hotel: 8083,
  travel: 8084,
  food: 8085,
} as const

/** 生成一组「把 /api/xxx/** 转到某个本地端口、并去掉 /api 前缀」的代理项（对齐网关的 StripPrefix=1） */
function directProxy(port: number, prefixes: string[]) {
  return Object.fromEntries(
    prefixes.map((prefix) => [
      prefix,
      {
        target: `http://localhost:${port}`,
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/api/, ''),
      },
    ]),
  )
}

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // 在 frontend/.env.local 里写 VITE_USE_GATEWAY=1 可切回「全部走网关」的原模式
  const useGateway = env.VITE_USE_GATEWAY === '1'

  return {
    plugins: [vue(), vueDevTools()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      // 8093/8094 留给将来可能恢复的旧前端；8080-8085、8092 是后端，不能占用
      port: 5173,
      proxy: useGateway
        ? {
            /*
             * 全部请求发到 /api/**，由这里转发到网关。
             * 网关按资源前缀路由，并 StripPrefix=1 去掉 /api，所以这里不做 rewrite。
             * 走代理而不是直连 8080，开发时是同源请求，不受 CORS 影响。
             */
            '/api': {
              target: 'http://localhost:8080',
              changeOrigin: true,
            },
          }
        : {
            /*
             * 代理按声明顺序匹配，先命中先赢，所以比 '/api' 更具体的键必须写在它前面。
             * 每个前缀都要显式列出：'/api/attraction' 不会自动覆盖 '/api/attraction_type'。
             * _staff 三条也必须显式写出来：'/api/hotel_staff' 不被任何 '/api/hotel_*' 键覆盖
             * （前缀不同名），漏了就会掉到最下面的 '/api' 兜底、绕到网关去了。
             */
            ...directProxy(DIRECT_PORT.attraction, [
              '/api/attraction_staff',
              '/api/attraction',
              '/api/attraction_type',
              '/api/attraction_comment',
              '/api/ticket_order',
            ]),
            ...directProxy(DIRECT_PORT.hotel, [
              '/api/hotel_staff',
              '/api/hotel_info',
              '/api/room_type',
              '/api/hotel_comment',
              '/api/hotel_reservation',
            ]),
            ...directProxy(DIRECT_PORT.food, [
              '/api/restaurant_staff',
              '/api/restaurant',
              '/api/restaurant_comment',
              '/api/restaurant_reservation',
            ]),
            /*
             * user 服务（8081）。这一组以前全靠下面的 '/api' 兜底绕到网关，
             * 也就是「游客的实名认证/消费记录、管理员的登录与审核台」在 VM 里的
             * Nacos 没起来时一律 503。现在和其它四个模块一样直连。
             *
             * /api/user 与 /api/users 必须都写：vite 的键是前缀匹配（startsWith），
             * 所以 '/api/user' 本来也会吃到 /api/users/** 的请求，但两个都写出来
             * 才不会在读代码时误以为 users 落到了别处。
             */
            ...directProxy(DIRECT_PORT.user, [
              '/api/users',
              '/api/user',
            ]),
            /*
             * 旅游这一组（8084）。之前漏了，travel 的前缀全掉到下面的 '/api' 兜底绕到网关，
             * 于是网关一没重启（比如新加了 /api/consult 路由）这些页面就 404，
             * 而同一台机上的其它三个模块直连、照常好用 —— 很容易只往后端身上想。
             *
             * tour_guide 是导游的登录/注册所在（与 *_staff 同理），漏了它导游就登不进来。
             * travel_guide 与 travel_guide_tag 是两个前缀，必须成对列（'/api/travel_guide'
             * 会先命中 /api/travel_guide_tag 的请求，两个都写才不依赖命中顺序）。
             * group_tour 是管理端「报团信息」那一页在用。
             */
            ...directProxy(DIRECT_PORT.travel, [
              '/api/tour_guide',
              '/api/travel_route',
              '/api/travel_guide',
              '/api/travel_guide_tag',
              '/api/consult',
              '/api/group_tour',
            ]),
            // 兜底：其余 /api/** 仍交给网关。现在只剩 /api/agent/**（AI 行程助手）这类
            // 没有列在上面、也没有本机端口的请求 —— 它本来就依赖网关。
            '/api': {
              target: 'http://localhost:8080',
              changeOrigin: true,
            },
          },
    },
  }
})
