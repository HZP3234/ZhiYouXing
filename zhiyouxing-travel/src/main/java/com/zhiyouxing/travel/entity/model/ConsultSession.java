package com.zhiyouxing.travel.entity.model;

import java.io.Serializable;

/**
 * 导游端「咨询消息」列表里的一行。
 *
 * <p>为什么不直接返回 ConsultConversation 让前端自己取最后一条：
 * 那个对象带着整个 messages 数组，导游开着列表页每 3 秒轮询一次，
 * 十来个会话就把几十上百条消息反复传一遍 —— 列表只需要最后一条和未读数。
 */
public class ConsultSession implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 线路id */
    private Long routeId;

    /** 线路名称 */
    private String routeName;

    /** 游客账号（手机号），会话的另一端 */
    private String userAccount;

    /** 最后一条消息的正文，列表上做预览 */
    private String lastContent;

    /** 最后一条消息的发送方："user" / "guide" */
    private String lastFrom;

    /** 最后一条消息的时间，epoch 毫秒 */
    private long lastTime;

    /** 未读条数：游客发的、时间晚于导游已读点的消息数 */
    private int unread;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getLastContent() {
        return lastContent;
    }

    public void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }

    public String getLastFrom() {
        return lastFrom;
    }

    public void setLastFrom(String lastFrom) {
        this.lastFrom = lastFrom;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }
}
