package com.zhiyouxing.travel.service;

import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.model.ConsultConversation;
import com.zhiyouxing.travel.entity.model.ConsultMessage;
import com.zhiyouxing.travel.entity.model.ConsultSession;

import java.util.List;

/**
 * 线路咨询：游客与线路导游之间的对话中转。
 *
 * <p>按需求**不落数据库**，消息全部存在实现类的内存 Map 里，服务重启即清空。
 * 真正的「留存」由前端 localStorage 承担（见 components/ConsultChat.vue）——
 * 这里只管「这一刻能不能把消息从一端送到另一端」，前端轮询取。
 *
 * <p>鉴权不在这层做：调用方（ConsultController）已经从会话里拿到了身份，
 * 也校验过「这个导游是不是这条线路的导游」，传进来的参数是可信的。
 */
public interface ConsultService {

    /**
     * 追加一条消息。会话不存在就按 route 的信息新建。
     *
     * @param route       会话所属线路，用来快照线路名与导游工号
     * @param userAccount 游客账号（手机号），会话的另一端
     * @param content     消息正文
     * @param from        "user" 或 "guide"
     */
    ConsultMessage send(TravelRouteEntity route, String userAccount, String content, String from);

    /**
     * 取一条会话的完整消息列表，并把**请求方的已读点**推到最新。
     *
     * @param asGuide true=导游在读（推 guideReadTime），false=游客在读（推 userReadTime）
     * @return 会话；从未有过消息时返回 null，调用方按「空会话」处理
     */
    ConsultConversation history(Long routeId, String userAccount, boolean asGuide);

    /** 某导游名下所有会话，按最后一条消息的时间倒序（新的在前） */
    List<ConsultSession> conversations(String guideNo);
}
