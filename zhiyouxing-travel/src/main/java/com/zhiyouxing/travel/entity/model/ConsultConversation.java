package com.zhiyouxing.travel.entity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一条咨询会话：某位游客就某条线路与该线路导游之间的对话。
 *
 * <p>同样**不是数据库实体**（见 ConsultMessage 的说明）。会话在内存里以
 * {@code routeId + ":" + userAccount} 为键保存，见 ConsultServiceImpl。
 *
 * <p>routeName / guideName 存的是**首次发消息那一刻的快照**：导游端会话列表要显示
 * 「哪条线路、哪个游客」，若每次都回表查，线路被删掉之后会话列表就会变成一行空白。
 * 快照下来之后，即使线路没了，历史对话仍然读得懂。
 */
public class ConsultConversation implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 线路id（travel_route.id） */
    private Long routeId;

    /** 游客账号（user 表的 username，即手机号）。会话的另一端是线路的 guide_no */
    private String userAccount;

    /**
     * 线路导游工号（travel_route.guide_no），首次建会话时快照。
     *
     * <p>存下来是为了让导游端列表能**直接按工号筛**：列表页每 3 秒轮询一次，
     * 若每个会话都回 travel_route 表查一次 guide_no，十来个会话就是十几次查询，
     * 而且线路被删之后那些会话会凭空从导游列表里消失。快照下来两个问题都没有。
     */
    private String guideNo;

    /** 线路名称，首次建会话时快照 */
    private String routeName;

    /** 导游姓名，首次建会话时快照 */
    private String guideName;

    /** 游客已读到的时间点（epoch 毫秒）：time 不晚于它的导游消息算已读 */
    private long userReadTime;

    /** 导游已读到的时间点（epoch 毫秒） */
    private long guideReadTime;

    /** 按时间升序的完整消息列表 */
    private List<ConsultMessage> messages = new ArrayList<>();

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getGuideNo() {
        return guideNo;
    }

    public void setGuideNo(String guideNo) {
        this.guideNo = guideNo;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public long getUserReadTime() {
        return userReadTime;
    }

    public void setUserReadTime(long userReadTime) {
        this.userReadTime = userReadTime;
    }

    public long getGuideReadTime() {
        return guideReadTime;
    }

    public void setGuideReadTime(long guideReadTime) {
        this.guideReadTime = guideReadTime;
    }

    public List<ConsultMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ConsultMessage> messages) {
        this.messages = messages;
    }
}
