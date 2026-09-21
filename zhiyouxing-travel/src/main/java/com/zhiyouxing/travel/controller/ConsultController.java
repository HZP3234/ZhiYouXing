package com.zhiyouxing.travel.controller;

import cn.hutool.core.util.StrUtil;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.model.ConsultConversation;
import com.zhiyouxing.travel.entity.model.ConsultMessage;
import com.zhiyouxing.travel.service.ConsultService;
import com.zhiyouxing.travel.service.TravelRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线路咨询：游客 ↔ 线路导游的双向对话。
 *
 * <p>这套接口**没有 @IgnoreAuth**：两端都要有身份才谈得上「谁在跟谁说」，
 * 所以一律过 AuthorizationInterceptor。身份不重新解析 —— 拦截器已经把
 * user_id / username / table_name / role 写进会话，这里直接读：
 * 游客 table_name="user" 且 username 是手机号，导游 table_name="tour_guide"
 * 且 username 是工号（同时就是 travel_route.guide_no，见 TourGuideController.login）。
 *
 * <p>消息本身不落库（见 ConsultService），前端靠 2 秒轮询 /history 取增量，
 * 另把历史存进 localStorage 做留存。所以这里没有「已送达」这类回执，
 * 消息进了内存就算送到了 —— 服务重启会丢，这是需求明确接受的。
 */
@RestController
@RequestMapping("/consult")
public class ConsultController {

    /** 会话表里 sender 的两个取值，前端据此决定气泡靠左还是靠右 */
    private static final String FROM_USER = "user";
    private static final String FROM_GUIDE = "guide";

    private static final String TABLE_USER = "user";
    private static final String TABLE_GUIDE = "tour_guide";

    /** 单条消息长度上限。不落库也拦一道，避免把超长文本塞进内存与前端 localStorage */
    private static final int MAX_CONTENT = 500;

    @Autowired
    private ConsultService consultService;

    @Autowired
    private TravelRouteService travelRouteService;

    /**
     * 发一条消息。
     *
     * <p>body：{ routeId, content, userAccount? }
     * userAccount 只有导游端要传（回给哪个游客）；游客端传了也忽略 ——
     * 游客的身份一律以会话里的 username 为准，不然可以伪造成别人的账号发消息。
     */
    @PostMapping("/send")
    public R send(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String tableName = (String) request.getSession().getAttribute("table_name");
        String username = (String) request.getSession().getAttribute("username");

        Long routeId = toLong(body.get("routeId"));
        if (routeId == null) {
            return R.error("缺少线路id");
        }
        String content = StrUtil.trim(body.get("content") == null ? null : body.get("content").toString());
        if (StrUtil.isBlank(content)) {
            return R.error("消息内容不能为空");
        }
        if (content.length() > MAX_CONTENT) {
            return R.error("消息太长了，请控制在 " + MAX_CONTENT + " 字以内");
        }

        TravelRouteEntity route = travelRouteService.getById(routeId);
        if (route == null) {
            return R.error("线路不存在");
        }

        String from;
        String userAccount;
        if (TABLE_USER.equals(tableName)) {
            from = FROM_USER;
            userAccount = username;
        } else if (TABLE_GUIDE.equals(tableName)) {
            from = FROM_GUIDE;
            userAccount = StrUtil.trim(str(body.get("userAccount")));
            if (StrUtil.isBlank(userAccount)) {
                return R.error("缺少咨询对象");
            }
            /*
             * 导游只能回自己线路的咨询。不校验的话，任何导游登录后改个 routeId
             * 就能读到、并插话进别人的咨询会话 —— 会话键里没有导游，一旦写进去
             * 就再也分不出这条是哪个导游说的。
             */
            if (!username.equals(route.getGuideNo())) {
                return R.error("无权回复该线路的咨询");
            }
        } else {
            return R.error(401, "请先登录");
        }

        consultService.send(route, userAccount, content, from);
        return R.ok();
    }

    /**
     * 取会话的消息列表，并把请求方标成已读。
     *
     * <p>params：{ routeId, userAccount? } —— 同 /send，userAccount 只有导游端要传。
     * 会话不存在时返回线路信息 + 空列表，而不是报错：游客第一次点开咨询窗口时
     * 还没有任何消息，前端要靠这里拿到的线路名/导游名渲染窗口标题。
     */
    @GetMapping("/history")
    public R history(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        String tableName = (String) request.getSession().getAttribute("table_name");
        String username = (String) request.getSession().getAttribute("username");

        Long routeId = toLong(params.get("routeId"));
        if (routeId == null) {
            return R.error("缺少线路id");
        }
        TravelRouteEntity route = travelRouteService.getById(routeId);
        if (route == null) {
            return R.error("线路不存在");
        }

        String userAccount;
        boolean asGuide;
        if (TABLE_USER.equals(tableName)) {
            userAccount = username;
            asGuide = false;
        } else if (TABLE_GUIDE.equals(tableName)) {
            userAccount = str(params.get("userAccount"));
            asGuide = true;
            if (StrUtil.isBlank(userAccount)) {
                return R.error("缺少咨询对象");
            }
            if (!username.equals(route.getGuideNo())) {
                return R.error("无权查看该线路的咨询");
            }
        } else {
            return R.error(401, "请先登录");
        }

        ConsultConversation conv = consultService.history(routeId, userAccount, asGuide);
        Map<String, Object> data = new HashMap<>();
        data.put("routeId", route.getId());
        data.put("routeName", route.getRouteName());
        data.put("guideName", route.getGuideName());
        data.put("userAccount", userAccount);
        data.put("messages", conv == null ? new ArrayList<ConsultMessage>() : conv.getMessages());
        return R.ok().put("data", data);
    }

    /**
     * 导游端的会话列表：自己名下所有线路的咨询，新的在前，带未读数。
     */
    @GetMapping("/conversations")
    public R conversations(HttpServletRequest request) {
        String tableName = (String) request.getSession().getAttribute("table_name");
        String username = (String) request.getSession().getAttribute("username");
        if (!TABLE_GUIDE.equals(tableName)) {
            return R.error("只有导游可以查看咨询列表");
        }
        List<?> list = consultService.conversations(username);
        return R.ok().put("data", list);
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    /** 前端 JSON 里的 id 可能是数字也可能是字符串，统一收成 Long */
    private static Long toLong(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return Long.valueOf(o.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
