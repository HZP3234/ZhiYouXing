package com.zhiyouxing.travel.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.model.ConsultConversation;
import com.zhiyouxing.travel.entity.model.ConsultMessage;
import com.zhiyouxing.travel.entity.model.ConsultSession;
import com.zhiyouxing.travel.service.ConsultService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 咨询消息的内存中转（需求要求不落库，见 ConsultService 的说明）。
 *
 * <p>为什么是进程内 Map 而不是 Redis：这只是一个演示性质的「两端在同一套后端上
 * 就能互通」的通道，消息量小、且本来就允许随服务重启丢失（真正的留存靠前端
 * localStorage）。引入 Redis 只会多一个要维护的中间件，换不来实际好处。
 * 代价也说清楚：多实例部署时两个游客可能落到不同实例、互相看不到 —— 本项目单实例，
 * 不构成问题。
 *
 * <p>线程安全：外层 Map 用 ConcurrentHashMap；对单个会话的「追加消息 + 推已读点」
 * 是复合操作，所以额外在该会话对象上同步。粒度是会话级，不同会话之间不互相阻塞。
 */
@Service("consultService")
public class ConsultServiceImpl implements ConsultService {

    /** 键：routeId + ":" + userAccount。两端用同一套键，才能落到同一个会话上 */
    private final Map<String, ConsultConversation> conversations = new ConcurrentHashMap<>();

    private static String key(Long routeId, String userAccount) {
        return routeId + ":" + userAccount;
    }

    @Override
    public ConsultMessage send(TravelRouteEntity route, String userAccount, String content, String from) {
        String k = key(route.getId(), userAccount);
        ConsultConversation conv = conversations.computeIfAbsent(k, ignored -> {
            ConsultConversation created = new ConsultConversation();
            created.setRouteId(route.getId());
            created.setUserAccount(userAccount);
            created.setGuideNo(route.getGuideNo());
            created.setRouteName(route.getRouteName());
            created.setGuideName(route.getGuideName());
            return created;
        });

        ConsultMessage msg = new ConsultMessage(
                IdUtil.fastSimpleUUID(), from, content, System.currentTimeMillis());
        synchronized (conv) {
            conv.getMessages().add(msg);
        }
        return msg;
    }

    @Override
    public ConsultConversation history(Long routeId, String userAccount, boolean asGuide) {
        ConsultConversation conv = conversations.get(key(routeId, userAccount));
        if (conv == null) {
            return null;
        }
        synchronized (conv) {
            // 已读点推到「当前最新一条消息的时间」。用消息时间而不是 now()：
            // now() 会把这一瞬间之后才到达的消息也一并标成已读，未读数就永远归零了。
            long latest = 0L;
            for (ConsultMessage m : conv.getMessages()) {
                if (m.getTime() > latest) {
                    latest = m.getTime();
                }
            }
            if (asGuide) {
                conv.setGuideReadTime(Math.max(conv.getGuideReadTime(), latest));
            } else {
                conv.setUserReadTime(Math.max(conv.getUserReadTime(), latest));
            }
            // 复制一份再返回：下面还要读 messages，不复制的话调用方序列化到一半
            // 另一边正好在 append，会抛 ConcurrentModificationException
            return copyOf(conv);
        }
    }

    @Override
    public List<ConsultSession> conversations(String guideNo) {
        List<ConsultSession> result = new ArrayList<>();
        if (StrUtil.isBlank(guideNo)) {
            return result;
        }
        for (ConsultConversation conv : conversations.values()) {
            if (!guideNo.equals(conv.getGuideNo())) {
                continue;
            }
            ConsultSession s = new ConsultSession();
            s.setRouteId(conv.getRouteId());
            s.setRouteName(conv.getRouteName());
            s.setUserAccount(conv.getUserAccount());

            synchronized (conv) {
                List<ConsultMessage> msgs = conv.getMessages();
                if (msgs.isEmpty()) {
                    continue;
                }
                ConsultMessage last = msgs.get(msgs.size() - 1);
                s.setLastContent(last.getContent());
                s.setLastFrom(last.getFrom());
                s.setLastTime(last.getTime());

                int unread = 0;
                for (ConsultMessage m : msgs) {
                    // 只数「游客发的、导游还没读到的」
                    if ("user".equals(m.getFrom()) && m.getTime() > conv.getGuideReadTime()) {
                        unread++;
                    }
                }
                s.setUnread(unread);
            }
            result.add(s);
        }
        result.sort(Comparator.comparingLong(ConsultSession::getLastTime).reversed());
        return result;
    }

    /** 会话的浅拷贝：messages 另起一个 List，消息对象本身不可变、可以直接共享引用 */
    private static ConsultConversation copyOf(ConsultConversation src) {
        ConsultConversation dst = new ConsultConversation();
        dst.setRouteId(src.getRouteId());
        dst.setUserAccount(src.getUserAccount());
        dst.setGuideNo(src.getGuideNo());
        dst.setRouteName(src.getRouteName());
        dst.setGuideName(src.getGuideName());
        dst.setUserReadTime(src.getUserReadTime());
        dst.setGuideReadTime(src.getGuideReadTime());
        dst.setMessages(new ArrayList<>(src.getMessages()));
        return dst;
    }
}
